package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;
import java.util.List; import java.util.Random;
public final class WeightedRandom {
    public static <T extends WeightedRandomChoice> T getRandomItem(Random r,T[] items){ int total=0; for(T x:items) total+=x.itemWeight; int n=r.nextInt(total); for(T x:items){n-=x.itemWeight;if(n<0)return x;} return items[items.length-1];}
    public static <T extends WeightedRandomChoice> T getRandomItem(Random r,List<T> items){int total=0;for(T x:items)total+=x.itemWeight;int n=r.nextInt(total);for(T x:items){n-=x.itemWeight;if(n<0)return x;}return items.get(items.size()-1);}
}
