package com.qidai.morefunctionalswordmod.item;

import com.qidai.morefunctionalswordmod.ModDimensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RainbowKeyItem extends Item {
    public RainbowKeyItem(Settings settings) {
        super(settings.maxDamage(2));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        if (world.isClient) {
            return TypedActionResult.success(stack);
        }

        if (!(user instanceof ServerPlayerEntity serverPlayer)) {
            return TypedActionResult.pass(stack);
        }

        ServerWorld currentWorld = (ServerWorld) world;
        ServerWorld targetWorld;
        boolean isInRainbowSea = false;
        
        if (currentWorld.getRegistryKey() == ModDimensions.RAINBOW_SEA_WORLD_KEY) {
            targetWorld = currentWorld.getServer().getWorld(World.OVERWORLD);
            isInRainbowSea = true;
        } else {
            targetWorld = currentWorld.getServer().getWorld(ModDimensions.RAINBOW_SEA_WORLD_KEY);
        }
        
        if (targetWorld == null) {
            return TypedActionResult.pass(stack);
        }
        
        BlockPos safePos = findSafePosition(targetWorld, serverPlayer.getBlockPos());
        if (safePos == null) {
            user.sendMessage(net.minecraft.text.Text.literal("§c找不到安全的传送位置！"), true);
            return TypedActionResult.fail(stack);
        }
        
        serverPlayer.teleport(targetWorld, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, 
                              serverPlayer.getYaw(), serverPlayer.getPitch());
        
        targetWorld.playSound(null, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, 
            SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        
        if (!serverPlayer.getAbilities().creativeMode) {
            stack.damage(1, serverPlayer, (p) -> p.sendToolBreakStatus(hand));
        }
        
        if (isInRainbowSea) {
            serverPlayer.sendMessage(net.minecraft.text.Text.literal("§a你已返回主世界！"), true);
        } else {
            serverPlayer.sendMessage(net.minecraft.text.Text.literal("§a你已进入彩虹海！"), true);
        }
        
        return TypedActionResult.success(stack);
    }
    
    private BlockPos findSafePosition(ServerWorld world, BlockPos originalPos) {
        int searchRadius = 10;
        int verticalSearch = 20;
        
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        
        for (int yOffset = 0; yOffset <= verticalSearch; yOffset++) {
            BlockPos checkPos = findSafeAtHeight(world, originalPos.getX(), originalPos.getY() + yOffset, originalPos.getZ(), searchRadius);
            if (checkPos != null) return checkPos;
            
            if (yOffset > 0) {
                checkPos = findSafeAtHeight(world, originalPos.getX(), originalPos.getY() - yOffset, originalPos.getZ(), searchRadius);
                if (checkPos != null) return checkPos;
            }
        }
        
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                for (int y = -verticalSearch; y <= verticalSearch; y++) {
                    mutablePos.set(originalPos.getX() + x, originalPos.getY() + y, originalPos.getZ() + z);
                    if (isSafePosition(world, mutablePos)) {
                        return mutablePos.toImmutable();
                    }
                }
            }
        }
        return null;
    }
    
    private BlockPos findSafeAtHeight(ServerWorld world, int x, int y, int z, int radius) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                mutablePos.set(x + dx, y, z + dz);
                if (isSafePosition(world, mutablePos)) {
                    return mutablePos.toImmutable();
                }
            }
        }
        return null;
    }
    
    private boolean isSafePosition(ServerWorld world, BlockPos pos) {
        // 检查脚下是否有固体方块
        if (world.getBlockState(pos.down()).isAir()) return false;
        // 检查玩家站立位置（1格高）
        if (!world.getBlockState(pos).isAir()) return false;
        // 检查玩家头部位置（2格高，玩家实际高度1.8格）
        if (!world.getBlockState(pos.up()).isAir()) return false;
        // 额外检查头顶上方是否有窒息方块（防止卡进天花板）
        if (!world.getBlockState(pos.up(2)).isAir()) {
            // 如果头顶上方有方块，检查是否有足够空间
            if (!world.getBlockState(pos.up(2)).isReplaceable()) return false;
        }
        return true;
    }
}
