package com.qidai.morefunctionalswordmod;

import com.qidai.morefunctionalswordmod.network.ClientCheatPacketHandler;
import com.qidai.morefunctionalswordmod.network.ClientUIPacketHandler;
import net.fabricmc.api.ClientModInitializer;

public class ExampleModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModKeyBindings.register();
        ModEntityRenderers.register();
        ClientUIPacketHandler.register();
        ClientCheatPacketHandler.register();
    }
}
