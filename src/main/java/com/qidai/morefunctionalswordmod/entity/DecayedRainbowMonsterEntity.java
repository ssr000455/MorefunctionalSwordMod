package com.qidai.morefunctionalswordmod.entity;

import com.qidai.morefunctionalswordmod.ModRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DecayedRainbowMonsterEntity extends HostileEntity implements Monster {
    private static final TrackedData<Integer> HURT_TIME = DataTracker.registerData(DecayedRainbowMonsterEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> DEATH_TIME = DataTracker.registerData(DecayedRainbowMonsterEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> IN_DECAYED_WATER = DataTracker.registerData(DecayedRainbowMonsterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private static final float BASE_SPEED = 0.3f;
    private static final float BUFFED_SPEED = 0.9f;
    private static final double BASE_HEALTH = 25.0;
    private static final double BUFFED_HEALTH = 50.0;
    private static final float BASE_DAMAGE = 4.0f;
    private static final float BUFFED_DAMAGE = 8.0f;
    private static final float BASE_ARMOR = 1.5f;
    private static final float BUFFED_ARMOR = 3.0f;

    public DecayedRainbowMonsterEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 10;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HURT_TIME, 0);
        this.dataTracker.startTracking(DEATH_TIME, -1);
        this.dataTracker.startTracking(IN_DECAYED_WATER, false);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, BASE_HEALTH)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, BASE_DAMAGE)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, BASE_SPEED)
                .add(EntityAttributes.GENERIC_ARMOR, BASE_ARMOR)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, LivingEntity.class, 10, true, false, 
            entity -> !(entity instanceof DecayedRainbowMonsterEntity)));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient) {
            // 检测是否在衰败彩虹水中（简化版，实际需要检查流体）
            boolean inDecayedWater = this.isTouchingWater(); // 简化，实际应检查特定流体
            boolean wasInWater = this.dataTracker.get(IN_DECAYED_WATER);
            if (inDecayedWater != wasInWater) {
                this.dataTracker.set(IN_DECAYED_WATER, inDecayedWater);
                updateAttributes(inDecayedWater);
            }

            int hurt = dataTracker.get(HURT_TIME);
            if (hurt > 0) {
                dataTracker.set(HURT_TIME, hurt - 1);
            }

            int deathTime = dataTracker.get(DEATH_TIME);
            if (deathTime >= 0) {
                if (deathTime == 0) {
                    this.remove(RemovalReason.KILLED);
                } else {
                    dataTracker.set(DEATH_TIME, deathTime - 1);
                }
            }
        } else {
            spawnParticles();
        }
    }

    private void updateAttributes(boolean inWater) {
        if (inWater) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(BUFFED_HEALTH);
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(BUFFED_DAMAGE);
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(BUFFED_SPEED);
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(BUFFED_ARMOR);
            this.setHealth((float)this.getMaxHealth());
        } else {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(BASE_HEALTH);
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(BASE_DAMAGE);
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(BASE_SPEED);
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(BASE_ARMOR);
            if (this.getHealth() > this.getMaxHealth()) {
                this.setHealth(this.getMaxHealth());
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean damaged = super.damage(source, amount);
        if (damaged && !this.getWorld().isClient) {
            dataTracker.set(HURT_TIME, 2);
        }
        return damaged;
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!this.getWorld().isClient) {
            dataTracker.set(DEATH_TIME, 20);
            this.setHealth(1.0f);
        }
        super.onDeath(source);
    }

    private void spawnParticles() {
        int hurt = dataTracker.get(HURT_TIME);
        int death = dataTracker.get(DEATH_TIME);
        boolean inWater = dataTracker.get(IN_DECAYED_WATER);

        float redFactor = (hurt > 0) ? 1.5f : 1.0f;
        float sizeFactor = inWater ? 1.2f : 0.8f;

        double x = this.getX();
        double y = this.getY() + sizeFactor;
        double z = this.getZ();

        for (int i = 0; i < 50; i++) {
            double offsetX = (random.nextDouble() - 0.5) * sizeFactor * 1.5;
            double offsetY = (random.nextDouble() - 0.5) * sizeFactor * 2.5;
            double offsetZ = (random.nextDouble() - 0.5) * sizeFactor * 1.5;

            float r = 0.3f * redFactor;
            float g = 0.0f;
            float b = 0.6f * redFactor;

            ParticleEffect particle = new DustParticleEffect(new Vec3d(r, g, b).toVector3f(), sizeFactor);
            this.getWorld().addParticle(particle, x + offsetX, y + offsetY, z + offsetZ, 0, 0, 0);
        }
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
    }

    @Override
    public boolean shouldRender(double distance) {
        return true;
    }
}
