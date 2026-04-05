package com.qidai.morefunctionalswordmod.entity.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PhantomEntityModel;
import net.minecraft.client.render.entity.model.WardenEntityModel;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class GenericRainbowRenderer extends MobEntityRenderer<MobEntity, EntityModel<MobEntity>> {
    private final Identifier texture;
    private final float scale;
    private final float[] colorFactors;

    public GenericRainbowRenderer(EntityRendererFactory.Context ctx, Identifier texture,
                                  EntityModel<?> model, float scale, float[] colorFactors) {
        super(ctx, (EntityModel<MobEntity>) model, 0.5f);
        this.texture = texture;
        this.scale = scale;
        this.colorFactors = colorFactors;
    }

    @Override
    public Identifier getTexture(MobEntity entity) {
        return texture;
    }

    @Override
    public void render(MobEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.scale(scale, scale, scale);

        float time = (System.currentTimeMillis() % 20000) / 20000.0f * (float)Math.PI * 2;
        float r = (float)(Math.sin(time) * colorFactors[0] + colorFactors[1]);
        float g = (float)(Math.sin(time + 2.094f) * colorFactors[2] + colorFactors[3]);
        float b = (float)(Math.sin(time + 4.188f) * colorFactors[4] + colorFactors[5]);
        float a = colorFactors.length > 6 ? colorFactors[6] : 1.0f;

        RenderSystem.setShaderColor(r, g, b, a);
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }

    public static GenericRainbowRenderer createDepleted(EntityRendererFactory.Context ctx) {
        return new GenericRainbowRenderer(ctx,
            new Identifier("textures/entity/zombie/zombie.png"),
            new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)),
            1.2f, new float[]{0.2f, 0.3f, 0.2f, 0.3f, 0.2f, 0.3f});
    }

    public static GenericRainbowRenderer createGhost(EntityRendererFactory.Context ctx) {
        return new GenericRainbowRenderer(ctx,
            new Identifier("textures/entity/phantom.png"),
            new PhantomEntityModel<>(ctx.getPart(EntityModelLayers.PHANTOM)),
            1.0f, new float[]{0.1f, 0.5f, 0.3f, 0.7f, 0.3f, 0.8f, 0.7f});
    }

    public static GenericRainbowRenderer createGuardian(EntityRendererFactory.Context ctx) {
        return new GenericRainbowRenderer(ctx,
            new Identifier("textures/entity/warden/warden.png"),
            new WardenEntityModel<>(ctx.getPart(EntityModelLayers.WARDEN)),
            1.0f, new float[]{0.2f, 0.4f, 0.2f, 0.4f, 0.3f, 0.6f});
    }
}
