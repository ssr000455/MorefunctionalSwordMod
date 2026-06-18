package com.qidai.morefunctionalswordmod.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CheatScreen extends Screen {
    private static final Identifier CHEAT_IMG = new Identifier("mfswordmod", "textures/gui/cheat_screen.png");
    private static final int IMG_W = 1024;
    private static final int IMG_H = 512;

    // 缓存位置和尺寸，只在窗口改变时重新计算
    private int cachedSw = -1, cachedSh = -1;
    private int cachedX, cachedY, cachedDrawW, cachedDrawH;

    public CheatScreen() {
        super(Text.literal(""));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int sw = this.width;
        int sh = this.height;

        // 如果窗口尺寸没变，直接使用缓存值；否则重新计算
        if (sw != cachedSw || sh != cachedSh) {
            float scale = Math.min((float) sw / IMG_W, (float) sh / IMG_H);
            cachedDrawW = (int) (IMG_W * scale);
            cachedDrawH = (int) (IMG_H * scale);
            cachedX = (sw - cachedDrawW) / 2;
            cachedY = (sh - cachedDrawH) / 2;
            cachedSw = sw;
            cachedSh = sh;
        }

        context.fill(0, 0, sw, sh, 0xFF000000);
        context.drawTexture(CHEAT_IMG, cachedX, cachedY, 0, 0, cachedDrawW, cachedDrawH, cachedDrawW, cachedDrawH);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(DrawContext context) {
        super.renderBackground(context);
    }
}
