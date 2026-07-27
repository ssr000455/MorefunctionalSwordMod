#include <jni.h>
#include <string.h>
#include <stdbool.h>
#include <pthread.h>
#include <unistd.h>
#include <stdlib.h>
#include <time.h>

#define LOGI(...) printf("[mfswordmod] " __VA_ARGS__)
#define LOGE(...) printf("[mfswordmod] " __VA_ARGS__)
#define LOGW(...) printf("[mfswordmod] " __VA_ARGS__)

#include "dobby.h"

static JavaVM* g_jvm = NULL;
static bool g_hooks_installed = false;
static pthread_t g_guardian_thread = 0;
static bool g_guardian_running = false;

// ============================================================
// 功能开关
// ============================================================
static bool g_auto_block = true;
static bool g_no_knockback = true;
static bool g_no_fall_damage = true;
static bool g_infinite_combo = true;
static bool g_anti_anticheat = true;
static bool g_kick_protect = true;
static bool g_inventory_lock = false;
static bool g_extended_attack_range = true;
static bool g_ignore_armor = true;
static bool g_always_critical = true;
static bool g_no_cooldown = true;
static bool g_remove_fog = true;
static bool g_item_glint = true;
static bool g_bypass_chat_filter = true;
static bool g_ladder_speed = true;
static bool g_swim_speed = true;
static bool g_no_clip = true;
static bool g_multi_jump = true;
static int g_multi_jump_count = 5;

// ============================================================
// 原始函数指针
// ============================================================
typedef jfloat (*GetHardnessFunc)(void* blockState, void* world, void* pos);
typedef jboolean (*DamageFunc)(void* entity, void* damageSource, jfloat amount);
typedef jboolean (*AttackEntityFromFunc)(void* entity, void* damageSource, jfloat amount);
typedef void (*SendPacketFunc)(void* networkHandler, void* packet);
typedef void (*AddVelocityFunc)(void* entity, double x, double y, double z);
typedef jboolean (*HandleFallDamageFunc)(void* entity, float distance, float damageMultiplier);
typedef jfloat (*GetAttackCooldownFunc)(void* player, float baseTime);
typedef void (*OnDisconnectFunc)(void* networkHandler, void* text);
typedef void (*TransferSlotFunc)(void* handler, int slot, int otherSlot);
typedef double (*GetAttackRangeFunc)(void* player);
typedef float (*ApplyArmorToDamageFunc)(void* entity, float damage, void* damageSource);
typedef float (*GetBlockSpeedFactorFunc)(void* entity, void* blockState);
typedef float (*GetFluidSpeedFunc)(void* entity, void* fluidState);
typedef void (*PushOutOfBlocksFunc)(void* entity, double x, double y, double z);
typedef void (*JumpFunc)(void* livingEntity);
typedef void (*RenderFogFunc)(void* worldRenderer, void* camera, float tickDelta);
typedef void (*RenderItemFunc)(void* itemRenderer, void* stack, void* mode, void* matrices, void* consumers, int light, int overlay);
typedef void (*ChatMessageC2SPacketFunc)(void* packet, void* message);

static GetHardnessFunc original_getHardness = NULL;
static DamageFunc original_damage = NULL;
static AttackEntityFromFunc original_attackEntityFrom = NULL;
static SendPacketFunc original_sendPacket = NULL;
static AddVelocityFunc original_addVelocity = NULL;
static HandleFallDamageFunc original_handleFallDamage = NULL;
static GetAttackCooldownFunc original_getAttackCooldown = NULL;
static OnDisconnectFunc original_onDisconnect = NULL;
static TransferSlotFunc original_transferSlot = NULL;
static GetAttackRangeFunc original_getAttackRange = NULL;
static ApplyArmorToDamageFunc original_applyArmorToDamage = NULL;
static GetBlockSpeedFactorFunc original_getBlockSpeedFactor = NULL;
static GetFluidSpeedFunc original_getFluidSpeed = NULL;
static PushOutOfBlocksFunc original_pushOutOfBlocks = NULL;
static JumpFunc original_jump = NULL;
static RenderFogFunc original_renderFog = NULL;
static RenderItemFunc original_renderItem = NULL;
static ChatMessageC2SPacketFunc original_chatMessageC2SPacket = NULL;

