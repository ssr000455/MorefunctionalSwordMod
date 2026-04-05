package com.qidai.morefunctionalswordmod;

import com.qidai.morefunctionalswordmod.fluid.*;
import com.qidai.morefunctionalswordmod.block.*;
import com.qidai.morefunctionalswordmod.item.CalamityWindItem;
import com.qidai.morefunctionalswordmod.item.RainbowKeyItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;

public class ModRegistry {
    public static final String MOD_ID = "mfswordmod";

    private static <T extends Item> T registerItem(String name, T item) {
        return Registry.register(Registries.ITEM, id(name), item);
    }

    private static <T extends Block> T registerBlock(String name, T block) {
        return Registry.register(Registries.BLOCK, id(name), block);
    }

    private static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    // ========== 材料类 ==========
    public static final Item GOLD_STICK = registerItem("gold_stick", new Item(new FabricItemSettings()));
    public static final Item COMPRESSED_DIAMOND = registerItem("compressed_diamond", new Item(new FabricItemSettings().fireproof()));
    public static final Item GREEN_DIAMOND = registerItem("green_diamond", new Item(new FabricItemSettings()));
    public static final Item YELLOW_DIAMOND = registerItem("yellow_diamond", new Item(new FabricItemSettings()));
    public static final Item PURPLE_DIAMOND = registerItem("purple_diamond", new Item(new FabricItemSettings()));
    public static final Item PURPLE_DIAMOND_STICK = registerItem("purple_diamond_stick", new Item(new FabricItemSettings()));
    public static final Item PURPLE_DIAMOND_GEM = registerItem("purple_diamond_gem", new Item(new FabricItemSettings().maxCount(1)));
    public static final Item PINK_DIAMOND = registerItem("pink_diamond", new Item(new FabricItemSettings()));
    public static final Item PINK_DIAMOND_GEM = registerItem("pink_diamond_gem", new Item(new FabricItemSettings().maxCount(1)));

