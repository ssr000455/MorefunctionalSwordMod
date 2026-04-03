package com.qidai.morefunctionalswordmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import com.qidai.morefunctionalswordmod.network.RainbowUIPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class ModCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("mfswordmod")
            // 物品给予指令
            .then(CommandManager.literal("give")
                .then(CommandManager.argument("item", StringArgumentType.word())
                    .executes(context -> giveItem(context, StringArgumentType.getString(context, "item"), 1))
                    .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 64))
                        .executes(context -> giveItem(context,
                            StringArgumentType.getString(context, "item"),
                            IntegerArgumentType.getInteger(context, "count")))))
            )
            // 七彩神剑指令
            .then(CommandManager.literal("rainbow")
                .then(CommandManager.literal("mode")
                    .then(CommandManager.argument("mode", IntegerArgumentType.integer(0, 3))
                        .executes(ModCommands::setRainbowMode)))
                .then(CommandManager.literal("range")
                    .then(CommandManager.argument("range", IntegerArgumentType.integer(1, 64))
                        .executes(ModCommands::setRainbowRange)))
                .then(CommandManager.literal("damage")
                    .then(CommandManager.argument("damage", FloatArgumentType.floatArg(1))
                        .executes(ModCommands::setRainbowDamage)))
                .then(CommandManager.literal("contract")
                    .executes(ModCommands::forceContract))
                .then(CommandManager.literal("settings")
                    .then(CommandManager.argument("open", StringArgumentType.word())
                        .executes(context -> {
                            String arg = StringArgumentType.getString(context, "open");
                            if (!"true".equalsIgnoreCase(arg)) {
                                context.getSource().sendError(Text.literal("使用方法: /mfswordmod rainbow settings true"));
                                return 0;
                            }
                            ServerCommandSource source = context.getSource();
                            if (source.getEntity() instanceof ServerPlayerEntity player) {
                                ServerPlayNetworking.send(player, RainbowUIPacket.OPEN_UI_ID, PacketByteBufs.create());
                                source.sendFeedback(() -> Text.literal("正在打开七彩神剑设置界面...").formatted(Formatting.GREEN), false);
                                return 1;
                            } else {
                                source.sendError(Text.literal("只有玩家才能使用此命令"));
                                return 0;
                            }
                        }))))
            // 钻石附魔指令
            .then(CommandManager.literal("green")
                .then(CommandManager.literal("enchant")
                    .executes(context -> applyDiamondEnchant(context, "green"))))
            .then(CommandManager.literal("yellow")
                .then(CommandManager.literal("enchant")
                    .executes(context -> applyDiamondEnchant(context, "yellow"))))
            .then(CommandManager.literal("purple")
                .then(CommandManager.literal("enchant")
                    .executes(context -> applyDiamondEnchant(context, "purple"))))
            .then(CommandManager.literal("pink")
                .then(CommandManager.literal("enchant")
                    .executes(context -> applyDiamondEnchant(context, "pink")))
                .then(CommandManager.literal("bow")
                    .then(CommandManager.argument("power", IntegerArgumentType.integer(1, 10))
                        .executes(ModCommands::setPinkBowPower))))
            .then(CommandManager.literal("ultra")
                .then(CommandManager.literal("mode")
                    .then(CommandManager.argument("mode", IntegerArgumentType.integer(0, 1))
                        .executes(ModCommands::setUltraMode))))
            .then(CommandManager.literal("help")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    source.sendFeedback(() -> Text.literal("=== 更多扩展模组指令帮助 ===").formatted(Formatting.GOLD), false);
                    source.sendFeedback(() -> Text.literal("/mfswordmod give <物品ID> [数量] - 给予指定物品").formatted(Formatting.AQUA), false);
                    source.sendFeedback(() -> Text.literal("/mfswordmod rainbow mode <0-3> - 设置七彩神剑攻击模式").formatted(Formatting.AQUA), false);
                    source.sendFeedback(() -> Text.literal("/mfswordmod rainbow range <1-64> - 设置范围攻击范围").formatted(Formatting.AQUA), false);
                    source.sendFeedback(() -> Text.literal("/mfswordmod rainbow damage <数值> - 设置基础伤害").formatted(Formatting.AQUA), false);
                    source.sendFeedback(() -> Text.literal("/mfswordmod rainbow contract - 强制达成契约").formatted(Formatting.AQUA), false);
                    source.sendFeedback(() -> Text.literal("/mfswordmod rainbow settings true - 打开设置界面").formatted(Formatting.AQUA), false);
                    source.sendFeedback(() -> Text.literal("/mfswordmod green/yellow/purple/pink enchant - 应用钻石附魔").formatted(Formatting.AQUA), false);
                    source.sendFeedback(() -> Text.literal("/mfswordmod pink bow <1-10> - 设置粉钻弓力量").formatted(Formatting.AQUA), false);
                    source.sendFeedback(() -> Text.literal("/mfswordmod ultra mode <0/1> - 超级粉钻剑模式").formatted(Formatting.AQUA), false);
                    return 1;
                }))
        );
    }

    private static int giveItem(CommandContext<ServerCommandSource> context, String itemId, int count) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("只有玩家才能使用此命令"));
            return 0;
        }
        Identifier id = Identifier.tryParse(itemId);
        if (id == null || !Registries.ITEM.containsId(id)) {
            source.sendError(Text.literal("未知物品ID: " + itemId));
            return 0;
        }
        Item item = Registries.ITEM.get(id);
        ItemStack stack = new ItemStack(item, count);
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
        source.sendFeedback(() -> Text.literal("已给予 " + count + " 个 " + item.getName().getString()).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setRainbowMode(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof RainbowSwordItem)) {
            source.sendError(Text.literal("你必须手持七彩神剑"));
            return 0;
        }
        int mode = IntegerArgumentType.getInteger(context, "mode");
        stack.getOrCreateNbt().putInt("AttackMode", mode);
        RainbowSwordHelper.update(player, stack);
        source.sendFeedback(() -> Text.literal("七彩神剑攻击模式已设置为: " + mode).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setRainbowRange(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof RainbowSwordItem)) {
            source.sendError(Text.literal("你必须手持七彩神剑"));
            return 0;
        }
        int range = IntegerArgumentType.getInteger(context, "range");
        stack.getOrCreateNbt().putInt("AttackRange", range); // 修正字段名
        RainbowSwordHelper.update(player, stack);
        source.sendFeedback(() -> Text.literal("七彩神剑范围攻击范围已设置为: " + range).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setRainbowDamage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof RainbowSwordItem)) {
            source.sendError(Text.literal("你必须手持七彩神剑"));
            return 0;
        }
        float damage = FloatArgumentType.getFloat(context, "damage");
        stack.getOrCreateNbt().putFloat("BaseDamage", damage);
        RainbowSwordHelper.update(player, stack);
        source.sendFeedback(() -> Text.literal("七彩神剑基础伤害已设置为: " + damage).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int forceContract(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof RainbowSwordItem)) {
            source.sendError(Text.literal("你必须手持七彩神剑"));
            return 0;
        }
        ModUtil.setContract(stack, true);
        RainbowSwordHelper.update(player, stack);
        source.sendFeedback(() -> Text.literal("七彩神剑契约已强制达成").formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int applyDiamondEnchant(CommandContext<ServerCommandSource> context, String type) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) {
            source.sendError(Text.literal("你必须手持物品"));
            return 0;
        }
        switch (type) {
            case "green": DiamondEnchantmentHelper.applyGreenDiamondEffects(stack, player); break;
            case "yellow": DiamondEnchantmentHelper.applyYellowDiamondEffects(stack, player); break;
            case "purple": DiamondEnchantmentHelper.applyPurpleDiamondEffects(stack, player); break;
            case "pink": DiamondEnchantmentHelper.applyPinkDiamondEffects(stack, player); break;
        }
        source.sendFeedback(() -> Text.literal("已应用" + type + "钻效果").formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setPinkBowPower(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        ItemStack stack = player.getMainHandStack();
        if (stack.getItem() != ModTools.PINK_DIAMOND_BOW) {
            source.sendError(Text.literal("你必须手持粉钻弓"));
            return 0;
        }
        int power = IntegerArgumentType.getInteger(context, "power");
        stack.getOrCreateNbt().putInt("BowPower", power);
        source.sendFeedback(() -> Text.literal("粉钻弓力量已设置为: " + power).formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setUltraMode(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        ItemStack stack = player.getMainHandStack();
        if (stack.getItem() != ModTools.ULTRA_PINK_DIAMOND_SWORD) {
            source.sendError(Text.literal("你必须手持超级粉钻剑"));
            return 0;
        }
        int mode = IntegerArgumentType.getInteger(context, "mode");
        stack.getOrCreateNbt().putBoolean("UltraMode", mode == 1);
        source.sendFeedback(() -> Text.literal("超级粉钻剑模式已设置为: " + (mode == 1 ? "开启" : "关闭")).formatted(Formatting.GREEN), false);
        return 1;
    }
}
