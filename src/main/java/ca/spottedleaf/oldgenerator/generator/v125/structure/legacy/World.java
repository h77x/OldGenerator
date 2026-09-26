package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

import ca.spottedleaf.oldgenerator.generator.v125.V125BiomeSource;
import ca.spottedleaf.oldgenerator.world.BlockAccess;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import java.util.HashMap;
import java.util.Map;

public final class World {
    public final WorldProvider worldProvider=new WorldProvider();
    public boolean editingBlocks;
    public boolean scheduledUpdatesAreImmediate;
    private final BlockAccess access;
    private final long seed;
    private final WorldChunkManager manager;
    private final Map<Long, TileEntityChest> chests = new HashMap<>();
    private final Map<Long, TileEntityMobSpawner> spawners = new HashMap<>();
    private final Map<Long, Integer> legacyIds = new HashMap<>();
    private final Map<Long, Integer> legacyMetadata = new HashMap<>();

    public World(final long seed, final BlockAccess access, final V125BiomeSource source){
        this.seed=seed;this.access=access;this.manager=new WorldChunkManager(source,seed);
    }
    public World(final BlockAccess access){ this.seed=0L; this.access=access; this.manager=null; }
    public long getSeed(){return seed;}
    public WorldChunkManager getWorldChunkManager(){return manager;}
    public boolean isInRegion(final int x, final int y, final int z){return access.isInRegion(x, y, z);}
    public int getBlockId(int x,int y,int z){
        final long key = blockKey(x, y, z);
        final Integer stored = legacyIds.get(key);
        if (stored != null) return stored;
        final org.bukkit.Material m=access.getType(x,y,z);
        if (m == org.bukkit.Material.WATER) return Block.waterStill.blockID;
        if (m == org.bukkit.Material.LAVA) return Block.lavaStill.blockID;
        if (m == org.bukkit.Material.OAK_LOG || m == org.bukkit.Material.SPRUCE_LOG
                || m == org.bukkit.Material.BIRCH_LOG || m == org.bukkit.Material.JUNGLE_LOG
                || m == org.bukkit.Material.OAK_WOOD || m == org.bukkit.Material.SPRUCE_WOOD
                || m == org.bukkit.Material.BIRCH_WOOD || m == org.bukkit.Material.JUNGLE_WOOD) return Block.wood.blockID;
        if (m == org.bukkit.Material.OAK_LEAVES || m == org.bukkit.Material.SPRUCE_LEAVES
                || m == org.bukkit.Material.BIRCH_LEAVES || m == org.bukkit.Material.JUNGLE_LEAVES) return Block.leaves.blockID;
        if (m == org.bukkit.Material.OAK_PLANKS || m == org.bukkit.Material.SPRUCE_PLANKS
                || m == org.bukkit.Material.BIRCH_PLANKS || m == org.bukkit.Material.JUNGLE_PLANKS) return Block.planks.blockID;
        if (m == org.bukkit.Material.OAK_SAPLING || m == org.bukkit.Material.SPRUCE_SAPLING
                || m == org.bukkit.Material.BIRCH_SAPLING || m == org.bukkit.Material.JUNGLE_SAPLING) return Block.sapling.blockID;
        if (m == org.bukkit.Material.SHORT_GRASS || m == org.bukkit.Material.FERN) return Block.tallGrass.blockID;
        for (Block b : Block.blocksList) if (b != null && b.material == m) return b.blockID;
        return m == org.bukkit.Material.AIR ? 0 : 1;
    }
    public Material getBlockMaterial(int x,int y,int z){
        int id=getBlockId(x,y,z); Block b=id>=0&&id<Block.blocksList.length?Block.blocksList[id]:null;
        return b==null?new Material(false,false):b.blockMaterial;
    }
    public boolean isAirBlock(int x,int y,int z){return access.getType(x,y,z).isAir();}
    public void setBlockAndMetadata(int x,int y,int z,int id,int meta){setInternal(x,y,z,id,meta);}
    public void setBlockAndMetadataWithNotify(int x,int y,int z,int id,int meta){setInternal(x,y,z,id,meta);}
    public void setBlockWithNotify(int x,int y,int z,int id){setInternal(x,y,z,id,0);}
    public void setBlock(int x,int y,int z,int id){setInternal(x,y,z,id,0);}
    public void setBlockTileEntity(final int x, final int y, final int z, final Object tile) {
        if (tile instanceof TileEntityChest chest) {
            syncChest(x, y, z, chest.getItems());
        } else if (tile instanceof TileEntityMobSpawner spawner) {
            syncSpawner(x, y, z, spawner.getMobID());
        }
    }

