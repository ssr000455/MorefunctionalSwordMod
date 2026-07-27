#include "dobby.h"
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/mman.h>
#include <pthread.h>

static DobbyHookEntry* g_hook_list = NULL;
static pthread_mutex_t g_hook_lock = PTHREAD_MUTEX_INITIALIZER;
static DobbyLogCallback g_log_callback = NULL;

static void dobby_log(const char* fmt, ...) {
    char buffer[512];
    va_list args;
    va_start(args, fmt);
    vsnprintf(buffer, sizeof(buffer), fmt, args);
    va_end(args);
    if (g_log_callback) g_log_callback(buffer);
    else printf("[Dobby] %s\n", buffer);
}

static long get_page_size(void) {
    static long page_size = 0;
    if (!page_size) page_size = sysconf(_SC_PAGESIZE);
    return page_size;
}

static void* align_to_page(void* addr) {
    return (void*)((uintptr_t)addr & ~(get_page_size() - 1));
}

static int set_memory_permission(void* addr, size_t size, int prot) {
    void* page = align_to_page(addr);
    size_t len = ((uintptr_t)addr + size - (uintptr_t)page + get_page_size() - 1) & ~(get_page_size() - 1);
    if (mprotect(page, len, prot) != 0) {
        DOBBY_LOG("mprotect failed for %p", addr);
        return -1;
    }
    return 0;
}

static void flush_instruction_cache(void* addr, size_t size) {
#if defined(__arm__) || defined(__aarch64__)
    __clear_cache(addr, (void*)((uintptr_t)addr + size));
#elif defined(__x86_64__) || defined(__i386__)
    // x86 doesn't need explicit flush
#else
    __builtin___clear_cache((char*)addr, (char*)addr + size);
#endif
}

static DobbyHookEntry* find_hook_entry(void* target) {
    DobbyHookEntry* e = g_hook_list;
    while (e) {
        if (e->target == target) return e;
        e = e->next;
    }
    return NULL;
}

static void add_hook_entry(DobbyHookEntry* entry) {
    pthread_mutex_lock(&g_hook_lock);
    entry->next = g_hook_list;
    g_hook_list = entry;
    pthread_mutex_unlock(&g_hook_lock);
}

static void remove_hook_entry(void* target) {
    pthread_mutex_lock(&g_hook_lock);
    DobbyHookEntry** pp = &g_hook_list;
    while (*pp) {
        if ((*pp)->target == target) {
            DobbyHookEntry* entry = *pp;
            *pp = entry->next;
            if (entry->original_bytes) free(entry->original_bytes);
            free(entry);
            break;
        }
        pp = &(*pp)->next;
    }
    pthread_mutex_unlock(&g_hook_lock);
}

#if defined(__aarch64__)
typedef uint32_t arm64_insn_t;

static int arm64_install_hook(void* target, void* replace, void** origin) {
    if (!target || !replace) return DOBBY_ERROR_INVALID_TARGET;
    if (find_hook_entry(target)) return DOBBY_ERROR_ALREADY_HOOKED;
    if (set_memory_permission(target, 16, PROT_READ | PROT_WRITE | PROT_EXEC)) return DOBBY_ERROR_PERMISSION;

    arm64_insn_t* addr = (arm64_insn_t*)target;
    uintptr_t t = (uintptr_t)target, r = (uintptr_t)replace;

    // allocate entry
    DobbyHookEntry* entry = calloc(1, sizeof(DobbyHookEntry));
    if (!entry) return DOBBY_ERROR_MEMORY_ALLOC;
    entry->target = target;
    entry->replace = replace;
    entry->enabled = 1;
    entry->original_size = 8;
    entry->original_bytes = malloc(entry->original_size);
    if (!entry->original_bytes) { free(entry); return DOBBY_ERROR_MEMORY_ALLOC; }
    memcpy(entry->original_bytes, addr, entry->original_size);

    // trampoline
    if (origin) {
        void* tramp = DobbyAllocExecutableMemory(32);
        if (!tramp) { free(entry->original_bytes); free(entry); return DOBBY_ERROR_MEMORY_ALLOC; }
        entry->origin = tramp;
        arm64_insn_t* tr = (arm64_insn_t*)tramp;
        tr[0] = ((arm64_insn_t*)entry->original_bytes)[0];
        tr[1] = ((arm64_insn_t*)entry->original_bytes)[1];
        uintptr_t ret = t + 8;
        int64_t off = (ret - ((uintptr_t)tr + 12)) >> 2; // PC = tramp+8, 我们写在第3条指令，PC=tr+8? 实际B偏移相对PC=tr+8+4=tr+12，所以减12
        if (off >= -0x03FFFFFF && off <= 0x03FFFFFF)
            tr[2] = 0x14000000 | (0x03FFFFFF & off);
        else {
            // long jump: ADRP + ADD + BR
            tr[2] = 0x90000000 | (((ret >> 12) & 0x1FFFFF) << 5);
            tr[3] = 0x91BE0210 | ((ret & 0xFFF) << 10);
            tr[4] = 0xD61F0200;
        }
        flush_instruction_cache(tramp, 32);
        *origin = tramp;
    }

    // write jump
    int64_t off = (r - t - 4) >> 2; // PC = t + 4
    if (off >= -0x03FFFFFF && off <= 0x03FFFFFF) {
        addr[0] = 0x14000000 | (0x03FFFFFF & off);
    } else {
        // long jump: ADRP + ADD + BR
        uintptr_t page = r & ~0xFFF;
        int64_t page_off = (page - (t & ~0xFFF)) >> 12;
        addr[0] = 0x90000000 | ((page_off & 0x1FFFFF) << 5);
        addr[1] = 0x91BE0210 | ((r & 0xFFF) << 10);
        addr[2] = 0xD61F0200;
    }
    flush_instruction_cache(target, 16);
    add_hook_entry(entry);
    DOBBY_LOG("ARM64 hook installed at %p -> %p", target, replace);
    return DOBBY_SUCCESS;
}

