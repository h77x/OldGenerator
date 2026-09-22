package ca.spottedleaf.oldgenerator.listener;

import ca.spottedleaf.oldgenerator.OldGenerator;
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

public final class V125PopulateListener implements Listener {
    private static final PersistentDataType<Byte, Boolean> BOOLEAN_TYPE = new PersistentDataType<Byte, Boolean>() {
        private final Byte ZERO = 0;
        private final Byte ONE = 1;

        @Override
        public Class<Byte> getPrimitiveType() {
            return Byte.class;
        }

        @Override
        public Class<Boolean> getComplexType() {
            return Boolean.class;
        }

        @Override
        public Byte toPrimitive(final Boolean value, final PersistentDataAdapterContext context) {
            return value ? ONE : ZERO;
        }

        @Override
        public Boolean fromPrimitive(final Byte value, final PersistentDataAdapterContext context) {
            return value.byteValue() == 1;
        }
    };

    private final NamespacedKey populatedKey =
            new NamespacedKey(JavaPlugin.getPlugin(OldGenerator.class), "v125_populated");

    private void setHasPopulated(final Chunk chunk, final boolean value) {
        chunk.getPersistentDataContainer().set(this.populatedKey, BOOLEAN_TYPE, value);
    }

    private boolean hasPopulated(final Chunk chunk) {
        return Boolean.TRUE.equals(
                chunk.getPersistentDataContainer().get(this.populatedKey, BOOLEAN_TYPE)
        );
    }

    private boolean hasPopulatedKey(final Chunk chunk) {
        return chunk.getPersistentDataContainer().get(this.populatedKey, BOOLEAN_TYPE) != null;
    }

    private static Chunk getChunkAtNoLoad(final World world, final int x, final int z) {
        return world.isChunkLoaded(x, z) ? world.getChunkAt(x, z) : null;
    }

    private void runPopulators(final World world, final Chunk chunk, final V125ChunkGenerator generator) {
        generator.runPopulators(world, chunk);
        this.setHasPopulated(chunk, true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChunkLoad(final ChunkLoadEvent event) {
        final Chunk center = event.getChunk();
        final int cx = center.getX();
        final int cz = center.getZ();
        final World world = event.getWorld();
        final ChunkGenerator generator = world.getGenerator();

        if (!(generator instanceof V125ChunkGenerator v125)) {
            return;
        }

        if (event.isNewChunk()) {
            this.setHasPopulated(center, false);
        } else if (!this.hasPopulatedKey(center)) {
            this.setHasPopulated(center, true);
        }

        final Chunk topRight = getChunkAtNoLoad(world, cx + 1, cz + 1);
        final Chunk top = getChunkAtNoLoad(world, cx, cz + 1);
        final Chunk right = getChunkAtNoLoad(world, cx + 1, cz);
        final Chunk left = getChunkAtNoLoad(world, cx - 1, cz);
        final Chunk topLeft = getChunkAtNoLoad(world, cx - 1, cz + 1);
        final Chunk bottom = getChunkAtNoLoad(world, cx, cz - 1);
        final Chunk bottomRight = getChunkAtNoLoad(world, cx + 1, cz - 1);
        final Chunk bottomLeft = getChunkAtNoLoad(world, cx - 1, cz - 1);

        if (!hasPopulated(center) && topRight != null && top != null && right != null) {
            this.runPopulators(world, center, v125);
        }
        if (left != null && !hasPopulated(left) && topLeft != null && top != null) {
            this.runPopulators(world, left, v125);
        }
        if (bottom != null && !hasPopulated(bottom) && bottomRight != null && right != null) {
            this.runPopulators(world, bottom, v125);
        }
        if (bottomLeft != null && !hasPopulated(bottomLeft) && bottom != null && left != null) {
            this.runPopulators(world, bottomLeft, v125);
        }
    }
}
