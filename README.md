# SopLib

`SopLib` is the shared core library for the `Sop*` plugin ecosystem.

Its main purpose is to keep ordinary plugins simple:
- compile most plugins against `1.16.5`
- keep code on `Java 8`
- move version-specific/runtime compatibility into one place

## What It Provides

- text and color helpers
- item utilities
- item NBT helpers
- custom block visual services
- region helpers
- protection helpers
- reusable database layer
- version/service resolution

## Text formatting

`SopLib` text helpers support legacy color codes, hex colors, and common MiniMessage-style tags.

That now also includes:

- `<center>your text</center>`
- `<center-10>your text</center>`
- `<center+10>your text</center>`

`<center>` is resolved before MiniMessage conversion and is meant for single-line chat text centering.
Signed numeric offsets shift the centered text in pixels:

- `-` moves left
- `+` moves right

## Version Model

Most `Sop*` plugins are built against old stable baselines and rely on `SopLib` for runtime compatibility.

In practice:
- plugin compile target is usually `1.16.5`
- plugin Java target is usually `Java 8`
- actual runtime support depends on the versions supported by `SopLib`

Current built-in version modules:
- `1.16.5`
- `1.20.4`
- `1.21.1`
- `1.21.11`
- `26.1.x`
- `26.2.x`
- `26.3.x`

## Used By

Examples of plugins in the current ecosystem that depend on `SopLib`:

- `SopAFKZone`
- `SopBattlePass`
- `SopChat`
- `SopCrates`
- `SopCurrency`
- `SopCustomBlocks`
- `SopDisplays`
- `SopEmojis`
- `SopHighlight`
- `SopLocales`
- `SopMines`
- `SopMobGuard`
- `SopPillars`
- `SopPrefix`
- `SopPrison`
- `SopPromocodes`
- `SopRegionLimit`

## Build

Typical build:

```bash
mvn -q -DskipTests install
```

`install` is useful because other local `Sop*` plugins often depend on `soplib-core`.

Build artifacts are not stored in the repository.
Use local Maven builds or download published jars from GitHub Releases.

## Diagnostics and verification

`/soplib doctor` (permission `soplib.doctor`, default: operators) reports the
Minecraft/Paper and Java versions, selected adapters, optional dependencies,
and enabled/disabled Sop plugins. It is read-only and works from the console.
`doctor` and `reload` have permission-aware tab completion. A loaded adapter is
not a guarantee that its packet operations have been tested with a player.

Run the regression tests and distributable check from the project directory:

```sh
mvn --batch-mode clean verify
python tools/verify_artifact.py
```

Build the full reactor with JDK 25 or newer. The GitHub workflow runs these
checks on pushes and pull requests and uploads only `shade/target/SopLib.jar`.
A `v*` tag additionally creates a GitHub Release containing that tested JAR.
The workflow must first be committed and pushed; adding it locally does not
activate GitHub Actions. Version changes and tag creation remain explicit.

### Isolated server smoke test

Supply a trusted Paper server JAR and any compatible dependency JARs. Review
the Minecraft EULA before passing the explicit acceptance flag:

```sh
python tools/server_smoke.py --server-jar /path/to/paper.jar --dependency-jar /path/to/NBTAPI.jar --java /path/to/java --accept-eula
```

Each run uses a fresh temporary directory, a loopback-only listener and an
automatically assigned port. It does not touch existing worlds or plugin
configs. Logs and the test world are retained at the printed location for
inspection. The test waits for startup, invokes `soplib doctor`, stops the
server and fails on errors, exceptions, doctor failures or a timeout. Download
and bootstrap time count toward `--timeout` (default: 240 seconds).

This only verifies startup and diagnostics. Corpse rendering, virtual posters,
translated packets and interactions need a separate test with a player.
External NBTAPI/ProtocolLib/WorldGuard versions must support the tested server.

If Java cannot download the Mojang bootstrap JAR, download it separately from
the URL in Paper's `META-INF/download-context` and pass `--bootstrap-jar PATH`.
The script verifies its SHA-256 against Paper's metadata before seeding the
isolated cache. This does not skip any plugin checks or accept the EULA for you.

### Compatibility status

| Minecraft | Adapter exists | Local Maven build | Server smoke | Player test |
| --- | --- | --- | --- | --- |
| 1.16.5 | yes | passed | not run | not run |
| 1.20.4 | yes | passed | not run | not run |
| 1.21.1 | yes | passed | not run | not run |
| 1.21.11 | yes | passed | not run | not run |
| 26.1.x | yes | passed | not run | not run |
| 26.2.x | yes | passed | not run | not run |
| 26.3.x | yes | passed | not run | not run |

The Maven column means the existing adapter modules compile against their
declared dependencies, not that every plugin has been built against every API.
No server compatibility certification is inferred from these build results.
