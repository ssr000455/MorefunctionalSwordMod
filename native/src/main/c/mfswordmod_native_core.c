#include "mfswordmod_native_config.h"
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

JavaVM* g_jvm = NULL;
bool g_hooks_installed = false;
pthread_t g_guardian_thread = 0;
bool g_guardian_running = false;
int g_jump_count = 0;
jobject g_last_jump_player = NULL;
double g_anti_anticheat_offset_x = 0.0;
double g_anti_anticheat_offset_y = 0.0;
double g_anti_anticheat_offset_z = 0.0;

// ============================================================
// JNI 工具函数
// ============================================================
JNIEnv* get_env() {
    JNIEnv* env = NULL;
    if (g_jvm != NULL) {
        (*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL);
    }
    return env;
}

void detach_env() {
    if (g_jvm != NULL) {
        (*g_jvm)->DetachCurrentThread(g_jvm);
    }
}

jobject get_client_player(JNIEnv* env) {
    jclass mcClass = (*env)->FindClass(env, "net/minecraft/class_310");
    if (mcClass == NULL) return NULL;
    jmethodID getInstance = (*env)->GetStaticMethodID(env, mcClass, "method_1551", "()Lnet/minecraft/class_310;");
    if (getInstance == NULL) return NULL;
    jobject mc = (*env)->CallStaticObjectMethod(env, mcClass, getInstance);
    if (mc == NULL) return NULL;
    jmethodID getPlayer = (*env)->GetMethodID(env, mcClass, "method_1528", "()Lnet/minecraft/class_746;");
    return (*env)->CallObjectMethod(env, mc, getPlayer);
}

bool is_local_player(JNIEnv* env, jobject entity) {
    if (entity == NULL) return false;
    jobject local = get_client_player(env);
    if (local == NULL) return false;
    return (*env)->IsSameObject(env, entity, local);
}

bool is_attacker_local(JNIEnv* env, jobject damageSource) {
    if (damageSource == NULL) return false;
    jclass dsClass = (*env)->GetObjectClass(env, damageSource);
    jmethodID getAttacker = (*env)->GetMethodID(env, dsClass, "method_11598", "()Lnet/minecraft/class_1297;");
    jobject attacker = (*env)->CallObjectMethod(env, damageSource, getAttacker);
    if (attacker == NULL) return false;
    return is_local_player(env, attacker);
}

bool isRainbowSword(JNIEnv* env, jobject item) {
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

bool isRainbowArmor(JNIEnv* env, jobject item) {
    if (item == NULL || env == NULL) return false;
    jclass itemClass = (*env)->GetObjectClass(env, item);
    const char* classNames[] = {
        "com/qidai/morefunctionalswordmod/RainbowGemItems$GemHelmet",
        "com/qidai/morefunctionalswordmod/RainbowGemItems$GemChestplate",
        "com/qidai/morefunctionalswordmod/RainbowGemItems$GemLeggings",
        "com/qidai/morefunctionalswordmod/RainbowGemItems$GemBoots"
    };
    for (int i = 0; i < 4; i++) {
        jclass cls = (*env)->FindClass(env, classNames[i]);
        if (cls != NULL && (*env)->IsInstanceOf(env, item, cls)) {
            return true;
        }
    }
    return false;
}

jobject getMainHandStack(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return NULL;
    jclass playerClass = (*env)->GetObjectClass(env, player);
    jmethodID getMainHand = (*env)->GetMethodID(env, playerClass, "method_5998", "()Lnet/minecraft/class_1799;");
    if (getMainHand == NULL) return NULL;
    return (*env)->CallObjectMethod(env, player, getMainHand);
}

bool playerHasRainbowSword(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return false;
    jobject stack = getMainHandStack(env, player);
    if (stack == NULL) return false;
    jclass stackClass = (*env)->GetObjectClass(env, stack);
    jmethodID getItem = (*env)->GetMethodID(env, stackClass, "method_7909", "()Lnet/minecraft/class_1792;");
    jobject item = (*env)->CallObjectMethod(env, stack, getItem);
    return isRainbowSword(env, item);
}

bool playerHasRainbowArmor(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return false;
    jclass playerClass = (*env)->GetObjectClass(env, player);
    jmethodID getArmorItems = (*env)->GetMethodID(env, playerClass, "method_7337", "()Ljava/lang/Iterable;");
    if (getArmorItems == NULL) return false;
    jobject armorIterable = (*env)->CallObjectMethod(env, player, getArmorItems);
    if (armorIterable == NULL) return false;
    jclass iterableClass = (*env)->GetObjectClass(env, armorIterable);
    jmethodID iterator = (*env)->GetMethodID(env, iterableClass, "iterator", "()Ljava/util/Iterator;");
    jobject iter = (*env)->CallObjectMethod(env, armorIterable, iterator);
    if (iter == NULL) return false;
    jclass iteratorClass = (*env)->GetObjectClass(env, iter);
    jmethodID hasNext = (*env)->GetMethodID(env, iteratorClass, "hasNext", "()Z");
    jmethodID next = (*env)->GetMethodID(env, iteratorClass, "next", "()Ljava/lang/Object;");
    while ((*env)->CallBooleanMethod(env, iter, hasNext)) {
        jobject stack = (*env)->CallObjectMethod(env, iter, next);
        if (stack != NULL) {
            jclass stackClass = (*env)->GetObjectClass(env, stack);
            jmethodID getItem = (*env)->GetMethodID(env, stackClass, "method_7909", "()Lnet/minecraft/class_1792;");
            jobject item = (*env)->CallObjectMethod(env, stack, getItem);
            if (isRainbowArmor(env, item)) {
                return true;
            }
        }
    }
    return false;
}

bool isLadderBlock(JNIEnv* env, jobject block) {
    if (block == NULL || env == NULL) return false;
    jclass blockClass = (*env)->GetObjectClass(env, block);
    jmethodID getTranslationKey = (*env)->GetMethodID(env, blockClass, "method_9530", "()Ljava/lang/String;");
    jstring key = (*env)->CallObjectMethod(env, block, getTranslationKey);
    const char* keyStr = (*env)->GetStringUTFChars(env, key, NULL);
    bool isLadder = (strstr(keyStr, "ladder") != NULL) || (strstr(keyStr, "vine") != NULL) || (strstr(keyStr, "scaffolding") != NULL);
    (*env)->ReleaseStringUTFChars(env, key, keyStr);
    return isLadder;
}

void setEntityHealth(JNIEnv* env, jobject entity, float health) {
    if (entity == NULL || env == NULL) return;
    jclass entityClass = (*env)->GetObjectClass(env, entity);
    jmethodID setHealth = (*env)->GetMethodID(env, entityClass, "method_6044", "(F)V");
    if (setHealth != NULL) {
        (*env)->CallVoidMethod(env, entity, setHealth, health);
    }
}

JNIEXPORT void JNICALL Java_com_qidai_morefunctionalswordmod_NativeLoader_setGameDir(JNIEnv* env, jobject obj, jstring dir) {
    const char* d = (*env)->GetStringUTFChars(env, dir, NULL);
    if (d) {
        set_game_dir(d);
        (*env)->ReleaseStringUTFChars(env, dir, d);
    }
}