package com.qidai.morefunctionalswordmod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModUtil {
    public static final String MOD_ID = "mfswordmod";

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static <T extends Item> T register(String path, T item) {
        return Registry.register(Registries.ITEM, id(path), item);
    }

    public static boolean hasContract(ItemStack stack) {
        return stack.getItem() instanceof RainbowSwordItem &&
               stack.getOrCreateNbt().getBoolean("HasContract");
    }

    public static void setContract(ItemStack stack, boolean value) {
        if (stack.getItem() instanceof RainbowSwordItem) {
            stack.getOrCreateNbt().putBoolean("HasContract", value);
        }
    }

    public static boolean isActive(ItemStack stack) {
        return hasContract(stack);
    }
}
