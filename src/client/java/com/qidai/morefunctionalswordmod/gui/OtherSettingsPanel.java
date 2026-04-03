package com.qidai.morefunctionalswordmod.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class OtherSettingsPanel {
    private final List<ClickableWidget> widgets = new ArrayList<>();
    private static final Identifier DESTROY_WORLD_ID = new Identifier("mfswordmod", "destroy_world");

    public boolean memoryFieldProtection;
    public boolean antiCheatProtection;
    public boolean antiCheatEnhanced;
    public boolean expAbsorption;
    public boolean freezeMode;
    public boolean healMode;
    public int healRange;
    public boolean swordWaveMode;
    public int swordWaveDuration;
    public float swordWaveDamage;
    public int swordWaveMiningLevel;

    private boolean confirmDestroy = false;

    public OtherSettingsPanel(int x, int y, int width) {
        initWidgets(x, y, width);
    }

    private void initWidgets(int x, int y, int width) {
        int rowY = y;
        int spacing = 25;
        int labelWidth = 120;
        int controlWidth = 80;

        addToggle("内存字段保护：", memoryFieldProtection, val -> memoryFieldProtection = val, x, rowY);
        rowY += spacing;
        addToggle("防作弊保护：", antiCheatProtection, val -> antiCheatProtection = val, x, rowY);
        rowY += spacing;
        addToggle("反作弊增强：", antiCheatEnhanced, val -> antiCheatEnhanced = val, x, rowY);
        rowY += spacing;
        addToggle("经验吸收：", expAbsorption, val -> expAbsorption = val, x, rowY);
        rowY += spacing;
        addToggle("冰冻模式：", freezeMode, val -> freezeMode = val, x, rowY);
        rowY += spacing;
        addToggle("治疗模式：", healMode, val -> healMode = val, x, rowY);
        rowY += spacing;

        TextFieldWidget healRangeField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + labelWidth, rowY, controlWidth, 20, Text.literal("治疗范围"));
        healRangeField.setText(String.valueOf(healRange));
        healRangeField.setChangedListener(text -> {
            try {
                healRange = Integer.parseInt(text);
                if (healRange < 1) healRange = 1;
                if (healRange > 50) healRange = 50;
            } catch (NumberFormatException e) {}
        });
        widgets.add(healRangeField);
        rowY += spacing;

        addToggle("剑气模式：", swordWaveMode, val -> swordWaveMode = val, x, rowY);
        rowY += spacing;

        TextFieldWidget durationField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + labelWidth, rowY, controlWidth, 20, Text.literal("剑气持续(秒)"));
        durationField.setText(String.valueOf(swordWaveDuration));
        durationField.setChangedListener(text -> {
            try {
                swordWaveDuration = Integer.parseInt(text);
                if (swordWaveDuration < 1) swordWaveDuration = 1;
                if (swordWaveDuration > 60) swordWaveDuration = 60;
            } catch (NumberFormatException e) {}
        });
        widgets.add(durationField);
        rowY += spacing;

        TextFieldWidget damageField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + labelWidth, rowY, controlWidth, 20, Text.literal("剑气伤害"));
        damageField.setText(String.valueOf((int) swordWaveDamage));
        damageField.setChangedListener(text -> {
            try {
                swordWaveDamage = Float.parseFloat(text);
                if (swordWaveDamage < 1) swordWaveDamage = 1;
                if (swordWaveDamage > 9999999) swordWaveDamage = 9999999;
            } catch (NumberFormatException e) {}
        });
        widgets.add(damageField);
        rowY += spacing;

        TextFieldWidget miningField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + labelWidth, rowY, controlWidth, 20, Text.literal("挖掘等级"));
        miningField.setText(String.valueOf(swordWaveMiningLevel));
        miningField.setChangedListener(text -> {
            try {
                swordWaveMiningLevel = Integer.parseInt(text);
                if (swordWaveMiningLevel < 0) swordWaveMiningLevel = 0;
                if (swordWaveMiningLevel > 99) swordWaveMiningLevel = 99;
            } catch (NumberFormatException e) {}
        });
        widgets.add(miningField);
        rowY += spacing;

        ButtonWidget destroyButton = ButtonWidget.builder(Text.literal("摧毁存档"), button -> {
            if (!confirmDestroy) {
                button.setMessage(Text.literal("再次点击确认摧毁").formatted(Formatting.RED));
                confirmDestroy = true;
                return;
            }
            ClientPlayNetworking.send(DESTROY_WORLD_ID, PacketByteBufs.create());
            button.setMessage(Text.literal("存档已摧毁").formatted(Formatting.RED));
            confirmDestroy = false;
        }).dimensions(x + 120, rowY, 80, 20).build();
        widgets.add(destroyButton);
    }

    private void addToggle(String label, boolean initial, java.util.function.Consumer<Boolean> setter, int x, int y) {
        ButtonWidget button = ButtonWidget.builder(Text.literal(initial ? "是" : "否"), btn -> {
            boolean newVal = btn.getMessage().getString().equals("否");
            setter.accept(newVal);
            btn.setMessage(Text.literal(newVal ? "是" : "否"));
        }).dimensions(x + 120, y, 50, 20).build();
        widgets.add(button);
    }

    public List<ClickableWidget> getWidgets() { return widgets; }

    public void loadFromNbt(NbtCompound nbt) {
        memoryFieldProtection = nbt.getBoolean("MemoryFieldProtection");
        antiCheatProtection = nbt.getBoolean("AntiCheatProtection");
        antiCheatEnhanced = nbt.getBoolean("AntiCheatEnhanced");
        expAbsorption = nbt.getBoolean("ExpAbsorption");
        freezeMode = nbt.getBoolean("FreezeMode");
        healMode = nbt.getBoolean("HealMode");
        healRange = nbt.getInt("HealRange");
        swordWaveMode = nbt.getBoolean("SwordWaveMode");
        swordWaveDuration = nbt.getInt("SwordWaveDuration");
        swordWaveDamage = nbt.getFloat("SwordWaveDamage");
        swordWaveMiningLevel = nbt.getInt("SwordWaveMiningLevel");
        updateWidgets();
    }

    private void updateWidgets() {
        int btnIdx = 0;
        for (ClickableWidget w : widgets) {
            if (w instanceof TextFieldWidget) {
                TextFieldWidget tf = (TextFieldWidget) w;
                String hint = tf.getMessage().getString();
                if (hint.equals("治疗范围")) tf.setText(String.valueOf(healRange));
                if (hint.equals("剑气持续(秒)")) tf.setText(String.valueOf(swordWaveDuration));
                if (hint.equals("剑气伤害")) tf.setText(String.valueOf((int) swordWaveDamage));
                if (hint.equals("挖掘等级")) tf.setText(String.valueOf(swordWaveMiningLevel));
            } else if (w instanceof ButtonWidget && w.getMessage().getString().length() <= 2) {
                if (btnIdx == 0) ((ButtonWidget) w).setMessage(Text.literal(memoryFieldProtection ? "是" : "否"));
                if (btnIdx == 1) ((ButtonWidget) w).setMessage(Text.literal(antiCheatProtection ? "是" : "否"));
                if (btnIdx == 2) ((ButtonWidget) w).setMessage(Text.literal(antiCheatEnhanced ? "是" : "否"));
                if (btnIdx == 3) ((ButtonWidget) w).setMessage(Text.literal(expAbsorption ? "是" : "否"));
                if (btnIdx == 4) ((ButtonWidget) w).setMessage(Text.literal(freezeMode ? "是" : "否"));
                if (btnIdx == 5) ((ButtonWidget) w).setMessage(Text.literal(healMode ? "是" : "否"));
                if (btnIdx == 6) ((ButtonWidget) w).setMessage(Text.literal(swordWaveMode ? "是" : "否"));
                btnIdx++;
            }
        }
    }

    public void saveToNbt(NbtCompound nbt) {
        nbt.putBoolean("MemoryFieldProtection", memoryFieldProtection);
        nbt.putBoolean("AntiCheatProtection", antiCheatProtection);
        nbt.putBoolean("AntiCheatEnhanced", antiCheatEnhanced);
        nbt.putBoolean("ExpAbsorption", expAbsorption);
        nbt.putBoolean("FreezeMode", freezeMode);
        nbt.putBoolean("HealMode", healMode);
        nbt.putInt("HealRange", healRange);
        nbt.putBoolean("SwordWaveMode", swordWaveMode);
        nbt.putInt("SwordWaveDuration", swordWaveDuration);
        nbt.putFloat("SwordWaveDamage", swordWaveDamage);
        nbt.putInt("SwordWaveMiningLevel", swordWaveMiningLevel);
    }
}
