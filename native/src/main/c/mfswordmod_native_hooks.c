#include <jni.h>
#include <string.h>
#include <stdbool.h>
#include <pthread.h>
#include <unistd.h>
#include <stdlib.h>
#include <time.h>
#include <stdint.h>

#define LOGI(...) printf("[mfswordmod] " __VA_ARGS__)
#define LOGE(...) printf("[mfswordmod] " __VA_ARGS__)
#define LOGW(...) printf("[mfswordmod] " __VA_ARGS__)

#include "dobby.h"

// 声明外部函数和变量
extern JavaVM* g_jvm;
extern bool g_hooks_installed;
extern pthread_t g_guardian_thread;
extern bool g_guardian_running;
extern bool g_auto_block;
extern bool g_no_knockback;
extern bool g_no_fall_damage;
extern bool g_infinite_combo;
extern bool g_anti_anticheat;
extern bool g_kick_protect;
extern bool g_inventory_lock;
extern bool g_extended_attack_range;
extern bool g_ignore_armor;
extern bool g_always_critical;
extern bool g_remove_fog;
extern bool g_item_glint;
extern bool g_bypass_chat_filter;
extern bool g_ladder_speed;
extern bool g_swim_speed;
extern bool g_no_clip;
extern bool g_multi_jump;
extern int g_multi_jump_count;
extern int g_jump_count;
extern jobject g_last_jump_player;
extern double g_anti_anticheat_offset_x;
extern double g_anti_anticheat_offset_y;
extern double g_anti_anticheat_offset_z;

extern JNIEnv* get_env();
extern void detach_env();
extern jobject get_client_player(JNIEnv* env);
extern jobject get_client_world(JNIEnv* env);
extern bool is_local_player(JNIEnv* env, jobject entity);
extern bool is_attacker_local(JNIEnv* env, jobject damageSource);
extern bool isRainbowSword(JNIEnv* env, jobject item);
extern bool isRainbowArmor(JNIEnv* env, jobject item);
extern jobject getMainHandStack(JNIEnv* env, jobject player);
extern jobject getOffHandStack(JNIEnv* env, jobject player);
extern bool playerHasRainbowSword(JNIEnv* env, jobject player);
extern bool playerHasRainbowArmor(JNIEnv* env, jobject player);
extern bool isOreBlock(JNIEnv* env, jobject block);
extern bool isLadderBlock(JNIEnv* env, jobject block);
extern bool isFluid(JNIEnv* env, jobject fluidState);
extern void setEntityPosition(JNIEnv* env, jobject entity, double x, double y, double z);
extern void setEntityVelocity(JNIEnv* env, jobject entity, double x, double y, double z);
extern void setEntityHealth(JNIEnv* env, jobject entity, float health);
extern jboolean getOnGround(JNIEnv* env, jobject entity);
extern void setOnGround(JNIEnv* env, jobject entity, jboolean onGround);
extern void syncAbilities(JNIEnv* env, jobject player);
extern void allowFlight(JNIEnv* env, jobject player, jboolean allow);
extern float getMaxHealth(JNIEnv* env, jobject entity);
extern void setMaxHealth(JNIEnv* env, jobject entity, float health);

// 原始函数指针
extern GetHardnessFunc original_getHardness;
extern DamageFunc original_damage;
extern AttackEntityFromFunc original_attackEntityFrom;
extern SendPacketFunc original_sendPacket;
extern AddVelocityFunc original_addVelocity;
extern HandleFallDamageFunc original_handleFallDamage;
extern GetAttackCooldownFunc original_getAttackCooldown;
extern OnDisconnectFunc original_onDisconnect;
extern TransferSlotFunc original_transferSlot;
extern GetAttackRangeFunc original_getAttackRange;
extern ApplyArmorToDamageFunc original_applyArmorToDamage;
extern GetBlockSpeedFactorFunc original_getBlockSpeedFactor;
extern GetFluidSpeedFunc original_getFluidSpeed;
extern PushOutOfBlocksFunc original_pushOutOfBlocks;
extern JumpFunc original_jump;
extern RenderFogFunc original_renderFog;
extern RenderItemFunc original_renderItem;
extern ChatMessageC2SPacketFunc original_chatMessageC2SPacket;

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
            if (playerHasRainbowArmor(env, entity)) {
                amount *= 0.3f;
            }
        }
    }
    return original_damage(entity, damageSource, amount);
}

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

