package com.qidai.morefunctionalswordmod;

import com.qidai.morefunctionalswordmod.anticheat.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
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

        // 攻击检测（使用 Fabric API）
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
                if (!CombatCheck.onAttack(attacker)) {
                    return false;
                }
            }
            return true;
        });

        // 方块破坏检测
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                return BlockBreakCheck.onBlockBreak(serverPlayer);
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
                    boolean hasAntiCheat = false;
                    for (ItemStack stack : player.getInventory().main) {
                        if (stack.getItem() == ModRegistry.ANTI_CHEAT) {
                            hasAntiCheat = true;
                            break;
                        }
                    }
                    if (!hasAntiCheat) continue;

                    if (!MoveCheck.check(player, AntiCheatManager.getInstance().getData(player))) continue;
                    if (!NbtCheck.check(player)) continue;
                    if (!PotionCheck.check(player)) continue;
                    if (!InventoryCheck.check(player)) continue;
                    if (!MemoryScanner.scan(player)) continue;
                }
            }
        });

        // 玩家断开时清理数据
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            AntiCheatManager.getInstance().removePlayer(handler.player);
        });
    }
}
