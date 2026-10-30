package zone.moddev.mc.steamadvantage.machines;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import static org.junit.Assert.*;

public class SteamOperationalPumpSourceTest {
    private static final BlockPos SOURCE = new BlockPos(8, 80, 8);

    @BeforeClass public static void bootstrap() {
        net.minecraft.util.registry.Bootstrap.register();
    }

    @Test public void nativePickupRetainsTheSlabAndOnlyClearsItsWater() {
        BlockState wet = Blocks.OAK_SLAB.getDefaultState().with(BlockStateProperties.WATERLOGGED, true);
        PickupWorld world = new PickupWorld(wet);
        assertSame(Fluids.WATER, pickup(world.world, SOURCE, wet));
        assertEquals(1, world.writes);
        assertEquals(wet.with(BlockStateProperties.WATERLOGGED, false), world.state);
        assertSame(Fluids.EMPTY, pickup(world.world, SOURCE, world.state));
        assertEquals("A dry host cannot produce another bucket", 1, world.writes);
    }

    @Test public void nativePickupConsumesOrdinaryWaterAndLavaSources() {
        for (Block block : new Block[]{Blocks.WATER, Blocks.LAVA}) {
            BlockState source = block.getDefaultState();
            PickupWorld world = new PickupWorld(source);
            assertSame(source.getFluidState().getFluid(), pickup(world.world, SOURCE, source));
            assertEquals(1, world.writes);
            assertEquals(Blocks.AIR.getDefaultState(), world.state);
        }
    }

    @Test public void unsupportedModSourceIsNeverReplacedOrCredited() {
        Block host = new Block(Block.Properties.create(Material.ROCK)) {
            @Override public IFluidState getFluidState(BlockState state) { return Fluids.WATER.getDefaultState(); }
        };
        BlockState source = host.getDefaultState();
        assertTrue(source.getFluidState().isSource());
        PickupWorld world = new PickupWorld(source);
        assertSame(Fluids.EMPTY, pickup(world.world, SOURCE, source));
        assertSame(source, world.state);
        assertEquals(0, world.writes);
    }

    @Test public void flowingAndRefusedSourcesCannotProduceABucket() {
        BlockState flowing = Blocks.WATER.getDefaultState().with(FlowingFluidBlock.LEVEL, 1);
        PickupWorld world = new PickupWorld(flowing);
        assertSame(Fluids.EMPTY, pickup(world.world, SOURCE, flowing));
        assertEquals(0, world.writes);
        RefusingSource host = new RefusingSource();
        BlockState refused = host.getDefaultState();
        PickupWorld refusedWorld = new PickupWorld(refused);
        assertSame(Fluids.EMPTY, pickup(refusedWorld.world, SOURCE, refused));
        assertSame(refused, refusedWorld.state);
        assertEquals(0, refusedWorld.writes);
    }

