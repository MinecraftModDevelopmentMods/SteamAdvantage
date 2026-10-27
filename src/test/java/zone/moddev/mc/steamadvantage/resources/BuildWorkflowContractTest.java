package zone.moddev.mc.steamadvantage.resources;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

public class BuildWorkflowContractTest {
    @Test public void everyHostedBuildUsesPinnedToolchainProjectProperties() throws Exception {
        for (String name : new String[] {"ci.yml", "codeql-analysis.yml"}) {
            String source = new String(Files.readAllBytes(Paths.get(".github/workflows", name)),
                    StandardCharsets.UTF_8);
            assertTrue(name, source.contains("ref: 995f6cc4dd2d5eecc628a42e5c43fa5390c24bca"));
            assertTrue(name, source.contains("BaseMetals-3.1.1.116051-deobf.jar"));
            assertTrue(name, source.contains("ref: a113e5d767169189f0e35ade1e173a12d550b00e"));
            assertTrue(name, source.contains("ref: 3f1100e6f75b1fb949cc883be7288aa30789876d"));
            assertTrue(name, source.contains("Final Power 1.16 dependency pin is pending."));
            assertTrue(name, source.contains("^[a-f0-9]{40}$"));
            assertTrue(name, source.contains("^[A-F0-9]{64}$"));
            assertTrue(name, source.indexOf("Validate final Power source pin")
                    < source.indexOf("Check out PowerAdvantage 1.16.5"));
            assertTrue(name, source.contains("-Porg.gradle.java.installations.paths="));
            assertTrue(name, source.contains("-Porg.gradle.java.installations.auto-detect=false"));
            assertTrue(name, source.contains("-Porg.gradle.java.installations.auto-download=false"));
            assertFalse(name, source.contains("-Dorg.gradle.java.installations."));
            for (String line : source.split("\\R")) {
                if (line.contains("./gradlew ") && !line.contains("chmod")) {
                    assertTrue(name + ": " + line, line.contains("./gradlew $MMD_GRADLE_TOOLCHAINS "));
                }
            }
        }
    }
}
