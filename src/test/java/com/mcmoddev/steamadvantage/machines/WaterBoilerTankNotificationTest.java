package com.mcmoddev.steamadvantage.machines;

import static org.junit.Assert.*;
import cyano.poweradvantage.api.IPowerMachine;
import com.mcmoddev.poweradvantage.init.Fluids;
import java.util.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Save notifications only; native restart reports remain the disk-persistence evidence. */
@RunWith(Parameterized.class)
public class WaterBoilerTankNotificationTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> boilers() { return Arrays.asList(new Object[][]{{"electric"}, {"geothermal"}}); }
    private final String type;
    private Probe probe;
    public WaterBoilerTankNotificationTest(String type) { this.type = type; }
    @BeforeClass public static void bootstrap() {
        net.minecraft.init.Bootstrap.register();
    }
    @Before public void createRealBoiler() {
        probe = type.equals("electric") ? new ElectricProbe() : new GeothermalProbe();
        probe.save().remember(probe.tank());
    }

    @Test public void publicAcceptedFillRequestsSavingBeforeAnyTick() throws Exception {
        FluidStack offer = water(5);
        assertEquals(5, fill(offer, true));
        assertEquals(5, offer.amount);
        saved(5);
    }

    @Test public void networkAcceptedWaterAlsoRequestsSavingBeforeAnyTick() {
        assertEquals(5, probe.machine().addEnergy(5, Fluids.fluidToConduitType(net.minecraftforge.fluids.FluidRegistry.WATER)), 0);
        saved(5);
    }

    @Test public void directTankFillUsesTheSameOwnerNotification() {
        assertEquals(5, probe.tank().fill(water(5), true));
        saved(5);
    }

    @Test public void publicRealDrainRequestsSaving() throws Exception {
        stock(10);
        assertEquals(5, drain(5, true).amount);
        saved(5);
    }

    @Test public void directRealDrainUsesTheSameOwnerNotification() {
        stock(10);
        assertEquals(5, probe.tank().drain(5, true).amount);
        saved(5);
    }

    @Test public void drainingToEmptyRequestsSavingOfTheNativeEmptyRepresentation() {
        stock(5);
        assertEquals(5, probe.tank().drain(5, true).amount);
        saved(0);
        assertNull(FluidStack.loadFluidStackFromNBT(probe.save().savedTank));
    }

    @Test public void directAndPublicSimulationsNeverRequestSaving() throws Exception {
        stock(10);
        assertEquals(5, probe.tank().fill(water(5), false));
        assertEquals(5, probe.tank().drain(5, false).amount);
        assertEquals(5, fill(water(5), false));
        assertEquals(5, drain(5, false).amount);
        unchanged(10);
    }

    @Test public void aClientFillChangesOnlyItsLocalTankWithoutRequestingServerSaving() throws Exception {
        stock(10); probe.side(true);
        assertEquals(5, fill(water(5), true));
        assertEquals(15, probe.tank().getFluidAmount());
        assertEquals(0, probe.save().dirtyCalls);
        assertEquals(10, probe.save().savedTank.getInteger("Amount"));
    }

    @Test public void aClientDirectDrainDoesNotRequestServerSaving() {
        stock(10); probe.side(true);
        assertEquals(5, probe.tank().drain(5, true).amount);
        assertEquals(5, probe.tank().getFluidAmount());
        assertEquals(0, probe.save().dirtyCalls);
        assertEquals(10, probe.save().savedTank.getInteger("Amount"));
    }

    private static FluidStack water(int amount) { return new FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, amount); }
    private void stock(int amount) { probe.tank().setFluid(water(amount)); probe.save().remember(probe.tank()); }
    private void saved(int amount) {
        assertTrue("Real server-side tank mutation must notify its owner immediately", probe.save().dirtyCalls > 0);
        assertEquals(amount, probe.tank().getFluidAmount());
        assertEquals(amount, probe.save().savedTank.getInteger("Amount"));
    }
    private void unchanged(int amount) {
        assertEquals(0, probe.save().dirtyCalls);
        assertEquals(amount, probe.tank().getFluidAmount());
        assertEquals(amount, probe.save().savedTank.getInteger("Amount"));
    }
    private int fill(FluidStack offer, boolean execute) throws Exception {
        return ((Number) probe.machine().getClass().getMethod("fill", net.minecraft.util.EnumFacing.class, FluidStack.class, boolean.class)
                .invoke(probe.machine(), null, offer, execute)).intValue();
    }
    private FluidStack drain(int amount, boolean execute) throws Exception {
        return (FluidStack) probe.machine().getClass().getMethod("drain", net.minecraft.util.EnumFacing.class, int.class, boolean.class)
                .invoke(probe.machine(), null, amount, execute);
    }
    private interface Probe {
        FluidTank tank();
        SaveWitness save();
        IPowerMachine machine();
        void side(boolean client);
    }
    private static final class SaveWitness {
        int dirtyCalls;
        NBTTagCompound savedTank;
        void remember(FluidTank tank) { dirtyCalls = 0; savedTank = tank.writeToNBT(new NBTTagCompound()).copy(); }
        void changed(FluidTank tank) { dirtyCalls++; savedTank = tank.writeToNBT(new NBTTagCompound()).copy(); }
    }
    private static final class ElectricProbe extends ElectricBoilerTileEntity implements Probe {
        private final SaveWitness witness = new SaveWitness();
        private World testWorld = world(false);
        @Override public World getWorld() { return testWorld; }
        @Override public void markDirty() { witness.changed(getTank()); }
        @Override public FluidTank tank() { return getTank(); }
        @Override public SaveWitness save() { return witness; }
        @Override public IPowerMachine machine() { return this; }
        @Override public void side(boolean client) { testWorld = world(client); }
    }
    private static final class GeothermalProbe extends GeothermalBoilerTileEntity implements Probe {
        private final SaveWitness witness = new SaveWitness();
        private World testWorld = world(false);
        @Override public World getWorld() { return testWorld; }
        @Override public void markDirty() { witness.changed(getTank()); }
        @Override public FluidTank tank() { return getTank(); }
        @Override public SaveWitness save() { return witness; }
        @Override public IPowerMachine machine() { return this; }
        @Override public void side(boolean client) { testWorld = world(client); }
    }
    private static World world(boolean client) { return new SaveWorld(client); }
    private static final class SaveWorld extends World {
        SaveWorld(boolean client) { super(null, null, new net.minecraft.world.WorldProviderSurface(), new net.minecraft.profiler.Profiler(), client); }
        @Override protected net.minecraft.world.chunk.IChunkProvider createChunkProvider() { throw new AssertionError("No world ticks in notification tests"); }
        @Override protected boolean isChunkLoaded(int x, int z, boolean empty) { return true; }
    }
}