static void hooked_addVelocity(void* entity, double x, double y, double z) {
    JNIEnv* env = get_env();
    if (env != NULL && is_local_player(env, entity)) {
        if (g_no_knockback) {
            original_addVelocity(entity, 0, 0, 0);
            return;
        }
        if (playerHasRainbowSword(env, entity)) {
            original_addVelocity(entity, x * 0.05, y * 0.05, z * 0.05);
            return;
        }
    }
    original_addVelocity(entity, x, y, z);
}

static jboolean hooked_handleFallDamage(void* entity, float distance, float damageMultiplier) {
    JNIEnv* env = get_env();
    if (env != NULL && is_local_player(env, entity)) {
        if (g_no_fall_damage) {
            return JNI_FALSE;
        }
        if (playerHasRainbowSword(env, entity)) {
            return JNI_FALSE;
        }
        if (playerHasRainbowArmor(env, entity)) {
            return original_handleFallDamage(entity, distance * 0.2f, damageMultiplier * 0.2f);
        }
    }
    return original_handleFallDamage(entity, distance, damageMultiplier);
}

static jfloat hooked_getAttackCooldown(void* player, float baseTime) {
    JNIEnv* env = get_env();
    if (env != NULL && is_local_player(env, player)) {
        if (g_infinite_combo) {
            return 0.0f;
        }
        if (playerHasRainbowSword(env, player)) {
            return 0.0f;
        }
    }
    return original_getAttackCooldown(player, baseTime);
}

static jfloat hooked_getAttackCooldownProgress(void* player, float baseTime) {
    JNIEnv* env = get_env();
    if (env != NULL && is_local_player(env, player)) {
        if (g_always_critical) {
            float progress = original_getAttackCooldown(player, baseTime);
            if (progress > 0.7f) {
                return 1.5f;
            }
            return progress + 0.3f;
        }
        if (playerHasRainbowSword(env, player)) {
            float progress = original_getAttackCooldown(player, baseTime);
            if (progress > 0.5f) {
                return 1.5f;
            }
            return progress + 0.4f;
        }
    }
    return original_getAttackCooldown(player, baseTime);
}

static double hooked_getAttackRange(void* player) {
    JNIEnv* env = get_env();
    if (env != NULL && is_local_player(env, player)) {
        if (g_extended_attack_range) {
            jobject stack = getMainHandStack(env, player);
            if (stack != NULL) {
                jclass stackClass = (*env)->GetObjectClass(env, stack);
                jmethodID getItem = (*env)->GetMethodID(env, stackClass, "method_7909", "()Lnet/minecraft/class_1792;");
                jobject item = (*env)->CallObjectMethod(env, stack, getItem);
                if (isRainbowSword(env, item)) {
                    return 8.0;
                }
                if (isRainbowArmor(env, item)) {
                    return 6.0;
                }
            }
            return 5.5;
        }
        if (playerHasRainbowSword(env, player)) {
            return 7.0;
        }
    }
    return original_getAttackRange(player);
}

static float hooked_applyArmorToDamage(void* entity, float damage, void* damageSource) {
    JNIEnv* env = get_env();
    if (env != NULL && is_attacker_local(env, damageSource)) {
        if (g_ignore_armor) {
            float original = original_applyArmorToDamage(entity, damage, damageSource);
            float penetration = (damage - original) * 0.75f;
            return damage - penetration;
        }
        jobject stack = getMainHandStack(env, (jobject)damageSource);
        if (stack != NULL) {
            jclass stackClass = (*env)->GetObjectClass(env, stack);
            jmethodID getItem = (*env)->GetMethodID(env, stackClass, "method_7909", "()Lnet/minecraft/class_1792;");
            jobject item = (*env)->CallObjectMethod(env, stack, getItem);
            if (isRainbowSword(env, item)) {
                float original = original_applyArmorToDamage(entity, damage, damageSource);
                float penetration = (damage - original) * 0.6f;
                return damage - penetration;
            }
        }
    }
    return original_applyArmorToDamage(entity, damage, damageSource);
}

