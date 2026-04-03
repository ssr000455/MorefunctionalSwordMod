package com.qidai.morefunctionalswordmod.anticheat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;

public class PotionCheck {
    private static final int MAX_AMPLIFIER = 255;

    public static boolean check(ServerPlayerEntity player) {
        if (AntiCheatManager.getInstance().isExempt(player)) return true;

        for (StatusEffectInstance effect : player.getActiveStatusEffects().values()) {
            int amplifier = effect.getAmplifier();
            if (amplifier > MAX_AMPLIFIER) {
                AntiCheatManager.getInstance().kickPlayer(player, 
                    "药水效果等级异常: " + Registries.STATUS_EFFECT.getId(effect.getEffectType()) + " level " + amplifier);
                return false;
            }
            if (!effect.isInfinite() && effect.getDuration() > 72000 && amplifier < 10) {
                AntiCheatManager.getInstance().kickPlayer(player, 
                    "药水效果持续时间异常: " + effect.getDuration() + " ticks");
                return false;
            }
        }
        return true;
    }
}
