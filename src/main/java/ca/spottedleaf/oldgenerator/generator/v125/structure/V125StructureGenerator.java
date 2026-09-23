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
                StructureStart start = state.mineshafts.get(key);

                if (start == null && state.checkedMineshafts.add(key)) {
                    final Random mapRandom = structureRandom(state, cx, cz);
                    mapRandom.nextInt();

                    if (mapRandom.nextInt(100) == 0
                            && mapRandom.nextInt(80) < Math.max(Math.abs(cx), Math.abs(cz))) {
                        start = new StructureMineshaftStart(world, mapRandom, cx, cz);
                        state.mineshafts.put(key, start);
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
                StructureStart start = state.villages.get(key);

                if (start == null && state.checkedVillages.add(key) && isVillageStart(seed, cx, cz, biomes)) {
                    final Random mapRandom = structureRandom(state, cx, cz);
                    mapRandom.nextInt();
                    start = new StructureVillageStart(world, mapRandom, cx, cz, 0);
                    state.villages.put(key, start);
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

            StructureStart start = state.strongholds.get(key);
            if (start == null) {
                final Random mapRandom = structureRandom(state, startChunkX, startChunkZ);
                mapRandom.nextInt(); // MapGenStructure.recursiveGenerate()
                start = new StructureStrongholdStart(world, mapRandom, startChunkX, startChunkZ);
                final StructureStart existing = state.strongholds.putIfAbsent(key, start);
                if (existing != null) {
                    start = existing;
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
