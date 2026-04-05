package com.qidai.morefunctionalswordmod;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;

public class DiamondEnchantmentHelper {

    public static void applyGreenDiamondEffects(ItemStack stack, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            if (stack.getItem() instanceof ArmorItem) {
                stack.addEnchantment(Enchantments.PROTECTION, 2);
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE,
                    Integer.MAX_VALUE,
                    0,
                    false,
                    false,
                    true
                ));
            }

            if (stack.getItem() instanceof SwordItem) {
                stack.addEnchantment(Enchantments.SHARPNESS, 3);
                stack.addEnchantment(Enchantments.SMITE, 1);
            }

            if (stack.getItem() instanceof AxeItem) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.HASTE,
                    Integer.MAX_VALUE,
                    0,
                    false,
                    false,
                    true
                ));
            }

            stack.addEnchantment(Enchantments.UNBREAKING, 1);
            stack.addEnchantment(Enchantments.LUCK_OF_THE_SEA, 1);
        }
    }

    public static void applyYellowDiamondEffects(ItemStack stack, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            if (stack.getItem() instanceof ArmorItem) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE,
                    Integer.MAX_VALUE,
                    0,
                    false,
                    false,
                    true
                ));
            }

            if (stack.getItem() instanceof SwordItem) {
                stack.addEnchantment(Enchantments.SHARPNESS, 1);
                stack.addEnchantment(Enchantments.SMITE, 1);
                stack.addEnchantment(Enchantments.FIRE_ASPECT, 1);
            }

            stack.addEnchantment(Enchantments.UNBREAKING, 1);
        }
    }

    public static void applyPurpleDiamondEffects(ItemStack stack, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            if (stack.getItem() instanceof ArmorItem) {
                stack.addEnchantment(Enchantments.PROTECTION, 3);
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE,
                    Integer.MAX_VALUE,
                    1,
                    false,
                    false,
                    true
                ));
            }

            if (stack.getItem() instanceof SwordItem) {
                stack.addEnchantment(Enchantments.SHARPNESS, 5);
                stack.addEnchantment(Enchantments.SMITE, 1);
                stack.addEnchantment(Enchantments.BANE_OF_ARTHROPODS, 1);
                stack.addEnchantment(Enchantments.FIRE_ASPECT, 2);
            }

            if (stack.getItem() instanceof PickaxeItem) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.HASTE,
                    Integer.MAX_VALUE,
                    2,
                    false,
                    false,
                    true
                ));
            }

            stack.addEnchantment(Enchantments.UNBREAKING, 3);
            stack.addEnchantment(Enchantments.EFFICIENCY, 3);
        }
    }

    public static void applyPinkDiamondEffects(ItemStack stack, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            if (stack.getItem() instanceof ArmorItem) {
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, Integer.MAX_VALUE, 2, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, Integer.MAX_VALUE, 0, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, Integer.MAX_VALUE, 0, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 0, false, false, true));
            }

            if (stack.getItem() instanceof SwordItem) {
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.LOOTING, 3);
                stack.addEnchantment(Enchantments.FIRE_ASPECT, 2);
                stack.addEnchantment(Enchantments.SMITE, 5);
                stack.addEnchantment(Enchantments.MENDING, 1);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, Integer.MAX_VALUE, 1, false, false, true));
            }

            if (stack.getItem() instanceof ToolItem && !(stack.getItem() instanceof SwordItem)) {
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.EFFICIENCY, 4);
                stack.addEnchantment(Enchantments.MENDING, 1);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, Integer.MAX_VALUE, 1, false, false, true));
            }
        }
    }

    public static void applyUltraPinkDiamondEffects(ItemStack stack, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            stack.addEnchantment(Enchantments.MENDING, 1);
            stack.addEnchantment(Enchantments.FIRE_ASPECT, 2);
            stack.addEnchantment(Enchantments.SMITE, 5);
            stack.addEnchantment(Enchantments.BANE_OF_ARTHROPODS, 5);
            stack.addEnchantment(Enchantments.SHARPNESS, 5);
            stack.addEnchantment(Enchantments.UNBREAKING, 3);

            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, Integer.MAX_VALUE, 8, false, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, Integer.MAX_VALUE, 8, false, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 8, false, false, true));

            stack.getOrCreateNbt().putBoolean("UltraSword", true);
        }
    }
}
