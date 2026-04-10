package com.qidai.morefunctionalswordmod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RainbowSwordHelper {
    private static final Map<PlayerEntity, NbtCompound> BACKUP = new ConcurrentHashMap<>();

    public static String signature(PlayerEntity player, NbtCompound nbt) {
        try {
            var data = player.getUuid().toString() +
                       nbt.getBoolean("HasContract") +
                       nbt.getInt("AttackMode") +
                       nbt.getFloat("BaseDamage") +
                       nbt.getInt("AttackRange");
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean verify(PlayerEntity player, ItemStack stack) {
        if (!(stack.getItem() instanceof RainbowSwordItem)) return true;
        var nbt = stack.getOrCreateNbt();
        var stored = nbt.getString("Signature");
        return stored.isEmpty() || stored.equals(signature(player, nbt));
    }

    public static void update(PlayerEntity player, ItemStack stack) {
        if (stack.getItem() instanceof RainbowSwordItem) {
            var nbt = stack.getOrCreateNbt();
            nbt.putString("Signature", signature(player, nbt));
        }
    }

    public static void backup(ServerPlayerEntity player, ItemStack stack) {
        if (stack.getItem() instanceof RainbowSwordItem) {
            BACKUP.put(player, stack.getOrCreateNbt().copy());
        }
    }

    public static boolean rollback(ServerPlayerEntity player, ItemStack stack) {
        var backup = BACKUP.get(player);
        if (backup != null && stack.getItem() instanceof RainbowSwordItem) {
            stack.setNbt(backup.copy());
            player.sendMessage(Text.literal("数据已恢复").formatted(Formatting.GREEN), false);
            return true;
        }
        return false;
    }

    public static void clear(ServerPlayerEntity player) {
        BACKUP.remove(player);
    }
}
