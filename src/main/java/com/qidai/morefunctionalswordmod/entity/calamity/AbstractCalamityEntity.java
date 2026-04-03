package com.qidai.morefunctionalswordmod.entity.calamity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class AbstractCalamityEntity extends HostileEntity implements Monster {
    protected static final TrackedData<Integer> HURT_TIME = DataTracker.registerData(AbstractCalamityEntity.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Integer> DEATH_TIME = DataTracker.registerData(AbstractCalamityEntity.class, TrackedDataHandlerRegistry.INTEGER);

    protected AbstractCalamityEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HURT_TIME, 0);
        this.dataTracker.startTracking(DEATH_TIME, -1);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            int hurt = dataTracker.get(HURT_TIME);
            if (hurt > 0) dataTracker.set(HURT_TIME, hurt - 1);
            int deathTime = dataTracker.get(DEATH_TIME);
            if (deathTime >= 0) {
                if (deathTime == 0) this.remove(RemovalReason.KILLED);
                else dataTracker.set(DEATH_TIME, deathTime - 1);
            }
        } else {
            spawnParticles();
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean damaged = super.damage(source, amount);
        if (damaged && !this.getWorld().isClient) dataTracker.set(HURT_TIME, 2);
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

    // 子类必须提供粒子颜色参数
    protected abstract float[] getParticleColor();

    // 子类可以覆盖此方法以改变粒子生成逻辑
    protected void spawnParticles() {
        int hurt = dataTracker.get(HURT_TIME);
        int death = dataTracker.get(DEATH_TIME);
        float[] color = getParticleColor();

        float redFactor = (hurt > 0) ? 1.5f : 1.0f;
        float alpha = (death >= 0) ? death / 20.0f : 1.0f;

        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        // 根据实体尺寸调整粒子范围
        float width = this.getWidth();
        float height = this.getHeight();

        // 头部粒子
        spawnBoxParticles(x, y + height * 0.8, z, width * 0.8f, height * 0.2f, width * 0.8f,
                          color[0] * redFactor, color[1] * redFactor, color[2] * redFactor, alpha);

        // 身体粒子
        spawnBoxParticles(x, y + height * 0.3, z, width * 0.6f, height * 0.5f, width * 0.3f,
                          color[0] * 0.8f * redFactor, color[1] * 0.8f * redFactor, color[2] * 0.8f * redFactor, alpha);

        // 四肢粒子（简化为随机点）
        for (int i = 0; i < 20; i++) {
            double offsetX = (random.nextDouble() - 0.5) * width;
            double offsetY = (random.nextDouble() - 0.5) * height;
            double offsetZ = (random.nextDouble() - 0.5) * width;
            spawnParticle(x + offsetX, y + offsetY, z + offsetZ,
                          color[0] * 0.7f * redFactor, color[1] * 0.7f * redFactor, color[2] * 0.7f * redFactor, alpha);
        }
    }

    protected void spawnBoxParticles(double centerX, double centerY, double centerZ,
                                     float sizeX, float sizeY, float sizeZ,
                                     float r, float g, float b, float alpha) {
        int stepsX = Math.max(2, (int)(sizeX * 8));
        int stepsY = Math.max(2, (int)(sizeY * 8));
        int stepsZ = Math.max(2, (int)(sizeZ * 8));

        for (int ix = 0; ix < stepsX; ix++) {
            for (int iy = 0; iy < stepsY; iy++) {
                for (int iz = 0; iz < stepsZ; iz++) {
                    double offsetX = (ix / (double)stepsX - 0.5) * sizeX;
                    double offsetY = (iy / (double)stepsY - 0.5) * sizeY;
                    double offsetZ = (iz / (double)stepsZ - 0.5) * sizeZ;
                    spawnParticle(centerX + offsetX, centerY + offsetY, centerZ + offsetZ, r, g, b, alpha);
                }
            }
        }
    }

    protected void spawnParticle(double x, double y, double z, float r, float g, float b, float alpha) {
        ParticleEffect particle = new DustParticleEffect(new Vec3d(r, g, b).toVector3f(), 1.0f);
        this.getWorld().addParticle(particle, x, y, z, 0, 0, 0);
    }

    @Override
    public boolean shouldRender(double distance) {
        return true;
    }
}
