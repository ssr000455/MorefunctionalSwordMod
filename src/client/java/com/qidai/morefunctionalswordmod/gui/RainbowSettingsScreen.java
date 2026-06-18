package com.qidai.morefunctionalswordmod.gui;

import com.qidai.morefunctionalswordmod.RainbowSwordHelper;
import com.qidai.morefunctionalswordmod.RainbowSwordItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 七彩神剑设置界面 — 现代化全屏布局
 *
 * 标签页/底部按钮通过 addDrawableChild 注册，由 Screen 原生管理事件。
 * 条目控件创建后手动渲染及处理点击，TextField 按键通过 keyPressed/charTyped 路由。
 * 所有 NBT 键名、存储逻辑、中文选项文字均与原始版本保持一致。
 */
public class RainbowSettingsScreen extends Screen {
    private static final int PANEL_PAD = 20;
    private static final int TAB_H = 32;
    private static final int HEADER_H = 36;
    private static final int FOOTER_H = 44;
    private static final int ENTRY_H = 28;
    private static final int SCROLLBAR_W = 4;

    // 主题色
    private static final int CLR_BG           = 0xCC0E0E1A;
    private static final int CLR_PANEL        = 0xF0121222;
    private static final int CLR_BORDER       = 0xFF2A2A4A;
    private static final int CLR_ACCENT       = 0xFF6C5CE7;
    private static final int CLR_ACCENT2      = 0xFFA29BFE;
    private static final int CLR_ROW_A        = 0x18000000;
    private static final int CLR_ROW_B        = 0x08000000;
    private static final int CLR_ROW_HOVER    = 0x30000000;
    private static final int CLR_TEXT         = 0xFFE8E8F0;
    private static final int CLR_TEXT_DIM     = 0xFF8888AA;
    private static final int CLR_SCROLLBAR    = 0x996C5CE7;
    private static final int CLR_SCROLLBAR_BG = 0x331A1A3A;

    private ItemStack swordStack;
    private NbtCompound nbt;
    private ClientPlayerEntity player;

    private int activeTab = 0;
    private int scrollY = 0;
    private int contentHeight = 0;

    // 面板区域
    private int panelX, panelY, panelW, panelH;
    private int contentX, contentY, contentW, contentH;

    // 条目
    private final List<SettingEntry> entries = new ArrayList<>();

    // 所有设置字段
    private boolean modifyNbt, removeEntity, removeEntityData, rangeAttack, fieldReflection,
            continuousAttack, lightningAttack, fireAttack, explosionAttack, infiniteDamage;
    private int attackRange = 16, continuousAttackTime = 100, lightningCount = 1, explosionRadius = 2;
    private float baseDamage = 999999f;

    private boolean allowFlight, immuneDamage, protectAndEncryptNbt, verifyProtection,
            attackProtection, memoryProtection;
    private int playerSpeed = 1, gamemode = 0, maxHealth = 20;

    private boolean memoryFieldProtection, antiCheatProtection, antiCheatEnhanced,
            expAbsorption, freezeMode, healMode, swordWaveMode;
    private int healRange = 3, swordWaveDuration = 5, swordWaveMiningLevel = 0, miningRange = 5;
    private float swordWaveDamage = 999999f;

    public RainbowSettingsScreen() {
        super(Text.translatable("gui.mfswordmod.settings.title"));
    }

    // ═══════════════════ init ═══════════════════

    @Override
    protected void init() {
        super.init();
        player = MinecraftClient.getInstance().player;
        if (player == null) { close(); return; }
        swordStack = player.getMainHandStack();
        if (!(swordStack.getItem() instanceof RainbowSwordItem)) { close(); return; }
        nbt = swordStack.getOrCreateNbt();
        loadFromNbt();

        recalcLayout();
        rebuildWidgets();
    }

    private void recalcLayout() {
        panelW = Math.min(width - 40, 480);
        panelH = Math.min(height - 40, height - 40);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        contentX = panelX + PANEL_PAD;
        contentY = panelY + HEADER_H + TAB_H + 6;
        contentW = panelW - PANEL_PAD * 2 - SCROLLBAR_W - 6;
        contentH = panelH - HEADER_H - TAB_H - FOOTER_H - 14;
    }

