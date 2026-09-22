package ca.spottedleaf.oldgenerator.generator.v125.genlayer;

public final class AddSnow125 extends GenLayer125 {
    public AddSnow125(final long seed, final GenLayer125 parent){super(seed);this.parent=parent;}
    @Override public int[] getInts(final int x,final int z,final int width,final int height){
        final int px=x-1,pz=z-1,pw=width+2,ph=height+2; final int[] p=this.parent.getInts(px,pz,pw,ph),o=new int[width*height];
        for(int iz=0;iz<height;++iz) for(int ix=0;ix<width;++ix){
            final int v=p[ix+1+(iz+1)*pw]; this.initChunkSeed(ix+x,iz+z);
            if(v==0) o[ix+iz*width]=0; else o[ix+iz*width]=this.nextInt(5)==0?12:1;
        } return o;
    }
}
