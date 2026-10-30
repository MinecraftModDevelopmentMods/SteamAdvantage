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

public class GeothermalBoilerBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerMachine{

	public GeothermalBoilerBlock() {
		super(Material.PISTON, 0.75f, Power.steam_power, Fluids.fluidConduit_general);
	}

	@Override
	public PoweredEntity newBlockEntity(net.minecraft.world.IBlockReader world) {
		return new GeothermalBoilerTileEntity();
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState bs) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState bs, World world, BlockPos coord) {
		if(world.getBlockEntity(coord) instanceof GeothermalBoilerTileEntity){
			return ((GeothermalBoilerTileEntity)world.getBlockEntity(coord)).getComparatorOutput();
		}
		return 0;
	}
///// Overrides to make this a multi-type block /////
@Override
public boolean isPowerSink(ConduitType type){
	return !ConduitType.areSameType(Power.steam_power, type);
}

	@Override
	public boolean isPowerSource(ConduitType type){
		return ConduitType.areSameType(Power.steam_power, type);
	}
	///// end multi-type overrides /////

}