    /** 重建所有控件（init / 切换标签页时调用） */
    private void rebuildWidgets() {
        entries.clear();

        // ── 标签页按钮 ──
        int tabW = (panelW - PANEL_PAD * 2) / 3;
        int tabY = panelY + HEADER_H + 2;
        addDrawableChild(new TabButton(panelX + PANEL_PAD,            tabY, tabW, TAB_H - 4, "攻击设置", 0));
        addDrawableChild(new TabButton(panelX + PANEL_PAD + tabW,     tabY, tabW, TAB_H - 4, "保护设置", 1));
        addDrawableChild(new TabButton(panelX + PANEL_PAD + tabW * 2, tabY, tabW, TAB_H - 4, "其他设置", 2));

        // ── 底部按钮 ──
        int btnY = panelY + panelH - FOOTER_H + 8;
        int btnW2 = 90;
        int btnGap = 16;
        int totalBtnW = btnW2 * 2 + btnGap;
        int startX = panelX + (panelW - totalBtnW) / 2;
        addDrawableChild(new ThemeButton(startX, btnY, btnW2, 28,
                Text.translatable("gui.mfswordmod.settings.save"),
                0xFF00B894, 0xFF00D2A0, 0xFFFFFFFF, b -> saveSettings()));
        addDrawableChild(new ThemeButton(startX + btnW2 + btnGap, btnY, btnW2, 28,
                Text.translatable("gui.mfswordmod.settings.close"),
                0xFFD63031, 0xFFFF4757, 0xFFFFFFFF, b -> close()));

        // ── 条目 ──
        buildEntries();
    }

    private void buildEntries() {
        entries.clear();
        if (activeTab == 0) {
            entries.add(new BooleanEntry("修改NBT", modifyNbt, v -> modifyNbt = v));
            entries.add(new BooleanEntry("移除实体", removeEntity, v -> removeEntity = v));
            entries.add(new BooleanEntry("移除实体数据", removeEntityData, v -> removeEntityData = v));
            entries.add(new BooleanEntry("范围攻击", rangeAttack, v -> rangeAttack = v));
            entries.add(new IntEntry("攻击范围", attackRange, 1, 256, v -> attackRange = v));
            entries.add(new BooleanEntry("字段反射", fieldReflection, v -> fieldReflection = v));
            entries.add(new BooleanEntry("无限伤害", infiniteDamage, v -> infiniteDamage = v));
            entries.add(new FloatEntry("基础伤害", baseDamage, 1f, 99999999f, v -> baseDamage = v));
            entries.add(new BooleanEntry("持续攻击生物", continuousAttack, v -> continuousAttack = v));
            entries.add(new IntEntry("攻击时间(刻)", continuousAttackTime, 1, 9999, v -> continuousAttackTime = v));
            entries.add(new BooleanEntry("攻击带闪电", lightningAttack, v -> lightningAttack = v));
            entries.add(new IntEntry("闪电数量", lightningCount, 1, 5, v -> lightningCount = v));
            entries.add(new BooleanEntry("攻击带火焰", fireAttack, v -> fireAttack = v));
            entries.add(new BooleanEntry("攻击带爆炸", explosionAttack, v -> explosionAttack = v));
            entries.add(new IntEntry("爆炸半径", explosionRadius, 1, 10, v -> explosionRadius = v));
        } else if (activeTab == 1) {
            entries.add(new BooleanEntry("飞行", allowFlight, v -> allowFlight = v));
            entries.add(new BooleanEntry("免疫伤害", immuneDamage, v -> immuneDamage = v));
            entries.add(new BooleanEntry("保护并加密NBT", protectAndEncryptNbt, v -> protectAndEncryptNbt = v));
            entries.add(new BooleanEntry("验证保护机制", verifyProtection, v -> verifyProtection = v));
            entries.add(new BooleanEntry("攻击保护", attackProtection, v -> attackProtection = v));
            entries.add(new BooleanEntry("内存保护", memoryProtection, v -> memoryProtection = v));
            entries.add(new IntEntry("人物速度", playerSpeed, 1, 99, v -> playerSpeed = v));
            entries.add(new GamemodeEntry("游戏模式", gamemode, v -> gamemode = v));
            entries.add(new IntEntry("最大生命值", maxHealth, 1, 9999, v -> maxHealth = v));
        } else {
            entries.add(new BooleanEntry("内存字段保护", memoryFieldProtection, v -> memoryFieldProtection = v));
            entries.add(new BooleanEntry("防作弊保护", antiCheatProtection, v -> antiCheatProtection = v));
            entries.add(new BooleanEntry("反作弊增强", antiCheatEnhanced, v -> antiCheatEnhanced = v));
            entries.add(new BooleanEntry("经验吸收", expAbsorption, v -> expAbsorption = v));
            entries.add(new BooleanEntry("冰冻模式", freezeMode, v -> freezeMode = v));
            entries.add(new BooleanEntry("治疗模式", healMode, v -> healMode = v));
            entries.add(new IntEntry("治疗范围", healRange, 1, 50, v -> healRange = v));
            entries.add(new BooleanEntry("剑气模式", swordWaveMode, v -> swordWaveMode = v));
            entries.add(new IntEntry("剑气持续(秒)", swordWaveDuration, 1, 60, v -> swordWaveDuration = v));
            entries.add(new FloatEntry("剑气伤害", swordWaveDamage, 1f, 9999999f, v -> swordWaveDamage = v));
            entries.add(new IntEntry("挖掘等级", swordWaveMiningLevel, 0, 99, v -> swordWaveMiningLevel = v));
            entries.add(new IntEntry("挖掘范围", miningRange, 1, 10, v -> miningRange = v));
        }
        contentHeight = entries.size() * ENTRY_H;
    }

