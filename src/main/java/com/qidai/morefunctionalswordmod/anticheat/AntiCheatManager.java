package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import com.qidai.morefunctionalswordmod.ModRegistry;
import com.qidai.morefunctionalswordmod.RainbowSwordItem;
import com.qidai.morefunctionalswordmod.ModTools;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class AntiCheatManager {
    private static final AntiCheatManager INSTANCE = new AntiCheatManager();
    private final ConcurrentHashMap<UUID, AntiCheatData> playerData = new ConcurrentHashMap<>();
    private boolean enabled = true;
    private boolean strictMode = false;

    public static AntiCheatManager getInstance() { return INSTANCE; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }

    public void setStrictMode(boolean strict) { this.strictMode = strict; }
    public boolean isStrictMode() { return strictMode; }

    public AntiCheatData getData(ServerPlayerEntity player) {
        return playerData.computeIfAbsent(player.getUuid(), uuid -> new AntiCheatData(player));
    }

    public void removePlayer(ServerPlayerEntity player) {
        playerData.remove(player.getUuid());
    }

    // 检查是否豁免检测
    // 优先检查主副手和快捷栏，减少全背包扫描
    public boolean isExempt(ServerPlayerEntity player) {
        // 检查主手
        if (hasExemptItem(player.getMainHandStack())) return true;
        // 检查副手
        if (hasExemptItem(player.getOffHandStack())) return true;
        // 只扫描背包，不重复检查主副手
        for (ItemStack stack : player.getInventory().main) {
            if (hasExemptItem(stack)) return true;
        }
        return false;
    }

    private boolean hasExemptItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof RainbowSwordItem) return true;
        return stack.getItem() == ModTools.ULTRA_PINK_DIAMOND_SWORD ||
               stack.getItem() == ModTools.RAINBOW_GEM_SWORD ||
               stack.getItem() == ModTools.RAINBOW_GEM_CHESTPLATE ||
               stack.getItem() == ModTools.RAINBOW_GEM_HELMET ||
               stack.getItem() == ModTools.RAINBOW_GEM_LEGGINGS ||
               stack.getItem() == ModTools.RAINBOW_GEM_BOOTS;
    }

    // 累积违规分
    public void addViolation(ServerPlayerEntity player) {
        getData(player).addViolation();
    }

    // 踢出玩家
    public void kickPlayer(ServerPlayerEntity player, String reason) {
        player.networkHandler.disconnect(Text.literal("反作弊器检测到异常: " + reason));
        removePlayer(player);
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
        public int violationLevel;
        public float lastHealth;
        public long lastHealthCheckTime;

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
            violationLevel = 0;
            lastHealth = player.getHealth();
            lastHealthCheckTime = System.currentTimeMillis();
        }

        public void addViolation() {
            violationLevel++;
            if (violationLevel >= 5 && AntiCheatManager.getInstance().isStrictMode()) {
                player.networkHandler.disconnect(Text.literal("累计违规次数过多"));
                AntiCheatManager.getInstance().removePlayer(player);
            }
        }
    }
}
