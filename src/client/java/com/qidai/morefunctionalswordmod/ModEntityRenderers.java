package com.qidai.morefunctionalswordmod;

import com.qidai.morefunctionalswordmod.entity.client.GenericRainbowRenderer;
import com.qidai.morefunctionalswordmod.entity.client.calamity.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class ModEntityRenderers {
    public static void register() {
        // 使用通用渲染器
        EntityRendererRegistry.register(ModEntities.RAINBOW_GUARDIAN, GenericRainbowRenderer::createGuardian);
        EntityRendererRegistry.register(ModEntities.DEPLETED_RAINBOW, GenericRainbowRenderer::createDepleted);
        EntityRendererRegistry.register(ModEntities.RAINBOW_GHOST, GenericRainbowRenderer::createGhost);

        // 灾厄系列实体渲染器（保持不变）
        EntityRendererRegistry.register(ModEntities.CALAMITY_WRAITH, CalamityWraithRenderer::new);
        EntityRendererRegistry.register(ModEntities.CALAMITY_PHANTOM, CalamityPhantomRenderer::new);
        EntityRendererRegistry.register(ModEntities.CALAMITY_WIND, CalamityWindRenderer::new);
        EntityRendererRegistry.register(ModEntities.CALAMITY_SOLDIER, CalamitySoldierRenderer::new);
    }
}
