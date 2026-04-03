package com.qidai.morefunctionalswordmod.gui;

import com.qidai.morefunctionalswordmod.ModUtil;
import com.qidai.morefunctionalswordmod.RainbowSwordHelper;
import com.qidai.morefunctionalswordmod.RainbowSwordItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RainbowSettingsScreen extends Screen {
    private ItemStack swordStack;
    private NbtCompound nbt;
    private ClientPlayerEntity player;

    private ScrollablePanel scrollPanel;
    private AttackSettingsPanel attackPanel;
    private ProtectionSettingsPanel protectionPanel;
    private OtherSettingsPanel otherPanel;

    private int activeTab = 0;
    private ButtonWidget attackTabBtn;
    private ButtonWidget protectTabBtn;
    private ButtonWidget otherTabBtn;
    private ButtonWidget saveBtn;
    private ButtonWidget exitBtn;

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

        int panelX = 20;
        int panelY = 60;
        int panelWidth = width - 40;
        int panelHeight = height - 100;

        scrollPanel = new ScrollablePanel(panelX, panelY, panelWidth, panelHeight);

        attackPanel = new AttackSettingsPanel(panelX, 0, panelWidth);
        protectionPanel = new ProtectionSettingsPanel(panelX, 0, panelWidth);
        otherPanel = new OtherSettingsPanel(panelX, 0, panelWidth);

        attackPanel.loadFromNbt(nbt);
        protectionPanel.loadFromNbt(nbt);
        otherPanel.loadFromNbt(nbt);

        for (ClickableWidget w : attackPanel.getWidgets()) { w.setY(w.getY() + 10); scrollPanel.addChild(w); }
        for (ClickableWidget w : protectionPanel.getWidgets()) { w.setY(w.getY() + 10); scrollPanel.addChild(w); }
        for (ClickableWidget w : otherPanel.getWidgets()) { w.setY(w.getY() + 10); scrollPanel.addChild(w); }

        int tabWidth = (panelWidth - 20) / 3;
        attackTabBtn = ButtonWidget.builder(Text.literal("攻击设置"), button -> { activeTab = 0; updateVisibility(); })
                .dimensions(panelX, panelY - 25, tabWidth, 20).build();
        protectTabBtn = ButtonWidget.builder(Text.literal("保护设置"), button -> { activeTab = 1; updateVisibility(); })
                .dimensions(panelX + tabWidth, panelY - 25, tabWidth, 20).build();
        otherTabBtn = ButtonWidget.builder(Text.literal("其他设置"), button -> { activeTab = 2; updateVisibility(); })
                .dimensions(panelX + tabWidth * 2, panelY - 25, tabWidth, 20).build();

        addDrawable(attackTabBtn);
        addDrawable(protectTabBtn);
        addDrawable(otherTabBtn);
        addDrawable(scrollPanel);

        saveBtn = ButtonWidget.builder(Text.literal("保存修改"), button -> saveSettings())
                .dimensions(width / 2 - 100, height - 35, 90, 20).build();
        exitBtn = ButtonWidget.builder(Text.literal("退出UI"), button -> close())
                .dimensions(width / 2 + 10, height - 35, 90, 20).build();
        addDrawable(saveBtn);
        addDrawable(exitBtn);

        updateVisibility();
    }

    private void updateVisibility() {
        for (ClickableWidget w : attackPanel.getWidgets()) w.visible = (activeTab == 0);
        for (ClickableWidget w : protectionPanel.getWidgets()) w.visible = (activeTab == 1);
        for (ClickableWidget w : otherPanel.getWidgets()) w.visible = (activeTab == 2);
    }

    private void saveSettings() {
        attackPanel.saveToNbt(nbt);
        protectionPanel.saveToNbt(nbt);
        otherPanel.saveToNbt(nbt);
        RainbowSwordHelper.update(player, swordStack);
        player.sendMessage(Text.literal("设置已保存").formatted(Formatting.GREEN), false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.fill(0, 0, width, 40, 0x88000000);
        context.drawCenteredTextWithShadow(textRenderer, this.title, width / 2, 15, 0xFFFFFF);
        context.fill(0, 40, width, 41, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (scrollPanel.isMouseOver(mouseX, mouseY)) {
            return scrollPanel.mouseScrolled(mouseX, mouseY, amount);
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
