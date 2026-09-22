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

The 1.2.5 port is being developed as a standalone implementation so the historical
algorithm does not depend on modern server internals.

### Minecraft 1.2.5 port status

The current `mc-1.2.5-port` branch contains the first terrain-generation foundation:

- 256-block historical world-height model.
- 4x4x8 terrain interpolation grid (5x33 density samples).
- standalone deterministic Perlin/octave noise implementation.
- historical sea level of Y=63.
- stone/water terrain shaping.
- basic grass/dirt surface pass.
- modern Bukkit/Paper `ChunkGenerator.ChunkData` output.
- generator selection through `OldGenerator:v125`.

The port is intentionally being built in stages. Biome GenLayer parity, caves/ravines,
structures, ores, vegetation, lakes, and full population are still to be ported and
validated against the 1.2.5 source.

The 1.2.5 generator must therefore currently be considered **development/experimental**;
use `b173` for the existing stable historical generator.

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

The historical generator implementations are based on decompiled Minecraft server
sources. The original Minecraft code is copyrighted by Mojang AB. The plugin's
non-Minecraft portions remain under the MIT license in `LICENSE.md`.

For the 1.2.5 port, the target source is the Minecraft 1.2.5 generation implementation,
including its terrain interpolation and GenLayer/biome system.
