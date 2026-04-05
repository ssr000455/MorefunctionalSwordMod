package com.qidai.morefunctionalswordmod.network;

import com.qidai.morefunctionalswordmod.RainbowSwordItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class ServerSyncHandler {
    public static final Identifier SYNC_NBT_ID = new Identifier("mfswordmod", "sync_rainbow_nbt");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SYNC_NBT_ID, (server, player, handler, buf, responseSender) -> {
            NbtCompound nbt = buf.readNbt();
            server.execute(() -> {
                var stack = player.getMainHandStack();
                if (stack.getItem() instanceof RainbowSwordItem && nbt != null) {
                    stack.setNbt(nbt);
                    // 可选：同步给其他玩家（如果需要）
                }
            });
        });
    }
}
