package com.qidai.morefunctionalswordmod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DepletedRainbowBlock extends Block {
    public DepletedRainbowBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClient && entity instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0));
        }
        super.onSteppedOn(world, pos, state, entity);
    }
}
