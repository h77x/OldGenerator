package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

public final class ItemStack {
    public final int itemID, stackSize, itemDamage;
    public ItemStack(final Item item) { this(item, 1, 0); }
    public ItemStack(final Item item, final int size) { this(item, size, 0); }
    public ItemStack(final Item item, final int size, final int damage) {
        this(item == null ? -1 : item.shiftedIndex, size, damage);
    }
    public ItemStack(final int id, final int size, final int damage) {
        this.itemID = id;
        this.stackSize = size;
        this.itemDamage = damage;
    }
}
