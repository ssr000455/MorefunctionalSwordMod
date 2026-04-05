package com.qidai.morefunctionalswordmod.mixin;

import com.qidai.morefunctionalswordmod.RainbowSwordItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class RemoveImmunityMixin {
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof PlayerEntity player) {
            // 拦截直接杀死或强制移除
            if (reason == Entity.RemovalReason.KILLED ||
                reason == Entity.RemovalReason.DISCARDED) {
                ItemStack mainHand = player.getMainHandStack();
                if (mainHand.getItem() instanceof RainbowSwordItem && 
                    mainHand.getOrCreateNbt().getBoolean("HasContract")) {
                    ci.cancel();
                }
            }
        }
    }
}
