package zone.moddev.mc.steamadvantage.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraftforge.event.ForgeEventFactory;

/** Forge-aware access to the vanilla furnace fuel calculation. */
public final class FurnaceFuelHelper {
    private FurnaceFuelHelper() {}

    public static int getBurnTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        int itemBurnTime = stack.getBurnTime();
        int vanillaBurnTime = itemBurnTime == -1
                ? AbstractFurnaceTileEntity.getBurnTimes().getOrDefault(item, 0)
                : itemBurnTime;
        return ForgeEventFactory.getItemBurnTime(stack, vanillaBurnTime);
    }
}
