package com.qidai.morefunctionalswordmod.mixin;

import com.qidai.morefunctionalswordmod.RainbowSwordItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MiningSpeedMixin {
    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyMiningSpeed(BlockState block, CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack mainHand = player.getMainHandStack();
        if (mainHand.getItem() instanceof RainbowSwordItem && 
            mainHand.getOrCreateNbt().getBoolean("HasContract")) {
            // 返回极大值，实现瞬间挖掘
            cir.setReturnValue(9999.0f);
        }
    }
}
