package zone.moddev.mc.steamadvantage.resources;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.EmptyBlockReader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import zone.moddev.mc.steamadvantage.blocks.PlatformBlock;
import zone.moddev.mc.steamadvantage.blocks.SteamTrackBlock;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class NativeBlockPropertiesTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {
                {"steam_track", -1}, {"platform_height_0", 0}, {"platform_height_1", 1},
                {"platform_height_2", 2}, {"platform_height_3", 3}, {"platform_height_4", 4}
        });
    }

    @Parameterized.Parameter public String name;
    @Parameterized.Parameter(1) public int height;
    private Block block;

    @BeforeClass public static void bootstrapNativeRegistries() { Bootstrap.register(); }

    @Before public void createNativeBlockAndStateCaches() {
        block = height < 0 ? new SteamTrackBlock() : new PlatformBlock();
        for (BlockState state : block.getStateContainer().getValidStates()) state.cacheState();
    }

    @Test public void remainsNonSolid() {
        for (BlockState state : states()) assertFalse(state.toString(), state.isSolid());
    }

    @Test public void remainsNonOpaque() {
        for (BlockState state : states()) {
            assertFalse(state.toString(), state.isOpaqueCube(EmptyBlockReader.INSTANCE, BlockPos.ZERO));
        }
    }

    @Test public void retainsNativeOpacityForTheLegacyShape() {
        // Native opacity is 1, not 0, for a non-solid block with a full-cube outline.
        int expected = height < 0 || height == 4 ? 1 : 0;
        for (BlockState state : states()) {
            assertEquals(state.toString(), expected, state.getOpacity(EmptyBlockReader.INSTANCE, BlockPos.ZERO));
        }
    }

    @Test public void remainsANonNormalCube() {
        for (BlockState state : states()) {
            assertFalse(state.toString(), state.isNormalCube(EmptyBlockReader.INSTANCE, BlockPos.ZERO));
        }
    }

    @Test public void doesNotConductWeakPowerOnAnyFace() {
        for (BlockState state : states()) {
            for (Direction face : Direction.values()) {
                // The native hook delegates to isNormalCube; these constant shapes need no world lookup.
                assertFalse(state + " face=" + face, state.shouldCheckWeakPower(null, BlockPos.ZERO, face));
            }
        }
    }

    @Test public void preservesOutlineAndCollisionShapes() {
        VoxelShape expected = height < 0 || height == 4 ? VoxelShapes.fullCube()
                : height == 0 ? VoxelShapes.create(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D)
                : VoxelShapes.create(0.0D, 0.0D, 0.0D, 1.0D, height * 0.25D, 1.0D);
        if (height < 0) assertEquals(64, states().size());
        for (BlockState state : states()) {
            assertFalse(state + " outline", VoxelShapes.compare(expected,
                    state.getShape(EmptyBlockReader.INSTANCE, BlockPos.ZERO), IBooleanFunction.NOT_SAME));
            assertFalse(state + " collision", VoxelShapes.compare(expected,
                    state.getCollisionShape(EmptyBlockReader.INSTANCE, BlockPos.ZERO), IBooleanFunction.NOT_SAME));
        }
    }

    private Collection<BlockState> states() {
        return height < 0 ? block.getStateContainer().getValidStates()
                : Collections.singletonList(block.getDefaultState().with(PlatformBlock.HEIGHT, height));
    }
}
