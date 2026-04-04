package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryScanner {
    private static final Map<UUID, PlayerSnapshot> snapshots = new HashMap<>();

    public static boolean scan(ServerPlayerEntity player) {
        if (!AntiCheatManager.getInstance().isEnabled()) return true;
        if (AntiCheatManager.getInstance().isExempt(player)) return true;

        UUID uuid = player.getUuid();
        PlayerSnapshot current = new PlayerSnapshot(player);
        PlayerSnapshot last = snapshots.get(uuid);

        if (last == null) {
            snapshots.put(uuid, current);
            return true;
        }

        long now = System.currentTimeMillis();
        long timeDelta = now - (snapshots.get(uuid) != null ? snapshots.get(uuid).timestamp : now);
        
        // 生命值异常检测（排除正常变化）
        // 正常变化范围：受伤最多20点/次，回血最多2点/秒，吃东西最多8点
        float healthDelta = Math.abs(current.health - last.health);
        boolean isHealing = current.health > last.health;
        boolean isHurt = current.health < last.health;
        
        // 瞬间变化超过20点（不是正常受伤或回血）
        if (healthDelta > 20.0f && !player.isDead()) {
            punish(player, "生命值异常变化: " + healthDelta + " 点");
            return false;
        }
        
        // 瞬间回血超过10点（没有吃东西或药水的合理范围）
        if (isHealing && healthDelta > 10.0f && timeDelta < 1000) {
            punish(player, "生命值异常恢复: +" + healthDelta + " 点");
            return false;
        }

        // 最大生命值异常检测
        if (Math.abs(current.maxHealth - last.maxHealth) > 0.1) {
            punish(player, "最大生命值被篡改: " + current.maxHealth);
            return false;
        }

        // 移动速度异常检测（属性修改）
        if (Math.abs(current.movementSpeed - last.movementSpeed) > 0.5) {
            punish(player, "移动速度属性被非法修改: " + current.movementSpeed);
            return false;
        }

        // 飞行能力检测（非创造模式下允许飞行）
        if (current.allowFlying && !player.getAbilities().creativeMode) {
            if (!last.allowFlying) {
                punish(player, "非法开启飞行能力");
                return false;
            }
        }

        // 物品堆叠异常（背包内物品数量超出127）
        for (ItemStack stack : player.getInventory().main) {
            if (!stack.isEmpty() && stack.getCount() > 127) {
                punish(player, "物品堆叠数量异常: " + stack.getCount() + "x " + stack.getItem().getName().getString());
                return false;
            }
        }

        // 药水效果异常（等级>255或无限时长且无来源）
        for (StatusEffectInstance effect : player.getActiveStatusEffects().values()) {
            if (effect.getAmplifier() > 255) {
                punish(player, "药水效果等级异常: " + effect.getAmplifier());
                return false;
            }
            if (effect.getDuration() > 1000000 && effect.getDuration() < Integer.MAX_VALUE) {
                punish(player, "药水效果持续时间异常: " + effect.getDuration());
                return false;
            }
        }

        snapshots.put(uuid, current);
        return true;
    }

    private static void punish(ServerPlayerEntity player, String reason) {
        player.networkHandler.disconnect(net.minecraft.text.Text.literal("内存扫描器检测到异常: " + reason));
        AntiCheatManager.getInstance().removePlayer(player);
    }

    private static class PlayerSnapshot {
        float health;
        double maxHealth;
        double movementSpeed;
        boolean allowFlying;
        boolean creativeMode;
        long timestamp;

        PlayerSnapshot(ServerPlayerEntity player) {
            this.health = player.getHealth();
            this.maxHealth = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).getValue();
            this.movementSpeed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getValue();
            this.allowFlying = player.getAbilities().allowFlying;
            this.creativeMode = player.getAbilities().creativeMode;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
