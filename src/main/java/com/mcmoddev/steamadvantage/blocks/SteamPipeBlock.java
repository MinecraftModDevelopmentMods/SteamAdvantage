package com.mcmoddev.steamadvantage.blocks;

import com.mcmoddev.steamadvantage.init.Power;

import net.minecraft.block.material.Material;

public class SteamPipeBlock extends com.mcmoddev.poweradvantage.api.simple.BlockSimplePowerConduit{

	public SteamPipeBlock() {
		super(Material.IRON, 0.75f, 2f/16f, Power.steam_power);
	}
	
}
