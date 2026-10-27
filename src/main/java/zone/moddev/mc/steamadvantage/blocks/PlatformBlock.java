package zone.moddev.mc.steamadvantage.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import zone.moddev.mc.steamadvantage.init.Blocks;

/** Hidden elevator platform segments. HEIGHT preserves the legacy metadata values 0-4. */
public class PlatformBlock extends Block {
    public static final IntegerProperty HEIGHT = IntegerProperty.create("height", 0, 4);
    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            VoxelShapes.create(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D),
            VoxelShapes.create(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D),
            VoxelShapes.create(0.0D, 0.0D, 0.0D, 1.0D, 0.50D, 1.0D),
            VoxelShapes.create(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D),
            VoxelShapes.fullCube()
    };

    public PlatformBlock() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5.0F, 2000.0F).notSolid());
        setDefaultState(getStateContainer().getBaseState().with(HEIGHT, 1));
    }

    @Override protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) { builder.add(HEIGHT); }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return SHAPES[state.get(HEIGHT)]; }
    @Override public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return SHAPES[state.get(HEIGHT)]; }
    @Override public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos) { return false; }
    @Override public List<ItemStack> getDrops(BlockState state, LootContext.Builder context) { return Collections.emptyList(); }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (!canBlockStay(world, pos)) world.destroyBlock(pos, true);
    }

    public boolean canBlockStay(World world, BlockPos pos) {
        Block below = world.getBlockState(pos.down()).getBlock();
        return below == this || below == Blocks.steam_elevator_platform || below == Blocks.steam_elevator;
    }
}
