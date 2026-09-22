package ca.spottedleaf.oldgenerator.generator.v125.genlayer;

public final class IslandBiome125 extends GenLayer125 {
    private static final int[] DEFAULT={2,4,3,6,1,5,21};
    public IslandBiome125(final long seed,final GenLayer125 parent){super(seed);this.parent=parent;}
    @Override public int[] getInts(final int x,final int z,final int width,final int height){
        final int[] p=this.parent.getInts(x,z,width,height),o=new int[width*height];
        for(int iz=0;iz<height;++iz) for(int ix=0;ix<width;++ix){
            this.initChunkSeed(ix+x,iz+z); int v=p[ix+iz*width];
            if(v==0||v==14) o[ix+iz*width]=v; else if(v==1)o[ix+iz*width]=DEFAULT[this.nextInt(DEFAULT.length)]; else o[ix+iz*width]=12;
        } return o;
    }
}
