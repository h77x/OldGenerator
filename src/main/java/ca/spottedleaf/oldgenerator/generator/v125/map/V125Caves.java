package ca.spottedleaf.oldgenerator.generator.v125.map;

import ca.spottedleaf.oldgenerator.generator.v125.V125BiomeSource;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public final class V125Caves {
    private final long worldSeed;
    private final Random rand;
    private final int range=8;

    public V125Caves(final long worldSeed){this.worldSeed=worldSeed;this.rand=new Random();}
    public void generate(final int targetChunkX,final int targetChunkZ,final ChunkGenerator.ChunkData data,final V125BiomeSource biomes){
        this.rand.setSeed(worldSeed);
        final long a=this.rand.nextLong(), b=this.rand.nextLong();
        for(int cx=targetChunkX-range;cx<=targetChunkX+range;cx++)
            for(int cz=targetChunkZ-range;cz<=targetChunkZ+range;cz++){
                this.rand.setSeed(((long)cx*a)^((long)cz*b)^worldSeed);
                recursiveGenerate(cx,cz,targetChunkX,targetChunkZ,data);
            }
    }

    private void recursiveGenerate(final int cx,final int cz,final int targetX,final int targetZ,final ChunkGenerator.ChunkData data){
        int count=this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(40)+1)+1);
        if(this.rand.nextInt(15)!=0) count=0;
        for(int n=0;n<count;n++){
            final double x=cx*16+this.rand.nextInt(16);
            final double y=this.rand.nextInt(this.rand.nextInt(120)+8);
            final double z=cz*16+this.rand.nextInt(16);
            int nodes=1;
            if(this.rand.nextInt(4)==0){generateLarge(this.rand.nextLong(),targetX,targetZ,data,x,y,z);nodes+=this.rand.nextInt(4);}
            for(int i=0;i<nodes;i++){
                final float yaw=this.rand.nextFloat()*(float)Math.PI*2.0F;
                final float pitch=(this.rand.nextFloat()-0.5F)*2.0F/8.0F;
                float width=this.rand.nextFloat()*2.0F+this.rand.nextFloat();
                if(this.rand.nextInt(10)==0) width*=this.rand.nextFloat()*this.rand.nextFloat()*3.0F+1.0F;
                generateNode(this.rand.nextLong(),targetX,targetZ,data,x,y,z,width,yaw,pitch,0,0,1.0D);
            }
        }
    }

    private void generateLarge(long seed,int targetX,int targetZ,ChunkGenerator.ChunkData data,double x,double y,double z){
        generateNode(seed,targetX,targetZ,data,x,y,z,1.0F+this.rand.nextFloat()*6.0F,0.0F,0.0F,-1,-1,0.5D);
    }

    private void generateNode(final long seed,final int targetX,final int targetZ,final ChunkGenerator.ChunkData data,
                              double x,double y,double z,float width,float yaw,float pitch,int start,int end,double scale){
        final double centerX=targetX*16+8, centerZ=targetZ*16+8;
        float yawAcc=0.0F,pitchAcc=0.0F;
        final Random random=new Random(seed);
        if(end<=0){
            int span=range*16-16;
            end=span-random.nextInt(Math.max(1,span/4));
        }
        boolean largeStart=false;
        if(start==-1){start=end/2;largeStart=true;}
        final int split=random.nextInt(end/2)+end/4;

        for(boolean branch=random.nextInt(6)==0;start<end;start++){
            final double radius=1.5D+Math.sin((float)start*Math.PI/(float)end)*width;
            final double verticalRadius=radius*scale;
            final float cosPitch=(float)Math.cos(pitch), sinPitch=(float)Math.sin(pitch);
            x+=Math.cos(yaw)*cosPitch;
            y+=sinPitch;
            z+=Math.sin(yaw)*cosPitch;
            pitch=branch?pitch*0.92F:pitch*0.7F;
            pitch+=pitchAcc*0.1F;
            yaw+=yawAcc*0.1F;
            pitchAcc*=0.9F; yawAcc*=0.75F;
            pitchAcc+=(random.nextFloat()-random.nextFloat())*random.nextFloat()*2.0F;
            yawAcc+=(random.nextFloat()-random.nextFloat())*random.nextFloat()*4.0F;

            if(!largeStart&&start==split&&width>1.0F&&end>0){
                generateNode(random.nextLong(),targetX,targetZ,data,x,y,z,random.nextFloat()*0.5F+0.5F,yaw-(float)Math.PI/2F,pitch/3.0F,start,end,1.0D);
                generateNode(random.nextLong(),targetX,targetZ,data,x,y,z,random.nextFloat()*0.5F+0.5F,yaw+(float)Math.PI/2F,pitch/3.0F,start,end,1.0D);
                return;
            }
            if(largeStart||random.nextInt(4)!=0){
                final double dx=x-centerX,dz=z-centerZ,remaining=end-start;
                final double limit=width+2.0F+16.0D;
                if(dx*dx+dz*dz-remaining*remaining>limit*limit)return;
                if(x< centerX-16-radius*2||z<centerZ-16-radius*2||x>centerX+16+radius*2||z>centerZ+16+radius*2)continue;

                int minX=(int)Math.floor(x-radius)-targetX*16-1;
                int maxX=(int)Math.floor(x+radius)-targetX*16+1;
                int minY=(int)Math.floor(y-verticalRadius)-1;
                int maxY=(int)Math.floor(y+verticalRadius)+1;
                int minZ=(int)Math.floor(z-radius)-targetZ*16-1;
                int maxZ=(int)Math.floor(z+radius)-targetZ*16+1;
                minX=Math.max(0,minX); maxX=Math.min(16,maxX); minY=Math.max(1,minY); maxY=Math.min(120,maxY); minZ=Math.max(0,minZ); maxZ=Math.min(16,maxZ);

                boolean hitWater=false;
                for(int bx=minX;bx<maxX&&!hitWater;bx++)for(int bz=minZ;bz<maxZ&&!hitWater;bz++)for(int by=maxY+1;by>=minY-1;by--){
                    if(by>=0&&by<256){
                        Material m=data.getType(bx,by,bz);
                        if(m==Material.WATER){hitWater=true;break;}
                        if(by!=minY-1&&bx!=minX&&bx!=maxX-1&&bz!=minZ&&bz!=maxZ-1)by=minY;
                    }
                }
                if(hitWater)continue;

                for(int bx=minX;bx<maxX;bx++){
                    final double normX=((bx+targetX*16)+0.5D-x)/radius;
                    for(int bz=minZ;bz<maxZ;bz++){
                        final double normZ=((bz+targetZ*16)+0.5D-z)/radius;
                        int index=(bx*16+bz)*256+maxY;
                        boolean grass=false;
                        if(normX*normX+normZ*normZ<1.0D){
                            for(int by=maxY-1;by>=minY;by--){
                                final double normY=(by+0.5D-y)/verticalRadius;
                                if(normY>-0.7D&&normX*normX+normY*normY+normZ*normZ<1.0D){
                                    final Material m=data.getType(bx,by,bz);
                                    if(m==Material.GRASS_BLOCK)grass=true;
                                    if(m==Material.STONE||m==Material.DIRT||m==Material.GRASS_BLOCK){
                                        data.setBlock(bx,by,bz,by<10?Material.LAVA:Material.AIR);
                                        if(grass&&data.getType(bx,by-1,bz)==Material.DIRT)data.setBlock(bx,by-1,bz,Material.GRASS_BLOCK);
                                    }
                                }
                                --index;
                            }
                        }
                    }
                }
                if(largeStart)return;
            }
        }
    }
}
