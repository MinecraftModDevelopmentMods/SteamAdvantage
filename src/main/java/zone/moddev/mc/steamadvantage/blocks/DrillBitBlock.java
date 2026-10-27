package zone.moddev.mc.steamadvantage.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
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
import net.minecraft.item.ItemStack;
import zone.moddev.mc.steamadvantage.init.Power;

/** Hidden moving drill helper. It deliberately has no item form or drops. */
public class DrillBitBlock extends Block implements ITileEntityProvider {
    private static final VoxelShape SHAPE = VoxelShapes.create(0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D);

    public DrillBitBlock() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5.0F, 2000.0F).notSolid());
    }

    @Override public TileEntity createNewTileEntity(IBlockReader world) { return new DrillBitTileEntity(); }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return SHAPE; }
    @Override public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return SHAPE; }
    @Override public List<ItemStack> getDrops(BlockState state, LootContext.Builder context) { return Collections.emptyList(); }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity victim) {
        victim.attackEntityFrom(Power.machine_damage, 2.0F);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tile = world.getTileEntity(pos);
            if (!world.isRemote && tile instanceof DrillBitTileEntity) {
                ((DrillBitTileEntity) tile).destroyLine(false);
            }
        }
        super.onReplaced(state, world, pos, newState, moving);
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
        destroyNeighbors(world, pos);
        super.onExplosionDestroy(world, pos, explosion);
    }

    private void destroyNeighbors(World world, BlockPos pos) {
        if (world.isRemote) return;
        for (Direction side : Direction.values()) {
            TileEntity tile = world.getTileEntity(pos.offset(side));
            if (tile instanceof DrillBitTileEntity) ((DrillBitTileEntity) tile).destroyLine();
        }
    }
}
