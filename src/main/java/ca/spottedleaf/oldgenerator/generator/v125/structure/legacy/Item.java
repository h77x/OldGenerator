package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;
public final class Item {
    public static final Item[] itemsList=new Item[4096];
    private final int id; private Item(int id){this.id=id;if(id>=0&&id<itemsList.length)itemsList[id]=this;}
    public int getItemStackLimit(){return 64;}
    static { for(int i=0;i<itemsList.length;i++)itemsList[i]=new Item(i); }
}
