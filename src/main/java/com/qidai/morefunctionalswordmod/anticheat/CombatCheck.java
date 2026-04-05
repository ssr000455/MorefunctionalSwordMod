package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import com.qidai.morefunctionalswordmod.RainbowSwordItem;

public class CombatCheck {
    private static final int MAX_CPS = 15;
    private static final int WINDOW_MS = 1000;
    private static final int MAX_INVALID_ATTACKS = 3;

    public static boolean onAttack(ServerPlayerEntity player) {
        AntiCheatManager.AntiCheatData data = AntiCheatManager.getInstance().getData(player);
        ItemStack mainHand = player.getMainHandStack();
        boolean isRainbowSword = mainHand.getItem() instanceof RainbowSwordItem;
        boolean isSpecialMode = isRainbowSword && mainHand.getOrCreateNbt().getBoolean("SpecialAttackMode");
        boolean isContinuousAttack = isRainbowSword && mainHand.getOrCreateNbt().getBoolean("ContinuousAttack");

        if (isSpecialMode || isContinuousAttack) {
            return true;
        }

        long now = System.currentTimeMillis();
        if (now - data.lastAttackTime > WINDOW_MS) {
            data.attackCount = 1;
        } else {
            data.attackCount++;
            double cps = data.attackCount * 1000.0 / (now - data.lastAttackTime);
            if (cps > MAX_CPS) {
                data.invalidAttackCount++;
                if (data.invalidAttackCount >= MAX_INVALID_ATTACKS) {
                    CheatPunisher.punish(player, "攻击速度异常 (" + String.format("%.1f", cps) + " CPS)");
                    return false;
                }
            } else {
                data.invalidAttackCount = Math.max(0, data.invalidAttackCount - 1);
            }
        }
        data.lastAttackTime = now;
        return true;
    }
}
