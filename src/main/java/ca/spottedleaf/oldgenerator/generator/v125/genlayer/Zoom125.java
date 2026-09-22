package ca.spottedleaf.oldgenerator.generator.v125.genlayer;

public final class Zoom125 extends GenLayer125 {
    public Zoom125(final long seed, final GenLayer125 parent){super(seed);this.parent=parent;}
    @Override public int[] getInts(final int x,final int z,final int width,final int height){
        final int px=x>>1,pz=z>>1,pw=(width>>1)+3,ph=(height>>1)+3;
        final int[] p=this.parent.getInts(px,pz,pw,ph),e=new int[pw*2*ph*2],stride=pw<<1;
        for(int iz=0;iz<ph-1;++iz){int out=(iz<<1)*stride;int a=p[iz*pw],c=p[(iz+1)*pw];
            for(int ix=0;ix<pw-1;++ix){this.initChunkSeed(((long)(ix+px))<<1,((long)(iz+pz))<<1);
                int b=p[ix+1+iz*pw],d=p[ix+1+(iz+1)*pw];e[out]=a;e[out+stride]=this.choose(a,c);e[++out]=this.choose(a,b);e[out+stride]=this.choose4(a,b,c,d);++out;a=b;c=d;}}
        final int[]r=new int[width*height];int sx=x&1,sz=z&1,est=pw<<1;for(int iz=0;iz<height;++iz)System.arraycopy(e,(iz+sz)*est+sx,r,iz*width,width);return r;
    }
}
