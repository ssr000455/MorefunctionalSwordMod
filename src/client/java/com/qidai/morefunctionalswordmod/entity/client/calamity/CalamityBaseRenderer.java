package com.qidai.morefunctionalswordmod.entity.client.calamity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.Identifier;

/**
 * 基础灾厄生物渲染器
 * 使用原版模型 + 颜色染色，在没有自定义模型的情况下显示基本的生物轮廓
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
        matrices.push();
        matrices.scale(scaleX, scaleY, scaleZ);
        RenderSystem.setShaderColor(color[0], color[1], color[2], color.length > 3 ? color[3] : 1.0f);
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        matrices.pop();
    }

    // ---- 工厂方法 ----

    /** 灾厄士兵：人形，暗紫色 */
    public static CalamityBaseRenderer createSoldier(EntityRendererFactory.Context ctx) {
        return new CalamityBaseRenderer(ctx,
                new Identifier("textures/entity/zombie/zombie.png"),
                new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)),
                1.0f, 1.0f, 1.0f,
                new float[]{0.5f, 0.0f, 0.8f});
    }

    /** 灾厄之灵：瘦高人形，幽灵白绿色 */
    public static CalamityBaseRenderer createWraith(EntityRendererFactory.Context ctx) {
        return new CalamityBaseRenderer(ctx,
                new Identifier("textures/entity/zombie/zombie.png"),
                new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)),
                0.8f, 1.6f, 0.8f,
                new float[]{0.3f, 0.9f, 0.5f, 0.7f});
    }

    /** 灾厄幻影：小型飞行体，暗绿色 */
    public static CalamityBaseRenderer createPhantom(EntityRendererFactory.Context ctx) {
        return new CalamityBaseRenderer(ctx,
                new Identifier("textures/entity/phantom.png"),
                new net.minecraft.client.render.entity.model.PhantomEntityModel<>(
                        ctx.getPart(EntityModelLayers.PHANTOM)),
                0.8f, 0.8f, 0.8f,
                new float[]{0.1f, 0.5f, 0.1f});
    }

    /** 灾厄之风：大型人形，暗红色（首领） */
    public static CalamityBaseRenderer createWind(EntityRendererFactory.Context ctx) {
        return new CalamityBaseRenderer(ctx,
                new Identifier("textures/entity/warden/warden.png"),
                new net.minecraft.client.render.entity.model.WardenEntityModel<>(
                        ctx.getPart(EntityModelLayers.WARDEN)),
                1.2f, 1.2f, 1.2f,
                new float[]{0.7f, 0.1f, 0.1f});
    }
}
