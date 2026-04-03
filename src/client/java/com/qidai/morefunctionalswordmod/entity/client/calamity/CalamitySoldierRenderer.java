package com.qidai.morefunctionalswordmod.entity.client.calamity;

import com.qidai.morefunctionalswordmod.entity.calamity.CalamitySoldier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CalamitySoldierRenderer extends EntityRenderer<CalamitySoldier> {
    public CalamitySoldierRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(CalamitySoldier entity) {
        return null;
    }

    @Override
    public void render(CalamitySoldier entity, float yaw, float tickDelta, net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
