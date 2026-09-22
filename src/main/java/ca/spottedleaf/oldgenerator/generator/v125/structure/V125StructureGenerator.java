package ca.spottedleaf.oldgenerator.generator.v125.structure;

import ca.spottedleaf.oldgenerator.generator.v125.V125BiomeSource;
import ca.spottedleaf.oldgenerator.generator.v125.structure.legacy.*;
import ca.spottedleaf.oldgenerator.world.BlockAccess;

import java.util.Random;

public final class V125StructureGenerator {
    private static final int MAP_RANGE = 8;
    private static final int[] STRONGHOLD_BIOMES = {2, 4, 3, 6, 5, 12, 13, 17, 18, 20, 21, 22};

    public Result generate(final long seed, final int targetChunkX, final int targetChunkZ,
                           final BlockAccess access, final V125BiomeSource biomes,
                           final Random populationRandom) {
        final World world = new World(seed, access, biomes);
        final StructureBoundingBox chunkBox = new StructureBoundingBox(
                targetChunkX << 4, 0, targetChunkZ << 4,
                (targetChunkX << 4) + 15, 127, (targetChunkZ << 4) + 15);

        final Random seedRandom = new Random(seed);
        final long mapSeedX = seedRandom.nextLong();
        final long mapSeedZ = seedRandom.nextLong();

        boolean village = false;

        for (int cx = targetChunkX - MAP_RANGE; cx <= targetChunkX + MAP_RANGE; ++cx) {
            for (int cz = targetChunkZ - MAP_RANGE; cz <= targetChunkZ + MAP_RANGE; ++cz) {
                final Random mapRandom = structureRandom(seed, cx, cz, mapSeedX, mapSeedZ);
                // MapGenStructure.recursiveGenerate() consumes one nextInt()
                // before canSpawnStructureAtCoords().
                mapRandom.nextInt();

                if (mapRandom.nextInt(100) == 0
                        && mapRandom.nextInt(80) < Math.max(Math.abs(cx), Math.abs(cz))) {
                    final StructureMineshaftStart start =
                            new StructureMineshaftStart(world, mapRandom, cx, cz);
                    if (start.getBoundingBox() != null && start.getBoundingBox().intersectsWith(chunkBox)) {
                        start.generateStructure(world, populationRandom, chunkBox);
                    }
                }
            }
        }

        for (int cx = targetChunkX - MAP_RANGE; cx <= targetChunkX + MAP_RANGE; ++cx) {
            for (int cz = targetChunkZ - MAP_RANGE; cz <= targetChunkZ + MAP_RANGE; ++cz) {
                if (!isVillageStart(seed, cx, cz, biomes)) {
                    continue;
                }

                final Random mapRandom = structureRandom(seed, cx, cz, mapSeedX, mapSeedZ);
                mapRandom.nextInt(); // MapGenStructure.recursiveGenerate()

                final StructureVillageStart start =
                        new StructureVillageStart(world, mapRandom, cx, cz, 0);
                if (!start.isSizeableStructure()) {
                    continue;
                }

                if (start.getBoundingBox() != null && start.getBoundingBox().intersectsWith(chunkBox)) {
                    village = true;
                    start.generateStructure(world, populationRandom, chunkBox);
                }
            }
        }

        for (final int[] stronghold : strongholdChunks(seed, biomes)) {
            if (stronghold[0] != targetChunkX || stronghold[1] != targetChunkZ) {
                continue;
            }

            final Random mapRandom = structureRandom(seed, targetChunkX, targetChunkZ, mapSeedX, mapSeedZ);
            mapRandom.nextInt(); // MapGenStructure.recursiveGenerate()
            final StructureStrongholdStart start =
                    new StructureStrongholdStart(world, mapRandom, targetChunkX, targetChunkZ);
            if (start.getBoundingBox() != null && start.getBoundingBox().intersectsWith(chunkBox)) {
                start.generateStructure(world, populationRandom, chunkBox);
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
        return isVillageStart(seed, chunkX, chunkZ, biomes);
    }

    private static Random structureRandom(final long seed, final int chunkX, final int chunkZ,
                                          final long mapSeedX, final long mapSeedZ) {
        return new Random((long)chunkX * mapSeedX ^ (long)chunkZ * mapSeedZ ^ seed);
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
        if (chunkX != candidateX || chunkZ != candidateZ) {
            return false;
        }

        final int biome = biomes.getBiomeIds(seed, chunkX * 4 + 2, chunkZ * 4 + 2, 1, 1)[0];
        return biome == 1 || biome == 2;
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
