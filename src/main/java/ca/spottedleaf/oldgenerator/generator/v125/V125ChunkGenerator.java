package ca.spottedleaf.oldgenerator.generator.v125;

import ca.spottedleaf.oldgenerator.generator.b173.populator.*;
import ca.spottedleaf.oldgenerator.generator.v125.noise.NoiseGeneratorOctaves125;
import ca.spottedleaf.oldgenerator.generator.v125.map.V125Caves;
import ca.spottedleaf.oldgenerator.generator.v125.map.V125Ravine;
import ca.spottedleaf.oldgenerator.generator.v125.populate.*;
import ca.spottedleaf.oldgenerator.generator.v125.structure.V125StructureGenerator;
import org.bukkit.generator.BlockPopulator;
import java.util.Collections;
import java.util.List;
import ca.spottedleaf.oldgenerator.util.BlockConstants;
import ca.spottedleaf.oldgenerator.world.BlockAccess;
import ca.spottedleaf.oldgenerator.world.WorldBlockAccess;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

/**
 * Minecraft 1.2.5 Overworld generator.
 *
 * The terrain RNG/noise object construction order and density interpolation
 * follow the 1.2.5 server algorithm. Population is executed separately so
 * the legacy chunk ordering can be reproduced by LegacyPopulateHack.
 */
public final class V125ChunkGenerator extends ChunkGenerator {
    private static final int SEA_LEVEL = 63;
    private static final int WORLD_HEIGHT = 128;

    private final V125BiomeSource biomeSource = new V125BiomeSource();
    private final ThreadLocal<NoiseState> noiseStates = new ThreadLocal<>();

    private static final class NoiseState {
        final long seed;
        final NoiseGeneratorOctaves125 noiseGen1;
        final NoiseGeneratorOctaves125 noiseGen2;
        final NoiseGeneratorOctaves125 noiseGen3;
        final NoiseGeneratorOctaves125 noiseGen4;
        final NoiseGeneratorOctaves125 noiseGen5;
        final NoiseGeneratorOctaves125 noiseGen6;

        NoiseState(final long seed) {
            this.seed = seed;
            final Random random = new Random(seed);
            this.noiseGen1 = new NoiseGeneratorOctaves125(random, 16);
            this.noiseGen2 = new NoiseGeneratorOctaves125(random, 16);
            this.noiseGen3 = new NoiseGeneratorOctaves125(random, 8);
            this.noiseGen4 = new NoiseGeneratorOctaves125(random, 4);
            this.noiseGen5 = new NoiseGeneratorOctaves125(random, 10);
            this.noiseGen6 = new NoiseGeneratorOctaves125(random, 16);
        }
    }

    private NoiseState state(final long seed) {
        NoiseState state = this.noiseStates.get();
        if (state == null || state.seed != seed) {
            state = new NoiseState(seed);
            this.noiseStates.set(state);
        }
        return state;
    }

    @Override
    public boolean isParallelCapable() {
        return true;
    }

    @Override
    public boolean shouldGenerateCaves() { return false; }

    @Override
    public boolean shouldGenerateDecorations() { return false; }

    @Override
    public boolean shouldGenerateMobs() { return false; }

    @Override
    public boolean shouldGenerateStructures() { return false; }

