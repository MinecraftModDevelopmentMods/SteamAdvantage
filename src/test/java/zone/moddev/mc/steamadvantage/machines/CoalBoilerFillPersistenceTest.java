package zone.moddev.mc.steamadvantage.machines;

import static org.junit.Assert.*;

import cyano.poweradvantage.api.ConduitType;
import zone.moddev.mc.poweradvantage.init.Fluids;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import cyano.poweradvantage.api.fluid.FluidStack;
import org.junit.BeforeClass;
import org.junit.Test;

/** Save-notification unit witness; actual disk persistence is checked by the native restart rig. */
public class CoalBoilerFillPersistenceTest {
    @BeforeClass public static void bootstrap() {
        zone.moddev.mc.steamadvantage.support.SteamNativeTestBootstrap.register();
        if (zone.moddev.mc.steamadvantage.init.Entities.STEAM_BOILER_COAL == null)
            zone.moddev.mc.steamadvantage.init.Entities.STEAM_BOILER_COAL = net.minecraft.tileentity.TileEntityType.Builder.create(CoalBoilerTileEntity::new).build(null);
    }

    @Test public void acceptingTheLastFiveMillibucketsRequestsSavingBeforeAnyTick() {
        SaveWitnessBoiler boiler = seeded(3995);
        FluidStack offered = new FluidStack(net.minecraft.init.Fluids.WATER, 5);

        assertEquals(5, boiler.fill(null, offered, true));
        assertEquals(4000, boiler.getTank().getFluidAmount());
        assertEquals(5, offered.amount);
        assertSaved(boiler, 4000);
    }

    @Test public void partialAcceptanceSavesOnlyTheQuantityActuallyAccepted() {
        SaveWitnessBoiler boiler = seeded(3995);
        FluidStack offered = new FluidStack(net.minecraft.init.Fluids.WATER, 100);

        assertEquals(5, boiler.fill(null, offered, true));
        assertEquals(4000, boiler.getTank().getFluidAmount());
        assertEquals(100, offered.amount);
        assertSaved(boiler, 4000);
    }

    @Test public void waterAcceptedThroughTheNetworkAlsoRequestsSavingImmediately() {
        SaveWitnessBoiler boiler = seeded(3995);
        ConduitType water = Fluids.fluidToConduitType(net.minecraft.init.Fluids.WATER);

        assertEquals(5, boiler.addEnergy(5, water), 0);
        assertEquals(4000, boiler.getTank().getFluidAmount());
        assertSaved(boiler, 4000);
    }

    @Test public void simulationNeitherChangesTheTankNorRequestsSaving() {
        SaveWitnessBoiler boiler = seeded(3995);
        FluidStack offered = new FluidStack(net.minecraft.init.Fluids.WATER, 100);

        assertEquals(5, boiler.fill(null, offered, false));
        assertEquals(3995, boiler.getTank().getFluidAmount());
        assertEquals(100, offered.amount);
        assertUnchangedSave(boiler, 3995);
    }

    @Test public void aFullTankRejectsInputWithoutRequestingSaving() {
        SaveWitnessBoiler boiler = seeded(4000);

        assertEquals(0, boiler.fill(null, new FluidStack(net.minecraft.init.Fluids.WATER, 5), true));
        assertEquals(4000, boiler.getTank().getFluidAmount());
        assertUnchangedSave(boiler, 4000);
    }

    @Test public void mismatchedFluidIsNotCreditedOrSaved() {
        SaveWitnessBoiler boiler = seeded(3995);
        FluidStack offered = new FluidStack(net.minecraft.init.Fluids.LAVA, 5);

        assertEquals(0, boiler.fill(null, offered, true));
        assertSame(net.minecraft.init.Fluids.WATER, boiler.getTank().getFluid().getFluid());
        assertEquals(3995, boiler.getTank().getFluidAmount());
        assertEquals(5, offered.amount);
        assertUnchangedSave(boiler, 3995);
    }

    @Test public void aZeroQuantityDoesNotRequestSaving() {
        SaveWitnessBoiler boiler = seeded(3995);

        assertEquals(0, boiler.fill(null, new FluidStack(net.minecraft.init.Fluids.WATER, 0), true));
        assertEquals(3995, boiler.getTank().getFluidAmount());
        assertUnchangedSave(boiler, 3995);
    }

    @Test public void theImmediateSaveWitnessRetainsFluidIdentityAndTypedNbt() {
        SaveWitnessBoiler boiler = new SaveWitnessBoiler();
        FluidStack offered = new FluidStack(net.minecraft.init.Fluids.WATER, 5);
        offered.tag = new NBTTagCompound();
        offered.tag.setString("AdvWorksWitness", "external-fill-before-next-tick");
        offered.tag.setShort("TypedWitness", (short) 7);
        NBTTagCompound original = offered.tag.copy();
        boiler.rememberSavedTank();

        assertEquals(5, boiler.fill(null, offered, true));
        assertSaved(boiler, 5);
        FluidStack saved = FluidStack.loadFluidStackFromNBT(boiler.savedTank);
        assertNotNull(saved);
        assertSame(net.minecraft.init.Fluids.WATER, saved.getFluid());
        assertEquals(original, saved.tag);
        assertEquals(original, offered.tag);
        assertEquals(5, offered.amount);
    }

