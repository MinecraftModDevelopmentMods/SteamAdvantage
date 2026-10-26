package zone.moddev.mc.steamadvantage.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/** Hidden pipe shaft placed beneath a steam pump. */
public class PumpPipeBlock extends Block {
    private static final VoxelShape SHAPE = VoxelShapes.create(0.0D, 0.25D, 0.0D, 0.75D, 1.0D, 1.0D);

    public PumpPipeBlock() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5.0F, 10.0F).notSolid());
    }

    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return SHAPE; }
    @Override public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return SHAPE; }
    @Override public List<ItemStack> getDrops(BlockState state, LootContext.Builder context) { return Collections.emptyList(); }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moving) {
        if (state.getBlock() != newState.getBlock()) destroyShaft(world, pos);
        super.onReplaced(state, world, pos, newState, moving);
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
        destroyShaft(world, pos);
        super.onExplosionDestroy(world, pos, explosion);
    }

    private void destroyShaft(World world, BlockPos pos) {
        if (world.isRemote) return;
        BlockPos cursor = pos.down();
        while (cursor.getY() > 0 && world.getBlockState(cursor).getBlock() == this) {
            world.destroyBlock(cursor, false);
            cursor = cursor.down();
        }
    }
}
