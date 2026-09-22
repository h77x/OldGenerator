package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

import ca.spottedleaf.oldgenerator.generator.v125.V125BiomeSource;
import ca.spottedleaf.oldgenerator.world.BlockAccess;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Gate;

public final class World {
    public final WorldProvider worldProvider=new WorldProvider();
    public boolean editingBlocks;
    public boolean scheduledUpdatesAreImmediate;
    private final BlockAccess access;
    private final long seed;
    private final WorldChunkManager manager;

    public World(final long seed, final BlockAccess access, final V125BiomeSource source){
        this.seed=seed;this.access=access;this.manager=new WorldChunkManager(source,seed);
    }
    public World(final BlockAccess access){ this.seed=0L; this.access=access; this.manager=null; }
    public long getSeed(){return seed;}
    public WorldChunkManager getWorldChunkManager(){return manager;}
    public int getBlockId(int x,int y,int z){
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
    public void setBlockTileEntity(int x,int y,int z,Object tile){}
    public Object getBlockTileEntity(int x,int y,int z){
        int id=getBlockId(x,y,z);
        if(id==Block.chest.blockID)return new TileEntityChest();
        if(id==Block.mobSpawner.blockID)return new TileEntityMobSpawner();
        return null;
    }
    private void setInternal(final int x, final int y, final int z, final int id, final int meta) {
        if (y < access.getMinHeight() || y > access.getMaxHeight()) return;
        final Block block = (id >= 0 && id < Block.blocksList.length) ? Block.blocksList[id] : null;
        if (block == null) {
            access.setType(x, y, z, org.bukkit.Material.AIR, false);
            return;
        }

        final org.bukkit.Material material = modernMaterial(id, meta, block.material);
        final BlockData data = createBlockData(id, meta, material);
        if (data != null) {
            access.setBlockData(x, y, z, data, false);
        } else {
            access.setType(x, y, z, material, false);
        }
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
            return (meta & 1) == 1 ? org.bukkit.Material.FERN : org.bukkit.Material.SHORT_GRASS;
        }
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
    public int getTopSolidOrLiquidBlock(int x,int z){return access.getHighestBlockYAt(x,z);}
    public int getHeightValue(int x,int z){return access.getHighestBlockYAt(x,z);}
    public void spawnEntityInWorld(EntityVillager v){}
    public boolean isBlockNormalCube(int x,int y,int z){return Block.opaqueCubeLookup[getBlockId(x,y,z)];}
    public int getSavedLightValue(EnumSkyBlock skyBlock, int x, int y, int z){ return skyBlock == EnumSkyBlock.Sky ? 15 : 0; }
    public BiomeGenBase getBiomeGenForCoords(int x, int z){
        if (manager == null) return BiomeGenBase.plains;
        return manager.getBiomeGenAt(x, z);
    }
    public boolean isBlockFreezable(int x, int y, int z){
        final Material m = getBlockMaterial(x,y,z);
        return m == Material.water;
    }
    public void notifyBlocksOfNeighborChange(int x,int y,int z,int id){}
    public static final class WorldProvider { public int getAverageGroundLevel(){return 64;} }
}
