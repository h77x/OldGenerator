package ca.spottedleaf.oldgenerator.world;

import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

public final class LimitedRegionBlockAccess implements BlockAccess {
    private final LimitedRegion region;
    private final int minHeight;
    private final int maxHeight;

    public LimitedRegionBlockAccess(final WorldInfo worldInfo, final LimitedRegion region) {
        this.region = region;
        this.minHeight = worldInfo.getMinHeight();
        this.maxHeight = worldInfo.getMaxHeight() - 1;
    }

    @Override
    public boolean isLoaded(final int chunkX, final int chunkZ) {
        final int minX = chunkX << 4;
        final int minZ = chunkZ << 4;
        return this.region.isInRegion(minX, this.minHeight, minZ)
                && this.region.isInRegion(minX + 15, this.minHeight, minZ + 15);
    }

    @Override public Material getType(final int x, final int y, final int z) { return this.region.getType(x, y, z); }
    @Override public void setType(final int x, final int y, final int z, final Material material) { if (this.region.isInRegion(x,y,z)) this.region.setType(x,y,z,material); }
    @Override public void setType(final int x, final int y, final int z, final Material material, final boolean applyPhysics) { if (this.region.isInRegion(x,y,z)) this.region.setType(x,y,z,material); }
    @Override public BlockData getBlockData(final int x, final int y, final int z) { return this.region.getBlockData(x,y,z); }
    @Override public void setBlockData(final int x, final int y, final int z, final BlockData data) { if (this.region.isInRegion(x,y,z)) this.region.setBlockData(x,y,z,data); }
    @Override public void setBlockData(final int x, final int y, final int z, final BlockData data, final boolean applyPhysics) { if (this.region.isInRegion(x,y,z)) this.region.setBlockData(x,y,z,data); }
    @Override public BlockState getBlockState(final int x, final int y, final int z) { return this.region.getBlockState(x,y,z); }
    @Override public byte getLightFromSky(final int x, final int y, final int z) { return 0; }
    @Override public byte getLightFromBlocks(final int x, final int y, final int z) { return 0; }
    @Override public byte getLightLevel(final int x, final int y, final int z) { return 0; }
    @Override public int getHighestBlockYAt(final int x, final int z) { return this.region.getHighestBlockYAt(x,z); }
    @Override public int getHighestBlockYAt(final int x, final int z, final HeightMap map) { return this.region.getHighestBlockYAt(x,z,map); }
    @Override public int getMinHeight() { return this.minHeight; }
    @Override public int getMaxHeight() { return this.maxHeight; }
}