    // ========== 彩虹宝石系列 ==========
    public static final Item RAINBOW_GEM = registerItem("rainbow_gem", new RainbowGemItem(new FabricItemSettings().fireproof()));
    public static final Block RAINBOW_GEM_BLOCK = registerBlock("rainbow_gem_block",
        new RainbowGemBlock(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).strength(50f, 1200f).requiresTool()));
    public static final Item RAINBOW_GEM_BLOCK_ITEM = registerItem("rainbow_gem_block",
        new BlockItem(RAINBOW_GEM_BLOCK, new FabricItemSettings().fireproof()));

    // ========== 彩虹维度方块 ==========
    public static final Block DEPLETED_RAINBOW_BLOCK = registerBlock("depleted_rainbow_block",
        new DepletedRainbowBlock(FabricBlockSettings.copyOf(Blocks.STONE).strength(3f, 6f).requiresTool()));
    public static final Item DEPLETED_RAINBOW_BLOCK_ITEM = registerItem("depleted_rainbow_block",
        new BlockItem(DEPLETED_RAINBOW_BLOCK, new FabricItemSettings()));

    public static final Block RAINBOW_GEM_ORE = registerBlock("rainbow_gem_ore",
        new RainbowGemOreBlock(FabricBlockSettings.copyOf(Blocks.STONE).strength(2f, 3f).requiresTool()));
    public static final Item RAINBOW_GEM_ORE_ITEM = registerItem("rainbow_gem_ore",
        new BlockItem(RAINBOW_GEM_ORE, new FabricItemSettings()));

    public static final Block RAINBOW_CRAFTING_TABLE = registerBlock("rainbow_crafting_table",
        new RainbowCraftingTableBlock(FabricBlockSettings.copyOf(Blocks.CRAFTING_TABLE).strength(2.5f, 6f)));
    public static final Item RAINBOW_CRAFTING_TABLE_ITEM = registerItem("rainbow_crafting_table",
        new BlockItem(RAINBOW_CRAFTING_TABLE, new FabricItemSettings()));

    // ========== 矿石和方块 ==========
    public static final Block COMPRESSED_DIAMOND_BLOCK = registerBlock("compressed_diamond_block",
        new Block(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).strength(5f, 6f).requiresTool()));
    public static final Block GREEN_DIAMOND_ORE = registerBlock("green_diamond_ore",
        new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.DIAMOND_ORE).strength(3f, 3f).requiresTool(), UniformIntProvider.create(3, 7)));
    public static final Block YELLOW_DIAMOND_ORE = registerBlock("yellow_diamond_ore",
        new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.IRON_ORE).strength(3f, 3f).requiresTool(), UniformIntProvider.create(0, 2)));
    public static final Block DEEPSLATE_YELLOW_DIAMOND_ORE = registerBlock("deepslate_yellow_diamond_ore",
        new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_IRON_ORE).strength(4.5f, 3f).requiresTool(), UniformIntProvider.create(0, 2)));
    public static final Block PURPLE_DIAMOND_ORE = registerBlock("purple_diamond_ore",
        new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_DIAMOND_ORE).strength(4.5f, 3f).requiresTool(), UniformIntProvider.create(3, 7)));
    public static final Block PURPLE_DIAMOND_BLOCK = registerBlock("purple_diamond_block",
        new Block(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).strength(5f, 6f).requiresTool()));
    public static final Block PINK_DIAMOND_ORE = registerBlock("pink_diamond_ore",
        new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_DIAMOND_ORE).strength(6f, 6f).requiresTool(), UniformIntProvider.create(4, 8)));
    public static final Block PINK_DIAMOND_BLOCK = registerBlock("pink_diamond_block",
        new Block(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).strength(8f, 12f).requiresTool()));

    public static final Item COMPRESSED_DIAMOND_BLOCK_ITEM = registerItem("compressed_diamond_block", new BlockItem(COMPRESSED_DIAMOND_BLOCK, new FabricItemSettings()));
    public static final Item GREEN_DIAMOND_ORE_ITEM = registerItem("green_diamond_ore", new BlockItem(GREEN_DIAMOND_ORE, new FabricItemSettings()));
    public static final Item YELLOW_DIAMOND_ORE_ITEM = registerItem("yellow_diamond_ore", new BlockItem(YELLOW_DIAMOND_ORE, new FabricItemSettings()));
    public static final Item DEEPSLATE_YELLOW_DIAMOND_ORE_ITEM = registerItem("deepslate_yellow_diamond_ore", new BlockItem(DEEPSLATE_YELLOW_DIAMOND_ORE, new FabricItemSettings()));
    public static final Item PURPLE_DIAMOND_ORE_ITEM = registerItem("purple_diamond_ore", new BlockItem(PURPLE_DIAMOND_ORE, new FabricItemSettings()));
    public static final Item PURPLE_DIAMOND_BLOCK_ITEM = registerItem("purple_diamond_block", new BlockItem(PURPLE_DIAMOND_BLOCK, new FabricItemSettings()));
    public static final Item PINK_DIAMOND_ORE_ITEM = registerItem("pink_diamond_ore", new BlockItem(PINK_DIAMOND_ORE, new FabricItemSettings()));
    public static final Item PINK_DIAMOND_BLOCK_ITEM = registerItem("pink_diamond_block", new BlockItem(PINK_DIAMOND_BLOCK, new FabricItemSettings()));

    // ========== 灾厄系列 ==========
    public static final Item CALAMITY_WIND_ITEM = registerItem("calamity_wind",
        new CalamityWindItem(new FabricItemSettings().fireproof().maxCount(1)));

    public static final Item RAINBOW_KEY = registerItem("rainbow_key",
        new RainbowKeyItem(new FabricItemSettings().fireproof().maxCount(1).maxDamage(2)));

    // ========== 流体 ==========
    public static final RainbowPoisonWater STILL_RAINBOW_POISON_WATER = Registry.register(Registries.FLUID, id("rainbow_poison_water"), new RainbowPoisonWater.Still());
    public static final RainbowPoisonWater FLOWING_RAINBOW_POISON_WATER = Registry.register(Registries.FLUID, id("flowing_rainbow_poison_water"), new RainbowPoisonWater.Flowing());
    public static final Block RAINBOW_POISON_WATER_BLOCK = registerBlock("rainbow_poison_water", new RainbowFluidBlock(STILL_RAINBOW_POISON_WATER, FabricBlockSettings.copyOf(Blocks.WATER)) {
        @Override
        public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
            super.onEntityCollision(state, world, pos, entity);
            if (!world.isClient && entity instanceof LivingEntity living) {
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));
                living.damage(living.getDamageSources().magic(), 5f);
            }
        }
    });
    public static final Item RAINBOW_POISON_WATER_BUCKET = registerItem("rainbow_poison_water_bucket",
        new BucketItem(STILL_RAINBOW_POISON_WATER, new FabricItemSettings().recipeRemainder(Items.BUCKET).maxCount(1)));

    public static final RainbowSeaWater STILL_RAINBOW_SEA_WATER = Registry.register(Registries.FLUID, id("rainbow_sea_water"), new RainbowSeaWater.Still());
    public static final RainbowSeaWater FLOWING_RAINBOW_SEA_WATER = Registry.register(Registries.FLUID, id("flowing_rainbow_sea_water"), new RainbowSeaWater.Flowing());
    public static final Block RAINBOW_SEA_WATER_BLOCK = registerBlock("rainbow_sea_water", new RainbowFluidBlock(STILL_RAINBOW_SEA_WATER, FabricBlockSettings.copyOf(Blocks.WATER)) {
        @Override
        public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
            super.onEntityCollision(state, world, pos, entity);
            if (!world.isClient && entity instanceof LivingEntity living) {
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 2));
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0));
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 0));
            }
        }
    });
    public static final Item RAINBOW_SEA_WATER_BUCKET = registerItem("rainbow_sea_water_bucket",
        new BucketItem(STILL_RAINBOW_SEA_WATER, new FabricItemSettings().recipeRemainder(Items.BUCKET).maxCount(1)));

    public static final DecayedRainbowWater STILL_DECAYED_RAINBOW_WATER = Registry.register(Registries.FLUID, id("decayed_rainbow_water"), new DecayedRainbowWater.Still());
    public static final DecayedRainbowWater FLOWING_DECAYED_RAINBOW_WATER = Registry.register(Registries.FLUID, id("flowing_decayed_rainbow_water"), new DecayedRainbowWater.Flowing());
    public static final Block DECAYED_RAINBOW_WATER_BLOCK = registerBlock("decayed_rainbow_water", new RainbowFluidBlock(STILL_DECAYED_RAINBOW_WATER, FabricBlockSettings.copyOf(Blocks.WATER).luminance(state -> 8)) {
        @Override
        public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
            super.onEntityCollision(state, world, pos, entity);
            if (!world.isClient && entity instanceof LivingEntity living) {
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 0));
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0));
            }
        }
    });
    public static final Item DECAYED_RAINBOW_WATER_BUCKET = registerItem("decayed_rainbow_water_bucket",
        new BucketItem(STILL_DECAYED_RAINBOW_WATER, new FabricItemSettings().recipeRemainder(Items.BUCKET).maxCount(1)));

    // ========== 生物蛋 ==========
    public static final Item RAINBOW_GUARDIAN_SPAWN_EGG = registerItem("rainbow_guardian_spawn_egg",
        new SpawnEggItem(ModEntities.RAINBOW_GUARDIAN, 0xFF0000, 0x00FF00, new FabricItemSettings()));
    public static final Item DEPLETED_RAINBOW_SPAWN_EGG = registerItem("depleted_rainbow_spawn_egg",
        new SpawnEggItem(ModEntities.DEPLETED_RAINBOW, 0x888888, 0x444444, new FabricItemSettings()));
    public static final Item RAINBOW_GHOST_SPAWN_EGG = registerItem("rainbow_ghost_spawn_egg",
        new SpawnEggItem(ModEntities.RAINBOW_GHOST, 0xFFFFFF, 0xAAAAAA, new FabricItemSettings()));
    public static final Item CALAMITY_WRAITH_SPAWN_EGG = registerItem("calamity_wraith_spawn_egg",
        new SpawnEggItem(ModEntities.CALAMITY_WRAITH, 0x333333, 0x666666, new FabricItemSettings()));
    public static final Item CALAMITY_PHANTOM_SPAWN_EGG = registerItem("calamity_phantom_spawn_egg",
        new SpawnEggItem(ModEntities.CALAMITY_PHANTOM, 0x555555, 0x777777, new FabricItemSettings()));
    public static final Item CALAMITY_WIND_SPAWN_EGG = registerItem("calamity_wind_spawn_egg",
        new SpawnEggItem(ModEntities.CALAMITY_WIND, 0x00AAFF, 0x00FFAA, new FabricItemSettings()));
    public static final Item CALAMITY_SOLDIER_SPAWN_EGG = registerItem("calamity_soldier_spawn_egg",
        new SpawnEggItem(ModEntities.CALAMITY_SOLDIER, 0xAA00AA, 0xFF00FF, new FabricItemSettings()));
    public static final Item DECAYED_RAINBOW_MONSTER_SPAWN_EGG = registerItem("decayed_rainbow_monster_spawn_egg",
        new SpawnEggItem(ModEntities.DECAYED_RAINBOW_MONSTER, 0x660066, 0x990099, new FabricItemSettings()));

    // ========== 反作弊器 ==========

    public static void registerModItems() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(GOLD_STICK);
            entries.add(COMPRESSED_DIAMOND);
            entries.add(GREEN_DIAMOND);
            entries.add(YELLOW_DIAMOND);
            entries.add(PURPLE_DIAMOND);
            entries.add(PURPLE_DIAMOND_STICK);
            entries.add(PURPLE_DIAMOND_GEM);
            entries.add(PINK_DIAMOND);
            entries.add(PINK_DIAMOND_GEM);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(COMPRESSED_DIAMOND_BLOCK_ITEM);
            entries.add(PURPLE_DIAMOND_BLOCK_ITEM);
            entries.add(PINK_DIAMOND_BLOCK_ITEM);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(entries -> {
            entries.add(GREEN_DIAMOND_ORE_ITEM);
            entries.add(YELLOW_DIAMOND_ORE_ITEM);
            entries.add(DEEPSLATE_YELLOW_DIAMOND_ORE_ITEM);
            entries.add(PURPLE_DIAMOND_ORE_ITEM);
            entries.add(PINK_DIAMOND_ORE_ITEM);
        });
    }
}
