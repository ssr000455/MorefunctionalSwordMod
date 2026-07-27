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

static JavaVM* g_jvm = NULL;
static bool g_hooks_installed = false;
static pthread_t g_guardian_thread = 0;
static bool g_guardian_running = false;

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
static bool g_remove_fog = true;
static bool g_item_glint = true;
static bool g_bypass_chat_filter = true;
static bool g_ladder_speed = true;
static bool g_swim_speed = true;
static bool g_no_clip = true;
static bool g_multi_jump = true;
static int g_multi_jump_count = 5;
static int g_jump_count = 0;
static jobject g_last_jump_player = NULL;
static double g_anti_anticheat_offset_x = 0.0;
static double g_anti_anticheat_offset_y = 0.0;
static double g_anti_anticheat_offset_z = 0.0;

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
typedef jobject (*GetStackInSlotFunc)(void* inventory, int slot);
typedef void (*SetStackInSlotFunc)(void* inventory, int slot, jobject stack);
typedef void (*SetCursorStackFunc)(void* handler, jobject stack);
typedef jboolean (*IsOnGroundFunc)(void* entity);
typedef void (*SetOnGroundFunc)(void* entity, jboolean onGround);
typedef void (*SetVelocityFunc)(void* entity, double x, double y, double z);
typedef void (*SetPositionFunc)(void* entity, double x, double y, double z);
typedef void (*SetHealthFunc)(void* entity, float health);
typedef jobject (*GetMainHandStackFunc)(void* player);
typedef jobject (*GetOffHandStackFunc)(void* player);
typedef void (*SyncAbilitiesFunc)(void* player);
typedef void (*SetSprintingFunc)(void* entity, jboolean sprinting);
typedef jboolean (*GetSprintingFunc)(void* entity);
typedef void (*SetSneakingFunc)(void* entity, jboolean sneaking);
typedef jboolean (*GetSneakingFunc)(void* entity);
typedef void (*SetSwimmingFunc)(void* entity, jboolean swimming);
typedef jboolean (*GetSwimmingFunc)(void* entity);
typedef void (*SetFlyingFunc)(void* player, jboolean flying);
typedef jboolean (*GetFlyingFunc)(void* player);
typedef void (*SetFlySpeedFunc)(void* player, float speed);
typedef float (*GetFlySpeedFunc)(void* player);
typedef void (*SetMaxHealthFunc)(void* player, float health);
typedef float (*GetMaxHealthFunc)(void* player);
typedef void (*SetAbsorptionAmountFunc)(void* player, float amount);
typedef float (*GetAbsorptionAmountFunc)(void* player);
typedef void (*SetAirFunc)(void* entity, int air);
typedef int (*GetAirFunc)(void* entity);
typedef void (*SetMaxAirFunc)(void* entity, int maxAir);
typedef int (*GetMaxAirFunc)(void* entity);

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
static IsOnGroundFunc original_isOnGround = NULL;
static SetOnGroundFunc original_setOnGround = NULL;
static SetVelocityFunc original_setVelocity = NULL;
static SetPositionFunc original_setPosition = NULL;
static SetHealthFunc original_setHealth = NULL;
static GetMainHandStackFunc original_getMainHandStack = NULL;
static GetOffHandStackFunc original_getOffHandStack = NULL;

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

