package zone.moddev.mc.steamadvantage.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeLootContractTest {
    private static final Path ROOT = Paths.get("src/main/resources");
    private static final Path TABLES = ROOT.resolve("data/steamadvantage/loot_tables/blocks");

    @Test public void ordinaryBlocksKeepOneSelfDropWithExplosionSurvival() throws Exception {
        for (String id : new String[] { "steam_pipe", "steam_track", "steam_boiler_coal",
                "steam_boiler_electric", "steam_boiler_geothermal", "steam_tank", "steam_furnace",
                "steam_crusher", "steam_drill", "steam_elevator", "steam_switch", "steam_still",
                "steam_pump", "steam_boiler_oil" }) {
            JsonObject table = read(TABLES.resolve(id + ".json"));
            assertEquals("minecraft:block", table.get("type").getAsString());
            assertEquals(1, table.getAsJsonArray("pools").size());
            JsonObject pool = table.getAsJsonArray("pools").get(0).getAsJsonObject();
            assertEquals(1, pool.get("rolls").getAsInt());
            assertEquals(1, pool.getAsJsonArray("entries").size());
            JsonObject entry = pool.getAsJsonArray("entries").get(0).getAsJsonObject();
            assertEquals("minecraft:item", entry.get("type").getAsString());
            assertEquals("steamadvantage:" + id, entry.get("name").getAsString());
            assertEquals("minecraft:survives_explosion", pool.getAsJsonArray("conditions")
                    .get(0).getAsJsonObject().get("condition").getAsString());
        }
    }

    @Test public void helpersHaveNoSelfDropTables() {
        for (String id : new String[] { "drillbit", "platform", "pump_pipe_steam" }) {
            assertFalse(id, Files.exists(TABLES.resolve(id + ".json")));
        }
    }

    @Test public void packUsesNative116Format() throws Exception {
        assertEquals(6, read(ROOT.resolve("pack.mcmeta")).getAsJsonObject("pack").get("pack_format").getAsInt());
    }

    private static JsonObject read(Path path) throws Exception {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }
}