    @Override
    public ChunkData generateChunkData(final World world, final Random random,
                                       final int chunkX, final int chunkZ,
                                       final BiomeGrid biomeGrid) {
        if (world.getEnvironment() != World.Environment.NORMAL) {
            throw new IllegalStateException("v125 only supports the 1.2.5 Overworld");
        }

        final ChunkData data = this.createChunkData(world);
        final long seed = world.getSeed();
        final int[] biomes = this.biomeSource.getBlockBiomeIds(seed, chunkX * 16, chunkZ * 16, 16, 16);

        for (int z = 0; z < 16; ++z) {
            for (int x = 0; x < 16; ++x) {
                biomeGrid.setBiome(x, z, this.biomeSource.toBukkit(biomes[x + z * 16]));
            }
        }

        final double[] noise = this.generateDensity(seed, chunkX, chunkZ);
        for (int cellX = 0; cellX < 4; ++cellX) {
            for (int cellZ = 0; cellZ < 4; ++cellZ) {
                for (int cellY = 0; cellY < 16; ++cellY) {
                    final int i000 = densityIndex(cellX, cellZ, cellY);
                    final int i001 = densityIndex(cellX, cellZ, cellY + 1);
                    final int i100 = densityIndex(cellX + 1, cellZ, cellY);
                    final int i101 = densityIndex(cellX + 1, cellZ, cellY + 1);
                    final int i010 = densityIndex(cellX, cellZ + 1, cellY);
                    final int i011 = densityIndex(cellX, cellZ + 1, cellY + 1);
                    final int i110 = densityIndex(cellX + 1, cellZ + 1, cellY);
                    final int i111 = densityIndex(cellX + 1, cellZ + 1, cellY + 1);

                    double d000 = noise[i000];
                    double d001 = noise[i001];
                    double d100 = noise[i100];
                    double d101 = noise[i101];
                    double d010 = noise[i010];
                    double d011 = noise[i011];
                    double d110 = noise[i110];
                    double d111 = noise[i111];

                    final double dy000 = (d001 - d000) * 0.125D;
                    final double dy010 = (d011 - d010) * 0.125D;
                    final double dy100 = (d101 - d100) * 0.125D;
                    final double dy110 = (d111 - d110) * 0.125D;

                    for (int localY = 0; localY < 8; ++localY) {
                        double x0 = d000;
                        double x1 = d010;
                        final double dx0 = (d100 - d000) * 0.25D;
                        final double dx1 = (d110 - d010) * 0.25D;

                        for (int localX = 0; localX < 4; ++localX) {
                            double densityValue = x0;
                            final double dz = (x1 - x0) * 0.25D;
                            for (int localZ = 0; localZ < 4; ++localZ) {
                                densityValue += dz;
                                final int x = cellX * 4 + localX;
                                final int z = cellZ * 4 + localZ;
                                final int y = cellY * 8 + localY;
                                if (densityValue > 0.0D) {
                                    data.setBlock(x, y, z, BlockConstants.STONE);
                                } else if (y < SEA_LEVEL) {
                                    data.setBlock(x, y, z, BlockConstants.SOURCE_WATER);
                                }
                            }
                            x0 += dx0;
                            x1 += dx1;
                        }

                        d000 += dy000;
                        d010 += dy010;
                        d100 += dy100;
                        d110 += dy110;
                    }
                }
            }
        }

        this.applySurface(data, biomes, seed, chunkX, chunkZ);
        new V125Caves(seed).generate(chunkX, chunkZ, data, this.biomeSource);
        new V125Ravine(seed).generate(chunkX, chunkZ, data, this.biomeSource);
        return data;
    }

