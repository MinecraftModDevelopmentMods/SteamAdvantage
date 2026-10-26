# SteamAdvantage

**Minecraft 1.14.4 port, version 2.2.6.114041.** Native compilation and 27 unit/resource tests pass.
The combined-mod automated checks also cover client resources, connected machinery,
drill movement, chunk lifecycle, restart persistence and disposable-copy migration.
The [1.14 test results](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.14.4/docs/1.14/BROADER-QUALIFICATION.md)
describe the tested scenarios and remaining limits, not a guarantee for an entire modpack.

Steam Advantage adds steam-powered machines and infrastructure to Power Advantage for Minecraft 1.14.4.

Every crafting variant has an ingredient-triggered recipe-book discovery. See the [shared material checklist and discovery tests](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/RECIPE-DISCOVERY.md). Regenerate unlock JSON after recipe changes with `node tools/generate-recipe-unlocks.js`; `check` audits recipe coverage and criteria. Crushing recipes are deliberately excluded from the crafting book.

The historical [manufacturing progression protocol](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/MANUFACTURING-PROGRESSION.md) describes raw-material, processing and loot acquisition paths in every mode, including restart persistence. See the 1.14 test results above for this port's coverage.

## Building Minecraft 1.14.4

The `master-1.14.4` build targets Forge 28.2.26, snapshot `20190719-1.14.3` mappings,
and MCP `20190829.143755`. It uses ForgeGradle 7.0.34 and Gradle 9.6.1.
Run Gradle with Java 17; production code is compiled for Java 8.

Build the matching 1.14 PowerAdvantage, Base Metals and OreSpawn deobf jars first.
Dependency versions, checksums and the exact Power source reference belong in
`gradle.properties`. Release audits reject missing or placeholder pins; do not
bypass those checks or substitute a 1.13 dependency.
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

The 1.14 source adaptations preserve machine callbacks, all six drill directions, native
helper-tile lifecycle, saved progress and target snapshots, energy/inventory NBT, legacy
fluid contracts, musket loaded-state bytes and migration codecs. Native villager professions
replace the removed smith careers (armorer, weaponsmith, toolsmith) and priest (cleric).
Trades retain their legacy level and price ranges; 1.14 restocking and profession selection
follow the native engine.

Automated coverage is bounded to the recorded scenarios. It does not establish every
in-world GUI, rendering, sound, control or musket interaction, multiplayer behavior,
or compatibility with arbitrary third-party mods. Unit/resource tests alone are not
runtime or world-upgrade evidence.

For an all-Advantage play-test, run `prepareCombinedEclipseLaunch` in the sibling PowerAdvantage checkout and select its `runClientAllAdvantage.launch`. The [native resource probe](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.14.4/test-rig/client-resources/README.md) checks models, textures and names across all three mods. These resource checks do not replace gameplay testing.

The implementation namespace is `zone.moddev.mc.steamadvantage`. Runtime identities, registries, tile IDs, saved NBT, recipes, and assets remain stable. Steam registers its legacy state and NBT codecs with PowerAdvantage's shared migration hook so 1.10 and 1.12 worlds can be converted before Minecraft flattens old numeric block states.

The historical [1.13 migration guide](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/MIGRATION.md) explains the legacy conversion and backup process; the 1.14 test results above describe this port's coverage. Always make a whole-world backup before upgrading, keep the complete set of migration backups, and test only disposable copies. Check that every other mod supports the target version; these results do not qualify unrelated mods or an entire existing modpack.
