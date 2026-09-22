package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

public final class ItemDoor {
    public static void placeDoorBlock(World world, int x, int y, int z, int meta, Block block) {
        world.setBlockAndMetadata(x, y, z, block.blockID, meta);
        world.setBlockAndMetadata(x, y + 1, z, block.blockID, 8 | (meta & 1));
    }

    public static void placeDoorBlock(World world, int x, int y, int z, EnumDoor door) {
        placeDoorBlock(world, x, y, z, 0, door == EnumDoor.IRON_DOOR ? Block.doorSteel : Block.doorWood);
    }
}