    private double[] generateDensity(final long seed, final int chunkX, final int chunkZ) {
        final NoiseState state = this.state(seed);
        final int width = 5;
        final int height = 17;
        final double[] out = new double[width * width * height];

        final int[] biomes = this.biomeSource.getBiomeIds(seed, chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);
        final double[] noise5 = state.noiseGen5.generateNoise(null,
                chunkX * 4, chunkZ * 4, 5, 5, 1.121D, 1.121D);
        final double[] noise6 = state.noiseGen6.generateNoise(null,
                chunkX * 4, chunkZ * 4, 5, 5, 200.0D, 200.0D);
        final double[] noise3 = state.noiseGen3.generateNoise(null,
                chunkX * 4, 0, chunkZ * 4, 5, 17, 5,
                8.55515D, 4.277575D, 8.55515D);
        final double[] noise1 = state.noiseGen1.generateNoise(null,
                chunkX * 4, 0, chunkZ * 4, 5, 17, 5,
                684.412D, 684.412D, 684.412D);
        final double[] noise2 = state.noiseGen2.generateNoise(null,
                chunkX * 4, 0, chunkZ * 4, 5, 17, 5,
                684.412D, 684.412D, 684.412D);

        final float[] weights = new float[25];
        for (int dx = -2; dx <= 2; ++dx) {
            for (int dz = -2; dz <= 2; ++dz) {
                weights[dx + 2 + (dz + 2) * 5] =
                        10.0F / (float)Math.sqrt(dx * dx + dz * dz + 0.2F);
            }
        }

        int noiseIndex = 0;
        int depthIndex = 0;
        for (int x = 0; x < width; ++x) {
            for (int z = 0; z < width; ++z) {
                final V125BiomeData center = this.biomeSource.getBiomeData(biomes[x + 2 + (z + 2) * 10]);
                float maxHeight = 0.0F;
                float minHeight = 0.0F;
                float totalWeight = 0.0F;

                for (int dx = -2; dx <= 2; ++dx) {
                    for (int dz = -2; dz <= 2; ++dz) {
                        final V125BiomeData neighbor =
                                this.biomeSource.getBiomeData(biomes[x + dx + 2 + (z + dz + 2) * 10]);
                        float weight = weights[dx + 2 + (dz + 2) * 5] /
                                (neighbor.heightVariation + 2.0F);
                        if (neighbor.heightVariation > center.heightVariation) {
                            weight /= 2.0F;
                        }
                        maxHeight += neighbor.heightVariation * weight;
                        minHeight += neighbor.baseHeight * weight;
                        totalWeight += weight;
                    }
                }

                maxHeight /= totalWeight;
                minHeight /= totalWeight;
                maxHeight = maxHeight * 0.9F + 0.1F;
                minHeight = (minHeight * 4.0F - 1.0F) / 8.0F;

                double depth = noise6[depthIndex++] / 8000.0D;
                if (depth < 0.0D) {
                    depth = -depth * 0.3D;
                }
                depth = depth * 3.0D - 2.0D;
                if (depth < 0.0D) {
                    depth /= 2.0D;
                    if (depth < -1.0D) {
                        depth = -1.0D;
                    }
                    depth /= 1.4D;
                    depth /= 2.0D;
                } else {
                    if (depth > 1.0D) {
                        depth = 1.0D;
                    }
                    depth /= 8.0D;
                }

                for (int y = 0; y < height; ++y) {
                    double densityHeight = minHeight;
                    densityHeight += depth * 0.2D;
                    densityHeight = densityHeight * height / 16.0D;
                    final double centerHeight = height / 2.0D + densityHeight * 4.0D;
                    final double vertical = ((double)y - centerHeight) * 12.0D * 128.0D / 128.0D / maxHeight;
                    final double adjustedVertical = vertical < 0.0D ? vertical * 4.0D : vertical;

                    final double low = noise1[noiseIndex] / 512.0D;
                    final double high = noise2[noiseIndex] / 512.0D;
                    final double selector = (noise3[noiseIndex] / 10.0D + 1.0D) / 2.0D;
                    double density;
                    if (selector < 0.0D) {
                        density = low;
                    } else if (selector > 1.0D) {
                        density = high;
                    } else {
                        density = low + (high - low) * selector;
                    }

                    density -= adjustedVertical;
                    if (y > height - 4) {
                        final double edge = (double)(y - (height - 4)) / 3.0D;
                        density = density * (1.0D - edge) + -10.0D * edge;
                    }
                    out[noiseIndex] = density;
                    ++noiseIndex;
                }
            }
        }
        return out;
    }

