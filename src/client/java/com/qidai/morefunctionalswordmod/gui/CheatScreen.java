package com.qidai.morefunctionalswordmod.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CheatScreen extends Screen {
    private static final Identifier CHEAT_IMG = new Identifier("mfswordmod", "textures/gui/cheat_screen.png");
    private static final int IMG_W = 1024;
    private static final int IMG_H = 512;

    public CheatScreen() {
        super(Text.literal(""));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int sw = this.width;
        int sh = this.height;

        // 计算缩放比例（保持图片完整，不变形）
        float scale = Math.min((float) sw / IMG_W, (float) sh / IMG_H);
        int drawW = (int) (IMG_W * scale);
        int drawH = (int) (IMG_H * scale);
        int x = (sw - drawW) / 2;
        int y = (sh - drawH) / 2;

        // 黑色背景填充多余区域
        context.fill(0, 0, sw, sh, 0xFF000000);

        // 绘制图片（居中，不变形）
        context.drawTexture(CHEAT_IMG, x, y, 0, 0, drawW, drawH, drawW, drawH);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // 不渲染默认背景
    }
}
