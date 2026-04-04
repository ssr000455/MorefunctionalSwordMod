package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.network.ServerPlayerEntity;
import com.qidai.morefunctionalswordmod.ModUtil;
import com.qidai.morefunctionalswordmod.RainbowSwordItem;

public class MoveCheck {
    private static final double MAX_HORIZONTAL_SPEED = 20.0;
    private static final double MAX_VERTICAL_SPEED = 10.0;
    private static final double MAX_ACCELERATION = 5.0;
    private static final int MAX_INVALID_MOVES = 5;

    public static boolean check(ServerPlayerEntity player, AntiCheatManager.AntiCheatData data) {
        if (AntiCheatManager.getInstance().isExempt(player)) return true;

        double dx = player.getX() - data.lastX;
        double dz = player.getZ() - data.lastZ;
        double dy = player.getY() - data.lastY;
        double horizontalDist = Math.sqrt(dx*dx + dz*dz);
        double verticalDist = Math.abs(dy);
        double timeDelta = (System.currentTimeMillis() - data.lastTickTime) / 1000.0;
        if (timeDelta <= 0) timeDelta = 0.05;

        double speedH = horizontalDist / timeDelta;
        double speedV = verticalDist / timeDelta;

        // 检查是否持有七彩神剑（可能开启了飞行能力）
        boolean hasRainbowSword = false;
        for (var stack : player.getInventory().main) {
            if (stack.getItem() instanceof RainbowSwordItem) {
                hasRainbowSword = true;
                break;
            }
        }
        
        // 七彩神剑契约后的飞行能力豁免
        boolean exemptFlight = player.getAbilities().flying || player.getAbilities().allowFlying || hasRainbowSword;
        boolean invalid = false;

        if (!exemptFlight && speedH > MAX_HORIZONTAL_SPEED) {
            invalid = true;
        }
        if (!exemptFlight && speedV > MAX_VERTICAL_SPEED) {
            invalid = true;
        }
        // 加速度检测（防止瞬间加速）
        double accel = (horizontalDist - data.totalMovement) / timeDelta;
        if (!exemptFlight && accel > MAX_ACCELERATION && horizontalDist > 1.0) {
            invalid = true;
        }

        if (invalid) {
            data.invalidMoveCount++;
            if (data.invalidMoveCount >= MAX_INVALID_MOVES) {
                punish(player, "非法移动速度");
                return false;
            }
        } else {
            data.invalidMoveCount = Math.max(0, data.invalidMoveCount - 1);
        }

        data.lastX = player.getX();
        data.lastY = player.getY();
        data.lastZ = player.getZ();
        data.lastYaw = player.getYaw();
        data.lastPitch = player.getPitch();
        data.totalMovement = horizontalDist;
        data.lastTickTime = System.currentTimeMillis();
        return true;
    }

    private static void punish(ServerPlayerEntity player, String reason) {
        player.networkHandler.disconnect(net.minecraft.text.Text.literal("反作弊器检测到异常移动: " + reason));
        AntiCheatManager.getInstance().removePlayer(player);
    }
}
