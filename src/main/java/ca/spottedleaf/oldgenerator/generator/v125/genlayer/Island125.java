package ca.spottedleaf.oldgenerator.generator.v125.genlayer;

public final class Island125 extends GenLayer125 {
    public Island125(final long seed) { super(seed); }
    @Override public int[] getInts(final int x, final int z, final int width, final int height) {
        final int[] out = new int[width * height];
        for (int iz = 0; iz < height; ++iz) {
            for (int ix = 0; ix < width; ++ix) {
                this.initChunkSeed(x + ix, z + iz);
                out[ix + iz * width] = this.nextInt(10) == 0 ? 1 : 0;
            }
        }
        if (x > -width && x <= 0 && z > -height && z <= 0) {
            out[-x + -z * width] = 1;
        }
        return out;
    }
}
