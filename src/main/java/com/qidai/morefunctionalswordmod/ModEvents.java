package com.qidai.morefunctionalswordmod;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class ModEvents {
    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player) {
                if (source.getAttacker() == null && amount > player.getHealth() + 10) {
                    player.sendMessage(net.minecraft.text.Text.literal("⚠ 检测到异常伤害，已拦截").formatted(net.minecraft.util.Formatting.RED), false);
                    return false;
                }
            }
            return true;
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {});
    }
}
