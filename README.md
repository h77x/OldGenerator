OldGenerator
==

Spigot/Paper plugin for running historical Minecraft world-generation algorithms on modern servers.

## Generators

| ID | Target |
| --- | --- |
| `b173` | Minecraft Beta 1.7.3 overworld |
| `sb173` | Minecraft Beta 1.7.3 skylands |
| `v125` | Minecraft 1.2.5 overworld |
| `1.2.5` | Alias for `v125` |

The 1.2.5 port is a standalone implementation of the historical generator. It does not
run an old Minecraft server jar.

### Minecraft 1.2.5 port status

The `mc-1.2.5-port` branch now contains the 1.2.5 terrain and population foundation:

- 128-block historical world-height model (Y 0–127).
- Source-faithful 1.2.5 Perlin/octave noise construction and Java-Random seeding.
- 5x17x5 density field with the original 4x4x8 interpolation.
- Historical sea level at Y=63 and the 1.2.5 biome-weighted density calculation.
- 1.2.5 GenLayer chain, biome IDs, RiverMix and final Voronoi block-biome layer.
- 1.2.5 caves and ravines.
- Historical chunk population seed (`oddX`/`oddZ`) and decorator ordering.
- Ores, sand/clay patches, lakes, dungeons, trees, flowers, grass, mushrooms,
  reeds, pumpkins, cacti, liquid springs, and cold-biome ice/snow handling.
- Deterministic 1.2.5-style mineshaft, village and stronghold start placement.
- Modern Bukkit/Paper `ChunkGenerator.ChunkData` integration and a legacy
  population hook for post-generation decoration.

Structure **piece layouts are not yet byte-for-byte 1.2.5 ports**. The current
structure bridge reproduces the legacy placement rules and deterministic locations,
then uses compact modern Bukkit implementations for the generated pieces. Full
component-level parity for mineshafts, villages, and strongholds remains the final
structure-generation milestone.

The 1.2.5 generator is still **development/experimental** and should be validated
against reference worlds before being treated as a finished parity implementation.

## How do I use?

Add the generator to your world configuration:

```yaml
worlds:
  oldworld:
    generator: OldGenerator:v125
```

The `1.2.5` alias may also be used:

```yaml
worlds:
  oldworld:
    generator: OldGenerator:1.2.5
```

Existing Beta 1.7.3 configurations remain supported:

```yaml
worlds:
  beta:
    generator: OldGenerator:b173
  skylands:
    generator: OldGenerator:sb173
```

## Development

Build with:

```bash
mvn -B -ntp clean verify
```

The GitHub Actions build runs the Maven verification build on Java 17.

## Source and licensing

The historical generator implementation targets the Minecraft 1.2.5 server
generation source. The original Minecraft code is copyrighted by Mojang AB. The
plugin's non-Minecraft portions remain under the MIT license in `LICENSE.md`.
