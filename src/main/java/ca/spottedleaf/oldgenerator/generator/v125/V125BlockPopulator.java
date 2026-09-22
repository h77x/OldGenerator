package ca.spottedleaf.oldgenerator.generator.v125;

import ca.spottedleaf.oldgenerator.world.LimitedRegionBlockAccess;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public final class V125BlockPopulator extends BlockPopulator {
    private final V125ChunkGenerator generator;

    public V125BlockPopulator(final V125ChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void populate(final WorldInfo worldInfo, final Random random,
                         final int chunkX, final int chunkZ, final LimitedRegion limitedRegion) {
        this.generator.runPopulators(
                worldInfo.getSeed(),
                chunkX,
                chunkZ,
                new LimitedRegionBlockAccess(worldInfo, limitedRegion)
        );
    }
}
