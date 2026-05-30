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
