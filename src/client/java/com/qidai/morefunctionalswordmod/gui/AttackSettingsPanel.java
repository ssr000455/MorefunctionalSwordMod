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

public class AttackSettingsPanel {
    private final List<ClickableWidget> widgets = new ArrayList<>();

    public int currentMode;
    public boolean modifyNbt;
    public boolean removeEntity;
    public boolean removeEntityData;
    public boolean rangeAttack;
    public int attackRange;
    public boolean fieldReflection;
    public float baseDamage;
    public boolean continuousAttack;
    public int continuousAttackTime;
    public boolean lightningAttack;
    public int lightningCount;
    public boolean fireAttack;
    public boolean explosionAttack;
    public int explosionRadius;

    public AttackSettingsPanel(int x, int y, int width) {
        initWidgets(x, y, width);
    }

    private void initWidgets(int x, int y, int width) {
        int rowY = y;
        int labelWidth = 120;
        int controlWidth = 80;
        int spacing = 25;

        widgets.add(CyclingButtonWidget.<Integer>builder(mode -> Text.literal(getModeText(mode)))
                .values(0, 1, 2, 3)
                .initially(currentMode)
                .build(x + labelWidth, rowY, controlWidth, 20, Text.literal("当前模式"), (button, value) -> currentMode = value));
        rowY += spacing;

        addToggle("修改NBT：", modifyNbt, val -> modifyNbt = val, x, rowY);
        rowY += spacing;
        addToggle("移除实体：", removeEntity, val -> removeEntity = val, x, rowY);
        rowY += spacing;
        addToggle("移除实体数据：", removeEntityData, val -> removeEntityData = val, x, rowY);
        rowY += spacing;
        addToggle("范围攻击：", rangeAttack, val -> rangeAttack = val, x, rowY);
        rowY += spacing;

        TextFieldWidget rangeField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + labelWidth, rowY, controlWidth, 20, Text.literal("攻击范围"));
        rangeField.setText(String.valueOf(attackRange));
        rangeField.setChangedListener(text -> { try { attackRange = Integer.parseInt(text); if (attackRange < 1) attackRange = 1; if (attackRange > 256) attackRange = 256; } catch (NumberFormatException e) {} });
        widgets.add(rangeField);
        rowY += spacing;

        addToggle("字段反射：", fieldReflection, val -> fieldReflection = val, x, rowY);
        rowY += spacing;

