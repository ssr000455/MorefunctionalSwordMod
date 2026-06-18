package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
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
            // Minecraft 附魔存储在 NbtList 中，键名为 "Enchantments"
            // 每个元素是 {id: "minecraft:sharpness", lvl: 5}
            NbtCompound stackNbt = stack.getNbt();
            if (stackNbt != null && stackNbt.contains("Enchantments", 9)) {
                NbtList enchants = stackNbt.getList("Enchantments", 10);
                for (int i = 0; i < enchants.size(); i++) {
                    NbtCompound enchant = enchants.getCompound(i);
                    int lvl = enchant.getInt("lvl");
                    if (lvl > 32767 || lvl < 0) {
                        AntiCheatManager.getInstance().kickPlayer(player,
                            "附魔等级异常: " + enchant.getString("id") + " level " + lvl);
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
