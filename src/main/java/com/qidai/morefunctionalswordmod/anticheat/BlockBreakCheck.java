package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import com.qidai.morefunctionalswordmod.RainbowSwordItem;
import com.qidai.morefunctionalswordmod.ModUtil;

public class BlockBreakCheck {
    private static final int MAX_BREAKS_PER_SECOND = 30;
    private static final int MAX_INVALID_BREAKS = 5;

    public static boolean onBlockBreak(ServerPlayerEntity player) {
        if (AntiCheatManager.getInstance().isExempt(player)) return true;

        AntiCheatManager.AntiCheatData data = AntiCheatManager.getInstance().getData(player);
        ItemStack mainHand = player.getMainHandStack();
        boolean isRainbowSword = mainHand.getItem() instanceof RainbowSwordItem;
        boolean hasContract = isRainbowSword && ModUtil.hasContract(mainHand);

        if (hasContract) return true;

        long now = System.currentTimeMillis();
        if (now - data.lastBreakTime > 1000) {
            data.breakCount = 1;
        } else {
            data.breakCount++;
            if (data.breakCount > MAX_BREAKS_PER_SECOND) {
                data.invalidBreakCount++;
                if (data.invalidBreakCount >= MAX_INVALID_BREAKS) {
                    AntiCheatManager.getInstance().kickPlayer(player, "破坏方块速度异常 (" + data.breakCount + " blocks/sec)");
                    return false;
                }
            } else {
                data.invalidBreakCount = Math.max(0, data.invalidBreakCount - 1);
            }
        }
        data.lastBreakTime = now;
        return true;
    }
}
