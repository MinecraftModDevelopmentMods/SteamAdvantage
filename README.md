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

The [stronger regression-test backport](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/TEST-COVERAGE-BACKPORT.md)
records measured machine work, restart checks, repaired windows and remaining limits.

For an all-Advantage play-test, run `prepareCombinedEclipseLaunch` in the sibling PowerAdvantage checkout and select its `runClientAllAdvantage.launch`. The [native resource probe](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/test-rig/client-resources/README.md) checks models, textures and names across all three mods. These resource checks do not replace gameplay testing.

The implementation namespace is `zone.moddev.mc.steamadvantage`. Runtime identities, registries, tile IDs, saved NBT, recipes, and assets remain stable. Steam registers its legacy state and NBT codecs with PowerAdvantage's shared migration hook so 1.10 and 1.12 worlds can be converted before Minecraft flattens old numeric block states.

Read PowerAdvantage's [1.13 migration guide](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/MIGRATION.md) before upgrading, and always use a backup. The [qualification notes](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/CURRENT-QUALIFICATION.md) list the tested scenarios and remaining gaps.

## Generator Follow-Up

The [generator follow-up](../../PowerAdvantage/docs/1.13/TEST-COVERAGE-BACKPORT.md#generator-follow-up) records the older `pa113-candidate-g` inputs.
Its 126 operating jobs and 126 cold checks, 39 GUI windows, 351 caption cases,
355 rejected broken controls, eight frozen routes, five connected routes and
paired 30-minute runs passed. This is a pre-final Steam-control checkpoint,
not qualification of the newer SteamAdvantage jars or harness. Those still need
the final sweep. Manual play-testing and release review remain separate.

## Final T Candidate

The [final-t checkpoint](../../PowerAdvantage/docs/1.13/TEST-COVERAGE-BACKPORT.md#final-t-candidate)
records profile `pa113-candidate-i` and its exact jar hashes. All 132 operating
jobs and 132 cold-restart comparisons pass, including an independent audit of
six Steam controls and their nine tile-hold records. GUI checks pass for 39
windows, 351 caption cases and 355 rejected broken controls.
Frozen migration passed on 8 routes and connected-source operation passed on
all five declared routes.
Fresh and migrated-copy 30-minute runs pass. Power and Electric retain their
exact-hash release and reproducibility proof; final-t Steam passes its full suite
and two independent export builds. All 18 mod/version release and reproducibility
gates pass across the six maintained Minecraft lines. Both requested broad
profiles and their independent audit pass; the reporter-only unit/replay
qualification preserves the original native proof. Survival harvest checks pass
before and after restart. The report's extra network/drill scopes are now
qualified; manual acceptance remains separate. Earlier checkpoints keep their original scope;
nothing has been published by this local test work.
