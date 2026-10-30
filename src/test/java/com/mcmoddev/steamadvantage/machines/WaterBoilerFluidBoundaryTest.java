package com.mcmoddev.steamadvantage.machines;

import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.util.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Calls each actual boiler method; no stand-in fill/drain implementation or world ticks. */
@RunWith(Parameterized.class)
public class WaterBoilerFluidBoundaryTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> boilers() {
        return Arrays.asList(new Object[][]{{CoalBoilerTileEntity.class},
                {ElectricBoilerTileEntity.class}, {GeothermalBoilerTileEntity.class}});
    }
    private final Class<?> type;
    private Object boiler;
    private FluidTank tank;
    public WaterBoilerFluidBoundaryTest(Class<?> type) { this.type = type; }
    @BeforeClass public static void bootstrap() {
        net.minecraft.init.Bootstrap.register();
    }
    @Before public void createActualBoiler() throws Exception {
        boiler = type.getConstructor().newInstance();
        tank = (FluidTank) type.getMethod("getTank").invoke(boiler);
    }

    @Test public void emptyTankRejectsLavaInSimulationAndExecutionWithoutChangingTheOffer() throws Exception {
        for (boolean execute : new boolean[]{false, true}) {
            stock(0);
            FluidStack lava = new FluidStack(net.minecraftforge.fluids.FluidRegistry.LAVA, 5);
            NBTTagCompound original = lava.writeToNBT(new NBTTagCompound()).copy();
            assertEquals(Boolean.FALSE, type.getMethod("canFill", net.minecraft.util.EnumFacing.class, net.minecraftforge.fluids.Fluid.class).invoke(boiler, null, net.minecraftforge.fluids.FluidRegistry.LAVA));
            assertEquals(0, fill(lava, execute));
            assertEquals(0, tank.getFluidAmount());
            assertEquals(original, lava.writeToNBT(new NBTTagCompound()));
        }
    }

    @Test public void emptyTankStillAcceptsWaterWithoutChangingTheOffer() throws Exception {
        FluidStack offered = new FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, 5);
        NBTTagCompound original = offered.writeToNBT(new NBTTagCompound()).copy();
        assertEquals(Boolean.TRUE, type.getMethod("canFill", net.minecraft.util.EnumFacing.class, net.minecraftforge.fluids.Fluid.class).invoke(boiler, null, net.minecraftforge.fluids.FluidRegistry.WATER));
        assertEquals(5, fill(offered, true));
        assertEquals(5, tank.getFluidAmount());
        assertSame(net.minecraftforge.fluids.FluidRegistry.WATER, tank.getFluid().getFluid());
        assertEquals(original, offered.writeToNBT(new NBTTagCompound()));
    }

    @Test public void seededTankRejectsMismatchedFillAndDrain() throws Exception {
        stock(100);
        FluidStack lava = new FluidStack(net.minecraftforge.fluids.FluidRegistry.LAVA, 5);
        NBTTagCompound original = lava.writeToNBT(new NBTTagCompound()).copy();
        for (boolean execute : new boolean[]{false, true}) {
            assertEquals(0, fill(lava, execute));
            assertNull(drain(lava, execute));
        }
        assertEquals(100, tank.getFluidAmount());
        assertSame(net.minecraftforge.fluids.FluidRegistry.WATER, tank.getFluid().getFluid());
        assertEquals(original, lava.writeToNBT(new NBTTagCompound()));
    }

    @Test public void nullFillIsSafeOnEmptyAndSeededTanks() throws Exception {
        for (int amount : new int[]{0, 100}) {
            stock(amount);
            assertEquals(0, fill(null, false));
            assertEquals(0, fill(null, true));
            assertEquals(amount, tank.getFluidAmount());
        }
    }

    @Test public void zeroAndNegativeFillsDoNotChangeTheTank() throws Exception {
        stock(100);
        for (int amount : new int[]{0, -5}) {
            FluidStack request = new FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, amount);
            request.amount = amount;
            assertEquals(0, fill(request, false));
            assertEquals(0, fill(request, true));
        }
        assertEquals(100, tank.getFluidAmount());
    }

    @Test public void typedDrainOnAnEmptyTankReturnsNull() throws Exception {
        assertNull(drain(new FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, 5), false));
        assertNull(drain(new FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, 5), true));
        assertEquals(0, tank.getFluidAmount());
    }

    @Test public void nullTypedDrainIsSafeOnEmptyAndSeededTanks() throws Exception {
        for (int amount : new int[]{0, 100}) {
            stock(amount);
            assertNull(drain((FluidStack)null, false));
            assertNull(drain((FluidStack)null, true));
            assertEquals(amount, tank.getFluidAmount());
        }
    }

    @Test public void zeroAndNegativeTypedDrainsReturnNull() throws Exception {
        stock(100);
        for (int amount : new int[]{0, -5}) {
            FluidStack request = new FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, amount);
            request.amount = amount;
            assertNull(drain(request, false));
            assertNull(drain(request, true));
        }
        assertEquals(100, tank.getFluidAmount());
    }

    @Test public void zeroAndNegativeAmountDrainsReturnNull() throws Exception {
        stock(100);
        for (int amount : new int[]{0, -5}) {
            assertNull(drain(amount, false));
            assertNull(drain(amount, true));
        }
        assertEquals(100, tank.getFluidAmount());
    }

    @Test public void matchingSimulationKeepsAllStockAndTheFiniteRequestUnchanged() throws Exception {
        stock(100);
        FluidStack request = new FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, 5);
        NBTTagCompound original = request.writeToNBT(new NBTTagCompound()).copy();
        assertEquals(5, fill(request, false));
        assertEquals(5, drain(request, false).amount);
        assertEquals(5, drain(5, false).amount);
        assertEquals(100, tank.getFluidAmount());
        assertEquals(original, request.writeToNBT(new NBTTagCompound()));
    }

    @Test public void aRealMatchingDrainReturnsOnlyTheRequestedWater() throws Exception {
        stock(100);
        FluidStack request = new FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, 5);
        FluidStack result = drain(request, true);
        assertNotNull(result);
        assertSame(net.minecraftforge.fluids.FluidRegistry.WATER, result.getFluid());
        assertEquals(5, result.amount);
        assertEquals(95, tank.getFluidAmount());
        assertEquals(5, request.amount);
    }

    private void stock(int amount) { tank.setFluid(amount == 0 ? null : new FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, amount)); }
    private int fill(FluidStack offered, boolean execute) throws Exception {
        Method method = type.getMethod("fill", net.minecraft.util.EnumFacing.class, FluidStack.class, boolean.class);
        return ((Number) method.invoke(boiler, null, offered, execute)).intValue();
    }
    private FluidStack drain(FluidStack request, boolean execute) throws Exception {
        return (FluidStack) type.getMethod("drain", net.minecraft.util.EnumFacing.class, FluidStack.class, boolean.class)
                .invoke(boiler, null, request, execute);
    }
    private FluidStack drain(int amount, boolean execute) throws Exception {
        return (FluidStack) type.getMethod("drain", net.minecraft.util.EnumFacing.class, int.class, boolean.class)
                .invoke(boiler, null, amount, execute);
    }
}
