package ca.spottedleaf.oldgenerator.generator.v125.noise;

import java.util.Random;

public final class NoiseGeneratorOctaves125 {
    private final NoiseGeneratorPerlin125[] octaves;

    public NoiseGeneratorOctaves125(final Random random, final int octaveCount) {
        this.octaves = new NoiseGeneratorPerlin125[octaveCount];
        for (int i = 0; i < octaveCount; ++i) {
            this.octaves[i] = new NoiseGeneratorPerlin125(random);
        }
    }

    public double sample(final double x, final double y, final double z, final double scaleX, final double scaleY, final double scaleZ) {
        double result = 0.0D;
        double amplitude = 1.0D;
        double frequency = 1.0D;

        for (final NoiseGeneratorPerlin125 octave : this.octaves) {
            result += octave.noise(x * scaleX * frequency, y * scaleY * frequency, z * scaleZ * frequency) * amplitude;
            frequency *= 2.0D;
            amplitude *= 0.5D;
        }
        return result;
    }
}
