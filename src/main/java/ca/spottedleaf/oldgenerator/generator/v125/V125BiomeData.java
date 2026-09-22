package ca.spottedleaf.oldgenerator.generator.v125;

public final class V125BiomeData {
    public final float baseHeight;
    public final float heightVariation;
    private V125BiomeData(final float baseHeight, final float heightVariation) {
        this.baseHeight=baseHeight;
        this.heightVariation=heightVariation;
    }
    public static V125BiomeData of(final int id) {
        switch(id) {
            case 0: return new V125BiomeData(-1.0F,0.4F);
            case 1: return new V125BiomeData(0.1F,0.3F);
            case 2: return new V125BiomeData(0.1F,0.2F);
            case 3: return new V125BiomeData(0.2F,1.3F);
            case 4: return new V125BiomeData(0.1F,0.3F);
            case 5: return new V125BiomeData(0.1F,0.4F);
            case 6: return new V125BiomeData(-0.2F,0.1F);
            case 7: return new V125BiomeData(-0.5F,0.0F);
            case 10:return new V125BiomeData(-1.0F,0.5F);
            case 11:return new V125BiomeData(-0.5F,0.0F);
            case 12:return new V125BiomeData(0.1F,0.3F);
            case 13:return new V125BiomeData(0.2F,1.2F);
            case 14:return new V125BiomeData(0.2F,1.0F);
            case 15:return new V125BiomeData(-1.0F,0.1F);
            case 16:return new V125BiomeData(0.0F,0.1F);
            case 17:return new V125BiomeData(0.2F,0.7F);
            case 18:return new V125BiomeData(0.2F,0.6F);
            case 19:return new V125BiomeData(0.2F,0.7F);
            case 20:return new V125BiomeData(0.2F,0.8F);
            case 21:return new V125BiomeData(0.2F,0.4F);
            case 22:return new V125BiomeData(1.8F,0.2F);
            default:return new V125BiomeData(0.1F,0.3F);
        }
    }
}