static int arm64_unhook(void* target) {
    DobbyHookEntry* entry = find_hook_entry(target);
    if (!entry) return DOBBY_ERROR_NOT_HOOKED;
    if (set_memory_permission(target, 16, PROT_READ | PROT_WRITE | PROT_EXEC))
        return DOBBY_ERROR_PERMISSION;
    memcpy(target, entry->original_bytes, entry->original_size);
    flush_instruction_cache(target, entry->original_size);
    if (entry->origin) DobbyFreeExecutableMemory(entry->origin, 32);
    remove_hook_entry(target);
    DOBBY_LOG("ARM64 unhook at %p", target);
    return DOBBY_SUCCESS;
}

#elif defined(__arm__)
typedef uint32_t arm32_insn_t;
static int arm32_install_hook(void* target, void* replace, void** origin) {
    if (!target || !replace) return DOBBY_ERROR_INVALID_TARGET;
    if (find_hook_entry(target)) return DOBBY_ERROR_ALREADY_HOOKED;
    if (set_memory_permission(target, 8, PROT_READ | PROT_WRITE | PROT_EXEC)) return DOBBY_ERROR_PERMISSION;

    arm32_insn_t* addr = (arm32_insn_t*)target;
    uintptr_t t = (uintptr_t)target, r = (uintptr_t)replace;

    DobbyHookEntry* entry = calloc(1, sizeof(DobbyHookEntry));
    if (!entry) return DOBBY_ERROR_MEMORY_ALLOC;
    entry->target = target;
    entry->replace = replace;
    entry->enabled = 1;
    entry->original_size = 8;
    entry->original_bytes = malloc(entry->original_size);
    if (!entry->original_bytes) { free(entry); return DOBBY_ERROR_MEMORY_ALLOC; }
    memcpy(entry->original_bytes, addr, entry->original_size);

    if (origin) {
        void* tramp = DobbyAllocExecutableMemory(24);
        if (!tramp) { free(entry->original_bytes); free(entry); return DOBBY_ERROR_MEMORY_ALLOC; }
        entry->origin = tramp;
        arm32_insn_t* tr = (arm32_insn_t*)tramp;
        tr[0] = ((arm32_insn_t*)entry->original_bytes)[0];
        tr[1] = ((arm32_insn_t*)entry->original_bytes)[1];
        tr[2] = 0xE51FF004; // LDR PC, [PC, #-4]
        tr[3] = (arm32_insn_t)(t + 8);
        flush_instruction_cache(tramp, 24);
        *origin = tramp;
    }

    int32_t off = (r - t - 8) >> 2;
    addr[0] = 0xEA000000 | (0x00FFFFFF & off);
    flush_instruction_cache(target, 8);
    add_hook_entry(entry);
    DOBBY_LOG("ARM32 hook installed at %p -> %p", target, replace);
    return DOBBY_SUCCESS;
}

static int arm32_unhook(void* target) {
    DobbyHookEntry* entry = find_hook_entry(target);
    if (!entry) return DOBBY_ERROR_NOT_HOOKED;
    if (set_memory_permission(target, 8, PROT_READ | PROT_WRITE | PROT_EXEC))
        return DOBBY_ERROR_PERMISSION;
    memcpy(target, entry->original_bytes, entry->original_size);
    flush_instruction_cache(target, entry->original_size);
    if (entry->origin) DobbyFreeExecutableMemory(entry->origin, 24);
    remove_hook_entry(target);
    DOBBY_LOG("ARM32 unhook at %p", target);
    return DOBBY_SUCCESS;
}

