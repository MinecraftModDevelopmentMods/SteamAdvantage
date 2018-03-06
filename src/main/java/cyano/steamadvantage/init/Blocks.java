package cyano.steamadvantage.init;

import cyano.poweradvantage.api.GUIBlock;
import cyano.poweradvantage.blocks.BlockPowerSwitch;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.blocks.*;
import cyano.steamadvantage.machines.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public abstract class Blocks {
	private static final Map<String,Block> allBlocks = new HashMap<>();


	public static GUIBlock steam_boiler_coal;
	public static GUIBlock steam_boiler_electric;
	public static GUIBlock steam_boiler_geothermal;
	public static GUIBlock steam_crusher;
	public static GUIBlock steam_furnace;
	public static GUIBlock steam_tank;
	public static GUIBlock steam_drill;
	public static GUIBlock steam_elevator;
	public static Block steam_elevator_platform;
	public static IBlockState[] steam_elevator_platforms;
	public static Block steam_pipe;
	public static Block drillbit;
	public static Block steam_track;

	public static Block pump_pipe_steam;
	public static Block steam_switch;
	public static GUIBlock steam_still;
	public static GUIBlock steam_pump;
	public static GUIBlock steam_boiler_oil;
	
	private static boolean initDone = false;

	@SuppressWarnings("deprecation")
	public static void init(){
		if(initDone) return;
		
		steam_pipe = addBlock(new SteamPipeBlock(),"steam_pipe");
		OreDictionary.registerOre("conduitSteam", steam_pipe);
		steam_track = addBlock(new SteamTrackBlock(),"steam_track");
		steam_boiler_coal = (GUIBlock)addBlock(new CoalBoilerBlock(),"steam_boiler_coal");
		steam_boiler_electric = (GUIBlock)addBlock(new ElectricBoilerBlock(),"steam_boiler_electric");
		steam_boiler_geothermal = (GUIBlock)addBlock(new GeothermalBoilerBlock(),"steam_boiler_geothermal");
		steam_tank = (GUIBlock)addBlock(new SteamTankBlock(),"steam_tank");
		steam_furnace = (GUIBlock)addBlock(new BlastFurnaceBlock(),"steam_furnace");
		steam_crusher = (GUIBlock)addBlock(new RockCrusherBlock(),"steam_crusher");
		steam_drill = (GUIBlock)addBlock(new SteamDrillBlock(),"steam_drill");
		drillbit = addBlock(new DrillBitBlock(),"drillbit");
		steam_elevator_platform = addBlock(new PlatformBlock(),"platform");
		steam_elevator_platforms = new IBlockState[5];
		for(int i = 0; i < steam_elevator_platforms.length; i++){
			steam_elevator_platforms[i] = steam_elevator_platform.getStateFromMeta(i);
		}
		steam_elevator = (GUIBlock)addBlock(new SteamElevatorBlock(),"steam_elevator");

		steam_switch = addBlock(new BlockPowerSwitch(Power.steam_power),"steam_switch");
		pump_pipe_steam = addBlock(new PumpPipeBlock(),"pump_pipe_steam");
		pump_pipe_steam.setCreativeTab(CreativeTabs.SEARCH);
		steam_still = (GUIBlock)addBlock(new SteamStillBlock(),"steam_still");
		steam_pump = (GUIBlock)addBlock(new SteamPumpBlock(),"steam_pump");
		steam_boiler_oil = (GUIBlock)addBlock(new OilBoilerBlock(),"steam_boiler_oil");
		
		initDone = true;
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event){
		event.getRegistry().registerAll(allBlocks.values().toArray(new Block[0]));
	}

	@SubscribeEvent
	public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
		Arrays.stream(allBlocks.values().toArray(new Block[0]))
				.filter(block -> block.getRegistryName() != null)
				.map((block -> new ItemBlock(block).setRegistryName(block.getRegistryName())))
				.forEach((item -> event.getRegistry().register(item)));
	}

	@SuppressWarnings("deprecation")
	private static Block addBlock(Block block, String name ){
		block.setUnlocalizedName(SteamAdvantage.MODID+"."+name);
		block.setRegistryName(SteamAdvantage.MODID, name);
		block.setCreativeTab(cyano.poweradvantage.init.ItemGroups.tab_powerAdvantage);
		allBlocks.put(name, block);
		return block;
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerItemRenders(FMLInitializationEvent event){
		for(Map.Entry<String, Block> e : allBlocks.entrySet()){
			String name = e.getKey();
			Block block = e.getValue();
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
			.register(net.minecraft.item.Item.getItemFromBlock(block), 0, 
				new ModelResourceLocation(SteamAdvantage.MODID+":"+name, "inventory"));
		}
	}

	
}
