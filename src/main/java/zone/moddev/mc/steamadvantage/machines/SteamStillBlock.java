package zone.moddev.mc.steamadvantage.machines;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PoweredEntity;
import zone.moddev.mc.poweradvantage.conduitnetwork.ConduitRegistry;
import zone.moddev.mc.poweradvantage.init.Fluids;
import zone.moddev.mc.steamadvantage.init.Power;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;


/**
 * The coal boiler is a bit more complicated because it consumes fluids (water) and produces steam, 
 * making it a multi-type powered block.
 * @author DrCyano
 *
 */
public class SteamStillBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerMachine{

	
	public SteamStillBlock() {
		super(Material.PISTON, 0.75f, Power.steam_power, Fluids.fluidConduit_general);
	}

	@Override
	public PoweredEntity createNewTileEntity(net.minecraft.world.IBlockReader world) {
		return new SteamStillTileEntity();
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState bs) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState bs, World world, BlockPos coord) {
		if(world.getTileEntity(coord) instanceof SteamStillTileEntity){
			return ((SteamStillTileEntity)world.getTileEntity(coord)).getComparatorOutput();
		}
		return 0;
	}

	
	
	///// Overrides to make this a multi-type block /////


	@Override
	public boolean isPowerSink(ConduitType type){
		return true;
	}

	@Override
	public boolean isPowerSource(ConduitType type){
		return Fluids.isFluidType(type);
	}

	///// end multi-type overrides /////
}
