package ca.spottedleaf.oldgenerator.generator.v125.structure;

import ca.spottedleaf.oldgenerator.generator.v125.V125BiomeSource;
import ca.spottedleaf.oldgenerator.util.BlockConstants;
import org.bukkit.Material;
import ca.spottedleaf.oldgenerator.world.BlockAccess;

import java.util.Random;

/**
 * Legacy 1.2.5 structure placement bridge.
 *
 * The location RNGs are ported from the 1.2.5 MapGen* classes. The piece
 * layouts are deliberately compact modern-Bukkit implementations; they do
 * not depend on the modern server's structure generator and are deterministic
 * from the 1.2.5 world seed.
 */
public final class V125StructureGenerator {
    private static final int MAX_Y = 127;

    public Result generate(final long worldSeed, final int targetChunkX, final int targetChunkZ,
                           final BlockAccess data, final V125BiomeSource biomes) {
        boolean village = false;

        for (int cx = targetChunkX - 4; cx <= targetChunkX + 4; ++cx) {
            for (int cz = targetChunkZ - 4; cz <= targetChunkZ + 4; ++cz) {
                if (isMineshaftStart(worldSeed, cx, cz)) {
                    generateMineshaft(worldSeed, cx, cz, targetChunkX, targetChunkZ, data);
                }
            }
        }

        for (int cx = targetChunkX - 2; cx <= targetChunkX + 2; ++cx) {
            for (int cz = targetChunkZ - 2; cz <= targetChunkZ + 2; ++cz) {
                if (isVillageStart(worldSeed, cx, cz, biomes)) {
                    village = true;
                    generateVillage(worldSeed, cx, cz, targetChunkX, targetChunkZ, data);
                }
            }
        }

        final int[][] strongholds = strongholdChunks(worldSeed, biomes);
        for (int[] pos : strongholds) {
            if (pos[0] == targetChunkX && pos[1] == targetChunkZ) {
                generateStronghold(worldSeed, targetChunkX, targetChunkZ, data);
            }
        }

        return new Result(village);
    }

    public boolean hasVillageStart(final long seed, final int chunkX, final int chunkZ,
                                   final V125BiomeSource biomes) {
        return isVillageStart(seed, chunkX, chunkZ, biomes);
    }

    private static Random mapSeedRandom(final long seed, final int chunkX, final int chunkZ) {
        final Random random = new Random(seed);
        final long a = random.nextLong();
        final long b = random.nextLong();
        random.setSeed((long) chunkX * a ^ (long) chunkZ * b ^ seed);
        return random;
    }