#elif defined(__x86_64__) || defined(__i386__)
static int x86_install_hook(void* target, void* replace, void** origin) {
    if (!target || !replace) return DOBBY_ERROR_INVALID_TARGET;
    if (find_hook_entry(target)) return DOBBY_ERROR_ALREADY_HOOKED;
    if (set_memory_permission(target, 16, PROT_READ | PROT_WRITE | PROT_EXEC)) return DOBBY_ERROR_PERMISSION;

    uint8_t* addr = (uint8_t*)target;
    uintptr_t t = (uintptr_t)target, r = (uintptr_t)replace;

    DobbyHookEntry* entry = calloc(1, sizeof(DobbyHookEntry));
    if (!entry) return DOBBY_ERROR_MEMORY_ALLOC;
    entry->target = target;
    entry->replace = replace;
    entry->enabled = 1;
    entry->original_size = 5;
    entry->original_bytes = malloc(entry->original_size);
    if (!entry->original_bytes) { free(entry); return DOBBY_ERROR_MEMORY_ALLOC; }
    memcpy(entry->original_bytes, addr, entry->original_size);

    if (origin) {
        void* tramp = DobbyAllocExecutableMemory(32);
        if (!tramp) { free(entry->original_bytes); free(entry); return DOBBY_ERROR_MEMORY_ALLOC; }
        entry->origin = tramp;
        uint8_t* tr = (uint8_t*)tramp;
        memcpy(tr, addr, 5);
        tr[5] = 0xE9;
        int32_t off = (t + 5) - ((uintptr_t)tr + 10);
        memcpy(&tr[6], &off, 4);
        flush_instruction_cache(tramp, 32);
        *origin = tramp;
    }

    addr[0] = 0xE9;
    int32_t off = (r - t) - 5;
    memcpy(&addr[1], &off, 4);
    flush_instruction_cache(target, 16);
    add_hook_entry(entry);
    DOBBY_LOG("x86 hook installed at %p -> %p", target, replace);
    return DOBBY_SUCCESS;
}

static int x86_unhook(void* target) {
    DobbyHookEntry* entry = find_hook_entry(target);
    if (!entry) return DOBBY_ERROR_NOT_HOOKED;
    if (set_memory_permission(target, 16, PROT_READ | PROT_WRITE | PROT_EXEC))
        return DOBBY_ERROR_PERMISSION;
    memcpy(target, entry->original_bytes, entry->original_size);
    flush_instruction_cache(target, entry->original_size);
    if (entry->origin) DobbyFreeExecutableMemory(entry->origin, 32);
    remove_hook_entry(target);
    DOBBY_LOG("x86 unhook at %p", target);
    return DOBBY_SUCCESS;
}
#else
static int unsupported_install_hook(void* target, void* replace, void** origin) {
    DOBBY_LOG("Unsupported architecture");
    return DOBBY_ERROR_UNSUPPORTED_ARCH;
}
static int unsupported_unhook(void* target) {
    DOBBY_LOG("Unsupported architecture");
    return DOBBY_ERROR_UNSUPPORTED_ARCH;
}
#endif

int DobbyHook(void* target, void* replace, void** origin) {
    DOBBY_LOG("DobbyHook: %p -> %p", target, replace);
    if (!target) return DOBBY_ERROR_INVALID_TARGET;
    if (!replace) return DOBBY_ERROR_INVALID_REPLACE;
    if (DobbyIsHooked(target)) return DOBBY_ERROR_ALREADY_HOOKED;
#if defined(__aarch64__)
    return arm64_install_hook(target, replace, origin);
#elif defined(__arm__)
    return arm32_install_hook(target, replace, origin);
#elif defined(__x86_64__) || defined(__i386__)
    return x86_install_hook(target, replace, origin);
#else
    return unsupported_install_hook(target, replace, origin);
#endif
}

int DobbyUnhook(void* target) {
    DOBBY_LOG("DobbyUnhook: %p", target);
    if (!target) return DOBBY_ERROR_INVALID_TARGET;
#if defined(__aarch64__)
    return arm64_unhook(target);
#elif defined(__arm__)
    return arm32_unhook(target);
#elif defined(__x86_64__) || defined(__i386__)
    return x86_unhook(target);
#else
    return unsupported_unhook(target);
#endif
}