    public Object getBlockTileEntity(final int x, final int y, final int z) {
        final int id = getBlockId(x, y, z);
        final long key = blockKey(x, y, z);
        if (id == Block.chest.blockID) {
            return chests.computeIfAbsent(key, ignored -> new TileEntityChest(this, x, y, z));
        }
        if (id == Block.mobSpawner.blockID) {
            return spawners.computeIfAbsent(key, ignored -> new TileEntityMobSpawner(this, x, y, z));
        }
        return null;
    }

    private static long blockKey(final int x, final int y, final int z) {
        return (((long)x & 0x3FFFFFFL) << 38)
                | (((long)z & 0x3FFFFFFL) << 12)
                | (y & 0xFFFL);
    }

    void syncChest(final int x, final int y, final int z, final ItemStack[] items) {
        final org.bukkit.block.BlockState state = access.getBlockState(x, y, z);
        if (!(state instanceof Chest chest)) return;
        final org.bukkit.inventory.Inventory inventory = chest.getBlockInventory();
        inventory.clear();
        for (int slot = 0; slot < items.length; ++slot) {
            final org.bukkit.inventory.ItemStack item = toBukkitItem(items[slot]);
            if (item != null) inventory.setItem(slot, item);
        }
        state.update(false, false);
    }

    void syncSpawner(final int x, final int y, final int z, final String mobId) {
        final org.bukkit.block.BlockState state = access.getBlockState(x, y, z);
        if (!(state instanceof CreatureSpawner spawner)) return;
        final EntityType type = EntityType.fromName(mobId);
        if (type != null) {
            spawner.setSpawnedType(type);
            state.update(false, false);
        }
    }

    private static org.bukkit.inventory.ItemStack toBukkitItem(final ItemStack item) {
        if (item == null) return null;
        final org.bukkit.Material material;
        switch (item.itemID) {
            case 256: material = org.bukkit.Material.IRON_SHOVEL; break;
            case 257: material = org.bukkit.Material.IRON_PICKAXE; break;
            case 258: material = org.bukkit.Material.IRON_AXE; break;
            case 259: material = org.bukkit.Material.FLINT_AND_STEEL; break;
            case 260: material = org.bukkit.Material.APPLE; break;
            case 261: material = org.bukkit.Material.BOW; break;
            case 262: material = org.bukkit.Material.ARROW; break;
            case 263: material = org.bukkit.Material.COAL; break;
            case 264: material = org.bukkit.Material.DIAMOND; break;
            case 265: material = org.bukkit.Material.IRON_INGOT; break;
            case 266: material = org.bukkit.Material.GOLD_INGOT; break;
            case 267: material = org.bukkit.Material.IRON_SWORD; break;
            case 268: material = org.bukkit.Material.STONE_SWORD; break;
            case 269: material = org.bukkit.Material.WOODEN_SHOVEL; break;
            case 270: material = org.bukkit.Material.WOODEN_PICKAXE; break;
            case 271: material = org.bukkit.Material.WOODEN_AXE; break;
            case 272: material = org.bukkit.Material.STONE_SWORD; break;
            case 273: material = org.bukkit.Material.STONE_SHOVEL; break;
            case 274: material = org.bukkit.Material.STONE_PICKAXE; break;
            case 275: material = org.bukkit.Material.STONE_AXE; break;
            case 280: material = org.bukkit.Material.STICK; break;
            case 287: material = org.bukkit.Material.STRING; break;
            case 289: material = org.bukkit.Material.GUNPOWDER; break;
            case 296: material = org.bukkit.Material.WHEAT; break;
            case 297: material = org.bukkit.Material.BREAD; break;
            case 306: material = org.bukkit.Material.IRON_HELMET; break;
            case 307: material = org.bukkit.Material.IRON_CHESTPLATE; break;
            case 308: material = org.bukkit.Material.IRON_LEGGINGS; break;
            case 309: material = org.bukkit.Material.IRON_BOOTS; break;
            case 322: material = org.bukkit.Material.GOLDEN_APPLE; break;
            case 325: material = org.bukkit.Material.BUCKET; break;
            case 329: material = org.bukkit.Material.SADDLE; break;
            case 331: material = org.bukkit.Material.REDSTONE; break;
            case 339: material = org.bukkit.Material.PAPER; break;
            case 340: material = org.bukkit.Material.BOOK; break;
            case 345: material = org.bukkit.Material.COMPASS; break;
            case 358: material = org.bukkit.Material.FILLED_MAP; break;
            case 361: material = org.bukkit.Material.PUMPKIN_SEEDS; break;
            case 362: material = org.bukkit.Material.MELON_SEEDS; break;
            case 368: material = org.bukkit.Material.ENDER_PEARL; break;
            case 351: material = item.itemDamage == 3 ? org.bukkit.Material.COCOA_BEANS : org.bukkit.Material.INK_SAC; break;
            case 2256: material = org.bukkit.Material.MUSIC_DISC_13; break;
            case 2257: material = org.bukkit.Material.MUSIC_DISC_CAT; break;
            default: return null;
        }
        return new org.bukkit.inventory.ItemStack(material, Math.max(1, item.stackSize));
    }

