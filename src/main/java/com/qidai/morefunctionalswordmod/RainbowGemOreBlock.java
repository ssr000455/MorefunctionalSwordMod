package com.qidai.morefunctionalswordmod;

import net.minecraft.block.BlockState;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class RainbowGemOreBlock extends ExperienceDroppingBlock {
    public RainbowGemOreBlock(Settings settings) {
        super(settings, UniformIntProvider.create(2, 5));
    }

    @Override
    public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.onStacksDropped(state, world, pos, stack, dropExperience);
        dropStack(world, pos, new ItemStack(ModRegistry.RAINBOW_GEM, 1 + world.random.nextInt(2)));
    }
}
