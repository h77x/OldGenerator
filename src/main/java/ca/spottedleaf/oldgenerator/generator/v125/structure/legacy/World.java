package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

import ca.spottedleaf.oldgenerator.generator.v125.V125BiomeSource;
import ca.spottedleaf.oldgenerator.world.BlockAccess;
import org.bukkit.Material;

public final class World {
    public final WorldProvider worldProvider=new WorldProvider();
    public boolean editingBlocks;
    private final BlockAccess access;
    private final long seed;
    private final WorldChunkManager manager;

    public World(final long seed, final BlockAccess access, final V125BiomeSource source){
        this.seed=seed;this.access=access;this.manager=new WorldChunkManager(source,seed);
    }
    public World(final BlockAccess access){ this.seed=0L; this.access=access; this.manager=null; }
    public long getSeed(){return seed;}
    public WorldChunkManager getWorldChunkManager(){return manager;}
    public int getBlockId(int x,int y,int z){
        final Material m=access.getType(x,y,z);
        if (m == org.bukkit.Material.WATER) return Block.waterStill.blockID;
        if (m == org.bukkit.Material.LAVA) return Block.lavaStill.blockID;
        for (Block b : Block.blocksList) if (b != null && b.material == m) return b.blockID;
        return m == org.bukkit.Material.AIR ? 0 : 1;
    }
    public Material getBlockMaterial(int x,int y,int z){
        int id=getBlockId(x,y,z); Block b=id>=0&&id<Block.blocksList.length?Block.blocksList[id]:null;
        return b==null?new Material(false,false):b.blockMaterial;
    }
    public boolean isAirBlock(int x,int y,int z){return access.getType(x,y,z).isAir();}
    public void setBlockAndMetadata(int x,int y,int z,int id,int meta){setInternal(x,y,z,id,meta);}
    public void setBlockAndMetadataWithNotify(int x,int y,int z,int id,int meta){setInternal(x,y,z,id,meta);}
    public void setBlockWithNotify(int x,int y,int z,int id){setInternal(x,y,z,id,0);}
    public void setBlock(int x,int y,int z,int id){setInternal(x,y,z,id,0);}
    public void setBlockTileEntity(int x,int y,int z,Object tile){}
    public Object getBlockTileEntity(int x,int y,int z){
        int id=getBlockId(x,y,z);
        if(id==Block.chest.blockID)return new TileEntityChest();
        if(id==Block.mobSpawner.blockID)return new TileEntityMobSpawner();
        return null;
    }
    private void setInternal(int x,int y,int z,int id,int meta){
        if(y<access.getMinHeight()||y>access.getMaxHeight())return;
        Block b=(id>=0&&id<Block.blocksList.length)?Block.blocksList[id]:null;
        Material m=b==null?Material.AIR:b.material;
        access.setType(x,y,z,m,false);
    }
    public int getTopSolidOrLiquidBlock(int x,int z){return access.getHighestBlockYAt(x,z);}
    public int getHeightValue(int x,int z){return access.getHighestBlockYAt(x,z);}
    public void spawnEntityInWorld(EntityVillager v){}
    public boolean isBlockNormalCube(int x,int y,int z){return Block.opaqueCubeLookup[getBlockId(x,y,z)];}
    public int getSavedLightValue(EnumSkyBlock skyBlock, int x, int y, int z){ return skyBlock == EnumSkyBlock.Sky ? 15 : 0; }
    public BiomeGenBase getBiomeGenForCoords(int x, int z){
        if (manager == null) return BiomeGenBase.plains;
        return manager.getBiomeGenAt(x, z);
    }
    public boolean isBlockFreezable(int x, int y, int z){
        final Material m = getBlockMaterial(x,y,z);
        return m == Material.water;
    }
    public void notifyBlocksOfNeighborChange(int x,int y,int z,int id){}
    public static final class WorldProvider { public int getAverageGroundLevel(){return 64;} }
}
