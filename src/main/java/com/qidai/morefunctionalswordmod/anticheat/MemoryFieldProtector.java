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
    private static final ConcurrentHashMap<String, Integer> lastFoodCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> lastXpCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Float> lastMaxHealthCache = new ConcurrentHashMap<>();

    // 健康检测
    public static boolean checkHealthAbnormal(ServerPlayerEntity player) {
        float currentHealth = player.getHealth();
        float currentMaxHealth = player.getMaxHealth();
        String uuid = player.getUuid().toString();

        // 检测最大生命值突变
        Float lastMaxHealth = lastMaxHealthCache.get(uuid);
        if (lastMaxHealth != null && Math.abs(currentMaxHealth - lastMaxHealth) > 40 && currentMaxHealth > 0) {
            player.sendMessage(Text.literal("⚠ 检测到最大生命值异常修改！已自动恢复。").formatted(Formatting.RED), false);
            player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(lastMaxHealth);
            return false;
        }
        lastMaxHealthCache.put(uuid, currentMaxHealth);

        // 检测生命值瞬变
        Float lastHealth = lastHealthCache.get(uuid);
        if (lastHealth != null && lastHealth > 10 && currentHealth <= 0) {
            player.sendMessage(Text.literal("⚠ 检测到异常生命值修改！已自动恢复。").formatted(Formatting.RED), false);
            player.setHealth(lastHealth);
            return false;
        }
        // 检测生命值异常上涨（1tick内从残血回满）
        if (lastHealth != null && currentHealth - lastHealth > 20 && currentHealth > 20) {
            player.sendMessage(Text.literal("⚠ 检测到生命值异常快速恢复！").formatted(Formatting.YELLOW), false);
            AntiCheatManager.getInstance().addViolation(player);
        }
        lastHealthCache.put(uuid, currentHealth);
        return true;
    }

    // NBT 异常检测
    public static boolean checkNbtAbnormal(ServerPlayerEntity player, ItemStack stack) {
        if (!(stack.getItem() instanceof com.qidai.morefunctionalswordmod.RainbowSwordItem)) return true;
        NbtCompound nbt = stack.getOrCreateNbt();
        String uuid = player.getUuid().toString();
        long currentHash = 0;
        currentHash += nbt.getBoolean("HasContract") ? 1 : 0;
        currentHash += nbt.getInt("AttackMode") * 31;
        currentHash += nbt.getFloat("BaseDamage") * 131;
        currentHash += nbt.getInt("AttackRange") * 7;
        currentHash += nbt.getBoolean("ModifyNbt") ? 17 : 0;
        currentHash += nbt.getBoolean("RemoveEntity") ? 19 : 0;
        Long lastHash = lastChecksumCache.get(uuid);
        if (lastHash != null && Math.abs(currentHash - lastHash) > 10000) {
            player.sendMessage(Text.literal("⚠ 检测到 NBT 异常修改！已回滚。").formatted(Formatting.RED), false);
            com.qidai.morefunctionalswordmod.RainbowSwordHelper.rollback(player, stack);
            return false;
        }
        lastChecksumCache.put(uuid, currentHash);
        return true;
    }

    // 饥饿值检测
    public static void checkFoodAbnormal(ServerPlayerEntity player) {
        String uuid = player.getUuid().toString();
        int currentFood = player.getHungerManager().getFoodLevel();
        Integer lastFood = lastFoodCache.get(uuid);
        if (lastFood != null && currentFood - lastFood > 10) {
            player.sendMessage(Text.literal("⚠ 检测到饥饿值异常修改！").formatted(Formatting.YELLOW), false);
            player.getHungerManager().setFoodLevel(lastFood);
        }
        lastFoodCache.put(uuid, currentFood);
    }

    // 经验值检测
    public static void checkXpAbnormal(ServerPlayerEntity player) {
        String uuid = player.getUuid().toString();
        int currentXp = player.experienceLevel;
        Integer lastXp = lastXpCache.get(uuid);
        if (lastXp != null && currentXp - lastXp > 50) {
            player.sendMessage(Text.literal("⚠ 检测到经验值异常修改！").formatted(Formatting.YELLOW), false);
            AntiCheatManager.getInstance().addViolation(player);
        }
        lastXpCache.put(uuid, currentXp);
    }
}
