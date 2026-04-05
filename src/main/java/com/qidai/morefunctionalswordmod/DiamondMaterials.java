package com.qidai.morefunctionalswordmod;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.function.Supplier;

public class DiamondMaterials {
    public enum Type {
        GREEN(1561, 8.0f, 3.0f, 3, 10, () -> Ingredient.ofItems(ModRegistry.GREEN_DIAMOND),
              33, new int[]{3,6,8,3}, 10, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 2.0f, 0.0f),
        YELLOW(1561, 8.0f, 3.0f, 3, 10, () -> Ingredient.ofItems(ModRegistry.YELLOW_DIAMOND),
               33, new int[]{3,6,8,3}, 10, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 2.0f, 0.0f),
        PURPLE(2031, 9.0f, 4.0f, 4, 15, () -> Ingredient.ofItems(ModRegistry.PURPLE_DIAMOND),
               37, new int[]{4,7,9,4}, 15, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 3.0f, 0.1f),
        PINK(4062, 18.0f, 8.0f, 5, 30, () -> Ingredient.ofItems(ModRegistry.PINK_DIAMOND),
             74, new int[]{6,12,16,6}, 30, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 6.0f, 0.2f);

        public final int toolDurability;
        public final float miningSpeed;
        public final float toolAttackDamage;
        public final int miningLevel;
        public final int toolEnchantability;
        public final Supplier<Ingredient> repairIngredient;

        public final int armorDurabilityMultiplier;
        public final int[] protectionValues;
        public final int armorEnchantability;
        public final SoundEvent equipSound;
        public final float toughness;
        public final float knockbackResistance;

        Type(int toolDurability, float miningSpeed, float toolAttackDamage, int miningLevel,
             int toolEnchantability, Supplier<Ingredient> repairIngredient,
             int armorDurabilityMultiplier, int[] protectionValues, int armorEnchantability,
             SoundEvent equipSound, float toughness, float knockbackResistance) {
            this.toolDurability = toolDurability;
            this.miningSpeed = miningSpeed;
            this.toolAttackDamage = toolAttackDamage;
            this.miningLevel = miningLevel;
            this.toolEnchantability = toolEnchantability;
            this.repairIngredient = repairIngredient;
            this.armorDurabilityMultiplier = armorDurabilityMultiplier;
            this.protectionValues = protectionValues;
            this.armorEnchantability = armorEnchantability;
            this.equipSound = equipSound;
            this.toughness = toughness;
            this.knockbackResistance = knockbackResistance;
        }

        public ToolMaterial asToolMaterial() {
            return new ToolMaterial() {
                @Override public int getDurability() { return toolDurability; }
                @Override public float getMiningSpeedMultiplier() { return miningSpeed; }
                @Override public float getAttackDamage() { return toolAttackDamage; }
                @Override public int getMiningLevel() { return miningLevel; }
                @Override public int getEnchantability() { return toolEnchantability; }
                @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
            };
        }

        public ArmorMaterial asArmorMaterial() {
            return new ArmorMaterial() {
                @Override public int getDurability(ArmorItem.Type type) {
                    return BASE_DURABILITY[type.ordinal()] * armorDurabilityMultiplier;
                }
                @Override public int getProtection(ArmorItem.Type type) {
                    return protectionValues[type.ordinal()];
                }
                @Override public int getEnchantability() { return armorEnchantability; }
                @Override public SoundEvent getEquipSound() { return equipSound; }
                @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
                @Override public String getName() { return name().toLowerCase() + "_diamond"; }
                @Override public float getToughness() { return toughness; }
                @Override public float getKnockbackResistance() { return knockbackResistance; }
            };
        }
    }

    private static final int[] BASE_DURABILITY = new int[] {13, 15, 16, 11};
}
