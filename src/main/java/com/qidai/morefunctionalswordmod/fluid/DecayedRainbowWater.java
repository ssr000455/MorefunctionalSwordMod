package com.qidai.morefunctionalswordmod.fluid;

import com.qidai.morefunctionalswordmod.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;

public abstract class DecayedRainbowWater extends RainbowFluid {
    @Override
    public Fluid getFlowing() {
        return ModRegistry.FLOWING_DECAYED_RAINBOW_WATER;
    }

    @Override
    public Fluid getStill() {
        return ModRegistry.STILL_DECAYED_RAINBOW_WATER;
    }

    @Override
    public Item getBucketItem() {
        return ModRegistry.DECAYED_RAINBOW_WATER_BUCKET;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return ModRegistry.DECAYED_RAINBOW_WATER_BLOCK.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
    }

    public static class Flowing extends DecayedRainbowWater {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }
    }

    public static class Still extends DecayedRainbowWater {
        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }
    }
}