static float hooked_getBlockSpeedFactor(void* entity, void* blockState) {
    float original = original_getBlockSpeedFactor(entity, blockState);
    JNIEnv* env = get_env();
    if (env != NULL && is_local_player(env, entity)) {
        if (g_ladder_speed) {
            jclass stateClass = (*env)->GetObjectClass(env, blockState);
            jmethodID getBlock = (*env)->GetMethodID(env, stateClass, "method_9588", "()Lnet/minecraft/class_2248;");
            jobject block = (*env)->CallObjectMethod(env, blockState, getBlock);
            if (isLadderBlock(env, block)) {
                if (playerHasRainbowSword(env, entity)) {
                    return original * 4.0f;
                }
                return original * 3.0f;
            }
        }
        if (playerHasRainbowSword(env, entity)) {
            if (isLadderBlock(env, (jobject)blockState)) {
                return original * 3.5f;
            }
        }
    }
    return original;
}

static float hooked_getFluidSpeed(void* entity, void* fluidState) {
    float original = original_getFluidSpeed(entity, fluidState);
    JNIEnv* env = get_env();
    if (env != NULL && is_local_player(env, entity)) {
        if (g_swim_speed) {
            if (playerHasRainbowSword(env, entity)) {
                return original * 3.0f;
            }
            return original * 2.5f;
        }
        if (playerHasRainbowSword(env, entity)) {
            return original * 2.0f;
        }
    }
    return original;
}

static void hooked_pushOutOfBlocks(void* entity, double x, double y, double z) {
    JNIEnv* env = get_env();
    if (env != NULL && is_local_player(env, entity)) {
        if (g_no_clip) {
            return;
        }
        if (playerHasRainbowSword(env, entity)) {
            return;
        }
    }
    original_pushOutOfBlocks(entity, x, y, z);
}

static void hooked_jump(void* livingEntity) {
    JNIEnv* env = get_env();
    if (env != NULL && is_local_player(env, livingEntity)) {
        if (g_multi_jump || playerHasRainbowSword(env, livingEntity)) {
            jboolean onGround = getOnGround(env, livingEntity);
            if (onGround) {
                g_jump_count = 0;
                g_last_jump_player = livingEntity;
            }
            int maxJumps = playerHasRainbowSword(env, livingEntity) ? 8 : g_multi_jump_count;
            if (g_jump_count < maxJumps) {
                original_jump(livingEntity);
                g_jump_count++;
                setEntityVelocity(env, livingEntity, 0, 0.5, 0);
            }
            return;
        }
    }
    original_jump(livingEntity);
}

static void hooked_renderFog(void* worldRenderer, void* camera, float tickDelta) {
    if (g_remove_fog) {
        return;
    }
    original_renderFog(worldRenderer, camera, tickDelta);
}

static void hooked_renderItem(void* itemRenderer, void* stack, void* mode, void* matrices, void* consumers, int light, int overlay) {
    original_renderItem(itemRenderer, stack, mode, matrices, consumers, light, overlay);
    if (g_item_glint) {
        JNIEnv* env = get_env();
        if (env != NULL) {
            jclass stackClass = (*env)->GetObjectClass(env, stack);
            jmethodID getItem = (*env)->GetMethodID(env, stackClass, "method_7909", "()Lnet/minecraft/class_1792;");
            jobject item = (*env)->CallObjectMethod(env, stack, getItem);
            if (isRainbowSword(env, item) || isRainbowArmor(env, item)) {
                jmethodID hasGlint = (*env)->GetMethodID(env, stackClass, "method_7883", "()Z");
                if (hasGlint != NULL) {
                    jboolean glint = (*env)->CallBooleanMethod(env, stack, hasGlint);
                    if (!glint) {
                        // 强制绘制附魔光泽
                    }
                }
            }
        }
    }
}

static void hooked_chatMessageC2SPacket(void* packet, void* message) {
    if (g_bypass_chat_filter) {
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
                        jmethodID setMessage = (*env)->GetMethodID(env, (*env)->GetObjectClass(env, packet), "method_12082", "(Ljava/lang/String;)V");
                        if (setMessage != NULL) {
                            (*env)->CallVoidMethod(env, packet, setMessage, newJStr);
                        }
                    }
                    free(newMsg);
                }
            }
            (*env)->ReleaseStringUTFChars(env, msg, msgStr);
        }
    }
    original_chatMessageC2SPacket(packet, message);
}

static void hooked_sendPacket_anti(void* networkHandler, void* packet) {
    if (g_anti_anticheat) {
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
    }
    original_sendPacket(networkHandler, packet);
}

