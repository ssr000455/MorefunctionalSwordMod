package com.qidai.morefunctionalswordmod.entity.client.calamity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PhantomEntityModel;
import net.minecraft.client.render.entity.model.WardenEntityModel;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.Identifier;

/**
 * 基础灾厄生物渲染器
 * 手动渲染模型，避免类型转换崩溃
 */
public class CalamityBaseRenderer extends MobEntityRenderer<HostileEntity, EntityModel<HostileEntity>> {
    private final Identifier texture;
    private final float scaleX, scaleY, scaleZ;
    private final float[] color;

    public CalamityBaseRenderer(EntityRendererFactory.Context ctx, Identifier texture,
                                EntityModel<?> model, float sx, float sy, float sz, float[] color) {
        super(ctx, (EntityModel<HostileEntity>) model, 0.5f);
        this.texture = texture;
        this.scaleX = sx;
        this.scaleY = sy;
        this.scaleZ = sz;
        this.color = color;
    }

    @Override
    public Identifier getTexture(HostileEntity entity) {
        return texture;
    }

    @Override
    public void render(HostileEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        // 手动渲染，不调用 super.render，避免类型转换问题
        matrices.push();
        matrices.scale(scaleX, scaleY, scaleZ);
        RenderSystem.setShaderColor(color[0], color[1], color[2], color.length > 3 ? color[3] : 1.0f);

        // 设置模型角度
        model.setAngles(entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(),
                entity.age, entity.headYaw, entity.getPitch());
        // 渲染模型
        model.render(matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        matrices.pop();
    }

    // ---- 工厂方法 ----

    public static CalamityBaseRenderer createSoldier(EntityRendererFactory.Context ctx) {
        return new CalamityBaseRenderer(ctx,
                new Identifier("textures/entity/zombie/zombie.png"),
                new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)),
                1.0f, 1.0f, 1.0f,
                new float[]{0.5f, 0.0f, 0.8f});
    }

    public static CalamityBaseRenderer createWraith(EntityRendererFactory.Context ctx) {
        return new CalamityBaseRenderer(ctx,
                new Identifier("textures/entity/zombie/zombie.png"),
                new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)),
                0.8f, 1.6f, 0.8f,
                new float[]{0.3f, 0.9f, 0.5f, 0.7f});
    }

    public static CalamityBaseRenderer createPhantom(EntityRendererFactory.Context ctx) {
        return new CalamityBaseRenderer(ctx,
                new Identifier("textures/entity/phantom.png"),
                new PhantomEntityModel<>(ctx.getPart(EntityModelLayers.PHANTOM)),
                0.8f, 0.8f, 0.8f,
                new float[]{0.1f, 0.5f, 0.1f});
    }

    public static CalamityBaseRenderer createWind(EntityRendererFactory.Context ctx) {
        return new CalamityBaseRenderer(ctx,
                new Identifier("textures/entity/warden/warden.png"),
                new WardenEntityModel<>(ctx.getPart(EntityModelLayers.WARDEN)),
                1.2f, 1.2f, 1.2f,
                new float[]{0.7f, 0.1f, 0.1f});
    }
}