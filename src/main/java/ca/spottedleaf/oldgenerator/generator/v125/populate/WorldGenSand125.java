package ca.spottedleaf.oldgenerator.generator.v125.populate;

import ca.spottedleaf.oldgenerator.world.BlockAccess;
import org.bukkit.Material;

import java.util.Random;

public final class WorldGenSand125 {
    private final Material material;
    private final int radius;

    public WorldGenSand125(final int radius, final Material material) {
        this.radius = radius;
        this.material = material;
    }

    public boolean generate(final BlockAccess world, final Random random,
                            final int x, final int y, final int z) {
        if (world.getType(x, y, z) != Material.WATER) {
            return false;
        }
        final int actualRadius = random.nextInt(this.radius - 2) + 2;
        final int verticalRadius = 2;
        for (int dx = -actualRadius; dx <= actualRadius; ++dx) {
            for (int dz = -actualRadius; dz <= actualRadius; ++dz) {
                if (dx * dx + dz * dz > actualRadius * actualRadius) continue;
                for (int dy = -verticalRadius; dy <= verticalRadius; ++dy) {
                    final int bx = x + dx;
                    final int by = y + dy;
                    final int bz = z + dz;
                    final Material existing = world.getType(bx, by, bz);
                    if (existing == Material.DIRT || existing == Material.GRASS_BLOCK) {
                        world.setType(bx, by, bz, this.material, false);
                    }
                }
            }
        }
        return true;
    }
}