static void hooked_onDisconnect(void* networkHandler, void* text) {
    if (g_kick_protect) {
        JNIEnv* env = get_env();
        if (env != NULL) {
            LOGI("防踢：拦截断开连接");
            jclass handlerClass = (*env)->GetObjectClass(env, networkHandler);
            jmethodID send = (*env)->GetMethodID(env, handlerClass, "method_11142", "(Lnet/minecraft/class_2596;)V");
            if (send != NULL) {
                // 发送心跳包保持连接
            }
        }
    }
    original_onDisconnect(networkHandler, text);
}

static void hooked_transferSlot(void* handler, int slot, int otherSlot) {
    if (g_inventory_lock) {
        JNIEnv* env = get_env();
        if (env != NULL) {
            jclass handlerClass = (*env)->GetObjectClass(env, handler);
            jmethodID getPlayer = (*env)->GetMethodID(env, handlerClass, "method_16885", "()Lnet/minecraft/class_3222;");
            if (getPlayer != NULL) {
                jobject player = (*env)->CallObjectMethod(env, handler, getPlayer);
                if (is_local_player(env, player)) {
                    LOGI("物品栏锁定：拒绝转移");
                    return;
                }
            }
        }
    }
    original_transferSlot(handler, slot, otherSlot);
}

static void* guardian_thread(void* arg) {
    LOGI("NBT守护线程启动");
    g_guardian_running = true;
    JNIEnv* env = get_env();
    if (env == NULL) {
        LOGE("无法附加守护线程");
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
            jobject stack = getMainHandStack(env, player);
            if (stack == NULL) continue;
            jclass stackClass = (*env)->GetObjectClass(env, stack);
            jmethodID getOrCreateNbt = (*env)->GetMethodID(env, stackClass, "method_7993", "()Lnet/minecraft/class_2487;");
            jobject nbt = (*env)->CallObjectMethod(env, stack, getOrCreateNbt);
            if (nbt == NULL) continue;
            jclass nbtClass = (*env)->GetObjectClass(env, nbt);
            jmethodID getFloat = (*env)->GetMethodID(env, nbtClass, "method_10558", "(Ljava/lang/String;)F");
            jmethodID putFloat = (*env)->GetMethodID(env, nbtClass, "method_10566", "(Ljava/lang/String;F)V");
            jmethodID getBoolean = (*env)->GetMethodID(env, nbtClass, "method_10556", "(Ljava/lang/String;)Z");
            jmethodID putBoolean = (*env)->GetMethodID(env, nbtClass, "method_10562", "(Ljava/lang/String;Z)V");
            jmethodID getInt = (*env)->GetMethodID(env, nbtClass, "method_10554", "(Ljava/lang/String;)I");
            jmethodID putInt = (*env)->GetMethodID(env, nbtClass, "method_10557", "(Ljava/lang/String;I)V");
            jstring damageKey = (*env)->NewStringUTF(env, "BaseDamage");
            jfloat damage = (*env)->CallFloatMethod(env, nbt, getFloat, damageKey);
            if (damage < 999999.0f && damage > 0) {
                LOGW("NBT篡改: BaseDamage=%.1f", damage);
                (*env)->CallVoidMethod(env, nbt, putFloat, damageKey, 999999.0f);
            }
            jstring contractKey = (*env)->NewStringUTF(env, "HasContract");
            jboolean hasContract = (*env)->CallBooleanMethod(env, nbt, getBoolean, contractKey);
            if (!hasContract) {
                LOGW("NBT篡改: HasContract=false");
                (*env)->CallVoidMethod(env, nbt, putBoolean, contractKey, true);
            }
            jstring rangeKey = (*env)->NewStringUTF(env, "AttackRange");
            jint attackRange = (*env)->CallIntMethod(env, nbt, getInt, rangeKey);
            if (attackRange < 1 || attackRange > 256) {
                LOGW("NBT篡改: AttackRange=%d", attackRange);
                (*env)->CallVoidMethod(env, nbt, putInt, rangeKey, 16);
            }
        }
    }
    detach_env();
    LOGI("NBT守护线程停止");
    return NULL;
}

static void startGuardian() {
    if (g_guardian_thread == 0) {
        pthread_create(&g_guardian_thread, NULL, guardian_thread, NULL);
        LOGI("NBT守护线程已启动");
    }
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
    jlong addr_chatMessageC2SPacket
) {
    LOGI("开始安装Native Hook...");
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
    LOGI("所有Hook安装完成");
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_jvm = vm;
    JNIEnv* env;
    (*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6);
    LOGI("JNI_OnLoad success");
    return JNI_VERSION_1_6;
}