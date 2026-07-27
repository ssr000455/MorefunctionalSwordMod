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
 * 负责从 JAR 中提取 .so 并加载到 JVM
 */
public class NativeLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("mfswordmod-native");
    private static boolean loaded = false;

    /**
     * 加载 Native 库，失败时仅记录日志，不影响游戏启动
     */
    public static void load() {
        if (loaded) return;

        try {
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

            // 解压 .so 到临时目录（有执行权限）
            File nativeDir = new File(FabricLoader.getInstance().getGameDir().toFile(), "native_cache");
            if (!nativeDir.exists() && !nativeDir.mkdirs()) {
                LOGGER.warn("无法创建 native_cache 目录，Native 加载失败");
                return;
            }

            File soFile = new File(nativeDir, libName);
            // 如果文件已存在且大小匹配，跳过解压（提高启动速度）
            try (InputStream in = NativeLoader.class.getResourceAsStream("/native/" + libName)) {
                if (in == null) {
                    LOGGER.warn("未找到 {}，请检查 JAR 包是否包含 native 目录", libName);
                    return;
                }
                Files.copy(in, soFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // 设置可执行权限（Android/Linux 必须）
            if (!soFile.setExecutable(true)) {
                LOGGER.warn("无法设置 {} 可执行权限", libName);
            }

            // 加载 .so
            System.load(soFile.getAbsolutePath());
            loaded = true;
            LOGGER.info("Native 层加载成功: {}", libName);

        } catch (Throwable t) {
            LOGGER.error("Native 层加载失败，降级为纯 Java 模式", t);
        }
    }

    /**
     * 检查 Native 是否已加载
     */
    public static boolean isLoaded() {
        return loaded;
    }
}