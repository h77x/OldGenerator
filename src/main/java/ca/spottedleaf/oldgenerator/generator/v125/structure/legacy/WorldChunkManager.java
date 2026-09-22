package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

import ca.spottedleaf.oldgenerator.generator.v125.V125BiomeSource;
import java.util.List;

public final class WorldChunkManager {
    private final V125BiomeSource source;
    private final long seed;
    public WorldChunkManager(V125BiomeSource source,long seed){this.source=source;this.seed=seed;}
    public BiomeGenBase getBiomeGenAt(int x,int z){int id=source.getBlockBiomeIds(seed,x,z,1,1)[0];return BiomeGenBase.biomeList[Math.max(0,Math.min(255,id))];}
    public boolean areBiomesViable(int x,int z,int range,List allowed){
        int minX=x-range>>2,minZ=z-range>>2,maxX=x+range>>2,maxZ=z+range>>2;
        int w=maxX-minX+1,h=maxZ-minZ+1;
        int[] ids=source.getBiomeIds(seed,minX,minZ,w,h);
        for(int id:ids){BiomeGenBase b=BiomeGenBase.biomeList[Math.max(0,Math.min(255,id))];if(!allowed.contains(b))return false;}
        return true;
    }
}