    private void setInternal(final int x, final int y, final int z, final int id, final int meta) {
        if (!access.isInRegion(x, y, z)) return;
        final Block block = (id >= 0 && id < Block.blocksList.length) ? Block.blocksList[id] : null;
        if (block == null) {
            access.setType(x, y, z, org.bukkit.Material.AIR, false);
            return;
        }

        final org.bukkit.Material material = modernMaterial(id, meta, block.material);
        final long key = blockKey(x, y, z);
        if (id == Block.air.blockID) {
            legacyIds.remove(key);
            legacyMetadata.remove(key);
        } else {
            legacyIds.put(key, id);
            legacyMetadata.put(key, meta);
        }

        final int stateMeta = normalizePlacementMetadata(x, y, z, id, meta);
        final org.bukkit.Material stateMaterial = modernMaterial(id, stateMeta, block.material);
        final BlockData data = createBlockData(id, stateMeta, stateMaterial);
        if (data != null) {
            access.setBlockData(x, y, z, data, false);
        } else {
            access.setType(x, y, z, stateMaterial, false);
        }

        // Vanilla 1.2.5 recalculates attachable/connecting block shapes when
        // neighbours change. Modern BlockData is static when physics is disabled,
        // so refresh the affected neighbourhood explicitly.
        refreshLegacyConnectables(x, y, z);
    }

    private int normalizePlacementMetadata(final int x, final int y, final int z,
                                           final int id, final int meta) {
        if (id != Block.torchWood.blockID || (meta != 0 && meta != 15)) return meta;

        // BlockTorch.onBlockAdded() in 1.2.5 checks these supports in this exact order.
        if (isTorchSupport(x - 1, y, z)) return 1;
        if (isTorchSupport(x + 1, y, z)) return 2;
        if (isTorchSupport(x, y, z - 1)) return 3;
        if (isTorchSupport(x, y, z + 1)) return 4;
        if (isTorchSupport(x, y - 1, z)) return 5;
        return meta;
    }

    private boolean isTorchSupport(final int x, final int y, final int z) {
        final int id = getBlockId(x, y, z);
        if (id < 0 || id >= Block.blocksList.length) return false;
        final Block block = Block.blocksList[id];
        if (block == null) return false;
        if (Block.opaqueCubeLookup[id]) return true;
        return id == Block.fence.blockID
                || id == Block.fenceGate.blockID
                || id == Block.glass.blockID
                || id == Block.stairCompactCobblestone.blockID
                || id == Block.stairCompactPlanks.blockID
                || id == Block.stairsNetherBrick.blockID
                || id == Block.stairsStoneBrickSmooth.blockID;
    }

    private void refreshLegacyConnectables(final int x, final int y, final int z) {
        refreshLegacyConnectable(x, y, z);
        refreshLegacyConnectable(x - 1, y, z);
        refreshLegacyConnectable(x + 1, y, z);
        refreshLegacyConnectable(x, y, z - 1);
        refreshLegacyConnectable(x, y, z + 1);
    }

