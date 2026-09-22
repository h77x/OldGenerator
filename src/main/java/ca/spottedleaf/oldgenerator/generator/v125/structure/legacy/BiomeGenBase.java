package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

public class BiomeGenBase {
    public static final BiomeGenBase[] biomeList=new BiomeGenBase[256];
    public static final BiomeGenBase ocean=of(0), plains=of(1), desert=of(2), extremeHills=of(3),
        forest=of(4), taiga=of(5), swampland=of(6), river=of(7), hell=of(8), sky=of(9),
        frozenOcean=of(10), frozenRiver=of(11), icePlains=of(12), iceMountains=of(13),
        mushroomIsland=of(14), mushroomIslandShore=of(15), beach=of(16), desertHills=of(17),
        forestHills=of(18), taigaHills=of(19), extremeHillsEdge=of(20), jungle=of(21), jungleHills=of(22);
    public final int biomeID;
    public byte topBlock = (byte)Block.grass.blockID;
    public byte fillerBlock = (byte)Block.dirt.blockID;
    private BiomeGenBase(int id){this.biomeID=id; biomeList[id]=this;}
    private static BiomeGenBase of(int id){return new BiomeGenBase(id);}
    static {
        mushroomIsland.topBlock = (byte)Block.mycelium.blockID;
        mushroomIslandShore.topBlock = (byte)Block.mycelium.blockID;
        desert.topBlock = (byte)Block.sand.blockID;
        desert.fillerBlock = (byte)Block.sand.blockID;
        beach.topBlock = (byte)Block.sand.blockID;
        beach.fillerBlock = (byte)Block.sand.blockID;
    }
}
