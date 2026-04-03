package com.qidai.morefunctionalswordmod.network;

import com.qidai.morefunctionalswordmod.gui.RainbowSettingsScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class ClientUIPacketHandler {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(RainbowUIPacket.OPEN_UI_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                if (client.player != null && client.player.getMainHandStack().getItem() instanceof com.qidai.morefunctionalswordmod.RainbowSwordItem) {
                    client.setScreen(new RainbowSettingsScreen());
                } else {
                    client.player.sendMessage(net.minecraft.text.Text.literal("你必须手持七彩神剑才能打开设置界面").formatted(net.minecraft.util.Formatting.RED), false);
                }
            });
        });
    }
}
