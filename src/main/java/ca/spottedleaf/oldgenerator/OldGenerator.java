package ca.spottedleaf.oldgenerator;

import ca.spottedleaf.oldgenerator.generator.v125.V125ChunkGenerator;
import ca.spottedleaf.oldgenerator.generator.v125.structure.V125StructureLocateListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public final class OldGenerator extends JavaPlugin {

    @Override
    public void onLoad() {
    }

    protected void setupMetrics() {
        new Metrics(this, 7761);
    }

    @Override
    public void onEnable() {
        this.setupMetrics();
        this.getServer().getPluginManager().registerEvents(new V125StructureLocateListener(), this);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String id) {
        if (id == null || id.isBlank()) {
            this.getLogger().warning("Generator id must be '1.2.5'");
            return null;
        }

        if (id.equalsIgnoreCase("1.2.5")) {
            this.getLogger().info("Registering Minecraft 1.2.5 overworld generator for '" + worldName + "'");
            return new V125ChunkGenerator();
        }

        this.getLogger().warning("Generator id '" + id + "' is invalid. This plugin only supports Minecraft 1.2.5.");
        return null;
    }
}
