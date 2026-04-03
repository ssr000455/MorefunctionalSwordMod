package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import com.qidai.morefunctionalswordmod.RainbowSwordHelper;
import com.qidai.morefunctionalswordmod.RainbowSwordItem;

public class NbtCheck {
    public static boolean check(ServerPlayerEntity player) {
        if (AntiCheatManager.getInstance().isExempt(player)) return true;

        ItemStack mainHand = player.getMainHandStack();
        if (mainHand.getItem() instanceof RainbowSwordItem) {
            if (!RainbowSwordHelper.verify(player, mainHand)) {
                AntiCheatManager.getInstance().kickPlayer(player, "七彩神剑 NBT 签名验证失败");
                return false;
            }
            NbtCompound nbt = mainHand.getOrCreateNbt();
            float damage = nbt.getFloat("BaseDamage");
            if (damage > 1e7f) {
                AntiCheatManager.getInstance().kickPlayer(player, "BaseDamage 数值异常");
                return false;
            }
        }
        return true;
    }
}
