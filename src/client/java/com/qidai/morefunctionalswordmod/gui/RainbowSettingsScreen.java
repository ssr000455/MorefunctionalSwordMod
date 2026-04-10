package com.qidai.morefunctionalswordmod.gui;

import com.qidai.morefunctionalswordmod.RainbowSwordHelper;
import com.qidai.morefunctionalswordmod.RainbowSwordItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RainbowSettingsScreen extends Screen {
    private static final Identifier SYNC_ID = new Identifier("mfswordmod", "rainbow_settings_sync");

    private ItemStack swordStack;
    private NbtCompound nbt;
    private ClientPlayerEntity player;

    private ScrollablePanel scrollPanel;
    private final List<ClickableWidget> allWidgets = new ArrayList<>();

    // 设置数据
    private boolean modifyNbt, removeEntity, removeEntityData, rangeAttack, fieldReflection, continuousAttack, lightningAttack, fireAttack, explosionAttack;
    private int attackRange = 16, continuousAttackTime = 100, lightningCount = 1, explosionRadius = 2;
    private float baseDamage = 999999f;
    private boolean infiniteDamage = true;

    private boolean allowFlight, immuneDamage, protectAndEncryptNbt, verifyProtection, attackProtection, memoryProtection;
    private int playerSpeed = 1, gamemode = 0, maxHealth = 20;

    private boolean memoryFieldProtection, antiCheatProtection, antiCheatEnhanced, expAbsorption, freezeMode, healMode, swordWaveMode;
    private int healRange = 3, swordWaveDuration = 5, swordWaveMiningLevel = 0, miningRange = 5;
    private float swordWaveDamage = 999999f;

    private int activeTab = 0;
    private ButtonWidget attackTabBtn, protectTabBtn, otherTabBtn, saveBtn, exitBtn;

    public RainbowSettingsScreen() {
        super(Text.literal("七彩神剑设置"));
    }

    @Override
    protected void init() {
        super.init();
        player = MinecraftClient.getInstance().player;
        if (player == null) { close(); return; }
        swordStack = player.getMainHandStack();
        if (!(swordStack.getItem() instanceof RainbowSwordItem)) { close(); return; }
        nbt = swordStack.getOrCreateNbt();

        loadFromNbt();

        int panelX = 20;
        int panelY = 60;
        int panelWidth = width - 40;
        int panelHeight = height - 100;
        scrollPanel = new ScrollablePanel(this, panelX, panelY, panelWidth, panelHeight);
        addDrawableChild(scrollPanel);

        // 攻击页
        int y = panelY + 10;
        y = addToggle("修改NBT", modifyNbt, v -> modifyNbt = v, panelX, y, 0);
        y = addToggle("移除实体", removeEntity, v -> removeEntity = v, panelX, y, 0);
        y = addToggle("移除实体数据", removeEntityData, v -> removeEntityData = v, panelX, y, 0);
        y = addToggle("范围攻击", rangeAttack, v -> rangeAttack = v, panelX, y, 0);
        y = addIntInput("攻击范围", attackRange, 1, 256, v -> attackRange = v, panelX, y, 0);
        y = addToggle("字段反射", fieldReflection, v -> fieldReflection = v, panelX, y, 0);
        y = addToggle("无限伤害", infiniteDamage, v -> infiniteDamage = v, panelX, y, 0);
        y = addFloatInput("基础伤害", baseDamage, 1f, 99999999f, v -> baseDamage = v, panelX, y, 0);
        y = addToggle("持续攻击生物", continuousAttack, v -> continuousAttack = v, panelX, y, 0);
        y = addIntInput("攻击时间(刻)", continuousAttackTime, 1, 9999, v -> continuousAttackTime = v, panelX, y, 0);
        y = addToggle("攻击带闪电", lightningAttack, v -> lightningAttack = v, panelX, y, 0);
        y = addIntInput("闪电数量", lightningCount, 1, 5, v -> lightningCount = v, panelX, y, 0);
        y = addToggle("攻击带火焰", fireAttack, v -> fireAttack = v, panelX, y, 0);
        y = addToggle("攻击带爆炸", explosionAttack, v -> explosionAttack = v, panelX, y, 0);
        y = addIntInput("爆炸半径", explosionRadius, 1, 10, v -> explosionRadius = v, panelX, y, 0);

        // 保护页
        y = panelY + 10;
        y = addToggle("飞行", allowFlight, v -> allowFlight = v, panelX, y, 1);
        y = addToggle("免疫伤害", immuneDamage, v -> immuneDamage = v, panelX, y, 1);
        y = addToggle("保护并加密NBT", protectAndEncryptNbt, v -> protectAndEncryptNbt = v, panelX, y, 1);
        y = addToggle("验证保护机制", verifyProtection, v -> verifyProtection = v, panelX, y, 1);
        y = addToggle("攻击保护", attackProtection, v -> attackProtection = v, panelX, y, 1);
        y = addToggle("内存保护", memoryProtection, v -> memoryProtection = v, panelX, y, 1);
        y = addIntInputWithConfirm("人物速度", playerSpeed, 1, 99, v -> playerSpeed = v, panelX, y, 1);
        y = addGamemodeCycle("游戏模式", gamemode, v -> gamemode = v, panelX, y, 1);
        y = addIntInput("最大生命值", maxHealth, 1, 9999, v -> maxHealth = v, panelX, y, 1);

        // 其他页
        y = panelY + 10;
        y = addToggle("内存字段保护", memoryFieldProtection, v -> memoryFieldProtection = v, panelX, y, 2);
        y = addToggle("防作弊保护", antiCheatProtection, v -> antiCheatProtection = v, panelX, y, 2);
        y = addToggle("反作弊增强", antiCheatEnhanced, v -> antiCheatEnhanced = v, panelX, y, 2);
        y = addToggle("经验吸收", expAbsorption, v -> expAbsorption = v, panelX, y, 2);
        y = addToggle("冰冻模式", freezeMode, v -> freezeMode = v, panelX, y, 2);
        y = addToggle("治疗模式", healMode, v -> healMode = v, panelX, y, 2);
        y = addIntInput("治疗范围", healRange, 1, 50, v -> healRange = v, panelX, y, 2);
        y = addToggle("剑气模式", swordWaveMode, v -> swordWaveMode = v, panelX, y, 2);
        y = addIntInput("剑气持续(秒)", swordWaveDuration, 1, 60, v -> swordWaveDuration = v, panelX, y, 2);
        y = addFloatInput("剑气伤害", swordWaveDamage, 1f, 9999999f, v -> swordWaveDamage = v, panelX, y, 2);
        y = addIntInput("挖掘等级", swordWaveMiningLevel, 0, 99, v -> swordWaveMiningLevel = v, panelX, y, 2);
        y = addIntInput("挖掘范围", miningRange, 1, 10, v -> miningRange = v, panelX, y, 2);

        for (ClickableWidget w : allWidgets) w.visible = false;

        int tabWidth = (panelWidth - 20) / 3;
        attackTabBtn = ButtonWidget.builder(Text.literal("攻击设置"), b -> { activeTab = 0; updateVisibility(); })
                .dimensions(panelX, panelY - 25, tabWidth, 20).build();
        protectTabBtn = ButtonWidget.builder(Text.literal("保护设置"), b -> { activeTab = 1; updateVisibility(); })
                .dimensions(panelX + tabWidth, panelY - 25, tabWidth, 20).build();
        otherTabBtn = ButtonWidget.builder(Text.literal("其他设置"), b -> { activeTab = 2; updateVisibility(); })
                .dimensions(panelX + tabWidth * 2, panelY - 25, tabWidth, 20).build();

        addDrawableChild(attackTabBtn);
        addDrawableChild(protectTabBtn);
        addDrawableChild(otherTabBtn);

        saveBtn = ButtonWidget.builder(Text.literal("保存修改"), b -> saveSettings())
                .dimensions(width / 2 - 100, height - 35, 90, 20).build();
        exitBtn = ButtonWidget.builder(Text.literal("退出UI"), b -> close())
                .dimensions(width / 2 + 10, height - 35, 90, 20).build();
        addDrawableChild(saveBtn);
        addDrawableChild(exitBtn);

        updateVisibility();
    }

    private void loadFromNbt() {
        modifyNbt = nbt.getBoolean("ModifyNbt");
        removeEntity = nbt.getBoolean("RemoveEntity");
        removeEntityData = nbt.getBoolean("RemoveEntityData");
        rangeAttack = nbt.getBoolean("RangeAttack");
        attackRange = nbt.getInt("AttackRange"); if (attackRange <= 0) attackRange = 16;
        fieldReflection = nbt.getBoolean("FieldReflection");
        baseDamage = nbt.getFloat("BaseDamage"); if (baseDamage <= 0 && !Float.isInfinite(baseDamage)) baseDamage = 999999f;
        infiniteDamage = Float.isInfinite(baseDamage);
        continuousAttack = nbt.getBoolean("ContinuousAttack");
        continuousAttackTime = nbt.getInt("ContinuousAttackTime"); if (continuousAttackTime <= 0) continuousAttackTime = 100;
        lightningAttack = nbt.getBoolean("LightningAttack");
        lightningCount = nbt.getInt("LightningCount"); if (lightningCount <= 0) lightningCount = 1;
        fireAttack = nbt.getBoolean("FireAttack");
        explosionAttack = nbt.getBoolean("ExplosionAttack");
        explosionRadius = nbt.getInt("ExplosionRadius"); if (explosionRadius <= 0) explosionRadius = 2;

        allowFlight = nbt.getBoolean("AllowFlight");
        immuneDamage = nbt.getBoolean("ImmuneDamage");
        protectAndEncryptNbt = nbt.getBoolean("ProtectAndEncryptNbt");
        verifyProtection = nbt.getBoolean("VerifyProtection");
        attackProtection = nbt.getBoolean("AttackProtection");
        memoryProtection = nbt.getBoolean("MemoryProtection");
        playerSpeed = nbt.getInt("PlayerSpeed"); if (playerSpeed < 1) playerSpeed = 1;
        gamemode = nbt.getInt("Gamemode");
        maxHealth = nbt.getInt("MaxHealth"); if (maxHealth < 1) maxHealth = 20;

        memoryFieldProtection = nbt.getBoolean("MemoryFieldProtection");
        antiCheatProtection = nbt.getBoolean("AntiCheatProtection");
        antiCheatEnhanced = nbt.getBoolean("AntiCheatEnhanced");
        expAbsorption = nbt.getBoolean("ExpAbsorption");
        freezeMode = nbt.getBoolean("FreezeMode");
        healMode = nbt.getBoolean("HealMode");
        healRange = nbt.getInt("HealRange"); if (healRange <= 0) healRange = 3;
        swordWaveMode = nbt.getBoolean("SwordWaveMode");
        swordWaveDuration = nbt.getInt("SwordWaveDuration"); if (swordWaveDuration <= 0) swordWaveDuration = 5;
        swordWaveDamage = nbt.getFloat("SwordWaveDamage"); if (swordWaveDamage <= 0) swordWaveDamage = 999999f;
        swordWaveMiningLevel = nbt.getInt("SwordWaveMiningLevel");
        miningRange = nbt.getInt("MiningRange"); if (miningRange <= 0) miningRange = 5;
    }

    private void saveSettings() {
        nbt.putBoolean("ModifyNbt", modifyNbt);
        nbt.putBoolean("RemoveEntity", removeEntity);
        nbt.putBoolean("RemoveEntityData", removeEntityData);
        nbt.putBoolean("RangeAttack", rangeAttack);
        nbt.putInt("AttackRange", attackRange);
        nbt.putBoolean("FieldReflection", fieldReflection);
        if (infiniteDamage) {
            nbt.putFloat("BaseDamage", Float.POSITIVE_INFINITY);
        } else {
            nbt.putFloat("BaseDamage", baseDamage);
        }
        nbt.putBoolean("ContinuousAttack", continuousAttack);
        nbt.putInt("ContinuousAttackTime", continuousAttackTime);
        nbt.putBoolean("LightningAttack", lightningAttack);
        nbt.putInt("LightningCount", lightningCount);
        nbt.putBoolean("FireAttack", fireAttack);
        nbt.putBoolean("ExplosionAttack", explosionAttack);
        nbt.putInt("ExplosionRadius", explosionRadius);

        nbt.putBoolean("AllowFlight", allowFlight);
        nbt.putBoolean("ImmuneDamage", immuneDamage);
        nbt.putBoolean("ProtectAndEncryptNbt", protectAndEncryptNbt);
        nbt.putBoolean("VerifyProtection", verifyProtection);
        nbt.putBoolean("AttackProtection", attackProtection);
        nbt.putBoolean("MemoryProtection", memoryProtection);
        nbt.putInt("PlayerSpeed", playerSpeed);
        nbt.putInt("Gamemode", gamemode);
        nbt.putInt("MaxHealth", maxHealth);

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
        nbt.putInt("MiningRange", miningRange);

        RainbowSwordHelper.update(player, swordStack);
        var buf = PacketByteBufs.create();
        buf.writeNbt(nbt);
        ClientPlayNetworking.send(SYNC_ID, buf);
        player.sendMessage(Text.literal("设置已保存").formatted(Formatting.GREEN), false);
    }

    private void updateVisibility() {
        for (ClickableWidget w : allWidgets) w.visible = false;
        int start = 0, end = 0;
        switch (activeTab) {
            case 0:
                start = 0;
                end = 15;
                break;
            case 1:
                start = 15;
                end = 15 + 9;
                break;
            case 2:
                start = 15 + 9;
                end = allWidgets.size();
                break;
        }
        for (int i = start; i < end; i++) allWidgets.get(i).visible = true;
    }

    private void drawLabel(DrawContext context, String text, int x, int y) {
        context.drawText(textRenderer, text, x, y + 6, 0xFFFFFF, false);
    }

    private int addToggle(String label, boolean initial, Consumer<Boolean> setter, int x, int y, int tabId) {
        scrollPanel.addLabel(label, 0, y - 60);
        ButtonWidget btn = ButtonWidget.builder(Text.literal(initial ? "是" : "否"), b -> {
            boolean newVal = b.getMessage().getString().equals("否");
            setter.accept(newVal);
            b.setMessage(Text.literal(newVal ? "是" : "否"));
        }).dimensions(x + 100, y, 50, 20).build();
        allWidgets.add(btn);
        scrollPanel.addChild(btn);
        return y + 25;
    }

    private int addIntInput(String label, int defaultValue, int min, int max, Consumer<Integer> setter, int x, int y, int tabId) {
        scrollPanel.addLabel(label, 0, y - 60);
        TextFieldWidget tf = new TextFieldWidget(textRenderer, x + 100, y, 60, 20, Text.literal(""));
        tf.setText(String.valueOf(defaultValue));
        tf.setChangedListener(t -> {
            try {
                int val = Integer.parseInt(t);
                if (val < min) val = min;
                if (val > max) val = max;
                setter.accept(val);
                tf.setText(String.valueOf(val));
            } catch (Exception e) {}
        });
        allWidgets.add(tf);
        scrollPanel.addChild(tf);
        return y + 25;
    }

    private int addIntInputWithConfirm(String label, int defaultValue, int min, int max, Consumer<Integer> setter, int x, int y, int tabId) {
        scrollPanel.addLabel(label, 0, y - 60);
        TextFieldWidget tf = new TextFieldWidget(textRenderer, x + 100, y, 50, 20, Text.literal(""));
        tf.setText(String.valueOf(defaultValue));
        int[] pending = {defaultValue};
        tf.setChangedListener(t -> { try { pending[0] = Integer.parseInt(t); } catch (Exception e) {} });
        ButtonWidget confirm = ButtonWidget.builder(Text.literal("确定"), b -> {
            int val = pending[0];
            if (val < min) val = min;
            if (val > max) val = max;
            setter.accept(val);
            tf.setText(String.valueOf(val));
        }).dimensions(x + 155, y, 40, 20).build();
        allWidgets.add(tf);
        allWidgets.add(confirm);
        scrollPanel.addChild(tf);
        scrollPanel.addChild(confirm);
        return y + 25;
    }

    private int addFloatInput(String label, float defaultValue, float min, float max, Consumer<Float> setter, int x, int y, int tabId) {
        scrollPanel.addLabel(label, 0, y - 60);
        TextFieldWidget tf = new TextFieldWidget(textRenderer, x + 100, y, 60, 20, Text.literal(""));
        tf.setText(Float.isInfinite(defaultValue) ? "infinity" : String.valueOf(defaultValue));
        tf.setChangedListener(t -> {
            try {
                float val;
                if (t.equalsIgnoreCase("infinity") || t.equalsIgnoreCase("inf")) {
                    val = Float.POSITIVE_INFINITY;
                    infiniteDamage = true;
                } else {
                    val = Float.parseFloat(t);
                    if (val < min) val = min;
                    if (val > max) val = max;
                    infiniteDamage = false;
                }
                setter.accept(val);
                tf.setText(Float.isInfinite(val) ? "infinity" : String.valueOf(val));
            } catch (Exception e) {}
        });
        allWidgets.add(tf);
        scrollPanel.addChild(tf);
        return y + 25;
    }

    private int addGamemodeCycle(String label, int current, Consumer<Integer> setter, int x, int y, int tabId) {
        scrollPanel.addLabel(label, 0, y - 60);
        String[] modes = {"生存模式", "创造模式", "冒险模式", "旁观模式"};
        ButtonWidget cycle = ButtonWidget.builder(Text.literal(modes[current]), b -> {
            int newVal = (gamemode + 1) % 4;
            setter.accept(newVal);
            b.setMessage(Text.literal(modes[newVal]));
        }).dimensions(x + 100, y, 80, 20).build();
        allWidgets.add(cycle);
        scrollPanel.addChild(cycle);
        return y + 25;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.fill(0, 0, width, 40, 0xCC000000);
        context.drawCenteredTextWithShadow(textRenderer, this.title, width / 2, 15, 0xFFFFFF);
        context.fill(0, 40, width, 41, 0xFF222222);

        int panelX = 20;
        int panelY = 60;
        int panelWidth = width - 40;
        int panelHeight = height - 100;

        context.fill(panelX - 6, panelY - 6, panelX + panelWidth + 6, panelY + panelHeight + 6, 0xAA000000);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF101010);
        context.fill(panelX + panelWidth - 8, panelY, panelX + panelWidth, panelY + panelHeight, 0x55000000);

        if (scrollPanel.getContentHeight() > panelHeight) {
            int maxScroll = Math.max(1, scrollPanel.getContentHeight() - panelHeight);
            int thumbHeight = Math.max(24, panelHeight * panelHeight / scrollPanel.getContentHeight());
            int thumbY = panelY + (int) ((double) scrollPanel.getScrollY() / maxScroll * (panelHeight - thumbHeight));
            context.fill(panelX + panelWidth - 8, thumbY, panelX + panelWidth, thumbY + thumbHeight, 0xFF8888FF);
        }

        int baseY = 60;
        if (activeTab == 0) {
            // Labels are now handled by ScrollablePanel
        } else if (activeTab == 1) {
            // Labels are now handled by ScrollablePanel
        } else if (activeTab == 2) {
            // Labels are now handled by ScrollablePanel
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrollPanel.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (scrollPanel.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrollPanel.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (scrollPanel.mouseScrolled(mouseX, mouseY, amount)) return true;
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    private static class Label {
        String text;
        int x, y;
        Label(String text, int x, int y) {
            this.text = text; this.x = x; this.y = y;
        }
    }

    // 滚动面板
    private class ScrollablePanel implements net.minecraft.client.gui.Drawable, Element, Selectable {
        private final int x, y, width, height;
        private final List<ClickableWidget> children = new ArrayList<>();
        private final List<Label> labels = new ArrayList<>();
        private int scrollY = 0, contentHeight = 0;
        private boolean focused = false;
        private boolean draggingScrollbar = false;
        private int dragStartY = 0;
        private final RainbowSettingsScreen screen;

        public ScrollablePanel(RainbowSettingsScreen screen, int x, int y, int width, int height) {
            this.screen = screen;
            this.x = x; this.y = y; this.width = width; this.height = height;
        }

        public void addChild(ClickableWidget child) {
            children.add(child);
            contentHeight = Math.max(contentHeight, child.getY() + child.getHeight());
        }

        public void addLabel(String text, int relX, int relY) {
            labels.add(new Label(text, relX, relY));
            contentHeight = Math.max(contentHeight, relY + 10);
        }

        public int getContentHeight() {
            return contentHeight;
        }

        public int getScrollY() {
            return scrollY;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.enableScissor(x, y, x + width, y + height);
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(0, -scrollY, 0);
            for (ClickableWidget child : children) {
                child.render(context, mouseX, mouseY + scrollY, delta);
            }
            for (Label label : labels) {
                context.drawTextWithShadow(screen.textRenderer, label.text, x + label.x, y + label.y, 0xFFFFFF);
            }
            matrices.pop();
            context.disableScissor();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (contentHeight > height) {
                int scrollbarX = x + width - 8;
                if (mouseX >= scrollbarX && mouseX <= scrollbarX + 8) {
                    int maxScroll = Math.max(1, contentHeight - height);
                    int thumbHeight = Math.max(24, height * height / contentHeight);
                    int thumbY = y + (int) ((double) scrollY / maxScroll * (height - thumbHeight));
                    if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                        draggingScrollbar = true;
                        dragStartY = (int) (mouseY - thumbY);
                        return true;
                    }
                }
            }
            if (!isMouseOver(mouseX, mouseY)) return false;
            double adjY = mouseY + scrollY;
            for (ClickableWidget child : children) {
                if (child.isMouseOver(mouseX, adjY) && child.mouseClicked(mouseX, adjY, button))
                    return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (draggingScrollbar) {
                draggingScrollbar = false;
                return true;
            }
            if (!isMouseOver(mouseX, mouseY)) return false;
            double adjY = mouseY + scrollY;
            for (ClickableWidget child : children) {
                if (child.isMouseOver(mouseX, adjY) && child.mouseReleased(mouseX, adjY, button))
                    return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (draggingScrollbar) {
                int maxScroll = Math.max(1, contentHeight - height);
                int thumbHeight = Math.max(24, height * height / contentHeight);
                int newThumbY = (int) mouseY - dragStartY;
                int relativeY = newThumbY - y;
                scrollY = (int) ((double) relativeY / (height - thumbHeight) * maxScroll);
                scrollY = Math.max(0, Math.min(scrollY, maxScroll));
                return true;
            }
            if (!isMouseOver(mouseX, mouseY)) return false;
            double adjY = mouseY + scrollY;
            for (ClickableWidget child : children) {
                if (child.isMouseOver(mouseX, adjY) && child.mouseDragged(mouseX, adjY, button, deltaX, deltaY))
                    return true;
            }
            return false;
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            if (!isMouseOver(mouseX, mouseY)) return false;
            int maxScroll = Math.max(0, contentHeight - height);
            scrollY = (int) Math.max(0, Math.min(maxScroll, scrollY - amount * 20));
            return true;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        @Override
        public boolean isFocused() { return focused; }
        @Override
        public void setFocused(boolean focused) { this.focused = focused; }
        @Override
        public SelectionType getType() { return SelectionType.NONE; }
        @Override
        public void appendNarrations(NarrationMessageBuilder builder) {}
    }
}
