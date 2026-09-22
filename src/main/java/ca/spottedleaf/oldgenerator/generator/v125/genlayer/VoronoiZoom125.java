package ca.spottedleaf.oldgenerator.generator.v125.genlayer;

public final class VoronoiZoom125 extends GenLayer125 {
    public VoronoiZoom125(final long seed, final GenLayer125 parent) {
        super(seed);
        this.parent = parent;
    }

    @Override
    public int[] getInts(final int x, final int z, final int width, final int height) {
        final int shiftedX = x - 2;
        final int shiftedZ = z - 2;
        final int shift = 2;
        final int scale = 1 << shift;
        final int parentX = shiftedX >> shift;
        final int parentZ = shiftedZ >> shift;
        final int parentWidth = (width >> shift) + 3;
        final int parentHeight = (height >> shift) + 3;
        final int[] parentValues = this.parent.getInts(parentX, parentZ, parentWidth, parentHeight);
        final int expandedWidth = parentWidth << shift;
        final int expandedHeight = parentHeight << shift;
        final int[] expanded = new int[expandedWidth * expandedHeight];

        for (int iz = 0; iz < parentHeight - 1; ++iz) {
            int topLeft = parentValues[iz * parentWidth];
            int bottomLeft = parentValues[(iz + 1) * parentWidth];

            for (int ix = 0; ix < parentWidth - 1; ++ix) {
                final double jitter = scale * 0.9D;

                this.initChunkSeed((long)((ix + parentX) << shift), (long)((iz + parentZ) << shift));
                final double x0 = (this.nextInt(1024) / 1024.0D - 0.5D) * jitter;
                final double z0 = (this.nextInt(1024) / 1024.0D - 0.5D) * jitter;

                this.initChunkSeed((long)((ix + parentX + 1) << shift), (long)((iz + parentZ) << shift));
                final double x1 = (this.nextInt(1024) / 1024.0D - 0.5D) * jitter + scale;
                final double z1 = (this.nextInt(1024) / 1024.0D - 0.5D) * jitter;

                this.initChunkSeed((long)((ix + parentX) << shift), (long)((iz + parentZ + 1) << shift));
                final double x2 = (this.nextInt(1024) / 1024.0D - 0.5D) * jitter;
                final double z2 = (this.nextInt(1024) / 1024.0D - 0.5D) * jitter + scale;

                this.initChunkSeed((long)((ix + parentX + 1) << shift), (long)((iz + parentZ + 1) << shift));
                final double x3 = (this.nextInt(1024) / 1024.0D - 0.5D) * jitter + scale;
                final double z3 = (this.nextInt(1024) / 1024.0D - 0.5D) * jitter + scale;

                final int topRight = parentValues[ix + 1 + iz * parentWidth];
                final int bottomRight = parentValues[ix + 1 + (iz + 1) * parentWidth];

                for (int localZ = 0; localZ < scale; ++localZ) {
                    int out = ((iz << shift) + localZ) * expandedWidth + (ix << shift);
                    for (int localX = 0; localX < scale; ++localX) {
                        final double d0 = (localX - z0) * (localX - z0) + (localZ - x0) * (localZ - x0);
                        final double d1 = (localX - z1) * (localX - z1) + (localZ - x1) * (localZ - x1);
                        final double d2 = (localX - z2) * (localX - z2) + (localZ - x2) * (localZ - x2);
                        final double d3 = (localX - z3) * (localX - z3) + (localZ - x3) * (localZ - x3);

                        if (d0 < d1 && d0 < d2 && d0 < d3) {
                            expanded[out++] = topLeft;
                        } else if (d1 < d0 && d1 < d2 && d1 < d3) {
                            expanded[out++] = topRight;
                        } else if (d2 < d0 && d2 < d1 && d2 < d3) {
                            expanded[out++] = bottomLeft;
                        } else {
                            expanded[out++] = bottomRight;
                        }
                    }
                }

                topLeft = topRight;
                bottomLeft = bottomRight;
            }
        }

        final int[] result = new int[width * height];
        final int offsetX = shiftedX & (scale - 1);
        final int offsetZ = shiftedZ & (scale - 1);
        for (int row = 0; row < height; ++row) {
            System.arraycopy(expanded,
                    (row + offsetZ) * expandedWidth + offsetX,
                    result, row * width, width);
        }
        return result;
    }
}
