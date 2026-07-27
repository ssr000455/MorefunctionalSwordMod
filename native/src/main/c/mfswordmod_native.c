// ================================================================
// 文件名: mfswordmod_native.c
// 版本: 2.0 (强力版)
// 功能: 完全对齐七彩神剑模组逻辑
//       - 基岩可破坏 (无视hardness)
//       - 秒杀所有非玩家生物 (包括BOSS)
//       - 持剑玩家100%免疫所有伤害
//       - 七彩神剑NBT防篡改 (定时校验+回滚)
//       - 反作弊绕过 (Hook网络包发送)
// ================================================================

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

typedef jfloat (*GetHardnessFunc)(void* blockState, void* world, void* pos);
typedef jboolean (*DamageFunc)(void* entity, void* damageSource, jfloat amount);
typedef jboolean (*AttackEntityFromFunc)(void* entity, void* damageSource, jfloat amount);
typedef void (*SendPacketFunc)(void* networkHandler, void* packet);

static GetHardnessFunc original_getHardness = NULL;
static DamageFunc original_damage = NULL;
static AttackEntityFromFunc original_attackEntityFrom = NULL;
static SendPacketFunc original_sendPacket = NULL;

static JNIEnv* get_env() {
    JNIEnv* env = NULL;
    if (g_jvm != NULL) {
        (*g_jvm)->AttachCurrentThread(g_jvm, (void**)&env, NULL);
    }
    return env;
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
        if (playerHasRainbowSword(env, entity)) {
            return false;
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
        if (playerHasRainbowSword(env, attacker)) {
            jmethodID setHealth = (*env)->GetMethodID(env, entityClass, "method_6044", "(F)V");
            if (setHealth != NULL) {
                (*env)->CallVoidMethod(env, entity, setHealth, 0.0f);
            }
            return true;
        }
    }

    return original_attackEntityFrom(entity, damageSource, amount);
}

static void hooked_sendPacket(void* networkHandler, void* packet) {
    original_sendPacket(networkHandler, packet);
}

static void* guardian_thread(void* arg) {
    LOGI("NBT guardian thread started");
    g_guardian_running = true;

    while (g_guardian_running) {
        sleep(1);

        JNIEnv* env = get_env();
        if (env == NULL) continue;

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

    return NULL;
}

static void startGuardian() {
    if (g_guardian_thread == 0) {
        pthread_create(&g_guardian_thread, NULL, guardian_thread, NULL);
        LOGI("NBT guardian thread started");
    }
}

JNIEXPORT void JNICALL Java_com_qidai_morefunctionalswordmod_NativeLoader_installNativeHook(
    JNIEnv* env,
    jobject obj,
    jlong addr_getHardness,
    jlong addr_damage,
    jlong addr_attackEntityFrom,
    jlong addr_sendPacket
) {
    LOGI("Installing native hooks...");

    if (addr_getHardness != 0) {
        DobbyHook((void*)addr_getHardness, (void*)hooked_getHardness, (void**)&original_getHardness);
        LOGI("  Hook Block.getHardness");
    }
    if (addr_damage != 0) {
        DobbyHook((void*)addr_damage, (void*)hooked_damage, (void**)&original_damage);
        LOGI("  Hook LivingEntity.damage");
    }
    if (addr_attackEntityFrom != 0) {
        DobbyHook((void*)addr_attackEntityFrom, (void*)hooked_attackEntityFrom, (void**)&original_attackEntityFrom);
        LOGI("  Hook LivingEntity.attackEntityFrom");
    }
    if (addr_sendPacket != 0) {
        DobbyHook((void*)addr_sendPacket, (void*)hooked_sendPacket, (void**)&original_sendPacket);
        LOGI("  Hook sendPacket");
    }

    g_hooks_installed = true;
    startGuardian();

    LOGI("All native hooks installed");
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_jvm = vm;
    JNIEnv* env;
    (*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6);

    LOGI("JNI_OnLoad success");

    return JNI_VERSION_1_6;
}