int DobbyDisableHook(void* target) {
    DobbyHookEntry* entry = find_hook_entry(target);
    if (!entry) return DOBBY_ERROR_NOT_HOOKED;
    if (!entry->enabled) return DOBBY_SUCCESS;
    if (set_memory_permission(target, 16, PROT_READ | PROT_WRITE | PROT_EXEC))
        return DOBBY_ERROR_PERMISSION;
    memcpy(target, entry->original_bytes, entry->original_size);
    flush_instruction_cache(target, entry->original_size);
    entry->enabled = 0;
    DOBBY_LOG("Hook disabled at %p", target);
    return DOBBY_SUCCESS;
}

int DobbyEnableHook(void* target) {
    DobbyHookEntry* entry = find_hook_entry(target);
    if (!entry) return DOBBY_ERROR_NOT_HOOKED;
    if (entry->enabled) return DOBBY_SUCCESS;
    if (set_memory_permission(target, 16, PROT_READ | PROT_WRITE | PROT_EXEC))
        return DOBBY_ERROR_PERMISSION;

    // re-install the jump using same logic as install
    void* replace = entry->replace;
#if defined(__aarch64__)
    uint32_t* addr = (uint32_t*)target;
    uintptr_t t = (uintptr_t)target, r = (uintptr_t)replace;
    int64_t off = (r - t - 4) >> 2;
    if (off >= -0x03FFFFFF && off <= 0x03FFFFFF) {
        addr[0] = 0x14000000 | (0x03FFFFFF & off);
    } else {
        uintptr_t page = r & ~0xFFF;
        int64_t page_off = (page - (t & ~0xFFF)) >> 12;
        addr[0] = 0x90000000 | ((page_off & 0x1FFFFF) << 5);
        addr[1] = 0x91BE0210 | ((r & 0xFFF) << 10);
        addr[2] = 0xD61F0200;
    }
#elif defined(__arm__)
    uint32_t* addr = (uint32_t*)target;
    uintptr_t t = (uintptr_t)target, r = (uintptr_t)replace;
    int32_t off = (r - t - 8) >> 2;
    addr[0] = 0xEA000000 | (0x00FFFFFF & off);
#elif defined(__x86_64__) || defined(__i386__)
    uint8_t* addr = (uint8_t*)target;
    uintptr_t t = (uintptr_t)target, r = (uintptr_t)replace;
    addr[0] = 0xE9;
    int32_t off = (r - t) - 5;
    memcpy(&addr[1], &off, 4);
#endif
    flush_instruction_cache(target, 16);
    entry->enabled = 1;
    DOBBY_LOG("Hook enabled at %p", target);
    return DOBBY_SUCCESS;
}

void* DobbyCopyCode(void* address, size_t size) {
    if (!address || !size) return NULL;
    void* new_addr = DobbyAllocExecutableMemory(size);
    if (!new_addr) return NULL;
    memcpy(new_addr, address, size);
    flush_instruction_cache(new_addr, size);
    return new_addr;
}

void* DobbyAllocExecutableMemory(size_t size) {
    void* addr = mmap(NULL, size, PROT_READ | PROT_WRITE | PROT_EXEC,
                      MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    return (addr == MAP_FAILED) ? NULL : addr;
}

void DobbyFreeExecutableMemory(void* address, size_t size) {
    if (address && size) munmap(address, size);
}

int DobbyResetHook(void* target) {
    DobbyHookEntry* entry = find_hook_entry(target);
    if (!entry) return DOBBY_ERROR_NOT_HOOKED;
    void* replace = entry->replace;
    int ret = DobbyUnhook(target);
    if (ret != DOBBY_SUCCESS) return ret;
    return DobbyHook(target, replace, NULL);
}

bool DobbyIsHooked(void* target) {
    return find_hook_entry(target) != NULL;
}

int DobbyGetHookInfo(void* target, void** replace, void** origin) {
    DobbyHookEntry* e = find_hook_entry(target);
    if (!e) return DOBBY_ERROR_NOT_HOOKED;
    if (replace) *replace = e->replace;
    if (origin) *origin = e->origin;
    return DOBBY_SUCCESS;
}

void DobbySetLogCallback(DobbyLogCallback cb) { g_log_callback = cb; }
DobbyHookEntry* DobbyGetHookList(void) { return g_hook_list; }

__attribute__((constructor)) static void init() { DOBBY_LOG("Dobby Hook initialized"); }
__attribute__((destructor)) static void fini() {
    DOBBY_LOG("Dobby Hook shutting down");
    while (g_hook_list) {
        DobbyHookEntry* e = g_hook_list;
        g_hook_list = e->next;
        if (e->original_bytes) free(e->original_bytes);
        if (e->origin) DobbyFreeExecutableMemory(e->origin, 32);
        free(e);
    }
}