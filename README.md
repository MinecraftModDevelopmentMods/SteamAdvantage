# SteamAdvantage

Steam Advantage adds steam-powered machines and infrastructure to Power Advantage for Minecraft 1.12.2.

## Building Minecraft 1.12.2

The `master-1.12.2` build uses ForgeGradle 7.0.34 and Gradle 9.6.1. Run Gradle with Java 17; Gradle resolves the Java 8 toolchain used for compilation.

First build the pinned sibling PowerAdvantage development jar:

```text
cd ..\..\PowerAdvantage
gradlew.bat deobfJar
```

Then build and audit SteamAdvantage:

```text
gradlew.bat clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums
```

Release jars are written to `build/libs`. The deobfuscated development jar is written to `build/libs-dev` by `deobfJar` or `build`.

For Eclipse, import this repository as an existing Gradle project and run:

```text
gradlew.bat cleanEclipse verifyEclipseProductionClasspath
```

Power Advantage is required at compile time and runtime but is never bundled. Base Metals remains a required distribution dependency while its integrations stay reflection-based and absent from the compile classpath.