// ============================================================
// JNI 工具函数
// ============================================================
static JNIEnv* get_env() {
    JNIEnv* env = NULL;
    if (g_jvm != NULL) {
        (*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL);
    }
    return env;
}

static void detach_env() {
    if (g_jvm != NULL) {
        (*g_jvm)->DetachCurrentThread(g_jvm);
    }
}

static jobject get_client_player(JNIEnv* env) {
    jclass mcClass = (*env)->FindClass(env, "net/minecraft/class_310");
    if (mcClass == NULL) return NULL;
    jmethodID getInstance = (*env)->GetStaticMethodID(env, mcClass, "method_1551", "()Lnet/minecraft/class_310;");
    if (getInstance == NULL) return NULL;
    jobject mc = (*env)->CallStaticObjectMethod(env, mcClass, getInstance);
    if (mc == NULL) return NULL;
    jmethodID getPlayer = (*env)->GetMethodID(env, mcClass, "method_1528", "()Lnet/minecraft/class_746;");
    return (*env)->CallObjectMethod(env, mc, getPlayer);
}

static bool is_local_player(JNIEnv* env, jobject entity) {
    if (entity == NULL) return false;
    jobject local = get_client_player(env);
    if (local == NULL) return false;
    return (*env)->IsSameObject(env, entity, local);
}

static bool is_attacker_local(JNIEnv* env, jobject damageSource) {
    if (damageSource == NULL) return false;
    jclass dsClass = (*env)->GetObjectClass(env, damageSource);
    jmethodID getAttacker = (*env)->GetMethodID(env, dsClass, "method_11598", "()Lnet/minecraft/class_1297;");
    jobject attacker = (*env)->CallObjectMethod(env, damageSource, getAttacker);
    if (attacker == NULL) return false;
    return is_local_player(env, attacker);
}

static bool isRainbowSword(JNIEnv* env, jobject item) {
    if (item == NULL || env == NULL) return false;
    jclass itemClass = (*env)->GetObjectClass(env, item);
    jclass cls = (*env)->FindClass(env, "com/qidai/morefunctionalswordmod/RainbowSwordItem");
    if (cls != NULL && (*env)->IsInstanceOf(env, item, cls)) {
        return true;
    }
    jmethodID getRegistryId = (*env)->GetMethodID(env, itemClass, "method_10303", "()Lnet/minecraft/class_2960;");
    if (getRegistryId != NULL) {
        jobject id = (*env)->CallObjectMethod(env, item, getRegistryId);
        if (id != NULL) {
            jclass idClass = (*env)->GetObjectClass(env, id);
            jmethodID toString = (*env)->GetMethodID(env, idClass, "toString", "()Ljava/lang/String;");
            jstring idStr = (*env)->CallObjectMethod(env, id, toString);
            const char* idCstr = (*env)->GetStringUTFChars(env, idStr, NULL);
            bool result = (strstr(idCstr, "rainbow_sword") != NULL);
            (*env)->ReleaseStringUTFChars(env, idStr, idCstr);
            return result;
        }
    }
    return false;
}

static jobject getMainHandItem(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return NULL;
    jclass playerClass = (*env)->GetObjectClass(env, player);
    jmethodID getMainHand = (*env)->GetMethodID(env, playerClass, "method_5998", "()Lnet/minecraft/class_1799;");
    if (getMainHand == NULL) return NULL;
    jobject stack = (*env)->CallObjectMethod(env, player, getMainHand);
    if (stack == NULL) return NULL;
    jclass stackClass = (*env)->GetObjectClass(env, stack);
    jmethodID getItem = (*env)->GetMethodID(env, stackClass, "method_7909", "()Lnet/minecraft/class_1792;");
    if (getItem == NULL) return NULL;
    return (*env)->CallObjectMethod(env, stack, getItem);
}

