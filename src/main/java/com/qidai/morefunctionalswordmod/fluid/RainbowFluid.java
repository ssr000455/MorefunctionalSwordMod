package com.qidai.morefunctionalswordmod.fluid;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class RainbowFluid extends FlowableFluid {
    protected boolean isInfinite(World world) {
        return false;
    }

    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        world.syncWorldEvent(1501, pos, 0);
    }

    protected int getFlowSpeed(WorldView world) {
        return 2;
    }

    protected int getLevelDecreasePerBlock(WorldView world) {
        return 1;
    }

    public int getTickRate(WorldView world) {
        return 5;
    }

    protected float getBlastResistance() {
        return 100.0f;
    }

    protected int getMaxFlowDistance(WorldView world) {
        return 8;
    }

    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    public static class Flowing extends RainbowFluid {
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }

        public boolean isStill(FluidState state) {
            return false;
        }

        public Fluid getFlowing() { return this; }
        
        public Fluid getStill() { return null; }
        
        protected BlockState toBlockState(FluidState state) { return null; }
        
        public Item getBucketItem() { return null; }
    }

    public static class Still extends RainbowFluid {
        public int getLevel(FluidState state) {
            return 8;
        }

        public boolean isStill(FluidState state) {
            return true;
        }

        public Fluid getFlowing() { return null; }
        
        public Fluid getStill() { return this; }
        
        protected BlockState toBlockState(FluidState state) { return null; }
        
        public Item getBucketItem() { return null; }
    }
}
