package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

public final class TileEntityChest {
    private final ItemStack[] items = new ItemStack[27];
    private final World world;
    private final int x, y, z;

    public TileEntityChest(final World world, final int x, final int y, final int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getSizeInventory() { return items.length; }

    public void setInventorySlotContents(final int slot, final ItemStack item) {
        if (slot >= 0 && slot < items.length) {
            items[slot] = item;
            world.syncChest(x, y, z, items);
        }
    }

    public ItemStack getStackInSlot(final int slot) {
        return slot >= 0 && slot < items.length ? items[slot] : null;
    }
}