    private static boolean isMineshaftStart(final long seed, final int chunkX, final int chunkZ) {
        final Random random = mapSeedRandom(seed, chunkX, chunkZ);
        random.nextInt();
        return random.nextInt(100) == 0 &&
                random.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ));
    }

    private static boolean isVillageStart(final long seed, final int chunkX, final int chunkZ,
                                          final V125BiomeSource biomes) {
        final int regionSize = 32;
        final int offset = 8;
        int adjustedX = chunkX;
        int adjustedZ = chunkZ;
        if (adjustedX < 0) adjustedX -= regionSize - 1;
        if (adjustedZ < 0) adjustedZ -= regionSize - 1;

        final int regionX = adjustedX / regionSize;
        final int regionZ = adjustedZ / regionSize;
        final Random random = new Random((long) regionX * 341873128712L +
                (long) regionZ * 132897987541L + seed + 10387312L);
        int candidateX = regionX * regionSize + random.nextInt(regionSize - offset);
        int candidateZ = regionZ * regionSize + random.nextInt(regionSize - offset);

        if (chunkX != candidateX || chunkZ != candidateZ) {
            return false;
        }

        final int biome = biomes.getBlockBiomeIds(seed, chunkX * 16 + 8, chunkZ * 16 + 8, 1, 1)[0];
        return biome == 1 || biome == 2;
    }

    private static int[][] strongholdChunks(final long seed, final V125BiomeSource biomes) {
        final int[][] result = new int[3][2];
        final int[] allowed = {2, 4, 3, 6, 5, 12, 13, 17, 18, 20, 21, 22};
        final Random random = new Random(seed);
        double angle = random.nextDouble() * Math.PI * 2.0D;
        for (int i = 0; i < result.length; ++i) {
            final double distance = (1.25D + random.nextDouble()) * 32.0D;
            int chunkX = (int)Math.round(Math.cos(angle) * distance);
            int chunkZ = (int)Math.round(Math.sin(angle) * distance);
            final long[] position = biomes.findBiomePosition(
                    seed, (chunkX << 4) + 8, (chunkZ << 4) + 8, 112, allowed, random);
            if (position != null) {
                chunkX = (int)(position[0] >> 4);
                chunkZ = (int)(position[1] >> 4);
            }
            result[i][0] = chunkX;
            result[i][1] = chunkZ;
            angle += Math.PI * 2.0D / 3.0D;
        }
        return result;
    }

    private static void generateMineshaft(final long seed, final int startChunkX, final int startChunkZ,
                                          final int targetChunkX, final int targetChunkZ,
                                          final BlockAccess data) {
        final Random random = mapSeedRandom(seed, startChunkX, startChunkZ);
        final int roomY = 20 + random.nextInt(35);
        final int length = 32 + random.nextInt(48);
        final int direction = random.nextInt(4);

        for (int i = 0; i < 3; ++i) {
            final int branchLength = length / 2 + random.nextInt(Math.max(1, length / 2));
            final int dx = direction == 1 ? 1 : direction == 3 ? -1 : 0;
            final int dz = direction == 0 ? -1 : direction == 2 ? 1 : 0;
            final int startX = (startChunkX << 4) + 8 + (direction == 1 ? 7 : direction == 3 ? -7 : 0);
            final int startZ = (startChunkZ << 4) + 8 + (direction == 0 ? -7 : direction == 2 ? 7 : 0);
            generateCorridor(data, targetChunkX, targetChunkZ, startX, roomY, startZ, dx, dz, branchLength);
        }
    }

    private static void generateCorridor(final ChunkData data, final int targetChunkX, final int targetChunkZ,
                                         final int startX, final int y, final int startZ,
                                         final int dx, final int dz, final int length) {
        for (int step = 0; step < length; ++step) {
            final int x = startX + dx * step;
            final int z = startZ + dz * step;
            for (int yy = -1; yy <= 2; ++yy) {
                for (int side = -1; side <= 1; ++side) {
                    final int xx = x + (dz == 0 ? side : 0);
                    final int zz = z + (dx == 0 ? side : 0);
                    set(data, targetChunkX, targetChunkZ, xx, y + yy, zz,
                            yy == -1 ? Material.COBBLESTONE : Material.AIR);
                }
            }
            if ((step & 4) == 0) {
                set(data, targetChunkX, targetChunkZ, x, y, z, Material.RAIL);
                set(data, targetChunkX, targetChunkZ, x, y + 1, z - 1, Material.OAK_FENCE);
                set(data, targetChunkX, targetChunkZ, x, y + 1, z + 1, Material.OAK_FENCE);
            }
        }
    }

    private static void generateVillage(final long seed, final int startChunkX, final int startChunkZ,
                                        final int targetChunkX, final int targetChunkZ,
                                        final BlockAccess data) {
        final Random random = mapSeedRandom(seed, startChunkX, startChunkZ);
        final int centerX = (startChunkX << 4) + 8;
        final int centerZ = (startChunkZ << 4) + 8;
        final int y = surfaceY(data, targetChunkX, targetChunkZ, centerX, centerZ);

        if (Math.abs(targetChunkX - startChunkX) > 2 || Math.abs(targetChunkZ - startChunkZ) > 2) return;

        buildRoad(data, targetChunkX, targetChunkZ, centerX, y, centerZ, 40, 0);
        buildRoad(data, targetChunkX, targetChunkZ, centerX, y, centerZ, -40, 0);
        buildRoad(data, targetChunkX, targetChunkZ, centerX, y, centerZ, 0, 40);
        buildRoad(data, targetChunkX, targetChunkZ, centerX, y, centerZ, 0, -40);
        buildWell(data, targetChunkX, targetChunkZ, centerX, y, centerZ);

        final int houses = 3 + random.nextInt(3);
        for (int i = 0; i < houses; ++i) {
            final int x = centerX + random.nextInt(48) - 24;
            final int z = centerZ + random.nextInt(48) - 24;
            final int houseY = surfaceY(data, targetChunkX, targetChunkZ, x, z);
            buildHouse(data, targetChunkX, targetChunkZ, x, houseY, z);
        }
    }

    private static void buildRoad(final ChunkData data, final int targetChunkX, final int targetChunkZ,
                                  final int x, final int y, final int z, final int dx, final int dz) {
        final int steps = Math.max(Math.abs(dx), Math.abs(dz));
        final int sx = Integer.compare(dx, 0);
        final int sz = Integer.compare(dz, 0);
        for (int i = 0; i <= steps; ++i) {
            final int px = x + sx * i;
            final int pz = z + sz * i;
            for (int side = -1; side <= 1; ++side) {
                final int rx = px + (sz != 0 ? side : 0);
                final int rz = pz + (sx != 0 ? side : 0);
                set(data, targetChunkX, targetChunkZ, rx, y, rz, Material.GRAVEL);
            }
        }
    }

    private static void buildWell(final ChunkData data, final int targetChunkX, final int targetChunkZ,
                                  final int x, final int y, final int z) {
        for (int dx = -2; dx <= 2; ++dx) {
            for (int dz = -2; dz <= 2; ++dz) {
                final boolean edge = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                set(data, targetChunkX, targetChunkZ, x + dx, y, z + dz,
                        edge ? Material.COBBLESTONE : Material.WATER);
            }
        }
        for (int yy = 1; yy <= 2; ++yy) {
            for (int dx : new int[]{-2, 2}) for (int dz : new int[]{-2, 2}) {
                set(data, targetChunkX, targetChunkZ, x + dx, y + yy, z + dz, Material.COBBLESTONE);
            }
        }
    }

    private static void buildHouse(final ChunkData data, final int targetChunkX, final int targetChunkZ,
                                   final int x, final int y, final int z) {
        for (int dx = -3; dx <= 3; ++dx) {
            for (int dz = -3; dz <= 3; ++dz) {
                for (int yy = 0; yy <= 3; ++yy) {
                    final boolean wall = Math.abs(dx) == 3 || Math.abs(dz) == 3 || yy == 0;
                    if (!wall && yy < 3) set(data, targetChunkX, targetChunkZ, x + dx, y + yy, z + dz, Material.AIR);
                    else set(data, targetChunkX, targetChunkZ, x + dx, y + yy, z + dz, wall ? Material.OAK_PLANKS : Material.OAK_PLANKS);
                }
            }
        }
        for (int dx = -3; dx <= 3; ++dx) for (int dz = -3; dz <= 3; ++dz) {
            set(data, targetChunkX, targetChunkZ, x + dx, y + 4, z + dz,
                    Math.abs(dx) == 3 && Math.abs(dz) == 3 ? Material.OAK_LOG : Material.OAK_PLANKS);
        }
        set(data, targetChunkX, targetChunkZ, x, y + 1, z + 3, Material.OAK_DOOR);
        set(data, targetChunkX, targetChunkZ, x - 2, y + 2, z + 3, Material.GLASS_PANE);
        set(data, targetChunkX, targetChunkZ, x + 2, y + 2, z + 3, Material.GLASS_PANE);
    }

    private static void generateStronghold(final long seed, final int chunkX, final int chunkZ,
                                           final BlockAccess data) {
        final Random random = mapSeedRandom(seed, chunkX, chunkZ);
        final int y = 30 + random.nextInt(16);
        final int centerX = (chunkX << 4) + 8;
        final int centerZ = (chunkZ << 4) + 8;

        for (int room = 0; room < 3; ++room) {
            final int rx = centerX + random.nextInt(24) - 12;
            final int rz = centerZ + random.nextInt(24) - 12;
            buildStoneRoom(data, chunkX, chunkZ, rx, y + room * 4, rz, room == 2);
        }
    }

    private static void buildStoneRoom(final ChunkData data, final int targetChunkX, final int targetChunkZ,
                                       final int x, final int y, final int z, final boolean portal) {
        final Material wall = Material.STONE_BRICKS;
        final Material floor = portal ? Material.END_PORTAL_FRAME : Material.STONE_BRICKS;
        for (int dx = -4; dx <= 4; ++dx) {
            for (int dz = -4; dz <= 4; ++dz) {
                for (int yy = 0; yy <= 4; ++yy) {
                    final boolean shell = Math.abs(dx) == 4 || Math.abs(dz) == 4 || yy == 0 || yy == 4;
                    set(data, targetChunkX, targetChunkZ, x + dx, y + yy, z + dz,
                            shell ? wall : Material.AIR);
                }
            }
        }
        for (int dx = -2; dx <= 2; ++dx) for (int dz = -2; dz <= 2; ++dz) {
            set(data, targetChunkX, targetChunkZ, x + dx, y, z + dz, floor);
        }
        if (portal) {
            set(data, targetChunkX, targetChunkZ, x, y, z, Material.END_PORTAL);
        }
    }

    private static int surfaceY(final BlockAccess data, final int targetChunkX, final int targetChunkZ,
                                final int worldX, final int worldZ) {
        final int lx = worldX - (targetChunkX << 4);
        final int lz = worldZ - (targetChunkZ << 4);
        if (lx < 0 || lx > 15 || lz < 0 || lz > 15) return 63;
        for (int y = MAX_Y; y > 0; --y) {
            if (!data.getType(lx, y, lz).isAir()) return y + 1;
        }
        return 63;
    }

    private static void set(final BlockAccess data, final int chunkX, final int chunkZ,
                            final int worldX, final int y, final int worldZ, final Material material) {
        if (y < 0 || y > MAX_Y) return;
        final int localX = worldX - (chunkX << 4);
        final int localZ = worldZ - (chunkZ << 4);
        if (localX < 0 || localX > 15 || localZ < 0 || localZ > 15) return;
        data.setBlock(localX, y, localZ, material);
    }

    public static final class Result {
        private final boolean villageGenerated;
        Result(final boolean villageGenerated) {
            this.villageGenerated = villageGenerated;
        }
        public boolean hasVillage() {
            return villageGenerated;
        }
    }
}
