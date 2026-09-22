package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

public class Material {
    private final boolean solid;
    private final boolean liquid;

    public static final Material water = new Material(false, true);
    public static final Material lava = new Material(false, true);

    public Material(final boolean solid, final boolean liquid) {
        this.solid = solid;
        this.liquid = liquid;
    }

    public boolean isLiquid() { return this.liquid; }
    public boolean isSolid() { return this.solid; }
    public boolean blocksMovement() { return this.solid; }
    public boolean isOpaque() { return this.solid; }
}
