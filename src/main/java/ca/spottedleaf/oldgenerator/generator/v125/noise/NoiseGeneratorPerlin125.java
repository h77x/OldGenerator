package ca.spottedleaf.oldgenerator.generator.v125.noise;

import java.util.Random;

/**
 * Source-faithful Minecraft 1.2.5 NoiseGeneratorPerlin.
 *
 * This intentionally keeps the old permutation construction and octave
 * accumulation semantics instead of using a modernised Perlin implementation.
 */
public final class NoiseGeneratorPerlin125 {
    private static final double[] GRAD_X = {
        1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D,
        0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D, -1.0D, 0.0D
    };
    private static final double[] GRAD_Y = {
        1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D,
        1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D
    };
    private static final double[] GRAD_Z = {
        0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, -1.0D, -1.0D,
        1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 1.0D, 0.0D, -1.0D
    };

    private final int[] permutation = new int[512];
    private final double xOffset;
    private final double yOffset;
    private final double zOffset;

    public NoiseGeneratorPerlin125(final Random random) {
        this.xOffset = random.nextDouble() * 256.0D;
        this.yOffset = random.nextDouble() * 256.0D;
        this.zOffset = random.nextDouble() * 256.0D;

        for (int i = 0; i < 256; ++i) {
            this.permutation[i] = i;
        }

        for (int i = 0; i < 256; ++i) {
            final int j = random.nextInt(256 - i) + i;
            final int k = this.permutation[i];
            this.permutation[i] = this.permutation[j];
            this.permutation[j] = k;
            this.permutation[i + 256] = this.permutation[i];
        }
    }

    private static double lerp(final double t, final double a, final double b) {
        return a + t * (b - a);
    }

    private static double grad2(final int hash, final double x, final double y) {
        final int index = hash & 15;
        return GRAD_X[index] * x + GRAD_Y[index] * y;
    }

    private static double grad3(final int hash, final double x, final double y, final double z) {
        final int index = hash & 15;
        return GRAD_X[index] * x + GRAD_Y[index] * y + GRAD_Z[index] * z;
    }

    private static double floor(final double value) {
        final int integer = (int)value;
        return value < integer ? integer - 1.0D : integer;
    }

