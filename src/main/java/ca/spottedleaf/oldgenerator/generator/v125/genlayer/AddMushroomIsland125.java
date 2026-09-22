package ca.spottedleaf.oldgenerator.generator.v125.genlayer;

public final class AddMushroomIsland125 extends GenLayer125 {
    public AddMushroomIsland125(final long seed, final GenLayer125 parent){super(seed);this.parent=parent;}
    @Override public int[] getInts(final int x,final int z,final int width,final int height){
        final int px=x-1,pz=z-1,pw=width+2; final int[] p=this.parent.getInts(px,pz,width+2,height+2),o=new int[width*height];
        for(int iz=0;iz<height;++iz) for(int ix=0;ix<width;++ix){
            final int tl=p[ix+(iz)*pw],tr=p[ix+2+(iz)*pw],bl=p[ix+(iz+2)*pw],br=p[ix+2+(iz+2)*pw],c=p[ix+1+(iz+1)*pw];
            this.initChunkSeed(ix+x,iz+z);
            o[ix+iz*width]=(c==0&&tl==0&&tr==0&&bl==0&&br==0&&this.nextInt(100)==0)?14:c;
        } return o;
    }
}
