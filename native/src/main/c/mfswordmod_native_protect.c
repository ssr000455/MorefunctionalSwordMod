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

extern JavaVM* g_jvm;
extern bool g_hooks_installed;
extern pthread_t g_guardian_thread;
extern bool g_guardian_running;

extern JNIEnv* get_env();
extern void detach_env();
extern bool is_local_player(JNIEnv* env, jobject entity);
extern bool isRainbowSword(JNIEnv* env, jobject item);
extern bool isRainbowArmor(JNIEnv* env, jobject item);
extern jobject getMainHandStack(JNIEnv* env, jobject player);
extern jobject getOffHandStack(JNIEnv* env, jobject player);
extern bool playerHasRainbowSword(JNIEnv* env, jobject player);
extern bool playerHasRainbowArmor(JNIEnv* env, jobject player);
extern void setEntityHealth(JNIEnv* env, jobject entity, float health);

// ============================================================
// 原始函数指针
// ============================================================
typedef void (*DropInventoryFunc)(void* player);
typedef void (*RemoveStackFunc)(void* inventory, int slot);
typedef void (*SetStackFunc)(void* inventory, int slot, jobject stack);
typedef jobject (*RemoveStack2Func)(void* inventory, int slot);
typedef void (*OnDeathFunc)(void* entity, void* damageSource);
typedef void (*KillFunc)(void* entity);
typedef void (*ClearInventoryFunc)(void* player);
typedef void (*SetHealthFunc)(void* entity, float health);
typedef void (*OnDisconnectFunc)(void* networkHandler, void* text);
typedef void (*DropItemFunc)(void* player, jobject stack, int slot);
typedef void (*DropSelectedItemFunc)(void* player);
typedef void (*SetSlotFunc)(void* handler, int slot, int button, jobject stack);
typedef jobject (*QuickMoveFunc)(void* handler, int slot);
typedef void (*ClickSlotFunc)(void* handler, int slot, int button, int action, jobject stack);
typedef void (*SyncInventoryFunc)(void* player);

static DropInventoryFunc original_dropInventory = NULL;
static RemoveStackFunc original_removeStack = NULL;
static SetStackFunc original_setStack = NULL;
static RemoveStack2Func original_removeStack2 = NULL;
static OnDeathFunc original_onDeath = NULL;
static KillFunc original_kill = NULL;
static ClearInventoryFunc original_clearInventory = NULL;
static SetHealthFunc original_setHealth = NULL;
static DropItemFunc original_dropItem = NULL;
static DropSelectedItemFunc original_dropSelectedItem = NULL;
static SetSlotFunc original_setSlot = NULL;
static QuickMoveFunc original_quickMove = NULL;
static ClickSlotFunc original_clickSlot = NULL;
static SyncInventoryFunc original_syncInventory = NULL;

// ============================================================
// 全局防护状态
// ============================================================
static bool g_protect_active = true;
static bool g_death_keep_active = true;
static bool g_clear_protect_active = true;
static bool g_drop_protect_active = true;
static bool g_item_delete_protect_active = true;
static bool g_item_replace_protect_active = true;
static bool g_kill_protect_active = true;
static bool g_kick_protect_active = true;
static bool g_quick_move_protect_active = true;

static pthread_mutex_t g_protect_lock = PTHREAD_MUTEX_INITIALIZER;
static jobject g_saved_main_hand = NULL;
static bool g_has_saved_item = false;

// ============================================================
// 辅助函数 - 获取玩家主手槽位
// ============================================================
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

// ============================================================
// 辅助函数 - 检测是否为主手槽位
// ============================================================
static bool is_main_hand_slot(JNIEnv* env, jobject player, int slot) {
    if (player == NULL || env == NULL) return false;
    int mainSlot = get_main_hand_slot(env, player);
    return (slot == mainSlot);
}

// ============================================================
// 辅助函数 - 检测是否为热键栏槽位 (0-8)
// ============================================================
static bool is_hotbar_slot(JNIEnv* env, jobject player, int slot) {
    if (player == NULL || env == NULL) return false;
    int mainSlot = get_main_hand_slot(env, player);
    return (slot >= 0 && slot <= 8 && slot == mainSlot);
}

