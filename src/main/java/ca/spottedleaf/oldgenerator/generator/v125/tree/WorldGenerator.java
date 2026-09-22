package ca.spottedleaf.oldgenerator.generator.v125.tree;

import java.util.Random;
import ca.spottedleaf.oldgenerator.generator.v125.structure.legacy.World;

public abstract class WorldGenerator {
    private final boolean doBlockNotify;
    protected WorldGenerator(){this(false);}
    protected WorldGenerator(boolean doBlockNotify){this.doBlockNotify=doBlockNotify;}
    public abstract boolean generate(World world, Random random, int x, int y, int z);
    public void setScale(double x,double y,double z){}
    protected void setBlock(World world,int x,int y,int z,int id){setBlockAndMetadata(world,x,y,z,id,0);}
    protected void setBlockAndMetadata(World world,int x,int y,int z,int id,int meta){
        if(doBlockNotify) world.setBlockAndMetadataWithNotify(x,y,z,id,meta);
        else world.setBlockAndMetadata(x,y,z,id,meta);
    }
}
