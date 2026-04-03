package com.qidai.morefunctionalswordmod.item;

import com.qidai.morefunctionalswordmod.ModEntities;
import com.qidai.morefunctionalswordmod.entity.calamity.CalamitySoldier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CalamityWindItem extends Item {
    public CalamityWindItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(stack);

        NbtCompound nbt = stack.getOrCreateNbt();
        int uses = nbt.getInt("Uses");
        if (uses >= 20) {
            stack.setCount(0);
            return TypedActionResult.consume(stack);
        }

        long lastUse = nbt.getLong("LastUse");
        long time = world.getTime();
        if (time - lastUse < 600) {
            return TypedActionResult.fail(stack);
        }

        CalamitySoldier soldier = new CalamitySoldier(ModEntities.CALAMITY_SOLDIER, world);
        soldier.setPosition(user.getX(), user.getY(), user.getZ());
        ((ServerWorld) world).spawnEntityAndPassengers(soldier);

        nbt.putLong("LastUse", time);
        nbt.putInt("Uses", uses + 1);
        stack.setNbt(nbt);

        return TypedActionResult.success(stack);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
