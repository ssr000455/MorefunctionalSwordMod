#include "mfswordmod_native_config.h"
#include <jni.h>
#include <string.h>
#include <stdbool.h>
#include <pthread.h>
#include <unistd.h>
#include <stdlib.h>
#include <time.h>
#include <sys/stat.h>
#include <fcntl.h>

#define LOGI(...) printf("[mfswordmod] " __VA_ARGS__)
#define LOGE(...) printf("[mfswordmod] " __VA_ARGS__)
#define LOGW(...) printf("[mfswordmod] " __VA_ARGS__)

#include "dobby.h"

typedef void (*RemoveStackFunc)(void* inventory, int slot);
typedef void (*SetStackFunc)(void* inventory, int slot, jobject stack);
typedef jobject (*RemoveStack2Func)(void* inventory, int slot);
typedef void (*DropInventoryFunc)(void* player);
typedef void (*DropSelectedItemFunc)(void* player);
typedef void (*DropItemFunc)(void* player, jobject stack, int slot);
typedef void (*OnDeathFunc)(void* entity, void* damageSource);
typedef void (*KillFunc)(void* entity);
typedef void (*ClearInventoryFunc)(void* player);
typedef void (*SetHealthFunc)(void* entity, float health);
typedef void (*SetSlotFunc)(void* handler, int slot, int button, jobject stack);
typedef jobject (*QuickMoveFunc)(void* handler, int slot);
typedef void (*ClickSlotFunc)(void* handler, int slot, int button, int action, jobject stack);
typedef void (*SyncInventoryFunc)(void* player);

extern JavaVM* g_jvm;
extern JNIEnv* get_env();
extern void detach_env();
extern bool is_local_player(JNIEnv* env, jobject entity);
extern bool isRainbowSword(JNIEnv* env, jobject item);
extern bool isRainbowArmor(JNIEnv* env, jobject item);
extern jobject getMainHandStack(JNIEnv* env, jobject player);
extern bool playerHasRainbowSword(JNIEnv* env, jobject player);
extern bool playerHasRainbowArmor(JNIEnv* env, jobject player);
extern void setEntityHealth(JNIEnv* env, jobject entity, float health);

static RemoveStackFunc original_removeStack = NULL;
static SetStackFunc original_setStack = NULL;
static RemoveStack2Func original_removeStack2 = NULL;
static DropInventoryFunc original_dropInventory = NULL;
static DropSelectedItemFunc original_dropSelectedItem = NULL;
static DropItemFunc original_dropItem = NULL;
static OnDeathFunc original_onDeath = NULL;
static KillFunc original_kill = NULL;
static ClearInventoryFunc original_clearInventory = NULL;
static SetHealthFunc original_setHealth = NULL;
static QuickMoveFunc original_quickMove = NULL;
static ClickSlotFunc original_clickSlot = NULL;
static SyncInventoryFunc original_syncInventory = NULL;

static bool g_protect_active = true;
static bool g_death_keep_active = true;
static bool g_clear_protect_active = true;
static bool g_drop_protect_active = true;
static bool g_item_delete_protect_active = true;
static bool g_item_replace_protect_active = true;
static bool g_kill_protect_active = true;
static bool g_quick_move_protect_active = true;
static bool g_has_saved_item = false;
static jobject g_saved_main_hand = NULL;
static pthread_mutex_t g_protect_lock = PTHREAD_MUTEX_INITIALIZER;

static int get_main_hand_slot(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return 0;
    jclass playerClass = (*env)->GetObjectClass(env, player);
    jfieldID slotField = (*env)->GetFieldID(env, playerClass, "field_7506", "I");
    if (slotField == NULL) {
        jmethodID getSlot = (*env)->GetMethodID(env, playerClass, "method_7364", "()I");
        if (getSlot != NULL) {
            return (*env)->CallIntMethod(env, player, getSlot);
        }
        return 0;
    }
    return (*env)->GetIntField(env, player, slotField);
}

static bool is_main_hand_slot(JNIEnv* env, jobject player, int slot) {
    if (player == NULL || env == NULL) return false;
    int mainSlot = get_main_hand_slot(env, player);
    return (slot == mainSlot);
}

