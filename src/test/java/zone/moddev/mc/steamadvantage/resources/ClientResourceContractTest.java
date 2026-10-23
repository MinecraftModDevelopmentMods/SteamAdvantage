package zone.moddev.mc.steamadvantage.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.Assert.*;

public class ClientResourceContractTest {
    private static final Path ROOT = Paths.get("src/main/resources");
    private static final Path ASSETS = ROOT.resolve("assets/steamadvantage");

    @Test public void blockstatesUseLiteralModelPaths() throws Exception {
        try (Stream<Path> files = Files.walk(ASSETS.resolve("blockstates"))) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".json"))::iterator) {
                verifyModels(read(file), file);
            }
        }
    }

    @Test public void obsoleteSelectorsAreAbsent() throws Exception {
        try (Stream<Path> files = Files.walk(ASSETS.resolve("blockstates"))) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".json"))::iterator) {
                JsonObject state = read(file);
                assertFalse(file.toString(), state.has("forge_marker"));
                if (state.has("variants")) {
                    assertFalse(file.toString(), state.getAsJsonObject("variants").has("normal"));
                    assertFalse(file.toString(), state.getAsJsonObject("variants").has("fluid"));
                }
            }
        }
    }

    @Test public void parentsAndTexturesExistInActualPacks() throws Exception {
        try (Stream<Path> files = Files.walk(ASSETS.resolve("models"))) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".json"))::iterator) {
                JsonObject model = read(file);
                if (model.has("parent")) reference(model.get("parent").getAsString(), "models", ".json", file);
                if (model.has("textures")) for (Map.Entry<String, JsonElement> entry : model.getAsJsonObject("textures").entrySet())
                    reference(entry.getValue().getAsString(), "textures", ".png", file);
            }
        }
    }

    @Test public void modernAliasesRetainEveryLegacyTranslation() throws Exception {
        try (Stream<Path> files = Files.walk(ASSETS.resolve("lang"))) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".json"))::iterator) {
                JsonObject locale = read(file);
                for (Map.Entry<String, JsonElement> entry : locale.entrySet()) {
                    String key = entry.getKey();
                    if (!(key.startsWith("tile.steamadvantage.") || key.startsWith("item.steamadvantage."))
                            || !key.endsWith(".name")) continue;
                    String alias = key.substring(0, key.length() - 5);
                    if (alias.startsWith("tile.")) alias = "block." + alias.substring(5);
                    assertEquals(file + " lost alias " + alias, entry.getValue(), locale.get(alias));
                }
            }
        }
    }

    private static void verifyModels(JsonElement node, Path file) {
        if (node.isJsonObject()) for (Map.Entry<String, JsonElement> entry : node.getAsJsonObject().entrySet()) {
            if ("model".equals(entry.getKey())) reference(entry.getValue().getAsString(), "models", ".json", file);
            else verifyModels(entry.getValue(), file);
        }
        else if (node.isJsonArray()) for (JsonElement child : node.getAsJsonArray()) verifyModels(child, file);
    }

    private static void reference(String id, String directory, String suffix, Path file) {
        if (id.startsWith("#")) return;
        String[] parts = id.split(":", 2);
        String namespace = parts.length == 2 ? parts[0] : "minecraft";
        String path = parts.length == 2 ? parts[1] : parts[0];
        if ("minecraft".equals(namespace) && path.startsWith("builtin/")) return;
        String resource = "assets/" + namespace + "/" + directory + "/" + path + suffix;
        boolean exists = "steamadvantage".equals(namespace) ? Files.isRegularFile(ROOT.resolve(resource))
                : ClientResourceContractTest.class.getClassLoader().getResource(resource) != null;
        assertTrue(file + " references missing " + resource, exists);
    }

    private static JsonObject read(Path file) throws Exception {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }
}
