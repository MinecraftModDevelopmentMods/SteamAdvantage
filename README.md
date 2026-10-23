# SteamAdvantage

Steam Advantage adds steam-powered machines and infrastructure to Power Advantage for Minecraft 1.13.2.

Every crafting variant has an ingredient-triggered recipe-book discovery. See the [shared material checklist and discovery tests](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/RECIPE-DISCOVERY.md). Regenerate unlock JSON after recipe changes with `node tools/generate-recipe-unlocks.js`; `check` audits recipe coverage and criteria. Crushing recipes are deliberately excluded from the crafting book.

The [manufacturing progression audit](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/MANUFACTURING-PROGRESSION.md) checks raw-material, processing and loot acquisition paths in every mode, including restart persistence.

## Building Minecraft 1.13.2

The `master-1.13.2` build uses ForgeGradle 7.0.34 and Gradle 9.6.1. Run Gradle with Java 17; production code is compiled for Java 8.

Build the pinned PowerAdvantage, Base Metals, and OreSpawn deobf jars first.
Exact source commits and checksums are recorded in `gradle.properties`; CI builds the same commits.
With the standard sibling layout:

```text
cd ..\..\PowerAdvantage
gradlew.bat deobfJar
```

Then build SteamAdvantage:

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

For an all-Advantage play-test, run `prepareCombinedEclipseLaunch` in the sibling PowerAdvantage checkout and select its `runClientAllAdvantage.launch`. The [native resource probe](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/test-rig/client-resources/README.md) checks models, textures and names across all three mods. These resource checks do not replace gameplay testing.

The implementation namespace is `zone.moddev.mc.steamadvantage`. Runtime identities, registries, tile IDs, saved NBT, recipes, and assets remain stable. Steam registers its legacy state and NBT codecs with PowerAdvantage's shared migration hook so 1.10 and 1.12 worlds can be converted before Minecraft flattens old numeric block states.

Read PowerAdvantage's [1.13 migration guide](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/MIGRATION.md) before upgrading, and always use a backup. The [qualification notes](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/CURRENT-QUALIFICATION.md) list the tested scenarios and remaining gaps.
