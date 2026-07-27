#include "mfswordmod_native_config.h"
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

extern JavaVM* g_jvm;
extern int g_jump_count;
extern jobject g_last_jump_player;
extern double g_anti_anticheat_offset_x;
extern double g_anti_anticheat_offset_y;
extern double g_anti_anticheat_offset_z;

extern JNIEnv* get_env();
extern void detach_env();
extern jobject get_client_player(JNIEnv* env);
extern bool is_local_player(JNIEnv* env, jobject entity);
extern bool is_attacker_local(JNIEnv* env, jobject damageSource);
extern bool isRainbowSword(JNIEnv* env, jobject item);
extern bool isRainbowArmor(JNIEnv* env, jobject item);
extern jobject getMainHandStack(JNIEnv* env, jobject player);
extern bool playerHasRainbowSword(JNIEnv* env, jobject player);
extern bool playerHasRainbowArmor(JNIEnv* env, jobject player);
extern bool isLadderBlock(JNIEnv* env, jobject block);
extern void setEntityHealth(JNIEnv* env, jobject entity, float health);

GetHardnessFunc original_getHardness = NULL;
DamageFunc original_damage = NULL;
AttackEntityFromFunc original_attackEntityFrom = NULL;
SendPacketFunc original_sendPacket = NULL;
AddVelocityFunc original_addVelocity = NULL;
HandleFallDamageFunc original_handleFallDamage = NULL;
GetAttackCooldownFunc original_getAttackCooldown = NULL;
OnDisconnectFunc original_onDisconnect = NULL;
TransferSlotFunc original_transferSlot = NULL;
GetAttackRangeFunc original_getAttackRange = NULL;
ApplyArmorToDamageFunc original_applyArmorToDamage = NULL;
GetBlockSpeedFactorFunc original_getBlockSpeedFactor = NULL;
GetFluidSpeedFunc original_getFluidSpeed = NULL;
PushOutOfBlocksFunc original_pushOutOfBlocks = NULL;
JumpFunc original_jump = NULL;
RenderFogFunc original_renderFog = NULL;
RenderItemFunc original_renderItem = NULL;
ChatMessageC2SPacketFunc original_chatMessageC2SPacket = NULL;

