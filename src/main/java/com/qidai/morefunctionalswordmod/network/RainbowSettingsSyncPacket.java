package com.qidai.morefunctionalswordmod.network;

import com.qidai.morefunctionalswordmod.RainbowSwordItem;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class RainbowSettingsSyncPacket {
    public static final Identifier ID = new Identifier("mfswordmod", "rainbow_settings_sync");

    public static PacketByteBuf write(NbtCompound nbt) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeNbt(nbt);
        return buf;
    }

    public static void handle(ServerPlayerEntity player, NbtCompound nbt) {
        if (nbt != null && player.getMainHandStack().getItem() instanceof RainbowSwordItem) {
            player.getMainHandStack().setNbt(nbt);
        }
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            NbtCompound nbt = buf.readNbt();
            server.execute(() -> handle(player, nbt));
        });
    }
}
