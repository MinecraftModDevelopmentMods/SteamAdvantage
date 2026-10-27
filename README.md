# SteamAdvantage

**Minecraft 1.16.5 port candidate, version 2.2.8.116051.**
This branch is local work pending full verification and manual acceptance.
No 1.16 gameplay, rendering, or world-upgrade
qualification is claimed. Accepted 1.15 results do not qualify this candidate.

Steam Advantage adds steam-powered machines and infrastructure to Power Advantage.

## Build Target

The `master-1.16.5` target uses Minecraft 1.16.5, Forge 36.2.34,
official 1.16.5 mappings, and MCP config 20210115.111550.
ForgeGradle 7.0.34 and Gradle 9.6.1 run on Temurin 17.0.1+12.
Compilation and Minecraft launches use Temurin 8.0.502+7 and Java 8 bytecode;
Mavenizer uses Temurin 25.0.3+9.0.LTS.

Power Advantage 2.4.7.116051 remains a separate required mod. The stable local API
bootstrap B source is `098d4625abc8a849bd1a11dcf697b3fee0cade7b`; its development jar SHA-256 is
`78E58DD2AB04E2B9F3267E5735C64AAE7E93EAC97571CB04485038862C853ED3`.
These exact pins are recorded in `gradle.properties` and audited during builds.
Use an immutable development jar through `-PpowerAdvantageDeobfJar=<path>`.
This API bootstrap is not runtime qualification and may be repinned after runtime
fixes. Release audits reject missing or placeholder pins.

Steam's 87-test build and artifact evidence belongs to bootstrap A, not B.
Bootstrap B initializes the GUI matrix before legacy direct decoration calls.
Steam compilation and the full suite against B remain pending the next cache
lease. Neither bootstrap has Steam runtime or manual acceptance.

The supplied native development dependencies are Base Metals 3.1.1.116051
(source `995f6cc4dd2d5eecc628a42e5c43fa5390c24bca`) and OreSpawn 4.1.0.116051
(source `a113e5d767169189f0e35ade1e173a12d550b00e`). Their exact jar hashes
are recorded in `gradle.properties`. None of the dependencies is embedded.

With matching native development jars available:

```text
gradlew.bat compileJava compileTestJava -PpowerAdvantageDeobfJar=<path>
```

Alternative layouts may also pass `-PbaseMetalsDeobfJar=<path>` and
`-PoreSpawnDeobfJar=<path>`. Explicit toolchain installations can be supplied
through Gradle project properties. Do not use a dependency from an older target.

With the exact dependency jars pinned, the full build gates are:

```text
gradlew.bat clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums verifyEclipseProductionClasspath
```

A compilation pass is not a test pass, release-audit pass, or runtime acceptance.
Shared Gradle caches require exclusive ownership; do not run competing builds
against the same cache or checkout.

## Compatibility

Runtime identities remain `steamadvantage:*`; the implementation namespace remains
`zone.moddev.mc.steamadvantage`. Registry names, tile IDs, resources, recipes,
trade ranges, machine costs, output quantities, and persistent NBT remain stable.

The native port uses world-only conduit callbacks. Power Advantage owns registry-key
dimension handling, shared capability invalidation/revival, fluid adapters, and base
tile lifecycle. Steam uses native `load(BlockState, CompoundNBT)` and
`save(CompoundNBT)`, retaining deferred drill targets, cached facing, progress,
inventory, energy, legacy tank aliases, and musket loaded-state bytes.
Saving a drill must not reacquire its world or neighbouring chunks.

Client setup registers native item model predicates, drill rendering, and the
track's cutout layer. Native block predicates preserve non-occlusion and prevent
full-collision tracks and platforms from conducting redstone. Existing renderer
geometry, UVs, lighting, and animation axes are retained.

Every crafting variant has ingredient-triggered recipe-book discovery.
Regenerate unlock JSON after recipe changes with
`node tools/generate-recipe-unlocks.js`. Processing recipes remain outside the
crafting book. Recipe conditions are registered on the queued setup thread.

Unit tests cover native properties, exact levels and costs, persistence,
world-only callbacks, recipe discovery, and resources. They do not establish
in-world GUIs, sounds, interactions, multiplayer behavior, or arbitrary modpacks.

Power Advantage supplies the combined-mod runtime harness and shared legacy
migration hook. Upgrade testing must use disposable whole-world copies with
pinned inputs. Preserve original worlds and backups; never open a world in an
older Minecraft version after upgrading it. Manual acceptance and publication
approval remain separate from automated evidence.
