package com.qidai.morefunctionalswordmod;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class UltraPinkDiamondToolMaterial implements ToolMaterial {
    public static final UltraPinkDiamondToolMaterial INSTANCE = new UltraPinkDiamondToolMaterial();

    @Override
    public int getDurability() {
        return 10000;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 50.0f;
    }

    @Override
    public float getAttackDamage() {
        return 995.0f;
    }

    @Override
    public int getMiningLevel() {
        return 10;
    }

    @Override
    public int getEnchantability() {
        return 50;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(ModRegistry.PINK_DIAMOND_BLOCK_ITEM);
    }
}
