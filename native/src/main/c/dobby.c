// ================================================================
// 文件名: dobby.c
// 用途: Dobby Hook 完整实现
// 支持: ARM64 / ARM32 / x86_64
// ================================================================

#include "dobby.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/mman.h>
#include <pthread.h>

// ================================================================
// 内部全局状态
// ================================================================

static DobbyHookEntry* g_hook_list = NULL;
static pthread_mutex_t g_hook_lock = PTHREAD_MUTEX_INITIALIZER;
static DobbyLogCallback g_log_callback = NULL;

// ================================================================
// 内部日志函数
// ================================================================

static void dobby_log(const char* fmt, ...) {
    char buffer[512];
    va_list args;
    va_start(args, fmt);
    vsnprintf(buffer, sizeof(buffer), fmt, args);
    va_end(args);
    
    if (g_log_callback != NULL) {
        g_log_callback(buffer);
    } else {
        printf("[Dobby] %s\n", buffer);
    }
}

// ================================================================
// 内部辅助函数
// ================================================================

static long get_page_size(void) {
    static long page_size = 0;
    if (page_size == 0) {
        page_size = sysconf(_SC_PAGESIZE);
    }
    return page_size;
}

static void* align_to_page(void* address) {
    long page_size = get_page_size();
    return (void*)((uintptr_t)address & ~(page_size - 1));
}

static bool is_executable(void* address) {
    // 简单检查: 尝试读取并执行
    // 实际应该检查内存映射的权限，这里简化处理
    return address != NULL;
}

static int set_memory_permission(void* address, size_t size, int prot) {
    void* page_start = align_to_page(address);
    size_t page_size = get_page_size();
    size_t aligned_size = ((uintptr_t)address + size - (uintptr_t)page_start + page_size - 1) & ~(page_size - 1);
    
    if (mprotect(page_start, aligned_size, prot) != 0) {
        DOBBY_LOG("mprotect failed for address %p", address);
        return -1;
    }
    return 0;
}

static void flush_instruction_cache(void* address, size_t size) {
#if defined(__arm__) || defined(__aarch64__)
    __clear_cache(address, (void*)((uintptr_t)address + size));
#elif defined(__x86_64__) || defined(__i386__)
    // x86 不需要显式刷新缓存
#else
    // 通用方式
    __builtin___clear_cache((char*)address, (char*)address + size);
#endif
}

// ================================================================
// Hook 链表操作
// ================================================================

