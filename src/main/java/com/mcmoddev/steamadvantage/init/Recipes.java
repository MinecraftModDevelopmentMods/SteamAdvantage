package com.mcmoddev.steamadvantage.init;

import com.mcmoddev.basemetals.data.MaterialNames;
import com.mcmoddev.lib.data.Names;
import com.mcmoddev.lib.init.Materials;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import com.mcmoddev.lib.registry.CrusherRecipeRegistry;
import com.mcmoddev.steamadvantage.SteamAdvantage;
import com.mcmoddev.steamadvantage.data.SteamAdvantageNames;
import com.mcmoddev.poweradvantage.PowerAdvantage;
import com.mcmoddev.poweradvantage.RecipeMode;

@Mod.EventBusSubscriber
public class Recipes extends com.mcmoddev.lib.init.Recipes {

	private static boolean initDone = false;


	@SubscribeEvent
	public static void init(RegistryEvent.Register<IRecipe> event) {
		if(initDone) return;
		
		RecipeMode recipeMode = PowerAdvantage.recipeMode;

		ItemStack governor = Materials.DEFAULT.getItemStack(SteamAdvantageNames.STEAM_GOVERNOR);
		ItemStack two_govs = Materials.DEFAULT.getItemStack(SteamAdvantageNames.STEAM_GOVERNOR, 2);
		ItemStack three_govs = Materials.DEFAULT.getItemStack(SteamAdvantageNames.STEAM_GOVERNOR, 3);
		ItemStack musket = Materials.DEFAULT.getItemStack(SteamAdvantageNames.MUSKET);
		ItemStack drill_bit = Materials.DEFAULT.getItemStack(SteamAdvantageNames.STEAM_DRILL_BIT);
		ItemStack six_pipes = new ItemStack( Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_PIPE), 6);
		ItemStack cartridge = Materials.DEFAULT.getItemStack(SteamAdvantageNames.BLACKPOWDER_CARTRIDGE);
		ItemStack steam_switch = Materials.DEFAULT.getItemStack(SteamAdvantageNames.STEAM_SWITCH);
		
		Block crusher = Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_CRUSHER);
		Block furnace = Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_FURNACE);
		Block boiler_coal = Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_BOILER_COAL);
		Block boiler_electric = Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_BOILER_ELECTRIC);
		Block boiler_geothermal = Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_BOILER_GEOTHERMAL);
		Block boiler_oil = Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_BOILER_OIL);
		Block drill = Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_DRILL);
		Block elevator = Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_ELEVATOR);
		Block tank = Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_TANK);
		Block pipe = Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_PIPE);
		Block track = Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_TRACK);
		Block pump = Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_PUMP);
		Block still = Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_STILL);
		
		if(recipeMode == RecipeMode.TECH_PROGRESSION){
			addRecipe(event, governor, true,
					" t ","srs","btb",'t',"sprocket",'s',"rod",'r',"rod",'b',"ingotBrass");
			if(SteamAdvantage.MUSKET_ENABLE) {
				addRecipe(event, musket, true, "fss","w  ",'f',net.minecraft.init.Items.FLINT_AND_STEEL,'s',"ingotSteel",'w',"plankWood");
			}
		} else if(recipeMode == RecipeMode.APOCALYPTIC){
			addRecipe(event, governor, false, " t ","srs","btb",'t',"sprocket",'s',"rod",'r',"rod",'b',"ingotBrass");
			CrusherRecipeRegistry.addNewCrusherRecipe(governor, new ItemStack(com.mcmoddev.poweradvantage.init.Items.sprocket,2));

			CrusherRecipeRegistry.addNewCrusherRecipe(crusher, two_govs);
			CrusherRecipeRegistry.addNewCrusherRecipe(furnace, two_govs);
			CrusherRecipeRegistry.addNewCrusherRecipe(boiler_coal, two_govs);
			CrusherRecipeRegistry.addNewCrusherRecipe(boiler_electric, two_govs);
			CrusherRecipeRegistry.addNewCrusherRecipe(boiler_geothermal, two_govs);
			CrusherRecipeRegistry.addNewCrusherRecipe(drill, two_govs);
			CrusherRecipeRegistry.addNewCrusherRecipe(elevator, three_govs);
			CrusherRecipeRegistry.addNewCrusherRecipe(tank, governor);
		} else {
			// normal
			addRecipe(event, governor, false, " t ","sss","btb",
					't',net.minecraft.init.Items.IRON_NUGGET,
					's', net.minecraft.init.Items.STICK,
					'b', Materials.getMaterialByName(MaterialNames.BRASS).getItem(Names.INGOT));
			if(SteamAdvantage.MUSKET_ENABLE) {
				addRecipe(event, musket, true,"fss","w  ",
					'f', net.minecraft.init.Items.FLINT_AND_STEEL,
					's', Materials.getMaterialByName(MaterialNames.STEEL).getItem(Names.INGOT),
					'w', net.minecraft.init.Blocks.PLANKS);
			}
			addRecipe(event, steamMachineRecipe(drill, net.minecraft.init.Items.DIAMOND_PICKAXE));
		}

		addRecipe(event, drill_bit, false, " g "," i ","did",
				'g',com.mcmoddev.poweradvantage.init.Items.sprocket,
				'i', Materials.getMaterialByName(MaterialNames.STEEL).getItem(Names.INGOT),
				'd', net.minecraft.init.Items.DIAMOND);
		
		addRecipe(event, six_pipes, false,"xxx","   ","xxx",
				'x', Materials.getMaterialByName(MaterialNames.BRASS).getItem(Names.INGOT));
		addRecipe(event, steamMachineRecipe(crusher,net.minecraft.init.Blocks.PISTON,
				Materials.getMaterialByName(MaterialNames.STEEL).getBlock(Names.BLOCK)));
		addRecipe(event, steamMachineRecipe(furnace,net.minecraft.init.Blocks.FURNACE));
		addRecipe(event, steamMachineRecipe(boiler_coal, pipe));
		addRecipe(event, steamMachineRecipe(drill, drill_bit));
		addRecipe(event, steamMachineRecipe(elevator,
				net.minecraft.init.Blocks.PISTON,
				com.mcmoddev.poweradvantage.init.Items.sprocket));
		addRecipe(event, new ItemStack(tank), false, "xgx","xpx","xxx",
				'x', Materials.getMaterialByName(MaterialNames.COPPER).getBlock(Names.PLATE),
				'p', pipe,
				'g', governor);
