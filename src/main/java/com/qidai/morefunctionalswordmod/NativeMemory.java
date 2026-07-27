package com.qidai.morefunctionalswordmod;

import net.minecraft.entity.player.PlayerEntity;

public class NativeMemory {
    static {
        // 由 NativeLoader 负责加载，这里不需要重复加载
    }

    public static native float getPlayerHealth(PlayerEntity player);
    public static native void setPlayerPosition(PlayerEntity player, double x, double y, double z);
    public static native double[] getPlayerPosition(PlayerEntity player);
}