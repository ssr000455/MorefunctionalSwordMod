package com.qidai.morefunctionalswordmod.entity;

import com.qidai.morefunctionalswordmod.ModRegistry;
import com.qidai.morefunctionalswordmod.ModTools;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class RainbowGuardianEntity extends WardenEntity {
    public RainbowGuardianEntity(EntityType<? extends WardenEntity> entityType, World world) {
        super(entityType, world);
        this.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModTools.RAINBOW_GEM_SWORD));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0D)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0D)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.45D)
            .add(EntityAttributes.GENERIC_ARMOR, 0.0D)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        
        int gemCount = 1 + this.random.nextInt(3) + lootingMultiplier;
        this.dropItem(ModRegistry.RAINBOW_GEM, gemCount);
        
        int goldCount = 1 + this.random.nextInt(2) + lootingMultiplier / 2;
        this.dropItem(Items.GOLD_INGOT, goldCount);
        
        int ironCount = 2 + this.random.nextInt(4) + lootingMultiplier;
        this.dropItem(Items.IRON_INGOT, ironCount);
        
        int blockCount = this.random.nextInt(2) + lootingMultiplier / 3;
        if (blockCount > 0) {
            this.dropItem(ModRegistry.RAINBOW_GEM_BLOCK_ITEM, blockCount);
        }
        
        if (this.random.nextFloat() < 0.3f + lootingMultiplier * 0.1f) {
            this.dropItem(ModTools.RAINBOW_GEM_SWORD, 1);
        }
    }
}
