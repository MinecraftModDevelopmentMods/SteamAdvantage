package zone.moddev.mc.steamadvantage.blocks;

import cyano.poweradvantage.api.simple.BlockSimplePowerConduit;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IBlockReader;
import zone.moddev.mc.steamadvantage.init.Power;

public class SteamTrackBlock extends BlockSimplePowerConduit {
    public SteamTrackBlock() { super(Material.PISTON, 0.75F, 2.0F / 16.0F, Power.steam_power); }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return VoxelShapes.fullCube(); }
    @Override public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return VoxelShapes.fullCube(); }
}
