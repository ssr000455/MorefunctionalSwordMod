package com.qidai.morefunctionalswordmod;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;

public class ReinforcedPinkDiamondSwordItem extends SwordItem {
    public ReinforcedPinkDiamondSwordItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.getWorld().isClient && attacker instanceof PlayerEntity) {
            // 50% 概率造成双倍伤害
            if (attacker.getRandom().nextFloat() < 0.5f) {
                target.damage(attacker.getDamageSources().playerAttack((PlayerEntity)attacker), this.getAttackDamage() * 2);
            }
        }
        return super.postHit(stack, target, attacker);
    }
}