static DobbyHookEntry* find_hook_entry(void* target) {
    DobbyHookEntry* entry = g_hook_list;
    while (entry != NULL) {
        if (entry->target == target) {
            return entry;
        }
        entry = entry->next;
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
    while (*pp != NULL) {
        if ((*pp)->target == target) {
            DobbyHookEntry* entry = *pp;
            *pp = entry->next;
            if (entry->original_bytes != NULL) {
                free(entry->original_bytes);
            }
            free(entry);
            break;
        }
        pp = &(*pp)->next;
    }
    pthread_mutex_unlock(&g_hook_lock);
}

// ================================================================
// 架构相关 Hook 实现
// ================================================================

#if defined(__aarch64__)
// ================================================================
// ARM64 架构实现
// ================================================================

typedef uint32_t arm64_insn_t;

static size_t arm64_get_instruction_size(arm64_insn_t insn) {
    // ARM64 所有指令都是 4 字节
    return 4;
}

static int arm64_install_hook(void* target, void* replace, void** origin) {
    if (target == NULL || replace == NULL) {
        return DOBBY_ERROR_INVALID_TARGET;
    }
    
    // 检查是否已 hook
    if (find_hook_entry(target) != NULL) {
        return DOBBY_ERROR_ALREADY_HOOKED;
    }
    
    // 设置内存可写
    if (set_memory_permission(target, 16, PROT_READ | PROT_WRITE | PROT_EXEC) != 0) {
        return DOBBY_ERROR_PERMISSION;
    }
    
    arm64_insn_t* target_addr = (arm64_insn_t*)target;
    uintptr_t target_ptr = (uintptr_t)target;
    uintptr_t replace_ptr = (uintptr_t)replace;
    
    // 计算跳转距离
    int64_t offset = (replace_ptr - target_ptr) >> 2;
    bool is_near = (offset >= -0x03FFFFFF && offset <= 0x03FFFFFF);
    
    // 创建入口
    DobbyHookEntry* entry = (DobbyHookEntry*)calloc(1, sizeof(DobbyHookEntry));
    if (entry == NULL) {
        return DOBBY_ERROR_MEMORY_ALLOC;
    }
    entry->target = target;
    entry->replace = replace;
    entry->enabled = true;
    
    // 保存原始指令 (前 2 条)
    entry->original_size = 8;
    entry->original_bytes = (uint8_t*)malloc(entry->original_size);
    if (entry->original_bytes == NULL) {
        free(entry);
        return DOBBY_ERROR_MEMORY_ALLOC;
    }
    memcpy(entry->original_bytes, target_addr, entry->original_size);
    
    // 如果 origin 不为 NULL，生成 trampoline
    if (origin != NULL) {
        // 分配 trampoline 内存 (2条指令 + 1条跳转 + 对齐)
        size_t tramp_size = 32;
        void* trampoline = DobbyAllocExecutableMemory(tramp_size);
        if (trampoline == NULL) {
            free(entry->original_bytes);
            free(entry);
            return DOBBY_ERROR_MEMORY_ALLOC;
        }
        entry->origin = trampoline;
        
        arm64_insn_t* tramp = (arm64_insn_t*)trampoline;
        
        // 复制原始指令
        tramp[0] = ((arm64_insn_t*)entry->original_bytes)[0];
        tramp[1] = ((arm64_insn_t*)entry->original_bytes)[1];
        
        // 跳转回原函数 + 8 (跳过已复制的指令)
        uintptr_t return_addr = target_ptr + 8;
        int64_t return_offset = (return_addr - ((uintptr_t)tramp + 8)) >> 2;
        
        // B 无条件跳转
        if (return_offset >= -0x03FFFFFF && return_offset <= 0x03FFFFFF) {
            tramp[2] = 0x14000000 | (0x03FFFFFF & return_offset);
        } else {
            // 长跳转: ADRP + BR
            tramp[2] = 0x90000000 | (((return_addr >> 12) & 0x1FFFFF) << 5); // ADRP X16
            tramp[3] = 0x91BE0210 | ((return_addr & 0xFFF) << 10); // ADD X16, X16, #offset
            tramp[4] = 0xD61F0200; // BR X16
        }
        
        flush_instruction_cache(tramp, tramp_size);
        *origin = trampoline;
    }
    
    // 写入跳转指令
    if (is_near) {
        // B 无条件跳转
        target_addr[0] = 0x14000000 | (0x03FFFFFF & offset);
    } else {
        // 长跳转: ADRP + ADD + BR
        uintptr_t page_addr = replace_ptr & ~0xFFF;
        uintptr_t target_page = target_ptr & ~0xFFF;
        int64_t page_offset = (page_addr - target_page) >> 12;
        
        target_addr[0] = 0x90000000 | ((page_offset & 0x1FFFFF) << 5); // ADRP X16
        target_addr[1] = 0x91BE0210 | ((replace_ptr & 0xFFF) << 10); // ADD X16, X16, #offset
        target_addr[2] = 0xD61F0200; // BR X16
    }
    
    flush_instruction_cache(target, 16);
    
    add_hook_entry(entry);
    DOBBY_LOG("ARM64 hook installed at %p -> %p", target, replace);
    
    return DOBBY_SUCCESS;
}

static int arm64_unhook(void* target) {
    DobbyHookEntry* entry = find_hook_entry(target);
    if (entry == NULL) {
        return DOBBY_ERROR_NOT_HOOKED;
    }
    
    if (set_memory_permission(target, 16, PROT_READ | PROT_WRITE | PROT_EXEC) != 0) {
        return DOBBY_ERROR_PERMISSION;
    }
    
    // 恢复原始指令
    memcpy(target, entry->original_bytes, entry->original_size);
    flush_instruction_cache(target, entry->original_size);
    
    // 清理 trampoline
    if (entry->origin != NULL) {
        DobbyFreeExecutableMemory(entry->origin, 32);
    }
    
    remove_hook_entry(target);
    DOBBY_LOG("ARM64 unhook at %p", target);
    
    return DOBBY_SUCCESS;
}

#elif defined(__arm__)
// ================================================================
// ARM32 架构实现
// ================================================================

typedef uint32_t arm32_insn_t;

static int arm32_install_hook(void* target, void* replace, void** origin) {
    if (target == NULL || replace == NULL) {
        return DOBBY_ERROR_INVALID_TARGET;
    }
    
    if (find_hook_entry(target) != NULL) {
        return DOBBY_ERROR_ALREADY_HOOKED;
    }
    
    if (set_memory_permission(target, 8, PROT_READ | PROT_WRITE | PROT_EXEC) != 0) {
        return DOBBY_ERROR_PERMISSION;
    }
    
    arm32_insn_t* target_addr = (arm32_insn_t*)target;
    uintptr_t target_ptr = (uintptr_t)target;
    uintptr_t replace_ptr = (uintptr_t)replace;
    
    DobbyHookEntry* entry = (DobbyHookEntry*)calloc(1, sizeof(DobbyHookEntry));
    if (entry == NULL) {
        return DOBBY_ERROR_MEMORY_ALLOC;
    }
    entry->target = target;
    entry->replace = replace;
    entry->enabled = true;
    
    // 保存原始指令 (前 2 条)
    entry->original_size = 8;
    entry->original_bytes = (uint8_t*)malloc(entry->original_size);
    if (entry->original_bytes == NULL) {
        free(entry);
        return DOBBY_ERROR_MEMORY_ALLOC;
    }
    memcpy(entry->original_bytes, target_addr, entry->original_size);
    
    if (origin != NULL) {
        size_t tramp_size = 24;
        void* trampoline = DobbyAllocExecutableMemory(tramp_size);
        if (trampoline == NULL) {
            free(entry->original_bytes);
            free(entry);
            return DOBBY_ERROR_MEMORY_ALLOC;
        }
        entry->origin = trampoline;
        
        arm32_insn_t* tramp = (arm32_insn_t*)trampoline;
        tramp[0] = ((arm32_insn_t*)entry->original_bytes)[0];
        tramp[1] = ((arm32_insn_t*)entry->original_bytes)[1];
        
        // LDR PC, [PC, #-4] 跳转回原函数
        tramp[2] = 0xE51FF004; // LDR PC, [PC, #-4]
        tramp[3] = (arm32_insn_t)(target_ptr + 8);
        
        flush_instruction_cache(tramp, tramp_size);
        *origin = trampoline;
    }
    
    // B 指令跳转 (ARM 模式)
    int32_t offset = (replace_ptr - target_ptr - 8) >> 2;
    target_addr[0] = 0xEA000000 | (0x00FFFFFF & offset);
    
    flush_instruction_cache(target, 8);
    
    add_hook_entry(entry);
    DOBBY_LOG("ARM32 hook installed at %p -> %p", target, replace);
    
    return DOBBY_SUCCESS;
}

static int arm32_unhook(void* target) {
    DobbyHookEntry* entry = find_hook_entry(target);
    if (entry == NULL) {
        return DOBBY_ERROR_NOT_HOOKED;
    }
    
    if (set_memory_permission(target, 8, PROT_READ | PROT_WRITE | PROT_EXEC) != 0) {
        return DOBBY_ERROR_PERMISSION;
    }
    
    memcpy(target, entry->original_bytes, entry->original_size);
    flush_instruction_cache(target, entry->original_size);
    
    if (entry->origin != NULL) {
        DobbyFreeExecutableMemory(entry->origin, 24);
    }
    
    remove_hook_entry(target);
    DOBBY_LOG("ARM32 unhook at %p", target);
    
    return DOBBY_SUCCESS;
}

#elif defined(__x86_64__) || defined(__i386__)
// ================================================================
// x86 / x86_64 架构实现
// ================================================================

static int x86_install_hook(void* target, void* replace, void** origin) {
    if (target == NULL || replace == NULL) {
        return DOBBY_ERROR_INVALID_TARGET;
    }
    
    if (find_hook_entry(target) != NULL) {
        return DOBBY_ERROR_ALREADY_HOOKED;
    }
    
    if (set_memory_permission(target, 16, PROT_READ | PROT_WRITE | PROT_EXEC) != 0) {
        return DOBBY_ERROR_PERMISSION;
    }
    
    uint8_t* target_addr = (uint8_t*)target;
    uintptr_t target_ptr = (uintptr_t)target;
    uintptr_t replace_ptr = (uintptr_t)replace;
    
    DobbyHookEntry* entry = (DobbyHookEntry*)calloc(1, sizeof(DobbyHookEntry));
    if (entry == NULL) {
        return DOBBY_ERROR_MEMORY_ALLOC;
    }
    entry->target = target;
    entry->replace = replace;
    entry->enabled = true;
    
    // 保存原始指令 (前 5 字节)
    entry->original_size = 5;
    entry->original_bytes = (uint8_t*)malloc(entry->original_size);
    if (entry->original_bytes == NULL) {
        free(entry);
        return DOBBY_ERROR_MEMORY_ALLOC;
    }
    memcpy(entry->original_bytes, target_addr, entry->original_size);
    
    if (origin != NULL) {
        size_t tramp_size = 32;
        void* trampoline = DobbyAllocExecutableMemory(tramp_size);
        if (trampoline == NULL) {
            free(entry->original_bytes);
            free(entry);
            return DOBBY_ERROR_MEMORY_ALLOC;
        }
        entry->origin = trampoline;
        
        uint8_t* tramp = (uint8_t*)trampoline;
        memcpy(tramp, target_addr, 5);
        
        // JMP rel32 跳转回原函数+5
        tramp[5] = 0xE9;
        int32_t offset = (target_ptr + 5) - ((uintptr_t)tramp + 10);
        memcpy(&tramp[6], &offset, 4);
        
        flush_instruction_cache(tramp, tramp_size);
        *origin = trampoline;
    }
    
    // JMP rel32
    target_addr[0] = 0xE9;
    int32_t offset = (replace_ptr - target_ptr) - 5;
    memcpy(&target_addr[1], &offset, 4);
    
    flush_instruction_cache(target, 16);
    
    add_hook_entry(entry);
    DOBBY_LOG("x86 hook installed at %p -> %p", target, replace);
    
    return DOBBY_SUCCESS;
}

static int x86_unhook(void* target) {
    DobbyHookEntry* entry = find_hook_entry(target);
    if (entry == NULL) {
        return DOBBY_ERROR_NOT_HOOKED;
    }
    
    if (set_memory_permission(target, 16, PROT_READ | PROT_WRITE | PROT_EXEC) != 0) {
        return DOBBY_ERROR_PERMISSION;
    }
    
    memcpy(target, entry->original_bytes, entry->original_size);
    flush_instruction_cache(target, entry->original_size);
    
    if (entry->origin != NULL) {
        DobbyFreeExecutableMemory(entry->origin, 32);
    }
    
    remove_hook_entry(target);
    DOBBY_LOG("x86 unhook at %p", target);
    
    return DOBBY_SUCCESS;
}

#else
// ================================================================
// 不支持的架构
// ================================================================

static int unsupported_install_hook(void* target, void* replace, void** origin) {
    DOBBY_LOG("Unsupported architecture");
    return DOBBY_ERROR_UNSUPPORTED_ARCH;
}

static int unsupported_unhook(void* target) {
    DOBBY_LOG("Unsupported architecture");
    return DOBBY_ERROR_UNSUPPORTED_ARCH;
}

#endif

// ================================================================
// 公共 API 实现
// ================================================================

int DobbyHook(void* target, void* replace, void** origin) {
    DOBBY_LOG("DobbyHook: target=%p, replace=%p", target, replace);
    
    if (target == NULL) {
        return DOBBY_ERROR_INVALID_TARGET;
    }
    if (replace == NULL) {
        return DOBBY_ERROR_INVALID_REPLACE;
    }
    
    // 检查是否已被 hook
    if (DobbyIsHooked(target)) {
        DOBBY_LOG("Target %p already hooked", target);
        return DOBBY_ERROR_ALREADY_HOOKED;
    }
    
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
    DOBBY_LOG("DobbyUnhook: target=%p", target);
    
    if (target == NULL) {
        return DOBBY_ERROR_INVALID_TARGET;
    }
    
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
    if (entry == NULL) {
        return DOBBY_ERROR_NOT_HOOKED;
    }
    
    if (!entry->enabled) {
        return DOBBY_SUCCESS;
    }
    
    // 恢复原始指令
    if (set_memory_permission(target, 16, PROT_READ | PROT_WRITE | PROT_EXEC) != 0) {
        return DOBBY_ERROR_PERMISSION;
    }
    memcpy(target, entry->original_bytes, entry->original_size);
    flush_instruction_cache(target, entry->original_size);
    
    entry->enabled = false;
    DOBBY_LOG("Hook disabled at %p", target);
    
    return DOBBY_SUCCESS;
}

int DobbyEnableHook(void* target) {
    DobbyHookEntry* entry = find_hook_entry(target);
    if (entry == NULL) {
        return DOBBY_ERROR_NOT_HOOKED;
    }
    
    if (entry->enabled) {
        return DOBBY_SUCCESS;
    }
    
    // 重新安装 hook
    if (set_memory_permission(target, 16, PROT_READ | PROT_WRITE | PROT_EXEC) != 0) {
        return DOBBY_ERROR_PERMISSION;
    }
    
#if defined(__aarch64__)
    uint32_t* target_addr = (uint32_t*)target;
    uintptr_t target_ptr = (uintptr_t)target;
    uintptr_t replace_ptr = (uintptr_t)entry->replace;
    int64_t offset = (replace_ptr - target_ptr) >> 2;
    if (offset >= -0x03FFFFFF && offset <= 0x03FFFFFF) {
        target_addr[0] = 0x14000000 | (0x03FFFFFF & offset);
    }
#elif defined(__arm__)
    uint32_t* target_addr = (uint32_t*)target;
    uintptr_t target_ptr = (uintptr_t)target;
    uintptr_t replace_ptr = (uintptr_t)entry->replace;
    int32_t offset = (replace_ptr - target_ptr - 8) >> 2;
    target_addr[0] = 0xEA000000 | (0x00FFFFFF & offset);
#elif defined(__x86_64__) || defined(__i386__)
    uint8_t* target_addr = (uint8_t*)target;
    uintptr_t target_ptr = (uintptr_t)target;
    uintptr_t replace_ptr = (uintptr_t)entry->replace;
    target_addr[0] = 0xE9;
    int32_t offset = (replace_ptr - target_ptr) - 5;
    memcpy(&target_addr[1], &offset, 4);
#endif
    
    flush_instruction_cache(target, 16);
    entry->enabled = true;
    DOBBY_LOG("Hook enabled at %p", target);
    
    return DOBBY_SUCCESS;
}

void* DobbyCopyCode(void* address, size_t size) {
    if (address == NULL || size == 0) {
        return NULL;
    }
    
    void* new_addr = DobbyAllocExecutableMemory(size);
    if (new_addr == NULL) {
        return NULL;
    }
    
    memcpy(new_addr, address, size);
    flush_instruction_cache(new_addr, size);
    
    return new_addr;
}

void* DobbyAllocExecutableMemory(size_t size) {
    void* addr = mmap(NULL, size, PROT_READ | PROT_WRITE | PROT_EXEC,
                      MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (addr == MAP_FAILED) {
        DOBBY_LOG("mmap failed for size %zu", size);
        return NULL;
    }
    return addr;
}

void DobbyFreeExecutableMemory(void* address, size_t size) {
    if (address != NULL && size > 0) {
        munmap(address, size);
    }
}

int DobbyResetHook(void* target) {
    int result = DobbyUnhook(target);
    if (result != DOBBY_SUCCESS) {
        return result;
    }
    
    DobbyHookEntry* entry = find_hook_entry(target);
    if (entry == NULL) {
        return DOBBY_ERROR_NOT_HOOKED;
    }
    
    // 重新安装（使用保存的 replace）
    return DobbyHook(target, entry->replace, NULL);
}

bool DobbyIsHooked(void* target) {
    return find_hook_entry(target) != NULL;
}

int DobbyGetHookInfo(void* target, void** replace, void** origin) {
    DobbyHookEntry* entry = find_hook_entry(target);
    if (entry == NULL) {
        return DOBBY_ERROR_NOT_HOOKED;
    }
    
    if (replace != NULL) {
        *replace = entry->replace;
    }
    if (origin != NULL) {
        *origin = entry->origin;
    }
    
    return DOBBY_SUCCESS;
}

void DobbySetLogCallback(DobbyLogCallback callback) {
    g_log_callback = callback;
}

DobbyHookEntry* DobbyGetHookList(void) {
    return g_hook_list;
}

// ================================================================
// 初始化/清理 (可选)
// ================================================================

__attribute__((constructor))
static void dobby_init(void) {
    DOBBY_LOG("Dobby Hook initialized");
}

__attribute__((destructor))
static void dobby_fini(void) {
    DOBBY_LOG("Dobby Hook shutting down");
    // 清理所有 hook
    while (g_hook_list != NULL) {
        DobbyHookEntry* entry = g_hook_list;
        g_hook_list = entry->next;
        if (entry->original_bytes != NULL) {
            free(entry->original_bytes);
        }
        if (entry->origin != NULL) {
            DobbyFreeExecutableMemory(entry->origin, 32);
        }
        free(entry);
    }
}