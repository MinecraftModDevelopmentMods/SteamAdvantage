package cyano.steamadvantage.init;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import com.mcmoddev.lib.registry.CrusherRecipeRegistry;
import cyano.poweradvantage.PowerAdvantage;
import cyano.poweradvantage.RecipeMode;
import cyano.steamadvantage.SteamAdvantage;

public class Recipes {

	private static boolean initDone = false;


	@SubscribeEvent
	public static void init(RegistryEvent.Register<IRecipe> event) {
		if(initDone) return;
		
		Blocks.init();

		RecipeMode recipeMode = PowerAdvantage.recipeMode;
		OreDictionary.registerOre("stick", net.minecraft.init.Items.STICK);
		OreDictionary.registerOre("blockObsidian", net.minecraft.init.Blocks.OBSIDIAN);
		
		if(recipeMode == RecipeMode.TECH_PROGRESSION){
			addRecipe(event, new ItemStack(Items.steam_governor,1)," t ","srs","btb",'t',"sprocket",'s',"rod",'r',"rod",'b',"ingotBrass");
			if(SteamAdvantage.MUSKET_ENABLE)addRecipe(event, new ItemStack(Items.blackpowder_musket,1),"fss","w  ",'f',net.minecraft.init.Items.FLINT_AND_STEEL,'s',"ingotSteel",'w',"plankWood");
			if(SteamAdvantage.MUSKET_ENABLE)addRecipe(event, new ItemStack(Items.blackpowder_musket,1),"ssf","  w",'f',net.minecraft.init.Items.FLINT_AND_STEEL,'s',"ingotSteel",'w',"plankWood");
		} else if(recipeMode == RecipeMode.APOCALYPTIC){
			addRecipe(event, new ItemStack(Items.steam_governor,1)," t ","srs","btb",'t',"sprocket",'s',"rod",'r',"rod",'b',"ingotBrass");
			CrusherRecipeRegistry.addNewCrusherRecipe(Items.steam_governor, new ItemStack(cyano.poweradvantage.init.Items.sprocket,2));

			CrusherRecipeRegistry.addNewCrusherRecipe(Blocks.steam_crusher, new ItemStack(Items.steam_governor,2));
			CrusherRecipeRegistry.addNewCrusherRecipe(Blocks.steam_furnace, new ItemStack(Items.steam_governor,2));
			CrusherRecipeRegistry.addNewCrusherRecipe(Blocks.steam_boiler_coal, new ItemStack(Items.steam_governor,2));
			CrusherRecipeRegistry.addNewCrusherRecipe(Blocks.steam_boiler_electric, new ItemStack(Items.steam_governor,2));
			CrusherRecipeRegistry.addNewCrusherRecipe(Blocks.steam_boiler_geothermal, new ItemStack(Items.steam_governor,2));
			CrusherRecipeRegistry.addNewCrusherRecipe(Blocks.steam_drill, new ItemStack(Items.steam_governor,2));
			CrusherRecipeRegistry.addNewCrusherRecipe(Blocks.steam_elevator, new ItemStack(Items.steam_governor,3));
			CrusherRecipeRegistry.addNewCrusherRecipe(Blocks.steam_tank, new ItemStack(Items.steam_governor,1));
		} else {
			// normal
			addRecipe(event, new ItemStack(Items.steam_governor,1)," t ","srs","btb",'t',"nuggetIron",'s',"stick",'r',"stick",'b',"ingotBrass");
			if(SteamAdvantage.MUSKET_ENABLE)addRecipe(event, new ItemStack(Items.blackpowder_musket,1),"fss","w  ",'f',net.minecraft.init.Items.FLINT_AND_STEEL,'s',"ingotSteel",'w',"plankWood");
			if(SteamAdvantage.MUSKET_ENABLE)addRecipe(event, new ItemStack(Items.blackpowder_musket,1),"ssf","  w",'f',net.minecraft.init.Items.FLINT_AND_STEEL,'s',"ingotSteel",'w',"plankWood");
			addRecipe(event, steamMachineRecipe(Blocks.steam_drill,net.minecraft.init.Items.DIAMOND_PICKAXE));
		}

		addRecipe(event, new ItemStack(Items.steam_drill_bit)," g "," i ","did",'g',"sprocket",'i',"ingotSteel",'d',"gemDiamond");
		
		addRecipe(event, new ItemStack(Blocks.steam_pipe,6),"xxx","   ","xxx",'x',"ingotBrass");
		addRecipe(event, steamMachineRecipe(Blocks.steam_crusher,net.minecraft.init.Blocks.PISTON,"blockSteel"));
		addRecipe(event, steamMachineRecipe(Blocks.steam_furnace,net.minecraft.init.Blocks.FURNACE));
		addRecipe(event, steamMachineRecipe(Blocks.steam_boiler_coal,"conduitSteam"));
		addRecipe(event, steamMachineRecipe(Blocks.steam_drill,Items.steam_drill_bit));
		addRecipe(event, steamMachineRecipe(Blocks.steam_elevator,net.minecraft.init.Blocks.PISTON,"sprocket"));
		addRecipe(event, new ItemStack(Blocks.steam_tank),"xgx","xpx","xxx",'x',"plateCopper",'p',"conduitSteam",'g',"governor");
		addRecipe(event, steamMachineRecipe(Blocks.steam_boiler_electric,"wire",net.minecraft.init.Items.BUCKET));
		addRecipe(event, steamMachineRecipe(Blocks.steam_boiler_geothermal,"conduitSteam","blockObsidian"));
		
		addRecipe(event, new ItemStack(Items.blackpowder_cartridge) ,"L","g","p",'L',"nuggetLead",'g',net.minecraft.init.Items.GUNPOWDER,'p',net.minecraft.init.Items.PAPER);
		
		event.getRegistry().register(new ShapelessOreRecipe(Blocks.steam_track.getRegistryName(),
				new ItemStack(Blocks.steam_track),
				Blocks.steam_pipe, cyano.poweradvantage.init.Blocks.steel_frame));
		
		addRecipe(event, new ItemStack(Blocks.steam_switch)," L ","pfp",'L',net.minecraft.init.Blocks.LEVER,'p',"conduitSteam",'f',"frameSteel");
		addRecipe(event, steamMachineRecipe(Blocks.steam_boiler_oil,net.minecraft.init.Blocks.FURNACE, net.minecraft.init.Items.BUCKET));
		addRecipe(event, steamMachineRecipe(Blocks.steam_pump,net.minecraft.init.Blocks.PISTON,net.minecraft.init.Items.BUCKET));
		addRecipe(event, steamMachineRecipe(Blocks.steam_still,net.minecraft.init.Items.BUCKET,net.minecraft.init.Items.BUCKET));
		
		
		
		initDone = true;
	}

	static void addRecipe(RegistryEvent.Register<IRecipe> event, ShapedOreRecipe recipe) {
		event.getRegistry().register(recipe);
	}

	static void addRecipe(RegistryEvent.Register<IRecipe> event, ItemStack stack, Object... recipe) {
		event.getRegistry().register(new ShapedOreRecipe(stack.getItem().getRegistryName(), stack, recipe)
				.setRegistryName(stack.getItem().getRegistryName()));
	}

	private static ShapedOreRecipe steamMachineRecipe(Block output, Object item){
		return new ShapedOreRecipe(output.getRegistryName(), new ItemStack(output, 1), "gXg","pmp",'X',item,'g',"governor",'p',"plateIron",'m',"frameSteel");
	}

	private static ShapedOreRecipe steamMachineRecipe(Block output, Object item1, Object item2){
		return new ShapedOreRecipe(output.getRegistryName(), new ItemStack(output, 1), " Y ","gXg","pmp",'X',item1,'Y',item2,'g',"governor",'p',"plateIron",'m',"frameSteel");
	}
}
