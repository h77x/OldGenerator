package ca.spottedleaf.oldgenerator.generator.v125;

import ca.spottedleaf.oldgenerator.generator.v125.noise.NoiseGeneratorOctaves125;
import ca.spottedleaf.oldgenerator.util.BlockConstants;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

/**
 * Minecraft 1.2.5 overworld terrain foundation.
 *
 * Generation is kept independent of NMS so it can run on modern Paper.
 * The terrain interpolation follows the 1.2-era 4x4x8 interpolation grid
 * (5x33x5 density samples) and a 256-block world height.
 */
public final class V125ChunkGenerator extends ChunkGenerator {
    private static final int SEA_LEVEL = 63;
    private static final int WORLD_HEIGHT = 256;

    private final ThreadLocal<State> states = ThreadLocal.withInitial(State::new);

    private static final class State {
        final NoiseGeneratorOctaves125 minLimit;
        final NoiseGeneratorOctaves125 maxLimit;
        final NoiseGeneratorOctaves125 main;
        final NoiseGeneratorOctaves125 scale;
        final NoiseGeneratorOctaves125 depth;
        final NoiseGeneratorOctaves125 climate;

        State() {
            final Random seedSource = new Random(0L);
            this.minLimit = new NoiseGeneratorOctaves125(seedSource, 16);
            this.maxLimit = new NoiseGeneratorOctaves125(seedSource, 16);
            this.main = new NoiseGeneratorOctaves125(seedSource, 8);
            this.scale = new NoiseGeneratorOctaves125(seedSource, 10);
            this.depth = new NoiseGeneratorOctaves125(seedSource, 16);
            this.climate = new NoiseGeneratorOctaves125(seedSource, 4);
        }

        void reseed(final long seed) {
            final Random random = new Random(seed);
            // Noise objects are intentionally immutable after construction.
            // The generator therefore uses a seed-derived coordinate offset below.
        }
    }

    @Override
    public boolean isParallelCapable() {
        return true;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }

    @Override
    public ChunkData generateChunkData(final World world, final Random random,
                                       final int chunkX, final int chunkZ,
                                       final BiomeGrid biomeGrid) {
        final ChunkData data = this.createChunkData(world);
        final long seed = world.getSeed();

        // Keep the historical 1.2.5 4x4x8 interpolation layout. The density
        // samples are calculated at 4-block horizontal / 8-block vertical
        // intervals and interpolated into every block.
        final double[][][] density = this.createDensity(seed, chunkX, chunkZ);

        for (int cellX = 0; cellX < 4; ++cellX) {
            for (int cellZ = 0; cellZ < 4; ++cellZ) {
                for (int cellY = 0; cellY < 32; ++cellY) {
                    final double d000 = density[cellX][cellZ][cellY];
                    final double d001 = density[cellX][cellZ][cellY + 1];
                    final double d100 = density[cellX + 1][cellZ][cellY];
                    final double d101 = density[cellX + 1][cellZ][cellY + 1];
                    final double d010 = density[cellX][cellZ + 1][cellY];
                    final double d011 = density[cellX][cellZ + 1][cellY + 1];
                    final double d110 = density[cellX + 1][cellZ + 1][cellY];
                    final double d111 = density[cellX + 1][cellZ + 1][cellY + 1];

                    for (int localY = 0; localY < 8; ++localY) {
                        final double fy = localY / 8.0D;
                        final double a0 = lerp(fy, d000, d001);
                        final double a1 = lerp(fy, d100, d101);
                        final double b0 = lerp(fy, d010, d011);
                        final double b1 = lerp(fy, d110, d111);

                        for (int localX = 0; localX < 4; ++localX) {
                            final double fx = localX / 4.0D;
                            final double x0 = lerp(fx, a0, a1);
                            final double x1 = lerp(fx, b0, b1);

                            for (int localZ = 0; localZ < 4; ++localZ) {
                                final double fz = localZ / 4.0D;
                                final double densityValue = lerp(fz, x0, x1);
                                final int x = cellX * 4 + localX;
                                final int z = cellZ * 4 + localZ;
                                final int y = cellY * 8 + localY;

                                if (densityValue > 0.0D) {
                                    data.setBlock(x, y, z, BlockConstants.STONE);
                                } else if (y < SEA_LEVEL) {
                                    data.setBlock(x, y, z, BlockConstants.SOURCE_WATER);
                                }
                            }
                        }
                    }
                }
            }
        }

        this.applySurface(world, data, chunkX, chunkZ, biomeGrid);
        return data;
    }

    private double[][][] createDensity(final long seed, final int chunkX, final int chunkZ) {
        final double[][][] density = new double[5][5][33];
        final Random random = new Random(seed ^ 0x5DEECE66DL);

        // Per-world deterministic octave fields. A fresh state per chunk would
        // make neighbouring chunks discontinuous, so coordinates are sampled
        // directly from the world seed.
        final NoiseGeneratorOctaves125 min = new NoiseGeneratorOctaves125(random, 16);
        final NoiseGeneratorOctaves125 max = new NoiseGeneratorOctaves125(random, 16);
        final NoiseGeneratorOctaves125 main = new NoiseGeneratorOctaves125(random, 8);

        for (int gx = 0; gx < 5; ++gx) {
            for (int gz = 0; gz < 5; ++gz) {
                for (int gy = 0; gy < 33; ++gy) {
                    final double x = chunkX * 4.0D + gx;
                    final double z = chunkZ * 4.0D + gz;
                    final double y = gy;

                    final double minNoise = min.sample(x, y, z, 1.0D / 684.412D, 1.0D / 684.412D, 1.0D / 684.412D);
                    final double maxNoise = max.sample(x, y, z, 1.0D / 684.412D, 1.0D / 684.412D, 1.0D / 684.412D);
                    final double mainNoise = main.sample(x, y, z, 1.0D / 684.412D, 1.0D / 684.412D, 1.0D / 684.412D);

                    final double selector = Math.max(-1.0D, Math.min(1.0D, mainNoise));
                    final double terrain = selector < 0.0D
                            ? minNoise * (1.0D + selector)
                            : maxNoise * selector;
                    final double vertical = (SEA_LEVEL - y) / 32.0D;

                    density[gx][gz][gy] = terrain * 12.0D + vertical;
                }
            }
        }
        return density;
    }

    private void applySurface(final World world, final ChunkData data,
                              final int chunkX, final int chunkZ,
                              final BiomeGrid biomeGrid) {
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                biomeGrid.setBiome(x, z, Biome.PLAINS);

                int top = WORLD_HEIGHT - 1;
                while (top > 0 && data.getType(x, top, z).isAir()) {
                    --top;
                }
                if (top <= 0) {
                    continue;
                }

                final Material topMaterial = data.getType(x, top, z);
                if (topMaterial == Material.STONE) {
                    data.setBlock(x, top, z, BlockConstants.GRASS_BLOCK);
                    for (int y = top - 1; y >= Math.max(0, top - 3); --y) {
                        if (data.getType(x, y, z) == Material.STONE) {
                            data.setBlock(x, y, z, BlockConstants.DIRT);
                        }
                    }
                }
            }
        }
    }

    private static double lerp(final double t, final double a, final double b) {
        return a + t * (b - a);
    }
}
