package ca.spottedleaf.oldgenerator.generator.v125.populate;

import ca.spottedleaf.oldgenerator.world.BlockAccess;
import org.bukkit.Material;

import java.util.Random;

public final class WorldGenBigMushroom125 {
    public boolean generate(final BlockAccess world, final Random random,
                            final int x, final int y, final int z) {
        final int type = random.nextInt(2);
        final int height = random.nextInt(3) + 4;
        if (y < 1 || y + height + 1 >= 128) return false;

        for (int yy = y; yy <= y + 1 + height; ++yy) {
            final int radius = yy == y ? 0 : 3;
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    if (world.getType(x + dx, yy, z + dz) != Material.AIR) return false;
                }
            }
        }

        final Material ground = world.getType(x, y - 1, z);
        if (ground != Material.DIRT && ground != Material.GRASS_BLOCK && ground != Material.MYCELIUM) return false;

        final Material cap = type == 0 ? Material.BROWN_MUSHROOM_BLOCK : Material.RED_MUSHROOM_BLOCK;
        for (int yy = 0; yy < height; ++yy) {
            if (world.getType(x, y + yy, z).isAir()) {
                world.setType(x, y + yy, z, Material.MUSHROOM_STEM, false);
            }
        }
        for (int yy = y + height - 1; yy <= y + height; ++yy) {
            final int radius = yy == y + height ? 3 : 2;
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius) continue;
                    if (world.getType(x + dx, yy, z + dz).isAir()) {
                        world.setType(x + dx, yy, z + dz, cap, false);
                    }
                }
            }
        }
        return true;
    }
}
