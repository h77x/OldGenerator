OldGenerator
==

A standalone Paper plugin that reproduces the **Minecraft 1.2.5 Overworld world generator** on modern servers.

The plugin does not run an old Minecraft server jar. The generator is implemented directly against the historical 1.2.5 algorithms and adapted to the modern Paper/Bukkit generation API.

## Generator

| ID | Target |
| --- | --- |
| `1.2.5` | Minecraft 1.2.5 Overworld |

This is intentionally the only supported generator ID. Beta 1.7.3, Beta Skylands, Nether, and other historical generator modes have been removed from the plugin.

### Minecraft 1.2.5 implementation

The current implementation includes:

- 128-block historical world height (Y 0–127).
- Source-faithful 1.2.5 Perlin/octave noise construction and Java `Random` seeding.
- 5×17×5 density field with the original 4×4×8 interpolation.
- Historical sea level at Y=63 and biome-weighted terrain density.
- 1.2.5 GenLayer biome generation, RiverMix, and final Voronoi block-biome selection.
- 1.2.5 caves and ravines.
- Historical chunk population seed behavior and decorator ordering.
- Ores, sand/clay patches, lakes, dungeons, trees, flowers, grass, mushrooms,
  reeds, pumpkins, cacti, liquid springs, and cold-biome ice/snow handling.
- Deterministic 1.2.5-style mineshaft, village, and stronghold start placement.
- Modern Paper `ChunkGenerator.ChunkData` integration and a dedicated 1.2.5 population listener.

Structure **piece layouts are not yet byte-for-byte 1.2.5 ports**. The current structure bridge reproduces the historical placement rules and deterministic locations, then uses compact modern Bukkit implementations for the generated pieces. Full component-level parity for mineshafts, villages, and strongholds remains the final structure-generation milestone.

The 1.2.5 generator is still **development/experimental** and should be validated against reference worlds before being treated as a finished parity implementation.

## Usage

Configure the world to use:

```yaml
worlds:
  oldworld:
    generator: OldGenerator:1.2.5
```

## Development

Build with:

```bash
mvn -B -ntp clean verify
```

GitHub Actions runs the Maven verification build on Java 17.

## Source and licensing

The historical generator implementation targets the Minecraft 1.2.5 server generation source. The original Minecraft code is copyrighted by Mojang AB. The plugin's non-Minecraft portions remain under the MIT license in `LICENSE.md`.
