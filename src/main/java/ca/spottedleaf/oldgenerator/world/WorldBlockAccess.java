package ca.spottedleaf.oldgenerator.world;

import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;

public final class WorldBlockAccess implements BlockAccess {
    public final World world;
    private final int minHeight;
    private final int maxHeight;

    public WorldBlockAccess(final World world) {
        this(world, 0, 127);
    }

    public WorldBlockAccess(final World world, final int minHeight, final int maxHeight) {
        this.world = world;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    @Override public boolean isLoaded(final int chunkX, final int chunkZ) { return this.world.isChunkLoaded(chunkX, chunkZ); }
    @Override public Biome getBiome(final int x, final int y, final int z) { return this.world.getBiome(x, y, z); }
    @Override public Material getType(final int x, final int y, final int z) { return this.world.getBlockAt(x,y,z).getType(); }
    @Override public void setType(final int x, final int y, final int z, final Material material) { this.world.getBlockAt(x,y,z).setType(material); }
    @Override public void setType(final int x, final int y, final int z, final Material material, final boolean physics) { this.world.getBlockAt(x,y,z).setType(material,physics); }
    @Override public BlockData getBlockData(final int x, final int y, final int z) { return this.world.getBlockAt(x,y,z).getBlockData(); }
    @Override public void setBlockData(final int x, final int y, final int z, final BlockData data) { this.world.getBlockAt(x,y,z).setBlockData(data); }
    @Override public void setBlockData(final int x, final int y, final int z, final BlockData data, final boolean physics) { this.world.getBlockAt(x,y,z).setBlockData(data,physics); }
    @Override public BlockState getBlockState(final int x, final int y, final int z) { return this.world.getBlockAt(x,y,z).getState(); }
    @Override public byte getLightFromSky(final int x, final int y, final int z) { return this.world.getBlockAt(x,y,z).getLightFromSky(); }
    @Override public byte getLightFromBlocks(final int x, final int y, final int z) { return this.world.getBlockAt(x,y,z).getLightFromBlocks(); }
    @Override public byte getLightLevel(final int x, final int y, final int z) { return this.world.getBlockAt(x,y,z).getLightLevel(); }
    @Override public int getHighestBlockYAt(final int x, final int z) { return this.world.getHighestBlockYAt(x,z); }
    @Override public int getHighestBlockYAt(final int x, final int z, final HeightMap map) { return this.world.getHighestBlockYAt(x,z,map); }
    @Override public int getMinHeight() { return this.minHeight; }
    @Override public int getMaxHeight() { return this.maxHeight; }
}
