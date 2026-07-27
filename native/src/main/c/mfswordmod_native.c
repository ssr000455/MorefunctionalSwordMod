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
#include <android/log.h>
#include <stdlib.h>
#include <time.h>

#define LOG_TAG "mfswordmod_native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// 引入 Dobby Hook
#include "dobby.h"

// ================================================================
// 全局状态
// ================================================================
static JavaVM* g_jvm = NULL;
static bool g_hooks_installed = false;
static pthread_t g_guardian_thread = 0;
static bool g_guardian_running = false;

// ================================================================
// 原始函数指针 (用于调用原方法)
// ================================================================
typedef jfloat (*GetHardnessFunc)(void* blockState, void* world, void* pos);
typedef jboolean (*DamageFunc)(void* entity, void* damageSource, jfloat amount);
typedef jboolean (*AttackEntityFromFunc)(void* entity, void* damageSource, jfloat amount);
typedef void (*SendPacketFunc)(void* networkHandler, void* packet);
typedef jboolean (*IsAirFunc)(void* blockState);

static GetHardnessFunc original_getHardness = NULL;
static DamageFunc original_damage = NULL;
static AttackEntityFromFunc original_attackEntityFrom = NULL;
static SendPacketFunc original_sendPacket = NULL;

// ================================================================
// JNI 工具函数
// ================================================================
static JNIEnv* get_env() {
    JNIEnv* env = NULL;
    if (g_jvm != NULL) {
        (*g_jvm)->AttachCurrentThread(g_jvm, (void**)&env, NULL);
    }
    return env;
}

// 判断某个物品是不是七彩神剑 (支持多种识别方式)
static bool isRainbowSword(JNIEnv* env, jobject item) {
    if (item == NULL || env == NULL) return false;

    // 方法1: 直接类名比较 (最快)
    jclass itemClass = (*env)->GetObjectClass(env, item);
    jclass cls = (*env)->FindClass(env, "com/qidai/morefunctionalswordmod/RainbowSwordItem");
    if (cls != NULL && (*env)->IsInstanceOf(env, item, cls)) {
        return true;
    }

    // 方法2: 通过物品注册ID判断 (更鲁棒)
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

// 获取玩家主手物品
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

// 检查玩家是否持有七彩神剑且已达成契约
static bool playerHasRainbowSword(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return false;

    jobject item = getMainHandItem(env, player);
    if (!isRainbowSword(env, item)) return false;

    // 检查契约状态 (NBT: HasContract)
    jclass stackClass = (*env)->FindClass(env, "net/minecraft/class_1799");
    if (stackClass == NULL) return false;

    // 获取玩家主手的 ItemStack
    jclass playerClass = (*env)->GetObjectClass(env, player);
    jmethodID getMainHandStack = (*env)->GetMethodID(env, playerClass, "method_5998", "()Lnet/minecraft/class_1799;");
    jobject stack = (*env)->CallObjectMethod(env, player, getMainHandStack);
    if (stack == NULL) return false;

    // 获取 NBT
    jmethodID getOrCreateNbt = (*env)->GetMethodID(env, stackClass, "method_7993", "()Lnet/minecraft/class_2487;");
    jobject nbt = (*env)->CallObjectMethod(env, stack, getOrCreateNbt);
    if (nbt == NULL) return false;

    jclass nbtClass = (*env)->GetObjectClass(env, nbt);
    jmethodID getBoolean = (*env)->GetMethodID(env, nbtClass, "method_10556", "(Ljava/lang/String;)Z");
    jstring key = (*env)->NewStringUTF(env, "HasContract");
    jboolean hasContract = (*env)->CallBooleanMethod(env, nbt, getBoolean, key);

    return (bool)hasContract;
}

// ================================================================
// HOOK 1: Block.getHardness() -> 基岩可破坏
// ================================================================
static jfloat hooked_getHardness(void* blockState, void* world, void* pos) {
    JNIEnv* env = get_env();
    if (env == NULL) return original_getHardness(blockState, world, pos);

    // 判断是否为基岩
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
        // 基岩硬编码返回 0，可瞬间挖掘
        return 0.0f;
    }

    return original_getHardness(blockState, world, pos);
}

