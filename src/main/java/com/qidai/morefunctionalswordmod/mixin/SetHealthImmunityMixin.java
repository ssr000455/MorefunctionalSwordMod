package com.qidai.morefunctionalswordmod.mixin;

import com.qidai.morefunctionalswordmod.RainbowSwordItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class SetHealthImmunityMixin {
    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void onSetHealth(float health, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player) {
            if (health <= 0.0f) {
                ItemStack mainHand = player.getMainHandStack();
                if (mainHand.getItem() instanceof RainbowSwordItem && 
                    mainHand.getOrCreateNbt().getBoolean("HasContract")) {
                    ci.cancel();
                }
            }
        }
    }
}
