package com.qidai.morefunctionalswordmod;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Native 层加载器
 * 适配 FCL 的 Android 环境，将 .so 解压到应用内部存储目录
 */
public class NativeLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("mfswordmod-native");
    private static boolean loaded = false;

    public static void load() {
        if (loaded) return;

        try {
            // 获取 FCL 设置的应用内部运行时目录（MOD_ANDROID_RUNTIME）
            String modRuntime = System.getenv("MOD_ANDROID_RUNTIME");
            File nativeDir;
            if (modRuntime != null && !modRuntime.isEmpty()) {
                // 这是 FCL 的内部存储路径，可执行
                nativeDir = new File(modRuntime, "lib/arm64");
            } else {
                // 回退：使用游戏目录下的 native_cache（但外部存储可能不可执行，仅用于 PC 端）
                nativeDir = new File(FabricLoader.getInstance().getGameDir().toFile(), "native_cache");
            }

            if (!nativeDir.exists() && !nativeDir.mkdirs()) {
                LOGGER.warn("无法创建 native 目录: {}", nativeDir.getAbsolutePath());
                return;
            }

            // 获取当前架构
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

            File soFile = new File(nativeDir, libName);

            // 从 JAR 中提取 .so（如果已经存在且大小匹配则跳过，这里简单覆盖）
            try (InputStream in = NativeLoader.class.getResourceAsStream("/native/" + libName)) {
                if (in == null) {
                    LOGGER.warn("未找到 /native/{}，请检查 JAR 包", libName);
                    return;
                }
                Files.copy(in, soFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // 设置可执行权限（Android 必须）
            if (!soFile.setExecutable(true)) {
                LOGGER.warn("无法设置 {} 可执行权限", libName);
            }

            // 加载 .so
            System.load(soFile.getAbsolutePath());
            loaded = true;
            LOGGER.info("Native 层加载成功: {}", soFile.getAbsolutePath());

        } catch (Throwable t) {
            LOGGER.error("Native 层加载失败，降级为纯 Java 模式", t);
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }
}