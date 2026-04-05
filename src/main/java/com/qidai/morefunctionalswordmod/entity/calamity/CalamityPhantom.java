package com.qidai.morefunctionalswordmod.entity.calamity;

import com.qidai.morefunctionalswordmod.ModRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class CalamityPhantom extends AbstractCalamityEntity {
    public CalamityPhantom(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 25.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0D)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.8D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        PathAwareEntity self = this;
        this.goalSelector.add(2, new MeleeAttackGoal(self, 1.2, false));
        this.goalSelector.add(3, new FlyGoal(self, 1.0));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(5, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    protected float[] getParticleColor() {
        return new float[]{0.8f, 0.5f, 0.5f};
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        if (allowDrops) {
            this.dropItem(Items.ROTTEN_FLESH, 1 + this.random.nextInt(2));
            this.dropItem(ModRegistry.DEPLETED_RAINBOW_BLOCK_ITEM, 1);
            this.dropItem(Items.WHEAT, 1 + this.random.nextInt(3));
        }
    }
}
