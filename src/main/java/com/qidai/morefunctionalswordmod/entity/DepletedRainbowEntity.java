package com.qidai.morefunctionalswordmod.entity;

import com.qidai.morefunctionalswordmod.ModRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class DepletedRainbowEntity extends ZombieEntity {
    private boolean isElite;

    public DepletedRainbowEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        this.isElite = this.random.nextFloat() < 0.2f;

        if (this.isElite) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(120.0);
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(12.0);
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.45);
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(5.0);
            this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).setBaseValue(24.0);
            this.setHealth(120.0f);
            this.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.STONE_SWORD));
        } else {
            this.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WOODEN_SWORD));
        }
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.46)
            .add(EntityAttributes.GENERIC_ARMOR, 2.0)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(5, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);

        int gemCount = 1 + this.random.nextInt(2) + lootingMultiplier / 2;
        this.dropItem(ModRegistry.RAINBOW_GEM, gemCount);

        int stringCount = 1 + this.random.nextInt(3) + lootingMultiplier;
        this.dropItem(Items.STRING, stringCount);

        int rottenFleshCount = 1 + this.random.nextInt(2) + lootingMultiplier;
        this.dropItem(Items.ROTTEN_FLESH, rottenFleshCount);

        if (this.isElite) {
            int stoneCount = 1 + this.random.nextInt(3) + lootingMultiplier / 2;
            this.dropItem(Items.STONE, stoneCount);
            if (this.random.nextFloat() < 0.2f + lootingMultiplier * 0.05f) {
                this.dropItem(Items.STONE_SWORD, 1);
            }
        } else {
            if (this.random.nextFloat() < 0.1f + lootingMultiplier * 0.03f) {
                this.dropItem(Items.WOODEN_SWORD, 1);
            }
        }
    }

    public boolean isElite() {
        return isElite;
    }
}
