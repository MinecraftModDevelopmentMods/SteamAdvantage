package com.mcmoddev.steamadvantage.machines;

import com.mcmoddev.steamadvantage.init.Power;

import com.mcmoddev.poweradvantage.api.ConduitType;
import com.mcmoddev.poweradvantage.api.PoweredEntity;
import com.mcmoddev.poweradvantage.conduitnetwork.ConduitRegistry;
import com.mcmoddev.poweradvantage.init.Fluids;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;



public class ElectricBoilerBlock  extends com.mcmoddev.poweradvantage.api.simple.BlockSimplePowerMachine{

	
	public ElectricBoilerBlock() {
		super(Material.PISTON, 0.75f, Power.steam_power, ElectricBoilerTileEntity.ELECTRIC_POWER, Fluids.fluidConduit_general);
	}

	@Override
	public PoweredEntity createNewTileEntity(World world, int metaDataValue) {
		return new ElectricBoilerTileEntity();
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState bs) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState bs, World world, BlockPos coord) {
		if(world.getTileEntity(coord) instanceof ElectricBoilerTileEntity){
			return ((ElectricBoilerTileEntity)world.getTileEntity(coord)).getComparatorOutput();
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


	///// Overrides to make this a multi-type block /////
	
	/**
	 * This method is called whenever the block is placed into the world
	 */
	@Override
	public void onBlockAdded(World w, BlockPos coord, IBlockState state){
		super.onBlockAdded(w, coord, state);
		ConduitRegistry.getInstance().conduitBlockPlacedEvent(w, w.provider.getDimension(), coord, ElectricBoilerTileEntity.ELECTRIC_POWER);
		ConduitRegistry.getInstance().conduitBlockPlacedEvent(w, w.provider.getDimension(), coord, com.mcmoddev.poweradvantage.init.Fluids.fluidConduit_general);
	}
	
	/**
	 * This method is called when the block is removed from the world by an entity.
	 */
	@Override
	public void onPlayerDestroy(World w, BlockPos coord, IBlockState state){
		super.onPlayerDestroy(w, coord, state);
		ConduitRegistry.getInstance().conduitBlockPlacedEvent(w, w.provider.getDimension(), coord, ElectricBoilerTileEntity.ELECTRIC_POWER);
		ConduitRegistry.getInstance().conduitBlockPlacedEvent(w, w.provider.getDimension(), coord, com.mcmoddev.poweradvantage.init.Fluids.fluidConduit_general);
	}
	/**
	 * This method is called when the block is destroyed by an explosion.
	 */
	@Override
	public void onExplosionDestroy(World w, BlockPos coord, Explosion boom){
		super.onExplosionDestroy(w, coord, boom);
		ConduitRegistry.getInstance().conduitBlockPlacedEvent(w, w.provider.getDimension(), coord, ElectricBoilerTileEntity.ELECTRIC_POWER);
		ConduitRegistry.getInstance().conduitBlockPlacedEvent(w, w.provider.getDimension(), coord, com.mcmoddev.poweradvantage.init.Fluids.fluidConduit_general);
	}

	
	///// end multi-type overrides /////
}