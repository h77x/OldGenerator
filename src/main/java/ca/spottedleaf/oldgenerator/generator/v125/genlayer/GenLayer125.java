package ca.spottedleaf.oldgenerator.generator.v125.genlayer;

public abstract class GenLayer125 {
    protected GenLayer125 parent;
    private final long baseSeed;
    private long worldGenSeed;
    private long chunkSeed;

    protected GenLayer125(final long seed) {
        long s = seed;
        s = s * s * 6364136223846793005L + 1442695040888963407L;
        s += seed;
        s = s * s * 6364136223846793005L + 1442695040888963407L;
        s += seed;
        s = s * s * 6364136223846793005L + 1442695040888963407L;
        s += seed;
        this.baseSeed = s;
    }

    public void initWorldGenSeed(final long seed) {
        this.worldGenSeed = seed;
        if (this.parent != null) {
            this.parent.initWorldGenSeed(seed);
        }
        this.worldGenSeed = this.worldGenSeed * this.worldGenSeed * 6364136223846793005L + 1442695040888963407L + this.baseSeed;
        this.worldGenSeed = this.worldGenSeed * this.worldGenSeed * 6364136223846793005L + 1442695040888963407L + this.baseSeed;
        this.worldGenSeed = this.worldGenSeed * this.worldGenSeed * 6364136223846793005L + 1442695040888963407L + this.baseSeed;
    }

    protected final void initChunkSeed(final long x, final long z) {
        this.chunkSeed = this.worldGenSeed;
        this.chunkSeed = this.chunkSeed * this.chunkSeed * 6364136223846793005L + 1442695040888963407L + x;
        this.chunkSeed = this.chunkSeed * this.chunkSeed * 6364136223846793005L + 1442695040888963407L + z;
        this.chunkSeed = this.chunkSeed * this.chunkSeed * 6364136223846793005L + 1442695040888963407L + x;
        this.chunkSeed = this.chunkSeed * this.chunkSeed * 6364136223846793005L + 1442695040888963407L + z;
    }

    protected final int nextInt(final int bound) {
        int value = (int)((this.chunkSeed >> 24) % bound);
        if (value < 0) {
            value += bound;
        }
        this.chunkSeed = this.chunkSeed * this.chunkSeed * 6364136223846793005L + 1442695040888963407L + this.worldGenSeed;
        return value;
    }

    protected final int choose(final int a, final int b) {
        return this.nextInt(2) == 0 ? a : b;
    }

    protected final int choose4(final int a, final int b, final int c, final int d) {
        final int v = this.nextInt(4);
        return v == 0 ? a : (v == 1 ? b : (v == 2 ? c : d));
    }

    public abstract int[] getInts(int x, int z, int width, int height);

    public static GenLayer125 zoom(final long seed, GenLayer125 parent, final int times) {
        GenLayer125 layer = parent;
        for (int i = 0; i < times; ++i) {
            layer = new Zoom(seed + i, layer);
        }
        return layer;
    }

    private static final class Zoom extends GenLayer125 {
        private Zoom(final long seed, final GenLayer125 parent) {
            super(seed);
            this.parent = parent;
        }

        @Override
        public int[] getInts(final int x, final int z, final int width, final int height) {
            final int px = x >> 1;
            final int pz = z >> 1;
            final int pw = (width >> 1) + 3;
            final int ph = (height >> 1) + 3;
            final int[] parentValues = this.parent.getInts(px, pz, pw, ph);
            final int[] zoomed = new int[pw * 2 * ph * 2];
            final int stride = pw << 1;

            for (int iz = 0; iz < ph - 1; ++iz) {
                final int row = iz << 1;
                int northWest = parentValues[iz * pw];
                int southWest = parentValues[(iz + 1) * pw];
                int out = row * stride;
                for (int ix = 0; ix < pw - 1; ++ix) {
                    this.initChunkSeed(((long)(ix + px)) << 1, ((long)(iz + pz)) << 1);
                    final int northEast = parentValues[ix + 1 + iz * pw];
                    final int southEast = parentValues[ix + 1 + (iz + 1) * pw];
                    zoomed[out] = northWest;
                    zoomed[out + stride] = this.choose(northWest, southWest);
                    zoomed[++out] = this.choose(northWest, northEast);
                    zoomed[out + stride] = this.func35514b(northWest, northEast, southWest, southEast);
                    ++out;
                    northWest = northEast;
                    southWest = southEast;
                }
            }

            final int[] result = new int[width * height];
            final int offsetX = x & 1;
            final int offsetZ = z & 1;
            final int zoomStride = pw << 1;
            for (int iz = 0; iz < height; ++iz) {
                System.arraycopy(zoomed, (iz + offsetZ) * zoomStride + offsetX,
                        result, iz * width, width);
            }
            return result;
        }

        private int func35514b(final int a, final int b, final int c, final int d) {
            if (b == c && c == d) return b;
            if (a == b && a == c) return a;
            if (a == b && a == d) return a;
            if (a == c && a == d) return a;
            if (a == b && c != d) return a;
            if (a == c && b != d) return a;
            if (a == d && b != c) return a;
            if (b == a && c != d) return b;
            if (b == c && a != d) return b;
            if (b == d && a != c) return b;
            if (c == a && b != d) return c;
            if (c == b && a != d) return c;
            if (c == d && a != b) return c;
            if (d == a && b != c) return c;
            if (d == b && a != c) return c;
            if (d == c && a != b) return c;
            return this.choose4(a, b, c, d);
        }
    }
}
