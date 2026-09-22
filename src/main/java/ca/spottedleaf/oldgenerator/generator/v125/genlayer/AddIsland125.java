package ca.spottedleaf.oldgenerator.generator.v125.genlayer;

public final class AddIsland125 extends GenLayer125 {
    public AddIsland125(final long seed, final GenLayer125 parent){super(seed);this.parent=parent;}
    @Override public int[] getInts(final int x,final int z,final int width,final int height){
        final int px=x-1,pz=z-1,pw=width+2,ph=height+2;
        final int[] p=this.parent.getInts(px,pz,pw,ph),o=new int[width*height];
        for(int iz=0;iz<height;++iz) for(int ix=0;ix<width;++ix){
            final int tl=p[ix+(iz)*pw], tr=p[ix+2+(iz)*pw], bl=p[ix+(iz+2)*pw], br=p[ix+2+(iz+2)*pw], c=p[ix+1+(iz+1)*pw];
            this.initChunkSeed(ix+x,iz+z);
            if(c==0 && (tl!=0||tr!=0||bl!=0||br!=0)){
                int chance=1, selected=1;
                if(tl!=0 && this.nextInt(chance++)==0) selected=tl;
                if(tr!=0 && this.nextInt(chance++)==0) selected=tr;
                if(bl!=0 && this.nextInt(chance++)==0) selected=bl;
                if(br!=0 && this.nextInt(chance++)==0) selected=br;
                if(this.nextInt(3)==0) o[ix+iz*width]=selected;
                else o[ix+iz*width]=selected==12?10:0;
            } else if(c>0 && (tl==0||tr==0||bl==0||br==0)){
                o[ix+iz*width]=this.nextInt(5)==0 ? (c==12?10:0) : c;
            } else o[ix+iz*width]=c;
        }
        return o;
    }
}
