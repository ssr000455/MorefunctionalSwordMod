package com.qidai.morefunctionalswordmod.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ProtectionSettingsPanel {
    private final List<ClickableWidget> widgets = new ArrayList<>();

    public boolean allowFlight;
    public boolean immuneDamage;
    public boolean verifyProtection;
    public boolean attackProtection;
    public int playerSpeed;
    public int gamemode;
    public int maxHealth;

    public ProtectionSettingsPanel(int x, int y, int width) {
        initWidgets(x, y, width);
    }

    private void initWidgets(int x, int y, int width) {
        int rowY = y;
        int labelWidth = 120;
        int controlWidth = 80;
        int spacing = 25;

        addToggle("飞行：", allowFlight, val -> allowFlight = val, x, rowY);
        rowY += spacing;
        addToggle("免疫伤害：", immuneDamage, val -> immuneDamage = val, x, rowY);
        rowY += spacing;
        addToggle("验证保护机制：", verifyProtection, val -> verifyProtection = val, x, rowY);
        rowY += spacing;
        addToggle("攻击保护：", attackProtection, val -> attackProtection = val, x, rowY);
        rowY += spacing;

        TextFieldWidget speedField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + labelWidth, rowY, controlWidth, 20, Text.literal("速度"));
        speedField.setText(String.valueOf(playerSpeed));
        speedField.setChangedListener(text -> { try { playerSpeed = Integer.parseInt(text); if (playerSpeed < 1) playerSpeed = 1; if (playerSpeed > 99) playerSpeed = 99; } catch (NumberFormatException e) {} });
        widgets.add(speedField);
        rowY += spacing;

        widgets.add(CyclingButtonWidget.<Integer>builder(mode -> Text.literal(getGamemodeText(mode)))
                .values(0, 1, 2, 3)
                .initially(gamemode)
                .build(x + labelWidth, rowY, controlWidth, 20, Text.literal("游戏模式"), (button, value) -> gamemode = value));
        rowY += spacing;

        TextFieldWidget healthField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + labelWidth, rowY, controlWidth, 20, Text.literal("最大生命值"));
        healthField.setText(String.valueOf(maxHealth));
        healthField.setChangedListener(text -> { try { maxHealth = Integer.parseInt(text); if (maxHealth < 1) maxHealth = 1; if (maxHealth > 9999) maxHealth = 9999; } catch (NumberFormatException e) {} });
        widgets.add(healthField);
    }

    private void addToggle(String label, boolean initial, java.util.function.Consumer<Boolean> setter, int x, int y) {
        ButtonWidget button = ButtonWidget.builder(Text.literal(initial ? "是" : "否"), btn -> {
            boolean newVal = btn.getMessage().getString().equals("否");
            setter.accept(newVal);
            btn.setMessage(Text.literal(newVal ? "是" : "否"));
        }).dimensions(x + 120, y, 50, 20).build();
        widgets.add(button);
    }

    private String getGamemodeText(int mode) {
        return switch (mode) {
            case 0 -> "生存模式";
            case 1 -> "创造模式";
            case 2 -> "冒险模式";
            case 3 -> "旁观模式";
            default -> "未知";
        };
    }

    public List<ClickableWidget> getWidgets() { return widgets; }

    public void loadFromNbt(NbtCompound nbt) {
        allowFlight = nbt.getBoolean("AllowFlight");
        immuneDamage = nbt.getBoolean("ImmuneDamage");
        verifyProtection = nbt.getBoolean("VerifyProtection");
        attackProtection = nbt.getBoolean("AttackProtection");
        playerSpeed = nbt.getInt("PlayerSpeed");
        gamemode = nbt.getInt("Gamemode");
        maxHealth = nbt.getInt("MaxHealth");
        updateWidgets();
    }

    private void updateWidgets() {
        int btnIdx = 0;
        for (ClickableWidget w : widgets) {
            if (w instanceof TextFieldWidget) {
                TextFieldWidget tf = (TextFieldWidget) w;
                String hint = tf.getMessage().getString();
                if (hint.equals("速度")) tf.setText(String.valueOf(playerSpeed));
                if (hint.equals("最大生命值")) tf.setText(String.valueOf(maxHealth));
            } else if (w instanceof CyclingButtonWidget) {
                ((CyclingButtonWidget<Integer>) w).setValue(gamemode);
            } else if (w instanceof ButtonWidget && w.getMessage().getString().length() <= 2) {
                if (btnIdx == 0) ((ButtonWidget) w).setMessage(Text.literal(allowFlight ? "是" : "否"));
                if (btnIdx == 1) ((ButtonWidget) w).setMessage(Text.literal(immuneDamage ? "是" : "否"));
                if (btnIdx == 2) ((ButtonWidget) w).setMessage(Text.literal(verifyProtection ? "是" : "否"));
                if (btnIdx == 3) ((ButtonWidget) w).setMessage(Text.literal(attackProtection ? "是" : "否"));
                btnIdx++;
            }
        }
    }

    public void saveToNbt(NbtCompound nbt) {
        nbt.putBoolean("AllowFlight", allowFlight);
        nbt.putBoolean("ImmuneDamage", immuneDamage);
        nbt.putBoolean("VerifyProtection", verifyProtection);
        nbt.putBoolean("AttackProtection", attackProtection);
        nbt.putInt("PlayerSpeed", playerSpeed);
        nbt.putInt("Gamemode", gamemode);
        nbt.putInt("MaxHealth", maxHealth);
    }
}
