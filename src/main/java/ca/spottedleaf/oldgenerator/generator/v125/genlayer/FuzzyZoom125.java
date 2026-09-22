package ca.spottedleaf.oldgenerator.generator.v125.genlayer;

public final class FuzzyZoom125 extends GenLayer125 {
    public FuzzyZoom125(final long seed, final GenLayer125 parent) { super(seed); this.parent = parent; }
    @Override public int[] getInts(final int x, final int z, final int width, final int height) {
        final int px=x>>1, pz=z>>1, pw=(width>>1)+3, ph=(height>>1)+3;
        final int[] parentValues=this.parent.getInts(px,pz,pw,ph);
        final int[] expanded=new int[pw*2*ph*2];
        final int stride=pw<<1;
        for(int iz=0; iz<ph-1; ++iz){
            int out=(iz<<1)*stride;
            int a=parentValues[iz*pw], c=parentValues[(iz+1)*pw];
            for(int ix=0; ix<pw-1; ++ix){
                this.initChunkSeed(((long)(ix+px))<<1, ((long)(iz+pz))<<1);
                int b=parentValues[ix+1+iz*pw], d=parentValues[ix+1+(iz+1)*pw];
                expanded[out]=a;
                expanded[out+stride]=this.choose(a,c);
                expanded[++out]=this.choose(a,b);
                expanded[out+stride]=this.choose4(a,b,c,d);
                ++out;
                a=b; c=d;
            }
        }
        final int[] result=new int[width*height];
        final int sx=x&1, sz=z&1, est= pw<<1;
        for(int iz=0;iz<height;++iz) System.arraycopy(expanded,(iz+sz)*est+sx,result,iz*width,width);
        return result;
    }
}
