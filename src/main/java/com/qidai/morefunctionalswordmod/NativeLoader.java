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
        long addr_chatMessageC2SPacket
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

            // 获取所有Hook地址
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
                addr_chatMessageC2SPacket
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