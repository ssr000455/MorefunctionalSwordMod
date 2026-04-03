package com.qidai.morefunctionalswordmod.entity.client.calamity;

import com.qidai.morefunctionalswordmod.entity.calamity.CalamityWraith;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CalamityWraithRenderer extends EntityRenderer<CalamityWraith> {
    public CalamityWraithRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(CalamityWraith entity) {
        return null; // 无纹理，粒子效果
    }

    @Override
    public void render(CalamityWraith entity, float yaw, float tickDelta, net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light) {
        // 不需要渲染模型，粒子由实体自己生成
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
