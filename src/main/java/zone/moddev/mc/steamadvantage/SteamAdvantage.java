package zone.moddev.mc.steamadvantage;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import cyano.poweradvantage.api.migration.LegacyConfigMigration;
import cyano.poweradvantage.api.migration.LegacyConfigMigration.Mapping;
import cyano.poweradvantage.api.migration.LegacyConfigMigration.ValueType;
import zone.moddev.mc.steamadvantage.config.SteamAdvantageConfig;
import zone.moddev.mc.steamadvantage.init.Blocks;
import zone.moddev.mc.steamadvantage.init.Enchantments;
import zone.moddev.mc.steamadvantage.init.Entities;
import zone.moddev.mc.steamadvantage.init.GUI;
import zone.moddev.mc.steamadvantage.init.Items;
import zone.moddev.mc.steamadvantage.init.Recipes;
import zone.moddev.mc.steamadvantage.init.TreasureChests;
import zone.moddev.mc.steamadvantage.init.Villages;
import zone.moddev.mc.steamadvantage.migration.SteamLegacyMigrations;
import zone.moddev.mc.steamadvantage.recipe.RecipeConditions;

/** Forge 1.15 entry point for Steam Advantage. */
@Mod(SteamAdvantage.MODID)
public final class SteamAdvantage {
    public static final String MODID = "steamadvantage";
    public static final String NAME = "Steam Advantage";
    public static final String VERSION = "2.2.7.115021";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static float MUSKET_DAMAGE = 20.0F;
    public static int MUSKET_RELOAD = 20 * 5;
    public static boolean MUSKET_ENABLE = true;
    public static final Map<String, Float> fluidBurnValues = new HashMap<>();

    public SteamAdvantage() {
        SteamLegacyMigrations.register();
        Blocks.init();
        Items.init();
        Entities.init();
        Enchantments.init();
        GUI.init();

        migrateLegacyConfiguration();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SteamAdvantageConfig.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SteamAdvantageConfig::onConfigLoading);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(
                    zone.moddev.mc.steamadvantage.graphics.ClientRegistration::onClientSetup);
        });
    }

    private static void migrateLegacyConfiguration() {
        try {
            LegacyConfigMigration.Result result = LegacyConfigMigration.migrate(FMLPaths.CONFIGDIR.get(), MODID,
                    Mapping.of("options", "musket_damage", "options", "musketDamage", ValueType.DOUBLE),
                    Mapping.of("options", "musket_reload_time", "options", "musketReloadTime", ValueType.INTEGER),
                    Mapping.of("options", "musket_allowed", "options", "musketAllowed", ValueType.BOOLEAN),
                    Mapping.of("options", "fluid_fuel_values", "options", "fluidFuelValues", ValueType.STRING));
            if (result.wasMigrated()) {
                LOGGER.info("Imported {} legacy Steam Advantage settings; report: {}",
                        result.getImportedValues(), result.getReportFile());
            }
        } catch (IOException exception) {
            LOGGER.error("Could not migrate legacy Steam Advantage configuration", exception);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // CraftingHelper's shared condition map is not safe during parallel loading.
        net.minecraftforge.fml.DeferredWorkQueue.runLater(RecipeConditions::register);
        SteamAdvantageConfig.apply();
        Recipes.init();
        TreasureChests.init(FMLPaths.CONFIGDIR.get());
        Villages.init();
        LOGGER.info("Steam Advantage {} common setup complete", VERSION);
    }
}
