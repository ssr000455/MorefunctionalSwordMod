package com.qidai.morefunctionalswordmod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.world.World;

public class RainbowGhostEntity extends PhantomEntity {
    public RainbowGhostEntity(EntityType<? extends PhantomEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0D)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0D)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D);
    }
}
