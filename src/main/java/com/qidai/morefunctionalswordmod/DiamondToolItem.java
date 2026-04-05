package com.qidai.morefunctionalswordmod;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;

public class DiamondToolItem {

    public static class DiamondSword extends SwordItem {
        private final DiamondType type;
        public DiamondSword(DiamondType type, int attackDamage, float attackSpeed, Settings settings) {
            super(type.asToolMaterial(), attackDamage, attackSpeed, settings);
            this.type = type;
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) applySwordEnchants(stack, type);
        }
        @Override
        public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player && selected) {
                for (StatusEffectInstance effect : type.heldEffects) {
                    if (effect != null) player.addStatusEffect(new StatusEffectInstance(effect));
                }
            }
        }
    }

    public static class DiamondPickaxe extends PickaxeItem {
        private final DiamondType type;
        public DiamondPickaxe(DiamondType type, int attackDamage, float attackSpeed, Settings settings) {
            super(type.asToolMaterial(), attackDamage, attackSpeed, settings);
            this.type = type;
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) applyPickaxeEnchants(stack, type);
        }
        @Override
        public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player && selected) {
                for (StatusEffectInstance effect : type.heldEffects) {
                    if (effect != null) player.addStatusEffect(new StatusEffectInstance(effect));
                }
            }
        }
    }

    public static class DiamondAxe extends AxeItem {
        private final DiamondType type;
        public DiamondAxe(DiamondType type, float attackDamage, float attackSpeed, Settings settings) {
            super(type.asToolMaterial(), attackDamage, attackSpeed, settings);
            this.type = type;
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) applyAxeEnchants(stack, type);
        }
        @Override
        public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player && selected) {
                for (StatusEffectInstance effect : type.heldEffects) {
                    if (effect != null) player.addStatusEffect(new StatusEffectInstance(effect));
                }
            }
        }
    }

    public static class DiamondShovel extends ShovelItem {
        private final DiamondType type;
        public DiamondShovel(DiamondType type, float attackDamage, float attackSpeed, Settings settings) {
            super(type.asToolMaterial(), attackDamage, attackSpeed, settings);
            this.type = type;
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) applyShovelEnchants(stack, type);
        }
        @Override
        public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player && selected) {
                for (StatusEffectInstance effect : type.heldEffects) {
                    if (effect != null) player.addStatusEffect(new StatusEffectInstance(effect));
                }
            }
        }
    }

    public static class DiamondHoe extends HoeItem {
        private final DiamondType type;
        public DiamondHoe(DiamondType type, int attackDamage, float attackSpeed, Settings settings) {
            super(type.asToolMaterial(), attackDamage, attackSpeed, settings);
            this.type = type;
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) applyHoeEnchants(stack, type);
        }
        @Override
        public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player && selected) {
                for (StatusEffectInstance effect : type.heldEffects) {
                    if (effect != null) player.addStatusEffect(new StatusEffectInstance(effect));
                }
            }
        }
    }

    public static class DiamondHelmet extends ArmorItem {
        private final DiamondType type;
        public DiamondHelmet(DiamondType type, ArmorMaterial material, ArmorItem.Type slot, Settings settings) {
            super(material, slot, settings);
            this.type = type;
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) applyHelmetEnchants(stack, type);
        }
        @Override
        public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player) {
                for (StatusEffectInstance effect : type.armorEffects) {
                    if (effect != null) player.addStatusEffect(new StatusEffectInstance(effect));
                }
            }
        }
    }

    public static class DiamondChestplate extends ArmorItem {
        private final DiamondType type;
        public DiamondChestplate(DiamondType type, ArmorMaterial material, ArmorItem.Type slot, Settings settings) {
            super(material, slot, settings);
            this.type = type;
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) applyChestplateEnchants(stack, type);
        }
        @Override
        public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player) {
                for (StatusEffectInstance effect : type.armorEffects) {
                    if (effect != null) player.addStatusEffect(new StatusEffectInstance(effect));
                }
            }
        }
    }

    public static class DiamondLeggings extends ArmorItem {
        private final DiamondType type;
        public DiamondLeggings(DiamondType type, ArmorMaterial material, ArmorItem.Type slot, Settings settings) {
            super(material, slot, settings);
            this.type = type;
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) applyLeggingsEnchants(stack, type);
        }
        @Override
        public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player) {
                for (StatusEffectInstance effect : type.armorEffects) {
                    if (effect != null) player.addStatusEffect(new StatusEffectInstance(effect));
                }
            }
        }
    }

    public static class DiamondBoots extends ArmorItem {
        private final DiamondType type;
        public DiamondBoots(DiamondType type, ArmorMaterial material, ArmorItem.Type slot, Settings settings) {
            super(material, slot, settings);
            this.type = type;
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) applyBootsEnchants(stack, type);
        }
        @Override
        public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player) {
                for (StatusEffectInstance effect : type.armorEffects) {
                    if (effect != null) player.addStatusEffect(new StatusEffectInstance(effect));
                }
            }
        }
    }

    // 附魔方法（略，与之前相同）
    private static void applySwordEnchants(ItemStack stack, DiamondType type) {
        switch (type) {
            case GREEN:
                stack.addEnchantment(Enchantments.SHARPNESS, 3);
                stack.addEnchantment(Enchantments.SMITE, 1);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                stack.addEnchantment(Enchantments.LUCK_OF_THE_SEA, 1);
                break;
            case YELLOW:
                stack.addEnchantment(Enchantments.SHARPNESS, 1);
                stack.addEnchantment(Enchantments.SMITE, 1);
                stack.addEnchantment(Enchantments.FIRE_ASPECT, 1);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                break;
            case PURPLE:
                stack.addEnchantment(Enchantments.SHARPNESS, 5);
                stack.addEnchantment(Enchantments.SMITE, 1);
                stack.addEnchantment(Enchantments.BANE_OF_ARTHROPODS, 1);
                stack.addEnchantment(Enchantments.FIRE_ASPECT, 2);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.LOOTING, 2);
                break;
            case PINK:
                stack.addEnchantment(Enchantments.SHARPNESS, 5);
                stack.addEnchantment(Enchantments.SMITE, 5);
                stack.addEnchantment(Enchantments.FIRE_ASPECT, 2);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.LOOTING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
                break;
        }
    }

    private static void applyPickaxeEnchants(ItemStack stack, DiamondType type) {
        switch (type) {
            case GREEN:
                stack.addEnchantment(Enchantments.EFFICIENCY, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                stack.addEnchantment(Enchantments.FORTUNE, 1);
                break;
            case YELLOW:
                stack.addEnchantment(Enchantments.EFFICIENCY, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                break;
            case PURPLE:
                stack.addEnchantment(Enchantments.EFFICIENCY, 4);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.FORTUNE, 3);
                break;
            case PINK:
                stack.addEnchantment(Enchantments.EFFICIENCY, 5);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.FORTUNE, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
                break;
        }
    }

    private static void applyAxeEnchants(ItemStack stack, DiamondType type) {
        switch (type) {
            case GREEN:
                stack.addEnchantment(Enchantments.EFFICIENCY, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                stack.addEnchantment(Enchantments.SHARPNESS, 2);
                break;
            case YELLOW:
                stack.addEnchantment(Enchantments.EFFICIENCY, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                break;
            case PURPLE:
                stack.addEnchantment(Enchantments.EFFICIENCY, 4);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.SHARPNESS, 3);
                break;
            case PINK:
                stack.addEnchantment(Enchantments.EFFICIENCY, 5);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.SHARPNESS, 5);
                stack.addEnchantment(Enchantments.MENDING, 1);
                break;
        }
    }

    private static void applyShovelEnchants(ItemStack stack, DiamondType type) {
        switch (type) {
            case GREEN:
                stack.addEnchantment(Enchantments.EFFICIENCY, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                stack.addEnchantment(Enchantments.SILK_TOUCH, 1);
                break;
            case YELLOW:
                stack.addEnchantment(Enchantments.EFFICIENCY, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                break;
            case PURPLE:
                stack.addEnchantment(Enchantments.EFFICIENCY, 4);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                break;
            case PINK:
                stack.addEnchantment(Enchantments.EFFICIENCY, 5);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.SILK_TOUCH, 1);
                stack.addEnchantment(Enchantments.MENDING, 1);
                break;
        }
    }

    private static void applyHoeEnchants(ItemStack stack, DiamondType type) {
        switch (type) {
            case GREEN:
                stack.addEnchantment(Enchantments.EFFICIENCY, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                break;
            case YELLOW:
                stack.addEnchantment(Enchantments.EFFICIENCY, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                break;
            case PURPLE:
                stack.addEnchantment(Enchantments.EFFICIENCY, 4);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                break;
            case PINK:
                stack.addEnchantment(Enchantments.EFFICIENCY, 5);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
                break;
        }
    }

    private static void applyHelmetEnchants(ItemStack stack, DiamondType type) {
        switch (type) {
            case GREEN:
                stack.addEnchantment(Enchantments.PROTECTION, 2);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                stack.addEnchantment(Enchantments.RESPIRATION, 1);
                break;
            case YELLOW:
                stack.addEnchantment(Enchantments.PROTECTION, 1);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                break;
            case PURPLE:
                stack.addEnchantment(Enchantments.PROTECTION, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.RESPIRATION, 2);
                break;
            case PINK:
                stack.addEnchantment(Enchantments.PROTECTION, 4);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
                break;
        }
    }

    private static void applyChestplateEnchants(ItemStack stack, DiamondType type) {
        switch (type) {
            case GREEN:
                stack.addEnchantment(Enchantments.PROTECTION, 2);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                break;
            case YELLOW:
                stack.addEnchantment(Enchantments.PROTECTION, 1);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                break;
            case PURPLE:
                stack.addEnchantment(Enchantments.PROTECTION, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                break;
            case PINK:
                stack.addEnchantment(Enchantments.PROTECTION, 4);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
                break;
        }
    }

    private static void applyLeggingsEnchants(ItemStack stack, DiamondType type) {
        switch (type) {
            case GREEN:
                stack.addEnchantment(Enchantments.PROTECTION, 2);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                break;
            case YELLOW:
                stack.addEnchantment(Enchantments.PROTECTION, 1);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                break;
            case PURPLE:
                stack.addEnchantment(Enchantments.PROTECTION, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                break;
            case PINK:
                stack.addEnchantment(Enchantments.PROTECTION, 4);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
                break;
        }
    }

    private static void applyBootsEnchants(ItemStack stack, DiamondType type) {
        switch (type) {
            case GREEN:
                stack.addEnchantment(Enchantments.PROTECTION, 2);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                stack.addEnchantment(Enchantments.FEATHER_FALLING, 2);
                break;
            case YELLOW:
                stack.addEnchantment(Enchantments.PROTECTION, 1);
                stack.addEnchantment(Enchantments.UNBREAKING, 1);
                stack.addEnchantment(Enchantments.FEATHER_FALLING, 1);
                break;
            case PURPLE:
                stack.addEnchantment(Enchantments.PROTECTION, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.FEATHER_FALLING, 3);
                break;
            case PINK:
                stack.addEnchantment(Enchantments.PROTECTION, 4);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.FEATHER_FALLING, 4);
                stack.addEnchantment(Enchantments.MENDING, 1);
                break;
        }
    }
}
