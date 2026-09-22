package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;
public final class TileEntityChest {
    private final ItemStack[] items=new ItemStack[27];
    public int getSizeInventory(){return items.length;}
    public void setInventorySlotContents(int slot,ItemStack item){if(slot>=0&&slot<items.length)items[slot]=item;}
}
