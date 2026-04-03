package com.qidai.morefunctionalswordmod;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;

public class PinkDiamondBowItem extends BowItem {
    public PinkDiamondBowItem(Settings settings) {
        super(settings);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity player) {
            boolean creative = player.getAbilities().creativeMode;
            ItemStack arrowStack = player.getProjectileType(stack);
            if (!arrowStack.isEmpty() || creative) {
                if (arrowStack.isEmpty()) {
                    arrowStack = new ItemStack(Items.ARROW);
                }

                int pullTime = this.getMaxUseTime(stack) - remainingUseTicks;
                float pullProgress = getPullProgress(pullTime);
                if (pullProgress >= 0.1) {
                    if (!world.isClient) {
                        ArrowItem arrowItem = (ArrowItem)(arrowStack.getItem() instanceof ArrowItem ? arrowStack.getItem() : Items.ARROW);
                        PersistentProjectileEntity arrow = arrowItem.createArrow(world, arrowStack, player);
                        arrow.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, pullProgress * 4.5F, 1.0F);
                        arrow.setDamage(arrow.getDamage() * 2.0);
                        if (pullProgress >= 1.0F) {
                            arrow.setCritical(true);
                        }
                        int fireAspect = EnchantmentHelper.getLevel(Enchantments.FIRE_ASPECT, stack);
                        if (fireAspect > 0) {
                            arrow.setOnFireFor(100 * fireAspect);
                        }
                        world.spawnEntity(arrow);
                    }

                    world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + pullProgress * 0.5F);

                    if (!creative) {
                        arrowStack.decrement(1);
                        if (arrowStack.isEmpty()) {
                            player.getInventory().removeOne(arrowStack);
                        }
                    }
                    player.incrementStat(Stats.USED.getOrCreateStat(this));
                }
            }
        }
    }
}