static jfloat hooked_getHardness(void* blockState, void* world, void* pos) {
    if (!get_config_bool("enable_bedrock_break")) {
        return original_getHardness(blockState, world, pos);
    }
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

static jboolean hooked_damage(void* entity, void* damageSource, jfloat amount) {
    int enable_auto_block = get_config_bool("enable_auto_block");
    int enable_rainbow_immunity = get_config_bool("enable_rainbow_immunity");
    if (!enable_auto_block && !enable_rainbow_immunity) {
        return original_damage(entity, damageSource, amount);
    }
    JNIEnv* env = get_env();
    if (env == NULL) return original_damage(entity, damageSource, amount);
    jclass entityClass = (*env)->GetObjectClass(env, entity);
    jclass playerClass = (*env)->FindClass(env, "net/minecraft/class_3222");
    if (playerClass == NULL) return original_damage(entity, damageSource, amount);
    if ((*env)->IsInstanceOf(env, entity, playerClass)) {
        if (is_local_player(env, entity)) {
            if (enable_auto_block) {
                jmethodID setBlocking = (*env)->GetMethodID(env, entityClass, "method_6067", "(Z)V");
                if (setBlocking != NULL) {
                    (*env)->CallVoidMethod(env, entity, setBlocking, JNI_TRUE);
                }
                amount *= 0.2f;
            }
            if (enable_rainbow_immunity && playerHasRainbowSword(env, entity)) {
                return false;
            }
            if (enable_rainbow_immunity && playerHasRainbowArmor(env, entity)) {
                amount *= 0.3f;
            }
        }
    }
    return original_damage(entity, damageSource, amount);
}

static jboolean hooked_attackEntityFrom(void* entity, void* damageSource, jfloat amount) {
    if (!get_config_bool("enable_rainbow_kill")) {
        return original_attackEntityFrom(entity, damageSource, amount);
    }
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

static void hooked_addVelocity(void* entity, double x, double y, double z) {
    if (!get_config_bool("enable_no_knockback")) {
        original_addVelocity(entity, x, y, z);
        return;
    }
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_addVelocity(entity, x, y, z);
        return;
    }
    if (is_local_player(env, entity)) {
        float reserve = get_config_float("knockback_reserve_rate");
        original_addVelocity(entity, x * reserve, y * reserve, z * reserve);
        return;
    }
    original_addVelocity(entity, x, y, z);
}

static jboolean hooked_handleFallDamage(void* entity, float distance, float damageMultiplier) {
    if (!get_config_bool("enable_no_fall_damage")) {
        return original_handleFallDamage(entity, distance, damageMultiplier);
    }
    JNIEnv* env = get_env();
    if (env == NULL) return original_handleFallDamage(entity, distance, damageMultiplier);
    if (is_local_player(env, entity)) {
        return JNI_FALSE;
    }
    return original_handleFallDamage(entity, distance, damageMultiplier);
}

static jfloat hooked_getAttackCooldown(void* player, float baseTime) {
    if (!get_config_bool("enable_infinite_combo")) {
        return original_getAttackCooldown(player, baseTime);
    }
    JNIEnv* env = get_env();
    if (env == NULL) return original_getAttackCooldown(player, baseTime);
    if (is_local_player(env, player)) {
        return 0.0f;
    }
    return original_getAttackCooldown(player, baseTime);
}

static jfloat hooked_getAttackCooldownProgress(void* player, float baseTime) {
    if (!get_config_bool("enable_always_critical")) {
        return original_getAttackCooldown(player, baseTime);
    }
    JNIEnv* env = get_env();
    if (env == NULL) return original_getAttackCooldown(player, baseTime);
    if (is_local_player(env, player)) {
        float progress = original_getAttackCooldown(player, baseTime);
        if (progress > 0.7f) {
            return 1.5f;
        }
        return progress + 0.3f;
    }
    return original_getAttackCooldown(player, baseTime);
}

static double hooked_getAttackRange(void* player) {
    if (!get_config_bool("enable_extended_attack_range")) {
        return original_getAttackRange(player);
    }
    JNIEnv* env = get_env();
    if (env == NULL) return original_getAttackRange(player);
    if (is_local_player(env, player)) {
        int range = get_config_int("attack_range_value");
        if (playerHasRainbowSword(env, player)) {
            return range + 2.0;
        }
        return range;
    }
    return original_getAttackRange(player);
}

static float hooked_applyArmorToDamage(void* entity, float damage, void* damageSource) {
    if (!get_config_bool("enable_ignore_armor")) {
        return original_applyArmorToDamage(entity, damage, damageSource);
    }
    JNIEnv* env = get_env();
    if (env == NULL) return original_applyArmorToDamage(entity, damage, damageSource);
    if (is_attacker_local(env, damageSource)) {
        float penetration = get_config_float("armor_penetration_rate");
        float original = original_applyArmorToDamage(entity, damage, damageSource);
        return damage - (damage - original) * penetration;
    }
    return original_applyArmorToDamage(entity, damage, damageSource);
}

static float hooked_getBlockSpeedFactor(void* entity, void* blockState) {
    if (!get_config_bool("enable_ladder_speed")) {
        return original_getBlockSpeedFactor(entity, blockState);
    }
    float original = original_getBlockSpeedFactor(entity, blockState);
    JNIEnv* env = get_env();
    if (env == NULL) return original;
    if (is_local_player(env, entity)) {
        jclass stateClass = (*env)->GetObjectClass(env, blockState);
        jmethodID getBlock = (*env)->GetMethodID(env, stateClass, "method_9588", "()Lnet/minecraft/class_2248;");
        jobject block = (*env)->CallObjectMethod(env, blockState, getBlock);
        if (isLadderBlock(env, block)) {
            float mult = get_config_float("ladder_speed_multiplier");
            return original * mult;
        }
    }
    return original;
}

static float hooked_getFluidSpeed(void* entity, void* fluidState) {
    if (!get_config_bool("enable_swim_speed")) {
        return original_getFluidSpeed(entity, fluidState);
    }
    float original = original_getFluidSpeed(entity, fluidState);
    JNIEnv* env = get_env();
    if (env == NULL) return original;
    if (is_local_player(env, entity)) {
        float mult = get_config_float("swim_speed_multiplier");
        return original * mult;
    }
    return original;
}

static void hooked_pushOutOfBlocks(void* entity, double x, double y, double z) {
    if (!get_config_bool("enable_no_clip")) {
        original_pushOutOfBlocks(entity, x, y, z);
        return;
    }
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_pushOutOfBlocks(entity, x, y, z);
        return;
    }
    if (is_local_player(env, entity)) {
        return;
    }
    original_pushOutOfBlocks(entity, x, y, z);
}

