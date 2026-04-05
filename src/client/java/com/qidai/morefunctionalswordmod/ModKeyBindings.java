package com.qidai.morefunctionalswordmod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static KeyBinding modeSwitchKey;
    public static KeyBinding specialModeKey;
    public static KeyBinding clearModeKey;
    public static KeyBinding freezeModeKey;
    public static KeyBinding healModeKey;
    public static KeyBinding swordWaveKey;

    public static void register() {
        modeSwitchKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mfswordmod.mode_switch",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category.mfswordmod.general"
        ));

        specialModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mfswordmod.special_mode",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "category.mfswordmod.general"
        ));

        clearModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mfswordmod.clear_mode",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            "category.mfswordmod.general"
        ));

        freezeModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mfswordmod.freeze_mode",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "category.mfswordmod.general"
        ));

        healModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mfswordmod.heal_mode",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            "category.mfswordmod.general"
        ));

        swordWaveKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mfswordmod.sword_wave",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "category.mfswordmod.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.currentScreen != null) return;

            while (modeSwitchKey.wasPressed()) {
                if (client.player.getMainHandStack().getItem() instanceof RainbowSwordItem) {
                    NbtCompound nbt = client.player.getMainHandStack().getOrCreateNbt();
                    boolean current = nbt.getBoolean("DestructMode");
                    nbt.putBoolean("DestructMode", !current);
                    client.player.sendMessage(Text.literal("毁灭模式: " + (!current ? "开启" : "关闭"))
                        .formatted(!current ? Formatting.AQUA : Formatting.GRAY), false);
                }
            }

            while (specialModeKey.wasPressed()) {
                if (client.player.getMainHandStack().getItem() instanceof RainbowSwordItem) {
                    NbtCompound nbt = client.player.getMainHandStack().getOrCreateNbt();
                    boolean current = nbt.getBoolean("SpecialAttackMode");
                    nbt.putBoolean("SpecialAttackMode", !current);
                    client.player.sendMessage(Text.literal("特殊攻击模式: " + (!current ? "开启" : "关闭"))
                        .formatted(!current ? Formatting.LIGHT_PURPLE : Formatting.GRAY), false);
                }
            }

            while (clearModeKey.wasPressed()) {
                if (client.player.getMainHandStack().getItem() instanceof RainbowSwordItem) {
                    NbtCompound nbt = client.player.getMainHandStack().getOrCreateNbt();
                    nbt.putBoolean("DestructMode", false);
                    nbt.putBoolean("SpecialAttackMode", false);
                    client.player.sendMessage(Text.literal("所有模式已关闭").formatted(Formatting.GRAY), false);
                }
            }

            while (freezeModeKey.wasPressed()) {
                if (client.player.getMainHandStack().getItem() instanceof RainbowSwordItem) {
                    NbtCompound nbt = client.player.getMainHandStack().getOrCreateNbt();
                    boolean current = nbt.getBoolean("FreezeMode");
                    nbt.putBoolean("FreezeMode", !current);
                    client.player.sendMessage(Text.literal("冰冻模式: " + (!current ? "开启" : "关闭"))
                        .formatted(!current ? Formatting.AQUA : Formatting.GRAY), false);
                }
            }

            while (healModeKey.wasPressed()) {
                if (client.player.getMainHandStack().getItem() instanceof RainbowSwordItem) {
                    NbtCompound nbt = client.player.getMainHandStack().getOrCreateNbt();
                    boolean current = nbt.getBoolean("HealMode");
                    nbt.putBoolean("HealMode", !current);
                    client.player.sendMessage(Text.literal("治疗模式: " + (!current ? "开启" : "关闭"))
                        .formatted(!current ? Formatting.GREEN : Formatting.GRAY), false);
                }
            }

            while (swordWaveKey.wasPressed()) {
                if (client.player.getMainHandStack().getItem() instanceof RainbowSwordItem) {
                    NbtCompound nbt = client.player.getMainHandStack().getOrCreateNbt();
                    boolean current = nbt.getBoolean("SwordWaveMode");
                    nbt.putBoolean("SwordWaveMode", !current);
                    client.player.sendMessage(Text.literal("剑气模式: " + (!current ? "开启" : "关闭"))
                        .formatted(!current ? Formatting.YELLOW : Formatting.GRAY), false);
                }
            }
        });
    }
}
