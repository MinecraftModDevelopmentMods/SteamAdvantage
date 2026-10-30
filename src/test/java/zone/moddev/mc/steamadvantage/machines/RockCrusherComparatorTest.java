package zone.moddev.mc.steamadvantage.machines;

import static org.junit.Assert.assertEquals;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.BeforeClass;
import org.junit.Test;

public class RockCrusherComparatorTest {
    @BeforeClass public static void bootstrap() {
        net.minecraft.util.registry.Bootstrap.register();
        if (zone.moddev.mc.steamadvantage.init.Entities.STEAM_CRUSHER == null)
            zone.moddev.mc.steamadvantage.init.Entities.STEAM_CRUSHER = net.minecraft.tileentity.TileEntityType.Builder.create(RockCrusherTileEntity::new, net.minecraft.block.Blocks.STONE).build(null);
    }
    @Test public void emptyInputHasNoComparatorSignal() { assertSignal(0, 0); }
    @Test public void oneItemGivesSignalOne() { assertSignal(1, 1); }
    @Test public void halfAStackGivesSignalEight() { assertSignal(32, 8); }
    @Test public void aFullStackGivesSignalFifteen() { assertSignal(64, 15); }

    private static void assertSignal(int count, int expected) {
        RockCrusherTileEntity crusher = new RockCrusherTileEntity();
        crusher.setInventorySlotContents(0, count == 0 ? ItemStack.EMPTY : new ItemStack(Items.COAL, count));
        assertEquals("Actual crusher comparator at " + count + " input items", expected, crusher.getComparatorOutput());
    }
}