    private void refreshLegacyConnectable(final int x, final int y, final int z) {
        if (!access.isInRegion(x, y, z)) return;
        final int id = getBlockId(x, y, z);
        if (id == Block.torchWood.blockID) {
            final int meta = getBlockMetadata(x, y, z);
            if (meta == 0 || meta == 15) {
                final int oriented = normalizePlacementMetadata(x, y, z, id, meta);
                if (oriented != meta) writeModernBlockData(x, y, z, id, oriented);
            }
            return;
        }
        if (id != Block.fence.blockID && id != Block.fenceIron.blockID && id != Block.thinGlass.blockID) return;

        final BlockData data = access.getBlockData(x, y, z);
        if (!(data instanceof MultipleFacing multipleFacing)) return;

        final boolean north = legacyConnects(id, x, y, z - 1);
        final boolean south = legacyConnects(id, x, y, z + 1);
        final boolean west = legacyConnects(id, x - 1, y, z);
        final boolean east = legacyConnects(id, x + 1, y, z);
        multipleFacing.setFace(BlockFace.NORTH, north);
        multipleFacing.setFace(BlockFace.SOUTH, south);
        multipleFacing.setFace(BlockFace.WEST, west);
        multipleFacing.setFace(BlockFace.EAST, east);
        access.setBlockData(x, y, z, data, false);
    }

    private boolean legacyConnects(final int id, final int x, final int y, final int z) {
        final int neighbour = getBlockId(x, y, z);
        if (neighbour == 0) return false;
        if (id == Block.fence.blockID) {
            if (neighbour == Block.fence.blockID || neighbour == Block.fenceGate.blockID) return true;
            final Block block = neighbour >= 0 && neighbour < Block.blocksList.length ? Block.blocksList[neighbour] : null;
            return block != null && Block.opaqueCubeLookup[neighbour] && neighbour != Block.pumpkin.blockID;
        }
        if (id == Block.thinGlass.blockID) {
            if (neighbour == Block.thinGlass.blockID || neighbour == Block.fenceIron.blockID) return true;
            return neighbour >= 0 && neighbour < Block.blocksList.length && Block.opaqueCubeLookup[neighbour];
        }
        return neighbour == Block.fenceIron.blockID;
    }

    private void writeModernBlockData(final int x, final int y, final int z, final int id, final int meta) {
        final Block block = id >= 0 && id < Block.blocksList.length ? Block.blocksList[id] : null;
        if (block == null) return;
        final org.bukkit.Material material = modernMaterial(id, meta, block.material);
        final BlockData data = createBlockData(id, meta, material);
        if (data != null) access.setBlockData(x, y, z, data, false);
    }