    private void applySurface(final ChunkData data, final int[] biomeIds,
                              final long seed, final int chunkX, final int chunkZ) {
        final NoiseState state = this.state(seed);
        final Random chunkRandom = new Random((long)chunkX * 341873128712L + (long)chunkZ * 132897987541L);
        final double[] stoneNoise = state.noiseGen4.generateNoise(
                null, chunkX * 16, chunkZ * 16, 16, 16, 0.0625D, 0.0625D);

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                final int biomeId = biomeIds[z + x * 16];
                final float temperature = biomeId == 12 || biomeId == 13 ? 0.0F : 0.8F;
                int thickness = (int)(stoneNoise[x + z * 16] / 3.0D + 3.0D + chunkRandom.nextDouble() * 0.25D);
                int runDepth = -1;
                Material top = topForBiome(biomeId);
                Material filler = fillerForBiome(biomeId);

                for (int y = 127; y >= 0; --y) {
                    final Material current = data.getType(x, y, z);
                    if (y <= chunkRandom.nextInt(5)) {
                        data.setBlock(x, y, z, BlockConstants.BEDROCK);
                    } else if (current == Material.AIR) {
                        runDepth = -1;
                    } else if (current == Material.STONE) {
                        if (runDepth == -1) {
                            if (thickness <= 0) {
                                top = Material.AIR;
                                filler = Material.STONE;
                            } else if (y >= SEA_LEVEL - 4 && y <= SEA_LEVEL + 1) {
                                top = topForBiome(biomeId);
                                filler = fillerForBiome(biomeId);
                            }

                            if (y < SEA_LEVEL && top == Material.AIR) {
                                top = temperature < 0.15F ? Material.ICE : Material.WATER;
                            }

                            runDepth = thickness;
                            data.setBlock(x, y, z, y >= SEA_LEVEL - 1 ? top : filler);
                        } else if (runDepth > 0) {
                            --runDepth;
                            data.setBlock(x, y, z, filler);
                            if (runDepth == 0 && filler == Material.SAND) {
                                runDepth = chunkRandom.nextInt(4);
                                filler = Material.SANDSTONE;
                            }
                        }
                    }
                }
            }
        }
    }

    private static Material topForBiome(final int biomeId) {
        switch (biomeId) {
            case 2:
            case 16:
            case 17:
                return Material.SAND;
            case 14:
            case 15:
                return Material.MYCELIUM;
            default:
                return Material.GRASS_BLOCK;
        }
    }

    private static Material fillerForBiome(final int biomeId) {
        switch (biomeId) {
            case 2:
            case 16:
            case 17:
                return Material.SAND;
            case 14:
            case 15:
                return Material.DIRT;
            default:
                return Material.DIRT;
        }
    }

    private static int densityIndex(final int x, final int z, final int y) {
        return (x * 5 + z) * 17 + y;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(final World world) {
        return Collections.singletonList(new V125BlockPopulator(this));
    }

    public void runPopulators(final long seed, final int chunkX, final int chunkZ, final BlockAccess access) {
        final int blockX = chunkX * 16;
        final int blockZ = chunkZ * 16;

        final Random random = new Random(seed);
        final long oddX = random.nextLong() / 2L * 2L + 1L;
        final long oddZ = random.nextLong() / 2L * 2L + 1L;
        random.setSeed((long) chunkX * oddX + (long) chunkZ * oddZ ^ seed);

        final V125StructureGenerator.Result structures =
                new V125StructureGenerator().generate(seed, chunkX, chunkZ, access, this.biomeSource, random);
        final int biomeId = this.biomeSource
                .getBlockBiomeIds(seed, blockX + 16, blockZ + 16, 1, 1)[0];
        final boolean villageStart = structures.hasVillage();

        /*
         * ChunkProviderGenerate.populate():
         * structures -> lakes -> dungeons -> BiomeDecorator -> ice/snow.
         */
        if (!villageStart && random.nextInt(4) == 0) {
            final int x = blockX + random.nextInt(16) + 8;
            final int y = random.nextInt(128);
            final int z = blockZ + random.nextInt(16) + 8;
            new WorldGenLakes173(BlockConstants.SOURCE_WATER).populate(access, random, x, y, z);
        }

        if (!villageStart && random.nextInt(8) == 0) {
            final int x = blockX + random.nextInt(16) + 8;
            final int y = random.nextInt(random.nextInt(120) + 8);
            final int z = blockZ + random.nextInt(16) + 8;
            if (y < SEA_LEVEL || random.nextInt(10) == 0) {
                new WorldGenLakes173(BlockConstants.SOURCE_LAVA).populate(access, random, x, y, z);
            }
        }

        for (int i = 0; i < 8; ++i) {
            final int x = blockX + random.nextInt(16) + 8;
            final int y = random.nextInt(128);
            final int z = blockZ + random.nextInt(16) + 8;
            new WorldGenDungeons173().populate(access, random, x, y, z);
        }

        decorateBiome(access, random, biomeId, blockX, blockZ);
        freezeAndSnow(access, biomeId, blockX, blockZ);
    }

    private void generateOres(final BlockAccess world, final Random random,
                              final int baseX, final int baseZ) {
        ore(world, random, new WorldGenMinable173(BlockConstants.DIRT, 32), 20, baseX, baseZ, 0, 128);
        ore(world, random, new WorldGenMinable173(BlockConstants.GRAVEL, 32), 10, baseX, baseZ, 0, 128);
        ore(world, random, new WorldGenMinable173(BlockConstants.COAL_ORE, 16), 20, baseX, baseZ, 0, 128);
        ore(world, random, new WorldGenMinable173(BlockConstants.IRON_ORE, 8), 20, baseX, baseZ, 0, 64);
        ore(world, random, new WorldGenMinable173(BlockConstants.GOLD_ORE, 8), 2, baseX, baseZ, 0, 32);
        ore(world, random, new WorldGenMinable173(BlockConstants.REDSTONE_ORE, 7), 8, baseX, baseZ, 0, 16);
        ore(world, random, new WorldGenMinable173(BlockConstants.DIAMOND_ORE, 7), 1, baseX, baseZ, 0, 16);

        final WorldGenMinable173 lapis = new WorldGenMinable173(BlockConstants.LAPIS_ORE, 6);
        final int x = baseX + random.nextInt(16);
        final int z = baseZ + random.nextInt(16);
        final int y = random.nextInt(16) + random.nextInt(16);
        lapis.populate(world, random, x, y, z);
    }

    private static void ore(final BlockAccess world, final Random random,
                            final WorldGenMinable173 generator, final int count,
                            final int baseX, final int baseZ,
                            final int minY, final int maxY) {
        for (int i = 0; i < count; ++i) {
            generator.populate(world, random,
                    baseX + random.nextInt(16),
                    random.nextInt(maxY - minY) + minY,
                    baseZ + random.nextInt(16));
        }
    }

    private void decorateBiome(final BlockAccess world, final Random random,
                               final int biomeId, final int baseX, final int baseZ) {
        // BiomeDecorator defaults, then biome-specific overrides from 1.2.5.
        int trees = 0;
        int flowers = 2;
        int grass = 1;
        int deadBush = 0;
        int mushrooms = 0;
        int reeds = 0;
        int cacti = 0;
        int waterlilies = 0;
        int bigMushrooms = 0;

        switch (biomeId) {
            case 1: // plains
                trees = -999;
                flowers = 4;
                grass = 10;
                break;
            case 2: // desert
                trees = -999;
                deadBush = 2;
                reeds = 50;
                cacti = 10;
                break;
            case 4: // forest
            case 18: // forest hills
                trees = 10;
                grass = 2;
                break;
            case 5: // taiga
            case 19: // taiga hills
                trees = 10;
                grass = 1;
                break;
            case 6: // swamp
                trees = 2;
                flowers = -999;
                deadBush = 1;
                mushrooms = 8;
                reeds = 10;
                waterlilies = 4;
                break;
            case 14: // mushroom island
            case 15: // mushroom shore
                trees = -100;
                flowers = -100;
                grass = -100;
                mushrooms = 1;
                bigMushrooms = 1;
                break;
            case 16: // beach
                trees = -999;
                deadBush = 0;
                reeds = 0;
                cacti = 0;
                break;
            case 21: // jungle
            case 22: // jungle hills
                trees = 50;
                flowers = 4;
                grass = 25;
                break;
            default:
                break;
        }

        // Ores are the first BiomeDecorator stage.
        generateOres(world, random, baseX, baseZ);

        // Then the 3 sand patches, clay patch, and final sand patch.
        for (int i = 0; i < 3; ++i) generateSandPatch(world, random, baseX, baseZ, 7);
        generateClayPatch(world, random, baseX, baseZ);
        generateSandPatch(world, random, baseX, baseZ, 7);

        int treeCount = trees;
        if (random.nextInt(10) == 0) ++treeCount;

        for (int i = 0; i < treeCount; ++i) {
            final int x = baseX + random.nextInt(16) + 8;
            final int z = baseZ + random.nextInt(16) + 8;
            final int y = Math.min(127, world.getHighestBlockYAt(x, z));
            generateTree(world, random, biomeId, x, y, z);
        }

        for (int i = 0; i < bigMushrooms; ++i) {
            final int x = baseX + random.nextInt(16) + 8;
            final int z = baseZ + random.nextInt(16) + 8;
            final int y = Math.min(127, world.getHighestBlockYAt(x, z));
            new WorldGenBigMushroom125().generate(world, random, x, y, z);
        }

        for (int i = 0; i < flowers; ++i) {
            int x = baseX + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            int z = baseZ + random.nextInt(16) + 8;
            new WorldGenFlowers173(BlockConstants.DANDELION).populate(world, random, x, y, z);

            if (random.nextInt(4) == 0) {
                x = baseX + random.nextInt(16) + 8;
                y = random.nextInt(128);
                z = baseZ + random.nextInt(16) + 8;
                new WorldGenFlowers173(BlockConstants.POPPY).populate(world, random, x, y, z);
            }
        }

        for (int i = 0; i < grass; ++i) {
            final int x = baseX + random.nextInt(16) + 8;
            final int y = random.nextInt(128);
            final int z = baseZ + random.nextInt(16) + 8;
            new WorldGenGrass173(BlockConstants.SHORT_GRASS).populate(world, random, x, y, z);
        }

        for (int i = 0; i < deadBush; ++i) {
            final int x = baseX + random.nextInt(16) + 8;
            final int y = random.nextInt(128);
            final int z = baseZ + random.nextInt(16) + 8;
            new WorldGenDeadBush173(BlockConstants.DEAD_BUSH).populate(world, random, x, y, z);
        }

        for (int i = 0; i < waterlilies; ++i) {
            final int x = baseX + random.nextInt(16) + 8;
            final int z = baseZ + random.nextInt(16) + 8;
            int y = random.nextInt(128);
            while (y > 0 && world.getType(x, y - 1, z) == Material.AIR) --y;
            new WorldGenWaterlily125().generate(world, random, x, y, z);
        }

        for (int i = 0; i < mushrooms; ++i) {
            if (random.nextInt(4) == 0) {
                final int x = baseX + random.nextInt(16) + 8;
                final int z = baseZ + random.nextInt(16) + 8;
                final int y = Math.min(127, world.getHighestBlockYAt(x, z));
                new WorldGenFlowers173(BlockConstants.BROWN_MUSHROOM).populate(world, random, x, y, z);
            }
            if (random.nextInt(8) == 0) {
                final int x = baseX + random.nextInt(16) + 8;
                final int y = random.nextInt(128);
                final int z = baseZ + random.nextInt(16) + 8;
                new WorldGenFlowers173(BlockConstants.RED_MUSHROOM).populate(world, random, x, y, z);
            }
        }

        // Two unconditional base mushroom attempts follow the extra-mushroom loop.
        if (random.nextInt(4) == 0) {
            new WorldGenFlowers173(BlockConstants.BROWN_MUSHROOM).populate(world, random,
                    baseX + random.nextInt(16) + 8, random.nextInt(128),
                    baseZ + random.nextInt(16) + 8);
        }
        if (random.nextInt(8) == 0) {
            new WorldGenFlowers173(BlockConstants.RED_MUSHROOM).populate(world, random,
                    baseX + random.nextInt(16) + 8, random.nextInt(128),
                    baseZ + random.nextInt(16) + 8);
        }

        for (int i = 0; i < reeds; ++i) generateReed(world, random, baseX, baseZ);
        for (int i = 0; i < 10; ++i) generateReed(world, random, baseX, baseZ);

        if (random.nextInt(32) == 0) {
            new WorldGenPumpkin173().populate(world, random,
                    baseX + random.nextInt(16) + 8, random.nextInt(128),
                    baseZ + random.nextInt(16) + 8);
        }

        for (int i = 0; i < cacti; ++i) {
            new WorldGenCactus173().populate(world, random,
                    baseX + random.nextInt(16) + 8, random.nextInt(128),
                    baseZ + random.nextInt(16) + 8);
        }

        // BiomeDecorator's late liquid springs.
        decorateLiquids(world, random, baseX, baseZ);

        // Desert wells are part of BiomeGenDesert.decorate().
        if ((biomeId == 2 || biomeId == 17) && random.nextInt(1000) == 0) {
            final int x = baseX + random.nextInt(16) + 8;
            final int z = baseZ + random.nextInt(16) + 8;
            new WorldGenDesertWell125().generate(world, random, x, Math.min(127, world.getHighestBlockYAt(x, z) + 1), z);
        }
    }

    private static void generateSandPatch(final BlockAccess world, final Random random,
                                          final int baseX, final int baseZ, final int radius) {
        final int x = baseX + random.nextInt(16) + 8;
        final int z = baseZ + random.nextInt(16) + 8;
        final int y = Math.min(127, world.getHighestBlockYAt(x, z));
        new WorldGenSand125(radius, Material.SAND).generate(world, random, x, y, z);
    }

    private static void generateClayPatch(final BlockAccess world, final Random random,
                                          final int baseX, final int baseZ) {
        final int x = baseX + random.nextInt(16) + 8;
        final int z = baseZ + random.nextInt(16) + 8;
        final int y = Math.min(127, world.getHighestBlockYAt(x, z));
        new WorldGenClay173(4).populate(world, random, x, y, z);
    }

    private static void generateTree(final BlockAccess world, final Random random,
                                     final int biomeId, final int x, final int y, final int z) {
        switch (biomeId) {
            case 6:
                // 1.2.5 BiomeGenSwamp returns the dedicated swamp tree.
                new WorldGenTrees173().populate(world, random, x, y, z);
                return;
            case 4:
            case 18:
                // BiomeGenForest: nextInt(5), then nextInt(10).
                if (random.nextInt(5) == 0) {
                    new WorldGenForest173().populate(world, random, x, y, z);
                } else if (random.nextInt(10) == 0) {
                    new WorldGenBigTree173().populate(world, random, x, y, z);
                } else {
                    new WorldGenTrees173().populate(world, random, x, y, z);
                }
                return;
            case 5:
            case 19:
                // BiomeGenTaiga: nextInt(3), then taiga1/taiga2.
                if (random.nextInt(3) == 0) {
                    new WorldGenTaiga1173().populate(world, random, x, y, z);
                } else {
                    new WorldGenTaiga2173().populate(world, random, x, y, z);
                }
                return;
            case 21:
            case 22:
                // BiomeGenJungle RNG branch: 10 / 2 / 3. The existing legacy
                // generators provide the corresponding modern-block shapes.
                if (random.nextInt(10) == 0) {
                    new WorldGenBigTree173().populate(world, random, x, y, z);
                } else if (random.nextInt(2) == 0) {
                    new WorldGenTrees173().populate(world, random, x, y, z);
                } else if (random.nextInt(3) == 0) {
                    new WorldGenBigTree173().populate(world, random, x, y, z);
                } else {
                    new WorldGenTrees173().populate(world, random, x, y, z);
                }
                return;
            default:
                new WorldGenTrees173().populate(world, random, x, y, z);
        }
    }

    private static void generateReed(final BlockAccess world, final Random random,
                                     final int baseX, final int baseZ) {
        final int x = baseX + random.nextInt(16) + 8;
        final int y = random.nextInt(128);
        final int z = baseZ + random.nextInt(16) + 8;
        new WorldGenReed173().populate(world, random, x, y, z);
    }

    private static void decorateLiquids(final BlockAccess world, final Random random,
                                        final int baseX, final int baseZ) {
        for (int i = 0; i < 50; ++i) {
            new WorldGenLiquids173(BlockConstants.SOURCE_WATER).populate(world, random,
                    baseX + random.nextInt(16) + 8,
                    random.nextInt(random.nextInt(120) + 8),
                    baseZ + random.nextInt(16) + 8);
        }
        for (int i = 0; i < 20; ++i) {
            new WorldGenLiquids173(BlockConstants.SOURCE_LAVA).populate(world, random,
                    baseX + random.nextInt(16) + 8,
                    random.nextInt(random.nextInt(random.nextInt(112) + 8) + 8),
                    baseZ + random.nextInt(16) + 8);
        }
    }

    private static void freezeAndSnow(final BlockAccess access,
                                      final int biomeId, final int baseX, final int baseZ) {
        final boolean cold = isCold(biomeId);
        for (int x = baseX + 8; x < baseX + 24; ++x) {
            for (int z = baseZ + 8; z < baseZ + 24; ++z) {
                final int top = Math.min(127, access.getHighestBlockYAt(x, z));
                if (top <= 0 || top > 127) continue;

                final Material precipitationBlock = access.getType(x, top - 1, z);
                if (cold && precipitationBlock == Material.WATER) {
                    access.setType(x, top - 1, z, Material.ICE, false);
                }

                if (cold
                        && (precipitationBlock == Material.GRASS_BLOCK || precipitationBlock == Material.DIRT)
                        && access.getType(x, top, z).isAir()) {
                    access.setType(x, top, z, Material.SNOW, false);
                }
            }
        }
    }

    private static boolean isCold(final int biomeId) {
        return biomeId == 5 || biomeId == 10 || biomeId == 11 || biomeId == 12 || biomeId == 13 || biomeId == 19;
    }
}