static bool playerHasRainbowSword(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return false;
    jobject item = getMainHandItem(env, player);
    if (!isRainbowSword(env, item)) return false;
    jclass stackClass = (*env)->FindClass(env, "net/minecraft/class_1799");
    if (stackClass == NULL) return false;
    jclass playerClass = (*env)->GetObjectClass(env, player);
    jmethodID getMainHandStack = (*env)->GetMethodID(env, playerClass, "method_5998", "()Lnet/minecraft/class_1799;");
    jobject stack = (*env)->CallObjectMethod(env, player, getMainHandStack);
    if (stack == NULL) return false;
    jmethodID getOrCreateNbt = (*env)->GetMethodID(env, stackClass, "method_7993", "()Lnet/minecraft/class_2487;");
    jobject nbt = (*env)->CallObjectMethod(env, stack, getOrCreateNbt);
    if (nbt == NULL) return false;
    jclass nbtClass = (*env)->GetObjectClass(env, nbt);
    jmethodID getBoolean = (*env)->GetMethodID(env, nbtClass, "method_10556", "(Ljava/lang/String;)Z");
    jstring key = (*env)->NewStringUTF(env, "HasContract");
    jboolean hasContract = (*env)->CallBooleanMethod(env, nbt, getBoolean, key);
    return (bool)hasContract;
}

// ============================================================
// Hook: 基岩破坏
// ============================================================
static jfloat hooked_getHardness(void* blockState, void* world, void* pos) {
    JNIEnv* env = get_env();
    if (env == NULL) return original_getHardness(blockState, world, pos);
    jclass stateClass = (*env)->GetObjectClass(env, blockState);
    jmethodID getBlock = (*env)->GetMethodID(env, stateClass, "method_9588", "()Lnet/minecraft/class_2248;");
    jobject block = (*env)->CallObjectMethod(env, blockState, getBlock);
    jclass blockClass = (*env)->GetObjectClass(env, block);
    jmethodID getTranslationKey = (*env)->GetMethodID(env, blockClass, "method_9530", "()Ljava/lang/String;");
    jstring key = (*env)->CallObjectMethod(env, block, getTranslationKey);
    const char* keyStr = (*env)->GetStringUTFChars(env, key, NULL);
    bool isBedrock = (strstr(keyStr, "bedrock") != NULL);
    (*env)->ReleaseStringUTFChars(env, key, keyStr);
    if (isBedrock) {
        return 0.0f;
    }
    return original_getHardness(blockState, world, pos);
}

// ============================================================
// Hook: 免疫伤害 + 自动格挡
// ============================================================
static jboolean hooked_damage(void* entity, void* damageSource, jfloat amount) {
    JNIEnv* env = get_env();
    if (env == NULL) return original_damage(entity, damageSource, amount);

    jclass entityClass = (*env)->GetObjectClass(env, entity);
    jclass playerClass = (*env)->FindClass(env, "net/minecraft/class_3222");
    if (playerClass == NULL) return original_damage(entity, damageSource, amount);

    if ((*env)->IsInstanceOf(env, entity, playerClass)) {
        if (is_local_player(env, entity)) {
            if (g_auto_block) {
                jmethodID setBlocking = (*env)->GetMethodID(env, entityClass, "method_6067", "(Z)V");
                if (setBlocking != NULL) {
                    (*env)->CallVoidMethod(env, entity, setBlocking, JNI_TRUE);
                }
                amount *= 0.2f;
            }
            if (playerHasRainbowSword(env, entity)) {
                return false;
            }
        }
    }
    return original_damage(entity, damageSource, amount);
}

// ============================================================
// Hook: 秒杀生物
// ============================================================
static jboolean hooked_attackEntityFrom(void* entity, void* damageSource, jfloat amount) {
    JNIEnv* env = get_env();
    if (env == NULL) return original_attackEntityFrom(entity, damageSource, amount);

    jclass entityClass = (*env)->GetObjectClass(env, entity);
    jclass playerClass = (*env)->FindClass(env, "net/minecraft/class_3222");

    if (playerClass != NULL && (*env)->IsInstanceOf(env, entity, playerClass)) {
        return original_attackEntityFrom(entity, damageSource, amount);
    }

    jclass dsClass = (*env)->GetObjectClass(env, damageSource);
    jmethodID getAttacker = (*env)->GetMethodID(env, dsClass, "method_11598", "()Lnet/minecraft/class_1297;");
    jobject attacker = (*env)->CallObjectMethod(env, damageSource, getAttacker);

    if (attacker != NULL && (*env)->IsInstanceOf(env, attacker, playerClass)) {
        if (is_local_player(env, attacker) && playerHasRainbowSword(env, attacker)) {
            jmethodID setHealth = (*env)->GetMethodID(env, entityClass, "method_6044", "(F)V");
            if (setHealth != NULL) {
                (*env)->CallVoidMethod(env, entity, setHealth, 0.0f);
            }
            return true;
        }
    }
    return original_attackEntityFrom(entity, damageSource, amount);
}

