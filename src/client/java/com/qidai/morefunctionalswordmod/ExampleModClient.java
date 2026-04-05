package com.qidai.morefunctionalswordmod;

import com.qidai.morefunctionalswordmod.network.ClientUIPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.function.Function;

public class ExampleModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModKeyBindings.register();
        ModEntityRenderers.register();
        ClientUIPacketHandler.register();
        registerFluidColors();
    }

    private void registerFluidColors() {
        // 获取 Sprite 的 Function
        Function<Identifier, Sprite> spriteGetter = 
            MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        
        // 彩虹毒水的 Sprite
        Identifier poisonStillId = new Identifier(ModRegistry.MOD_ID, "block/rainbow_poison_water_still");
        Identifier poisonFlowId = new Identifier(ModRegistry.MOD_ID, "block/rainbow_poison_water_flow");
        Sprite[] poisonSprites = new Sprite[]{
            spriteGetter.apply(poisonStillId),
            spriteGetter.apply(poisonFlowId)
        };
        
        // 彩虹海水的 Sprite
        Identifier seaStillId = new Identifier(ModRegistry.MOD_ID, "block/rainbow_sea_water_still");
        Identifier seaFlowId = new Identifier(ModRegistry.MOD_ID, "block/rainbow_sea_water_flow");
        Sprite[] seaSprites = new Sprite[]{
            spriteGetter.apply(seaStillId),
            spriteGetter.apply(seaFlowId)
        };
        
        // 衰败彩虹水的 Sprite
        Identifier decayedStillId = new Identifier(ModRegistry.MOD_ID, "block/decayed_rainbow_water_still");
        Identifier decayedFlowId = new Identifier(ModRegistry.MOD_ID, "block/decayed_rainbow_water_flow");
        Sprite[] decayedSprites = new Sprite[]{
            spriteGetter.apply(decayedStillId),
            spriteGetter.apply(decayedFlowId)
        };
        
        // 彩虹毒水
        FluidRenderHandlerRegistry.INSTANCE.register(ModRegistry.STILL_RAINBOW_POISON_WATER,
            ModRegistry.FLOWING_RAINBOW_POISON_WATER, new FluidRenderHandler() {
                @Override
                public int getFluidColor(BlockRenderView view, BlockPos pos, FluidState state) {
                    return 0xFF88FF44;  // ARGB格式，不透明明亮黄绿色
                }
                
                @Override
                public Sprite[] getFluidSprites(BlockRenderView view, BlockPos pos, FluidState state) {
                    return poisonSprites;
                }
            }
        );
        
        // 彩虹海水
        FluidRenderHandlerRegistry.INSTANCE.register(ModRegistry.STILL_RAINBOW_SEA_WATER,
            ModRegistry.FLOWING_RAINBOW_SEA_WATER, new FluidRenderHandler() {
                @Override
                public int getFluidColor(BlockRenderView view, BlockPos pos, FluidState state) {
                    return 0xFF33CCFF;  // 不透明天蓝色
                }
                
                @Override
                public Sprite[] getFluidSprites(BlockRenderView view, BlockPos pos, FluidState state) {
                    return seaSprites;
                }
            }
        );
        
        // 衰败彩虹水
        FluidRenderHandlerRegistry.INSTANCE.register(ModRegistry.STILL_DECAYED_RAINBOW_WATER,
            ModRegistry.FLOWING_DECAYED_RAINBOW_WATER, new FluidRenderHandler() {
                @Override
                public int getFluidColor(BlockRenderView view, BlockPos pos, FluidState state) {
                    return 0xFFAA66CC;  // 不透明紫罗兰色
                }
                
                @Override
                public Sprite[] getFluidSprites(BlockRenderView view, BlockPos pos, FluidState state) {
                    return decayedSprites;
                }
            }
        );
    }
}