//		addRecipe(event, steamMachineRecipe(Blocks.steam_boiler_electric,"wire",net.minecraft.init.Items.BUCKET));
		addRecipe(event, steamMachineRecipe(boiler_geothermal,
				pipe,
				net.minecraft.init.Blocks.OBSIDIAN));

		addRecipe(event, cartridge, false,
				"L","g","p",
				'L',Materials.getMaterialByName(MaterialNames.COPPER).getItem(Names.NUGGET),
				'g',net.minecraft.init.Items.GUNPOWDER,
				'p',net.minecraft.init.Items.PAPER);

		event.getRegistry().register(new ShapelessOreRecipe(track.getRegistryName(),
				new ItemStack(track),
				pipe, com.mcmoddev.poweradvantage.init.Blocks.steel_frame)
				.setRegistryName(track.getRegistryName()));

		addRecipe(event, steam_switch, false,
				" L ","pfp",'L',net.minecraft.init.Blocks.LEVER,
				'p', pipe,
				'f', com.mcmoddev.poweradvantage.init.Blocks.steel_frame);
		addRecipe(event, steamMachineRecipe(boiler_oil,net.minecraft.init.Blocks.FURNACE, net.minecraft.init.Items.BUCKET));
		addRecipe(event, steamMachineRecipe(pump,net.minecraft.init.Blocks.PISTON,net.minecraft.init.Items.BUCKET));
		addRecipe(event, steamMachineRecipe(still,net.minecraft.init.Items.BUCKET,net.minecraft.init.Items.BUCKET));


		Villages.init();

		initDone = true;
	}

	static void addRecipe(RegistryEvent.Register<IRecipe> event, ShapedOreRecipe recipe) {
		event.getRegistry().register(recipe);
	}

	static void addRecipe(RegistryEvent.Register<IRecipe> event, ItemStack stack, boolean isMirrored, Object... recipe) {
		ShapedOreRecipe x = (ShapedOreRecipe) new ShapedOreRecipe(stack.getItem().getRegistryName(), stack, recipe)
				.setRegistryName(stack.getItem().getRegistryName());
		x.setMirrored(isMirrored);
		event.getRegistry().register(x);
	}

	private static ShapedOreRecipe steamMachineRecipe(Block output, Object item){
		ShapedOreRecipe ret = new ShapedOreRecipe(output.getRegistryName(), new ItemStack(output, 1),
				"gXg","pmp",
				'X',item,
				'g', Materials.DEFAULT.getItemStack(SteamAdvantageNames.STEAM_GOVERNOR),
				'p', Materials.getMaterialByName(MaterialNames.IRON).getBlock(Names.PLATE),
				'm', com.mcmoddev.poweradvantage.init.Blocks.steel_frame);
		ret.setRegistryName(output.getRegistryName());
		return ret;
	}

	private static ShapedOreRecipe steamMachineRecipe(Block output, Object item1, Object item2){
		ShapedOreRecipe ret = new ShapedOreRecipe(output.getRegistryName(), new ItemStack(output, 1),
				" Y ","gXg","pmp",
				'X',item1,
				'Y',item2,
				'g', Materials.DEFAULT.getItemStack(SteamAdvantageNames.STEAM_GOVERNOR),
				'p', Materials.getMaterialByName(MaterialNames.IRON).getBlock(Names.PLATE),
				'm', com.mcmoddev.poweradvantage.init.Blocks.steel_frame);
		ret.setRegistryName(output.getRegistryName());
		return ret;
	}
}