    private static org.bukkit.Material modernMaterial(final int id, final int meta, final org.bukkit.Material fallback) {
        if (id == Block.wood.blockID) {
            switch (meta & 3) {
                case 1: return org.bukkit.Material.SPRUCE_LOG;
                case 2: return org.bukkit.Material.BIRCH_LOG;
                case 3: return org.bukkit.Material.JUNGLE_LOG;
                default: return org.bukkit.Material.OAK_LOG;
            }
        }
        if (id == Block.leaves.blockID) {
            switch (meta & 3) {
                case 1: return org.bukkit.Material.SPRUCE_LEAVES;
                case 2: return org.bukkit.Material.BIRCH_LEAVES;
                case 3: return org.bukkit.Material.JUNGLE_LEAVES;
                default: return org.bukkit.Material.OAK_LEAVES;
            }
        }
        if (id == Block.planks.blockID) {
            switch (meta & 3) {
                case 1: return org.bukkit.Material.SPRUCE_PLANKS;
                case 2: return org.bukkit.Material.BIRCH_PLANKS;
                case 3: return org.bukkit.Material.JUNGLE_PLANKS;
                default: return org.bukkit.Material.OAK_PLANKS;
            }
        }
        if (id == Block.sapling.blockID) {
            switch (meta & 3) {
                case 1: return org.bukkit.Material.SPRUCE_SAPLING;
                case 2: return org.bukkit.Material.BIRCH_SAPLING;
                case 3: return org.bukkit.Material.JUNGLE_SAPLING;
                default: return org.bukkit.Material.OAK_SAPLING;
            }
        }
        if (id == Block.tallGrass.blockID) {
            switch (meta & 3) {
                case 0: return org.bukkit.Material.DEAD_BUSH;
                case 2: return org.bukkit.Material.FERN;
                default: return org.bukkit.Material.SHORT_GRASS;
            }
        }
        if (id == Block.stairSingle.blockID || id == Block.stairDouble.blockID) {
            switch (meta & 7) {
                case 1: return org.bukkit.Material.SANDSTONE_SLAB;
                case 2: return org.bukkit.Material.OAK_SLAB;
                case 3: return org.bukkit.Material.COBBLESTONE_SLAB;
                case 4: return org.bukkit.Material.BRICK_SLAB;
                case 5: return org.bukkit.Material.STONE_BRICK_SLAB;
                default: return org.bukkit.Material.STONE_SLAB;
            }
        }
        if (id == Block.sandStone.blockID) {
            switch (meta & 3) {
                case 1: return org.bukkit.Material.CHISELED_SANDSTONE;
                case 2: return org.bukkit.Material.SMOOTH_SANDSTONE;
                default: return org.bukkit.Material.SANDSTONE;
            }
        }
        if (id == Block.stoneBrick.blockID) {
            switch (meta & 3) {
                case 1: return org.bukkit.Material.MOSSY_STONE_BRICKS;
                case 2: return org.bukkit.Material.CRACKED_STONE_BRICKS;
                case 3: return org.bukkit.Material.CHISELED_STONE_BRICKS;
                default: return org.bukkit.Material.STONE_BRICKS;
            }
        }
        if (id == Block.torchWood.blockID) {
            return meta >= 1 && meta <= 4 ? org.bukkit.Material.WALL_TORCH : org.bukkit.Material.TORCH;
        }
        if (id == Block.chest.blockID) return org.bukkit.Material.CHEST;
        if (id == Block.button.blockID) return org.bukkit.Material.STONE_BUTTON;
        if (id == Block.pressurePlatePlanks.blockID) return org.bukkit.Material.OAK_PRESSURE_PLATE;
        if (id == Block.cloth.blockID) {
            final org.bukkit.Material[] wool = {
                org.bukkit.Material.WHITE_WOOL, org.bukkit.Material.ORANGE_WOOL, org.bukkit.Material.MAGENTA_WOOL,
                org.bukkit.Material.LIGHT_BLUE_WOOL, org.bukkit.Material.YELLOW_WOOL, org.bukkit.Material.LIME_WOOL,
                org.bukkit.Material.PINK_WOOL, org.bukkit.Material.GRAY_WOOL, org.bukkit.Material.LIGHT_GRAY_WOOL,
                org.bukkit.Material.CYAN_WOOL, org.bukkit.Material.PURPLE_WOOL, org.bukkit.Material.BLUE_WOOL,
                org.bukkit.Material.BROWN_WOOL, org.bukkit.Material.GREEN_WOOL, org.bukkit.Material.RED_WOOL,
                org.bukkit.Material.BLACK_WOOL
            };
            return wool[meta & 15];
        }
        return fallback;
    }