static jobject get_player_from_inventory(JNIEnv* env, jobject inventory) {
    if (inventory == NULL || env == NULL) return NULL;
    jclass invClass = (*env)->GetObjectClass(env, inventory);
    jmethodID getPlayer = (*env)->GetMethodID(env, invClass, "method_16885", "()Lnet/minecraft/class_3222;");
    if (getPlayer == NULL) {
        jmethodID getHolder = (*env)->GetMethodID(env, invClass, "method_16907", "()Lnet/minecraft/class_3222;");
        if (getHolder != NULL) {
            return (*env)->CallObjectMethod(env, inventory, getHolder);
        }
        return NULL;
    }
    return (*env)->CallObjectMethod(env, inventory, getPlayer);
}

static jobject get_main_hand_stack_ref(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return NULL;
    jclass playerClass = (*env)->GetObjectClass(env, player);
    jmethodID getMainHand = (*env)->GetMethodID(env, playerClass, "method_5998", "()Lnet/minecraft/class_1799;");
    if (getMainHand == NULL) return NULL;
    return (*env)->CallObjectMethod(env, player, getMainHand);
}

static bool is_same_item_stack(JNIEnv* env, jobject stack1, jobject stack2) {
    if (stack1 == NULL || stack2 == NULL || env == NULL) return false;
    if ((*env)->IsSameObject(env, stack1, stack2)) return true;
    jclass stackClass = (*env)->GetObjectClass(env, stack1);
    jmethodID equals = (*env)->GetMethodID(env, stackClass, "equals", "(Ljava/lang/Object;)Z");
    if (equals == NULL) return false;
    return (*env)->CallBooleanMethod(env, stack1, equals, stack2);
}

static bool should_protect_player(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return false;
    if (!is_local_player(env, player)) return false;
    if (!playerHasRainbowSword(env, player) && !playerHasRainbowArmor(env, player)) {
        return false;
    }
    return true;
}

static void hooked_removeStack(void* inventory, int slot) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_removeStack(inventory, slot);
        return;
    }
    if (g_item_delete_protect_active) {
        jobject player = get_player_from_inventory(env, inventory);
        if (should_protect_player(env, player) && is_main_hand_slot(env, player, slot)) {
            LOGI("防护: 拦截物品删除操作 slot=%d", slot);
            return;
        }
    }
    original_removeStack(inventory, slot);
}

static void hooked_setStack(void* inventory, int slot, jobject stack) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_setStack(inventory, slot, stack);
        return;
    }
    if (g_item_replace_protect_active) {
        jobject player = get_player_from_inventory(env, inventory);
        if (should_protect_player(env, player) && is_main_hand_slot(env, player, slot)) {
            if (stack == NULL) {
                LOGI("防护: 拦截物品替换操作 - 设为null slot=%d", slot);
                return;
            }
            jobject current = get_main_hand_stack_ref(env, player);
            if (current != NULL && !is_same_item_stack(env, current, stack)) {
                LOGI("防护: 拦截物品替换操作 slot=%d", slot);
                return;
            }
        }
    }
    original_setStack(inventory, slot, stack);
}

static jobject hooked_removeStack2(void* inventory, int slot) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        return original_removeStack2(inventory, slot);
    }
    if (g_item_delete_protect_active) {
        jobject player = get_player_from_inventory(env, inventory);
        if (should_protect_player(env, player) && is_main_hand_slot(env, player, slot)) {
            LOGI("防护: 拦截removeStack2删除 slot=%d", slot);
            return NULL;
        }
    }
    return original_removeStack2(inventory, slot);
}

static void hooked_dropInventory(void* player) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_dropInventory(player);
        return;
    }
    if (g_drop_protect_active && should_protect_player(env, player)) {
        LOGI("防护: 拦截丢弃操作");
        return;
    }
    original_dropInventory(player);
}

static void hooked_dropSelectedItem(void* player) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_dropSelectedItem(player);
        return;
    }
    if (g_drop_protect_active && should_protect_player(env, player)) {
        LOGI("防护: 拦截Q键丢弃");
        return;
    }
    original_dropSelectedItem(player);
}

static void hooked_dropItem(void* player, jobject stack, int slot) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_dropItem(player, stack, slot);
        return;
    }
    if (g_drop_protect_active && should_protect_player(env, player)) {
        if (is_main_hand_slot(env, player, slot)) {
            LOGI("防护: 拦截物品丢弃 slot=%d", slot);
            return;
        }
    }
    original_dropItem(player, stack, slot);
}

