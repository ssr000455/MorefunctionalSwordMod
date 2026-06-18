package com.qidai.morefunctionalswordmod.entity.client.calamity;

import com.qidai.morefunctionalswordmod.entity.calamity.CalamityPhantom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;

/**
 * 灾厄幻影渲染器 — 显示为暗绿色飞行生物轮廓
 */
@Environment(EnvType.CLIENT)
public class CalamityPhantomRenderer extends CalamityBaseRenderer {
    public CalamityPhantomRenderer(EntityRendererFactory.Context ctx) {
        super(ctx,
                new net.minecraft.util.Identifier("textures/entity/phantom.png"),
                new net.minecraft.client.render.entity.model.PhantomEntityModel<>(
                        ctx.getPart(net.minecraft.client.render.entity.model.EntityModelLayers.PHANTOM)),
                0.8f, 0.8f, 0.8f,
                new float[]{0.1f, 0.5f, 0.1f});
    }
}