    private static BlockData createBlockData(final int id, final int meta, final org.bukkit.Material material) {
        try {
            final BlockData data = material.createBlockData();

            if (id == Block.wood.blockID && data instanceof Orientable orientable) {
                orientable.setAxis((meta & 12) == 4 ? org.bukkit.Axis.X : ((meta & 12) == 8 ? org.bukkit.Axis.Z : org.bukkit.Axis.Y));
            } else if (id == Block.stairCompactPlanks.blockID || id == Block.stairCompactCobblestone.blockID
                    || id == Block.stairsNetherBrick.blockID || id == Block.stairsStoneBrickSmooth.blockID) {
                if (data instanceof Stairs stairs) {
                    final int facing = meta & 3;
                    stairs.setFacing(switch (facing) {
                        case 0 -> BlockFace.EAST;
                        case 1 -> BlockFace.WEST;
                        case 2 -> BlockFace.SOUTH;
                        default -> BlockFace.NORTH;
                    });
                    if (data instanceof Bisected bisected) {
                        bisected.setHalf((meta & 4) != 0 ? Bisected.Half.TOP : Bisected.Half.BOTTOM);
                    }
                }
            } else if (id == Block.ladder.blockID && data instanceof Directional directional) {
                directional.setFacing(switch (meta & 7) {
                    case 2 -> BlockFace.SOUTH;
                    case 3 -> BlockFace.NORTH;
                    case 4 -> BlockFace.EAST;
                    default -> BlockFace.WEST;
                });
            } else if (id == Block.rail.blockID && data instanceof Rail rail) {
                final int shape = meta & 15;
                rail.setShape(switch (shape) {
                    case 1 -> Rail.Shape.ASCENDING_EAST;
                    case 2 -> Rail.Shape.ASCENDING_WEST;
                    case 3 -> Rail.Shape.ASCENDING_NORTH;
                    case 4 -> Rail.Shape.ASCENDING_SOUTH;
                    case 5 -> Rail.Shape.SOUTH_EAST;
                    case 6 -> Rail.Shape.SOUTH_WEST;
                    case 7 -> Rail.Shape.NORTH_WEST;
                    case 8 -> Rail.Shape.NORTH_EAST;
                    default -> (shape == 0 ? Rail.Shape.NORTH_SOUTH : Rail.Shape.EAST_WEST);
                });
            } else if ((id == Block.doorWood.blockID || id == Block.doorSteel.blockID) && data instanceof Door door) {
                final boolean top = (meta & 8) != 0;
                if (top) {
                    door.setHalf(Bisected.Half.TOP);
                    door.setHinge((meta & 1) != 0 ? Door.Hinge.RIGHT : Door.Hinge.LEFT);
                } else {
                    door.setOpen((meta & 4) != 0);
                    door.setFacing(switch (meta & 3) {
                        case 0 -> BlockFace.EAST;
                        case 1 -> BlockFace.SOUTH;
                        case 2 -> BlockFace.WEST;
                        default -> BlockFace.NORTH;
                    });
                    door.setHalf(Bisected.Half.BOTTOM);
                }
            } else if (id == Block.fenceGate.blockID && data instanceof Gate gate) {
                gate.setFacing(switch (meta & 3) {
                    case 0 -> BlockFace.SOUTH;
                    case 1 -> BlockFace.WEST;
                    case 2 -> BlockFace.NORTH;
                    default -> BlockFace.EAST;
                });
                gate.setOpen((meta & 4) != 0);
            } else if (id == Block.vine.blockID && data instanceof org.bukkit.block.data.MultipleFacing vine) {
                vine.setFace(BlockFace.NORTH, (meta & 1) != 0);
                vine.setFace(BlockFace.EAST, (meta & 2) != 0);
                vine.setFace(BlockFace.SOUTH, (meta & 4) != 0);
                vine.setFace(BlockFace.WEST, (meta & 8) != 0);
            } else if (id == Block.torchWood.blockID && data instanceof Directional directional
                    && material == org.bukkit.Material.WALL_TORCH) {
                directional.setFacing(switch (meta & 7) {
                    case 1 -> BlockFace.WEST;
                    case 2 -> BlockFace.EAST;
                    case 3 -> BlockFace.NORTH;
                    default -> BlockFace.SOUTH;
                });
            } else if (id == Block.crops.blockID && data instanceof Ageable ageable) {
                ageable.setAge(Math.max(0, Math.min(ageable.getMaximumAge(), meta & 7)));
            } else if (id == Block.snow.blockID && data instanceof org.bukkit.block.data.BlockData) {
                try {
                    final org.bukkit.block.data.type.Snow snow = (org.bukkit.block.data.type.Snow)data;
                    snow.setLayers(Math.max(1, Math.min(8, (meta & 7) + 1)));
                } catch (Throwable ignored) {
                }
            } else if ((id == Block.stairSingle.blockID || id == Block.stairDouble.blockID)
                    && data instanceof Slab slab) {
                if (id == Block.stairDouble.blockID) {
                    slab.setType(Slab.Type.DOUBLE);
                } else {
                    slab.setType((meta & 8) != 0 ? Slab.Type.TOP : Slab.Type.BOTTOM);
                }
            } else if (id == Block.chest.blockID && data instanceof org.bukkit.block.data.type.Chest chest) {
                chest.setFacing(switch (meta & 7) {
                    case 2 -> BlockFace.NORTH;
                    case 3 -> BlockFace.SOUTH;
                    case 4 -> BlockFace.WEST;
                    case 5 -> BlockFace.EAST;
                    default -> BlockFace.NORTH;
                });
            } else if (id == Block.button.blockID && data instanceof Directional directional) {
                directional.setFacing(switch (meta & 7) {
                    case 1 -> BlockFace.WEST;
                    case 2 -> BlockFace.EAST;
                    case 3 -> BlockFace.NORTH;
                    case 4 -> BlockFace.SOUTH;
                    default -> BlockFace.NORTH;
                });
            } else if (id == Block.pumpkin.blockID && data instanceof Directional directional) {
                directional.setFacing(switch (meta & 3) {
                    case 0 -> BlockFace.SOUTH;
                    case 1 -> BlockFace.WEST;
                    case 2 -> BlockFace.NORTH;
                    default -> BlockFace.EAST;
                });
            } else if ((id == Block.stoneOvenIdle.blockID || id == Block.stoneOvenActive.blockID) && data instanceof Directional directional) {
                directional.setFacing(switch (meta & 7) {
                    case 2 -> BlockFace.NORTH;
                    case 3 -> BlockFace.SOUTH;
                    case 4 -> BlockFace.WEST;
                    case 5 -> BlockFace.EAST;
                    default -> BlockFace.NORTH;
                });
            }
            return data;
        } catch (Throwable ignored) {
            return null;
        }
    }
    public int getPrecipitationHeight(final int x, final int z) {
        for (int y = access.getMaxHeight(); y > 0; --y) {
            final int id = getBlockId(x, y, z);
            final Material material = id == 0 || Block.blocksList[id] == null
                    ? new Material(false, false)
                    : Block.blocksList[id].blockMaterial;
            if (!material.blocksMovement() && !material.isLiquid()) continue;
            return y + 1;
        }
        return 0;
    }

