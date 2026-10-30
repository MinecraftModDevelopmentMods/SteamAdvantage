package zone.moddev.mc.steamadvantage.machines;

import java.io.InputStream;
import java.lang.reflect.Method;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import static org.junit.Assert.*;

/** Forge drain-contract checks; real World pickup and transfer run in the packaged rig. */
public class SteamOperationalPumpSourceTest {
    private static final BlockPos SOURCE = new BlockPos(8, 80, 8);

    @BeforeClass public static void bootstrap() {
        net.minecraft.init.Bootstrap.register();
    }

    @Test public void forgeSourceOwnsItsDrainAndProvidesOneExactBucket() {
        DrainSource source = new DrainSource(1000, true);
        FluidStack collected = pickup(source.getDefaultState());
        assertNotNull(collected);
        assertSame(FluidRegistry.WATER, collected.getFluid());
        assertEquals(1000, collected.amount);
        assertTrue("Check the source's own drain permission", source.permissionChecks > 0);
        assertEquals("The real drain happens exactly once", 1, source.realDrains);
        assertNull("The source, not a generic block deletion, consumes its fluid", source.stored);
    }

    @Test public void refusedForgeSourceIsNotDrainedOrCredited() {
        DrainSource source = new DrainSource(1000, false);
        assertNull(pickup(source.getDefaultState()));
        assertEquals(0, source.realDrains);
        assertEquals(0, source.previews);
        assertEquals(1000, source.stored.amount);
    }

    @Test public void flowingVanillaAndUnsupportedSolidCannotSupplyABucket() {
        assertNull(pickup(Blocks.WATER.getDefaultState().withProperty(BlockLiquid.LEVEL, 1)));
        assertNull(pickup(Blocks.FLOWING_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL, 1)));
        assertNull(pickup(Blocks.STONE.getDefaultState()));
    }

    @Test public void partialForgeDrainKeepsActualQuantityAndFluidNbt() {
        DrainSource source = new DrainSource(375, true);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("AdvWorksWitness", "partial-forge-source");
        source.stored.tag = tag;
        FluidStack collected = pickup(source.getDefaultState());
        assertNotNull(collected);
        assertEquals("Never substitute an assumed 1000 mB for the native drain result", 375, collected.amount);
        assertEquals(tag, collected.tag);
        assertEquals(1, source.realDrains);
        assertNull(source.stored);
    }

    @Test public void successfulExtractionPrecedesCreditAndSteamCharge() throws Exception {
        ClassNode node = pump();
        MethodNode tick = method(node, "tickUpdate");
        MethodNode pickup = method(node, "pickupSource");
        int extraction = -1, credit = -1, charge = -1;
        boolean nativeDrain = false, permission = false;
        for (AbstractInsnNode instruction : tick.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            assertFalse("The tick caller must not erase a Forge source instead of draining it",
                    call.name.equals("setBlockToAir") || call.name.equals("destroyBlock"));
            int index = tick.instructions.indexOf(instruction);
            if (call.name.equals("pickupSource")) extraction = index;
            if (call.owner.equals("net/minecraftforge/fluids/FluidTank") && call.name.equals("fill")) credit = index;
            if (call.name.equals("subtractEnergy")) charge = index;
        }
        for (AbstractInsnNode instruction : pickup.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.owner.equals("net/minecraftforge/fluids/IFluidBlock")) {
                if (call.name.equals("canDrain")) permission = true;
                if (call.name.equals("drain")) nativeDrain = true;
            }
        }
        assertTrue("Use the Forge source's native permission and drain contract", permission && nativeDrain);
        assertTrue("A successful pickup precedes the final tank credit", extraction >= 0 && credit > extraction);
        assertTrue("A successful pickup precedes its steam charge", charge > extraction);
    }

    @Test public void rejectedNeighborFillDoesNotDrainOneMillibucket() throws Exception {
        boolean afterFill = false, guarded = false, drained = false;
        for (AbstractInsnNode instruction : method(pump(), "powerUpdate").instructions.toArray()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.owner.equals("net/minecraftforge/fluids/IFluidHandler")
                        && call.name.equals("fill")) afterFill = true;
                if (!afterFill) continue;
                assertFalse("A refused fill must not become a one-millibucket loss",
                        call.owner.equals("java/lang/Math") && call.name.equals("max") && call.desc.equals("(II)I"));
                if (call.owner.equals("net/minecraftforge/fluids/FluidTank") && call.name.equals("drain")) {
                    assertTrue("Reject nonpositive acceptance before draining", guarded);
                    drained = true;
                    break;
                }
            } else if (afterFill && instruction instanceof JumpInsnNode && instruction.getOpcode() == Opcodes.IFLE) guarded = true;
        }
        assertTrue("The direct-neighbor transfer path is covered", afterFill && drained);
    }

    private static FluidStack pickup(IBlockState state) {
        try {
            Method method = SteamPumpTileEntity.class.getDeclaredMethod("pickupSource", World.class, BlockPos.class, IBlockState.class);
            method.setAccessible(true);
            // The fake Forge block owns all state and rejects any replacement through World.
            return (FluidStack) method.invoke(null, null, SOURCE, state);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Pump pickup must honor native drain results", failure);
        }
    }

    private static ClassNode pump() throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = SteamOperationalPumpSourceTest.class.getResourceAsStream(
                "/zone/moddev/mc/steamadvantage/machines/SteamPumpTileEntity.class")) {
            assertNotNull(input);
            new ClassReader(input).accept(node, 0);
        }
        return node;
    }

    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(method -> method.name.equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("Missing pump method " + name));
    }

    private static final class DrainSource extends Block implements IFluidBlock {
        private FluidStack stored;
        private final boolean allowed;
        private int permissionChecks, previews, realDrains;

        private DrainSource(int amount, boolean allowed) {
            super(Material.WATER);
            stored = new FluidStack(FluidRegistry.WATER, amount);
            this.allowed = allowed;
        }

        @Override public Fluid getFluid() { return FluidRegistry.WATER; }
        @Override public FluidStack drain(World world, BlockPos pos, boolean real) {
            assertNull("This unit source owns its state; no World is needed", world);
            assertEquals(SOURCE, pos);
            assertTrue("Drain permission must be checked", allowed);
            assertNotNull("The source cannot be drained twice", stored);
            FluidStack result = stored.copy();
            if (real) {
                realDrains++;
                stored = null;
            } else previews++;
            return result;
        }
        @Override public boolean canDrain(World world, BlockPos pos) {
            permissionChecks++;
            assertEquals(SOURCE, pos);
            return allowed && stored != null;
        }
        @Override public float getFilledPercentage(World world, BlockPos pos) {
            return stored == null ? 0 : stored.amount / 1000.0F;
        }
    }
}
