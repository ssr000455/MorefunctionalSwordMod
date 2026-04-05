package com.qidai.morefunctionalswordmod.mixin;

import com.qidai.morefunctionalswordmod.anticheat.CheatPunisher;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class PlayerMoveCensorMixin {
    
    @Shadow public ServerPlayerEntity player;
    
    @Inject(method = "onPlayerMove", at = @At("HEAD"), cancellable = true)
    private void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (CheatPunisher.isUnderPunishment(this.player)) {
            ci.cancel();
        }
    }
    
    @Inject(method = "onClickSlot", at = @At("HEAD"), cancellable = true)
    private void onClickSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (CheatPunisher.isUnderPunishment(this.player)) {
            ci.cancel();
        }
    }
    
    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if (CheatPunisher.isUnderPunishment(this.player)) {
            ci.cancel();
        }
    }
    
    @Inject(method = "onPlayerInteractEntity", at = @At("HEAD"), cancellable = true)
    private void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        if (CheatPunisher.isUnderPunishment(this.player)) {
            ci.cancel();
        }
    }
    
    @Inject(method = "onPlayerInteractBlock", at = @At("HEAD"), cancellable = true)
    private void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        if (CheatPunisher.isUnderPunishment(this.player)) {
            ci.cancel();
        }
    }
    
    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
    private void onPlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        if (CheatPunisher.isUnderPunishment(this.player)) {
            ci.cancel();
        }
    }
    
    @Inject(method = "onHandSwing", at = @At("HEAD"), cancellable = true)
    private void onHandSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        if (CheatPunisher.isUnderPunishment(this.player)) {
            ci.cancel();
        }
    }
}
