package com.qidai.morefunctionalswordmod;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class ModEvents {
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 20;

    public static void register() {
        // 伤害检测
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player) {
                if (source.getAttacker() == null && amount > player.getHealth() + 10) {
                    player.sendMessage(net.minecraft.text.Text.literal("⚠ 检测到异常伤害，已拦截").formatted(net.minecraft.util.Formatting.RED), false);
                    return false;
                }
            }
            return true;
        });

        // 简单的主循环（无反作弊）
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            // 个人版不包含反作弊功能
        });

        // 玩家断开时清理
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            // 个人版无需清理
        });
    }
}
