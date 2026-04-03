package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.management.ManagementFactory;

public class AntiCheatProtector {
    public static boolean detectDebugger() {
        String vmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments().toString();
        return vmArgs.contains("-agentlib:jdwp") || vmArgs.contains("-Xdebug");
    }

    public static boolean detectCheatMods() {
        String[] cheatClasses = {
            "net.ccbluex.liquidbounce.LiquidBounce",
            "net.wurstclient.WurstClient",
            "net.impactclient.ImpactClient",
            "net.arikia.dev.drpc.DiscordRPC",  // 常见作弊辅助
            "net.minecraftforge.fml.common.Loader"  // Forge 可能冲突
        };
        for (String className : cheatClasses) {
            try {
                Class.forName(className);
                return true;
            } catch (ClassNotFoundException ignored) {}
        }
        return false;
    }

    /**
     * 检查玩家是否允许作弊（豁免检测）
     */
    private static boolean isExempt(ServerPlayerEntity player) {
        return AntiCheatManager.getInstance().isExempt(player);
    }

    public static void scanAndPunish(ServerPlayerEntity player) {
        if (isExempt(player)) return;

        if (detectDebugger()) {
            punish(player, "检测到调试器，可能存在作弊行为");
        } else if (detectCheatMods()) {
            punish(player, "检测到作弊模组，请移除后重新加入");
        }
    }

    private static void punish(ServerPlayerEntity player, String reason) {
        player.networkHandler.disconnect(Text.literal("反作弊器: " + reason).formatted(Formatting.RED));
        AntiCheatManager.getInstance().removePlayer(player);
    }
}
