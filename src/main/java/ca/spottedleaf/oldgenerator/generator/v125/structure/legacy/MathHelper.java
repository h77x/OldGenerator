package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

import java.util.Random;

public final class MathHelper {
    private static final float[] SIN_TABLE = new float[65536];
    public static float sin(final float v) { return SIN_TABLE[(int)(v * 10430.378F) & 65535]; }
    public static float cos(final float v) { return SIN_TABLE[(int)(v * 10430.378F + 16384.0F) & 65535]; }
    public static float sqrt_float(final float v) { return (float)Math.sqrt(v); }
    public static float sqrt_double(final double v) { return (float)Math.sqrt(v); }
    public static int floor_float(final float v) { int i=(int)v; return v<i?i-1:i; }
    public static int floor_double(final double v) { int i=(int)v; return v<i?i-1:i; }
    public static long floor_double_long(final double v) { long i=(long)v; return v<i?i-1:i; }
    public static int func_40346_b(final double v) { return (int)(v+1024.0D)-1024; }
    public static float abs(final float v) { return v >= 0 ? v : -v; }
    public static int clamp_int(final int v, final int a, final int b) { return v<a?a:v>b?b:v; }
    public static float clamp_float(final float v, final float a, final float b) { return v<a?a:v>b?b:v; }
    public static double abs_max(double a,double b){a=Math.abs(a);b=Math.abs(b);return Math.max(a,b);}
    public static int bucketInt(int a,int b){return a<0?-((-a-1)/b)-1:a/b;}
    public static boolean stringNullOrLengthZero(String s){return s==null||s.isEmpty();}
    public static int getRandomIntegerInRange(Random r,int a,int b){return a>=b?a:r.nextInt(b-a+1)+a;}
    static { for(int i=0;i<65536;i++) SIN_TABLE[i]=(float)Math.sin((double)i*Math.PI*2.0D/65536.0D); }
}
