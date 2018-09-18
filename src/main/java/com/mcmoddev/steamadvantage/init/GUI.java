package com.mcmoddev.steamadvantage.init;

import com.mcmoddev.lib.init.Materials;
import com.mcmoddev.steamadvantage.SteamAdvantage;
import com.mcmoddev.steamadvantage.data.SteamAdvantageNames;
import com.mcmoddev.steamadvantage.gui.*;
import com.mcmoddev.poweradvantage.api.GUIBlock;

public class GUI {

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		((GUIBlock)Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_BOILER_COAL))
		.setGuiID(GUIRegistry.addGUI(new CoalBoilerGUI()), SteamAdvantage.getInstance());
		((GUIBlock)Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_TANK))
		.setGuiID(GUIRegistry.addGUI(new SteamTankGUI()),SteamAdvantage.getInstance());
		((GUIBlock)Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_FURNACE))
		.setGuiID(GUIRegistry.addGUI(new BlastFurnaceGUI()),SteamAdvantage.getInstance());
		((GUIBlock)Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_CRUSHER))
		.setGuiID(GUIRegistry.addGUI(new RockCrusherGUI()),SteamAdvantage.getInstance());
		((GUIBlock)Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_DRILL))
		.setGuiID(GUIRegistry.addGUI(new SteamDrillGUI()),SteamAdvantage.getInstance());
		((GUIBlock)Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_BOILER_ELECTRIC))
		.setGuiID(GUIRegistry.addGUI(new ElectricBoilerGUI()),SteamAdvantage.getInstance());
		((GUIBlock)Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_BOILER_GEOTHERMAL))
		.setGuiID(GUIRegistry.addGUI(new GeothermalBoilerGUI()),SteamAdvantage.getInstance());
		((GUIBlock)Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_BOILER_OIL))
		.setGuiID(GUIRegistry.addGUI(new OilBoilerGUI()),SteamAdvantage.getInstance());
		((GUIBlock)Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_STILL))
		.setGuiID(GUIRegistry.addGUI(new SteamStillGUI()),SteamAdvantage.getInstance());
		((GUIBlock)Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_PUMP))
		.setGuiID(GUIRegistry.addGUI(new SteamPumpGUI()),SteamAdvantage.getInstance());

		
		initDone = true;
	}
}
