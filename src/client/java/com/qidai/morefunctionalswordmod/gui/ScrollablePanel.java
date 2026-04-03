package com.qidai.morefunctionalswordmod.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScrollablePanel implements Drawable, ParentElement {
    private final int x, y, width, height;
    private final List<ClickableWidget> children = new ArrayList<>();
    private int scrollY = 0;
    private int contentHeight = 0;
    private boolean focused = false;

    public ScrollablePanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void addChild(ClickableWidget child) {
        children.add(child);
        contentHeight = Math.max(contentHeight, child.getY() + child.getHeight());
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
        matrices.pop();
        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) return false;
        double adjY = mouseY + scrollY;
        for (ClickableWidget child : children) {
            if (child.mouseClicked(mouseX, adjY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) return false;
        double adjY = mouseY + scrollY;
        for (ClickableWidget child : children) {
            if (child.mouseReleased(mouseX, adjY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) return false;
        double adjY = mouseY + scrollY;
        for (ClickableWidget child : children) {
            if (child.mouseDragged(mouseX, adjY, button, deltaX, deltaY)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) return false;
        int maxScroll = Math.max(0, contentHeight - height);
        scrollY = (int) Math.max(0, Math.min(maxScroll, scrollY - amount * 20));
        return true;
    }

    @Override
    public List<? extends Element> children() {
        return children;
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
        children.forEach(consumer);
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }
}
