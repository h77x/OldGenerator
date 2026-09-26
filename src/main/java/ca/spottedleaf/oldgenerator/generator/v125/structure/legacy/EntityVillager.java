package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

public final class EntityVillager {
    private final World world;
    private final int profession;
    private double x;
    private double y;
    private double z;

    public EntityVillager(final World world) {
        this(world, 0);
    }

    public EntityVillager(final World world, final int profession) {
        this.world = world;
        this.profession = profession;
    }

    public void setLocationAndAngles(final double x, final double y, final double z,
                                     final float yaw, final float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public World getWorld() { return this.world; }
    public int getProfession() { return this.profession; }
    public double getX() { return this.x; }
    public double getY() { return this.y; }
    public double getZ() { return this.z; }
}