// ============================================================
// Hook: 反击退
// ============================================================
static void hooked_addVelocity(void* entity, double x, double y, double z) {
    JNIEnv* env = get_env();
    if (env != NULL && g_no_knockback && is_local_player(env, entity)) {
        original_addVelocity(entity, 0, 0, 0);
        return;
    }
    original_addVelocity(entity, x, y, z);
}

// ============================================================
// Hook: 反摔落
// ============================================================
static jboolean hooked_handleFallDamage(void* entity, float distance, float damageMultiplier) {
    JNIEnv* env = get_env();
    if (env != NULL && g_no_fall_damage && is_local_player(env, entity)) {
        return JNI_FALSE;
    }
    return original_handleFallDamage(entity, distance, damageMultiplier);
}

// ============================================================
// Hook: 无限连砍
// ============================================================
static jfloat hooked_getAttackCooldown(void* player, float baseTime) {
    JNIEnv* env = get_env();
    if (env != NULL && g_infinite_combo && is_local_player(env, player)) {
        return 0.0f;
    }
    return original_getAttackCooldown(player, baseTime);
}

// ============================================================
// Hook: 100%暴击
// ============================================================
static jfloat hooked_getAttackCooldownProgress(void* player, float baseTime) {
    JNIEnv* env = get_env();
    if (env != NULL && g_always_critical && is_local_player(env, player)) {
        return 1.5f;
    }
    return original_getAttackCooldown(player, baseTime);
}

// ============================================================
// Hook: 攻击距离翻倍
// ============================================================
static double hooked_getAttackRange(void* player) {
    JNIEnv* env = get_env();
    if (env != NULL && g_extended_attack_range && is_local_player(env, player)) {
        return 6.0;
    }
    return original_getAttackRange(player);
}

// ============================================================
// Hook: 无视护甲
// ============================================================
static float hooked_applyArmorToDamage(void* entity, float damage, void* damageSource) {
    JNIEnv* env = get_env();
    if (env != NULL && g_ignore_armor && is_attacker_local(env, damageSource)) {
        return damage;
    }
    return original_applyArmorToDamage(entity, damage, damageSource);
}

// ============================================================
// Hook: 爬梯子加速
// ============================================================
static float hooked_getBlockSpeedFactor(void* entity, void* blockState) {
    float original = original_getBlockSpeedFactor(entity, blockState);
    JNIEnv* env = get_env();
    if (env != NULL && g_ladder_speed && is_local_player(env, entity)) {
        jclass stateClass = (*env)->GetObjectClass(env, blockState);
        jmethodID getBlock = (*env)->GetMethodID(env, stateClass, "method_9588", "()Lnet/minecraft/class_2248;");
        jobject block = (*env)->CallObjectMethod(env, blockState, getBlock);
        jclass blockClass = (*env)->GetObjectClass(env, block);
        jmethodID getTranslationKey = (*env)->GetMethodID(env, blockClass, "method_9530", "()Ljava/lang/String;");
        jstring key = (*env)->CallObjectMethod(env, block, getTranslationKey);
        const char* keyStr = (*env)->GetStringUTFChars(env, key, NULL);
        bool isLadder = (strstr(keyStr, "ladder") != NULL) || (strstr(keyStr, "vine") != NULL);
        (*env)->ReleaseStringUTFChars(env, key, keyStr);
        if (isLadder) {
            return original * 2.0f;
        }
    }
    return original;
}

// ============================================================
// Hook: 游泳加速
// ============================================================
static float hooked_getFluidSpeed(void* entity, void* fluidState) {
    float original = original_getFluidSpeed(entity, fluidState);
    JNIEnv* env = get_env();
    if (env != NULL && g_swim_speed && is_local_player(env, entity)) {
        return original * 2.0f;
    }
    return original;
}

// ============================================================
// Hook: 无碰撞
// ============================================================
static void hooked_pushOutOfBlocks(void* entity, double x, double y, double z) {
    JNIEnv* env = get_env();
    if (env != NULL && g_no_clip && is_local_player(env, entity)) {
        return;
    }
    original_pushOutOfBlocks(entity, x, y, z);
}

