package ca.spottedleaf.oldgenerator;

import ca.spottedleaf.oldgenerator.generator.b173.Beta173ChunkGenerator;
import ca.spottedleaf.oldgenerator.generator.b173.listener.SkyGenerationListener;
import ca.spottedleaf.oldgenerator.generator.v125.V125ChunkGenerator;
import ca.spottedleaf.oldgenerator.listener.LegacyPopulateHack;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public final class OldGenerator extends JavaPlugin {

    @Override
    public void onLoad() {}

    protected void setupMetrics() {
        final Metrics metrics = new Metrics(this, 7761);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new LegacyPopulateHack(), this);
        Bukkit.getPluginManager().registerEvents(new SkyGenerationListener(this), this);
        this.setupMetrics();
    }

    @Override
    public void onDisable() {}

    @Override
    public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String id) {
        if (id == null) {
            this.getLogger().warning("Generator id cannot be null. Accepted ids: b173, sb173, v125");
            return null;
        }

        if (id.equalsIgnoreCase("b173")) {
            this.getLogger().info("Registering Beta 1.7.3 overworld generator for '" + worldName + "'");
            return new Beta173ChunkGenerator(false);
        }

        if (id.equalsIgnoreCase("sb173")) {
            this.getLogger().info("Registering Beta 1.7.3 skylands generator for '" + worldName + "'");
            return new Beta173ChunkGenerator(true);
        }

        if (id.equalsIgnoreCase("v125") || id.equalsIgnoreCase("1.2.5")) {
            this.getLogger().info("Registering Minecraft 1.2.5 overworld generator for '" + worldName + "'");
            return new V125ChunkGenerator();
        }

        this.getLogger().warning("Id '" + id + "' is invalid, accepted ids: b173, sb173, v125");
        return null;
    }
}
