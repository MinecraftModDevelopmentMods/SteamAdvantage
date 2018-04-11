package cyano.steamadvantage.init;

import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.gui.*;

public class GUI {

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Blocks.init();
		Entities.init();

		Blocks.steam_boiler_coal.setGuiID(GUIRegistry.addGUI(new CoalBoilerGUI()), SteamAdvantage.getInstance());
		Blocks.steam_tank.setGuiID(GUIRegistry.addGUI(new SteamTankGUI()),SteamAdvantage.getInstance());
		Blocks.steam_furnace.setGuiID(GUIRegistry.addGUI(new BlastFurnaceGUI()),SteamAdvantage.getInstance());
		Blocks.steam_crusher.setGuiID(GUIRegistry.addGUI(new RockCrusherGUI()),SteamAdvantage.getInstance());
		Blocks.steam_drill.setGuiID(GUIRegistry.addGUI(new SteamDrillGUI()),SteamAdvantage.getInstance());
		Blocks.steam_boiler_electric.setGuiID(GUIRegistry.addGUI(new ElectricBoilerGUI()),SteamAdvantage.getInstance());
		Blocks.steam_boiler_geothermal.setGuiID(GUIRegistry.addGUI(new GeothermalBoilerGUI()),SteamAdvantage.getInstance());
		Blocks.steam_boiler_oil.setGuiID(GUIRegistry.addGUI(new OilBoilerGUI()),SteamAdvantage.getInstance());
		Blocks.steam_still.setGuiID(GUIRegistry.addGUI(new SteamStillGUI()),SteamAdvantage.getInstance());
		Blocks.steam_pump.setGuiID(GUIRegistry.addGUI(new SteamPumpGUI()),SteamAdvantage.getInstance());

		
		initDone = true;
	}
}
