package com.qidai.morefunctionalswordmod;

import com.qidai.morefunctionalswordmod.entity.*;
import com.qidai.morefunctionalswordmod.entity.calamity.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    // 原有彩虹实体
    public static final EntityType<RainbowGuardianEntity> RAINBOW_GUARDIAN = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(ModRegistry.MOD_ID, "rainbow_guardian"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RainbowGuardianEntity::new)
            .dimensions(EntityDimensions.fixed(0.9f, 2.9f))
            .trackRangeBlocks(64)
            .trackedUpdateRate(2)
            .build()
    );

    public static final EntityType<DepletedRainbowEntity> DEPLETED_RAINBOW = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(ModRegistry.MOD_ID, "depleted_rainbow"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DepletedRainbowEntity::new)
            .dimensions(EntityDimensions.fixed(0.7f, 2.0f))
            .trackRangeBlocks(48)
            .trackedUpdateRate(3)
            .build()
    );

    public static final EntityType<RainbowGhostEntity> RAINBOW_GHOST = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(ModRegistry.MOD_ID, "rainbow_ghost"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RainbowGhostEntity::new)
            .dimensions(EntityDimensions.fixed(0.9f, 0.5f))
            .trackRangeBlocks(64)
            .trackedUpdateRate(2)
            .build()
    );

    // ========== 灾厄系列实体 ==========
    public static final EntityType<CalamityWraith> CALAMITY_WRAITH = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(ModRegistry.MOD_ID, "calamity_wraith"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, CalamityWraith::new)
            .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
            .trackRangeBlocks(64)
            .trackedUpdateRate(3)
            .build()
    );

    public static final EntityType<CalamityPhantom> CALAMITY_PHANTOM = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(ModRegistry.MOD_ID, "calamity_phantom"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, CalamityPhantom::new)
            .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
            .trackRangeBlocks(64)
            .trackedUpdateRate(3)
            .build()
    );

    public static final EntityType<CalamityWind> CALAMITY_WIND = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(ModRegistry.MOD_ID, "calamity_wind"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, CalamityWind::new)
            .dimensions(EntityDimensions.fixed(0.9f, 2.7f))
            .trackRangeBlocks(80)
            .trackedUpdateRate(2)
            .build()
    );

    public static final EntityType<CalamitySoldier> CALAMITY_SOLDIER = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(ModRegistry.MOD_ID, "calamity_soldier"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, CalamitySoldier::new)
            .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
            .trackRangeBlocks(64)
            .trackedUpdateRate(3)
            .build()
    );

    // ========== 衰败彩虹水怪 ==========
    public static final EntityType<DecayedRainbowMonsterEntity> DECAYED_RAINBOW_MONSTER = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(ModRegistry.MOD_ID, "decayed_rainbow_monster"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DecayedRainbowMonsterEntity::new)
            .dimensions(EntityDimensions.fixed(0.9f, 2.4f))
            .trackRangeBlocks(64)
            .trackedUpdateRate(2)
            .build()
    );

    public static void registerEntities() {
        // 此方法仅为占位，实际注册已在静态块完成
    }
}
