package ca.spottedleaf.oldgenerator.generator.v125;

import ca.spottedleaf.oldgenerator.generator.v125.genlayer.*;
import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Map;

public final class V125BiomeSource {
    private final ThreadLocal<State> states = new ThreadLocal<>();

    public int[] getBiomeIds(final long seed, final int x, final int z, final int width, final int height) {
        State state = this.states.get();
        if (state == null || state.seed != seed) {
            state = new State(seed);
            this.states.set(state);
        }
        return state.biomeLayer.getInts(x, z, width, height);
    }

    public int[] getBlockBiomeIds(final long seed, final int x, final int z, final int width, final int height) {
        State state = this.states.get();
        if (state == null || state.seed != seed) {
            state = new State(seed);
            this.states.set(state);
        }
        return state.blockBiomeLayer.getInts(x, z, width, height);
    }

    public V125BiomeData getBiomeData(final int id) {
        return V125BiomeData.of(id);
    }

    public Biome toBukkit(final int id) {
        return BukkitBiomeMapper.map(id);
    }

    private static GenLayer125 zoom(final long seed, GenLayer125 layer, final int times) {
        for (int i=0;i<times;i++) layer = new Zoom125(seed+i, layer);
        return layer;
    }

    private static final class State {
        final long seed;
        final GenLayer125 biomeLayer;
        final GenLayer125 blockBiomeLayer;
        State(final long seed) {
            this.seed=seed;

            GenLayer125 island=new Island125(1L);
            island=new FuzzyZoom125(2000L,island);
            island=new AddIsland125(1L,island);
            island=zoom(2001L,island,1);
            island=new AddIsland125(2L,island);
            island=new AddSnow125(2L,island);
            island=zoom(2002L,island,1);
            island=new AddIsland125(3L,island);
            island=zoom(2003L,island,1);
            island=new AddIsland125(4L,island);
            island=new AddMushroomIsland125(5L,island);

            GenLayer125 river=zoom(1000L,island,0);
            river=new RiverInit125(100L,river);
            river=zoom(1000L,river,6);
            river=new River125(1L,river);
            river=new Smooth125(1000L,river);

            GenLayer125 biome=zoom(1000L,island,0);
            biome=new IslandBiome125(200L,biome);
            biome=zoom(1000L,biome,2);
            biome=new Hills125(1000L,biome);
            for(int i=0;i<4;i++){
                biome=new Zoom125(1000L+i,biome);
                if(i==0) biome=new AddIsland125(3L,biome);
                if(i==1){
                    biome=new Shore125(1000L,biome);
                    biome=new SwampRivers125(1000L,biome);
                }
            }
            biome=new Smooth125(1000L,biome);
            this.biomeLayer=new RiverMix125(100L,biome,river);
            this.blockBiomeLayer=new VoronoiZoom125(10L,this.biomeLayer);
            this.blockBiomeLayer.initWorldGenSeed(seed);
        }
    }

    private static final class BukkitBiomeMapper {
        private static final Map<Integer,String[]> names=new HashMap<>();
        static{
            names.put(0,new String[]{"OCEAN"});
            names.put(1,new String[]{"PLAINS"});
            names.put(2,new String[]{"DESERT"});
            names.put(3,new String[]{"WINDSWEPT_HILLS","MOUNTAINS"});
            names.put(4,new String[]{"FOREST"});
            names.put(5,new String[]{"TAIGA"});
            names.put(6,new String[]{"SWAMP"});
            names.put(7,new String[]{"RIVER"});
            names.put(10,new String[]{"FROZEN_OCEAN"});
            names.put(11,new String[]{"FROZEN_RIVER"});
            names.put(12,new String[]{"SNOWY_PLAINS","ICE_SPIKES"});
            names.put(13,new String[]{"SNOWY_MOUNTAINS","SNOWY_SLOPES"});
            names.put(14,new String[]{"MUSHROOM_FIELDS"});
            names.put(15,new String[]{"MUSHROOM_FIELDS"});
            names.put(16,new String[]{"BEACH"});
            names.put(17,new String[]{"DESERT"});
            names.put(18,new String[]{"FOREST"});
            names.put(19,new String[]{"TAIGA"});
            names.put(20,new String[]{"WINDSWEPT_HILLS","MOUNTAINS"});
            names.put(21,new String[]{"JUNGLE"});
            names.put(22,new String[]{"JUNGLE"});
        }
        static Biome map(final int id){
            final String[] candidates=names.get(id);
            if(candidates!=null) for(String name:candidates) try{return Biome.valueOf(name);}catch(IllegalArgumentException ignored){}
            return Biome.PLAINS;
        }
    }
}
