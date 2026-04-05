package com.qidai.morefunctionalswordmod;

import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RainbowPortalBlock extends NetherPortalBlock {
    public RainbowPortalBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && !entity.hasVehicle() && !entity.hasPassengers() && entity.canUsePortals()) {
            // 获取彩虹海维度
            ServerWorld targetWorld = ((ServerWorld)world).getServer().getWorld(ModDimensions.RAINBOW_SEA_WORLD_KEY);
            if (targetWorld != null) {
                // 传送到彩虹海
                entity.moveToWorld(targetWorld);
            } else {
                // 如果彩虹海维度不存在，就传回主世界
                ServerWorld overworld = ((ServerWorld)world).getServer().getWorld(World.OVERWORLD);
                if (overworld != null) {
                    entity.moveToWorld(overworld);
                }
            }
        }
    }
}