    public void generateNoise(final double[] output,
                              final double x, final double y, final double z,
                              final int xSize, final int ySize, final int zSize,
                              final double scaleX, final double scaleY, final double scaleZ,
                              final double persistence) {
        if (ySize == 1) {
            int outIndex = 0;
            final double inversePersistence = 1.0D / persistence;

            for (int ix = 0; ix < xSize; ++ix) {
                final double sampleX = x + ix * scaleX + this.xOffset;
                int floorX = (int)sampleX;
                if (sampleX < floorX) {
                    --floorX;
                }
                final int permX = floorX & 255;
                final double fracX = sampleX - floorX;
                final double fadeX = fracX * fracX * fracX * (fracX * (fracX * 6.0D - 15.0D) + 10.0D);

                for (int iz = 0; iz < zSize; ++iz) {
                    final double sampleZ = z + iz * scaleZ + this.zOffset;
                    int floorZ = (int)sampleZ;
                    if (sampleZ < floorZ) {
                        --floorZ;
                    }
                    final int permZ = floorZ & 255;
                    final double fracZ = sampleZ - floorZ;
                    final double fadeZ = fracZ * fracZ * fracZ * (fracZ * (fracZ * 6.0D - 15.0D) + 10.0D);

                    final int a = this.permutation[permX];
                    final int aa = this.permutation[a + permZ];
                    final int b = this.permutation[permX + 1];
                    final int ba = this.permutation[b + permZ];

                    final double x0 = lerp(fadeX,
                            grad3(this.permutation[aa], fracX, 0.0D, fracZ),
                            grad3(this.permutation[ba], fracX - 1.0D, 0.0D, fracZ));
                    final double x1 = lerp(fadeX,
                            grad3(this.permutation[aa + 1], fracX, 0.0D, fracZ - 1.0D),
                            grad3(this.permutation[ba + 1], fracX - 1.0D, 0.0D, fracZ - 1.0D));
                    output[outIndex++] += lerp(fadeZ, x0, x1) * inversePersistence;
                }
            }
            return;
        }

        int outIndex = 0;
        final double inversePersistence = 1.0D / persistence;

        int lastYPermutation = -1;
        double x0 = 0.0D;
        double x1 = 0.0D;
        double z0 = 0.0D;
        double z1 = 0.0D;

        for (int ix = 0; ix < xSize; ++ix) {
            final double sampleX = x + ix * scaleX + this.xOffset;
            int floorX = (int)sampleX;
            if (sampleX < floorX) {
                --floorX;
            }
            final int permX = floorX & 255;
            final double fracX = sampleX - floorX;
            final double fadeX = fracX * fracX * fracX * (fracX * (fracX * 6.0D - 15.0D) + 10.0D);

            for (int iz = 0; iz < zSize; ++iz) {
                final double sampleZ = z + iz * scaleZ + this.zOffset;
                int floorZ = (int)sampleZ;
                if (sampleZ < floorZ) {
                    --floorZ;
                }
                final int permZ = floorZ & 255;
                final double fracZ = sampleZ - floorZ;
                final double fadeZ = fracZ * fracZ * fracZ * (fracZ * (fracZ * 6.0D - 15.0D) + 10.0D);

                for (int iy = 0; iy < ySize; ++iy) {
                    final double sampleY = y + iy * scaleY + this.yOffset;
                    int floorY = (int)sampleY;
                    if (sampleY < floorY) {
                        --floorY;
                    }
                    final int permY = floorY & 255;
                    final double fracY = sampleY - floorY;
                    final double fadeY = fracY * fracY * fracY * (fracY * (fracY * 6.0D - 15.0D) + 10.0D);

                    if (iy == 0 || permY != lastYPermutation) {
                        lastYPermutation = permY;
                        // Match the 1.2.5 lookup order exactly:
                        // permutation[x] -> +y -> permutation[...] -> +z.
                        // The previous implementation incorrectly added y and z
                        // before the second permutation lookup, producing indices
                        // above 511 and crashing during terrain generation.
                        final int p00 = this.permutation[permX] + permY;
                        final int p10 = this.permutation[permX + 1] + permY;
                        final int y00 = this.permutation[p00] + permZ;
                        final int y10 = this.permutation[p10] + permZ;
                        final int y01 = this.permutation[p00 + 1] + permZ;
                        final int y11 = this.permutation[p10 + 1] + permZ;

                        x0 = lerp(fadeX,
                                grad3(this.permutation[y00], fracX, fracY, fracZ),
                                grad3(this.permutation[y10], fracX - 1.0D, fracY, fracZ));
                        x1 = lerp(fadeX,
                                grad3(this.permutation[y01], fracX, fracY, fracZ - 1.0D),
                                grad3(this.permutation[y11], fracX - 1.0D, fracY, fracZ - 1.0D));

                        z0 = lerp(fadeX,
                                grad3(this.permutation[y00 + 1], fracX, fracY - 1.0D, fracZ),
                                grad3(this.permutation[y10 + 1], fracX - 1.0D, fracY - 1.0D, fracZ));
                        z1 = lerp(fadeX,
                                grad3(this.permutation[y01 + 1], fracX, fracY - 1.0D, fracZ - 1.0D),
                                grad3(this.permutation[y11 + 1], fracX - 1.0D, fracY - 1.0D, fracZ - 1.0D));
                    }

                    final double y0 = lerp(fadeY, x0, z0);
                    final double y1 = lerp(fadeY, x1, z1);
                    output[outIndex++] += lerp(fadeZ, y0, y1) * inversePersistence;
                }
            }
        }
    }
}
