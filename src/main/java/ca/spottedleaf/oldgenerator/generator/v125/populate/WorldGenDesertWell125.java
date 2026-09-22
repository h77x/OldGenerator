package ca.spottedleaf.oldgenerator.generator.v125.populate;

import ca.spottedleaf.oldgenerator.world.BlockAccess;
import org.bukkit.Material;
import java.util.Random;

public final class WorldGenDesertWell125 {
    public boolean generate(final BlockAccess world, final Random random, final int x, final int y, final int z) {
        if (y < 2 || y > 125) return false;
        final Material ground = world.getType(x, y - 1, z);
        if (ground != Material.SAND) return false;

        for (int yy = -1; yy <= 3; ++yy) {
            for (int dx = -2; dx <= 2; ++dx) {
                for (int dz = -2; dz <= 2; ++dz) {
                    if (yy == -1 || yy == 0) {
                        world.setType(x + dx, y + yy, z + dz,
                                Math.abs(dx) == 2 || Math.abs(dz) == 2 ? Material.SANDSTONE : Material.SANDSTONE, false);
                    } else if (yy == 1 || yy == 2) {
                        world.setType(x + dx, y + yy, z + dz,
                                (Math.abs(dx) == 2 || Math.abs(dz) == 2) && (dx == -2 || dx == 2 || dz == -2 || dz == 2)
                                        ? Material.SANDSTONE : Material.AIR, false);
                    } else if (yy == 3 && Math.abs(dx) == 2 && Math.abs(dz) == 2) {
                        world.setType(x + dx, y + yy, z + dz, Material.SANDSTONE, false);
                    }
                }
            }
        }
        for (int dx = -1; dx <= 1; ++dx) for (int dz = -1; dz <= 1; ++dz) {
            world.setType(x + dx, y, z + dz, Material.WATER, false);
        }
        return true;
    }
}
