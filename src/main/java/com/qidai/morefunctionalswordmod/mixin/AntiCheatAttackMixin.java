package com.qidai.morefunctionalswordmod.mixin;

import com.qidai.morefunctionalswordmod.anticheat.CombatCheck;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class AntiCheatAttackMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(ServerPlayerEntity player, Entity target, CallbackInfoReturnable<ActionResult> cir) {
        if (!CombatCheck.onAttack(this.player)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
