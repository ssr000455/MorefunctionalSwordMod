package com.qidai.morefunctionalswordmod.anticheat;

import com.qidai.morefunctionalswordmod.network.CheatScreenPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CheatPunisher {
    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private static final ConcurrentHashMap<ServerPlayerEntity, ScheduledFuture<?>> punishments = new ConcurrentHashMap<>();

    public static void punish(ServerPlayerEntity player, String reason) {
        if (punishments.containsKey(player)) return;

        // 发送打开作弊屏幕的数据包（无额外数据）
        ServerPlayNetworking.send(player, CheatScreenPacket.OPEN_SCREEN_ID, PacketByteBufs.create());

        // 10秒后踢出
        ScheduledFuture<?> task = executor.schedule(() -> {
            player.networkHandler.disconnect(Text.literal("作弊检测"));
            punishments.remove(player);
        }, 10, TimeUnit.SECONDS);

        punishments.put(player, task);
        
        // 禁用玩家操作
        player.changeGameMode(net.minecraft.world.GameMode.SPECTATOR);
    }
}