// ============================================================
// Hook: 多段跳
// ============================================================
static void hooked_jump(void* livingEntity) {
    JNIEnv* env = get_env();
    if (env != NULL && g_multi_jump && is_local_player(env, livingEntity)) {
        original_jump(livingEntity);
        return;
    }
    original_jump(livingEntity);
}

// ============================================================
// Hook: 移除雾效
// ============================================================
static void hooked_renderFog(void* worldRenderer, void* camera, float tickDelta) {
    if (g_remove_fog) {
        return;
    }
    original_renderFog(worldRenderer, camera, tickDelta);
}

// ============================================================
// Hook: 物品发光
// ============================================================
static void hooked_renderItem(void* itemRenderer, void* stack, void* mode, void* matrices, void* consumers, int light, int overlay) {
    original_renderItem(itemRenderer, stack, mode, matrices, consumers, light, overlay);
    if (g_item_glint) {
        // 强制绘制附魔光泽
        // 实际需要调用 RenderLayer.getGlint()
    }
}

// ============================================================
// Hook: 聊天过滤绕过
// ============================================================
static void hooked_chatMessageC2SPacket(void* packet, void* message) {
    if (g_bypass_chat_filter) {
        // 消息前后添加不可见字符
    }
    original_chatMessageC2SPacket(packet, message);
}

// ============================================================
// Hook: 反反作弊
// ============================================================
static void hooked_sendPacket_anti(void* networkHandler, void* packet) {
    if (g_anti_anticheat) {
        JNIEnv* env = get_env();
        if (env != NULL) {
            jclass packetClass = (*env)->GetObjectClass(env, packet);
            jclass posClass = (*env)->FindClass(env, "net/minecraft/class_2748");
            if (posClass != NULL && (*env)->IsInstanceOf(env, packet, posClass)) {
                // 篡改位置数据包
            }
        }
    }
    original_sendPacket(networkHandler, packet);
}

// ============================================================
// Hook: 踢人防反
// ============================================================
static void hooked_onDisconnect(void* networkHandler, void* text) {
    if (g_kick_protect) {
        // 反制逻辑
    }
    original_onDisconnect(networkHandler, text);
}

// ============================================================
// Hook: 物品栏锁定
// ============================================================
static void hooked_transferSlot(void* handler, int slot, int otherSlot) {
    if (g_inventory_lock) {
        return;
    }
    original_transferSlot(handler, slot, otherSlot);
}

