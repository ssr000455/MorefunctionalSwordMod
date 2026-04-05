package com.qidai.morefunctionalswordmod.entity.calamity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CalamitySoldier extends AbstractCalamityEntity {
    private int teleportCooldown = 0;

    public CalamitySoldier(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(com.qidai.morefunctionalswordmod.ModTools.RAINBOW_GEM_SWORD));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D);
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
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            if (teleportCooldown > 0) teleportCooldown--;
            if (teleportCooldown <= 0 && this.getTarget() != null) {
                teleportTowardsTarget();
                teleportCooldown = 300;
            }
        }
    }

    private void teleportTowardsTarget() {
        LivingEntity target = this.getTarget();
        if (target == null) return;
        Vec3d targetPos = target.getPos();
        for (int i = 0; i < 16; i++) {
            double x = targetPos.x + (this.random.nextDouble() - 0.5) * 8.0;
            double y = targetPos.y + this.random.nextInt(3) - 1;
            double z = targetPos.z + (this.random.nextDouble() - 0.5) * 8.0;
            if (this.tryTeleport(x, y, z)) {
                this.getWorld().playSound(null, this.prevX, this.prevY, this.prevZ,
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0f, 1.0f);
                break;
            }
        }
    }

    private boolean tryTeleport(double x, double y, double z) {
        if (!this.getWorld().isClient) {
            this.requestTeleport(x, y, z);
            return true;
        }
        return false;
    }

    @Override
    protected float[] getParticleColor() {
        return new float[]{0.5f, 0.0f, 0.8f};
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        if (allowDrops) {
            this.dropItem(com.qidai.morefunctionalswordmod.ModRegistry.DEPLETED_RAINBOW_BLOCK_ITEM, 1);
            this.dropItem(com.qidai.morefunctionalswordmod.ModTools.RAINBOW_GEM_SWORD, 1);
            this.dropItem(net.minecraft.item.Items.BREAD, 1 + this.random.nextInt(2));
        }
    }
}
