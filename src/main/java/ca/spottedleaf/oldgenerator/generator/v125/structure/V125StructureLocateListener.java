package ca.spottedleaf.oldgenerator.generator.v125.structure;

import ca.spottedleaf.oldgenerator.generator.v125.V125ChunkGenerator;
import ca.spottedleaf.oldgenerator.generator.v125.V125BiomeSource;
import io.papermc.paper.event.world.StructuresLocateEvent;
import io.papermc.paper.math.Position;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.generator.structure.Structure;

import java.util.List;

public final class V125StructureLocateListener implements Listener {
    private static final int PLAINS = 1;
    private static final int DESERT = 2;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStructuresLocate(final StructuresLocateEvent event) {
        if (event.getResult() != null) return;
        if (!(event.getWorld().getGenerator() instanceof V125ChunkGenerator generator)) return;

        final List<Structure> targets = event.getStructures();
        final boolean mineshaft = targets.contains(Structure.MINESHAFT);
        final boolean stronghold = targets.contains(Structure.STRONGHOLD);
        final boolean plainsVillage = targets.contains(Structure.VILLAGE_PLAINS);
        final boolean desertVillage = targets.contains(Structure.VILLAGE_DESERT);

        if (!mineshaft && !stronghold && !plainsVillage && !desertVillage) return;

        final V125StructureGenerator structures = generator.getStructureGenerator();
        final V125BiomeSource biomes = generator.getBiomeSource();
        final long seed = event.getWorld().getSeed();
        final Location origin = event.getOrigin();
        final int radius = Math.max(0, event.getRadius());

        int[] best = null;
        Structure resultStructure = null;
        double bestDistance = Double.POSITIVE_INFINITY;

        if (mineshaft) {
            best = structures.findNearestMineshaft(seed, origin.getBlockX(), origin.getBlockZ(), radius);
            resultStructure = Structure.MINESHAFT;
            bestDistance = distanceSquared(origin, best);
        }

        if (stronghold) {
            final int[] candidate = structures.findNearestStronghold(seed, origin.getBlockX(), origin.getBlockZ(), radius);
            final double distance = distanceSquared(origin, candidate);
            if (candidate != null && distance < bestDistance) {
                best = candidate;
                resultStructure = Structure.STRONGHOLD;
                bestDistance = distance;
            }
        }

        if (plainsVillage || desertVillage) {
            final int[] candidate = structures.findNearestVillage(
                    seed,
                    origin.getBlockX(),
                    origin.getBlockZ(),
                    radius,
                    plainsVillage,
                    desertVillage,
                    biomes
            );
            final double distance = distanceSquared(origin, candidate);
            if (candidate != null && distance < bestDistance) {
                final int biome = biomes.getBiomeIds(
                        seed, candidate[0] * 16 + 8, candidate[1] * 16 + 8, 1, 1
                )[0];
                best = candidate;
                resultStructure = biome == DESERT ? Structure.VILLAGE_DESERT : Structure.VILLAGE_PLAINS;
                bestDistance = distance;
            }
        }

        if (best == null || resultStructure == null) return;

        final Location result = new Location(
                event.getWorld(),
                (best[0] << 4) + 8,
                64,
                (best[1] << 4) + 8
        );
        event.setResult(new StructuresLocateEvent.Result(
                Position.block(result.getBlockX(), result.getBlockY(), result.getBlockZ()),
                resultStructure
        ));
    }

    private static double distanceSquared(final Location origin, final int[] chunk) {
        if (chunk == null) return Double.POSITIVE_INFINITY;
        final double x = (chunk[0] << 4) + 8 - origin.getX();
        final double z = (chunk[1] << 4) + 8 - origin.getZ();
        return x * x + z * z;
    }
}
