# SteamAdvantage

Steam Advantage adds steam-powered machines and infrastructure to Power Advantage for Minecraft 1.10.2.

The maintained implementation namespace is `zone.moddev.mc.steamadvantage`. This coordinated release
requires Power Advantage `2.4.3.110021` or newer; update the two mods together. Runtime registry,
tile-entity, asset, configuration, and saved-world identities are unchanged.

The cross-mod [Advantage Works 1.10.2 Handbook](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.10.2/docs/1.10/README.md) includes the Steam Engineer's Handbook, rated figures, commissioning layouts, and persistence tests. The [MMD material and fluid compatibility specification](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.10.2/docs/1.10/mmd-material-fluid-compatibility.md) defines crude-oil aliases and OreSpawn ownership.

## Drill Reload Fix

Version `2.2.5.110021` keeps unfinished drill work when a chunk or world reloads.
Existing inventories, energy, orientation and saved-world names are unchanged.
The regression tests cover resumed progress and full server restarts.

## Building Minecraft 1.10.2

The `master-1.10.2` build uses ForgeGradle 7.0.34 and Gradle 9.6.1. Run Gradle with Java 17; Gradle resolves the Java 8 toolchain used for compilation.

Build the pinned OreSpawn commit `5a50df1158e948c8db55814e819090b34c12d765` first. Then build the pinned sibling PowerAdvantage development jar:

```text
cd ..\..\PowerAdvantage
gradlew.bat deobfJar
```

The dependency locations can be overridden with `-PoreSpawnDeobfJar=<path>` and `-PpowerAdvantageDeobfJar=<path>`.

Then build and audit SteamAdvantage:

```text
gradlew.bat clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums
```

Release jars are written to `build/libs`. The deobfuscated development jar is written to `build/libs-dev` by `deobfJar` or `build`.

For Eclipse, import this repository as an existing Gradle project and run:

```text
gradlew.bat cleanEclipse verifyEclipseProductionClasspath
```

Power Advantage `2.4.3.110021` or newer is required at compile time and runtime but is never bundled. OreSpawn is required transitively by Power Advantage. Base Metals remains a required distribution dependency while its integrations stay reflection-based and absent from the compile classpath.
