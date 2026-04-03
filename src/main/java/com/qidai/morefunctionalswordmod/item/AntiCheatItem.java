package com.qidai.morefunctionalswordmod.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class AntiCheatItem extends Item {
    public AntiCheatItem(Settings settings) {
        super(settings.maxCount(1).fireproof());
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.literal("反作弊器").formatted(Formatting.DARK_RED, Formatting.BOLD);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            // 主动开启/关闭反作弊全局开关（仅OP可用）
            if (serverPlayer.hasPermissionLevel(2)) {
                boolean newState = !com.qidai.morefunctionalswordmod.anticheat.AntiCheatManager.getInstance().isEnabled();
                com.qidai.morefunctionalswordmod.anticheat.AntiCheatManager.getInstance().setEnabled(newState);
                serverPlayer.sendMessage(Text.literal("反作弊器已" + (newState ? "启用" : "禁用")).formatted(newState ? Formatting.GREEN : Formatting.RED), false);
                return TypedActionResult.success(user.getStackInHand(hand));
            } else {
                serverPlayer.sendMessage(Text.literal("你没有权限使用此功能").formatted(Formatting.RED), false);
                return TypedActionResult.fail(user.getStackInHand(hand));
            }
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
}
