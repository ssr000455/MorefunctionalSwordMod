package com.qidai.morefunctionalswordmod.entity.client.calamity;

import com.qidai.morefunctionalswordmod.entity.calamity.CalamityWraith;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;

/**
 * 灾厄之灵渲染器 — 显示为瘦长绿白色人形轮廓
 */
@Environment(EnvType.CLIENT)
public class CalamityWraithRenderer extends CalamityBaseRenderer {
    public CalamityWraithRenderer(EntityRendererFactory.Context ctx) {
        super(ctx,
                new net.minecraft.util.Identifier("textures/entity/zombie/zombie.png"),
                new net.minecraft.client.render.entity.model.ZombieEntityModel<>(
                        ctx.getPart(net.minecraft.client.render.entity.model.EntityModelLayers.ZOMBIE)),
                0.8f, 1.6f, 0.8f,
                new float[]{0.3f, 0.9f, 0.5f, 0.7f});
    }
}
