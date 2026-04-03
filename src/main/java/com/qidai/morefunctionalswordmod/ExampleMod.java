package com.qidai.morefunctionalswordmod;

import com.qidai.morefunctionalswordmod.entity.*;
import com.qidai.morefunctionalswordmod.entity.calamity.*;
import com.qidai.morefunctionalswordmod.world.gen.CalamitySpawner;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("mfswordmod");

    @Override
    public void onInitialize() {
        ModRegistry.registerModItems();
        ModTools.registerModTools();
        ModItemGroups.registerItemGroups();
        ModEvents.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ModCommands.register(dispatcher);
        });

        ModEntities.registerEntities();
        registerEntityAttributes();

        UltraSwordDamageHandler.register();

        // 启用灾厄怪物生成
        CalamitySpawner.register();

        LOGGER.info("更多扩展 Mod 初始化完成");
    }

    private void registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(ModEntities.RAINBOW_GUARDIAN, RainbowGuardianEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.DEPLETED_RAINBOW, DepletedRainbowEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.RAINBOW_GHOST, RainbowGhostEntity.createAttributes());

        FabricDefaultAttributeRegistry.register(ModEntities.CALAMITY_WRAITH, CalamityWraith.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CALAMITY_PHANTOM, CalamityPhantom.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CALAMITY_WIND, CalamityWind.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CALAMITY_SOLDIER, CalamitySoldier.createAttributes());

        FabricDefaultAttributeRegistry.register(ModEntities.DECAYED_RAINBOW_MONSTER, DecayedRainbowMonsterEntity.createAttributes());

        LOGGER.info("实体属性注册完成");
    }
}
