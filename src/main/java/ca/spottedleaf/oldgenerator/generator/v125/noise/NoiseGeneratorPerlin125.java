package ca.spottedleaf.oldgenerator.generator.v125.noise;

import java.util.Random;

/**
 * Standalone Perlin noise implementation used by the 1.2.5 terrain port.
 * The implementation deliberately has no dependency on NMS.
 */
public final class NoiseGeneratorPerlin125 {
    private final int[] permutations = new int[512];
    private final double xOffset;
    private final double yOffset;
    private final double zOffset;

    public NoiseGeneratorPerlin125(final Random random) {
        this.xOffset = random.nextDouble() * 256.0D;
        this.yOffset = random.nextDouble() * 256.0D;
        this.zOffset = random.nextDouble() * 256.0D;

        final int[] p = new int[256];
        for (int i = 0; i < 256; ++i) {
            p[i] = i;
        }
        for (int i = 0; i < 256; ++i) {
            final int j = i + random.nextInt(256 - i);
            final int v = p[i];
            p[i] = p[j];
            p[j] = v;
        }
        for (int i = 0; i < 512; ++i) {
            this.permutations[i] = p[i & 255];
        }
    }

    private static double fade(final double t) {
        return t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
    }

    private static double lerp(final double t, final double a, final double b) {
        return a + t * (b - a);
    }

    private static double grad(final int hash, final double x, final double y, final double z) {
        final int h = hash & 15;
        final double u = h < 8 ? x : y;
        final double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    public double noise(final double x, final double y, final double z) {
        double xx = x + this.xOffset;
        double yy = y + this.yOffset;
        double zz = z + this.zOffset;

        final int xi = ((int)Math.floor(xx)) & 255;
        final int yi = ((int)Math.floor(yy)) & 255;
        final int zi = ((int)Math.floor(zz)) & 255;

        xx -= Math.floor(xx);
        yy -= Math.floor(yy);
        zz -= Math.floor(zz);

        final double u = fade(xx);
        final double v = fade(yy);
        final double w = fade(zz);

        final int a = this.permutations[xi] + yi;
        final int aa = this.permutations[a] + zi;
        final int ab = this.permutations[a + 1] + zi;
        final int b = this.permutations[xi + 1] + yi;
        final int ba = this.permutations[b] + zi;
        final int bb = this.permutations[b + 1] + zi;

        return lerp(w,
                lerp(v, lerp(u, grad(this.permutations[aa], xx, yy, zz),
                                grad(this.permutations[ba], xx - 1.0D, yy, zz)),
                        lerp(u, grad(this.permutations[ab], xx, yy - 1.0D, zz),
                                grad(this.permutations[bb], xx - 1.0D, yy - 1.0D, zz))),
                lerp(v, lerp(u, grad(this.permutations[aa + 1], xx, yy, zz - 1.0D),
                                grad(this.permutations[ba + 1], xx - 1.0D, yy, zz - 1.0D)),
                        lerp(u, grad(this.permutations[ab + 1], xx, yy - 1.0D, zz - 1.0D),
                                grad(this.permutations[bb + 1], xx - 1.0D, yy - 1.0D, zz - 1.0D)));
    }
}
