package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;
import java.util.Random;
public abstract class StructurePieceBlockSelector {
    protected int selectedBlockId; protected int selectedBlockMetaData;
    public abstract void selectBlocks(Random r,int x,int y,int z,boolean edge);
    public int getSelectedBlockId(){return selectedBlockId;}
    public int getSelectedBlockMetaData(){return selectedBlockMetaData;}
}
