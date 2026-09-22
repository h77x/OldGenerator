package ca.spottedleaf.oldgenerator.generator.v125.genlayer;

public final class Zoom125 extends GenLayer125 {
    public Zoom125(final long seed, final GenLayer125 parent) {
        super(seed);
        this.parent = parent;
    }

    @Override
    public int[] getInts(final int x, final int z, final int width, final int height) {
        final int parentX = x >> 1;
        final int parentZ = z >> 1;
        final int parentWidth = (width >> 1) + 3;
        final int parentHeight = (height >> 1) + 3;
        final int[] parentValues = this.parent.getInts(parentX, parentZ, parentWidth, parentHeight);
        final int expandedWidth = parentWidth << 1;
        final int[] expanded = new int[expandedWidth * (parentHeight << 1)];

        for (int iz = 0; iz < parentHeight - 1; ++iz) {
            int out = (iz << 1) * expandedWidth;
            int northWest = parentValues[iz * parentWidth];
            int southWest = parentValues[(iz + 1) * parentWidth];

            for (int ix = 0; ix < parentWidth - 1; ++ix) {
                this.initChunkSeed(((long)(ix + parentX)) << 1, ((long)(iz + parentZ)) << 1);
                final int northEast = parentValues[ix + 1 + iz * parentWidth];
                final int southEast = parentValues[ix + 1 + (iz + 1) * parentWidth];

                expanded[out] = northWest;
                expanded[out + expandedWidth] = this.choose(northWest, southWest);
                ++out;
                expanded[out] = this.choose(northWest, northEast);
                expanded[out + expandedWidth] = this.choose4(northWest, northEast, southWest, southEast);
                ++out;

                northWest = northEast;
                southWest = southEast;
            }
        }

        final int[] result = new int[width * height];
        final int sourceX = x & 1;
        final int sourceZ = z & 1;
        for (int iz = 0; iz < height; ++iz) {
            System.arraycopy(expanded,
                    (iz + sourceZ) * expandedWidth + sourceX,
                    result, iz * width, width);
        }
        return result;
    }
}