    @Test public void emptyRequestedDrainReturnsNullWithoutDereferencingAMissingFluid() {
        SaveWitnessBoiler boiler = new SaveWitnessBoiler();
        boiler.rememberSavedTank();
        assertNull(boiler.drain(null, new FluidStack(net.minecraft.init.Fluids.WATER, 5), true));
        assertNull(boiler.drain(null, new FluidStack(net.minecraft.init.Fluids.WATER, 5), false));
        assertEquals(0, boiler.dirtyCalls);
    }

    @Test public void nullAndZeroRequestsNeitherMutateNorRequestSaving() {
        SaveWitnessBoiler boiler = seeded(10);
        assertEquals(0, boiler.fill(null, null, true));
        assertNull(boiler.drain(null, (FluidStack)null, true));
        assertNull(boiler.drain(null, new FluidStack(net.minecraft.init.Fluids.WATER, 0), true));
        assertNull(boiler.drain(null, 0, true));
        assertNull(boiler.drain(null, -1, true));
        assertEquals(10, boiler.getTank().getFluidAmount());
        assertUnchangedSave(boiler, 10);
    }

    @Test public void mismatchedRequestedDrainReturnsNullRatherThanAZeroStack() {
        SaveWitnessBoiler boiler = seeded(10);
        assertNull(boiler.drain(null, new FluidStack(net.minecraft.init.Fluids.LAVA, 5), true));
        assertNull(boiler.drain(null, new FluidStack(net.minecraft.init.Fluids.LAVA, 5), false));
        assertEquals(10, boiler.getTank().getFluidAmount());
        assertUnchangedSave(boiler, 10);
    }

    @Test public void nativeTankSimulationDoesNotRequestSaving() {
        SaveWitnessBoiler boiler = seeded(10);
        FluidStack preview = boiler.getTank().drain(5, false);
        assertNotNull(preview);
        assertEquals(5, preview.amount);
        assertEquals(10, boiler.getTank().getFluidAmount());
        assertUnchangedSave(boiler, 10);
    }

    @Test public void nativeRealTankDrainRequestsSavingEvenOutsideTheBoilerOverloads() {
        SaveWitnessBoiler boiler = seeded(10);
        FluidStack drained = boiler.getTank().drain(5, true);
        assertNotNull(drained);
        assertEquals(5, drained.amount);
        assertEquals(5, boiler.getTank().getFluidAmount());
        assertSaved(boiler, 5);
    }

    @Test public void aRealDrainToEmptySavesTheNativeEmptyTankRepresentation() {
        SaveWitnessBoiler boiler = seeded(5);
        assertEquals(5, boiler.drain(null, 5, true).amount);
        assertEquals(0, boiler.getTank().getFluidAmount());
        assertSaved(boiler, 0);
        assertNull(FluidStack.loadFluidStackFromNBT(boiler.savedTank));
        assertNull(boiler.drain(null, new FluidStack(net.minecraft.init.Fluids.WATER, 5), true));
    }

    @Test public void clientSideTankChangesDoNotRequestServerSaving() {
        SaveWitnessBoiler boiler = seeded(3995);
        boiler.saveWorld = world(true);
        assertEquals(5, boiler.fill(null, new FluidStack(net.minecraft.init.Fluids.WATER, 5), true));
        assertEquals(4000, boiler.getTank().getFluidAmount());
        assertUnchangedSave(boiler, 3995);
    }

    private static SaveWitnessBoiler seeded(int amount) {
        SaveWitnessBoiler boiler = new SaveWitnessBoiler();
        boiler.getTank().setFluid(new FluidStack(net.minecraft.init.Fluids.WATER, amount));
        boiler.rememberSavedTank();
        return boiler;
    }

    private static void assertSaved(SaveWitnessBoiler boiler, int amount) {
        assertTrue("An accepted external fill must request saving without relying on a later tile tick",
                boiler.dirtyCalls > 0);
        assertEquals(amount, boiler.savedTank.getInt("Amount"));
    }

    private static void assertUnchangedSave(SaveWitnessBoiler boiler, int amount) {
        assertEquals(0, boiler.dirtyCalls);
        assertEquals(amount, boiler.savedTank.getInt("Amount"));
    }

    private static final class SaveWitnessBoiler extends CoalBoilerTileEntity {
        int dirtyCalls;
        NBTTagCompound savedTank;
        SaveWorld saveWorld = world(false);
        @Override public World getWorld() { return saveWorld; }
        void rememberSavedTank() {
            dirtyCalls = 0;
            savedTank = getTank().writeToNBT(new NBTTagCompound()).copy();
        }
        @Override public void markDirty() {
            dirtyCalls++;
            // Record only the notification payload; do not impersonate a native server save.
            savedTank = getTank().writeToNBT(new NBTTagCompound()).copy();
        }
    }

    private static SaveWorld world(boolean remote) {
        try {
            Class<?> allocator = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field field = allocator.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            SaveWorld world = (SaveWorld) allocator.getMethod("allocateInstance", Class.class).invoke(field.get(null), SaveWorld.class);
            java.lang.reflect.Field side = World.class.getDeclaredField("isRemote");
            side.setAccessible(true);
            side.setBoolean(world, remote);
            return world;
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Cannot prepare the save-notification world", failure);
        }
    }
    private static final class SaveWorld extends net.minecraft.world.WorldServer {
        private SaveWorld() { super(null, null, null, null, null, null); }
    }
}
