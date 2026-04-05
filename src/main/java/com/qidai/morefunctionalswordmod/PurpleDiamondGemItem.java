package com.qidai.morefunctionalswordmod;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class PurpleDiamondGemItem extends Item {
    public PurpleDiamondGemItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity player) {
            if (selected || player.getOffHandStack() == stack) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 220, 2, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 220, 2, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 220, 0, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 220, 0, true, false, true));
            }
        }
    }
}
