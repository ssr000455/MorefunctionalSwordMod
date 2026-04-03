package com.qidai.morefunctionalswordmod.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class RainbowStoneBlock extends Block {
    public RainbowStoneBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        if (random.nextInt(5) == 0) {
            world.addParticle(
                new DustParticleEffect(new Vec3d(0.5, 0.2, 1.0).toVector3f(), 1.0f),
                pos.getX() + random.nextDouble(),
                pos.getY() + 0.5,
                pos.getZ() + random.nextDouble(),
                0, 0, 0
            );
        }
    }
}
