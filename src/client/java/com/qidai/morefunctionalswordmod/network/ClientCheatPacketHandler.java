package com.qidai.morefunctionalswordmod.network;

import com.qidai.morefunctionalswordmod.gui.CheatScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class ClientCheatPacketHandler {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(CheatScreenPacket.OPEN_SCREEN_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> client.setScreen(new CheatScreen()));
        });
    }
}
