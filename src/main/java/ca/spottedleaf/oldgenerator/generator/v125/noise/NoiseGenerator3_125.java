package ca.spottedleaf.oldgenerator.generator.v125.noise;

import java.util.Random;

final class NoiseGenerator3Handler125 {
    private static final int[][] GRADIENTS = {
        {1,1,0},{-1,1,0},{1,-1,0},{-1,-1,0},
        {1,0,1},{-1,0,1},{1,0,-1},{-1,0,-1},
        {0,1,1},{0,-1,1},{0,1,-1},{0,-1,-1}
    };
    private static final double SQRT_3 = Math.sqrt(3.0D);
    private static final double F2 = 0.5D * (SQRT_3 - 1.0D);
    private static final double G2 = (3.0D - SQRT_3) / 6.0D;
    private final int[] permutation = new int[512];
    final double xOffset;
    final double yOffset;

    NoiseGenerator3Handler125(final Random random) {
        this.xOffset = random.nextDouble() * 256.0D;
        this.yOffset = random.nextDouble() * 256.0D;
        for (int i=0;i<256;i++) permutation[i]=i;
        for (int i=0;i<256;i++) {
            int j=random.nextInt(256-i)+i;
            int k=permutation[i];
            permutation[i]=permutation[j];
            permutation[j]=k;
            permutation[i+256]=permutation[i];
        }
    }

    private static int floor(final double value) {
        final int i=(int)value;
        return value>i?i:i-1;
    }

    private static double dot(final int[] g, final double x, final double y) {
        return g[0]*x+g[1]*y;
    }

    void addNoise(final double[] out, final double x, final double z,
                  final int xSize, final int zSize,
                  final double scaleX, final double scaleZ,
                  final double amplitude) {
        int index=0;
        for(int iz=0;iz<zSize;iz++){
            final double dz=(z+iz)*scaleZ+this.yOffset;
            final double skew=(x*0.0D+dz)*0.0D; // keeps calculation order isolated below
            for(int ix=0;ix<xSize;ix++){
                final double dx=(x+ix)*scaleX+this.xOffset;
                final double s=(dx+dz)*F2;
                final int i=floor(dx+s), j=floor(dz+s);
                final double t=(i+j)*G2;
                final double x0=dx-(i-t), y0=dz-(j-t);
                final int i1=x0>y0?1:0, j1=x0>y0?0:1;
                final double x1=x0-i1+G2, y1=y0-j1+G2;
                final double x2=x0-1.0D+2.0D*G2, y2=y0-1.0D+2.0D*G2;
                final int ii=i&255, jj=j&255;
                final int g0=permutation[ii+permutation[jj]]%12;
                final int g1=permutation[ii+i1+permutation[jj+j1]]%12;
                final int g2=permutation[ii+1+permutation[jj+1]]%12;
                double n0=0,n1=0,n2=0;
                double q=0.5D-x0*x0-y0*y0;
                if(q>=0){q*=q;n0=q*q*dot(GRADIENTS[g0],x0,y0);}
                q=0.5D-x1*x1-y1*y1;
                if(q>=0){q*=q;n1=q*q*dot(GRADIENTS[g1],x1,y1);}
                q=0.5D-x2*x2-y2*y2;
                if(q>=0){q*=q;n2=q*q*dot(GRADIENTS[g2],x2,y2);}
                out[index++]+=70.0D*(n0+n1+n2)*amplitude;
            }
        }
    }
}
public final class NoiseGenerator3_125 {
    private final NoiseGenerator3Handler125[] octaves;
    public NoiseGenerator3_125(final Random random, final int count) {
        this.octaves=new NoiseGenerator3Handler125[count];
        for(int i=0;i<count;i++) this.octaves[i]=new NoiseGenerator3Handler125(random);
    }
    public double[] generateNoise(double[] output,double x,double z,int xSize,int zSize,double scaleX,double scaleZ,double persistence){
        return generateNoise(output,x,z,xSize,zSize,scaleX,scaleZ,persistence,0.5D);
    }
    public double[] generateNoise(double[] output,double x,double z,int xSize,int zSize,double scaleX,double scaleZ,double persistence,double octaveScale){
        final int size=xSize*zSize;
        final double[] out=output!=null&&output.length>=size?output:new double[size];
        java.util.Arrays.fill(out,0.0D);
        double d6=1.0D,d7=1.0D;
        for(NoiseGenerator3Handler125 octave:octaves){
            octave.addNoise(out,x,z,xSize,zSize,scaleX*d7*d6,scaleZ*d7*d6,0.55D/d6);
            d7*=persistence; d6*=octaveScale;
        }
        return out;
    }
}