// ============================================================
// NBT守护线程
// ============================================================
static void* guardian_thread(void* arg) {
    LOGI("NBT guardian thread started");
    g_guardian_running = true;

    JNIEnv* env = get_env();
    if (env == NULL) {
        LOGE("Failed to attach guardian thread");
        return NULL;
    }

    while (g_guardian_running) {
        sleep(1);
        jclass serverClass = (*env)->FindClass(env, "net/minecraft/server/MinecraftServer");
        if (serverClass == NULL) continue;
        jmethodID getInstance = (*env)->GetStaticMethodID(env, serverClass, "method_29740", "()Lnet/minecraft/server/MinecraftServer;");
        if (getInstance == NULL) continue;
        jobject server = (*env)->CallStaticObjectMethod(env, serverClass, getInstance);
        if (server == NULL) continue;
        jmethodID getPlayerManager = (*env)->GetMethodID(env, serverClass, "method_3822", "()Lnet/minecraft/server/PlayerManager;");
        if (getPlayerManager == NULL) continue;
        jobject playerManager = (*env)->CallObjectMethod(env, server, getPlayerManager);
        if (playerManager == NULL) continue;
        jclass pmClass = (*env)->GetObjectClass(env, playerManager);
        jmethodID getPlayerList = (*env)->GetMethodID(env, pmClass, "method_3840", "()Ljava/util/List;");
        if (getPlayerList == NULL) continue;
        jobject list = (*env)->CallObjectMethod(env, playerManager, getPlayerList);
        if (list == NULL) continue;
        jclass listClass = (*env)->GetObjectClass(env, list);
        jmethodID size = (*env)->GetMethodID(env, listClass, "size", "()I");
        jmethodID get = (*env)->GetMethodID(env, listClass, "get", "(I)Ljava/lang/Object;");
        if (size == NULL || get == NULL) continue;

        int len = (*env)->CallIntMethod(env, list, size);
        for (int i = 0; i < len; i++) {
            jobject player = (*env)->CallObjectMethod(env, list, get, i);
            if (player == NULL) continue;
            if (!is_local_player(env, player)) continue;
            jobject item = getMainHandItem(env, player);
            if (!isRainbowSword(env, item)) continue;

            jclass playerClass = (*env)->GetObjectClass(env, player);
            jmethodID getMainHandStack = (*env)->GetMethodID(env, playerClass, "method_5998", "()Lnet/minecraft/class_1799;");
            jobject stack = (*env)->CallObjectMethod(env, player, getMainHandStack);
            if (stack == NULL) continue;

            jclass stackClass = (*env)->FindClass(env, "net/minecraft/class_1799");
            jmethodID getOrCreateNbt = (*env)->GetMethodID(env, stackClass, "method_7993", "()Lnet/minecraft/class_2487;");
            jobject nbt = (*env)->CallObjectMethod(env, stack, getOrCreateNbt);
            if (nbt == NULL) continue;

            jclass nbtClass = (*env)->GetObjectClass(env, nbt);
            jmethodID getFloat = (*env)->GetMethodID(env, nbtClass, "method_10558", "(Ljava/lang/String;)F");
            jstring damageKey = (*env)->NewStringUTF(env, "BaseDamage");
            jfloat damage = (*env)->CallFloatMethod(env, nbt, getFloat, damageKey);
            if (damage < 999999.0f && damage > 0) {
                LOGW("NBT tamper detected: BaseDamage=%.1f, rolling back", damage);
                jmethodID putFloat = (*env)->GetMethodID(env, nbtClass, "method_10566", "(Ljava/lang/String;F)V");
                if (putFloat != NULL) {
                    (*env)->CallVoidMethod(env, nbt, putFloat, damageKey, 999999.0f);
                }
            }

            jmethodID getBoolean = (*env)->GetMethodID(env, nbtClass, "method_10556", "(Ljava/lang/String;)Z");
            jstring contractKey = (*env)->NewStringUTF(env, "HasContract");
            jboolean hasContract = (*env)->CallBooleanMethod(env, nbt, getBoolean, contractKey);
            if (!hasContract) {
                LOGW("NBT tamper detected: HasContract=false, restoring");
                jmethodID putBoolean = (*env)->GetMethodID(env, nbtClass, "method_10562", "(Ljava/lang/String;Z)V");
                if (putBoolean != NULL) {
                    (*env)->CallVoidMethod(env, nbt, putBoolean, contractKey, true);
                }
            }
        }
    }

    detach_env();
    LOGI("NBT guardian thread stopped");
    return NULL;
}

static void startGuardian() {
    if (g_guardian_thread == 0) {
        pthread_create(&g_guardian_thread, NULL, guardian_thread, NULL);
        LOGI("NBT guardian thread started");
    }
}

