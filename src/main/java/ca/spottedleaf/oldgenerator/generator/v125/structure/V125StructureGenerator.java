package ca.spottedleaf.oldgenerator.generator.v125.structure;

import ca.spottedleaf.oldgenerator.generator.v125.V125BiomeSource;
import ca.spottedleaf.oldgenerator.generator.v125.structure.legacy.*;
import ca.spottedleaf.oldgenerator.world.BlockAccess;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class V125StructureGenerator {
    private static final int MAP_RANGE = 8;
    private static final int[] STRONGHOLD_BIOMES = {2, 4, 3, 6, 5, 12, 13, 17, 18, 20, 21, 22};

    /*
     * MapGenStructure in 1.2.5 retains generated StructureStart instances in
     * coordMap for the lifetime of the generator. Modern chunk generation is
     * parallel, so the equivalent cache is per-world-seed and its starts are
     * synchronized while their components are generated.
     */
    private final Map<Long, StructureState> worlds = new ConcurrentHashMap<>();

    public Result generate(final long seed, final int targetChunkX, final int targetChunkZ,
                           final BlockAccess access, final V125BiomeSource biomes,
                           final Random populationRandom) {
        final StructureState state = this.worlds.computeIfAbsent(seed, StructureState::new);
        final World world = new World(seed, access, biomes);
        final StructureBoundingBox chunkBox = new StructureBoundingBox(
                targetChunkX << 4, 0, targetChunkZ << 4,
                (targetChunkX << 4) + 15, 127, (targetChunkZ << 4) + 15);

        boolean village = false;

        for (int cx = targetChunkX - MAP_RANGE; cx <= targetChunkX + MAP_RANGE; ++cx) {
            for (int cz = targetChunkZ - MAP_RANGE; cz <= targetChunkZ + MAP_RANGE; ++cz) {
                final long key = chunkKey(cx, cz);
                StructureStart start;
                synchronized (state) {
                    start = state.mineshafts.get(key);
                    if (start == null && state.checkedMineshafts.add(key)) {
                        final Random mapRandom = structureRandom(state, cx, cz);
                        mapRandom.nextInt();

                        if (isMineshaftStart(mapRandom, cx, cz)) {
                            start = new StructureMineshaftStart(world, mapRandom, cx, cz);
                            state.mineshafts.put(key, start);
                        }
                    }
                }

                if (start != null && start.isSizeableStructure()
                        && start.getBoundingBox() != null
                        && start.getBoundingBox().intersectsWith(chunkBox)) {
                    synchronized (start) {
                        start.generateStructure(world, populationRandom, chunkBox);
                    }
                }
            }
        }

        for (int cx = targetChunkX - MAP_RANGE; cx <= targetChunkX + MAP_RANGE; ++cx) {
            for (int cz = targetChunkZ - MAP_RANGE; cz <= targetChunkZ + MAP_RANGE; ++cz) {
                final long key = chunkKey(cx, cz);
                StructureStart start;
                synchronized (state) {
                    start = state.villages.get(key);
                    if (start == null && state.checkedVillages.add(key) && isVillageStart(seed, cx, cz, biomes)) {
                        final Random mapRandom = structureRandom(state, cx, cz);
                        mapRandom.nextInt();
                        start = new StructureVillageStart(world, mapRandom, cx, cz, 0);
                        state.villages.put(key, start);
                    }
                }

                if (start != null && start.isSizeableStructure()
                        && start.getBoundingBox() != null
                        && start.getBoundingBox().intersectsWith(chunkBox)) {
                    village = true;
                    synchronized (start) {
                        start.generateStructure(world, populationRandom, chunkBox);
                    }
                }
            }
        }

        for (final int[] stronghold : state.strongholdChunks) {
            final int startChunkX = stronghold[0];
            final int startChunkZ = stronghold[1];
            final long key = chunkKey(startChunkX, startChunkZ);

            StructureStart start;
            synchronized (state) {
                start = state.strongholds.get(key);
                if (start == null) {
                    final Random mapRandom = structureRandom(state, startChunkX, startChunkZ);
                    mapRandom.nextInt(); // MapGenStructure.recursiveGenerate()

                    StructureStrongholdStart strongholdStart;
                    do {
                        strongholdStart = new StructureStrongholdStart(world, mapRandom, startChunkX, startChunkZ);
                    } while (strongholdStart.getComponents().isEmpty()
                            || !(strongholdStart.getComponents().get(0) instanceof ComponentStrongholdStairs2 stairs)
                            || stairs.portalRoom == null);

                    start = strongholdStart;
                    state.strongholds.put(key, start);
                }
            }

            if (start.isSizeableStructure()
                    && start.getBoundingBox() != null
                    && start.getBoundingBox().intersectsWith(chunkBox)) {
                synchronized (start) {
                    start.generateStructure(world, populationRandom, chunkBox);
                }
            }
        }

        return new Result(village);
    }

    public Result generate(final long seed, final int targetChunkX, final int targetChunkZ,
                           final BlockAccess access, final V125BiomeSource biomes) {
        return this.generate(seed, targetChunkX, targetChunkZ, access, biomes, new Random(seed));
    }

    public boolean hasVillageStart(final long seed, final int chunkX, final int chunkZ,
                                   final V125BiomeSource biomes) {
        final StructureState state = this.worlds.computeIfAbsent(seed, StructureState::new);
        final long key = chunkKey(chunkX, chunkZ);
        if (!state.checkedVillages.contains(key)) {
            return isVillageStart(seed, chunkX, chunkZ, biomes);
        }
        final StructureStart start = state.villages.get(key);
        return start != null && start.isSizeableStructure();
    }

    public int[] findNearestMineshaft(final long seed, final int originX, final int originZ, final int radiusChunks) {
        final StructureState state = this.worlds.computeIfAbsent(seed, StructureState::new);
        final int originChunkX = Math.floorDiv(originX, 16);
        final int originChunkZ = Math.floorDiv(originZ, 16);
        final int radius = Math.max(0, radiusChunks);
        double bestDistance = Double.POSITIVE_INFINITY;
        int[] best = null;

        for (int cx = originChunkX - radius; cx <= originChunkX + radius; ++cx) {
            for (int cz = originChunkZ - radius; cz <= originChunkZ + radius; ++cz) {
                if (!isMineshaftStart(state, cx, cz)) continue;
                final double dx = (cx * 16 + 8) - originX;
                final double dz = (cz * 16 + 8) - originZ;
                final double distance = dx * dx + dz * dz;
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = new int[]{cx, cz};
                }
            }
        }
        return best;
    }

    public int[] findNearestVillage(final long seed, final int originX, final int originZ,
                                    final int radiusChunks, final boolean allowPlains, final boolean allowDesert,
                                    final V125BiomeSource biomes) {
        final int originChunkX = Math.floorDiv(originX, 16);
        final int originChunkZ = Math.floorDiv(originZ, 16);
        final int radius = Math.max(0, radiusChunks);
        final int minRegionX = Math.floorDiv(originChunkX - radius, 32);
        final int maxRegionX = Math.floorDiv(originChunkX + radius, 32);
        final int minRegionZ = Math.floorDiv(originChunkZ - radius, 32);
        final int maxRegionZ = Math.floorDiv(originChunkZ + radius, 32);
        double bestDistance = Double.POSITIVE_INFINITY;
        int[] best = null;

        for (int rx = minRegionX; rx <= maxRegionX; ++rx) {
            for (int rz = minRegionZ; rz <= maxRegionZ; ++rz) {
                final int[] candidate = villageCandidate(seed, rx, rz, biomes);
                if (candidate == null) continue;
                final int biome = biomes.getBiomeIds(seed, candidate[0] * 16 + 8, candidate[1] * 16 + 8, 1, 1)[0];
                if ((biome == 1 && !allowPlains) || (biome == 2 && !allowDesert)) continue;
                if (Math.abs(candidate[0] - originChunkX) > radius || Math.abs(candidate[1] - originChunkZ) > radius) continue;

                final double dx = (candidate[0] * 16 + 8) - originX;
                final double dz = (candidate[1] * 16 + 8) - originZ;
                final double distance = dx * dx + dz * dz;
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = candidate;
                }
            }
        }
        return best;
    }

    public int[] findNearestStronghold(final long seed, final int originX, final int originZ, final int radiusChunks) {
        final StructureState state = this.worlds.computeIfAbsent(seed, StructureState::new);
        final int radius = Math.max(0, radiusChunks);
        double bestDistance = Double.POSITIVE_INFINITY;
        int[] best = null;

        for (final int[] candidate : state.strongholdChunks) {
            final double dx = (candidate[0] * 16 + 8) - originX;
            final double dz = (candidate[1] * 16 + 8) - originZ;
            if (Math.abs(candidate[0] - Math.floorDiv(originX, 16)) > radius
                    || Math.abs(candidate[1] - Math.floorDiv(originZ, 16)) > radius) {
                continue;
            }
            final double distance = dx * dx + dz * dz;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = new int[]{candidate[0], candidate[1]};
            }
        }
        return best;
    }

    private static boolean isMineshaftStart(final Random random, final int chunkX, final int chunkZ) {
        return random.nextInt(100) == 0
                && random.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ));
    }

    private static boolean isMineshaftStart(final StructureState state, final int chunkX, final int chunkZ) {
        final Random random = structureRandom(state, chunkX, chunkZ);
        random.nextInt(); // MapGenStructure.recursiveGenerate()
        return isMineshaftStart(random, chunkX, chunkZ);
    }

    private static int[] villageCandidate(final long seed, final int regionX, final int regionZ,
                                          final V125BiomeSource biomes) {
        final Random random = new Random(
                (long)regionX * 341873128712L
                        + (long)regionZ * 132897987541L
                        + seed + 10387312L
        );
        final int candidateX = regionX * 32 + random.nextInt(24);
        final int candidateZ = regionZ * 32 + random.nextInt(24);
        if (!biomes.areBiomesViable(seed, candidateX * 16 + 8, candidateZ * 16 + 8, 0, new int[]{1, 2})) {
            return null;
        }
        return new int[]{candidateX, candidateZ};
    }

    private static final class StructureState {
        final long seed;
        final long mapSeedX;
        final long mapSeedZ;
        final Map<Long, StructureStart> mineshafts = new ConcurrentHashMap<>();
        final Map<Long, StructureStart> villages = new ConcurrentHashMap<>();
        final Map<Long, StructureStart> strongholds = new ConcurrentHashMap<>();
        final Set<Long> checkedMineshafts = ConcurrentHashMap.newKeySet();
        final Set<Long> checkedVillages = ConcurrentHashMap.newKeySet();
        final int[][] strongholdChunks;

        StructureState(final long seed) {
            this.seed = seed;
            final Random random = new Random(seed);
            this.mapSeedX = random.nextLong();
            this.mapSeedZ = random.nextLong();
            this.strongholdChunks = strongholdChunks(seed, new V125BiomeSource());
        }
    }

    private static long chunkKey(final int x, final int z) {
        return ((long)x << 32) ^ (z & 0xFFFFFFFFL);
    }

    private static Random structureRandom(final StructureState state, final int chunkX, final int chunkZ) {
        return new Random((long)chunkX * state.mapSeedX ^ (long)chunkZ * state.mapSeedZ ^ state.seed);
    }

    private static boolean isVillageStart(final long seed, final int chunkX, final int chunkZ,
                                          final V125BiomeSource biomes) {
        final int regionSize = 32;
        final int offset = 8;

        int x = chunkX;
        int z = chunkZ;
        if (x < 0) x -= regionSize - 1;
        if (z < 0) z -= regionSize - 1;

        final int regionX = x / regionSize;
        final int regionZ = z / regionSize;

        final Random random = new Random(
                (long)regionX * 341873128712L
                        + (long)regionZ * 132897987541L
                        + seed + 10387312L
        );

        final int candidateX = regionX * regionSize + random.nextInt(regionSize - offset);
        final int candidateZ = regionZ * regionSize + random.nextInt(regionSize - offset);
        if (chunkX != candidateX || chunkZ != candidateZ) return false;

        return biomes.areBiomesViable(seed, chunkX * 16 + 8, chunkZ * 16 + 8, 0,
                new int[]{1, 2});
    }

    private static int[][] strongholdChunks(final long seed, final V125BiomeSource biomes) {
        final int[][] result = new int[3][2];
        final Random random = new Random(seed);
        double angle = random.nextDouble() * Math.PI * 2.0D;

        for (int i = 0; i < 3; ++i) {
            final double distance = (1.25D + random.nextDouble()) * 32.0D;
            int chunkX = (int)Math.round(Math.cos(angle) * distance);
            int chunkZ = (int)Math.round(Math.sin(angle) * distance);

            final long[] selected = biomes.findBiomePosition(
                    seed, (chunkX << 4) + 8, (chunkZ << 4) + 8,
                    112, STRONGHOLD_BIOMES, random
            );
            if (selected != null) {
                chunkX = (int)(selected[0] >> 4);
                chunkZ = (int)(selected[1] >> 4);
            }

            result[i][0] = chunkX;
            result[i][1] = chunkZ;
            angle += Math.PI * 2.0D / 3.0D;
        }

        return result;
    }

    public static final class Result {
        private final boolean villageGenerated;
        Result(final boolean villageGenerated) { this.villageGenerated = villageGenerated; }
        public boolean hasVillage() { return villageGenerated; }
    }
}
