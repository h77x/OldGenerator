package ca.spottedleaf.oldgenerator.generator.v125;

import ca.spottedleaf.oldgenerator.generator.b173.populator.*;
import ca.spottedleaf.oldgenerator.generator.v125.noise.NoiseGeneratorOctaves125;
import ca.spottedleaf.oldgenerator.generator.v125.noise.NoiseGenerator3_125;
import ca.spottedleaf.oldgenerator.generator.v125.map.V125Caves;
import ca.spottedleaf.oldgenerator.generator.v125.map.V125Ravine;
import ca.spottedleaf.oldgenerator.util.BlockConstants;
import ca.spottedleaf.oldgenerator.world.BlockAccess;
import ca.spottedleaf.oldgenerator.world.WorldBlockAccess;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

/**
 * Minecraft 1.2.5 Overworld generator.
 *
 * The terrain RNG/noise object construction order and density interpolation
 * follow the 1.2.5 server algorithm. Population is executed separately so
 * the legacy chunk ordering can be reproduced by LegacyPopulateHack.
 */
public final class V125ChunkGenerator extends ChunkGenerator {
    private static final int SEA_LEVEL = 63;
    private static final int WORLD_HEIGHT = 256;

    private final V125BiomeSource biomeSource = new V125BiomeSource();
    private final ThreadLocal<NoiseState> noiseStates = new ThreadLocal<>();

    private static final class NoiseState {
        final long seed;
        final NoiseGeneratorOctaves125 minLimit;
        final NoiseGeneratorOctaves125 maxLimit;
        final NoiseGeneratorOctaves125 main;
        final NoiseGenerator3_125 surfaceStone;
        final NoiseGeneratorOctaves125 depth;
        final NoiseGeneratorOctaves125 scale;
        final NoiseGeneratorOctaves125 treeCount;

        NoiseState(final long seed) {
            this.seed = seed;
            final Random random = new Random(seed);

            // 1.2.5 ChunkProviderGenerate constructor order.
            this.minLimit = new NoiseGeneratorOctaves125(random, 16);
            this.maxLimit = new NoiseGeneratorOctaves125(random, 16);
            this.main = new NoiseGeneratorOctaves125(random, 8);
            this.surfaceStone = new NoiseGenerator3_125(random, 4);
            this.depth = new NoiseGeneratorOctaves125(random, 10);
            this.scale = new NoiseGeneratorOctaves125(random, 16);
            this.treeCount = new NoiseGeneratorOctaves125(random, 8);
        }
    }

    private NoiseState state(final long seed) {
        NoiseState state = this.noiseStates.get();
        if (state == null || state.seed != seed) {
            state = new NoiseState(seed);
            this.noiseStates.set(state);
        }
        return state;
    }

    @Override
    public boolean isParallelCapable() {
        return true;
    }

    @Override
    public boolean shouldGenerateCaves() { return false; }

    @Override
    public boolean shouldGenerateDecorations() { return false; }

    @Override
    public boolean shouldGenerateMobs() { return false; }

    @Override
    public boolean shouldGenerateStructures() { return false; }

