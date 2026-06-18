package com.qidai.morefunctionalswordmod.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class ServerSyncHandler {
    // 旧 ID，保留以兼容旧的网络包
    public static final Identifier SYNC_NBT_ID = new Identifier("mfswordmod", "sync_rainbow_nbt");

    public static void register() {
        // 此处理器已合并到 RainbowSettingsSyncPacket，此处仅注册旧 ID 做兼容转发
        ServerPlayNetworking.registerGlobalReceiver(SYNC_NBT_ID, (server, player, handler, buf, sender) -> {
            var nbt = buf.readNbt();
            if (nbt != null) {
                RainbowSettingsSyncPacket.handle(player, nbt);
            }
        });
    }
}
