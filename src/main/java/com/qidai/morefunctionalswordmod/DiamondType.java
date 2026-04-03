package com.qidai.morefunctionalswordmod;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public enum DiamondType {
    GREEN(
        "green",
        1561, 8.0f, 3.0f, 3, 10,
        () -> Ingredient.ofItems(ModRegistry.GREEN_DIAMOND),
        new StatusEffectInstance(StatusEffects.RESISTANCE, Integer.MAX_VALUE, 0, false, false, true),
        new StatusEffectInstance(StatusEffects.HASTE, Integer.MAX_VALUE, 0, false, false, true)
    ),
    YELLOW(
        "yellow",
        1561, 8.0f, 3.0f, 3, 10,
        () -> Ingredient.ofItems(ModRegistry.YELLOW_DIAMOND),
        new StatusEffectInstance(StatusEffects.RESISTANCE, Integer.MAX_VALUE, 0, false, false, true)
    ),
    PURPLE(
        "purple",
        2031, 9.0f, 4.0f, 4, 15,
        () -> Ingredient.ofItems(ModRegistry.PURPLE_DIAMOND),
        new StatusEffectInstance(StatusEffects.RESISTANCE, Integer.MAX_VALUE, 1, false, false, true),
        new StatusEffectInstance(StatusEffects.HASTE, Integer.MAX_VALUE, 2, false, false, true)
    ),
    PINK(
        "pink",
        4062, 18.0f, 8.0f, 5, 30,
        () -> Ingredient.ofItems(ModRegistry.PINK_DIAMOND),
        new StatusEffectInstance(StatusEffects.RESISTANCE, Integer.MAX_VALUE, 2, false, false, true),
        new StatusEffectInstance(StatusEffects.REGENERATION, Integer.MAX_VALUE, 0, false, false, true),
        new StatusEffectInstance(StatusEffects.LUCK, Integer.MAX_VALUE, 0, false, false, true),
        new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 0, false, false, true),
        new StatusEffectInstance(StatusEffects.HASTE, Integer.MAX_VALUE, 1, false, false, true)
    );

    public final String name;
    public final int durability;
    public final float miningSpeed;
    public final float attackDamage;
    public final int miningLevel;
    public final int enchantability;
    public final Supplier<Ingredient> repairIngredient;
    public final StatusEffectInstance[] heldEffects;      // 手持工具时获得的效果
    public final StatusEffectInstance[] armorEffects;     // 穿戴盔甲时获得的效果

    DiamondType(String name, int durability, float miningSpeed, float attackDamage,
                int miningLevel, int enchantability, Supplier<Ingredient> repairIngredient,
                StatusEffectInstance... effects) {
        this.name = name;
        this.durability = durability;
        this.miningSpeed = miningSpeed;
        this.attackDamage = attackDamage;
        this.miningLevel = miningLevel;
        this.enchantability = enchantability;
        this.repairIngredient = repairIngredient;
        // 前两个效果给工具，其余给盔甲（可根据需要调整）
        int split = Math.min(2, effects.length);
        this.heldEffects = new StatusEffectInstance[split];
        System.arraycopy(effects, 0, heldEffects, 0, split);
        this.armorEffects = new StatusEffectInstance[effects.length - split];
        System.arraycopy(effects, split, armorEffects, 0, effects.length - split);
    }

    public ToolMaterial asToolMaterial() {
        return new ToolMaterial() {
            @Override public int getDurability() { return durability; }
            @Override public float getMiningSpeedMultiplier() { return miningSpeed; }
            @Override public float getAttackDamage() { return attackDamage; }
            @Override public int getMiningLevel() { return miningLevel; }
            @Override public int getEnchantability() { return enchantability; }
            @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
        };
    }
}
