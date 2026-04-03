package com.qidai.morefunctionalswordmod;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class RainbowGemToolMaterial implements ToolMaterial {
    public static final RainbowGemToolMaterial INSTANCE = new RainbowGemToolMaterial();

    @Override
    public int getDurability() { return 8000; }

    @Override
    public float getMiningSpeedMultiplier() { return 27.0f; }

    @Override
    public float getAttackDamage() { return 299.0f; }

    @Override
    public int getMiningLevel() { return 5; }

    @Override
    public int getEnchantability() { return 30; }

    @Override
    public Ingredient getRepairIngredient() { return Ingredient.ofItems(ModRegistry.RAINBOW_GEM); }
}