// ============================================================
// 辅助函数 - 获取当前玩家
// ============================================================
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

// ============================================================
// 辅助函数 - 获取玩家主手物品的引用计数
// ============================================================
static jobject get_main_hand_stack_ref(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return NULL;
    jclass playerClass = (*env)->GetObjectClass(env, player);
    jmethodID getMainHand = (*env)->GetMethodID(env, playerClass, "method_5998", "()Lnet/minecraft/class_1799;");
    if (getMainHand == NULL) return NULL;
    return (*env)->CallObjectMethod(env, player, getMainHand);
}

// ============================================================
// 辅助函数 - 比较两个ItemStack是否相同
// ============================================================
static bool is_same_item_stack(JNIEnv* env, jobject stack1, jobject stack2) {
    if (stack1 == NULL || stack2 == NULL || env == NULL) return false;
    if ((*env)->IsSameObject(env, stack1, stack2)) return true;
    jclass stackClass = (*env)->GetObjectClass(env, stack1);
    jmethodID equals = (*env)->GetMethodID(env, stackClass, "equals", "(Ljava/lang/Object;)Z");
    if (equals == NULL) return false;
    return (*env)->CallBooleanMethod(env, stack1, equals, stack2);
}

// ============================================================
// 辅助函数 - 检查玩家是否应该被保护
// ============================================================
static bool should_protect_player(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return false;
    if (!is_local_player(env, player)) return false;
    if (!playerHasRainbowSword(env, player) && !playerHasRainbowArmor(env, player)) {
        return false;
    }
    return true;
}

// ============================================================
// Hook: 防物品被删除 (removeStack)
// ============================================================
static void hooked_removeStack(void* inventory, int slot) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_removeStack(inventory, slot);
        return;
    }

    if (g_item_delete_protect_active) {
        jobject player = get_player_from_inventory(env, inventory);
        if (should_protect_player(env, player) && is_main_hand_slot(env, player, slot)) {
            LOGI("防护: 拦截物品删除操作 - 主手物品被保护 slot=%d", slot);
            pthread_mutex_lock(&g_protect_lock);
            // 获取当前主手物品并保存引用，防止被GC回收
            jobject stack = get_main_hand_stack_ref(env, player);
            if (stack != NULL) {
                g_saved_main_hand = (*env)->NewGlobalRef(env, stack);
                g_has_saved_item = true;
                LOGI("防护: 已保存主手物品引用");
            }
            pthread_mutex_unlock(&g_protect_lock);
            return;
        }
    }

    original_removeStack(inventory, slot);
}

// ============================================================
// Hook: 防物品被替换 (setStack)
// ============================================================
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
                LOGI("防护: 拦截物品替换操作 - 尝试将主手物品设为null slot=%d", slot);
                return;
            }
            // 检查是否试图用不同物品替换主手物品
            jobject current = get_main_hand_stack_ref(env, player);
            if (current != NULL && !is_same_item_stack(env, current, stack)) {
                LOGI("防护: 拦截物品替换操作 - 尝试替换主手物品 slot=%d", slot);
                return;
            }
        }
    }

    original_setStack(inventory, slot, stack);
}

// ============================================================
// Hook: 防物品被丢弃 (dropInventory)
// ============================================================
static void hooked_dropInventory(void* player) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_dropInventory(player);
        return;
    }

    if (g_drop_protect_active && should_protect_player(env, player)) {
        LOGI("防护: 拦截丢弃操作 - 玩家持七彩神剑");
        return;
    }

    original_dropInventory(player);
}

// ============================================================
// Hook: 防物品被丢弃 (dropSelectedItem - Q键)
// ============================================================
static void hooked_dropSelectedItem(void* player) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_dropSelectedItem(player);
        return;
    }

    if (g_drop_protect_active && should_protect_player(env, player)) {
        LOGI("防护: 拦截Q键丢弃 - 玩家持七彩神剑");
        return;
    }

    original_dropSelectedItem(player);
}

