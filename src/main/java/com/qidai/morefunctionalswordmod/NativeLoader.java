package com.qidai.morefunctionalswordmod;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class NativeLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("mfswordmod-native");
    private static boolean loaded = false;

    private static native void setGameDir(String gameDir);

    private static native void installNativeHook(
        long addr_getHardness,
        long addr_damage,
        long addr_attackEntityFrom,
        long addr_sendPacket,
        long addr_addVelocity,
        long addr_handleFallDamage,
        long addr_getAttackCooldown,
        long addr_onDisconnect,
        long addr_transferSlot,
        long addr_getAttackRange,
        long addr_applyArmorToDamage,
        long addr_getAttackCooldownProgress,
        long addr_getBlockSpeedFactor,
        long addr_getFluidSpeed,
        long addr_pushOutOfBlocks,
        long addr_jump,
        long addr_renderFog,
        long addr_renderItem,
        long addr_chatMessageC2SPacket,
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
    );

    public static void load() {
        if (loaded) return;

        try {
            File targetDir;
            String modRuntime = System.getenv("MOD_ANDROID_RUNTIME");
            if (modRuntime != null && !modRuntime.isEmpty()) {
                targetDir = new File(modRuntime, "lib");
                LOGGER.info("使用FCL内部目录: {}", targetDir.getAbsolutePath());
            } else {
                targetDir = new File(FabricLoader.getInstance().getGameDir().toFile(), "native_cache");
                LOGGER.info("使用游戏目录: {}", targetDir.getAbsolutePath());
            }

            if (!targetDir.exists() && !targetDir.mkdirs()) {
                LOGGER.warn("无法创建目录: {}", targetDir.getAbsolutePath());
                return;
            }

            String arch = System.getProperty("os.arch").toLowerCase();
            String libName;
            if (arch.contains("aarch64") || arch.contains("arm64")) {
                libName = "libmfswordmod-arm64.so";
            } else if (arch.contains("arm")) {
                libName = "libmfswordmod-arm32.so";
            } else if (arch.contains("64")) {
                libName = "libmfswordmod-x64.so";
            } else {
                libName = "libmfswordmod-x86.so";
            }

            File soFile = new File(targetDir, libName);
            try (InputStream in = NativeLoader.class.getResourceAsStream("/native/" + libName)) {
                if (in == null) {
                    LOGGER.warn("找不到资源: /native/{}", libName);
                    return;
                }
                Files.copy(in, soFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("已解压: {}", soFile.getAbsolutePath());
            }

            if (!soFile.setExecutable(true)) {
                LOGGER.warn("设置可执行权限失败");
            }

            System.load(soFile.getAbsolutePath());
            LOGGER.info("so加载成功: {}", soFile.getAbsolutePath());

            String gameDir = FabricLoader.getInstance().getGameDir().toString();
            setGameDir(gameDir);
            LOGGER.info("游戏目录已设置: {}", gameDir);

            long addr_getHardness = getMethodAddress(
                "net.minecraft.class_2248",
                "method_9574",
                "()F"
            );

            long addr_damage = getMethodAddress(
                "net.minecraft.class_1309",
                "method_5643",
                "(Lnet/minecraft/class_1282;F)Z"
            );

            long addr_attackEntityFrom = getMethodAddress(
                "net.minecraft.class_1309",
                "method_5643",
                "(Lnet/minecraft/class_1282;F)Z"
            );

            long addr_sendPacket = getMethodAddress(
                "net.minecraft.class_2535",
                "method_11142",
                "(Lnet/minecraft/class_2596;)V"
            );

            long addr_addVelocity = getMethodAddress(
                "net.minecraft.class_1297",
                "method_18796",
                "(DDD)V"
            );

            long addr_handleFallDamage = getMethodAddress(
                "net.minecraft.class_1309",
                "method_6028",
                "(FF)Z"
            );

            long addr_getAttackCooldown = getMethodAddress(
                "net.minecraft.class_1309",
                "method_6128",
                "(F)F"
            );

            long addr_onDisconnect = getMethodAddress(
                "net.minecraft.class_2535",
                "method_11062",
                "(Lnet/minecraft/class_2561;)V"
            );

            long addr_transferSlot = getMethodAddress(
                "net.minecraft.class_1703",
                "method_7823",
                "(II)Lnet/minecraft/class_1799;"
            );

            long addr_getAttackRange = getMethodAddress(
                "net.minecraft.class_3222",
                "method_6258",
                "()D"
            );

            long addr_applyArmorToDamage = getMethodAddress(
                "net.minecraft.class_1309",
                "method_5716",
                "(FLnet/minecraft/class_1282;)F"
            );

            long addr_getAttackCooldownProgress = getMethodAddress(
                "net.minecraft.class_1309",
                "method_6128",
                "(F)F"
            );

            long addr_getBlockSpeedFactor = getMethodAddress(
                "net.minecraft.class_1309",
                "method_6075",
                "(Lnet/minecraft/class_2680;)F"
            );

            long addr_getFluidSpeed = getMethodAddress(
                "net.minecraft.class_1309",
                "method_6076",
                "(Lnet/minecraft/class_3610;)F"
            );

            long addr_pushOutOfBlocks = getMethodAddress(
                "net.minecraft.class_1297",
                "method_18792",
                "(DDD)V"
            );

            long addr_jump = getMethodAddress(
                "net.minecraft.class_1309",
                "method_6045",
                "()V"
            );

            long addr_renderFog = getMethodAddress(
                "net.minecraft.class_761",
                "method_22793",
                "(Lnet/minecraft/class_4184;F)V"
            );

            long addr_renderItem = getMethodAddress(
                "net.minecraft.class_918",
                "method_1101",
                "(Lnet/minecraft/class_1799;Lnet/minecraft/class_809;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;II)V"
            );

            long addr_chatMessageC2SPacket = getMethodAddress(
                "net.minecraft.class_2794",
                "method_12080",
                "(Ljava/lang/String;)V"
            );

            long addr_removeStack = getMethodAddress(
                "net.minecraft.class_1721",
                "method_7368",
                "(I)V"
            );

            long addr_setStack = getMethodAddress(
                "net.minecraft.class_1721",
                "method_7358",
                "(ILnet/minecraft/class_1799;)V"
            );

            long addr_dropInventory = getMethodAddress(
                "net.minecraft.class_3222",
                "method_5992",
                "()V"
            );

            long addr_dropSelectedItem = getMethodAddress(
                "net.minecraft.class_3222",
                "method_5978",
                "()V"
            );

            long addr_dropItem = getMethodAddress(
                "net.minecraft.class_3222",
                "method_5990",
                "(Lnet/minecraft/class_1799;Z)Lnet/minecraft/class_2477;"
            );

            long addr_onDeath = getMethodAddress(
                "net.minecraft.class_1309",
                "method_5574",
                "(Lnet/minecraft/class_1282;)V"
            );

            long addr_kill = getMethodAddress(
                "net.minecraft.class_1297",
                "method_5731",
                "()V"
            );

            long addr_clearInventory = getMethodAddress(
                "net.minecraft.class_3222",
                "method_5988",
                "()V"
            );

            long addr_setHealth = getMethodAddress(
                "net.minecraft.class_1309",
                "method_6044",
                "(F)V"
            );

            long addr_removeStack2 = getMethodAddress(
                "net.minecraft.class_1721",
                "method_7367",
                "(I)Lnet/minecraft/class_1799;"
            );

            long addr_quickMove = getMethodAddress(
                "net.minecraft.class_1703",
                "method_7823",
                "(II)Lnet/minecraft/class_1799;"
            );

            long addr_clickSlot = getMethodAddress(
                "net.minecraft.class_1703",
                "method_7818",
                "(IILnet/minecraft/class_1799;I)V"
            );

            long addr_syncInventory = getMethodAddress(
                "net.minecraft.class_3222",
                "method_5740",
                "()V"
            );

            installNativeHook(
                addr_getHardness,
                addr_damage,
                addr_attackEntityFrom,
                addr_sendPacket,
                addr_addVelocity,
                addr_handleFallDamage,
                addr_getAttackCooldown,
                addr_onDisconnect,
                addr_transferSlot,
                addr_getAttackRange,
                addr_applyArmorToDamage,
                addr_getAttackCooldownProgress,
                addr_getBlockSpeedFactor,
                addr_getFluidSpeed,
                addr_pushOutOfBlocks,
                addr_jump,
                addr_renderFog,
                addr_renderItem,
                addr_chatMessageC2SPacket,
                addr_removeStack,
                addr_setStack,
                addr_dropInventory,
                addr_dropSelectedItem,
                addr_dropItem,
                addr_onDeath,
                addr_kill,
                addr_clearInventory,
                addr_setHealth,
                addr_removeStack2,
                addr_quickMove,
                addr_clickSlot,
                addr_syncInventory
            );

            loaded = true;
            LOGGER.info("Native Hook安装完成");

        } catch (Throwable t) {
            LOGGER.error("Native加载失败，降级为纯Java模式", t);
        }
    }

    private static long getMethodAddress(String className, String methodName, String methodSig) {
        try {
            Class<?> clazz = Class.forName(className);
            Method[] methods = clazz.getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().equals(methodName)) {
                    String sig = getMethodSignature(m);
                    if (sig.equals(methodSig)) {
                        return getMethodAddressUnsafe(m);
                    }
                }
            }
            LOGGER.warn("未找到方法: {}.{}{}", className, methodName, methodSig);
            return 0;
        } catch (Exception e) {
            LOGGER.error("获取方法地址失败: {}.{}{}", className, methodName, methodSig, e);
            return 0;
        }
    }

    private static String getMethodSignature(Method method) {
        StringBuilder sb = new StringBuilder("(");
        for (Class<?> param : method.getParameterTypes()) {
            sb.append(getTypeDescriptor(param));
        }
        sb.append(")");
        sb.append(getTypeDescriptor(method.getReturnType()));
        return sb.toString();
    }

    private static String getTypeDescriptor(Class<?> type) {
        if (type == void.class) return "V";
        if (type == boolean.class) return "Z";
        if (type == byte.class) return "B";
        if (type == char.class) return "C";
        if (type == short.class) return "S";
        if (type == int.class) return "I";
        if (type == long.class) return "J";
        if (type == float.class) return "F";
        if (type == double.class) return "D";
        if (type.isArray()) return "[" + getTypeDescriptor(type.getComponentType());
        return "L" + type.getName().replace('.', '/') + ";";
    }

    private static long getMethodAddressUnsafe(Method method) {
        return 0;
    }

    public static boolean isLoaded() {
        return loaded;
    }
}