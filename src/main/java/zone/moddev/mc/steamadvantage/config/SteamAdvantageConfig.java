package zone.moddev.mc.steamadvantage.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import zone.moddev.mc.steamadvantage.SteamAdvantage;

/** Common configuration retaining the legacy gameplay defaults. */
public final class SteamAdvantageConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue MUSKET_DAMAGE;
    public static final ForgeConfigSpec.IntValue MUSKET_RELOAD;
    public static final ForgeConfigSpec.BooleanValue MUSKET_ALLOWED;
    public static final ForgeConfigSpec.ConfigValue<String> FLUID_FUEL_VALUES;

    private static boolean loaded;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("options");
        MUSKET_DAMAGE = builder.defineInRange("musketDamage", 20.0D, 0.0D, 100.0D);
        MUSKET_RELOAD = builder.defineInRange("musketReloadTime", 100, 20, 300);
        MUSKET_ALLOWED = builder.define("musketAllowed", true);
        FLUID_FUEL_VALUES = builder.comment(
                "Semi-colon delimited fluid registry names and burn ticks per bucket, for example oil=5000.")
                .define("fluidFuelValues", "oil=5000;fuel=25000");
        builder.pop();
        SPEC = builder.build();
    }

    private SteamAdvantageConfig() {}

    public static void onConfigLoading(ModConfig.Loading event) {
        if (event.getConfig().getSpec() != SPEC) return;
        loaded = true;
        apply();
    }

    public static void apply() {
        if (!loaded) return;
        SteamAdvantage.MUSKET_DAMAGE = MUSKET_DAMAGE.get().floatValue();
        SteamAdvantage.MUSKET_RELOAD = MUSKET_RELOAD.get();
        SteamAdvantage.MUSKET_ENABLE = MUSKET_ALLOWED.get();
        SteamAdvantage.fluidBurnValues.clear();
        for (String descriptor : FLUID_FUEL_VALUES.get().split(";")) {
            String[] parts = descriptor.split("=", 2);
            if (parts.length != 2) continue;
            try {
                SteamAdvantage.fluidBurnValues.put(parts[0].trim(), Float.parseFloat(parts[1].trim()));
            } catch (NumberFormatException ex) {
                SteamAdvantage.LOGGER.warn("Ignoring invalid fluid fuel descriptor '{}'", descriptor);
            }
        }
    }
}
