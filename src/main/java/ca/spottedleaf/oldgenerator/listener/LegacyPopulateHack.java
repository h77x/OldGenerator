package ca.spottedleaf.oldgenerator.listener;

import ca.spottedleaf.oldgenerator.OldGenerator;
import ca.spottedleaf.oldgenerator.generator.b173.Beta173ChunkGenerator;
import ca.spottedleaf.oldgenerator.generator.v125.V125ChunkGenerator;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class LegacyPopulateHack implements Listener {
    private static final PersistentDataType<Byte, Boolean> BOOLEAN_TYPE = new PersistentDataType<Byte, Boolean>() {
        private final Byte ZERO=0, ONE=1;
        @Override public Class<Byte> getPrimitiveType(){return Byte.class;}
        @Override public Class<Boolean> getComplexType(){return Boolean.class;}
        @Override public Byte toPrimitive(final Boolean value,final PersistentDataAdapterContext context){return value?ONE:ZERO;}
        @Override public Boolean fromPrimitive(final Byte value,final PersistentDataAdapterContext context){return value.byteValue()==1;}
    };
    private final NamespacedKey POPULATED_KEY=new NamespacedKey(JavaPlugin.getPlugin(OldGenerator.class),"populated");

    private void setHasPopulated(final Chunk chunk,final boolean value){
        chunk.getPersistentDataContainer().set(POPULATED_KEY,BOOLEAN_TYPE,value);
    }
    private boolean hasPopulated(final Chunk chunk){
        return Boolean.TRUE.equals(chunk.getPersistentDataContainer().get(POPULATED_KEY,BOOLEAN_TYPE));
    }
    private boolean hasPopulatedKeySet(final Chunk chunk){
        return chunk.getPersistentDataContainer().get(POPULATED_KEY,BOOLEAN_TYPE)!=null;
    }
    private static Chunk getChunkAtNoLoad(final World world,final int x,final int z){
        return world.isChunkLoaded(x,z)?world.getChunkAt(x,z):null;
    }

    private void runPopulators(final World world,final Chunk chunk,final ChunkGenerator generator){
        if(generator instanceof Beta173ChunkGenerator beta) beta.runPopulators(world,chunk);
        else if(generator instanceof V125ChunkGenerator v125) v125.runPopulators(world,chunk);
        else return;
        this.setHasPopulated(chunk,true);
    }

    @EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
    public void onChunkLoad(final ChunkLoadEvent event){
        final Chunk center=event.getChunk();
        final int cx=center.getX(),cz=center.getZ();
        final World world=event.getWorld();
        final ChunkGenerator generator=world.getGenerator();
        if(!(generator instanceof Beta173ChunkGenerator)&&!(generator instanceof V125ChunkGenerator))return;

        if(event.isNewChunk()) this.setHasPopulated(center,false);
        else if(!this.hasPopulatedKeySet(center)) this.setHasPopulated(center,true);

        final Chunk topRight=getChunkAtNoLoad(world,cx+1,cz+1);
        final Chunk top=getChunkAtNoLoad(world,cx,cz+1);
        final Chunk right=getChunkAtNoLoad(world,cx+1,cz);
        final Chunk left=getChunkAtNoLoad(world,cx-1,cz);
        final Chunk topLeft=getChunkAtNoLoad(world,cx-1,cz+1);
        final Chunk bottom=getChunkAtNoLoad(world,cx,cz-1);
        final Chunk bottomRight=getChunkAtNoLoad(world,cx+1,cz-1);
        final Chunk bottomLeft=getChunkAtNoLoad(world,cx-1,cz-1);

        if(!hasPopulated(center)&&topRight!=null&&top!=null&&right!=null)runPopulators(world,center,generator);
        if(left!=null&&!hasPopulated(left)&&topLeft!=null&&top!=null)runPopulators(world,left,generator);
        if(bottom!=null&&!hasPopulated(bottom)&&bottomRight!=null&&right!=null)runPopulators(world,bottom,generator);
        if(bottomLeft!=null&&!hasPopulated(bottomLeft)&&bottom!=null&&left!=null)runPopulators(world,bottomLeft,generator);
    }
}
