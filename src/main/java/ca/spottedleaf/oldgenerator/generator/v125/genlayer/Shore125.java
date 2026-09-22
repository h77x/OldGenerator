package ca.spottedleaf.oldgenerator.generator.v125.genlayer;

public final class Shore125 extends GenLayer125 {
    public Shore125(final long seed,final GenLayer125 parent){super(seed);this.parent=parent;}
    private boolean ocean(int v){return v==0;}
    @Override public int[] getInts(final int x,final int z,final int width,final int height){
        final int pw=width+2; final int[] p=this.parent.getInts(x-1,z-1,pw,height+2),o=new int[width*height];
        for(int iz=0;iz<height;++iz) for(int ix=0;ix<width;++ix){
            this.initChunkSeed(ix+x,iz+z);
            int v=p[ix+1+(iz+1)*pw], n=p[ix+1+iz*pw],e=p[ix+2+(iz+1)*pw],w=p[ix+(iz+1)*pw],s=p[ix+1+(iz+2)*pw];
            if(v==14)o[ix+iz*width]=(!ocean(n)&&!ocean(e)&&!ocean(w)&&!ocean(s))?14:15;
            else if(v!=0&&v!=7&&v!=6&&v!=3)o[ix+iz*width]=(!ocean(n)&&!ocean(e)&&!ocean(w)&&!ocean(s))?v:16;
            else if(v==3)o[ix+iz*width]=(n==3&&e==3&&w==3&&s==3)?3:20;
            else o[ix+iz*width]=v;
        } return o;
    }
}
