package com.qidai.morefunctionalswordmod;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    // 原有材料组
    public static final ItemGroup MATERIALS_GROUP = Registry.register(Registries.ITEM_GROUP,
        new Identifier(ModRegistry.MOD_ID, "materials"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemgroup.mfswordmod.materials"))
            .icon(() -> new ItemStack(ModRegistry.GREEN_DIAMOND))
            .entries((displayContext, entries) -> {
                entries.add(ModRegistry.GOLD_STICK);
                entries.add(ModRegistry.COMPRESSED_DIAMOND);
                entries.add(ModRegistry.GREEN_DIAMOND);
                entries.add(ModRegistry.YELLOW_DIAMOND);
                entries.add(ModRegistry.PURPLE_DIAMOND);
                entries.add(ModRegistry.PURPLE_DIAMOND_STICK);
                entries.add(ModRegistry.PURPLE_DIAMOND_GEM);
                entries.add(ModRegistry.PINK_DIAMOND);
                entries.add(ModRegistry.PINK_DIAMOND_GEM);
                entries.add(ModRegistry.COMPRESSED_DIAMOND_BLOCK_ITEM);
                entries.add(ModRegistry.PURPLE_DIAMOND_BLOCK_ITEM);
                entries.add(ModRegistry.PINK_DIAMOND_BLOCK_ITEM);
                entries.add(ModRegistry.GREEN_DIAMOND_ORE_ITEM);
                entries.add(ModRegistry.YELLOW_DIAMOND_ORE_ITEM);
                entries.add(ModRegistry.DEEPSLATE_YELLOW_DIAMOND_ORE_ITEM);
                entries.add(ModRegistry.PURPLE_DIAMOND_ORE_ITEM);
                entries.add(ModRegistry.PINK_DIAMOND_ORE_ITEM);
            })
            .build());

    // 原有工具组
    public static final ItemGroup TOOLS_GROUP = Registry.register(Registries.ITEM_GROUP,
        new Identifier(ModRegistry.MOD_ID, "tools"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemgroup.mfswordmod.tools"))
            .icon(() -> new ItemStack(ModTools.GREEN_DIAMOND_SWORD))
            .entries((displayContext, entries) -> {
                entries.add(ModTools.GREEN_DIAMOND_SWORD);
                entries.add(ModTools.GREEN_DIAMOND_PICKAXE);
                entries.add(ModTools.GREEN_DIAMOND_AXE);
                entries.add(ModTools.GREEN_DIAMOND_SHOVEL);
                entries.add(ModTools.GREEN_DIAMOND_HOE);
                entries.add(ModTools.YELLOW_DIAMOND_SWORD);
                entries.add(ModTools.YELLOW_DIAMOND_PICKAXE);
                entries.add(ModTools.YELLOW_DIAMOND_AXE);
                entries.add(ModTools.YELLOW_DIAMOND_SHOVEL);
                entries.add(ModTools.YELLOW_DIAMOND_HOE);
                entries.add(ModTools.PURPLE_DIAMOND_SWORD);
                entries.add(ModTools.PURPLE_DIAMOND_PICKAXE);
                entries.add(ModTools.PURPLE_DIAMOND_AXE);
                entries.add(ModTools.PURPLE_DIAMOND_SHOVEL);
                entries.add(ModTools.PURPLE_DIAMOND_HOE);
                entries.add(ModTools.PINK_DIAMOND_SWORD);
                entries.add(ModTools.PINK_DIAMOND_PICKAXE);
                entries.add(ModTools.PINK_DIAMOND_AXE);
                entries.add(ModTools.PINK_DIAMOND_SHOVEL);
                entries.add(ModTools.PINK_DIAMOND_HOE);
                entries.add(ModTools.PINK_DIAMOND_BOW);
                entries.add(ModTools.REINFORCED_PINK_DIAMOND_SWORD);
                entries.add(ModTools.ULTRA_PINK_DIAMOND_SWORD);
            })
            .build());

    // 原有盔甲组
    public static final ItemGroup ARMOR_GROUP = Registry.register(Registries.ITEM_GROUP,
        new Identifier(ModRegistry.MOD_ID, "armor"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemgroup.mfswordmod.armor"))
            .icon(() -> new ItemStack(ModTools.GREEN_DIAMOND_CHESTPLATE))
            .entries((displayContext, entries) -> {
                entries.add(ModTools.GREEN_DIAMOND_HELMET);
                entries.add(ModTools.GREEN_DIAMOND_CHESTPLATE);
                entries.add(ModTools.GREEN_DIAMOND_LEGGINGS);
                entries.add(ModTools.GREEN_DIAMOND_BOOTS);
                entries.add(ModTools.YELLOW_DIAMOND_HELMET);
                entries.add(ModTools.YELLOW_DIAMOND_CHESTPLATE);
                entries.add(ModTools.YELLOW_DIAMOND_LEGGINGS);
                entries.add(ModTools.YELLOW_DIAMOND_BOOTS);
                entries.add(ModTools.PURPLE_DIAMOND_HELMET);
                entries.add(ModTools.PURPLE_DIAMOND_CHESTPLATE);
                entries.add(ModTools.PURPLE_DIAMOND_LEGGINGS);
                entries.add(ModTools.PURPLE_DIAMOND_BOOTS);
                entries.add(ModTools.PINK_DIAMOND_HELMET);
                entries.add(ModTools.PINK_DIAMOND_CHESTPLATE);
                entries.add(ModTools.PINK_DIAMOND_LEGGINGS);
                entries.add(ModTools.PINK_DIAMOND_BOOTS);
            })
            .build());

    // 彩虹维度标签页
    public static final ItemGroup RAINBOW_DIMENSION_GROUP = Registry.register(Registries.ITEM_GROUP,
        new Identifier(ModRegistry.MOD_ID, "rainbow_dimension"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemgroup.mfswordmod.rainbow_dimension"))
            .icon(() -> new ItemStack(ModRegistry.RAINBOW_GEM))
            .entries((displayContext, entries) -> {
                entries.add(ModRegistry.RAINBOW_GEM_BLOCK_ITEM);
                entries.add(ModRegistry.RAINBOW_GEM_ORE_ITEM);
                entries.add(ModRegistry.DEPLETED_RAINBOW_BLOCK_ITEM);
                entries.add(ModRegistry.RAINBOW_CRAFTING_TABLE_ITEM);
                entries.add(ModRegistry.RAINBOW_POISON_WATER_BUCKET);
                entries.add(ModRegistry.RAINBOW_SEA_WATER_BUCKET);
                entries.add(ModRegistry.DECAYED_RAINBOW_WATER_BUCKET);
                entries.add(ModRegistry.RAINBOW_GEM);
                entries.add(ModTools.RAINBOW_GEM_SWORD);
                entries.add(ModTools.RAINBOW_GEM_PICKAXE);
                entries.add(ModTools.RAINBOW_GEM_AXE);
                entries.add(ModTools.RAINBOW_GEM_SHOVEL);
                entries.add(ModTools.RAINBOW_GEM_HOE);
                entries.add(ModTools.RAINBOW_GEM_BOW);
                entries.add(ModTools.RAINBOW_GEM_HELMET);
                entries.add(ModTools.RAINBOW_GEM_CHESTPLATE);
                entries.add(ModTools.RAINBOW_GEM_LEGGINGS);
                entries.add(ModTools.RAINBOW_GEM_BOOTS);
                entries.add(ModTools.RAINBOW_SWORD);
                entries.add(ModRegistry.RAINBOW_KEY);
            })
            .build());

    // 模组生物标签页
    public static final ItemGroup MOB_GROUP = Registry.register(Registries.ITEM_GROUP,
        new Identifier(ModRegistry.MOD_ID, "mobs"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemgroup.mfswordmod.mobs"))
            .icon(() -> new ItemStack(ModRegistry.RAINBOW_GUARDIAN_SPAWN_EGG))
            .entries((displayContext, entries) -> {
                entries.add(ModRegistry.RAINBOW_GUARDIAN_SPAWN_EGG);
                entries.add(ModRegistry.DEPLETED_RAINBOW_SPAWN_EGG);
                entries.add(ModRegistry.RAINBOW_GHOST_SPAWN_EGG);
                entries.add(ModRegistry.CALAMITY_WRAITH_SPAWN_EGG);
                entries.add(ModRegistry.CALAMITY_PHANTOM_SPAWN_EGG);
                entries.add(ModRegistry.CALAMITY_WIND_SPAWN_EGG);
                entries.add(ModRegistry.CALAMITY_SOLDIER_SPAWN_EGG);
                entries.add(ModRegistry.DECAYED_RAINBOW_MONSTER_SPAWN_EGG);
            })
            .build());

    public static void registerItemGroups() {
        // 空方法
    }
}
