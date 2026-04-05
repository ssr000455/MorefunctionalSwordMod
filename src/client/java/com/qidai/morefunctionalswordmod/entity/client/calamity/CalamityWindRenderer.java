package com.qidai.morefunctionalswordmod.entity.client.calamity;

import com.qidai.morefunctionalswordmod.entity.calamity.CalamityWind;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CalamityWindRenderer extends EntityRenderer<CalamityWind> {
    public CalamityWindRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(CalamityWind entity) {
        return null;
    }

    @Override
    public void render(CalamityWind entity, float yaw, float tickDelta, net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
