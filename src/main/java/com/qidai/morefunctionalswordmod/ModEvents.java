package com.qidai.morefunctionalswordmod;

import com.qidai.morefunctionalswordmod.anticheat.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class ModEvents {
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 20;

    public static void register() {
        // 原有伤害检测
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player) {
                if (source.getAttacker() == null && amount > player.getHealth() + 10) {
                    player.sendMessage(net.minecraft.text.Text.literal("⚠ 检测到异常伤害，已拦截").formatted(net.minecraft.util.Formatting.RED), false);
                    return false;
                }
            }
            return true;
        });

        // 反作弊主循环（仅当玩家持有反作弊器时执行检测）
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (!AntiCheatManager.getInstance().isEnabled()) return;
            tickCounter++;
            if (tickCounter >= CHECK_INTERVAL) {
                tickCounter = 0;
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    // 检测玩家是否持有反作弊器（任意背包槽位）
                    boolean hasAntiCheat = false;
                    for (ItemStack stack : player.getInventory().main) {
                        if (stack.getItem() == ModRegistry.ANTI_CHEAT) {
                            hasAntiCheat = true;
                            break;
                        }
                    }
                    if (!hasAntiCheat) continue;

                    // 执行各项检测（任一失败即跳过后续，因为玩家已被踢出）
                    if (!MoveCheck.check(player, AntiCheatManager.getInstance().getData(player))) continue;
                    if (!NbtCheck.check(player)) continue;
                    if (!PotionCheck.check(player)) continue;
                    if (!InventoryCheck.check(player)) continue;
                    // 扫描作弊模组/调试器
                    AntiCheatProtector.scanAndPunish(player);
                }
            }
        });

        // 玩家断开时清理数据
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            AntiCheatManager.getInstance().removePlayer(handler.player);
        });
    }
}
