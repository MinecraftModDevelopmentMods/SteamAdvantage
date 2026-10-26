package zone.moddev.mc.steamadvantage.machines;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PoweredEntity;
import zone.moddev.mc.steamadvantage.init.Power;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


/**
 * @author DrCyano
 *
 */
public class BlastFurnaceBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerMachine{

	
	public BlastFurnaceBlock() {
		super(Material.PISTON, 0.75f, Power.steam_power);
	}

	@Override
	public PoweredEntity createNewTileEntity(net.minecraft.world.IBlockReader world) {
		return new BlastFurnaceTileEntity();
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState bs) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState bs, World world, BlockPos coord) {
		if(world.getTileEntity(coord) instanceof BlastFurnaceTileEntity){
			return ((BlastFurnaceTileEntity)world.getTileEntity(coord)).getComparatorOutput();
		}
		return 0;
	}


	@Override
	public boolean isPowerSink(ConduitType powerType) {
		return ConduitType.areSameType(Power.steam_power,powerType);
	}

	@Override
	public boolean isPowerSource(ConduitType powerType) {
		return false;
	}

}
