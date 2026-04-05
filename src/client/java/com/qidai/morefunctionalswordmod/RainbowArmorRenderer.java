package com.qidai.morefunctionalswordmod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RainbowArmorRenderer {
    private static final Identifier RAINBOW_ARMOR_TEXTURE = new Identifier(ModRegistry.MOD_ID, "textures/models/armor/rainbow_gem_layer_1.png");
    private static final Identifier RAINBOW_ARMOR_TEXTURE_LEGGINGS = new Identifier(ModRegistry.MOD_ID, "textures/models/armor/rainbow_gem_layer_2.png");

    private static BipedEntityModel<LivingEntity> innerArmorModel;
    private static BipedEntityModel<LivingEntity> outerArmorModel;

    public static void render(LivingEntity entity, ItemStack stack, EquipmentSlot slot, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!(stack.getItem() instanceof ArmorItem)) return;

        if (innerArmorModel == null) {
            innerArmorModel = new BipedEntityModel<>(
                net.minecraft.client.MinecraftClient.getInstance().getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER_INNER_ARMOR)
            );
        }
        if (outerArmorModel == null) {
            outerArmorModel = new BipedEntityModel<>(
                net.minecraft.client.MinecraftClient.getInstance().getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER_OUTER_ARMOR)
            );
        }

        BipedEntityModel<LivingEntity> armorModel = slot == EquipmentSlot.LEGS ? outerArmorModel : innerArmorModel;
        armorModel.setAngles(entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), entity.age, entity.headYaw, entity.getPitch());

        Identifier texture = slot == EquipmentSlot.LEGS ? RAINBOW_ARMOR_TEXTURE_LEGGINGS : RAINBOW_ARMOR_TEXTURE;
        
        // 基础层
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(texture));
        armorModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
        
        // 发光层（泛光）
        float time = (System.currentTimeMillis() % 10000) / 10000.0f * (float)Math.PI * 2;
        float glowR = (float)(Math.sin(time) * 0.5 + 0.5);
        float glowG = (float)(Math.sin(time + 2.094) * 0.5 + 0.5);
        float glowB = (float)(Math.sin(time + 4.188) * 0.5 + 0.5);
        
        VertexConsumer glowConsumer = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(texture));
        armorModel.render(matrices, glowConsumer, 0xF000F0, OverlayTexture.DEFAULT_UV, glowR, glowG, glowB, 0.3f);
    }
}
