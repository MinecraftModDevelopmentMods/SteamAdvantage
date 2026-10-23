package zone.moddev.mc.steamadvantage.machines;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PoweredEntity;
import zone.moddev.mc.poweradvantage.conduitnetwork.ConduitRegistry;
import zone.moddev.mc.poweradvantage.init.Fluids;
import zone.moddev.mc.steamadvantage.init.Blocks;
import zone.moddev.mc.steamadvantage.init.Power;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;


/**
 * The coal boiler is a bit more complicated because it consumes fluids (water) and produces steam, 
 * making it a multi-type powered block.
 * @author DrCyano
 *
 */
public class SteamPumpBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerMachine{

	
	public SteamPumpBlock() {
		super(Material.PISTON, 0.75f, Power.steam_power, Fluids.fluidConduit_general);
	}

	@Override
	public PoweredEntity createNewTileEntity(net.minecraft.world.IBlockReader world) {
		return new SteamPumpTileEntity();
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState bs) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState bs, World world, BlockPos coord) {
		if(world.getTileEntity(coord) instanceof SteamPumpTileEntity){
			return ((SteamPumpTileEntity)world.getTileEntity(coord)).getComparatorOutput();
		}
		return 0;
	}

	
	
	///// Overrides to make this a multi-type block /////
	@Override
	public boolean isPowerSink(ConduitType type){
		return ConduitType.areSameType(Power.steam_power, type);
	}

	@Override
	public boolean isPowerSource(ConduitType type){
		return !ConduitType.areSameType(Power.steam_power, type);
	}
	@Override
	public void onReplaced(IBlockState state, World w, BlockPos coord, IBlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) destroyPipe(w, coord);
		super.onReplaced(state, w, coord, newState, isMoving);
	}

	@Override
	public void onExplosionDestroy(World w, BlockPos coord, Explosion boom){
		destroyPipe(w, coord);
		super.onExplosionDestroy(w, coord, boom);
	}
	
	
	private void destroyPipe(World w, BlockPos coord) {
		if(w.isRemote) return;
		// destroy connected drill bits
		BlockPos c = coord.down();
		while(c.getY() > 0 && w.getBlockState(c).getBlock() == Blocks.pump_pipe_steam){
			w.removeBlock(c);
			c = c.down();
		}
	}


	
	///// end multi-type overrides /////
}
