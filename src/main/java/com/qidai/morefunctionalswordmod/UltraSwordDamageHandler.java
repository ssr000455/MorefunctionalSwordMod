package com.qidai.morefunctionalswordmod;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class UltraSwordDamageHandler {
    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof PlayerEntity player) {
                ItemStack mainHand = player.getMainHandStack();
                if (mainHand.getItem() == ModTools.ULTRA_PINK_DIAMOND_SWORD &&
                    mainHand.hasNbt() && mainHand.getNbt().contains("UltraSword")) {
                    if (player.getRandom().nextFloat() < 0.1f) {
                        return false;
                    }
                }
            }
            return true;
        });
    }
}
