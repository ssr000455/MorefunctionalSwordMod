package com.qidai.morefunctionalswordmod.entity.client.calamity;

import com.qidai.morefunctionalswordmod.entity.calamity.CalamityWind;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;

/**
 * 灾厄之风渲染器 — 显示为暗红色大型人形轮廓（首领）
 */
@Environment(EnvType.CLIENT)
public class CalamityWindRenderer extends CalamityBaseRenderer {
    public CalamityWindRenderer(EntityRendererFactory.Context ctx) {
        super(ctx,
                new net.minecraft.util.Identifier("textures/entity/warden/warden.png"),
                new net.minecraft.client.render.entity.model.WardenEntityModel<>(
                        ctx.getPart(net.minecraft.client.render.entity.model.EntityModelLayers.WARDEN)),
                1.2f, 1.2f, 1.2f,
                new float[]{0.7f, 0.1f, 0.1f});
    }
}
