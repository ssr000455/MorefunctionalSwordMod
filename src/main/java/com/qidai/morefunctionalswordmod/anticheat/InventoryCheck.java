package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import com.qidai.morefunctionalswordmod.RainbowSwordItem;

public class InventoryCheck {
    private static final int MAX_STACK_SIZE = 127;

    public static boolean check(ServerPlayerEntity player) {
        if (AntiCheatManager.getInstance().isExempt(player)) return true;

        for (ItemStack stack : player.getInventory().main) {
            if (stack.isEmpty()) continue;
            int count = stack.getCount();
            if (count > MAX_STACK_SIZE && !(stack.getItem() instanceof RainbowSwordItem)) {
                AntiCheatManager.getInstance().kickPlayer(player, 
                    "物品堆叠数量异常: " + stack.getItem().getName().getString() + " x" + count);
                return false;
            }
            if (stack.hasNbt() && stack.getNbt().contains("Enchantments")) {
                NbtCompound enchants = stack.getNbt().getCompound("Enchantments");
                for (String key : enchants.getKeys()) {
                    int lvl = enchants.getInt(key);
                    if (lvl > 32767 || lvl < 0) {
                        AntiCheatManager.getInstance().kickPlayer(player, 
                            "附魔等级异常: " + key + " level " + lvl);
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
