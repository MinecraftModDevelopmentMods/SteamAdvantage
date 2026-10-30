package zone.moddev.mc.steamadvantage.recipe;

import com.google.gson.JsonObject;
import java.util.Locale;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import zone.moddev.mc.poweradvantage.PowerAdvantage;
import zone.moddev.mc.poweradvantage.RecipeMode;
import zone.moddev.mc.steamadvantage.SteamAdvantage;

/** Registers the data-pack conditions used by SteamAdvantage recipes. */
public final class RecipeConditions {
    private static boolean registered;

    private RecipeConditions() {}

    public static synchronized void register() {
        if (registered) return;
        CraftingHelper.register(new IConditionSerializer<ModeCondition>() {
            @Override public ResourceLocation getID() { return ModeCondition.ID; }
            @Override public ModeCondition read(JsonObject json) {
                return new ModeCondition(RecipeMode.valueOf(JSONUtils.getAsString(json, "mode").toUpperCase(Locale.ROOT)));
            }
            @Override public void write(JsonObject json, ModeCondition condition) {
                json.addProperty("mode", condition.mode.name());
            }
        });
        CraftingHelper.register(new IConditionSerializer<MusketCondition>() {
            @Override public ResourceLocation getID() { return MusketCondition.ID; }
            @Override public MusketCondition read(JsonObject json) { return new MusketCondition(); }
            @Override public void write(JsonObject json, MusketCondition condition) {}
        });
        registered = true;
    }

    private static final class ModeCondition implements ICondition {
        private static final ResourceLocation ID = new ResourceLocation(SteamAdvantage.MODID, "recipe_mode");
        private final RecipeMode mode;
        private ModeCondition(RecipeMode mode) { this.mode = mode; }
        @Override public ResourceLocation getID() { return ID; }
        @Override public boolean test() { return PowerAdvantage.recipeMode == mode; }
    }

    private static final class MusketCondition implements ICondition {
        private static final ResourceLocation ID = new ResourceLocation(SteamAdvantage.MODID, "musket_enabled");
        @Override public ResourceLocation getID() { return ID; }
        @Override public boolean test() { return SteamAdvantage.MUSKET_ENABLE; }
    }
}
