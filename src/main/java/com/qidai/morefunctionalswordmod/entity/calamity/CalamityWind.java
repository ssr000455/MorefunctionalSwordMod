package com.qidai.morefunctionalswordmod.entity.calamity;

import com.qidai.morefunctionalswordmod.ModEntities;
import com.qidai.morefunctionalswordmod.ModRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CalamityWind extends AbstractCalamityEntity {
    private int dashCooldown = 0;
    private int summonCooldown = 0;

    public CalamityWind(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.setCustomName(Text.literal("LV.2 小首领").formatted(Formatting.GOLD, Formatting.BOLD));
        this.setCustomNameVisible(true);
        this.experiencePoints = 20;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5D);
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
            if (dashCooldown > 0) dashCooldown--;
            if (summonCooldown > 0) summonCooldown--;

            if (dashCooldown <= 0 && this.random.nextFloat() < 0.25f) {
                performDash();
                dashCooldown = 600;
            }

            if (summonCooldown <= 0) {
                performSummon();
                summonCooldown = 600;
            }
        }
    }

    private void performDash() {
        LivingEntity target = this.getTarget();
        if (target == null) return;

        Vec3d direction = target.getPos().subtract(this.getPos()).normalize();
        this.setVelocity(direction.multiply(2.0));
        this.velocityDirty = true;

        if (this.distanceTo(target) < 3.0) {
            target.damage(this.getDamageSources().mobAttack(this), 15.0f);
            Vec3d knockback = target.getPos().subtract(this.getPos()).normalize().multiply(3.0);
            target.addVelocity(knockback.x, 0.5, knockback.z);
        }
    }

    private void performSummon() {
        for (int i = 0; i < 5; i++) {
            CalamityWraith wraith = new CalamityWraith(ModEntities.CALAMITY_WRAITH, this.getWorld());
            wraith.setPosition(this.getPos());
            this.getWorld().spawnEntity(wraith);
        }
        for (int i = 0; i < 3; i++) {
            CalamityPhantom phantom = new CalamityPhantom(ModEntities.CALAMITY_PHANTOM, this.getWorld());
            phantom.setPosition(this.getPos());
            this.getWorld().spawnEntity(phantom);
        }
        CalamitySoldier soldier = new CalamitySoldier(ModEntities.CALAMITY_SOLDIER, this.getWorld());
        soldier.setPosition(this.getPos());
        this.getWorld().spawnEntity(soldier);
    }

    @Override
    protected float[] getParticleColor() {
        int hurt = this.dataTracker.get(HURT_TIME);
        float factor = (hurt > 0) ? 1.5f : 1.0f;
        return new float[]{0.3f * factor, 0.8f * factor, 0.9f * factor};
    }

    @Override
    protected void spawnParticles() {
        int hurt = this.dataTracker.get(HURT_TIME);
        int death = this.dataTracker.get(DEATH_TIME);
        float[] color = getParticleColor();

        float alpha = (death >= 0) ? death / 20.0f : 1.0f;
        double x = this.getX();
        double y = this.getY() + 1.0;
        double z = this.getZ();

        for (int i = 0; i < 50; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = 1.0 + random.nextDouble() * 0.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            double offsetY = random.nextDouble() * 2.5 - 1.25;
            spawnParticle(x + offsetX, y + offsetY, z + offsetZ, color[0], color[1], color[2], alpha);
        }
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        if (allowDrops) {
            this.dropItem(ModRegistry.CALAMITY_WIND_ITEM, 1);
            this.dropItem(Items.ROTTEN_FLESH, 2 + this.random.nextInt(4));
            this.dropItem(Items.NAME_TAG, 1);
            this.dropItem(ModRegistry.DEPLETED_RAINBOW_BLOCK_ITEM, 1 + this.random.nextInt(3));
        }
    }
}
