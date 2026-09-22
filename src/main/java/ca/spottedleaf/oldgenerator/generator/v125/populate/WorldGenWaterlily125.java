package ca.spottedleaf.oldgenerator.generator.v125.populate;

import ca.spottedleaf.oldgenerator.world.BlockAccess;
import org.bukkit.Material;

import java.util.Random;

public final class WorldGenWaterlily125 {
    public boolean generate(final BlockAccess world, final Random random,
                            final int x, final int y, final int z) {
        for (int i = 0; i < 10; ++i) {
            final int bx = x + random.nextInt(8) - random.nextInt(8);
            final int by = y + random.nextInt(4) - random.nextInt(4);
            final int bz = z + random.nextInt(8) - random.nextInt(8);
            if (world.getType(bx, by, bz).isAir() && world.getType(bx, by - 1, bz) == Material.WATER) {
                world.setType(bx, by, bz, Material.LILY_PAD, false);
            }
        }
        return true;
    }
}
