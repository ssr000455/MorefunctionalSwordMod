package com.qidai.morefunctionalswordmod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RainbowGemBlock extends Block {
    public RainbowGemBlock(Settings settings) {
        super(settings.strength(50.0f, 1200.0f).requiresTool());
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof LivingEntity living) {
            living.setFireTicks(0); // 免疫火焰
        }
        super.onSteppedOn(world, pos, state, entity);
    }
}