// ============================================================
// Hook: 防物品被丢弃 (dropItem)
// ============================================================
static void hooked_dropItem(void* player, jobject stack, int slot) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_dropItem(player, stack, slot);
        return;
    }

    if (g_drop_protect_active && should_protect_player(env, player)) {
        if (is_main_hand_slot(env, player, slot)) {
            LOGI("防护: 拦截物品丢弃 - 主手物品被保护 slot=%d", slot);
            return;
        }
    }

    original_dropItem(player, stack, slot);
}

// ============================================================
// Hook: 防快捷移动 (quickMove - Shift+点击)
// ============================================================
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
                LOGI("防护: 拦截快捷移动 - 主手物品被保护 slot=%d", slot);
                return NULL;
            }
        }
    }

    return original_quickMove(handler, slot);
}

// ============================================================
// Hook: 防点击槽位移动 (clickSlot)
// ============================================================
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
                LOGI("防护: 拦截点击槽位移动 - 主手物品被保护 slot=%d button=%d action=%d", slot, button, action);
                return;
            }
        }
    }

    original_clickSlot(handler, slot, button, action, stack);
}

// ============================================================
// Hook: 防清背包 (clearInventory)
// ============================================================
static void hooked_clearInventory(void* player) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_clearInventory(player);
        return;
    }

    if (g_clear_protect_active && should_protect_player(env, player)) {
        LOGI("防护: 拦截清背包操作 - 玩家持七彩神剑");
        return;
    }

    original_clearInventory(player);
}

// ============================================================
// Hook: 死亡不掉落 (onDeath)
// ============================================================
static void hooked_onDeath(void* entity, void* damageSource) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_onDeath(entity, damageSource);
        return;
    }

    if (g_death_keep_active && is_local_player(env, entity)) {
        if (playerHasRainbowSword(env, entity)) {
            LOGI("防护: 死亡不掉落触发 - 保存主手物品");
            pthread_mutex_lock(&g_protect_lock);
            jobject stack = get_main_hand_stack_ref(env, entity);
            if (stack != NULL) {
                if (g_saved_main_hand != NULL) {
                    (*env)->DeleteGlobalRef(env, g_saved_main_hand);
                }
                g_saved_main_hand = (*env)->NewGlobalRef(env, stack);
                g_has_saved_item = true;
                LOGI("防护: 已保存死亡前主手物品");
            }
            pthread_mutex_unlock(&g_protect_lock);
        }
    }

    original_onDeath(entity, damageSource);
}

// ============================================================
// Hook: 防被杀 (kill)
// ============================================================
static void hooked_kill(void* entity) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_kill(entity);
        return;
    }

    if (g_kill_protect_active && is_local_player(env, entity)) {
        if (playerHasRainbowSword(env, entity) || playerHasRainbowArmor(env, entity)) {
            LOGI("防护: 拦截/kill指令 - 玩家持七彩神剑或彩虹盔甲");
            return;
        }
    }

    original_kill(entity);
}

// ============================================================
// Hook: 防被杀 (setHealth 设为0)
// ============================================================
static void hooked_setHealth(void* entity, float health) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_setHealth(entity, health);
        return;
    }

    if (g_kill_protect_active && is_local_player(env, entity)) {
        if (health <= 0.0f) {
            if (playerHasRainbowSword(env, entity) || playerHasRainbowArmor(env, entity)) {
                LOGI("防护: 拦截setHealth(0) - 玩家持七彩神剑或彩虹盔甲");
                return;
            }
        }
    }

    original_setHealth(entity, health);
}