// ============================================================
// Hook安装入口
// ============================================================
JNIEXPORT void JNICALL Java_com_qidai_morefunctionalswordmod_NativeLoader_installNativeHook(
    JNIEnv* env,
    jobject obj,
    jlong addr_getHardness,
    jlong addr_damage,
    jlong addr_attackEntityFrom,
    jlong addr_sendPacket,
    jlong addr_addVelocity,
    jlong addr_handleFallDamage,
    jlong addr_getAttackCooldown,
    jlong addr_onDisconnect,
    jlong addr_transferSlot,
    jlong addr_getAttackRange,
    jlong addr_applyArmorToDamage,
    jlong addr_getAttackCooldownProgress,
    jlong addr_getBlockSpeedFactor,
    jlong addr_getFluidSpeed,
    jlong addr_pushOutOfBlocks,
    jlong addr_jump,
    jlong addr_renderFog,
    jlong addr_renderItem,
    jlong addr_chatMessageC2SPacket
) {
    LOGI("Installing native hooks...");

    if (addr_getHardness != 0) {
        DobbyHook((void*)addr_getHardness, (void*)hooked_getHardness, (void**)&original_getHardness);
        LOGI("Hook Block.getHardness");
    }
    if (addr_damage != 0) {
        DobbyHook((void*)addr_damage, (void*)hooked_damage, (void**)&original_damage);
        LOGI("Hook LivingEntity.damage");
    }
    if (addr_attackEntityFrom != 0) {
        DobbyHook((void*)addr_attackEntityFrom, (void*)hooked_attackEntityFrom, (void**)&original_attackEntityFrom);
        LOGI("Hook LivingEntity.attackEntityFrom");
    }
    if (addr_sendPacket != 0) {
        DobbyHook((void*)addr_sendPacket, (void*)hooked_sendPacket_anti, (void**)&original_sendPacket);
        LOGI("Hook sendPacket");
    }
    if (addr_addVelocity != 0) {
        DobbyHook((void*)addr_addVelocity, (void*)hooked_addVelocity, (void**)&original_addVelocity);
        LOGI("Hook addVelocity");
    }
    if (addr_handleFallDamage != 0) {
        DobbyHook((void*)addr_handleFallDamage, (void*)hooked_handleFallDamage, (void**)&original_handleFallDamage);
        LOGI("Hook handleFallDamage");
    }
    if (addr_getAttackCooldown != 0) {
        DobbyHook((void*)addr_getAttackCooldown, (void*)hooked_getAttackCooldown, (void**)&original_getAttackCooldown);
        LOGI("Hook getAttackCooldown");
    }
    if (addr_onDisconnect != 0) {
        DobbyHook((void*)addr_onDisconnect, (void*)hooked_onDisconnect, (void**)&original_onDisconnect);
        LOGI("Hook onDisconnect");
    }
    if (addr_transferSlot != 0) {
        DobbyHook((void*)addr_transferSlot, (void*)hooked_transferSlot, (void**)&original_transferSlot);
        LOGI("Hook transferSlot");
    }
    if (addr_getAttackRange != 0) {
        DobbyHook((void*)addr_getAttackRange, (void*)hooked_getAttackRange, (void**)&original_getAttackRange);
        LOGI("Hook getAttackRange");
    }
    if (addr_applyArmorToDamage != 0) {
        DobbyHook((void*)addr_applyArmorToDamage, (void*)hooked_applyArmorToDamage, (void**)&original_applyArmorToDamage);
        LOGI("Hook applyArmorToDamage");
    }
    if (addr_getAttackCooldownProgress != 0) {
        DobbyHook((void*)addr_getAttackCooldownProgress, (void*)hooked_getAttackCooldownProgress, (void**)&original_getAttackCooldown);
        LOGI("Hook getAttackCooldownProgress");
    }
    if (addr_getBlockSpeedFactor != 0) {
        DobbyHook((void*)addr_getBlockSpeedFactor, (void*)hooked_getBlockSpeedFactor, (void**)&original_getBlockSpeedFactor);
        LOGI("Hook getBlockSpeedFactor");
    }
    if (addr_getFluidSpeed != 0) {
        DobbyHook((void*)addr_getFluidSpeed, (void*)hooked_getFluidSpeed, (void**)&original_getFluidSpeed);
        LOGI("Hook getFluidSpeed");
    }
    if (addr_pushOutOfBlocks != 0) {
        DobbyHook((void*)addr_pushOutOfBlocks, (void*)hooked_pushOutOfBlocks, (void**)&original_pushOutOfBlocks);
        LOGI("Hook pushOutOfBlocks");
    }
    if (addr_jump != 0) {
        DobbyHook((void*)addr_jump, (void*)hooked_jump, (void**)&original_jump);
        LOGI("Hook jump");
    }
    if (addr_renderFog != 0) {
        DobbyHook((void*)addr_renderFog, (void*)hooked_renderFog, (void**)&original_renderFog);
        LOGI("Hook renderFog");
    }
    if (addr_renderItem != 0) {
        DobbyHook((void*)addr_renderItem, (void*)hooked_renderItem, (void**)&original_renderItem);
        LOGI("Hook renderItem");
    }
    if (addr_chatMessageC2SPacket != 0) {
        DobbyHook((void*)addr_chatMessageC2SPacket, (void*)hooked_chatMessageC2SPacket, (void**)&original_chatMessageC2SPacket);
        LOGI("Hook chatMessageC2SPacket");
    }

    g_hooks_installed = true;
    startGuardian();
    LOGI("All hooks installed");
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_jvm = vm;
    JNIEnv* env;
    (*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6);
    LOGI("JNI_OnLoad success");
    return JNI_VERSION_1_6;
}