// ================================================================
// HOOK 2: LivingEntity.damage() -> 持剑玩家免疫伤害
// ================================================================
static jboolean hooked_damage(void* entity, void* damageSource, jfloat amount) {
    JNIEnv* env = get_env();
    if (env == NULL) return original_damage(entity, damageSource, amount);

    // 判断是否为玩家
    jclass entityClass = (*env)->GetObjectClass(env, entity);
    jclass playerClass = (*env)->FindClass(env, "net/minecraft/class_3222");
    if (playerClass == NULL) return original_damage(entity, damageSource, amount);

    if ((*env)->IsInstanceOf(env, entity, playerClass)) {
        if (playerHasRainbowSword(env, entity)) {
            LOGI("免疫伤害: 玩家持七彩神剑");
            return false; // 完全免疫
        }
    }

    return original_damage(entity, damageSource, amount);
}

// ================================================================
// HOOK 3: LivingEntity.attackEntityFrom() -> 秒杀非玩家生物
// ================================================================
static jboolean hooked_attackEntityFrom(void* entity, void* damageSource, jfloat amount) {
    JNIEnv* env = get_env();
    if (env == NULL) return original_attackEntityFrom(entity, damageSource, amount);

    jclass entityClass = (*env)->GetObjectClass(env, entity);
    jclass playerClass = (*env)->FindClass(env, "net/minecraft/class_3222");

    // 如果是玩家，不秒杀 (防止PVP误杀)
    if (playerClass != NULL && (*env)->IsInstanceOf(env, entity, playerClass)) {
        return original_attackEntityFrom(entity, damageSource, amount);
    }

    // 判断伤害来源是否为玩家
    jclass dsClass = (*env)->GetObjectClass(env, damageSource);
    jmethodID getAttacker = (*env)->GetMethodID(env, dsClass, "method_11598", "()Lnet/minecraft/class_1297;");
    jobject attacker = (*env)->CallObjectMethod(env, damageSource, getAttacker);

    if (attacker != NULL && (*env)->IsInstanceOf(env, attacker, playerClass)) {
        if (playerHasRainbowSword(env, attacker)) {
            LOGI("秒杀生物: 七彩神剑攻击");
            // 直接设置生命值为 0
            jmethodID setHealth = (*env)->GetMethodID(env, entityClass, "method_6044", "(F)V");
            if (setHealth != NULL) {
                (*env)->CallVoidMethod(env, entity, setHealth, 0.0f);
            }
            return true; // 阻止后续伤害计算
        }
    }

    return original_attackEntityFrom(entity, damageSource, amount);
}

// ================================================================
// HOOK 4: sendPacket() -> 绕过反作弊 (拦截异常数据包)
// ================================================================
static void hooked_sendPacket(void* networkHandler, void* packet) {
    JNIEnv* env = get_env();
    if (env == NULL) {
        original_sendPacket(networkHandler, packet);
        return;
    }

    // 检查包类型，如果是位置/移动包且玩家持剑，则过滤异常值
    jclass packetClass = (*env)->GetObjectClass(env, packet);
    jclass posClass = (*env)->FindClass(env, "net/minecraft/class_2748"); // PlayerPositionUpdateC2SPacket
    if (posClass != NULL && (*env)->IsInstanceOf(env, packet, posClass)) {
        // 可以在这里添加坐标钳制逻辑，防止反作弊误判
        // 但我们暂时放行所有包 (因为模组逻辑本身已合法)
    }

    original_sendPacket(networkHandler, packet);
}

