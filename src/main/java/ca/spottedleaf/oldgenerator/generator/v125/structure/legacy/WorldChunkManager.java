package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

import ca.spottedleaf.oldgenerator.generator.v125.V125BiomeSource;

public final class WorldChunkManager {
    private final V125BiomeSource source;
    private final long seed;
    public WorldChunkManager(V125BiomeSource source,long seed){this.source=source;this.seed=seed;}
    public BiomeGenBase getBiomeGenAt(int x,int z){int id=source.getBlockBiomeIds(seed,x,z,1,1)[0];return BiomeGenBase.biomeList[Math.max(0,Math.min(255,id))];}
}
