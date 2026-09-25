package ca.spottedleaf.oldgenerator.generator.v125;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.List;

public final class V125BiomeProvider extends BiomeProvider {
    private final V125BiomeSource biomeSource;

    public V125BiomeProvider(final V125BiomeSource biomeSource) {
        this.biomeSource = biomeSource;
    }

    @Override
    public Biome getBiome(final WorldInfo worldInfo, final int x, final int y, final int z) {
        final int id = this.biomeSource.getBlockBiomeIds(worldInfo.getSeed(), x, z, 1, 1)[0];
        return this.biomeSource.toBukkit(id);
    }

    @Override
    public List<Biome> getBiomes(final WorldInfo worldInfo) {
        return this.biomeSource.getUsedBukkitBiomes();
    }
}