static void hooked_jump(void* livingEntity) {
    if (!get_config_bool("enable_multi_jump")) {
        original_jump(livingEntity);
        return;
    }
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_jump(livingEntity);
        return;
    }
    if (is_local_player(env, livingEntity)) {
        jclass entityClass = (*env)->GetObjectClass(env, livingEntity);
        jmethodID isOnGround = (*env)->GetMethodID(env, entityClass, "method_18790", "()Z");
        if (isOnGround != NULL) {
            jboolean onGround = (*env)->CallBooleanMethod(env, livingEntity, isOnGround);
            if (onGround) {
                g_jump_count = 0;
                g_last_jump_player = livingEntity;
            }
        }
        int maxJumps = get_config_int("multi_jump_count");
        if (playerHasRainbowSword(env, livingEntity)) {
            maxJumps += 3;
        }
        if (g_jump_count < maxJumps) {
            original_jump(livingEntity);
            g_jump_count++;
        }
        return;
    }
    original_jump(livingEntity);
}

static void hooked_renderFog(void* worldRenderer, void* camera, float tickDelta) {
    if (!get_config_bool("enable_remove_fog")) {
        original_renderFog(worldRenderer, camera, tickDelta);
        return;
    }
    return;
}

static void hooked_renderItem(void* itemRenderer, void* stack, void* mode, void* matrices, void* consumers, int light, int overlay) {
    original_renderItem(itemRenderer, stack, mode, matrices, consumers, light, overlay);
    if (!get_config_bool("enable_item_glint")) {
        return;
    }
    JNIEnv* env = get_env();
    if (env == NULL) return;
    jclass stackClass = (*env)->GetObjectClass(env, stack);
    jmethodID getItem = (*env)->GetMethodID(env, stackClass, "method_7909", "()Lnet/minecraft/class_1792;");
    jobject item = (*env)->CallObjectMethod(env, stack, getItem);
    if (isRainbowSword(env, item) || isRainbowArmor(env, item)) {
        // 强制绘制附魔光泽 (具体实现需调用RenderLayer)
    }
}

static void hooked_chatMessageC2SPacket(void* packet, void* message) {
    if (!get_config_bool("enable_bypass_chat_filter")) {
        original_chatMessageC2SPacket(packet, message);
        return;
    }
    JNIEnv* env = get_env();
    if (env != NULL && message != NULL) {
        jstring msg = (jstring)message;
        const char* msgStr = (*env)->GetStringUTFChars(env, msg, NULL);
        if (msgStr != NULL) {
            size_t len = strlen(msgStr);
            char* newMsg = malloc(len + 5);
            if (newMsg != NULL) {
                const unsigned char zeroWidthSpace[] = {0xE2, 0x80, 0x8B, 0x00};
                const unsigned char zeroWidthJoiner[] = {0xE2, 0x80, 0x8D, 0x00};
                snprintf(newMsg, len + 5, "%s%s%s", zeroWidthSpace, msgStr, zeroWidthJoiner);
                jstring newJStr = (*env)->NewStringUTF(env, newMsg);
                if (newJStr != NULL) {
                    jclass packetClass = (*env)->GetObjectClass(env, packet);
                    jmethodID setMessage = (*env)->GetMethodID(env, packetClass, "method_12082", "(Ljava/lang/String;)V");
                    if (setMessage != NULL) {
                        (*env)->CallVoidMethod(env, packet, setMessage, newJStr);
                    }
                }
                free(newMsg);
            }
        }
        (*env)->ReleaseStringUTFChars(env, msg, msgStr);
    }
    original_chatMessageC2SPacket(packet, message);
}

