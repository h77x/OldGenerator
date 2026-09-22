package ca.spottedleaf.oldgenerator.generator.v125.genlayer;

public final class Hills125 extends GenLayer125 {
    public Hills125(final long seed,final GenLayer125 parent){super(seed);this.parent=parent;}
    @Override public int[] getInts(final int x,final int z,final int width,final int height){
        final int pw=width+2; final int[] p=this.parent.getInts(x-1,z-1,pw,height+2),o=new int[width*height];
        for(int iz=0;iz<height;++iz) for(int ix=0;ix<width;++ix){
            this.initChunkSeed(ix+x,iz+z); int v=p[ix+1+(iz+1)*pw], h=v;
            if(this.nextInt(3)==0){
                switch(v){case 2:h=17;break;case 4:h=18;break;case 5:h=19;break;case 1:h=4;break;case 12:h=13;break;case 21:h=22;break;default:break;}
                if(h!=v){
                    int n=p[ix+1+(iz)*pw],e=p[ix+2+(iz+1)*pw],w=p[ix+(iz+1)*pw],s=p[ix+1+(iz+2)*pw];
                    if(n==v&&e==v&&w==v&&s==v)v=h;
                }
            }
            o[ix+iz*width]=v;
        } return o;
    }
}
