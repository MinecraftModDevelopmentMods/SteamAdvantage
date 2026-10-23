package zone.moddev.mc.steamadvantage.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import zone.moddev.mc.steamadvantage.init.Power;

/** Hidden moving drill helper. It deliberately has no item form or drops. */
public class DrillBitBlock extends Block implements ITileEntityProvider {
    private static final VoxelShape SHAPE = VoxelShapes.create(0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D);

    public DrillBitBlock() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5.0F, 2000.0F));
    }

    @Override public TileEntity createNewTileEntity(IBlockReader world) { return new DrillBitTileEntity(); }
    @Override public VoxelShape getShape(IBlockState state, IBlockReader world, BlockPos pos) { return SHAPE; }
    @Override public VoxelShape getCollisionShape(IBlockState state, IBlockReader world, BlockPos pos) { return SHAPE; }
    @Override public boolean isNormalCube(IBlockState state) { return false; }
    @Override public boolean isSolid(IBlockState state) { return false; }
    @Override public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {}

    @Override
    public void onEntityCollision(IBlockState state, World world, BlockPos pos, Entity victim) {
        victim.attackEntityFrom(Power.machine_damage, 2.0F);
    }

    @Override
    public void onReplaced(IBlockState state, World world, BlockPos pos, IBlockState newState, boolean moving) {
        if (state.getBlock() != newState.getBlock()) destroyNeighbors(world, pos);
        super.onReplaced(state, world, pos, newState, moving);
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
        destroyNeighbors(world, pos);
        super.onExplosionDestroy(world, pos, explosion);
    }

    private void destroyNeighbors(World world, BlockPos pos) {
        if (world.isRemote) return;
        for (EnumFacing side : EnumFacing.values()) {
            TileEntity tile = world.getTileEntity(pos.offset(side));
            if (tile instanceof DrillBitTileEntity) ((DrillBitTileEntity) tile).destroyLine();
        }
    }
}