static void hooked_sendPacket_anti(void* networkHandler, void* packet) {
    if (!get_config_bool("enable_anti_anticheat")) {
        original_sendPacket(networkHandler, packet);
        return;
    }
    JNIEnv* env = get_env();
    if (env != NULL) {
        jclass packetClass = (*env)->GetObjectClass(env, packet);
        jclass posClass = (*env)->FindClass(env, "net/minecraft/class_2748");
        if (posClass != NULL && (*env)->IsInstanceOf(env, packet, posClass)) {
            jmethodID getX = (*env)->GetMethodID(env, posClass, "method_12637", "()D");
            jmethodID getY = (*env)->GetMethodID(env, posClass, "method_12639", "()D");
            jmethodID getZ = (*env)->GetMethodID(env, posClass, "method_12638", "()D");
            jmethodID setX = (*env)->GetMethodID(env, posClass, "method_12641", "(D)V");
            jmethodID setY = (*env)->GetMethodID(env, posClass, "method_12643", "(D)V");
            jmethodID setZ = (*env)->GetMethodID(env, posClass, "method_12642", "(D)V");
            if (getX != NULL && setX != NULL) {
                double x = (*env)->CallDoubleMethod(env, packet, getX);
                double y = (*env)->CallDoubleMethod(env, packet, getY);
                double z = (*env)->CallDoubleMethod(env, packet, getZ);
                double noise = 0.0005;
                g_anti_anticheat_offset_x += (rand() / (double)RAND_MAX - 0.5) * noise * 2;
                g_anti_anticheat_offset_y += (rand() / (double)RAND_MAX - 0.5) * noise * 2;
                g_anti_anticheat_offset_z += (rand() / (double)RAND_MAX - 0.5) * noise * 2;
                if (g_anti_anticheat_offset_x > 0.01) g_anti_anticheat_offset_x = 0.01;
                if (g_anti_anticheat_offset_x < -0.01) g_anti_anticheat_offset_x = -0.01;
                if (g_anti_anticheat_offset_y > 0.01) g_anti_anticheat_offset_y = 0.01;
                if (g_anti_anticheat_offset_y < -0.01) g_anti_anticheat_offset_y = -0.01;
                if (g_anti_anticheat_offset_z > 0.01) g_anti_anticheat_offset_z = 0.01;
                if (g_anti_anticheat_offset_z < -0.01) g_anti_anticheat_offset_z = -0.01;
                x += g_anti_anticheat_offset_x;
                y += g_anti_anticheat_offset_y;
                z += g_anti_anticheat_offset_z;
                (*env)->CallVoidMethod(env, packet, setX, x);
                (*env)->CallVoidMethod(env, packet, setY, y);
                (*env)->CallVoidMethod(env, packet, setZ, z);
            }
        }
    }
    original_sendPacket(networkHandler, packet);
}

