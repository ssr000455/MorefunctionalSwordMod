package com.qidai.morefunctionalswordmod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class RainbowSwordItem extends SwordItem {
    private static final UUID SPEED_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    public RainbowSwordItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    private static float getHueOffset() {
        return (System.currentTimeMillis() % 3600) / 3600.0f * 360f;
    }

    private static MutableText rainbowText(String text, float offsetHue) {
        MutableText result = Text.literal("");
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char ch = text.charAt(i);
            float hue = (offsetHue + i * 360f / len) % 360f;
            int rgb = java.awt.Color.HSBtoRGB(hue / 360f, 1.0f, 1.0f);
            MutableText part = Text.literal(String.valueOf(ch));
            part.setStyle(part.getStyle().withColor(TextColor.fromRgb(rgb)));
            result.append(part);
        }
        return result;
    }

    @Override
    public Text getName(ItemStack stack) {
        return rainbowText("七彩神剑", getHueOffset());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getOrCreateNbt();
        float offset = getHueOffset();

        tooltip.add(rainbowText("七彩神光", offset));
        float displayDamage = nbt.contains("BaseDamage") ? nbt.getFloat("BaseDamage") : 999999f;
        if (Float.isInfinite(displayDamage)) {
            tooltip.add(rainbowText("infinity+", offset));
        } else {
            tooltip.add(Text.literal("伤害：" + (int) displayDamage).formatted(Formatting.AQUA));
        }

        tooltip.add(Text.literal("当前模式：" + (nbt.getInt("AttackMode") == 0 ? "安全模式" : nbt.getInt("AttackMode") == 1 ? "范围攻击模式" : nbt.getInt("AttackMode") == 2 ? "无限制模式" : "攻击模式")).formatted(Formatting.WHITE));
        tooltip.add(Text.literal("毁灭模式：" + (nbt.getBoolean("DestructMode") ? "开启" : "关闭")).formatted(nbt.getBoolean("DestructMode") ? Formatting.AQUA : Formatting.GRAY));
        tooltip.add(Text.literal("特殊攻击：" + (nbt.getBoolean("SpecialAttackMode") ? "开启" : "关闭")).formatted(nbt.getBoolean("SpecialAttackMode") ? Formatting.LIGHT_PURPLE : Formatting.GRAY));
        tooltip.add(Text.literal("冰冻模式：" + (nbt.getBoolean("FreezeMode") ? "开启" : "关闭")).formatted(nbt.getBoolean("FreezeMode") ? Formatting.AQUA : Formatting.GRAY));
        tooltip.add(Text.literal("治疗模式：" + (nbt.getBoolean("HealMode") ? "开启" : "关闭")).formatted(nbt.getBoolean("HealMode") ? Formatting.GREEN : Formatting.GRAY));
        tooltip.add(Text.literal("剑气模式：" + (nbt.getBoolean("SwordWaveMode") ? "开启" : "关闭")).formatted(nbt.getBoolean("SwordWaveMode") ? Formatting.YELLOW : Formatting.GRAY));
    }

    // ========== 以下为原有功能代码，无改动 ==========
    private static class AttackConfig {
        int currentMode;
        boolean modifyNbt;
        boolean removeEntity;
        boolean removeEntityData;
        boolean rangeAttack;
        int attackRange;
        boolean fieldReflection;
        float baseDamage;
        boolean continuousAttack;
        int continuousAttackTime;
        boolean lightningAttack;
        int lightningCount;
        boolean fireAttack;
        boolean explosionAttack;
        int explosionRadius;
        boolean allowFlight;
        boolean immuneDamage;
        boolean verifyProtection;
        boolean attackProtection;
        int playerSpeed;
        int maxHealth;
        boolean memoryFieldProtection;
        boolean expAbsorption;
        boolean freezeMode;
        boolean healMode;
        int healRange;
        boolean swordWaveMode;
        int swordWaveDuration;
        float swordWaveDamage;
        int swordWaveMiningLevel;
    }

    private AttackConfig loadConfig(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        AttackConfig cfg = new AttackConfig();
        cfg.currentMode = nbt.contains("AttackMode") ? nbt.getInt("AttackMode") : 0;
        cfg.modifyNbt = nbt.contains("ModifyNbt") && nbt.getBoolean("ModifyNbt");
        cfg.removeEntity = nbt.contains("RemoveEntity") && nbt.getBoolean("RemoveEntity");
        cfg.removeEntityData = nbt.contains("RemoveEntityData") && nbt.getBoolean("RemoveEntityData");
        cfg.rangeAttack = nbt.contains("RangeAttack") && nbt.getBoolean("RangeAttack");
        cfg.attackRange = nbt.contains("AttackRange") ? nbt.getInt("AttackRange") : 16;
        if (cfg.attackRange <= 0) cfg.attackRange = 16;
        if (cfg.attackRange > 256) cfg.attackRange = 256;
        cfg.fieldReflection = nbt.contains("FieldReflection") && nbt.getBoolean("FieldReflection");
        cfg.baseDamage = nbt.contains("BaseDamage") ? nbt.getFloat("BaseDamage") : 999999f;
        if (cfg.baseDamage <= 0 && !Float.isInfinite(cfg.baseDamage)) cfg.baseDamage = 999999f;
        if (cfg.baseDamage > 99999999f && !Float.isInfinite(cfg.baseDamage)) cfg.baseDamage = 99999999f;
        cfg.continuousAttack = nbt.contains("ContinuousAttack") && nbt.getBoolean("ContinuousAttack");
        cfg.continuousAttackTime = nbt.contains("ContinuousAttackTime") ? nbt.getInt("ContinuousAttackTime") : 100;
        if (cfg.continuousAttackTime <= 0) cfg.continuousAttackTime = 100;
        if (cfg.continuousAttackTime > 9999) cfg.continuousAttackTime = 9999;
        cfg.lightningAttack = nbt.contains("LightningAttack") && nbt.getBoolean("LightningAttack");
        cfg.lightningCount = nbt.contains("LightningCount") ? nbt.getInt("LightningCount") : 1;
        if (cfg.lightningCount <= 0) cfg.lightningCount = 1;
        if (cfg.lightningCount > 5) cfg.lightningCount = 5;
        cfg.fireAttack = nbt.contains("FireAttack") && nbt.getBoolean("FireAttack");
        cfg.explosionAttack = nbt.contains("ExplosionAttack") && nbt.getBoolean("ExplosionAttack");
        cfg.explosionRadius = nbt.contains("ExplosionRadius") ? nbt.getInt("ExplosionRadius") : 2;
        if (cfg.explosionRadius <= 0) cfg.explosionRadius = 2;
        if (cfg.explosionRadius > 10) cfg.explosionRadius = 10;
        cfg.allowFlight = nbt.contains("AllowFlight") && nbt.getBoolean("AllowFlight");
        cfg.immuneDamage = nbt.contains("ImmuneDamage") && nbt.getBoolean("ImmuneDamage");
        cfg.verifyProtection = nbt.contains("VerifyProtection") && nbt.getBoolean("VerifyProtection");
        cfg.attackProtection = nbt.contains("AttackProtection") && nbt.getBoolean("AttackProtection");
        cfg.playerSpeed = nbt.contains("PlayerSpeed") ? nbt.getInt("PlayerSpeed") : 0;
        if (cfg.playerSpeed < 0) cfg.playerSpeed = 0;
        if (cfg.playerSpeed > 99) cfg.playerSpeed = 99;
        cfg.maxHealth = nbt.contains("MaxHealth") ? nbt.getInt("MaxHealth") : 0;
        if (cfg.maxHealth < 0) cfg.maxHealth = 0;
        if (cfg.maxHealth > 9999) cfg.maxHealth = 9999;
        cfg.memoryFieldProtection = nbt.contains("MemoryFieldProtection") && nbt.getBoolean("MemoryFieldProtection");
        cfg.expAbsorption = nbt.contains("ExpAbsorption") && nbt.getBoolean("ExpAbsorption");
        cfg.freezeMode = nbt.contains("FreezeMode") && nbt.getBoolean("FreezeMode");
        cfg.healMode = nbt.contains("HealMode") && nbt.getBoolean("HealMode");
        cfg.healRange = nbt.contains("HealRange") ? nbt.getInt("HealRange") : 3;
        if (cfg.healRange <= 0) cfg.healRange = 3;
        if (cfg.healRange > 50) cfg.healRange = 50;
        cfg.swordWaveMode = nbt.contains("SwordWaveMode") && nbt.getBoolean("SwordWaveMode");
        cfg.swordWaveDuration = nbt.contains("SwordWaveDuration") ? nbt.getInt("SwordWaveDuration") : 5;
        if (cfg.swordWaveDuration <= 0) cfg.swordWaveDuration = 5;
        if (cfg.swordWaveDuration > 60) cfg.swordWaveDuration = 60;
        cfg.swordWaveDamage = nbt.contains("SwordWaveDamage") ? nbt.getFloat("SwordWaveDamage") : 999999f;
        if (cfg.swordWaveDamage <= 0) cfg.swordWaveDamage = 999999f;
        if (cfg.swordWaveDamage > 9999999f) cfg.swordWaveDamage = 9999999f;
        cfg.swordWaveMiningLevel = nbt.contains("SwordWaveMiningLevel") ? nbt.getInt("SwordWaveMiningLevel") : 0;
        if (cfg.swordWaveMiningLevel < 0) cfg.swordWaveMiningLevel = 0;
        if (cfg.swordWaveMiningLevel > 99) cfg.swordWaveMiningLevel = 99;
        return cfg;
    }

    private void killEntity(Entity target, AttackConfig cfg) {
        if (target == null || target.isRemoved()) return;
        if (cfg.removeEntity) target.remove(Entity.RemovalReason.DISCARDED);
        if (cfg.removeEntityData && target instanceof LivingEntity living) {
            living.writeNbt(new NbtCompound());
        }
        if (cfg.modifyNbt && target instanceof LivingEntity) {
            target.writeNbt(new NbtCompound());
            ((LivingEntity) target).getActiveStatusEffects().clear();
        }
        if (cfg.fieldReflection && target instanceof LivingEntity living) {
            try {
                Field healthField = LivingEntity.class.getDeclaredField("health");
                healthField.setAccessible(true);
                healthField.setFloat(living, 0);
            } catch (Exception ignored) {}
        }
        if (!target.isRemoved() && target instanceof LivingEntity living && living.getHealth() > 0) {
            living.setHealth(0);
        }
    }

    private void rangeAttack(ServerPlayerEntity player, AttackConfig cfg) {
        var world = player.getWorld();
        var center = player.getBlockPos();
        Box box = new Box(center).expand(cfg.attackRange);
        List<Entity> entities = world.getEntitiesByClass(Entity.class, box, e -> e != player);
        for (Entity e : entities) {
            killEntity(e, cfg);
            addEffects(e, cfg);
        }
        player.sendMessage(Text.literal("已清除范围内 " + entities.size() + " 个实体").formatted(Formatting.RED), true);
    }

    private void addEffects(Entity target, AttackConfig cfg) {
        var world = target.getWorld();
        if (cfg.lightningAttack) {
            for (int i = 0; i < cfg.lightningCount; i++) {
                var lightning = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(world);
                if (lightning != null) {
                    lightning.setPosition(target.getX(), target.getY(), target.getZ());
                    world.spawnEntity(lightning);
                }
            }
        }
        if (cfg.fireAttack) target.setOnFireFor(5);
        if (cfg.explosionAttack) {
            world.createExplosion(null, target.getX(), target.getY(), target.getZ(), cfg.explosionRadius, World.ExplosionSourceType.MOB);
        }
    }

    private void giveExperience(ServerPlayerEntity player, int amount) {
        if (amount > 0) player.addExperience(amount);
    }

    private void releaseSwordWave(ServerPlayerEntity player, AttackConfig cfg) {
        World world = player.getWorld();
        Vec3d start = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0f);
        double step = 0.5;
        double maxDistance = 50.0;
        Vec3d current = start;
        BlockPos lastPos = null;

        for (double d = 0; d <= maxDistance; d += step) {
            current = start.add(lookVec.multiply(d));
            BlockPos pos = BlockPos.ofFloored(current);
            if (lastPos != null && lastPos.equals(pos)) continue;
            lastPos = pos;

            BlockState state = world.getBlockState(pos);
            if (!state.isAir()) {
                float hardness = state.getHardness(world, pos);
                if (hardness >= 0 && cfg.swordWaveMiningLevel >= (int) hardness) {
                    world.breakBlock(pos, true);
                }
            }
        }

        Box box = new Box(start, start.add(lookVec.multiply(maxDistance))).expand(1.5);
        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, box, e -> e != player);
        for (LivingEntity target : entities) {
            target.damage(player.getDamageSources().playerAttack(player), cfg.swordWaveDamage);
            addEffects(target, cfg);
        }

        for (int i = 0; i <= 60; i++) {
            double t = i / 60.0;
            Vec3d point = start.lerp(start.add(lookVec.multiply(maxDistance)), t);
            world.addParticle(net.minecraft.particle.ParticleTypes.END_ROD, point.x, point.y, point.z, 0, 0, 0);
        }
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1f, 1f);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient || !(entity instanceof ServerPlayerEntity player)) return;
        if (!RainbowSwordHelper.verify(player, stack)) RainbowSwordHelper.rollback(player, stack);
        RainbowSwordHelper.backup(player, stack);
        var nbt = stack.getOrCreateNbt();
        if (ModUtil.hasContract(stack)) {
            AttackConfig cfg = loadConfig(stack);

            if (cfg.allowFlight) {
                player.getAbilities().allowFlying = true;
                player.getAbilities().setFlySpeed(0.2f);
                player.sendAbilitiesUpdate();
            }
            if (cfg.playerSpeed > 0 && cfg.playerSpeed <= 99) {
                var speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                if (speedAttr != null) {
                    speedAttr.removeModifier(SPEED_ID);
                    speedAttr.addTemporaryModifier(new EntityAttributeModifier(SPEED_ID, "Rainbow speed", cfg.playerSpeed / 100.0, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }
            if (cfg.maxHealth > 0 && cfg.maxHealth <= 9999) {
                player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(cfg.maxHealth);
                if (player.getHealth() > cfg.maxHealth) player.setHealth(cfg.maxHealth);
            }

            if (cfg.continuousAttack && cfg.currentMode == 1) {
                int continuousTick = nbt.getInt("ContinuousTick");
                if (continuousTick >= cfg.continuousAttackTime) {
                    nbt.putInt("ContinuousTick", 0);
                    rangeAttack(player, cfg);
                } else {
                    nbt.putInt("ContinuousTick", continuousTick + 1);
                }
            }
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient || hand != Hand.MAIN_HAND) return TypedActionResult.pass(stack);
        if (!(user instanceof ServerPlayerEntity player)) return TypedActionResult.pass(stack);

        NbtCompound nbt = stack.getOrCreateNbt();

        if (nbt.getBoolean("FreezeMode")) {
            var hitResult = player.raycast(5.0, 0.0f, false);
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                var target = ((net.minecraft.util.hit.EntityHitResult) hitResult).getEntity();
                if (target instanceof LivingEntity living) {
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 255, false, false, true));
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 200, 255, false, false, true));
                    player.sendMessage(Text.literal("已冻结 " + target.getName().getString()).formatted(Formatting.AQUA), true);
                    return TypedActionResult.success(stack);
                }
            }
        }

        if (nbt.getBoolean("HealMode")) {
            long now = world.getTime();
            long lastHealTime = nbt.getLong("LastHealTime");
            if (now - lastHealTime < 300) {
                player.sendMessage(Text.literal("治疗模式冷却中").formatted(Formatting.RED), true);
                return TypedActionResult.fail(stack);
            }
            AttackConfig cfg = loadConfig(stack);
            int range = cfg.healRange;
            Box box = new Box(player.getBlockPos()).expand(range);
            List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive() && !(e instanceof ServerPlayerEntity && e != player));
            int count = 0;
            for (LivingEntity target : entities) {
                target.setHealth(target.getMaxHealth());
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1200, 1, false, false, true));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 1200, 1, false, false, true));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 1, false, false, true));
                count++;
            }
            if (count > 0) {
                nbt.putLong("LastHealTime", now);
                player.sendMessage(Text.literal("治疗了 " + count + " 个友方单位").formatted(Formatting.GREEN), true);
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.PLAYERS, 1f, 1f);
            } else {
                player.sendMessage(Text.literal("范围内没有友方单位").formatted(Formatting.GRAY), true);
            }
            return TypedActionResult.success(stack);
        }

        if (!nbt.contains("OwnerUUID")) {
            nbt.putUuid("OwnerUUID", player.getUuid());
            RainbowSwordHelper.update(player, stack);
            player.sendMessage(Text.literal("七彩神剑已绑定于你").formatted(Formatting.GOLD), false);
        }
        long now = world.getTime();
        if (nbt.contains("LastUseTime") && now - nbt.getLong("LastUseTime") < 10) {
            ModUtil.setContract(stack, true);
            RainbowSwordHelper.update(player, stack);
            player.sendMessage(Text.literal("契约达成！你已获得七彩神剑的认可").formatted(Formatting.GOLD, Formatting.BOLD), false);
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1f, 1f);
        }
        nbt.putLong("LastUseTime", now);
        RainbowSwordHelper.update(player, stack);

        player.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (world.isClient || !(user instanceof ServerPlayerEntity player)) return;
        if (!ModUtil.hasContract(stack)) return;

        int useTime = getMaxUseTime(stack) - remainingUseTicks;
        if (useTime < 20) return;

        AttackConfig cfg = loadConfig(stack);
        float multiplier = 1.0f;
        if (useTime >= 200) multiplier = 10.0f;
        else if (useTime >= 140) multiplier = 8.0f;
        else if (useTime >= 80) multiplier = 6.0f;
        else if (useTime >= 60) multiplier = 4.0f;
        else if (useTime >= 40) multiplier = 3.0f;
        else if (useTime >= 20) multiplier = 2.0f;

        Box box = new Box(player.getBlockPos()).expand(cfg.attackRange * 2);
        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, box, e -> e != player);
        for (LivingEntity target : entities) {
            target.damage(player.getDamageSources().playerAttack(player), cfg.baseDamage * multiplier);
            addEffects(target, cfg);
        }
        player.sendMessage(Text.literal("蓄力攻击！伤害倍率: x" + multiplier).formatted(Formatting.GOLD), true);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1f, 1f);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getWorld().isClient || !(attacker instanceof ServerPlayerEntity player)) return true;
        if (!ModUtil.hasContract(stack)) return super.postHit(stack, target, attacker);

        NbtCompound nbt = stack.getOrCreateNbt();
        boolean destructMode = nbt.getBoolean("DestructMode");
        boolean specialMode = nbt.getBoolean("SpecialAttackMode");
        boolean swordWaveMode = nbt.getBoolean("SwordWaveMode");

        if (destructMode) {
            target.remove(Entity.RemovalReason.DISCARDED);
            return true;
        }

        AttackConfig cfg = loadConfig(stack);

        if (cfg.expAbsorption) {
            int exp = target.getXpToDrop();
            giveExperience(player, exp * 2);
        }

        if (specialMode) {
            target.damage(player.getDamageSources().playerAttack(player), cfg.baseDamage * 10);
            addEffects(target, cfg);
            return true;
        }

        if (swordWaveMode) {
            releaseSwordWave(player, cfg);
            return true;
        }

        switch (cfg.currentMode) {
            case 0:
                target.damage(player.getDamageSources().playerAttack(player), cfg.baseDamage);
                break;
            case 1:
                killEntity(target, cfg);
                rangeAttack(player, cfg);
                break;
            case 2:
                killEntity(target, cfg);
                break;
            case 3:
                target.damage(player.getDamageSources().playerAttack(player), cfg.baseDamage * 2);
                if (cfg.explosionAttack) {
                    target.getWorld().createExplosion(null, target.getX(), target.getY(), target.getZ(), cfg.explosionRadius, World.ExplosionSourceType.MOB);
                }
                break;
        }
        addEffects(target, cfg);
        return true;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (world.isClient || !(miner instanceof ServerPlayerEntity player)) return true;
        if (!ModUtil.hasContract(stack)) return true;
        world.breakBlock(pos, true);
        return true;
    }

    @Override public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) { return true; }
    @Override public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) { return 9999f; }
    @Override public boolean isDamageable() { return false; }
    @Override public boolean hasGlint(ItemStack stack) { return true; }
}
