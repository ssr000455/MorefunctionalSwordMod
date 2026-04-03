package com.qidai.morefunctionalswordmod;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;

public class RainbowGemItems {

    public static class GemSword extends SwordItem {
        public GemSword(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
            super(material, attackDamage, attackSpeed, settings.fireproof());
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) {
                stack.addEnchantment(Enchantments.SHARPNESS, 3);
                stack.addEnchantment(Enchantments.FIRE_ASPECT, 2);
                stack.addEnchantment(Enchantments.LOOTING, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
            }
        }
        @Override
        public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            target.setOnFireFor(5);
            return super.postHit(stack, target, attacker);
        }
    }

    public static class GemPickaxe extends PickaxeItem {
        public GemPickaxe(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
            super(material, attackDamage, attackSpeed, settings.fireproof());
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) {
                stack.addEnchantment(Enchantments.EFFICIENCY, 3);
                stack.addEnchantment(Enchantments.FORTUNE, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
            }
        }
    }

    public static class GemAxe extends AxeItem {
        public GemAxe(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
            super(material, attackDamage, attackSpeed, settings.fireproof());
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) {
                stack.addEnchantment(Enchantments.EFFICIENCY, 3);
                stack.addEnchantment(Enchantments.SHARPNESS, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
            }
        }
    }

    public static class GemShovel extends ShovelItem {
        public GemShovel(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
            super(material, attackDamage, attackSpeed, settings.fireproof());
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) {
                stack.addEnchantment(Enchantments.EFFICIENCY, 3);
                stack.addEnchantment(Enchantments.SILK_TOUCH, 1);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
            }
        }
    }

    public static class GemHoe extends HoeItem {
        public GemHoe(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
            super(material, attackDamage, attackSpeed, settings.fireproof());
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) {
                stack.addEnchantment(Enchantments.EFFICIENCY, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
            }
        }
    }

    public static class GemBow extends BowItem {
        public GemBow(Settings settings) {
            super(settings.fireproof().maxDamage(8000));
        }
        @Override
        public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
            if (user instanceof PlayerEntity player) {
                boolean creative = player.getAbilities().creativeMode;
                ItemStack arrowStack = player.getProjectileType(stack);
                if (!arrowStack.isEmpty() || creative) {
                    if (arrowStack.isEmpty()) arrowStack = new ItemStack(Items.ARROW);
                    int pullTime = this.getMaxUseTime(stack) - remainingUseTicks;
                    float pullProgress = getPullProgress(pullTime);
                    if (pullProgress >= 0.1) {
                        if (!world.isClient) {
                            ArrowItem arrowItem = (ArrowItem)(arrowStack.getItem() instanceof ArrowItem ? arrowStack.getItem() : Items.ARROW);
                            PersistentProjectileEntity arrow = arrowItem.createArrow(world, arrowStack, player);
                            arrow.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, pullProgress * 6.0F, 1.0F);
                            arrow.setDamage(arrow.getDamage() * 3.0);
                            if (pullProgress >= 1.0F) arrow.setCritical(true);
                            world.spawnEntity(arrow);
                        }
                        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            net.minecraft.sound.SoundEvents.ENTITY_ARROW_SHOOT,
                            net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
                        if (!creative) {
                            arrowStack.decrement(1);
                            if (arrowStack.isEmpty()) player.getInventory().removeOne(arrowStack);
                        }
                        player.incrementStat(net.minecraft.stat.Stats.USED.getOrCreateStat(this));
                    }
                }
            }
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) {
                stack.addEnchantment(Enchantments.POWER, 3);
                stack.addEnchantment(Enchantments.FLAME, 1);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
            }
        }
    }

    public static class GemHelmet extends ArmorItem {
        public GemHelmet(ArmorMaterial material, Type type, Settings settings) {
            super(material, type, settings.fireproof());
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) {
                stack.addEnchantment(Enchantments.PROTECTION, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
            }
        }
        @Override
        public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 220, 2, true, false, true));
            }
        }
    }

    public static class GemChestplate extends ArmorItem {
        public GemChestplate(ArmorMaterial material, Type type, Settings settings) {
            super(material, type, settings.fireproof());
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) {
                stack.addEnchantment(Enchantments.PROTECTION, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
            }
        }
        @Override
        public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 220, 2, true, false, true));
            }
        }
    }

    public static class GemLeggings extends ArmorItem {
        public GemLeggings(ArmorMaterial material, Type type, Settings settings) {
            super(material, type, settings.fireproof());
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) {
                stack.addEnchantment(Enchantments.PROTECTION, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
            }
        }
        @Override
        public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 220, 2, true, false, true));
            }
        }
    }

    public static class GemBoots extends ArmorItem {
        public GemBoots(ArmorMaterial material, Type type, Settings settings) {
            super(material, type, settings.fireproof());
        }
        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient) {
                stack.addEnchantment(Enchantments.PROTECTION, 3);
                stack.addEnchantment(Enchantments.FEATHER_FALLING, 3);
                stack.addEnchantment(Enchantments.UNBREAKING, 3);
                stack.addEnchantment(Enchantments.MENDING, 1);
            }
        }
        @Override
        public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 220, 2, true, false, true));
            }
        }
    }
}
