package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import com.qidai.morefunctionalswordmod.ModRegistry;
import com.qidai.morefunctionalswordmod.RainbowSwordItem;
import com.qidai.morefunctionalswordmod.ModTools;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class AntiCheatManager {
    private static final AntiCheatManager INSTANCE = new AntiCheatManager();
    private final ConcurrentHashMap<UUID, AntiCheatData> playerData = new ConcurrentHashMap<>();
    private boolean enabled = true;

    public static AntiCheatManager getInstance() { return INSTANCE; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }

    public AntiCheatData getData(ServerPlayerEntity player) {
        return playerData.computeIfAbsent(player.getUuid(), uuid -> new AntiCheatData(player));
    }

    public void removePlayer(ServerPlayerEntity player) {
        playerData.remove(player.getUuid());
    }

    /**
     * 判断玩家是否持有本模组超强物品（豁免大部分检测）
     */
    public boolean isExempt(ServerPlayerEntity player) {
        for (ItemStack stack : player.getInventory().main) {
            if (stack.isEmpty()) continue;
            // 七彩神剑契约状态
            if (stack.getItem() instanceof RainbowSwordItem && 
                stack.getOrCreateNbt().getBoolean("HasContract")) {
                return true;
            }
            // 彩虹宝石全套任意一件（超强）
            if (stack.getItem() == ModTools.RAINBOW_GEM_SWORD ||
                stack.getItem() == ModTools.RAINBOW_GEM_PICKAXE ||
                stack.getItem() == ModTools.RAINBOW_GEM_AXE ||
                stack.getItem() == ModTools.RAINBOW_GEM_SHOVEL ||
                stack.getItem() == ModTools.RAINBOW_GEM_HOE ||
                stack.getItem() == ModTools.RAINBOW_GEM_BOW ||
                stack.getItem() == ModTools.RAINBOW_GEM_HELMET ||
                stack.getItem() == ModTools.RAINBOW_GEM_CHESTPLATE ||
                stack.getItem() == ModTools.RAINBOW_GEM_LEGGINGS ||
                stack.getItem() == ModTools.RAINBOW_GEM_BOOTS) {
                return true;
            }
            // 超级粉钻剑
            if (stack.getItem() == ModTools.ULTRA_PINK_DIAMOND_SWORD) {
                return true;
            }
            // 彩虹神剑（未契约但本身也很强，可选豁免）
            if (stack.getItem() == ModTools.RAINBOW_SWORD) {
                return true;
            }
        }
        return false;
    }

    /**
     * 踢出玩家并记录原因
     */
    public void kickPlayer(ServerPlayerEntity player, String reason) {
        if (player == null || player.networkHandler == null) return;
        player.networkHandler.disconnect(Text.literal("反作弊器检测到违规: " + reason).formatted(Formatting.RED));
        removePlayer(player);
    }

    public AntiCheatData getData(UUID uuid) {
        return playerData.get(uuid);
    }

    public static class AntiCheatData {
        public final ServerPlayerEntity player;
        public long lastTickTime;
        public double lastX, lastY, lastZ;
        public float lastYaw, lastPitch;
        public int attackCount;
        public long lastAttackTime;
        public int breakCount;
        public long lastBreakTime;
        public double totalMovement;
        public int invalidMoveCount;
        public int invalidAttackCount;
        public int invalidBreakCount;

        public AntiCheatData(ServerPlayerEntity player) {
            this.player = player;
            reset();
        }

        public void reset() {
            lastTickTime = System.currentTimeMillis();
            lastX = player.getX();
            lastY = player.getY();
            lastZ = player.getZ();
            lastYaw = player.getYaw();
            lastPitch = player.getPitch();
            attackCount = 0;
            lastAttackTime = 0;
            breakCount = 0;
            lastBreakTime = 0;
            totalMovement = 0;
            invalidMoveCount = 0;
            invalidAttackCount = 0;
            invalidBreakCount = 0;
        }
    }
}
