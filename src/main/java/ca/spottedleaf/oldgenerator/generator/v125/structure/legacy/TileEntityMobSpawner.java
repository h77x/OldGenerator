package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

public final class TileEntityMobSpawner {
    private final World world;
    private final int x, y, z;
    private String mobID = "Pig";

    public TileEntityMobSpawner(final World world, final int x, final int y, final int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getMobID() { return mobID; }

    public void setMobID(final String id) {
        mobID = id;
        world.syncSpawner(x, y, z, mobID);
    }
}
