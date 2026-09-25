OldGenerator
==

A standalone Paper plugin that reproduces the **Minecraft 1.2.5 Overworld world generator** on modern servers.

The plugin does not run an old Minecraft server jar. The generator is implemented directly from the historical 1.2.5 generation algorithms and adapted to the modern Paper/Bukkit generation API.

## Generator

| ID | Target |
| --- | --- |
| `1.2.5` | Minecraft 1.2.5 Overworld |

This is the only supported generator ID. Older experimental generator modes and non-Overworld targets are no longer part of this project.

### Minecraft 1.2.5 implementation

The current implementation includes:

- 128-block historical world height (Y 0–127).
- Source-faithful 1.2.5 Java `Random` seeding and Perlin/octave noise.
- 5×17×5 density field with the original 4×4×8 interpolation.
- Historical sea level at Y=63 and 1.2.5 biome terrain parameters.
- The 1.2.5 GenLayer chain, RiverMix, and final Voronoi biome selection.
- 1.2.5 caves and ravines.
- Historical chunk population seeding and decorator ordering.
- Source-derived 1.2.5 population generators for ores, sand/clay, lakes, dungeons, trees, flowers, grass, mushrooms, reeds, pumpkins, cacti, liquid springs, desert wells, and cold-biome ice/snow.
- Source-derived 1.2.5 mineshaft, village, and stronghold structure components and placement rules.
- Legacy block IDs, metadata, loot, chests, spawners, and structure state bridged into modern Paper/Bukkit block data and tile state.
- Modern Paper 26.2 `ChunkGenerator.ChunkData` integration.
- A registered `BiomeProvider` backed by the same GenLayer data used during chunk generation.
- Paper `StructuresLocateEvent` integration for legacy mineshaft, village, and stronghold searches.
- Population through Paper's `BlockPopulator` lifecycle using a `LimitedRegion` bridge.

Recent correctness fixes include the vanilla 1.2.5 GenLayer seed LCG recurrence and Perlin noise corner lookups. The project also preserves the historical height semantics used by vegetation and structure generation.

The implementation is **development/experimental** until it has been runtime-differentially tested against a trusted Minecraft 1.2.5 reference. The source-level structure and terrain algorithms are substantially ported, but source matching alone is not a claim of byte-for-byte or chunk-for-chunk output parity.

## Usage

Configure the world to use:

```yaml
worlds:
  oldworld:
    generator: OldGenerator:1.2.5
```

The current build targets Paper 26.2 and runs on the Java runtime required by that server generation.

## Development

Build with:

```bash
mvn -B -ntp clean verify
```

GitHub Actions runs the Maven verification build on Java 25; the plugin targets Java 21 bytecode for Maven Shade compatibility.

## Validation

The repository build is continuously verified by GitHub Actions. Runtime output parity still requires comparison against a trusted Minecraft 1.2.5 reference world; the implementation does not claim byte-for-byte parity until that differential test is completed.

## Source and licensing

The historical generator implementation targets the Minecraft 1.2.5 server generation source. The original Minecraft code is copyrighted by Mojang AB. The plugin's non-Minecraft portions remain under the MIT license in `LICENSE.md`.
