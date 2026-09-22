package ca.spottedleaf.oldgenerator.generator.v125.noise;

import java.util.Random;

/**
 * Minecraft 1.2.5 NoiseGeneratorOctaves port with the original frequency and
 * coordinate-wrapping semantics.
 */
public final class NoiseGeneratorOctaves125 {
    private final NoiseGeneratorPerlin125[] octaves;

    public NoiseGeneratorOctaves125(final Random random, final int octaveCount) {
        this.octaves = new NoiseGeneratorPerlin125[octaveCount];
        for (int i = 0; i < octaveCount; ++i) {
            this.octaves[i] = new NoiseGeneratorPerlin125(random);
        }
    }

    public double[] generateNoise(final double[] output,
                                  final int x, final int y, final int z,
                                  final int xSize, final int ySize, final int zSize,
                                  final double scaleX, final double scaleY, final double scaleZ) {
        final int size = xSize * ySize * zSize;
        final double[] target = output != null && output.length >= size ? output : new double[size];
        java.util.Arrays.fill(target, 0.0D);

        double frequency = 1.0D;
        for (NoiseGeneratorPerlin125 octave : this.octaves) {
            double sampleX = x * frequency * scaleX;
            double sampleY = y * frequency * scaleY;
            double sampleZ = z * frequency * scaleZ;

            long floorX = floor(sampleX);
            long floorZ = floor(sampleZ);
            sampleX -= floorX;
            sampleZ -= floorZ;

            floorX %= 16777216L;
            floorZ %= 16777216L;
            sampleX += floorX;
            sampleZ += floorZ;

            octave.generateNoise(target, sampleX, sampleY, sampleZ,
                    xSize, ySize, zSize,
                    scaleX * frequency, scaleY * frequency, scaleZ * frequency,
                    frequency);
            frequency /= 2.0D;
        }
        return target;
    }

    public double[] generateNoise(final double[] output,
                                  final int x, final int z,
                                  final int xSize, final int zSize,
                                  final double scaleX, final double scaleZ) {
        return this.generateNoise(output, x, 10, z, xSize, 1, zSize, scaleX, 1.0D, scaleZ);
    }

    private static long floor(final double value) {
        final long integer = (long)value;
        return value < integer ? integer - 1L : integer;
    }

    public double sample(final double x, final double y, final double z,
                         final double scaleX, final double scaleY, final double scaleZ) {
        final double[] out = new double[1];
        this.generateNoise(out, (int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z),
                1, 1, 1, scaleX, scaleY, scaleZ);
        return out[0];
    }
}
