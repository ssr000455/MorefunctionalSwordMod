package com.qidai.morefunctionalswordmod;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModTools {
    // ==================== 绿钻 ====================
    public static final Item GREEN_DIAMOND_SWORD = registerItem("green_diamond_sword",
        new DiamondToolItem.DiamondSword(DiamondType.GREEN, 12, -2.4f, new FabricItemSettings()));
    public static final Item GREEN_DIAMOND_PICKAXE = registerItem("green_diamond_pickaxe",
        new DiamondToolItem.DiamondPickaxe(DiamondType.GREEN, 1, -2.8f, new FabricItemSettings()));
    public static final Item GREEN_DIAMOND_AXE = registerItem("green_diamond_axe",
        new DiamondToolItem.DiamondAxe(DiamondType.GREEN, 5.0f, -3.0f, new FabricItemSettings()));
    public static final Item GREEN_DIAMOND_SHOVEL = registerItem("green_diamond_shovel",
        new DiamondToolItem.DiamondShovel(DiamondType.GREEN, 1.5f, -3.0f, new FabricItemSettings()));
    public static final Item GREEN_DIAMOND_HOE = registerItem("green_diamond_hoe",
        new DiamondToolItem.DiamondHoe(DiamondType.GREEN, -3, 0.0f, new FabricItemSettings()));
    public static final Item GREEN_DIAMOND_HELMET = registerItem("green_diamond_helmet",
        new DiamondToolItem.DiamondHelmet(DiamondType.GREEN, DiamondMaterials.Type.GREEN.asArmorMaterial(), ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item GREEN_DIAMOND_CHESTPLATE = registerItem("green_diamond_chestplate",
        new DiamondToolItem.DiamondChestplate(DiamondType.GREEN, DiamondMaterials.Type.GREEN.asArmorMaterial(), ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
    public static final Item GREEN_DIAMOND_LEGGINGS = registerItem("green_diamond_leggings",
        new DiamondToolItem.DiamondLeggings(DiamondType.GREEN, DiamondMaterials.Type.GREEN.asArmorMaterial(), ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
    public static final Item GREEN_DIAMOND_BOOTS = registerItem("green_diamond_boots",
        new DiamondToolItem.DiamondBoots(DiamondType.GREEN, DiamondMaterials.Type.GREEN.asArmorMaterial(), ArmorItem.Type.BOOTS, new FabricItemSettings()));

    // ==================== 黄钻 ====================
    public static final Item YELLOW_DIAMOND_SWORD = registerItem("yellow_diamond_sword",
        new DiamondToolItem.DiamondSword(DiamondType.YELLOW, 9, -2.4f, new FabricItemSettings()));
    public static final Item YELLOW_DIAMOND_PICKAXE = registerItem("yellow_diamond_pickaxe",
        new DiamondToolItem.DiamondPickaxe(DiamondType.YELLOW, 1, -2.8f, new FabricItemSettings()));
    public static final Item YELLOW_DIAMOND_AXE = registerItem("yellow_diamond_axe",
        new DiamondToolItem.DiamondAxe(DiamondType.YELLOW, 5.0f, -3.0f, new FabricItemSettings()));
    public static final Item YELLOW_DIAMOND_SHOVEL = registerItem("yellow_diamond_shovel",
        new DiamondToolItem.DiamondShovel(DiamondType.YELLOW, 1.5f, -3.0f, new FabricItemSettings()));
    public static final Item YELLOW_DIAMOND_HOE = registerItem("yellow_diamond_hoe",
        new DiamondToolItem.DiamondHoe(DiamondType.YELLOW, -3, 0.0f, new FabricItemSettings()));
    public static final Item YELLOW_DIAMOND_HELMET = registerItem("yellow_diamond_helmet",
        new DiamondToolItem.DiamondHelmet(DiamondType.YELLOW, DiamondMaterials.Type.YELLOW.asArmorMaterial(), ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item YELLOW_DIAMOND_CHESTPLATE = registerItem("yellow_diamond_chestplate",
        new DiamondToolItem.DiamondChestplate(DiamondType.YELLOW, DiamondMaterials.Type.YELLOW.asArmorMaterial(), ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
    public static final Item YELLOW_DIAMOND_LEGGINGS = registerItem("yellow_diamond_leggings",
        new DiamondToolItem.DiamondLeggings(DiamondType.YELLOW, DiamondMaterials.Type.YELLOW.asArmorMaterial(), ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
    public static final Item YELLOW_DIAMOND_BOOTS = registerItem("yellow_diamond_boots",
        new DiamondToolItem.DiamondBoots(DiamondType.YELLOW, DiamondMaterials.Type.YELLOW.asArmorMaterial(), ArmorItem.Type.BOOTS, new FabricItemSettings()));

    // ==================== 紫钻 ====================
    public static final Item PURPLE_DIAMOND_SWORD = registerItem("purple_diamond_sword",
        new DiamondToolItem.DiamondSword(DiamondType.PURPLE, 21, -2.4f, new FabricItemSettings()));
    public static final Item PURPLE_DIAMOND_PICKAXE = registerItem("purple_diamond_pickaxe",
        new DiamondToolItem.DiamondPickaxe(DiamondType.PURPLE, 1, -2.8f, new FabricItemSettings()));
    public static final Item PURPLE_DIAMOND_AXE = registerItem("purple_diamond_axe",
        new DiamondToolItem.DiamondAxe(DiamondType.PURPLE, 5.0f, -3.0f, new FabricItemSettings()));
    public static final Item PURPLE_DIAMOND_SHOVEL = registerItem("purple_diamond_shovel",
        new DiamondToolItem.DiamondShovel(DiamondType.PURPLE, 1.5f, -3.0f, new FabricItemSettings()));
    public static final Item PURPLE_DIAMOND_HOE = registerItem("purple_diamond_hoe",
        new DiamondToolItem.DiamondHoe(DiamondType.PURPLE, -3, 0.0f, new FabricItemSettings()));
    public static final Item PURPLE_DIAMOND_HELMET = registerItem("purple_diamond_helmet",
        new DiamondToolItem.DiamondHelmet(DiamondType.PURPLE, DiamondMaterials.Type.PURPLE.asArmorMaterial(), ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item PURPLE_DIAMOND_CHESTPLATE = registerItem("purple_diamond_chestplate",
        new DiamondToolItem.DiamondChestplate(DiamondType.PURPLE, DiamondMaterials.Type.PURPLE.asArmorMaterial(), ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
    public static final Item PURPLE_DIAMOND_LEGGINGS = registerItem("purple_diamond_leggings",
        new DiamondToolItem.DiamondLeggings(DiamondType.PURPLE, DiamondMaterials.Type.PURPLE.asArmorMaterial(), ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
    public static final Item PURPLE_DIAMOND_BOOTS = registerItem("purple_diamond_boots",
        new DiamondToolItem.DiamondBoots(DiamondType.PURPLE, DiamondMaterials.Type.PURPLE.asArmorMaterial(), ArmorItem.Type.BOOTS, new FabricItemSettings()));

    // ==================== 粉钻 ====================
    public static final Item PINK_DIAMOND_SWORD = registerItem("pink_diamond_sword",
        new DiamondToolItem.DiamondSword(DiamondType.PINK, 48, -2.4f, new FabricItemSettings()));
    public static final Item PINK_DIAMOND_PICKAXE = registerItem("pink_diamond_pickaxe",
        new DiamondToolItem.DiamondPickaxe(DiamondType.PINK, 1, -2.8f, new FabricItemSettings()));
    public static final Item PINK_DIAMOND_AXE = registerItem("pink_diamond_axe",
        new DiamondToolItem.DiamondAxe(DiamondType.PINK, 5.0f, -3.0f, new FabricItemSettings()));
    public static final Item PINK_DIAMOND_SHOVEL = registerItem("pink_diamond_shovel",
        new DiamondToolItem.DiamondShovel(DiamondType.PINK, 1.5f, -3.0f, new FabricItemSettings()));
    public static final Item PINK_DIAMOND_HOE = registerItem("pink_diamond_hoe",
        new DiamondToolItem.DiamondHoe(DiamondType.PINK, -3, 0.0f, new FabricItemSettings()));
    public static final Item PINK_DIAMOND_HELMET = registerItem("pink_diamond_helmet",
        new DiamondToolItem.DiamondHelmet(DiamondType.PINK, DiamondMaterials.Type.PINK.asArmorMaterial(), ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item PINK_DIAMOND_CHESTPLATE = registerItem("pink_diamond_chestplate",
        new DiamondToolItem.DiamondChestplate(DiamondType.PINK, DiamondMaterials.Type.PINK.asArmorMaterial(), ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
    public static final Item PINK_DIAMOND_LEGGINGS = registerItem("pink_diamond_leggings",
        new DiamondToolItem.DiamondLeggings(DiamondType.PINK, DiamondMaterials.Type.PINK.asArmorMaterial(), ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
    public static final Item PINK_DIAMOND_BOOTS = registerItem("pink_diamond_boots",
        new DiamondToolItem.DiamondBoots(DiamondType.PINK, DiamondMaterials.Type.PINK.asArmorMaterial(), ArmorItem.Type.BOOTS, new FabricItemSettings()));

    // ==================== 粉钻弓等特殊物品 ====================
    public static final Item PINK_DIAMOND_BOW = registerItem("pink_diamond_bow",
        new PinkDiamondBowItem(new FabricItemSettings().maxDamage(1000)));
    public static final Item REINFORCED_PINK_DIAMOND_SWORD = registerItem("reinforced_pink_diamond_sword",
        new ReinforcedPinkDiamondSwordItem(DiamondMaterials.Type.PINK.asToolMaterial(), 116, -2.4f, new FabricItemSettings()));
    public static final Item ULTRA_PINK_DIAMOND_SWORD = registerItem("ultra_pink_diamond_sword",
        new UltraPinkDiamondSwordItem(UltraPinkDiamondToolMaterial.INSTANCE, 0, -2.4f, new FabricItemSettings().fireproof()));

    // ==================== 七彩神剑 ====================
    public static final Item RAINBOW_SWORD = registerItem("rainbow_sword",
        new RainbowSwordItem(RainbowSwordToolMaterial.INSTANCE, 0, -2.4f, new FabricItemSettings().fireproof().maxCount(1)));

    // ==================== 彩虹宝石系列 ====================
    public static final Item RAINBOW_GEM_SWORD = registerItem("rainbow_gem_sword",
        new RainbowGemItems.GemSword(RainbowGemToolMaterial.INSTANCE, 0, -2.4f, new FabricItemSettings()));
    public static final Item RAINBOW_GEM_PICKAXE = registerItem("rainbow_gem_pickaxe",
        new RainbowGemItems.GemPickaxe(RainbowGemToolMaterial.INSTANCE, 1, -2.8f, new FabricItemSettings()));
    public static final Item RAINBOW_GEM_AXE = registerItem("rainbow_gem_axe",
        new RainbowGemItems.GemAxe(RainbowGemToolMaterial.INSTANCE, 5.0f, -3.0f, new FabricItemSettings()));
    public static final Item RAINBOW_GEM_SHOVEL = registerItem("rainbow_gem_shovel",
        new RainbowGemItems.GemShovel(RainbowGemToolMaterial.INSTANCE, 1.5f, -3.0f, new FabricItemSettings()));
    public static final Item RAINBOW_GEM_HOE = registerItem("rainbow_gem_hoe",
        new RainbowGemItems.GemHoe(RainbowGemToolMaterial.INSTANCE, -3, 0.0f, new FabricItemSettings()));
    public static final Item RAINBOW_GEM_BOW = registerItem("rainbow_gem_bow",
        new RainbowGemItems.GemBow(new FabricItemSettings()));

    public static final Item RAINBOW_GEM_HELMET = registerItem("rainbow_gem_helmet",
        new RainbowGemItems.GemHelmet(RainbowGemArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item RAINBOW_GEM_CHESTPLATE = registerItem("rainbow_gem_chestplate",
        new RainbowGemItems.GemChestplate(RainbowGemArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
    public static final Item RAINBOW_GEM_LEGGINGS = registerItem("rainbow_gem_leggings",
        new RainbowGemItems.GemLeggings(RainbowGemArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
    public static final Item RAINBOW_GEM_BOOTS = registerItem("rainbow_gem_boots",
        new RainbowGemItems.GemBoots(RainbowGemArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(ModRegistry.MOD_ID, name), item);
    }

    public static void registerModTools() {}
}
