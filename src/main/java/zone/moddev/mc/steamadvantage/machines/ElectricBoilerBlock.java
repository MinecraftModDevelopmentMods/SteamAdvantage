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



public class ElectricBoilerBlock  extends cyano.poweradvantage.api.simple.BlockSimplePowerMachine{


	public ElectricBoilerBlock() {
		super(Material.PISTON, 0.75f, Power.steam_power, ElectricBoilerTileEntity.ELECTRIC_POWER, Fluids.fluidConduit_general);
	}

	@Override
	public PoweredEntity newBlockEntity(net.minecraft.world.IBlockReader world) {
		return new ElectricBoilerTileEntity();
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState bs) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState bs, World world, BlockPos coord) {
		if(world.getBlockEntity(coord) instanceof ElectricBoilerTileEntity){
			return ((ElectricBoilerTileEntity)world.getBlockEntity(coord)).getComparatorOutput();
		}
		return 0;
	}

	@Override
	public boolean isPowerSink(ConduitType type){
		return !ConduitType.areSameType(Power.steam_power, type);
	}

	@Override
	public boolean isPowerSource(ConduitType type){
		return ConduitType.areSameType(Power.steam_power, type);
	}


}
