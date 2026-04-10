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

        float scale = Math.min((float) sw / IMG_W, (float) sh / IMG_H);
        int drawW = (int) (IMG_W * scale);
        int drawH = (int) (IMG_H * scale);
        int x = (sw - drawW) / 2;
        int y = (sh - drawH) / 2;

        context.fill(0, 0, sw, sh, 0xFF000000);
        context.drawTexture(CHEAT_IMG, x, y, 0, 0, drawW, drawH, drawW, drawH);

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

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // 保持默认背景渲染行为
        super.renderBackground(context);
    }
}
