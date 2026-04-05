package com.qidai.morefunctionalswordmod;

import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class RainbowDimension {
    public static final String MOD_ID = ModRegistry.MOD_ID;
    
    // 维度ID
    public static final Identifier RAINBOW_SEA_ID = new Identifier(MOD_ID, "rainbow_sea");
    public static final RegistryKey<World> RAINBOW_SEA_WORLD = RegistryKey.of(RegistryKeys.WORLD, RAINBOW_SEA_ID);
    public static final RegistryKey<DimensionType> RAINBOW_SEA_DIMENSION_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, RAINBOW_SEA_ID);
    
    // 传送门方块位置
    public static class PortalPosition {
        public int x, y, z;
        public PortalPosition(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