    @Override
    public ChunkData generateChunkData(final World world, final Random random,
                                       final int chunkX, final int chunkZ,
                                       final BiomeGrid biomeGrid) {
        if (world.getEnvironment() != World.Environment.NORMAL) {
            throw new IllegalStateException("v125 only supports the 1.2.5 Overworld");
        }

        final ChunkData data = this.createChunkData(world);
        final long seed = world.getSeed();
        final int[] generationBiomes = this.biomeSource.getBiomeIds(seed, chunkX * 16, chunkZ * 16, 16, 16);

        for (int z = 0; z < 16; ++z) {
            for (int x = 0; x < 16; ++x) {
                biomeGrid.setBiome(x, z, this.biomeSource.toBukkit(generationBiomes[x + z * 16]));
            }
        }

        final double[] density = this.generateDensity(seed, chunkX, chunkZ);

        // Vanilla 1.2.5 interpolates a 5x33x5 density lattice into
        // 4x4x8 cells.
        for (int cellX = 0; cellX < 4; ++cellX) {
            for (int cellZ = 0; cellZ < 4; ++cellZ) {
                for (int cellY = 0; cellY < 32; ++cellY) {
                    int i000 = densityIndex(cellX, cellZ, cellY);
                    int i001 = densityIndex(cellX, cellZ, cellY + 1);
                    int i100 = densityIndex(cellX + 1, cellZ, cellY);
                    int i101 = densityIndex(cellX + 1, cellZ, cellY + 1);
                    int i010 = densityIndex(cellX, cellZ + 1, cellY);
                    int i011 = densityIndex(cellX, cellZ + 1, cellY + 1);
                    int i110 = densityIndex(cellX + 1, cellZ + 1, cellY);
                    int i111 = densityIndex(cellX + 1, cellZ + 1, cellY + 1);

                    double d1 = density[i000];
                    double d2 = density[i010];
                    double d3 = density[i100];
                    double d4 = density[i110];
                    final double d5 = (density[i001] - d1) * 0.125D;
                    final double d6 = (density[i011] - d2) * 0.125D;
                    final double d7 = (density[i101] - d3) * 0.125D;
                    final double d8 = (density[i111] - d4) * 0.125D;

                    for (int localY = 0; localY < 8; ++localY) {
                        double d9 = d1;
                        double d10 = d2;
                        double d11 = (d3 - d1) * 0.25D;
                        double d12 = (d4 - d2) * 0.25D;

                        for (int localX = 0; localX < 4; ++localX) {
                            final int y = cellY * 8 + localY;
                            final int x = cellX * 4 + localX;

                            double d13 = d9;
                            double d14 = (d10 - d9) * 0.25D;
                            for (int localZ = 0; localZ < 4; ++localZ) {
                                final int z = cellZ * 4 + localZ;
                                final double densityValue = d13;

                                if (densityValue > 0.0D) {
                                    data.setBlock(x, y, z, BlockConstants.STONE);
                                } else if (y < SEA_LEVEL) {
                                    data.setBlock(x, y, z,
                                            y == SEA_LEVEL - 1 && generationBiomes[x + z * 16] == 12
                                                    ? BlockConstants.ICE : BlockConstants.SOURCE_WATER);
                                }

                                d13 += d14;
                            }

                            d9 += d11;
                            d10 += d12;
                        }
                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }

        this.applySurface(data, generationBiomes);
        new V125Caves(seed).generate(chunkX, chunkZ, data, this.biomeSource);
        new V125Ravine(seed).generate(chunkX, chunkZ, data, this.biomeSource);
        return data;
    }

    private double[] generateDensity(final long seed, final int chunkX, final int chunkZ) {
        final NoiseState state = this.state(seed);
        final double[] out = new double[5 * 5 * 33];

        final int[] biomes = this.biomeSource.getBiomeIds(seed, chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);

        final double[] depthNoise = state.depth.generateNoise(null,
                chunkX * 4 - 2, chunkZ * 4 - 2, 5, 5, 200.0D, 200.0D);
        final double[] mainNoise = state.main.generateNoise(null,
                chunkX * 4, 0, chunkZ * 4, 5, 33, 5,
                8.55515D, 4.277575D, 8.55515D);
        final double[] minNoise = state.minLimit.generateNoise(null,
                chunkX * 4, 0, chunkZ * 4, 5, 33, 5,
                684.412D, 684.412D, 684.412D);
        final double[] maxNoise = state.maxLimit.generateNoise(null,
                chunkX * 4, 0, chunkZ * 4, 5, 33, 5,
                684.412D, 684.412D, 684.412D);

        final float[] weights = new float[25];
        for (int dz = -2; dz <= 2; ++dz) {
            for (int dx = -2; dx <= 2; ++dx) {
                weights[dx + 2 + (dz + 2) * 5] =
                        10.0F / (float)Math.sqrt(dx * dx + dz * dz + 0.2F);
            }
        }

        int terrainIndex = 0;
        int columnIndex = 0;

        for (int gx = 0; gx < 5; ++gx) {
            final int centerX = gx + 2;
            for (int gz = 0; gz < 5; ++gz) {
                final int centerZ = gz + 2;
                final V125BiomeData center = this.biomeSource.getBiomeData(biomes[centerX + centerZ * 10]);

                float heightSum = 0.0F;
                float variationSum = 0.0F;
                float weightSum = 0.0F;

                for (int nx = -2; nx <= 2; ++nx) {
                    for (int nz = -2; nz <= 2; ++nz) {
                        final V125BiomeData biome =
                                this.biomeSource.getBiomeData(biomes[(centerX + nx) + (centerZ + nz) * 10]);
                        float weight = weights[nx + 2 + (nz + 2) * 5] / (biome.heightVariation + 2.0F);
                        if (biome.heightVariation > center.heightVariation) {
                            weight *= 0.5F;
                        }
                        heightSum += biome.baseHeight * weight;
                        variationSum += biome.heightVariation * weight;
                        weightSum += weight;
                    }
                }

                float baseHeight = heightSum / weightSum;
                float variation = variationSum / weightSum;
                baseHeight = baseHeight * 0.9F + 0.1F;
                variation = (variation * 4.0F - 1.0F) / 8.0F;

                final double depth = depthNoise[columnIndex++] / 8000.0D;
                double adjustedDepth = depth;
                if (adjustedDepth < 0.0D) adjustedDepth = -adjustedDepth * 0.3D;
                adjustedDepth = adjustedDepth * 3.0D - 2.0D;
                if (adjustedDepth < 0.0D) {
                    adjustedDepth /= 2.0D;
                    if (adjustedDepth < -1.0D) adjustedDepth = -1.0D;
                    adjustedDepth /= 1.4D;
                    adjustedDepth /= 2.0D;
                    baseHeight = 0.0F;
                } else {
                    if (adjustedDepth > 1.0D) adjustedDepth = 1.0D;
                    adjustedDepth /= 8.0D;
                }

                if (baseHeight < 0.0F) baseHeight = 0.0F;
                baseHeight += 0.5F;

                variation += adjustedDepth * 0.2F;
                variation = variation * 8.5F / 8.0F;
                final double terrainCenter = 8.5D + (double)variation * 4.0D;

                for (int gy = 0; gy < 33; ++gy) {
                    double d9 = ((double)gy - terrainCenter) * 12.0D * 128.0D / 256.0D / baseHeight;
                    if (d9 < 0.0D) d9 *= 4.0D;

                    final double low = minNoise[terrainIndex] / 512.0D;
                    final double high = maxNoise[terrainIndex] / 512.0D;
                    final double selector = (mainNoise[terrainIndex] / 10.0D + 1.0D) / 2.0D;
                    final double blended = selector < 0.0D ? low : selector > 1.0D ? high : low + (high - low) * selector;

                    double density = blended - d9;
                    if (gy > 29) {
                        final double edge = (double)(gy - 29) / 3.0D;
                        density = density * (1.0D - edge) + -10.0D * edge;
                    }
                    out[terrainIndex++] = density;
                }
            }
        }
        return out;
    }

    private void applySurface(final ChunkData data, final int[] biomeIds) {
        final double[] stoneNoise = this.stateForSurface(biomeIds).surfaceNoise;
        for (int z=0; z<16; ++z) {
            for (int x=0; x<16; ++x) {
                final int biomeId = biomeIds[x + z * 16];
                final Material[] surface = surfaceForBiome(biomeId);
                int depth = (int)(stoneNoise[x + z * 16] / 3.0D + 3.0D);
                if (depth < 1) depth = 1;
                int run = -1;
                for (int y=WORLD_HEIGHT-1; y>=0; --y) {
                    if (y <= 4) {
                        data.setBlock(x,y,z,BlockConstants.BEDROCK);
                        continue;
                    }
                    final Material material=data.getType(x,y,z);
                    if (material==Material.AIR || material==Material.WATER) {
                        run=-1;
                        continue;
                    }
                    if (material!=Material.STONE) continue;
                    if (run<0) {
                        run=depth;
                        if (y>=SEA_LEVEL-4 && y<=SEA_LEVEL+1) {
                            data.setBlock(x,y,z,surface[0]);
                            for(int d=1;d<run&&y-d>=0;d++){
                                if(data.getType(x,y-d,z)==Material.STONE) data.setBlock(x,y-d,z,surface[1]);
                            }
                        } else if (y<SEA_LEVEL && surface[0]==Material.AIR) {
                            data.setBlock(x,y,z,BlockConstants.SOURCE_WATER);
                        }
                    } else if(run>0) {
                        --run;
                        data.setBlock(x,y,z,surface[1]);
                    }
                }
            }
        }
    }

    private SurfaceNoiseState stateForSurface(final int[] biomeIds) {
        final long seed=0x9E3779B97F4A7C15L ^ java.util.Arrays.hashCode(biomeIds);
        final NoiseState state=state(lastWorldSeed);
        final double[] noise=state.surfaceStone.generateNoise(null,0,0,16,16,0.0625D,0.0625D,1.0D);
        return new SurfaceNoiseState(noise);
    }

    private long lastWorldSeed;
    private SurfaceNoiseState stateForSurfaceNoSeed(final long seed){
        this.lastWorldSeed=seed;
        return null;
    }
    private SurfaceNoiseState stateForSurface(final long seed){this.lastWorldSeed=seed;return null;}

    private static final class SurfaceNoiseState {
        final double[] surfaceNoise;
        SurfaceNoiseState(final double[] v){this.surfaceNoise=v;}
    }

    private static Material[] surfaceForBiome(final int biomeId) {
        switch (biomeId) {
            case 2: case 17: case 16:
                return new Material[]{Material.SAND, Material.SAND};
            case 14: case 15:
                return new Material[]{Material.MYCELIUM, Material.DIRT};
            case 3: case 20:
                return new Material[]{Material.GRASS_BLOCK, Material.DIRT};
            default:
                return new Material[]{Material.GRASS_BLOCK, Material.DIRT};
        }
    }

    private static int densityIndex(final int x, final int z, final int y) {
        return (x * 5 + z) * 33 + y;
    }

    public void runPopulators(final World world, final Chunk chunk) {
        final int chunkX=chunk.getX();
        final int chunkZ=chunk.getZ();
        final int worldX=chunkX*16;
        final int worldZ=chunkZ*16;
        final Random random=new Random();
        random.setSeed(world.getSeed());
        final long oddX=random.nextLong()/2L*2L+1L;
        final long oddZ=random.nextLong()/2L*2L+1L;
        random.setSeed((long)chunkX*oddX+(long)chunkZ*oddZ ^ world.getSeed());

        final BlockAccess access=new WorldBlockAccess(world,0,255);
        final int[] ids=this.biomeSource.getBiomeIds(world.getSeed(),worldX+8,worldZ+8,1,1);
        final int biomeId=ids[0];

        // 1.2.5 ordering: lakes, dungeons, biome decoration, then freeze/snow.
        if (random.nextInt(4)==0) {
            int x=worldX+random.nextInt(16)+8;
            int y=random.nextInt(256);
            int z=worldZ+random.nextInt(16)+8;
            new WorldGenLakes173(BlockConstants.SOURCE_WATER).populate(access,random,x,y,z);
        }
        if (random.nextInt(8)==0) {
            int x=worldX+random.nextInt(16)+8;
            int y=random.nextInt(random.nextInt(248)+8);
            int z=worldZ+random.nextInt(16)+8;
            if(y<63 || random.nextInt(10)==0) new WorldGenLakes173(BlockConstants.SOURCE_LAVA).populate(access,random,x,y,z);
        }
        for(int i=0;i<8;i++){
            int x=worldX+random.nextInt(16)+8;
            int y=random.nextInt(256);
            int z=worldZ+random.nextInt(16)+8;
            new WorldGenDungeons173().populate(access,random,x,y,z);
        }

        generateOres(access,random,worldX,worldZ);
        decorateBiome(access,random,world,biomeId,worldX,worldZ);
        freezeAndSnow(world,access,random,worldX,worldZ);
    }

    private void generateOres(final BlockAccess world, final Random random, final int baseX, final int baseZ) {
        for(int i=0;i<20;i++) ore(world,random,new WorldGenMinable173(BlockConstants.DIRT,32),baseX,baseZ,0,256);
        for(int i=0;i<10;i++) ore(world,random,new WorldGenMinable173(BlockConstants.GRAVEL,32),baseX,baseZ,0,256);
        for(int i=0;i<20;i++) ore(world,random,new WorldGenMinable173(BlockConstants.COAL_ORE,16),baseX,baseZ,0,128);
        for(int i=0;i<20;i++) ore(world,random,new WorldGenMinable173(BlockConstants.IRON_ORE,8),baseX,baseZ,0,64);
        for(int i=0;i<2;i++) ore(world,random,new WorldGenMinable173(BlockConstants.GOLD_ORE,8),baseX,baseZ,0,32);
        for(int i=0;i<8;i++) ore(world,random,new WorldGenMinable173(BlockConstants.REDSTONE_ORE,7),baseX,baseZ,0,16);
        for(int i=0;i<1;i++) ore(world,random,new WorldGenMinable173(BlockConstants.DIAMOND_ORE,7),baseX,baseZ,0,16);
        for(int i=0;i<1;i++) {
            int x=baseX+random.nextInt(16), z=baseZ+random.nextInt(16);
            int y=random.nextInt(16)+random.nextInt(16);
            new WorldGenMinable173(BlockConstants.LAPIS_ORE,6).populate(world,random,x,y,z);
        }
    }

    private void ore(BlockAccess world,Random random,WorldGenMinable173 generator,int x,int z,int min,int max){
        generator.populate(world,random,x+random.nextInt(16),min+random.nextInt(max-min),z+random.nextInt(16));
    }

    private void decorateBiome(final BlockAccess access,final Random random,final World world,final int id,final int baseX,final int baseZ) {
        int trees=0, flowers=2, grass=1, dead=0, mushrooms=0, reeds=0, cactus=0;
        switch(id){
            case 4: trees=10; break;
            case 18: trees=10; break;
            case 5: case 19: trees=10; grass=1; break;
            case 21: trees=50; grass=25; reeds=10; break;
            case 22: trees=50; grass=25; reeds=10; break;
            case 2: case 17: flowers=0; grass=0; dead=10; cactus=10; break;
            case 6: trees=0; flowers=1; grass=5; mushrooms=1; reeds=10; break;
            case 12: case 13: flowers=0; grass=0; break;
            case 14: case 15: trees=0; flowers=0; grass=0; mushrooms=1; break;
            case 1: trees=0; flowers=2; grass=10; break;
        }

        int treeAttempts=trees+(random.nextInt(10)==0?1:0);
        for(int i=0;i<treeAttempts;i++){
            int x=baseX+random.nextInt(16)+8,z=baseZ+random.nextInt(16)+8;
            new WorldGenTrees173(false).populate(access,random,x,world.getHighestBlockYAt(x,z),z);
        }
        for(int i=0;i<flowers;i++){
            int x=baseX+random.nextInt(16)+8,y=random.nextInt(256),z=baseZ+random.nextInt(16)+8;
            new WorldGenFlowers173(BlockConstants.DANDELION).populate(access,random,x,y,z);
            if(random.nextInt(4)==0)new WorldGenFlowers173(BlockConstants.POPPY).populate(access,random,x,y,z);
        }
        for(int i=0;i<grass;i++){
            int x=baseX+random.nextInt(16)+8,y=random.nextInt(256),z=baseZ+random.nextInt(16)+8;
            new WorldGenGrass173(BlockConstants.SHORT_GRASS).populate(access,random,x,y,z);
        }
        for(int i=0;i<dead;i++){
            int x=baseX+random.nextInt(16)+8,y=random.nextInt(256),z=baseZ+random.nextInt(16)+8;
            new WorldGenDeadBush173(BlockConstants.DEAD_BUSH).populate(access,random,x,y,z);
        }
        for(int i=0;i<mushrooms;i++){
            if(random.nextInt(4)==0)new WorldGenFlowers173(BlockConstants.BROWN_MUSHROOM).populate(access,random,baseX+random.nextInt(16)+8,random.nextInt(256),baseZ+random.nextInt(16)+8);
            if(random.nextInt(8)==0)new WorldGenFlowers173(BlockConstants.RED_MUSHROOM).populate(access,random,baseX+random.nextInt(16)+8,random.nextInt(256),baseZ+random.nextInt(16)+8);
        }
        for(int i=0;i<reeds+10;i++){
            new WorldGenReed173().populate(access,random,baseX+random.nextInt(16)+8,random.nextInt(256),baseZ+random.nextInt(16)+8);
        }
        if(random.nextInt(32)==0)new WorldGenPumpkin173().populate(access,random,baseX+random.nextInt(16)+8,random.nextInt(256),baseZ+random.nextInt(16)+8);
        for(int i=0;i<cactus;i++)new WorldGenCactus173().populate(access,random,baseX+random.nextInt(16)+8,random.nextInt(256),baseZ+random.nextInt(16)+8);
        for(int i=0;i<50;i++)new WorldGenLiquids173(BlockConstants.SOURCE_WATER).populate(access,random,baseX+random.nextInt(16)+8,random.nextInt(random.nextInt(248)+8),baseZ+random.nextInt(16)+8);
        for(int i=0;i<20;i++)new WorldGenLiquids173(BlockConstants.SOURCE_LAVA).populate(access,random,baseX+random.nextInt(16)+8,random.nextInt(Math.max(1,random.nextInt(Math.max(1,random.nextInt(232)+8)+8))),baseZ+random.nextInt(16)+8);
    }

    private void freezeAndSnow(final World world,final BlockAccess access,final Random random,final int baseX,final int baseZ){
        for(int x=baseX+8;x<baseX+24;x++){
            for(int z=baseZ+8;z<baseZ+24;z++){
                final int top=world.getHighestBlockYAt(x,z);
                if(top<=0||top>255)continue;
                final Material below=access.getType(x,top-1,z);
                if(below==Material.WATER){
                    access.setType(x,top-1,z,Material.ICE,false);
                    continue;
                }
                if((below==Material.GRASS_BLOCK||below==Material.DIRT)&&access.getType(x,top,z).isAir()){
                    if(random.nextInt(3)==0)access.setType(x,top,z,Material.SNOW,false);
                }
            }
        }
    }
}