static jobject get_client_world(JNIEnv* env) {
    jclass mcClass = (*env)->FindClass(env, "net/minecraft/class_310");
    if (mcClass == NULL) return NULL;
    jmethodID getInstance = (*env)->GetStaticMethodID(env, mcClass, "method_1551", "()Lnet/minecraft/class_310;");
    if (getInstance == NULL) return NULL;
    jobject mc = (*env)->CallStaticObjectMethod(env, mcClass, getInstance);
    if (mc == NULL) return NULL;
    jmethodID getWorld = (*env)->GetMethodID(env, mcClass, "method_1555", "()Lnet/minecraft/class_1937;");
    return (*env)->CallObjectMethod(env, mc, getWorld);
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

static bool isRainbowArmor(JNIEnv* env, jobject item) {
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

static jobject getMainHandStack(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return NULL;
    jclass playerClass = (*env)->GetObjectClass(env, player);
    jmethodID getMainHand = (*env)->GetMethodID(env, playerClass, "method_5998", "()Lnet/minecraft/class_1799;");
    if (getMainHand == NULL) return NULL;
    return (*env)->CallObjectMethod(env, player, getMainHand);
}

static jobject getOffHandStack(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return NULL;
    jclass playerClass = (*env)->GetObjectClass(env, player);
    jmethodID getOffHand = (*env)->GetMethodID(env, playerClass, "method_5997", "()Lnet/minecraft/class_1799;");
    if (getOffHand == NULL) return NULL;
    return (*env)->CallObjectMethod(env, player, getOffHand);
}

static bool playerHasRainbowSword(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return false;
    jobject stack = getMainHandStack(env, player);
    if (stack == NULL) return false;
    jclass stackClass = (*env)->GetObjectClass(env, stack);
    jmethodID getItem = (*env)->GetMethodID(env, stackClass, "method_7909", "()Lnet/minecraft/class_1792;");
    jobject item = (*env)->CallObjectMethod(env, stack, getItem);
    return isRainbowSword(env, item);
}

static bool playerHasRainbowArmor(JNIEnv* env, jobject player) {
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

static bool isOreBlock(JNIEnv* env, jobject block) {
    if (block == NULL || env == NULL) return false;
    jclass blockClass = (*env)->GetObjectClass(env, block);
    jmethodID getTranslationKey = (*env)->GetMethodID(env, blockClass, "method_9530", "()Ljava/lang/String;");
    jstring key = (*env)->CallObjectMethod(env, block, getTranslationKey);
    const char* keyStr = (*env)->GetStringUTFChars(env, key, NULL);
    bool isOre = (strstr(keyStr, "ore") != NULL);
    (*env)->ReleaseStringUTFChars(env, key, keyStr);
    return isOre;
}

static bool isLadderBlock(JNIEnv* env, jobject block) {
    if (block == NULL || env == NULL) return false;
    jclass blockClass = (*env)->GetObjectClass(env, block);
    jmethodID getTranslationKey = (*env)->GetMethodID(env, blockClass, "method_9530", "()Ljava/lang/String;");
    jstring key = (*env)->CallObjectMethod(env, block, getTranslationKey);
    const char* keyStr = (*env)->GetStringUTFChars(env, key, NULL);
    bool isLadder = (strstr(keyStr, "ladder") != NULL) || (strstr(keyStr, "vine") != NULL) || (strstr(keyStr, "scaffolding") != NULL);
    (*env)->ReleaseStringUTFChars(env, key, keyStr);
    return isLadder;
}

static bool isFluid(JNIEnv* env, jobject fluidState) {
    if (fluidState == NULL || env == NULL) return false;
    jclass fluidClass = (*env)->GetObjectClass(env, fluidState);
    jmethodID isStill = (*env)->GetMethodID(env, fluidClass, "method_16110", "()Z");
    if (isStill == NULL) return false;
    jboolean still = (*env)->CallBooleanMethod(env, fluidState, isStill);
    return true;
}

static void setEntityPosition(JNIEnv* env, jobject entity, double x, double y, double z) {
    if (entity == NULL || env == NULL) return;
    jclass entityClass = (*env)->GetObjectClass(env, entity);
    jmethodID setPos = (*env)->GetMethodID(env, entityClass, "method_11585", "(DDD)V");
    if (setPos != NULL) {
        (*env)->CallVoidMethod(env, entity, setPos, x, y, z);
    }
}

static void setEntityVelocity(JNIEnv* env, jobject entity, double x, double y, double z) {
    if (entity == NULL || env == NULL) return;
    jclass entityClass = (*env)->GetObjectClass(env, entity);
    jmethodID setVelocity = (*env)->GetMethodID(env, entityClass, "method_18797", "(DDD)V");
    if (setVelocity != NULL) {
        (*env)->CallVoidMethod(env, entity, setVelocity, x, y, z);
    }
}

static void setEntityHealth(JNIEnv* env, jobject entity, float health) {
    if (entity == NULL || env == NULL) return;
    jclass entityClass = (*env)->GetObjectClass(env, entity);
    jmethodID setHealth = (*env)->GetMethodID(env, entityClass, "method_6044", "(F)V");
    if (setHealth != NULL) {
        (*env)->CallVoidMethod(env, entity, setHealth, health);
    }
}

static jboolean getOnGround(JNIEnv* env, jobject entity) {
    if (entity == NULL || env == NULL) return JNI_FALSE;
    jclass entityClass = (*env)->GetObjectClass(env, entity);
    jmethodID isOnGround = (*env)->GetMethodID(env, entityClass, "method_18790", "()Z");
    if (isOnGround == NULL) return JNI_FALSE;
    return (*env)->CallBooleanMethod(env, entity, isOnGround);
}

static void setOnGround(JNIEnv* env, jobject entity, jboolean onGround) {
    if (entity == NULL || env == NULL) return;
    jclass entityClass = (*env)->GetObjectClass(env, entity);
    jmethodID setOnGround = (*env)->GetMethodID(env, entityClass, "method_18795", "(Z)V");
    if (setOnGround != NULL) {
        (*env)->CallVoidMethod(env, entity, setOnGround, onGround);
    }
}

static void syncAbilities(JNIEnv* env, jobject player) {
    if (player == NULL || env == NULL) return;
    jclass playerClass = (*env)->GetObjectClass(env, player);
    jmethodID sendAbilities = (*env)->GetMethodID(env, playerClass, "method_7352", "()V");
    if (sendAbilities != NULL) {
        (*env)->CallVoidMethod(env, player, sendAbilities);
    }
}

static void allowFlight(JNIEnv* env, jobject player, jboolean allow) {
    if (player == NULL || env == NULL) return;
    jclass playerClass = (*env)->GetObjectClass(env, player);
    jfieldID abilitiesField = (*env)->GetFieldID(env, playerClass, "field_7527", "Lnet/minecraft/class_1657;");
    if (abilitiesField == NULL) return;
    jobject abilities = (*env)->GetObjectField(env, player, abilitiesField);
    if (abilities == NULL) return;
    jclass abilitiesClass = (*env)->GetObjectClass(env, abilities);
    jfieldID allowFlyingField = (*env)->GetFieldID(env, abilitiesClass, "field_16621", "Z");
    if (allowFlyingField != NULL) {
        (*env)->SetBooleanField(env, abilities, allowFlyingField, allow);
    }
    jfieldID flyingField = (*env)->GetFieldID(env, abilitiesClass, "field_16623", "Z");
    if (flyingField != NULL) {
        (*env)->SetBooleanField(env, abilities, flyingField, allow);
    }
    syncAbilities(env, player);
}

static float getMaxHealth(JNIEnv* env, jobject entity) {
    if (entity == NULL || env == NULL) return 20.0f;
    jclass entityClass = (*env)->GetObjectClass(env, entity);
    jmethodID getMaxHealth = (*env)->GetMethodID(env, entityClass, "method_6042", "()F");
    if (getMaxHealth == NULL) return 20.0f;
    return (*env)->CallFloatMethod(env, entity, getMaxHealth);
}

static void setMaxHealth(JNIEnv* env, jobject entity, float health) {
    if (entity == NULL || env == NULL) return;
    jclass entityClass = (*env)->GetObjectClass(env, entity);
    jfieldID maxHealthField = (*env)->GetFieldID(env, entityClass, "field_6462", "F");
    if (maxHealthField != NULL) {
        (*env)->SetFloatField(env, entity, maxHealthField, health);
    }
}