static jobject hooked_quickMove(void* handler, int slot) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        return original_quickMove(handler, slot);
    }
    if (g_quick_move_protect_active) {
        jclass handlerClass = (*env)->GetObjectClass(env, handler);
        jmethodID getPlayer = (*env)->GetMethodID(env, handlerClass, "method_16885", "()Lnet/minecraft/class_3222;");
        if (getPlayer != NULL) {
            jobject player = (*env)->CallObjectMethod(env, handler, getPlayer);
            if (should_protect_player(env, player) && is_main_hand_slot(env, player, slot)) {
                LOGI("防护: 拦截快捷移动 slot=%d", slot);
                return NULL;
            }
        }
    }
    return original_quickMove(handler, slot);
}

static void hooked_clickSlot(void* handler, int slot, int button, int action, jobject stack) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_clickSlot(handler, slot, button, action, stack);
        return;
    }
    if (g_item_replace_protect_active) {
        jclass handlerClass = (*env)->GetObjectClass(env, handler);
        jmethodID getPlayer = (*env)->GetMethodID(env, handlerClass, "method_16885", "()Lnet/minecraft/class_3222;");
        if (getPlayer != NULL) {
            jobject player = (*env)->CallObjectMethod(env, handler, getPlayer);
            if (should_protect_player(env, player) && is_main_hand_slot(env, player, slot)) {
                LOGI("防护: 拦截点击移动 slot=%d", slot);
                return;
            }
        }
    }
    original_clickSlot(handler, slot, button, action, stack);
}

static void hooked_clearInventory(void* player) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_clearInventory(player);
        return;
    }
    if (g_clear_protect_active && should_protect_player(env, player)) {
        LOGI("防护: 拦截清背包");
        return;
    }
    original_clearInventory(player);
}

static void hooked_onDeath(void* entity, void* damageSource) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_onDeath(entity, damageSource);
        return;
    }
    if (g_death_keep_active && is_local_player(env, entity)) {
        if (playerHasRainbowSword(env, entity)) {
            LOGI("防护: 死亡不掉落触发");
            pthread_mutex_lock(&g_protect_lock);
            jobject stack = get_main_hand_stack_ref(env, entity);
            if (stack != NULL) {
                if (g_saved_main_hand != NULL) {
                    (*env)->DeleteGlobalRef(env, g_saved_main_hand);
                }
                g_saved_main_hand = (*env)->NewGlobalRef(env, stack);
                g_has_saved_item = true;
            }
            pthread_mutex_unlock(&g_protect_lock);
        }
    }
    original_onDeath(entity, damageSource);
}

static void hooked_kill(void* entity) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_kill(entity);
        return;
    }
    if (g_kill_protect_active && is_local_player(env, entity)) {
        if (playerHasRainbowSword(env, entity) || playerHasRainbowArmor(env, entity)) {
            LOGI("防护: 拦截/kill指令");
            return;
        }
    }
    original_kill(entity);
}

static void hooked_setHealth(void* entity, float health) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_setHealth(entity, health);
        return;
    }
    if (g_kill_protect_active && is_local_player(env, entity)) {
        if (health <= 0.0f) {
            if (playerHasRainbowSword(env, entity) || playerHasRainbowArmor(env, entity)) {
                LOGI("防护: 拦截setHealth(0)");
                return;
            }
        }
    }
    original_setHealth(entity, health);
}

static void hooked_syncInventory(void* player) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_syncInventory(player);
        return;
    }
    if (should_protect_player(env, player)) {
        jobject current = get_main_hand_stack_ref(env, player);
        if (current == NULL && g_has_saved_item) {
            LOGI("防护: 检测到主手物品丢失，尝试恢复");
            pthread_mutex_lock(&g_protect_lock);
            if (g_saved_main_hand != NULL) {
                jclass playerClass = (*env)->GetObjectClass(env, player);
                jmethodID setMainHand = (*env)->GetMethodID(env, playerClass, "method_6022", "(Lnet/minecraft/class_1799;)V");
                if (setMainHand != NULL) {
                    (*env)->CallVoidMethod(env, player, setMainHand, g_saved_main_hand);
                }
                (*env)->DeleteGlobalRef(env, g_saved_main_hand);
                g_saved_main_hand = NULL;
                g_has_saved_item = false;
            }
            pthread_mutex_unlock(&g_protect_lock);
        }
    }
    original_syncInventory(player);
}

