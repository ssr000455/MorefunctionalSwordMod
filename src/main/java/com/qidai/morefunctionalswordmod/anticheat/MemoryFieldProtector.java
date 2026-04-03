package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.ConcurrentHashMap;

public class MemoryFieldProtector {
    private static final ConcurrentHashMap<String, Float> lastHealthCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> lastChecksumCache = new ConcurrentHashMap<>();

    public static boolean checkHealthAbnormal(ServerPlayerEntity player) {
        float currentHealth = player.getHealth();
        String uuid = player.getUuid().toString();
        Float lastHealth = lastHealthCache.get(uuid);
        if (lastHealth != null && lastHealth > 10 && currentHealth <= 0) {
            player.sendMessage(Text.literal("⚠ 检测到异常生命值修改！已自动恢复。").formatted(Formatting.RED), false);
            player.setHealth(lastHealth);
            return false;
        }
        lastHealthCache.put(uuid, currentHealth);
        return true;
    }

    public static boolean checkNbtAbnormal(ServerPlayerEntity player, ItemStack stack) {
        if (!(stack.getItem() instanceof com.qidai.morefunctionalswordmod.RainbowSwordItem)) return true;
        NbtCompound nbt = stack.getOrCreateNbt();
        String uuid = player.getUuid().toString();
        long currentHash = 0;
        currentHash += nbt.getBoolean("HasContract") ? 1 : 0;
        currentHash += nbt.getInt("AttackMode") * 31;
        currentHash += nbt.getFloat("BaseDamage") * 131;
        Long lastHash = lastChecksumCache.get(uuid);
        if (lastHash != null && Math.abs(currentHash - lastHash) > 10000) {
            player.sendMessage(Text.literal("⚠ 检测到 NBT 异常修改！已回滚。").formatted(Formatting.RED), false);
            com.qidai.morefunctionalswordmod.RainbowSwordHelper.rollback(player, stack);
            return false;
        }
        lastChecksumCache.put(uuid, currentHash);
        return true;
    }
}
