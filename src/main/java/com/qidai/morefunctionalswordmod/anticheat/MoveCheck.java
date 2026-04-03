package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

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

        boolean exemptFlight = player.getAbilities().flying || player.getAbilities().allowFlying;
        boolean invalid = false;

        if (!exemptFlight && speedH > MAX_HORIZONTAL_SPEED) invalid = true;
        if (!exemptFlight && speedV > MAX_VERTICAL_SPEED) invalid = true;
        double accel = (horizontalDist - data.totalMovement) / timeDelta;
        if (!exemptFlight && accel > MAX_ACCELERATION && horizontalDist > 1.0) invalid = true;

        if (invalid) {
            data.invalidMoveCount++;
            if (data.invalidMoveCount >= MAX_INVALID_MOVES) {
                AntiCheatManager.getInstance().kickPlayer(player, "非法移动速度");
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
}