static void hooked_onDisconnect_protect(void* networkHandler, void* text) {
    if (!get_config_bool("enable_kick_protect")) {
        original_onDisconnect(networkHandler, text);
        return;
    }
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_onDisconnect(networkHandler, text);
        return;
    }
    jclass handlerClass = (*env)->GetObjectClass(env, networkHandler);
    jmethodID getPlayer = (*env)->GetMethodID(env, handlerClass, "method_11075", "()Lnet/minecraft/class_3222;");
    if (getPlayer == NULL) {
        getPlayer = (*env)->GetMethodID(env, handlerClass, "method_11088", "()Lnet/minecraft/class_3222;");
    }
    if (getPlayer != NULL) {
        jobject player = (*env)->CallObjectMethod(env, networkHandler, getPlayer);
        if (is_local_player(env, player) && playerHasRainbowSword(env, player)) {
            return;
        }
    }
    original_onDisconnect(networkHandler, text);
}

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
    jlong addr_chatMessageC2SPacket) {

    load_config();

    if (addr_getHardness != 0) {
        DobbyHook((void*)addr_getHardness, (void*)hooked_getHardness, (void**)&original_getHardness);
    }
    if (addr_damage != 0) {
        DobbyHook((void*)addr_damage, (void*)hooked_damage, (void**)&original_damage);
    }
    if (addr_attackEntityFrom != 0) {
        DobbyHook((void*)addr_attackEntityFrom, (void*)hooked_attackEntityFrom, (void**)&original_attackEntityFrom);
    }
    if (addr_sendPacket != 0) {
        DobbyHook((void*)addr_sendPacket, (void*)hooked_sendPacket_anti, (void**)&original_sendPacket);
    }
    if (addr_addVelocity != 0) {
        DobbyHook((void*)addr_addVelocity, (void*)hooked_addVelocity, (void**)&original_addVelocity);
    }
    if (addr_handleFallDamage != 0) {
        DobbyHook((void*)addr_handleFallDamage, (void*)hooked_handleFallDamage, (void**)&original_handleFallDamage);
    }
    if (addr_getAttackCooldown != 0) {
        DobbyHook((void*)addr_getAttackCooldown, (void*)hooked_getAttackCooldown, (void**)&original_getAttackCooldown);
    }
    if (addr_onDisconnect != 0) {
        DobbyHook((void*)addr_onDisconnect, (void*)hooked_onDisconnect_protect, (void**)&original_onDisconnect);
    }
    if (addr_transferSlot != 0) {
        DobbyHook((void*)addr_transferSlot, (void*)hooked_transferSlot, (void**)&original_transferSlot);
    }
    if (addr_getAttackRange != 0) {
        DobbyHook((void*)addr_getAttackRange, (void*)hooked_getAttackRange, (void**)&original_getAttackRange);
    }
    if (addr_applyArmorToDamage != 0) {
        DobbyHook((void*)addr_applyArmorToDamage, (void*)hooked_applyArmorToDamage, (void**)&original_applyArmorToDamage);
    }
    if (addr_getAttackCooldownProgress != 0) {
        DobbyHook((void*)addr_getAttackCooldownProgress, (void*)hooked_getAttackCooldownProgress, (void**)&original_getAttackCooldown);
    }
    if (addr_getBlockSpeedFactor != 0) {
        DobbyHook((void*)addr_getBlockSpeedFactor, (void*)hooked_getBlockSpeedFactor, (void**)&original_getBlockSpeedFactor);
    }
    if (addr_getFluidSpeed != 0) {
        DobbyHook((void*)addr_getFluidSpeed, (void*)hooked_getFluidSpeed, (void**)&original_getFluidSpeed);
    }
    if (addr_pushOutOfBlocks != 0) {
        DobbyHook((void*)addr_pushOutOfBlocks, (void*)hooked_pushOutOfBlocks, (void**)&original_pushOutOfBlocks);
    }
    if (addr_jump != 0) {
        DobbyHook((void*)addr_jump, (void*)hooked_jump, (void**)&original_jump);
    }
    if (addr_renderFog != 0) {
        DobbyHook((void*)addr_renderFog, (void*)hooked_renderFog, (void**)&original_renderFog);
    }
    if (addr_renderItem != 0) {
        DobbyHook((void*)addr_renderItem, (void*)hooked_renderItem, (void**)&original_renderItem);
    }
    if (addr_chatMessageC2SPacket != 0) {
        DobbyHook((void*)addr_chatMessageC2SPacket, (void*)hooked_chatMessageC2SPacket, (void**)&original_chatMessageC2SPacket);
    }
}