package com.mcmoddev.steamadvantage.machines;

import com.mcmoddev.steamadvantage.init.Power;

import com.mcmoddev.poweradvantage.api.ConduitType;
import com.mcmoddev.poweradvantage.api.PoweredEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


/**
 * @author DrCyano
 *
 */
public class BlastFurnaceBlock extends com.mcmoddev.poweradvantage.api.simple.BlockSimplePowerMachine{

	
	public BlastFurnaceBlock() {
		super(Material.PISTON, 0.75f, Power.steam_power);
	}

	@Override
	public PoweredEntity createNewTileEntity(World world, int metaDataValue) {
		return new BlastFurnaceTileEntity();
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState bs) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState bs, World world, BlockPos coord) {
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