        TextFieldWidget damageField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + labelWidth, rowY, controlWidth, 20, Text.literal("基础伤害"));
        damageField.setText(String.valueOf((int)baseDamage));
        damageField.setChangedListener(text -> { try { baseDamage = Float.parseFloat(text); if (baseDamage < 1) baseDamage = 1; if (baseDamage > 99999999) baseDamage = 99999999; } catch (NumberFormatException e) {} });
        widgets.add(damageField);
        rowY += spacing;

        addToggle("持续攻击生物：", continuousAttack, val -> continuousAttack = val, x, rowY);
        rowY += spacing;

        TextFieldWidget timeField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + labelWidth, rowY, controlWidth, 20, Text.literal("攻击时间(刻)"));
        timeField.setText(String.valueOf(continuousAttackTime));
        timeField.setChangedListener(text -> { try { continuousAttackTime = Integer.parseInt(text); if (continuousAttackTime < 1) continuousAttackTime = 1; if (continuousAttackTime > 9999) continuousAttackTime = 9999; } catch (NumberFormatException e) {} });
        widgets.add(timeField);
        rowY += spacing;

        addToggle("攻击带闪电：", lightningAttack, val -> lightningAttack = val, x, rowY);
        rowY += spacing;

        TextFieldWidget lightningCountField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + labelWidth, rowY, controlWidth, 20, Text.literal("闪电数量"));
        lightningCountField.setText(String.valueOf(lightningCount));
        lightningCountField.setChangedListener(text -> { try { lightningCount = Integer.parseInt(text); if (lightningCount < 1) lightningCount = 1; if (lightningCount > 5) lightningCount = 5; } catch (NumberFormatException e) {} });
        widgets.add(lightningCountField);
        rowY += spacing;

        addToggle("攻击带火焰：", fireAttack, val -> fireAttack = val, x, rowY);
        rowY += spacing;
        addToggle("攻击带爆炸：", explosionAttack, val -> explosionAttack = val, x, rowY);
        rowY += spacing;

        TextFieldWidget explosionField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + labelWidth, rowY, controlWidth, 20, Text.literal("爆炸半径"));
        explosionField.setText(String.valueOf(explosionRadius));
        explosionField.setChangedListener(text -> { try { explosionRadius = Integer.parseInt(text); if (explosionRadius < 1) explosionRadius = 1; if (explosionRadius > 10) explosionRadius = 10; } catch (NumberFormatException e) {} });
        widgets.add(explosionField);
    }

    private void addToggle(String label, boolean initial, java.util.function.Consumer<Boolean> setter, int x, int y) {
        ButtonWidget button = ButtonWidget.builder(Text.literal(initial ? "是" : "否"), btn -> {
            boolean newVal = btn.getMessage().getString().equals("否");
            setter.accept(newVal);
            btn.setMessage(Text.literal(newVal ? "是" : "否"));
        }).dimensions(x + 120, y, 50, 20).build();
        widgets.add(button);
    }

    private String getModeText(int mode) {
        return switch (mode) {
            case 0 -> "安全模式";
            case 1 -> "范围攻击模式";
            case 2 -> "无限制模式";
            case 3 -> "攻击模式";
            default -> "未知";
        };
    }

    public List<ClickableWidget> getWidgets() { return widgets; }

    public void loadFromNbt(NbtCompound nbt) {
        currentMode = nbt.getInt("AttackMode");
        modifyNbt = nbt.getBoolean("ModifyNbt");
        removeEntity = nbt.getBoolean("RemoveEntity");
        removeEntityData = nbt.getBoolean("RemoveEntityData");
        rangeAttack = nbt.getBoolean("RangeAttack");
        attackRange = nbt.getInt("AttackRange");
        fieldReflection = nbt.getBoolean("FieldReflection");
        baseDamage = nbt.getFloat("BaseDamage");
        continuousAttack = nbt.getBoolean("ContinuousAttack");
        continuousAttackTime = nbt.getInt("ContinuousAttackTime");
        lightningAttack = nbt.getBoolean("LightningAttack");
        lightningCount = nbt.getInt("LightningCount");
        fireAttack = nbt.getBoolean("FireAttack");
        explosionAttack = nbt.getBoolean("ExplosionAttack");
        explosionRadius = nbt.getInt("ExplosionRadius");
        updateWidgets();
    }

    private void updateWidgets() {
        int btnIdx = 0;
        for (ClickableWidget w : widgets) {
            if (w instanceof CyclingButtonWidget) {
                ((CyclingButtonWidget<Integer>) w).setValue(currentMode);
            } else if (w instanceof TextFieldWidget) {
                TextFieldWidget tf = (TextFieldWidget) w;
                String hint = tf.getMessage().getString();
                if (hint.equals("攻击范围")) tf.setText(String.valueOf(attackRange));
                if (hint.equals("基础伤害")) tf.setText(String.valueOf((int)baseDamage));
                if (hint.equals("攻击时间(刻)")) tf.setText(String.valueOf(continuousAttackTime));
                if (hint.equals("闪电数量")) tf.setText(String.valueOf(lightningCount));
                if (hint.equals("爆炸半径")) tf.setText(String.valueOf(explosionRadius));
            } else if (w instanceof ButtonWidget && w.getMessage().getString().length() <= 2) {
                if (btnIdx == 0) ((ButtonWidget) w).setMessage(Text.literal(modifyNbt ? "是" : "否"));
                if (btnIdx == 1) ((ButtonWidget) w).setMessage(Text.literal(removeEntity ? "是" : "否"));
                if (btnIdx == 2) ((ButtonWidget) w).setMessage(Text.literal(removeEntityData ? "是" : "否"));
                if (btnIdx == 3) ((ButtonWidget) w).setMessage(Text.literal(rangeAttack ? "是" : "否"));
                if (btnIdx == 4) ((ButtonWidget) w).setMessage(Text.literal(fieldReflection ? "是" : "否"));
                if (btnIdx == 5) ((ButtonWidget) w).setMessage(Text.literal(continuousAttack ? "是" : "否"));
                if (btnIdx == 6) ((ButtonWidget) w).setMessage(Text.literal(lightningAttack ? "是" : "否"));
                if (btnIdx == 7) ((ButtonWidget) w).setMessage(Text.literal(fireAttack ? "是" : "否"));
                if (btnIdx == 8) ((ButtonWidget) w).setMessage(Text.literal(explosionAttack ? "是" : "否"));
                btnIdx++;
            }
        }
    }

    public void saveToNbt(NbtCompound nbt) {
        nbt.putInt("AttackMode", currentMode);
        nbt.putBoolean("ModifyNbt", modifyNbt);
        nbt.putBoolean("RemoveEntity", removeEntity);
        nbt.putBoolean("RemoveEntityData", removeEntityData);
        nbt.putBoolean("RangeAttack", rangeAttack);
        nbt.putInt("AttackRange", attackRange);
        nbt.putBoolean("FieldReflection", fieldReflection);
        nbt.putFloat("BaseDamage", baseDamage);
        nbt.putBoolean("ContinuousAttack", continuousAttack);
        nbt.putInt("ContinuousAttackTime", continuousAttackTime);
        nbt.putBoolean("LightningAttack", lightningAttack);
        nbt.putInt("LightningCount", lightningCount);
        nbt.putBoolean("FireAttack", fireAttack);
        nbt.putBoolean("ExplosionAttack", explosionAttack);
        nbt.putInt("ExplosionRadius", explosionRadius);
    }
}