    public int getTopSolidOrLiquidBlock(final int x, final int z) {
        for (int y = access.getMaxHeight(); y > 0; --y) {
            final int id = getBlockId(x, y, z);
            final Block block = id >= 0 && id < Block.blocksList.length ? Block.blocksList[id] : null;
            if (block != null && block.blockMaterial.blocksMovement() && block != Block.leaves) {
                return y + 1;
            }
        }
        return -1;
    }

    public int getHeightValue(final int x, final int z) {
        for (int y = access.getMaxHeight() + 1; y > 0; --y) {
            if (getBlockId(x, y - 1, z) != 0) {
                return y;
            }
        }
        return 0;
    }
    public void spawnEntityInWorld(final EntityVillager villager) {
        this.access.spawnVillager(
                villager.getX(),
                villager.getY(),
                villager.getZ(),
                villager.getProfession()
        );
    }
    public boolean isBlockNormalCube(int x,int y,int z){return Block.opaqueCubeLookup[getBlockId(x,y,z)];}
    public int getSavedLightValue(EnumSkyBlock skyBlock, int x, int y, int z){ return skyBlock == EnumSkyBlock.Sky ? 15 : 0; }
    public BiomeGenBase getBiomeGenForCoords(int x, int z){
        if (manager == null) return BiomeGenBase.plains;
        return manager.getBiomeGenAt(x, z);
    }
    public boolean isBlockFreezable(final int x, final int y, final int z) {
        return canBlockFreeze(x, y, z, false);
    }

    public boolean canBlockFreeze(final int x, final int y, final int z, final boolean natural) {
        final BiomeGenBase biome = getBiomeGenForCoords(x, z);
        if (biome == null || biomeTemperature(biome.biomeID) > 0.15F) return false;
        if (y < access.getMinHeight() || y > access.getMaxHeight()) return false;
        final int id = getBlockId(x, y, z);
        if (getSavedLightValue(EnumSkyBlock.Block, x, y, z) >= 10) return false;
        if (id != Block.waterStill.blockID && id != Block.waterMoving.blockID) return false;
        if (getBlockMetadata(x, y, z) != 0) return false;
        if (!natural) return true;

        return getBlockMaterial(x - 1, y, z) != Material.water
                || getBlockMaterial(x + 1, y, z) != Material.water
                || getBlockMaterial(x, y, z - 1) != Material.water
                || getBlockMaterial(x, y, z + 1) != Material.water;
    }

