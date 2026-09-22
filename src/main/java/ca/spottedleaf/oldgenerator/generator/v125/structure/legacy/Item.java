package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;

public final class Item {
    public static final Item[] itemsList = new Item[4096];
    public final int itemID;
    public final int shiftedIndex;
    private Item(final int id) { this.itemID=id; this.shiftedIndex=id; if(id>=0&&id<itemsList.length)itemsList[id]=this; }
    private static Item register(final int id){return new Item(id);}

    public static final Item coal=register(263);
    public static final Item diamond=register(264);
    public static final Item ingotIron=register(265);
    public static final Item ingotGold=register(266);
    public static final Item swordSteel=register(267);
    public static final Item pickaxeSteel=register(257);
    public static final Item bread=register(297);
    public static final Item appleRed=register(260);
    public static final Item appleGold=register(322);
    public static final Item paper=register(339);
    public static final Item book=register(340);
    public static final Item compass=register(345);
    public static final Item map=register(358);
    public static final Item pumpkinSeeds=register(361);
    public static final Item melonSeeds=register(362);
    public static final Item enderPearl=register(368);
    public static final Item redstone=register(331);
    public static final Item dyePowder=register(351);
    public static final Item helmetSteel=register(306);
    public static final Item plateSteel=register(307);
    public static final Item legsSteel=register(308);
    public static final Item bootsSteel=register(309);
    public static final Item saddle=register(329);
    public static final Item wheat=register(296);
    public static final Item gunpowder=register(289);
    public static final Item silk=register(287);
    public static final Item bucketEmpty=register(325);
    public static final Item record13=register(2256);
    public static final Item recordCat=register(2257);

    private Item(){this(-1);}
    public int getItemStackLimit(){return 64;}
}
