package com.qidai.morefunctionalswordmod;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class RainbowSwordToolMaterial implements ToolMaterial {
    public static final RainbowSwordToolMaterial INSTANCE = new RainbowSwordToolMaterial();

    @Override
    public int getDurability() {
        return -1; // 无限耐久
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 9999.0f; // 瞬间挖掘
    }

    @Override
    public float getAttackDamage() {
        return 9995.0f; // 基础9995 + 4 = 9999
    }

    @Override
    public int getMiningLevel() {
        return 10; // 可破坏任何方块（包括基岩）
    }

    @Override
    public int getEnchantability() {
        return 50;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY; // 无法修复
    }
}