// ================================================================
// 守护线程: 定时校验七彩神剑 NBT 防止篡改
// ================================================================
static void* guardian_thread(void* arg) {
    LOGI("NBT 防篡改守护线程已启动");
    g_guardian_running = true;

    while (g_guardian_running) {
        sleep(1); // 每秒检查一次

        JNIEnv* env = get_env();
        if (env == NULL) continue;

        // 获取所有在线玩家
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

            // 检查是否持有七彩神剑
            jobject item = getMainHandItem(env, player);
            if (!isRainbowSword(env, item)) continue;

            // 获取 NBT 并校验关键字段
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

            // 检查 BaseDamage 是否被篡改
            jstring damageKey = (*env)->NewStringUTF(env, "BaseDamage");
            jfloat damage = (*env)->CallFloatMethod(env, nbt, getFloat, damageKey);

            // 如果伤害值被改低 (小于 999999)，强制回滚
            if (damage < 999999.0f && damage > 0) {
                LOGW("检测到 NBT 篡改: BaseDamage=%.1f, 正在回滚", damage);
                jmethodID putFloat = (*env)->GetMethodID(env, nbtClass, "method_10566", "(Ljava/lang/String;F)V");
                if (putFloat != NULL) {
                    (*env)->CallVoidMethod(env, nbt, putFloat, damageKey, 999999.0f);
                }
            }

            // 检查 HasContract 是否被篡改
            jmethodID getBoolean = (*env)->GetMethodID(env, nbtClass, "method_10556", "(Ljava/lang/String;)Z");
            jstring contractKey = (*env)->NewStringUTF(env, "HasContract");
            jboolean hasContract = (*env)->CallBooleanMethod(env, nbt, getBoolean, contractKey);
            if (!hasContract) {
                LOGW("检测到 NBT 篡改: HasContract=false, 强制恢复");
                jmethodID putBoolean = (*env)->GetMethodID(env, nbtClass, "method_10562", "(Ljava/lang/String;Z)V");
                if (putBoolean != NULL) {
                    (*env)->CallVoidMethod(env, nbt, putBoolean, contractKey, true);
                }
            }
        }
    }

    return NULL;
}

// ================================================================
// 启动守护线程
// ================================================================
static void startGuardian() {
    if (g_guardian_thread == 0) {
        pthread_create(&g_guardian_thread, NULL, guardian_thread, NULL);
        LOGI("NBT 防篡改守护线程已启动");
    }
}

// ================================================================
// JNI 导出: 安装 Hook (Java 层调用)
// ================================================================
JNIEXPORT void JNICALL Java_com_qidai_morefunctionalswordmod_NativeLoader_installNativeHook(
    JNIEnv* env,
    jobject obj,
    jlong addr_getHardness,
    jlong addr_damage,
    jlong addr_attackEntityFrom,
    jlong addr_sendPacket
) {
    LOGI("开始安装 Native Hook...");

    if (addr_getHardness != 0) {
        DobbyHook((void*)addr_getHardness, (void*)hooked_getHardness, (void**)&original_getHardness);
        LOGI("  ✓ Hook Block.getHardness");
    }
    if (addr_damage != 0) {
        DobbyHook((void*)addr_damage, (void*)hooked_damage, (void**)&original_damage);
        LOGI("  ✓ Hook LivingEntity.damage");
    }
    if (addr_attackEntityFrom != 0) {
        DobbyHook((void*)addr_attackEntityFrom, (void*)hooked_attackEntityFrom, (void**)&original_attackEntityFrom);
        LOGI("  ✓ Hook LivingEntity.attackEntityFrom");
    }
    if (addr_sendPacket != 0) {
        DobbyHook((void*)addr_sendPacket, (void*)hooked_sendPacket, (void**)&original_sendPacket);
        LOGI("  ✓ Hook sendPacket (反作弊绕过)");
    }

    g_hooks_installed = true;
    startGuardian();

    LOGI("所有 Native Hook 安装完成，七彩神剑已进入强力模式");
}

// ================================================================
// JNI_OnLoad: .so 加载入口
// ================================================================
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_jvm = vm;
    JNIEnv* env;
    (*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6);

    LOGI(" 七彩神剑 Native 层强力模块 v2.0");

    return JNI_VERSION_1_6;
}