    // ═══════════════════ NBT ═══════════════════

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
        nbt.putFloat("BaseDamage", infiniteDamage ? Float.POSITIVE_INFINITY : baseDamage);
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
        ClientPlayNetworking.send(new Identifier("mfswordmod", "rainbow_settings_sync"), buf);
        player.sendMessage(Text.translatable("gui.mfswordmod.settings.saved").formatted(Formatting.GREEN), false);
    }

    // ═══════════════════ 标签页 ═══════════════════

    /** 切换标签页时直接重建整个界面控件 */
    private void setTab(int tab) {
        if (activeTab == tab) return;
        activeTab = tab;
        scrollY = 0;
        // 完全重建 — 清空所有已注册控件后重新创建
        this.clearChildren();
        rebuildWidgets();
    }

    // ═══════════════════ render ═══════════════════

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        int mx = mouseX, my = mouseY;

        // 背景遮罩
        context.fill(0, 0, width, height, CLR_BG);

        // 主面板
        drawPanel(context);

        // 标题
        context.drawCenteredTextWithShadow(textRenderer, title, panelX + panelW / 2, panelY + 12, CLR_ACCENT2);

        // 标签页底部分隔线
        context.fill(contentX, panelY + HEADER_H + TAB_H, panelX + panelW - PANEL_PAD, panelY + HEADER_H + TAB_H + 1, CLR_BORDER);

        // ── 裁剪内容区域 + 绘制条目背景行 ──
        context.enableScissor(contentX, contentY, contentX + contentW + SCROLLBAR_W + 6, contentY + contentH);
        int rowY = contentY - scrollY;
        for (int idx = 0; idx < entries.size(); idx++) {
            boolean hover = mx >= contentX && mx < contentX + contentW && my >= rowY && my < rowY + ENTRY_H;
            int rowClr = hover ? CLR_ROW_HOVER : ((idx % 2 == 0) ? CLR_ROW_A : CLR_ROW_B);
            if (rowClr != 0) {
                context.fill(contentX, rowY, contentX + contentW + SCROLLBAR_W + 6, rowY + ENTRY_H, rowClr);
            }
            // 渲染条目标签 + 控件
            entries.get(idx).render(context, contentX, rowY, contentW, ENTRY_H, mx, my, delta);
            rowY += ENTRY_H;
        }
        context.disableScissor();

        // 绘制已注册的控件（tab、save/close 按钮）
        super.render(context, mouseX, mouseY, delta);

        // 滚动条
        drawScrollbar(context);
    }

    private void drawPanel(DrawContext ctx) {
        int x = panelX, y = panelY, w = panelW, h = panelH;
        ctx.fill(x, y, x + w, y + h, CLR_PANEL);
        ctx.fill(x, y, x + w, y + 1, CLR_BORDER);
        ctx.fill(x, y + h - 1, x + w, y + h, CLR_BORDER);
        ctx.fill(x, y, x + 1, y + h, CLR_BORDER);
        ctx.fill(x + w - 1, y, x + w, y + h, CLR_BORDER);
        ctx.fill(x + PANEL_PAD, y + HEADER_H - 1, x + w - PANEL_PAD, y + HEADER_H, CLR_BORDER);
    }

    private void drawScrollbar(DrawContext ctx) {
        if (contentHeight <= contentH) return;
        int maxScroll = contentHeight - contentH;
        int barH = Math.max(20, contentH * contentH / contentHeight);
        int barX = contentX + contentW + 1;
        int barY = contentY + (scrollY * (contentH - barH) / maxScroll);
        ctx.fill(barX, contentY, barX + SCROLLBAR_W, contentY + contentH, CLR_SCROLLBAR_BG);
        ctx.fill(barX, barY, barX + SCROLLBAR_W, barY + barH, CLR_SCROLLBAR);
    }

    // ═══════════════════ 事件 ═══════════════════

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX, my = (int) mouseY;

        // 条目点击（在裁剪区域内）
        if (mx >= contentX && mx < contentX + contentW + SCROLLBAR_W + 6
                && my >= contentY && my < contentY + contentH) {
            int y = contentY - scrollY;
            for (SettingEntry entry : entries) {
                if (my >= y && my < y + ENTRY_H) {
                    if (entry.mouseClicked(mx, my, contentX, y, ENTRY_H, button)) return true;
                }
                y += ENTRY_H;
            }
        }

        // 让 Screen 处理已注册控件的点击（tab、save/close）
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseX >= contentX && mouseX <= contentX + contentW + SCROLLBAR_W + 6
                && mouseY >= contentY && mouseY <= contentY + contentH) {
            int maxScroll = Math.max(0, contentHeight - contentH);
            scrollY = (int) Math.max(0, Math.min(maxScroll, scrollY - amount * 20));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { close(); return true; }
        // 路由按键给所有 TextField
        for (SettingEntry entry : entries) {
            if (entry instanceof NumberEntry<?> ne) {
                if (ne.textField != null && ne.textField.isFocused()
                        && ne.textField.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (SettingEntry entry : entries) {
            if (entry instanceof NumberEntry<?> ne) {
                if (ne.textField != null && ne.textField.isFocused()
                        && ne.textField.charTyped(chr, modifiers)) {
                    return true;
                }
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override public boolean shouldPause() { return false; }
    @Override public boolean shouldCloseOnEsc() { return true; }

    // ═══════════════════ 自定义控件类 ═══════════════════

    /** 主题色按钮（通过 addDrawableChild 注册，由 Screen 管理事件） */
    private class ThemeButton extends ButtonWidget {
        private final int normalColor, hoverColor, txtColor;

        public ThemeButton(int x, int y, int w, int h, Text msg,
                           int normalColor, int hoverColor, int txtColor, PressAction action) {
            super(x, y, w, h, msg, action, DEFAULT_NARRATION_SUPPLIER);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            this.txtColor = txtColor;
        }

        @Override
        public void renderButton(DrawContext ctx, int mx, int my, float delta) {
            int bg = isHovered() ? hoverColor : normalColor;
            ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
            ctx.drawCenteredTextWithShadow(textRenderer, getMessage(),
                    getX() + getWidth() / 2, getY() + (getHeight() - 9) / 2, txtColor);
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {}
    }

    /** 标签页按钮 */
    private class TabButton extends ButtonWidget {
        private final String label;
        private final int tabIdx;

        public TabButton(int x, int y, int w, int h, String label, int tabIdx) {
            super(x, y, w, h, Text.literal(label), b -> setTab(tabIdx), DEFAULT_NARRATION_SUPPLIER);
            this.label = label;
            this.tabIdx = tabIdx;
        }

        @Override
        public void renderButton(DrawContext ctx, int mx, int my, float delta) {
            boolean active = tabIdx == activeTab;
            boolean hover = isHovered();
            int bg = active ? CLR_ACCENT : (hover ? 0xAA3D3D6B : 0x882A2A4A);
            ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
            int txtClr = active ? 0xFFFFFFFF : CLR_TEXT_DIM;
            ctx.drawCenteredTextWithShadow(textRenderer, label,
                    getX() + getWidth() / 2, getY() + (getHeight() - 9) / 2, txtClr);
            if (active) {
                ctx.fill(getX() + 6, getY() + getHeight() - 2, getX() + getWidth() - 6, getY() + getHeight(), 0xFFFFFFFF);
            }
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {}
    }

    // ═══════════════════ 设置项接口与实现 ═══════════════════

    private interface SettingEntry {
        void render(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta);
        boolean mouseClicked(double mouseX, double mouseY, int entryX, int entryY, int entryH, int button);
    }

    private abstract class BaseEntry implements SettingEntry {
        protected final String label;

        protected BaseEntry(String label) { this.label = label; }

        protected void drawLabel(DrawContext ctx, int x, int y) {
            ctx.drawText(textRenderer, label, x + 6, y + (ENTRY_H - 9) / 2, CLR_TEXT, false);
        }
    }

    // ── 布尔开关 ──

    private class BooleanEntry extends BaseEntry {
        private boolean value;
        private final Consumer<Boolean> setter;
        // 开/关按钮的区域缓存（相对于 contentX/entryY）
        private boolean lastHovered;

        public BooleanEntry(String label, boolean initial, Consumer<Boolean> setter) {
            super(label);
            this.value = initial;
            this.setter = setter;
        }

        @Override
        public void render(DrawContext ctx, int x, int y, int width, int height, int mx, int my, float delta) {
            drawLabel(ctx, x, y);

            int btnW = 44, btnH = 22;
            int btnX = x + width - btnW - 4;
            int btnY = y + (height - btnH) / 2;
            boolean hover = mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + btnH;
            lastHovered = hover;

            int bg = value ? (hover ? 0xFF00B894 : 0xFF00A884)
                           : (hover ? 0xFF636E82 : 0xFF4A5568);
            ctx.fill(btnX, btnY, btnX + btnW, btnY + btnH, bg);
            ctx.drawCenteredTextWithShadow(textRenderer, value ? "开" : "关",
                    btnX + btnW / 2, btnY + (btnH - 9) / 2, 0xFFFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int entryX, int entryY, int entryH, int btn) {
            int btnW = 44, btnH = 22;
            int btnX = entryX + contentW - btnW - 4;
            int btnY = entryY + (entryH - btnH) / 2;
            if (mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + btnH) {
                value = !value;
                setter.accept(value);
                return true;
            }
            return false;
        }
    }

    // ── 数值输入 ──

    private abstract class NumberEntry<T extends Number> extends BaseEntry {
        protected T value;
        protected final Consumer<T> setter;
        protected TextFieldWidget textField;
        protected boolean invalid;

        protected NumberEntry(String label, T initial, Consumer<T> setter) {
            super(label);
            this.value = initial;
            this.setter = setter;
        }

        protected abstract T parseValue(String text) throws NumberFormatException;
        protected abstract String formatValue(T val);
        protected abstract boolean validate(T val);

        @Override
        public void render(DrawContext ctx, int x, int y, int width, int height, int mx, int my, float delta) {
            drawLabel(ctx, x, y);

            int tfW = 60, tfX = x + width - tfW - 4;
            int cy = y + (height - 20) / 2;

            if (textField == null) {
                textField = new TextFieldWidget(textRenderer, tfX, cy, tfW, 20, Text.literal(""));
                textField.setText(formatValue(value));
            } else {
                textField.setPosition(tfX, cy);
            }

            if (invalid) {
                ctx.fill(tfX - 1, cy - 1, tfX + tfW + 1, cy + 21, 0xFFFF6B6B);
            }

            textField.render(ctx, mx, my, delta);
        }

        private void confirmInput() {
            if (textField == null) return;
            invalid = false;
            try {
                T v = parseValue(textField.getText());
                if (validate(v)) {
                    value = v;
                    setter.accept(v);
                }
                textField.setText(formatValue(value));
            } catch (NumberFormatException e) {
                invalid = true;
            }
        }

        @Override
        public boolean mouseClicked(double mx, double my, int entryX, int entryY, int entryH, int btn) {
            if (textField != null) {
                int tfW = 60, tfX = entryX + contentW - tfW - 4;
                int cy = entryY + (entryH - 20) / 2;
                if (mx >= tfX && mx < tfX + tfW && my >= cy && my < cy + 20) {
                    textField.setFocused(true);
                    return true;
                }
                // 点击 TextField 外部时确认输入
                if (textField.isFocused()) {
                    confirmInput();
                    textField.setFocused(false);
                }
            }
            return false;
        }
    }

    private class IntEntry extends NumberEntry<Integer> {
        private final int min, max;

        public IntEntry(String label, int initial, int min, int max, Consumer<Integer> setter) {
            super(label, initial, setter);
            this.min = min;
            this.max = max;
        }

        @Override protected Integer parseValue(String text) { return Integer.parseInt(text); }
        @Override protected String formatValue(Integer val) { return String.valueOf(val); }
        @Override protected boolean validate(Integer val) {
            if (val < min) { value = min; return false; }
            if (val > max) { value = max; return false; }
            return true;
        }
    }

    private class FloatEntry extends NumberEntry<Float> {
        private final float min, max;

        public FloatEntry(String label, float initial, float min, float max, Consumer<Float> setter) {
            super(label, initial, setter);
            this.min = min;
            this.max = max;
        }

        @Override protected Float parseValue(String text) {
            if (text.equalsIgnoreCase("infinity") || text.equalsIgnoreCase("inf")) {
                return Float.POSITIVE_INFINITY;
            }
            return Float.parseFloat(text);
        }
        @Override protected String formatValue(Float val) {
            return Float.isInfinite(val) ? "infinity" : String.valueOf(val);
        }
        @Override protected boolean validate(Float val) {
            if (val < min && !Float.isInfinite(val)) { value = min; return false; }
            if (val > max && !Float.isInfinite(val)) { value = max; return false; }
            return true;
        }
    }

    // ── 游戏模式循环 ──

    private class GamemodeEntry extends BaseEntry {
        private int value;
        private final Consumer<Integer> setter;
        private static final String[] MODES = {"生存", "创造", "冒险", "旁观"};

        public GamemodeEntry(String label, int initial, Consumer<Integer> setter) {
            super(label);
            this.value = initial;
            this.setter = setter;
        }

        @Override
        public void render(DrawContext ctx, int x, int y, int width, int height, int mx, int my, float delta) {
            drawLabel(ctx, x, y);

            int btnW = 64, btnH = 22;
            int btnX = x + width - btnW - 4;
            int btnY = y + (height - btnH) / 2;
            boolean hover = mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + btnH;

            ctx.fill(btnX, btnY, btnX + btnW, btnY + btnH, hover ? 0xFF5A4BD1 : 0xFF6C5CE7);
            ctx.drawCenteredTextWithShadow(textRenderer, MODES[value],
                    btnX + btnW / 2, btnY + (btnH - 9) / 2, 0xFFFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int entryX, int entryY, int entryH, int btn) {
            int btnW = 64, btnH = 22;
            int btnX = entryX + contentW - btnW - 4;
            int btnY = entryY + (entryH - btnH) / 2;
            if (mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + btnH) {
                value = (value + 1) % 4;
                setter.accept(value);
                return true;
            }
            return false;
        }
    }
}