    public boolean canSnowAt(final int x, final int y, final int z) {
        final BiomeGenBase biome = getBiomeGenForCoords(x, z);
        if (biome == null || biomeTemperature(biome.biomeID) > 0.15F) return false;
        if (y < access.getMinHeight() || y > access.getMaxHeight()) return false;
        if (getSavedLightValue(EnumSkyBlock.Block, x, y, z) >= 10) return false;
        final int below = getBlockId(x, y - 1, z);
        return getBlockId(x, y, z) == Block.air.blockID
                && below != Block.air.blockID
                && below != Block.ice.blockID
                && Block.blocksList[below] != null
                && Block.blocksList[below].blockMaterial.blocksMovement();
    }

    private static float biomeTemperature(final int biomeId) {
        switch (biomeId) {
            case 2:
            case 17: return 2.0F;
            case 3:
            case 20: return 0.2F;
            case 4:
            case 18: return 0.7F;
            case 5:
            case 19: return 0.05F;
            case 6: return 0.8F;
            case 10:
            case 11:
            case 12:
            case 13: return 0.0F;
            case 14:
            case 15: return 0.9F;
            case 16: return 0.8F;
            case 21:
            case 22: return 1.2F;
            default: return 0.8F;
        }
    }
    public int getBlockMetadata(final int x, final int y, final int z) {
        final Integer stored = legacyMetadata.get(blockKey(x, y, z));
        return stored == null ? inferMetadata(x, y, z, getBlockId(x, y, z)) : stored;
    }

    private int inferMetadata(final int x, final int y, final int z, final int id) {
        try {
            final org.bukkit.block.data.BlockData data = access.getBlockData(x, y, z);
            if (id == Block.wood.blockID && data instanceof Orientable orientable) {
                return switch (orientable.getAxis()) {
                    case X -> 4;
                    case Z -> 8;
                    default -> 0;
                };
            }
            if (id == Block.leaves.blockID || id == Block.planks.blockID || id == Block.sapling.blockID) {
                final org.bukkit.Material material = access.getType(x, y, z);
                if (material.name().startsWith("SPRUCE_")) return 1;
                if (material.name().startsWith("BIRCH_")) return 2;
                if (material.name().startsWith("JUNGLE_")) return 3;
            }
            if (id == Block.crops.blockID && data instanceof Ageable ageable) {
                return ageable.getAge();
            }
            if (id == Block.snow.blockID && data instanceof org.bukkit.block.data.type.Snow snow) {
                return Math.max(0, snow.getLayers() - 1);
            }
            if ((id == Block.stairSingle.blockID || id == Block.stairDouble.blockID) && data instanceof Slab slab) {
                final int variant = slabVariant(access.getType(x, y, z));
                if (id == Block.stairDouble.blockID) return variant;
                return variant | (slab.getType() == Slab.Type.TOP ? 8 : 0);
            }
            if ((id == Block.doorWood.blockID || id == Block.doorSteel.blockID)
                    && data instanceof Door door) {
                if (door.getHalf() == Bisected.Half.TOP) {
                    return 8 | (door.getHinge() == Door.Hinge.RIGHT ? 1 : 0);
                }
                int value = switch (door.getFacing()) {
                    case EAST -> 0;
                    case SOUTH -> 1;
                    case WEST -> 2;
                    default -> 3;
                };
                if (door.isOpen()) value |= 4;
                return value;
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    private static int slabVariant(final org.bukkit.Material material) {
        switch (material) {
            case SANDSTONE_SLAB: return 1;
            case OAK_SLAB: return 2;
            case COBBLESTONE_SLAB: return 3;
            case BRICK_SLAB: return 4;
            case STONE_BRICK_SLAB: return 5;
            default: return 0;
        }
    }

    public void notifyBlocksOfNeighborChange(int x,int y,int z,int id){}
    public static final class WorldProvider { public int getAverageGroundLevel(){return 64;} }
}


