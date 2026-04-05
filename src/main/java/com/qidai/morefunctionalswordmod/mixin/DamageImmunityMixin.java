package com.qidai.morefunctionalswordmod.mixin;

import com.qidai.morefunctionalswordmod.RainbowSwordItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class DamageImmunityMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof PlayerEntity player) {
            ItemStack mainHand = player.getMainHandStack();
            if (mainHand.getItem() instanceof RainbowSwordItem && 
                mainHand.getOrCreateNbt().getBoolean("HasContract")) {
                cir.setReturnValue(false);
            }
        }
        // 如果伤害来源是玩家且该玩家持剑，则取消对目标的伤害（防止玩家攻击其他生物时也免疫？但这里目标是生物时不需要，所以只拦截玩家自身）
        // 上面的代码已经拦截了对玩家自身的伤害，下面拦截对别的实体的伤害（如果攻击者持剑，则取消对目标的伤害，这会让玩家攻击无伤害？但攻击者持剑我们可能想保留攻击伤害，所以不拦截对别的实体的伤害，只拦截对持剑者自身的伤害）
        // 因此不需要额外拦截对别的实体的伤害。
    }
}
