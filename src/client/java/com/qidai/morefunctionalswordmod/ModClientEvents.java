package com.qidai.morefunctionalswordmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;

public class ModClientEvents implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 彩虹盔甲渲染（保持不变）
        ArmorRenderer.register((matrices, vertexConsumers, stack, entity, slot, light, model) -> {
            RainbowArmorRenderer.render(entity, stack, slot, matrices, vertexConsumers, light);
        }, ModTools.RAINBOW_GEM_HELMET, ModTools.RAINBOW_GEM_CHESTPLATE,
           ModTools.RAINBOW_GEM_LEGGINGS, ModTools.RAINBOW_GEM_BOOTS);
        // 物品（包括彩虹剑）使用原版渲染，不需要任何注册
    }
}
