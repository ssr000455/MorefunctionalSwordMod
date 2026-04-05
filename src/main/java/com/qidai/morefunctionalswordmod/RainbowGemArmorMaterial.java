package com.qidai.morefunctionalswordmod;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class RainbowGemArmorMaterial implements ArmorMaterial {
    public static final RainbowGemArmorMaterial INSTANCE = new RainbowGemArmorMaterial();
    private static final int[] BASE_DURABILITY = new int[] {13, 15, 16, 11};
    private static final int[] PROTECTION_VALUES = new int[] {4, 7, 9, 4};

    @Override
    public int getDurability(ArmorItem.Type type) { return BASE_DURABILITY[type.ordinal()] * 37 * 2; }

    @Override
    public int getProtection(ArmorItem.Type type) { return PROTECTION_VALUES[type.ordinal()]; }

    @Override
    public int getEnchantability() { return 30; }

    @Override
    public SoundEvent getEquipSound() { return SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE; }

    @Override
    public Ingredient getRepairIngredient() { return Ingredient.ofItems(ModRegistry.RAINBOW_GEM); }

    @Override
    public String getName() { return "rainbow_gem"; }

    @Override
    public float getToughness() { return 4.0f; }

    @Override
    public float getKnockbackResistance() { return 0.2f; }
}
