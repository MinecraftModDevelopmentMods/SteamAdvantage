package zone.moddev.mc.steamadvantage.machines;

import static org.junit.Assert.*;
import java.lang.reflect.Field;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.junit.BeforeClass;
import org.junit.Test;

public class RockCrusherReloadRedstoneTest {
    @BeforeClass public static void bootstrap() {
        zone.moddev.mc.steamadvantage.support.SteamNativeTestBootstrap.register();
        if (zone.moddev.mc.steamadvantage.init.Entities.STEAM_CRUSHER == null)
            zone.moddev.mc.steamadvantage.init.Entities.STEAM_CRUSHER = net.minecraft.tileentity.TileEntityType.Builder.create(RockCrusherTileEntity::new).build(null);
        if (zone.moddev.mc.steamadvantage.init.Entities.STEAM_FURNACE == null)
            zone.moddev.mc.steamadvantage.init.Entities.STEAM_FURNACE = net.minecraft.tileentity.TileEntityType.Builder.create(BlastFurnaceTileEntity::new).build(null);
    }

    @Test public void resumedWorkCannotBeDecrementedBeforeTheFirstLiveControlSample() throws Exception {
        ProbeCrusher crusher = new ProbeCrusher();
        Field progress = RockCrusherTileEntity.class.getDeclaredField("progress");
        progress.setAccessible(true);
        progress.setInt(crusher, 106);
        crusher.testWorld = world();
        crusher.testWorld.stopAtFirstSample = true;
        try {
            crusher.tickUpdate(true);
            fail("Live control must be sampled before touching loaded progress");
        } catch (ControlSample expected) {
            assertEquals(106, progress.getInt(crusher));
            assertEquals(1, crusher.testWorld.samples);
        }
    }

    @Test public void scheduledPowerUpdateStillRefreshesTheInitializedCache() throws Exception {
        ProbeCrusher crusher = new ProbeCrusher();
        crusher.testWorld = world();
        crusher.tickUpdate(true);
        Field redstone = RockCrusherTileEntity.class.getDeclaredField("redstone");
        redstone.setAccessible(true);
        assertFalse(redstone.getBoolean(crusher));
        crusher.testWorld.powered = true;
        crusher.tickUpdate(true);
        assertFalse(redstone.getBoolean(crusher));
        crusher.powerUpdate();
        assertTrue(redstone.getBoolean(crusher));
        assertEquals(2, crusher.testWorld.samples);
    }

    @Test public void furnaceSamplesBeforeChangingItsRestoredHeatOrWork() throws Exception {
        ProbeFurnace furnace = new ProbeFurnace();
        Field temperature = BlastFurnaceTileEntity.class.getDeclaredField("temperature");
        temperature.setAccessible(true);
        temperature.setFloat(furnace, 1000);
        furnace.testWorld = world();
        furnace.testWorld.stopAtFirstSample = true;
        try {
            furnace.tickUpdate(true);
            fail("Live control must precede changes to loaded furnace heat or work");
        } catch (ControlSample expected) {
            assertEquals(1000, temperature.getFloat(furnace), 0);
            assertEquals(1, furnace.testWorld.samples);
        }
    }

    private static ControlWorld world() throws Exception {
        Class<?> allocator = Class.forName("sun.misc.Unsafe");
        Field field = allocator.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (ControlWorld) allocator.getMethod("allocateInstance", Class.class).invoke(field.get(null), ControlWorld.class);
    }
    private static final class ProbeCrusher extends RockCrusherTileEntity {
        ControlWorld testWorld;
        @Override public World getWorld() { return testWorld; }
    }
    private static final class ProbeFurnace extends BlastFurnaceTileEntity {
        ControlWorld testWorld;
        @Override public World getWorld() { return testWorld; }
    }
    private static final class ControlSample extends RuntimeException {}
    private static final class ControlWorld extends WorldServer {
        boolean powered, stopAtFirstSample;
        int samples;
        private ControlWorld() { super(null, null, null, null, null, null); }
        @Override public boolean isBlockPowered(BlockPos pos) {
            samples++;
            if (stopAtFirstSample) throw new ControlSample();
            return powered;
        }
    }
}
