package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

import org.bukkit.Material;

public final class Block {
    public final int blockID;
    public final ca.spottedleaf.oldgenerator.generator.v125.structure.legacy.Material blockMaterial;
    public final Material material;

    public static final Block[] blocksList = new Block[256];
    public static final boolean[] opaqueCubeLookup = new boolean[256];

    private Block(final int id, final Material material, final boolean solid, final boolean liquid) {
        this.blockID = id;
        this.material = material;
        this.blockMaterial = liquid ? (material == Material.WATER ? ca.spottedleaf.oldgenerator.generator.v125.structure.legacy.Material.water : ca.spottedleaf.oldgenerator.generator.v125.structure.legacy.Material.lava) : new ca.spottedleaf.oldgenerator.generator.v125.structure.legacy.Material(solid, false);
        if (id >= 0 && id < blocksList.length) {
            blocksList[id] = this;
            opaqueCubeLookup[id] = solid;
        }
    }

    private static Block b(final int id, final Material m) {
        final boolean liquid = m == Material.WATER || m == Material.LAVA;
        final boolean solid = m != Material.AIR && m != Material.WATER && m != Material.LAVA
                && m != Material.CAVE_AIR && !m.isTransparent();
        return new Block(id, m, solid, liquid);
    }

    public static final Block air = b(0, Material.AIR);
    public static final Block stone = b(1, Material.STONE);
    public static final Block grass = b(2, Material.GRASS_BLOCK);
    public static final Block dirt = b(3, Material.DIRT);
    public static final Block cobblestone = b(4, Material.COBBLESTONE);
    public static final Block planks = b(5, Material.OAK_PLANKS);
    public static final Block sapling = b(6, Material.OAK_SAPLING);
    public static final Block bedrock = b(7, Material.BEDROCK);
    public static final Block waterMoving = b(8, Material.WATER);
    public static final Block waterStill = b(9, Material.WATER);
    public static final Block lavaMoving = b(10, Material.LAVA);
    public static final Block lavaStill = b(11, Material.LAVA);
    public static final Block sand = b(12, Material.SAND);
    public static final Block gravel = b(13, Material.GRAVEL);
    public static final Block oreGold = b(14, Material.GOLD_ORE);
    public static final Block oreIron = b(15, Material.IRON_ORE);
    public static final Block oreCoal = b(16, Material.COAL_ORE);
    public static final Block wood = b(17, Material.OAK_LOG);
    public static final Block leaves = b(18, Material.OAK_LEAVES);
    public static final Block sponge = b(19, Material.SPONGE);
    public static final Block glass = b(20, Material.GLASS);
    public static final Block oreLapis = b(21, Material.LAPIS_ORE);
    public static final Block blockLapis = b(22, Material.LAPIS_BLOCK);
    public static final Block sandStone = b(24, Material.SANDSTONE);
    public static final Block bed = b(26, Material.RED_BED);
    public static final Block cloth = b(35, Material.WHITE_WOOL);
    public static final Block stairCompactPlanks = b(53, Material.OAK_STAIRS);
    public static final Block chest = b(54, Material.CHEST);
    public static final Block stoneOvenIdle = b(61, Material.FURNACE);
    public static final Block stoneOvenActive = b(62, Material.FURNACE);
    public static final Block doorWood = b(64, Material.OAK_DOOR);
    public static final Block ladder = b(65, Material.LADDER);
    public static final Block rail = b(66, Material.RAIL);
    public static final Block stairCompactCobblestone = b(67, Material.COBBLESTONE_STAIRS);
    public static final Block doorSteel = b(71, Material.IRON_DOOR);
    public static final Block torchWood = b(50, Material.TORCH);
    public static final Block mobSpawner = b(52, Material.SPAWNER);
    public static final Block button = b(77, Material.STONE_BUTTON);
    public static final Block web = b(30, Material.COBWEB);
    public static final Block crops = b(59, Material.WHEAT);
    public static final Block tilledField = b(60, Material.FARMLAND);
    public static final Block fence = b(85, Material.OAK_FENCE);
    public static final Block fenceIron = b(101, Material.IRON_BARS);
    public static final Block thinGlass = b(102, Material.GLASS_PANE);
    public static final Block workbench = b(58, Material.CRAFTING_TABLE);
    public static final Block pressurePlatePlanks = b(72, Material.OAK_PRESSURE_PLATE);
    public static final Block bookShelf = b(47, Material.BOOKSHELF);
    public static final Block obsidian = b(49, Material.OBSIDIAN);
    public static final Block stairDouble = b(43, Material.STONE_SLAB);
    public static final Block stairSingle = b(44, Material.STONE_SLAB);
    public static final Block stairsNetherBrick = b(114, Material.NETHER_BRICK_STAIRS);
    public static final Block stairsStoneBrickSmooth = b(109, Material.STONE_BRICK_STAIRS);
    public static final Block stoneBrick = b(98, Material.STONE_BRICKS);
    public static final Block cobblestoneMossy = b(48, Material.MOSSY_COBBLESTONE);
    public static final Block blockClay = b(82, Material.CLAY);
    public static final Block mushroomCapBrown = b(99, Material.BROWN_MUSHROOM_BLOCK);
    public static final Block endPortalFrame = b(120, Material.END_PORTAL_FRAME);
    public static final Block silverfish = b(97, Material.INFESTED_STONE);
    public static final Block waterlily = b(111, Material.LILY_PAD);
    public static final Block reed = b(83, Material.SUGAR_CANE);
    public static final Block deadBush = b(32, Material.DEAD_BUSH);
    public static final Block pumpkin = b(86, Material.PUMPKIN);
    public static final Block cactus = b(81, Material.CACTUS);
    public static final Block fenceGate = b(107, Material.OAK_FENCE_GATE);
    public static final Block plantYellow = b(37, Material.DANDELION);
    public static final Block plantRed = b(38, Material.POPPY);
    public static final Block mushroomBrown = b(39, Material.BROWN_MUSHROOM);
    public static final Block mushroomRed = b(40, Material.RED_MUSHROOM);
    public static final Block mycelium = b(110, Material.MYCELIUM);
    public static final Block vine = b(106, Material.VINE);

    public boolean canPlaceBlockAt(final World world, final int x, final int y, final int z) {
        return world.isAirBlock(x, y, z);
    }
    public boolean isLeaves(final World world, final int x, final int y, final int z) {
        return this == leaves;
    }
    public boolean canBeReplacedByLeaves(final World world, final int x, final int y, final int z) {
        return this == air || this == leaves || this == vine || this == waterMoving || this == waterStill;
    }
    public boolean isWood(final World world, final int x, final int y, final int z) {
        return this == wood || this == planks;
    }
    public boolean canPlaceBlockOnSide(final World world, final int x, final int y, final int z, final int side) {
        final int id;
        switch (side) {
            case 1: id = world.getBlockId(x, y + 1, z); break;
            case 2: id = world.getBlockId(x, y, z + 1); break;
            case 3: id = world.getBlockId(x, y, z - 1); break;
            case 4: id = world.getBlockId(x + 1, y, z); break;
            case 5: id = world.getBlockId(x - 1, y, z); break;
            default: return false;
        }
        return id != 0 && id < blocksList.length && blocksList[id] != null
                && blocksList[id].blockMaterial.isSolid();
    }

}
