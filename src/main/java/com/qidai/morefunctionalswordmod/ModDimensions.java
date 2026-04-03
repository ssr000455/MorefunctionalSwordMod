package com.qidai.morefunctionalswordmod;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;

public class ModDimensions {
    public static final Identifier RAINBOW_SEA_ID = new Identifier(ModRegistry.MOD_ID, "rainbow_sea");
    public static final RegistryKey<DimensionOptions> RAINBOW_SEA_KEY = RegistryKey.of(RegistryKeys.DIMENSION, RAINBOW_SEA_ID);
    public static final RegistryKey<World> RAINBOW_SEA_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD, RAINBOW_SEA_ID);
    public static final RegistryKey<DimensionType> RAINBOW_SEA_TYPE_KEY = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, RAINBOW_SEA_ID);
}