// ============================================================
// Hook: 防被踢出 (onDisconnect) - 增强版
// ============================================================
static void hooked_onDisconnect_protect(void* networkHandler, void* text) {
    if (!g_kick_protect_active) {
        original_onDisconnect(networkHandler, text);
        return;
    }

    JNIEnv* env = get_env();
    if (env == NULL) {
        original_onDisconnect(networkHandler, text);
        return;
    }

    // 检查是否为本地玩家被踢
    jclass handlerClass = (*env)->GetObjectClass(env, networkHandler);
    jmethodID getPlayer = (*env)->GetMethodID(env, handlerClass, "method_11075", "()Lnet/minecraft/class_3222;");
    if (getPlayer == NULL) {
        getPlayer = (*env)->GetMethodID(env, handlerClass, "method_11088", "()Lnet/minecraft/class_3222;");
    }
    if (getPlayer != NULL) {
        jobject player = (*env)->CallObjectMethod(env, networkHandler, getPlayer);
        if (should_protect_player(env, player)) {
            LOGI("防护: 拦截踢出操作 - 玩家持七彩神剑");
            // 解析踢出文本，如果是反作弊踢出则额外处理
            if (text != NULL) {
                jstring textStr = (jstring)text;
                const char* textCstr = (*env)->GetStringUTFChars(env, textStr, NULL);
                if (textCstr != NULL) {
                    if (strstr(textCstr, "cheat") != NULL || strstr(textCstr, "hack") != NULL ||
                        strstr(textCstr, "anticheat") != NULL || strstr(textCstr, "作弊") != NULL ||
                        strstr(textCstr, "飞行") != NULL || strstr(textCstr, "speed") != NULL) {
                        LOGI("防护: 检测到反作弊踢出 - 已拦截: %s", textCstr);
                        (*env)->ReleaseStringUTFChars(env, textStr, textCstr);
                        return;
                    }
                    (*env)->ReleaseStringUTFChars(env, textStr, textCstr);
                }
            }
            // 非反作弊踢出，仍然拦截
            return;
        }
    }

    original_onDisconnect(networkHandler, text);
}

// ============================================================
// Hook: 防物品被外部模组删除 (removeStack2)
// ============================================================
static jobject hooked_removeStack2(void* inventory, int slot) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        return original_removeStack2(inventory, slot);
    }

    if (g_item_delete_protect_active) {
        jobject player = get_player_from_inventory(env, inventory);
        if (should_protect_player(env, player) && is_main_hand_slot(env, player, slot)) {
            LOGI("防护: 拦截removeStack2删除 - 主手物品被保护 slot=%d", slot);
            return NULL;
        }
    }

    return original_removeStack2(inventory, slot);
}

// ============================================================
// 恢复死亡掉落的物品
// ============================================================
void restore_death_items(JNIEnv* env, jobject player) {
    if (env == NULL || player == NULL) return;

    pthread_mutex_lock(&g_protect_lock);
    if (g_has_saved_item && g_saved_main_hand != NULL) {
        LOGI("防护: 恢复死亡前主手物品");
        // 将保存的物品放回主手
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

// ============================================================
// 同步背包 - 防护后刷新
// ============================================================
static void hooked_syncInventory(void* player) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_syncInventory(player);
        return;
    }

    if (should_protect_player(env, player)) {
        // 同步前检查主手物品是否被篡改
        jobject current = get_main_hand_stack_ref(env, player);
        if (current == NULL && g_has_saved_item) {
            LOGI("防护: 检测到主手物品丢失，尝试恢复");
            restore_death_items(env, player);
        }
    }

    original_syncInventory(player);
}

// ============================================================
// 从配置文件加载防护开关
// ============================================================
static void load_protect_config() {
    g_protect_active = get_config_bool("enable_protect") != 0;
    g_death_keep_active = get_config_bool("enable_death_keep") != 0;
    g_clear_protect_active = get_config_bool("enable_clear_protect") != 0;
    g_drop_protect_active = get_config_bool("enable_drop_protect") != 0;
    g_item_delete_protect_active = get_config_bool("enable_item_delete_protect") != 0;
    g_item_replace_protect_active = get_config_bool("enable_item_replace_protect") != 0;
    g_kill_protect_active = get_config_bool("enable_kill_protect") != 0;
    g_kick_protect_active = get_config_bool("enable_kick_protect") != 0;
    g_quick_move_protect_active = get_config_bool("enable_quick_move_protect") != 0;

    LOGI("防护配置加载完成: protect=%d death_keep=%d clear=%d drop=%d delete=%d replace=%d kill=%d kick=%d quick=%d",
         g_protect_active, g_death_keep_active, g_clear_protect_active, g_drop_protect_active,
         g_item_delete_protect_active, g_item_replace_protect_active, g_kill_protect_active,
         g_kick_protect_active, g_quick_move_protect_active);
}

