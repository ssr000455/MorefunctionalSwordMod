package com.qidai.morefunctionalswordmod.mixin;

import com.qidai.morefunctionalswordmod.ModUtil;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ForceBreakMixin {
    @Shadow
    public net.minecraft.server.network.ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void onTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        var stack = this.player.getMainHandStack();
        if (ModUtil.hasContract(stack)) {
            if (this.player.getWorld().breakBlock(pos, true)) {
                cir.setReturnValue(true);
            }
        }
    }
}
