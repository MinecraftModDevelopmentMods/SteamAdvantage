package com.mcmoddev.steamadvantage.machines;

import net.minecraftforge.fluids.FluidStack;
import java.lang.reflect.Method;
import net.minecraftforge.fluids.FluidRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import com.mcmoddev.poweradvantage.registry.still.recipe.DistillationRecipe;
import com.mcmoddev.poweradvantage.registry.still.recipe.ExplicitFluidReference;
import static org.junit.Assert.*;

/** Pure capacity planning; native operating regressions live in SteamOperationalCases. */
public class SteamOperationalStillCapacityTest {
    @BeforeClass public static void bootstrap() {
        net.minecraft.init.Bootstrap.register();
    }

    @Test public void fullOutputCannotPlanOrDebitAnyInput() throws Exception {
        SteamStillTileEntity still = still(1000);
        FluidStack input = new FluidStack(FluidRegistry.WATER, 200);
        assertEquals(0, plan(still, recipe(2, 1), input));
        assertEquals(200, input.amount);
        assertEquals(1000, still.getOutputTank().getFluidAmount());
    }

    @Test public void oneOutputSpacePlansOnlyOneApplicationWithoutMutatingEitherTank() throws Exception {
        SteamStillTileEntity still = still(999);
        FluidStack input = new FluidStack(FluidRegistry.WATER, 200);
        assertEquals(1, plan(still, recipe(2, 1), input));
        assertEquals(200, input.amount);
        assertEquals(999, still.getOutputTank().getFluidAmount());
    }

    @Test public void normalSpeedPlansTwoApplicationsWhenBothProductsFit() throws Exception {
        SteamStillTileEntity still = still(998);
        FluidStack input = new FluidStack(FluidRegistry.WATER, 200);
        assertEquals(2, plan(still, recipe(2, 1), input));
        assertEquals(200, input.amount);
        assertEquals(998, still.getOutputTank().getFluidAmount());
    }

    @Test public void incompleteInputCannotPlanProduct() throws Exception {
        SteamStillTileEntity still = still(0);
        FluidStack input = new FluidStack(FluidRegistry.WATER, 1);
        assertEquals(0, plan(still, recipe(2, 1), input));
        assertEquals(1, input.amount);
        assertEquals(0, still.getOutputTank().getFluidAmount());
    }

    @Test public void mismatchedProductIsNotOverwritten() throws Exception {
        SteamStillTileEntity still = new SteamStillTileEntity();
        still.getOutputTank().setFluid(new FluidStack(FluidRegistry.WATER, 10));
        FluidStack input = new FluidStack(FluidRegistry.WATER, 200);
        assertEquals(0, plan(still, recipe(2, 1), input));
        assertEquals(200, input.amount);
        assertSame(FluidRegistry.WATER, still.getOutputTank().getFluid().getFluid());
        assertEquals(10, still.getOutputTank().getFluidAmount());
    }

    @Test public void multiMillibucketProductMustFitAsAWhole() throws Exception {
        FluidStack input = new FluidStack(FluidRegistry.WATER, 200);
        assertEquals(0, plan(still(997), recipe(3, 4), input));
        assertEquals(1, plan(still(996), recipe(3, 4), input));
        assertEquals(2, plan(still(992), recipe(3, 4), input));
        assertEquals(200, input.amount);
    }

    private static SteamStillTileEntity still(int output) {
        SteamStillTileEntity still = new SteamStillTileEntity();
        if (output > 0) still.getOutputTank().setFluid(new FluidStack(FluidRegistry.LAVA, output));
        return still;
    }

    private static DistillationRecipe recipe(int in, int out) {
        return new DistillationRecipe(new ExplicitFluidReference(FluidRegistry.WATER), in,
                new ExplicitFluidReference(FluidRegistry.LAVA), out);
    }

    private static int plan(SteamStillTileEntity still, DistillationRecipe recipe, FluidStack input) throws Exception {
        Method method = SteamStillTileEntity.class.getDeclaredMethod("distillationApplications",
                DistillationRecipe.class, FluidStack.class);
        method.setAccessible(true);
        return (Integer) method.invoke(still, recipe, input);
    }
}