static void load_protect_config() {
    g_protect_active = get_config_bool("enable_protect") != 0;
    g_death_keep_active = get_config_bool("enable_death_keep") != 0;
    g_clear_protect_active = get_config_bool("enable_clear_protect") != 0;
    g_drop_protect_active = get_config_bool("enable_drop_protect") != 0;
    g_item_delete_protect_active = get_config_bool("enable_item_delete_protect") != 0;
    g_item_replace_protect_active = get_config_bool("enable_item_replace_protect") != 0;
    g_kill_protect_active = get_config_bool("enable_kill_protect") != 0;
    g_quick_move_protect_active = get_config_bool("enable_quick_move_protect") != 0;
}

void init_protect() {
    pthread_mutex_init(&g_protect_lock, NULL);
    load_protect_config();
    LOGI("防护模块初始化完成");
}

void install_protect_hooks(
    long addr_removeStack,
    long addr_setStack,
    long addr_dropInventory,
    long addr_dropSelectedItem,
    long addr_dropItem,
    long addr_onDeath,
    long addr_kill,
    long addr_clearInventory,
    long addr_setHealth,
    long addr_removeStack2,
    long addr_quickMove,
    long addr_clickSlot,
    long addr_syncInventory
) {
    LOGI("安装防护Hook...");

    if (!get_config_bool("enable_protect")) {
        LOGI("防护模块未启用");
        return;
    }

    load_protect_config();

    if (addr_removeStack != 0) {
        DobbyHook((void*)addr_removeStack, (void*)hooked_removeStack, (void**)&original_removeStack);
        LOGI("  Hook removeStack");
    }
    if (addr_setStack != 0) {
        DobbyHook((void*)addr_setStack, (void*)hooked_setStack, (void**)&original_setStack);
        LOGI("  Hook setStack");
    }
    if (addr_dropInventory != 0) {
        DobbyHook((void*)addr_dropInventory, (void*)hooked_dropInventory, (void**)&original_dropInventory);
        LOGI("  Hook dropInventory");
    }
    if (addr_dropSelectedItem != 0) {
        DobbyHook((void*)addr_dropSelectedItem, (void*)hooked_dropSelectedItem, (void**)&original_dropSelectedItem);
        LOGI("  Hook dropSelectedItem");
    }
    if (addr_dropItem != 0) {
        DobbyHook((void*)addr_dropItem, (void*)hooked_dropItem, (void**)&original_dropItem);
        LOGI("  Hook dropItem");
    }
    if (addr_onDeath != 0) {
        DobbyHook((void*)addr_onDeath, (void*)hooked_onDeath, (void**)&original_onDeath);
        LOGI("  Hook onDeath");
    }
    if (addr_kill != 0) {
        DobbyHook((void*)addr_kill, (void*)hooked_kill, (void**)&original_kill);
        LOGI("  Hook kill");
    }
    if (addr_clearInventory != 0) {
        DobbyHook((void*)addr_clearInventory, (void*)hooked_clearInventory, (void**)&original_clearInventory);
        LOGI("  Hook clearInventory");
    }
    if (addr_setHealth != 0) {
        DobbyHook((void*)addr_setHealth, (void*)hooked_setHealth, (void**)&original_setHealth);
        LOGI("  Hook setHealth");
    }
    if (addr_removeStack2 != 0) {
        DobbyHook((void*)addr_removeStack2, (void*)hooked_removeStack2, (void**)&original_removeStack2);
        LOGI("  Hook removeStack2");
    }
    if (addr_quickMove != 0) {
        DobbyHook((void*)addr_quickMove, (void*)hooked_quickMove, (void**)&original_quickMove);
        LOGI("  Hook quickMove");
    }
    if (addr_clickSlot != 0) {
        DobbyHook((void*)addr_clickSlot, (void*)hooked_clickSlot, (void**)&original_clickSlot);
        LOGI("  Hook clickSlot");
    }
    if (addr_syncInventory != 0) {
        DobbyHook((void*)addr_syncInventory, (void*)hooked_syncInventory, (void**)&original_syncInventory);
        LOGI("  Hook syncInventory");
    }

    LOGI("所有防护Hook安装完成");
}