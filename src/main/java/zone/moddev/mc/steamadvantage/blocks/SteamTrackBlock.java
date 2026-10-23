package zone.moddev.mc.steamadvantage.blocks;

import cyano.poweradvantage.api.simple.BlockSimplePowerConduit;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import zone.moddev.mc.steamadvantage.init.Power;

public class SteamTrackBlock extends BlockSimplePowerConduit {
    public SteamTrackBlock() { super(Material.PISTON, 0.75F, 2.0F / 16.0F, Power.steam_power); }
    @Override public VoxelShape getShape(IBlockState state, IBlockReader world, BlockPos pos) { return VoxelShapes.fullCube(); }
    @Override public VoxelShape getCollisionShape(IBlockState state, IBlockReader world, BlockPos pos) { return VoxelShapes.fullCube(); }
    @Override public boolean isNormalCube(IBlockState state) { return false; }
    @Override public boolean isSolid(IBlockState state) { return false; }
    @OnlyIn(Dist.CLIENT) @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }
}
