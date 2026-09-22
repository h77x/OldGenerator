package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;
public class StructurePieceTreasure extends WeightedRandomChoice {
    public int itemID,itemMetadata,minItemStack,maxItemStack;
    public StructurePieceTreasure(int id,int meta,int min,int max,int weight){super(weight);itemID=id;itemMetadata=meta;minItemStack=min;maxItemStack=max;}
}
