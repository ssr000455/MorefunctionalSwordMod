package com.qidai.morefunctionalswordmod;

import com.qidai.morefunctionalswordmod.network.ClientUIPacketHandler;
import net.fabricmc.api.ClientModInitializer;

public class ExampleModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModKeyBindings.register();
        ModEntityRenderers.register();
        ClientUIPacketHandler.register();
        // 修复：注册彩虹盔甲渲染
        new ModClientEvents().onInitializeClient();
    }
}
