package zone.moddev.mc.steamadvantage.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

public class MusketPersistenceTest {
    @BeforeClass public static void bootstrapNativeRegistries() {
        net.minecraft.util.registry.Bootstrap.bootStrap();
    }

    @Test public void loadingCycleKeepsLegacyByteValuesAndOtherNbt() {
        ItemStack stack = new ItemStack(net.minecraft.item.Items.STICK);
        CompoundNBT tag = new CompoundNBT();
        tag.putString("owner", "legacy");
        stack.setTag(tag);
        assertTrue(MusketItem.isNotLoaded(stack));
        MusketItem.startLoad(stack);
        assertEquals(2, stack.getTag().getByte("loaded"));
        assertTrue(MusketItem.isAlmostLoaded(stack));
        MusketItem.finishLoad(stack);
        assertEquals(1, stack.getTag().getByte("loaded"));
        assertTrue(MusketItem.isLoaded(stack.copy()));
        MusketItem.unload(stack);
        assertEquals(0, stack.getTag().getByte("loaded"));
        assertTrue(MusketItem.isNotLoaded(stack));
        assertEquals("legacy", stack.getTag().getString("owner"));
    }

    @Test public void rayIntersectionSupportsEveryAxisAndRejectsTargetsBehind() {
        Vector3d origin = new Vector3d(0, 0, 0);
        for (net.minecraft.util.Direction direction : net.minecraft.util.Direction.values()) {
            Vector3d ray = new Vector3d(direction.getStepX(), direction.getStepY(), direction.getStepZ());
            Vector3d center = ray.scale(5);
            AxisAlignedBB ahead = new AxisAlignedBB(center.x - 0.5, center.y - 0.5, center.z - 0.5,
                    center.x + 0.5, center.y + 0.5, center.z + 0.5);
            assertTrue(MusketItem.rayIntersectsBoundingBox(origin, ray, ahead));
            assertFalse(MusketItem.rayIntersectsBoundingBox(origin, ray.scale(-1), ahead));
        }
    }
}
