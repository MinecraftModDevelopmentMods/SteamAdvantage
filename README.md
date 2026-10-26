# SteamAdvantage

**Minecraft 1.15.2 port, version 2.2.7.115021.** Native compilation and 30 unit/resource
tests pass, including native drill-renderer geometry and matrix-stack checks.
Combined-mod server, client-resource, connected-machine and disposable-copy migration
checks have not yet been repeated for this port. The historical
[1.14 test results](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.14.4/docs/1.14/BROADER-QUALIFICATION.md)
are not evidence of 1.15 gameplay or world-upgrade compatibility.

Steam Advantage adds steam-powered machines and infrastructure to Power Advantage for Minecraft 1.15.2.

Every crafting variant has an ingredient-triggered recipe-book discovery. See the [shared material checklist and discovery tests](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/RECIPE-DISCOVERY.md). Regenerate unlock JSON after recipe changes with `node tools/generate-recipe-unlocks.js`; `check` audits recipe coverage and criteria. Crushing recipes are deliberately excluded from the crafting book.

The historical [manufacturing progression protocol](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/MANUFACTURING-PROGRESSION.md) describes raw-material, processing and loot acquisition paths in every mode, including restart persistence. Earlier results do not establish those outcomes on 1.15.

## Building Minecraft 1.15.2

The `master-1.15.2` build targets Forge 31.2.57, snapshot `20200514-1.15.1` mappings,
and MCP `20200515.085601`. It uses ForgeGradle 7.0.34 and Gradle 9.6.1.
Run Gradle with Temurin 17.0.1+12; production code is compiled with Temurin 8.0.502+7.
The Forge Mavenizer also requires Temurin 25.0.3+9.0.LTS. CI installs all three
explicitly and disables automatic toolchain detection and downloads.

Build the matching 1.15 PowerAdvantage, Base Metals and OreSpawn deobf jars first.
Dependency versions, checksums and the exact Power source reference belong in
`gradle.properties`. Release audits reject missing or placeholder pins; do not
bypass those checks or substitute a dependency from an earlier Minecraft version.
With the standard sibling layout:

```text
cd ..\..\PowerAdvantage
gradlew.bat deobfJar
```

For local development, compile and run tests in SteamAdvantage:

```text
gradlew.bat compileJava test
```

With the exact Power dependency pinned, run the complete release gates:

```text
gradlew.bat clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums verifyEclipseProductionClasspath
```

Alternative layouts may pass `-PpowerAdvantageDeobfJar=<path>`, `-PbaseMetalsDeobfJar=<path>`, and `-PoreSpawnDeobfJar=<path>`. None of those dependencies is embedded.

For Eclipse, import this repository as an existing Gradle project and run:

```text
gradlew.bat cleanEclipse verifyEclipseProductionClasspath
```

Refresh the project in Eclipse, allow its Java build to finish, and launch the generated `runClient.launch`. Eclipse launches discover SteamAdvantage from `bin/main`; command-line Gradle launches use `build/forge-run-mod-classes/main`. The Eclipse audit checks that discovery points at the same production output Eclipse compiles into.

The Eclipse resource root is `build/resources/main`, so its builder copies expanded mod metadata rather than raw source templates. Refresh the Gradle project and clean/rebuild after this setup changes. After resource/version edits, run `processResources` or refresh Gradle before launching; do not put `src/main/resources` back on the Eclipse Java build path.

## Compatibility

The 1.15 source adaptations retain machine callbacks, all six drill directions, native
helper-tile lifecycle, saved progress and target snapshots, energy/inventory NBT, legacy
fluid contracts, musket loaded-state bytes and migration codecs. Native villager professions
replace the removed smith careers (armorer, weaponsmith, toolsmith) and priest (cleric).
Trades retain their legacy level and price ranges; 1.14 restocking and profession selection
follow the native engine.

Automated coverage is bounded to the recorded scenarios. It does not establish every
in-world GUI, rendering, sound, control or musket interaction, multiplayer behavior,
or compatibility with arbitrary third-party mods. Unit/resource tests alone are not
runtime or world-upgrade evidence.

The combined 1.15 harness must be prepared before an all-Advantage play-test. Its launch and resource probe belong to the sibling PowerAdvantage checkout. The [historical resource-probe protocol](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.14.4/test-rig/client-resources/README.md) describes model, texture and name checks; its previous passes do not establish 1.15 rendering or gameplay results.

The implementation namespace is `zone.moddev.mc.steamadvantage`. Runtime identities, registries, tile IDs, saved NBT, recipes, and assets remain stable. Steam registers its legacy state and NBT codecs with PowerAdvantage's shared migration hook so 1.10 and 1.12 worlds can be converted before Minecraft flattens old numeric block states.

The historical [1.13 migration guide](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/MIGRATION.md) explains the legacy conversion and backup process; it is not a 1.15 migration qualification. Always make a whole-world backup before upgrading, keep the complete set of migration backups, and test only disposable copies. Check that every other mod supports the target version; these results do not qualify unrelated mods or an entire existing modpack.
