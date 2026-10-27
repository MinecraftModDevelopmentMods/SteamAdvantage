package zone.moddev.mc.steamadvantage.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;

/** Forge-aware access to the vanilla furnace fuel calculation. */
public final class FurnaceFuelHelper {
    private FurnaceFuelHelper() {}

    public static int getBurnTime(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, null);
    }
}
