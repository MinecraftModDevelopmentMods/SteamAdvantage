package zone.moddev.mc.steamadvantage.support;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import net.minecraft.init.Bootstrap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/** Supplies Forge 25 launcher metadata before unit tests register native objects. */
public final class SteamNativeTestBootstrap {
    private SteamNativeTestBootstrap() {}

    public static synchronized void register() {
        Properties platform = new Properties();
        try (InputStream input = Files.newInputStream(Paths.get("gradle.properties"))) {
            platform.load(input);
            Class<?> loader = Class.forName("net.minecraftforge.fml.loading.FMLLoader");
            supplyVersion(loader, "mcVersion", platform.getProperty("minecraft_version"));
            supplyVersion(loader, "forgeVersion", platform.getProperty("forge_version"));
        } catch (IOException | ReflectiveOperationException failure) {
            throw new AssertionError("Native test bootstrap could not establish Forge launcher metadata", failure);
        }
        Bootstrap.register();
    }

    private static void supplyVersion(Class<?> loader, String name, String expected) throws ReflectiveOperationException {
        assertNotNull("Configured platform version exists for " + name, expected);
        Field field = loader.getDeclaredField(name);
        field.setAccessible(true);
        Object actual = field.get(null);
        if (actual == null) field.set(null, expected);
        else assertEquals("Existing launcher metadata matches this test platform: " + name, expected, actual);
    }
}
