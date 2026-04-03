package com.qidai.morefunctionalswordmod.mixin;

import com.qidai.morefunctionalswordmod.DiamondEnchantmentHelper;
import com.qidai.morefunctionalswordmod.ModRegistry;
import com.qidai.morefunctionalswordmod.ModTools;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {
    @Inject(method = "onTakeOutput", at = @At("HEAD"))
    private void onTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        AnvilScreenHandler handler = (AnvilScreenHandler)(Object)this;
        ItemStack left = handler.getSlot(0).getStack();
        ItemStack right = handler.getSlot(1).getStack();

        if (!left.isEmpty() && !right.isEmpty()) {
            if (right.getItem() == ModRegistry.GREEN_DIAMOND) {
                DiamondEnchantmentHelper.applyGreenDiamondEffects(left, player);
                handler.getSlot(0).setStack(left);
            }

            if (right.getItem() == ModRegistry.YELLOW_DIAMOND) {
                DiamondEnchantmentHelper.applyYellowDiamondEffects(left, player);
                handler.getSlot(0).setStack(left);
            }

            if (right.getItem() == ModRegistry.PURPLE_DIAMOND) {
                DiamondEnchantmentHelper.applyPurpleDiamondEffects(left, player);
                handler.getSlot(0).setStack(left);
            }

            if (right.getItem() == ModRegistry.PINK_DIAMOND) {
                DiamondEnchantmentHelper.applyPinkDiamondEffects(left, player);
                handler.getSlot(0).setStack(left);
            }

            if (right.getItem() == ModTools.ULTRA_PINK_DIAMOND_SWORD) {
                DiamondEnchantmentHelper.applyUltraPinkDiamondEffects(left, player);
                handler.getSlot(0).setStack(left);
            }
        }
    }
}
