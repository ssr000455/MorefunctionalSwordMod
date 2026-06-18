package com.qidai.morefunctionalswordmod.entity.client.calamity;

import com.qidai.morefunctionalswordmod.entity.calamity.CalamitySoldier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;

/**
 * 灾厄士兵渲染器 — 使用 CalamityBaseRenderer
 * 显示为暗紫色人形轮廓
 */
@Environment(EnvType.CLIENT)
public class CalamitySoldierRenderer extends CalamityBaseRenderer {
    public CalamitySoldierRenderer(EntityRendererFactory.Context ctx) {
        super(ctx,
                new net.minecraft.util.Identifier("textures/entity/zombie/zombie.png"),
                new net.minecraft.client.render.entity.model.ZombieEntityModel<>(
                        ctx.getPart(net.minecraft.client.render.entity.model.EntityModelLayers.ZOMBIE)),
                1.0f, 1.0f, 1.0f,
                new float[]{0.5f, 0.0f, 0.8f});
    }
}