    @Test public void pumpUsesNativePickupBeforeCreditWithoutDestructiveFallback() throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = getClass().getResourceAsStream(
                "/zone/moddev/mc/steamadvantage/machines/SteamPumpTileEntity.class")) {
            assertNotNull(input);
            new ClassReader(input).accept(node, 0);
        }
        MethodNode tick = method(node, "tickUpdate"), pickup = method(node, "pickupSource");
        int extraction = -1, fill = -1, index = 0;
        boolean nativePickup = false, nativeGuard = false;
        for (MethodNode candidate : new MethodNode[]{tick, pickup}) {
            for (AbstractInsnNode instruction : candidate.instructions.toArray()) {
                if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode field = (FieldInsnNode) instruction;
                    assertFalse("Caller must not erase waterlogged hosts with AIR",
                            field.getOpcode() == Opcodes.GETSTATIC && field.owner.equals("net/minecraft/block/Blocks")
                                    && field.name.equals("AIR"));
                } else if (instruction instanceof TypeInsnNode) {
                    TypeInsnNode type = (TypeInsnNode) instruction;
                    if (type.getOpcode() == Opcodes.INSTANCEOF && type.desc.equals("net/minecraft/block/IBucketPickupHandler")) nativeGuard = true;
                } else if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    assertFalse("No destructive fallback when native pickup is unavailable",
                            call.name.equals("removeBlock") || call.name.equals("destroyBlock"));
                    if (candidate == pickup) {
                        assertFalse("Pickup policy delegates block mutation to the native handler",
                                call.name.equals("setBlockState") || call.name.equals("setBlockState"));
                        if (call.owner.equals("net/minecraft/block/IBucketPickupHandler") && call.name.equals("pickupFluid")) nativePickup = true;
                    } else {
                        if (call.name.equals("pickupSource")) extraction = index;
                        if (call.owner.equals("cyano/poweradvantage/api/fluid/FluidTank") && call.name.equals("fill")) fill = index;
                    }
                }
                index++;
            }
        }
        assertTrue("Unsupported blocks are rejected before native pickup", nativeGuard && nativePickup);
        assertTrue("Successful native pickup precedes the final bucket credit", extraction >= 0 && fill > extraction);
    }

    @Test public void rejectedNeighborFillDoesNotDrainOneMillibucket() throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = getClass().getResourceAsStream(
                "/zone/moddev/mc/steamadvantage/machines/SteamPumpTileEntity.class")) {
            assertNotNull(input);
            new ClassReader(input).accept(node, 0);
        }
        boolean afterFill = false, guarded = false, drained = false;
        for (AbstractInsnNode instruction : method(node, "powerUpdate").instructions.toArray()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.owner.equals("cyano/poweradvantage/api/fluid/IAdvantageFluidHandler")
                        && call.name.equals("fill")) afterFill = true;
                if (!afterFill) continue;
                assertFalse("A zero acceptance must not be rounded up into a one-millibucket loss",
                        call.owner.equals("java/lang/Math") && call.name.equals("max")
                                && call.desc.equals("(II)I"));
                if (call.owner.equals("cyano/poweradvantage/api/fluid/FluidTank") && call.name.equals("drain")) {
                    assertTrue("Reject nonpositive acceptance before debiting the tank", guarded);
                    drained = true;
                    break;
                }
            } else if (afterFill && instruction instanceof org.objectweb.asm.tree.JumpInsnNode
                    && instruction.getOpcode() == Opcodes.IFLE) guarded = true;
        }
        assertTrue("The direct-neighbor transfer path is covered", afterFill && drained);
    }

    private static Fluid pickup(IWorld world, BlockPos pos, BlockState state) {
        try {
            java.lang.reflect.Method method = SteamPumpTileEntity.class.getDeclaredMethod(
                    "pickupSource", IWorld.class, BlockPos.class, BlockState.class);
            method.setAccessible(true);
            return (Fluid) method.invoke(null, world, pos, state);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Pump must delegate source pickup without destroying the host", failure);
        }
    }

    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(method -> method.name.equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("Missing pump method " + name));
    }

    private static final class RefusingSource extends Block implements IBucketPickupHandler {
        private RefusingSource() { super(Block.Properties.create(Material.ROCK)); }
        @Override public IFluidState getFluidState(BlockState state) { return Fluids.WATER.getDefaultState(); }
        @Override public Fluid pickupFluid(IWorld world, BlockPos pos, BlockState state) { return Fluids.EMPTY; }
    }

    private static final class PickupWorld {
        private BlockState state;
        private int writes;
        private final IWorld world;

        private PickupWorld(BlockState initial) {
            state = initial;
            world = (IWorld) Proxy.newProxyInstance(IWorld.class.getClassLoader(), new Class<?>[]{IWorld.class},
                    (proxy, method, arguments) -> {
                        if (method.getName().equals("setBlockState")) {
                            assertEquals(SOURCE, arguments[0]);
                            state = (BlockState) arguments[1];
                            writes++;
                            return true;
                        }
                        throw new AssertionError("Unexpected native pickup world call: " + method.getName());
                    });
        }
    }
}
