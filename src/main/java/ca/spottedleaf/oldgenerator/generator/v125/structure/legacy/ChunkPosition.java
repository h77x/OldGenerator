package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

public final class ChunkPosition {
    public final int x,y,z;
    public ChunkPosition(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
    public boolean equals(Object o){return o instanceof ChunkPosition p && p.x==x && p.y==y && p.z==z;}
    public int hashCode(){return x*8976890+y*981131+z;}
}