// ============================================================
// 初始化防护模块
// ============================================================
void init_protect() {
    pthread_mutex_init(&g_protect_lock, NULL);
    load_protect_config();
    LOGI("防护模块初始化完成");
}

// ============================================================
// 安装防护Hook (由installNativeHook调用)
// ============================================================
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
    long addr_onDisconnect,
    long addr_syncInventory
) {
    LOGI("开始安装防护Hook...");

    if (!get_config_bool("enable_protect")) {
        LOGI("防护模块未启用");
        return;
    }

    load_protect_config();

    if (addr_removeStack != 0) {
        DobbyHook((void*)addr_removeStack, (void*)hooked_removeStack, (void**)&original_removeStack);
        LOGI("  Hook removeStack (防删除)");
    }
    if (addr_setStack != 0) {
        DobbyHook((void*)addr_setStack, (void*)hooked_setStack, (void**)&original_setStack);
        LOGI("  Hook setStack (防替换)");
    }
    if (addr_dropInventory != 0) {
        DobbyHook((void*)addr_dropInventory, (void*)hooked_dropInventory, (void**)&original_dropInventory);
        LOGI("  Hook dropInventory (防丢弃)");
    }
    if (addr_dropSelectedItem != 0) {
        DobbyHook((void*)addr_dropSelectedItem, (void*)hooked_dropSelectedItem, (void**)&original_dropSelectedItem);
        LOGI("  Hook dropSelectedItem (防Q键丢弃)");
    }
    if (addr_dropItem != 0) {
        DobbyHook((void*)addr_dropItem, (void*)hooked_dropItem, (void**)&original_dropItem);
        LOGI("  Hook dropItem (防物品丢弃)");
    }
    if (addr_onDeath != 0) {
        DobbyHook((void*)addr_onDeath, (void*)hooked_onDeath, (void**)&original_onDeath);
        LOGI("  Hook onDeath (死亡不掉落)");
    }
    if (addr_kill != 0) {
        DobbyHook((void*)addr_kill, (void*)hooked_kill, (void**)&original_kill);
        LOGI("  Hook kill (防被杀)");
    }
    if (addr_clearInventory != 0) {
        DobbyHook((void*)addr_clearInventory, (void*)hooked_clearInventory, (void**)&original_clearInventory);
        LOGI("  Hook clearInventory (防清背包)");
    }
    if (addr_setHealth != 0) {
        DobbyHook((void*)addr_setHealth, (void*)hooked_setHealth, (void**)&original_setHealth);
        LOGI("  Hook setHealth (防setHealth(0))");
    }
    if (addr_removeStack2 != 0) {
        DobbyHook((void*)addr_removeStack2, (void*)hooked_removeStack2, (void**)&original_removeStack2);
        LOGI("  Hook removeStack2 (防删除)");
    }
    if (addr_quickMove != 0) {
        DobbyHook((void*)addr_quickMove, (void*)hooked_quickMove, (void**)&original_quickMove);
        LOGI("  Hook quickMove (防Shift移动)");
    }
    if (addr_clickSlot != 0) {
        DobbyHook((void*)addr_clickSlot, (void*)hooked_clickSlot, (void**)&original_clickSlot);
        LOGI("  Hook clickSlot (防点击移动)");
    }
    if (addr_onDisconnect != 0) {
        DobbyHook((void*)addr_onDisconnect, (void*)hooked_onDisconnect_protect, (void**)&original_onDisconnect);
        LOGI("  Hook onDisconnect (防踢出)");
    }
    if (addr_syncInventory != 0) {
        DobbyHook((void*)addr_syncInventory, (void*)hooked_syncInventory, (void**)&original_syncInventory);
        LOGI("  Hook syncInventory (背包同步保护)");
    }

    LOGI("所有防护Hook安装完成");
}