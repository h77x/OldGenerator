package ca.spottedleaf.oldgenerator.generator.v125.map;

import ca.spottedleaf.oldgenerator.generator.v125.V125BiomeSource;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import java.util.Random;

public final class V125Ravine {
    private final long seed;
    private final Random rand;
    private final int range=8;
    private final float[] sizeTable=new float[1024];
    public V125Ravine(final long seed){this.seed=seed;this.rand=new Random();}
    public void generate(final int targetX,final int targetZ,final ChunkGenerator.ChunkData data,final V125BiomeSource biomes){
        rand.setSeed(seed);long a=rand.nextLong(),b=rand.nextLong();
        for(int cx=targetX-range;cx<=targetX+range;cx++)for(int cz=targetZ-range;cz<=targetZ+range;cz++){
            rand.setSeed(((long)cx*a)^((long)cz*b)^seed);
            recursiveGenerate(cx,cz,targetX,targetZ,data);
        }
    }
    private void recursiveGenerate(int cx,int cz,int tx,int tz,ChunkGenerator.ChunkData data){
        if(rand.nextInt(50)!=0)return;
        double x=cx*16+rand.nextInt(16), y=rand.nextInt(rand.nextInt(40)+8)+20, z=cz*16+rand.nextInt(16);
        generateRavine(rand.nextLong(),tx,tz,data,x,y,z,(rand.nextFloat()*2.0F+rand.nextFloat())*2.0F,rand.nextFloat()*(float)Math.PI*2.0F,(rand.nextFloat()-0.5F)*2.0F/8.0F,0,0,3.0D);
    }
    private void generateRavine(final long seed,final int tx,final int tz,final ChunkGenerator.ChunkData data,double x,double y,double z,
                                float width,float yaw,float pitch,int start,int end,double scale){
        final Random r=new Random(seed); final double cx=tx*16+8,cz=tz*16+8; float yawAcc=0,pitchAcc=0;
        if(end<=0){int span=range*16-16;end=span-r.nextInt(Math.max(1,span/4));}
        boolean large=false;if(start==-1){start=end/2;large=true;}
        float widthScale=1.0F;
        for(int i=0;i<128;i++){if(i==0||r.nextInt(3)==0){widthScale=1.0F+r.nextFloat()*r.nextFloat();}sizeTable[i]=widthScale*widthScale;}
        for(;start<end;start++){
            double radius=1.5D+Math.sin(start*Math.PI/end)*width;
            double vr=radius*scale;float cp=(float)Math.cos(pitch),sp=(float)Math.sin(pitch);
            x+=Math.cos(yaw)*cp;y+=sp;z+=Math.sin(yaw)*cp;pitch=pitch*0.7F+pitchAcc*0.05F;yaw+=yawAcc*0.05F;
            pitchAcc*=0.8F;yawAcc*=0.5F;pitchAcc+=(r.nextFloat()-r.nextFloat())*r.nextFloat()*2.0F;yawAcc+=(r.nextFloat()-r.nextFloat())*r.nextFloat()*4.0F;
            if(large||r.nextInt(4)!=0){
                double dx=x-cx,dz=z-cz,rem=end-start,limit=width+2+16;
                if(dx*dx+dz*dz-rem*rem>limit*limit)return;
                if(x<cx-16-radius*2||z<cz-16-radius*2||x>cx+16+radius*2||z>cz+16+radius*2)continue;
                int minX=Math.max(0,(int)Math.floor(x-radius)-tx*16-1),maxX=Math.min(16,(int)Math.floor(x+radius)-tx*16+1);
                int minY=Math.max(1,(int)Math.floor(y-vr)-1),maxY=Math.min(120,(int)Math.floor(y+vr)+1);
                int minZ=Math.max(0,(int)Math.floor(z-radius)-tz*16-1),maxZ=Math.min(16,(int)Math.floor(z+radius)-tz*16+1);
                boolean water=false;
                for(int bx=minX;bx<maxX&&!water;bx++)for(int bz=minZ;bz<maxZ&&!water;bz++)for(int by=maxY+1;by>=minY-1;by--){
                    if(by>=0&&by<256){Material m=data.getType(bx,by,bz);if(m==Material.WATER){water=true;break;}if(by!=minY-1&&bx!=minX&&bx!=maxX-1&&bz!=minZ&&bz!=maxZ-1)by=minY;}
                }
                if(water)continue;
                for(int bx=minX;bx<maxX;bx++){double nx=((bx+tx*16)+0.5-x)/radius;
                    for(int bz=minZ;bz<maxZ;bz++){double nz=((bz+tz*16)+0.5-z)/radius;int index=(bx*16+bz)*256+maxY;boolean grass=false;
                        if(nx*nx+nz*nz<1.0D)for(int by=maxY-1;by>=minY;by--){double ny=((by+0.5-y)/vr);
                            if((nx*nx+nz*nz)*sizeTable[Math.max(0,Math.min(1023,by))]+ny*ny/6.0D<1.0D){
                                Material m=data.getType(bx,by,bz);if(m==Material.GRASS_BLOCK)grass=true;
                                if(m==Material.STONE||m==Material.DIRT||m==Material.GRASS_BLOCK)data.setBlock(bx,by,bz,by<10?Material.LAVA:Material.AIR);
                                if(grass&&data.getType(bx,by-1,bz)==Material.DIRT)data.setBlock(bx,by-1,bz,Material.GRASS_BLOCK);
                            }--index;
                        }
                    }
                }
                if(large)break;
            }
        }
    }
}
