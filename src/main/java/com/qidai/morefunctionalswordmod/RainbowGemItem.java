package com.qidai.morefunctionalswordmod;

import net.minecraft.item.Item;

public class RainbowGemItem extends Item {
    public RainbowGemItem(Settings settings) {
        super(settings.fireproof()); // 不可销毁
    }
}
