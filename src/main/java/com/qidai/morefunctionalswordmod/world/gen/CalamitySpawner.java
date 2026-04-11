package com.qidai.morefunctionalswordmod.world.gen;

import com.qidai.morefunctionalswordmod.ModEntities;
import com.qidai.morefunctionalswordmod.entity.calamity.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.GameRules;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class CalamitySpawner {
    private static final Set<RegistryKey<Biome>> ALLOWED_BIOMES = new HashSet<>();
    static {
        ALLOWED_BIOMES.add(BiomeKeys.PLAINS);
        ALLOWED_BIOMES.add(BiomeKeys.DESERT);
        ALLOWED_BIOMES.add(BiomeKeys.BADLANDS);
        ALLOWED_BIOMES.add(BiomeKeys.WINDSWEPT_HILLS);
        ALLOWED_BIOMES.add(BiomeKeys.SWAMP);
    }

    private static final int CHECK_INTERVAL = 400; // 降低生成频率（原来200）
    private static final int TEAMS_PER_AREA_MIN = 0; // 最少0队，不强制生成
    private static final int TEAMS_PER_AREA_MAX = 1; // 最多1队

    private static final Random RANDOM = new Random();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                if (world.getRegistryKey() == net.minecraft.world.World.OVERWORLD) {
                    // 检查游戏规则 doMobSpawning
                    if (!world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
                        continue;
                    }
                    tick(world);
                }
            }
        });
    }

    private static void tick(ServerWorld world) {
        long time = world.getTime();
        if (time % CHECK_INTERVAL != 0) return;

        world.getPlayers().forEach(player -> {
            BlockPos playerPos = player.getBlockPos();
            ChunkPos centerChunk = new ChunkPos(playerPos);

            for (int dx = -3; dx <= 3; dx++) { // 缩小范围
                for (int dz = -3; dz <= 3; dz++) {
                    if (RANDOM.nextFloat() < 0.01) { // 降低概率
                        ChunkPos chunk = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);
                        trySpawnTeamInChunk(world, chunk);
                    }
                }
            }
        });
    }

    private static void trySpawnTeamInChunk(ServerWorld world, ChunkPos chunk) {
        BlockPos center = chunk.getCenterAtY(0);
        RegistryKey<Biome> biomeKey = world.getBiome(center).getKey().orElse(null);
        if (biomeKey == null || !ALLOWED_BIOMES.contains(biomeKey)) return;

        int x = chunk.getStartX() + RANDOM.nextInt(16);
        int z = chunk.getStartZ() + RANDOM.nextInt(16);
        int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
        BlockPos spawnPos = new BlockPos(x, y, z);

        if (!world.getBlockState(spawnPos.down()).isSolid() || world.getBlockState(spawnPos).isLiquid()) return;

        int teamCount = RANDOM.nextInt(TEAMS_PER_AREA_MAX - TEAMS_PER_AREA_MIN + 1) + TEAMS_PER_AREA_MIN;
        for (int i = 0; i < teamCount; i++) {
            spawnTeam(world, spawnPos);
        }
    }

    private static void spawnTeam(ServerWorld world, BlockPos pos) {
        CalamityWind leader = new CalamityWind(ModEntities.CALAMITY_WIND, world);
        leader.setPosition(pos.getX(), pos.getY(), pos.getZ());
        leader.initialize(world, world.getLocalDifficulty(pos), SpawnReason.NATURAL, null, null);
        world.spawnEntityAndPassengers(leader);

        // 减少数量
        int wraithCount = 1 + RANDOM.nextInt(3);
        int soldierCount = RANDOM.nextInt(2);
        int phantomCount = 1 + RANDOM.nextInt(2);

        for (int i = 0; i < wraithCount; i++) {
            CalamityWraith wraith = new CalamityWraith(ModEntities.CALAMITY_WRAITH, world);
            BlockPos offset = pos.add(RANDOM.nextInt(3) - 1, 0, RANDOM.nextInt(3) - 1);
            wraith.setPosition(offset.getX(), world.getTopY(Heightmap.Type.WORLD_SURFACE, offset.getX(), offset.getZ()), offset.getZ());
            wraith.initialize(world, world.getLocalDifficulty(offset), SpawnReason.NATURAL, null, null);
            world.spawnEntityAndPassengers(wraith);
        }

        for (int i = 0; i < soldierCount; i++) {
            CalamitySoldier soldier = new CalamitySoldier(ModEntities.CALAMITY_SOLDIER, world);
            BlockPos offset = pos.add(RANDOM.nextInt(3) - 1, 0, RANDOM.nextInt(3) - 1);
            soldier.setPosition(offset.getX(), world.getTopY(Heightmap.Type.WORLD_SURFACE, offset.getX(), offset.getZ()), offset.getZ());
            soldier.initialize(world, world.getLocalDifficulty(offset), SpawnReason.NATURAL, null, null);
            world.spawnEntityAndPassengers(soldier);
        }

        for (int i = 0; i < phantomCount; i++) {
            CalamityPhantom phantom = new CalamityPhantom(ModEntities.CALAMITY_PHANTOM, world);
            BlockPos offset = pos.add(RANDOM.nextInt(3) - 1, 2 + RANDOM.nextInt(2), RANDOM.nextInt(3) - 1);
            phantom.setPosition(offset.getX(), offset.getY(), offset.getZ());
            phantom.initialize(world, world.getLocalDifficulty(offset), SpawnReason.NATURAL, null, null);
            world.spawnEntityAndPassengers(phantom);
        }
    }
}
