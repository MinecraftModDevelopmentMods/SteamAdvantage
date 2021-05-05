package com.mcmoddev.steamadvantage.init;

import com.mcmoddev.lib.data.SharedStrings;
import com.mcmoddev.lib.events.MMDLibRegisterBlocks;
import com.mcmoddev.lib.init.Materials;
import com.mcmoddev.lib.util.Oredicts;
import com.mcmoddev.poweradvantage.blocks.BlockPowerSwitch;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.mcmoddev.steamadvantage.SteamAdvantage;
import com.mcmoddev.steamadvantage.blocks.*;
import com.mcmoddev.steamadvantage.machines.*;
import com.mcmoddev.steamadvantage.data.SteamAdvantageNames;

@Mod.EventBusSubscriber
public abstract class Blocks extends com.mcmoddev.lib.init.Blocks {
	@SubscribeEvent
	public static void init(final MMDLibRegisterBlocks event){
//		event.setActive(SteamAdvantage.MODID);

		addBlock(new SteamPipeBlock(),"steam_pipe", ItemGroups.getTab(SteamAdvantage.MODID, SharedStrings.TAB_BLOCKS));
		addBlock(new SteamTrackBlock(),"steam_track", ItemGroups.getTab(SteamAdvantage.MODID, SharedStrings.TAB_BLOCKS));
		addBlock(new CoalBoilerBlock(),"steam_boiler_coal", ItemGroups.getTab(SteamAdvantage.MODID, SteamAdvantageNames.TabNames.TAB_MACHINES));
		addBlock(new ElectricBoilerBlock(),"steam_boiler_electric", ItemGroups.getTab(SteamAdvantage.MODID, SteamAdvantageNames.TabNames.TAB_MACHINES));
		addBlock(new GeothermalBoilerBlock(),"steam_boiler_geothermal", ItemGroups.getTab(SteamAdvantage.MODID, SteamAdvantageNames.TabNames.TAB_MACHINES));
		addBlock(new SteamTankBlock(),"steam_tank", ItemGroups.getTab(SteamAdvantage.MODID, SteamAdvantageNames.TabNames.TAB_MACHINES));
		addBlock(new BlastFurnaceBlock(),"steam_furnace", ItemGroups.getTab(SteamAdvantage.MODID, SteamAdvantageNames.TabNames.TAB_MACHINES));
		addBlock(new RockCrusherBlock(),"steam_crusher", ItemGroups.getTab(SteamAdvantage.MODID, SteamAdvantageNames.TabNames.TAB_MACHINES));
		addBlock(new SteamDrillBlock(),"steam_drill", ItemGroups.getTab(SteamAdvantage.MODID, SteamAdvantageNames.TabNames.TAB_MACHINES));
		addBlock(new DrillBitBlock(),"drillbit", ItemGroups.getTab(SteamAdvantage.MODID, SharedStrings.TAB_BLOCKS));
		addBlock(new PlatformBlock(),"platform", ItemGroups.getTab(SteamAdvantage.MODID, SharedStrings.TAB_BLOCKS));

		addBlock(new SteamElevatorBlock(),"steam_elevator", ItemGroups.getTab(SteamAdvantage.MODID, SteamAdvantageNames.TabNames.TAB_MACHINES));

		addBlock(new BlockPowerSwitch(Power.steam_power),"steam_switch", ItemGroups.getTab(SteamAdvantage.MODID, SharedStrings.TAB_BLOCKS));
		addBlock(new PumpPipeBlock(),"pump_pipe_steam", ItemGroups.getTab(SteamAdvantage.MODID, SharedStrings.TAB_BLOCKS));
		addBlock(new SteamStillBlock(),"steam_still", ItemGroups.getTab(SteamAdvantage.MODID, SteamAdvantageNames.TabNames.TAB_MACHINES));
		addBlock(new SteamPumpBlock(),"steam_pump", ItemGroups.getTab(SteamAdvantage.MODID, SteamAdvantageNames.TabNames.TAB_MACHINES));
		addBlock(new OilBoilerBlock(),"steam_boiler_oil", ItemGroups.getTab(SteamAdvantage.MODID, SteamAdvantageNames.TabNames.TAB_MACHINES));
		
		Oredicts.registerOre("conduit_steam", Materials.DEFAULT.getBlock(SteamAdvantageNames.STEAM_PIPE));
		Entities.init();
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event){
		Materials.DEFAULT.getBlocks().stream()
		.filter( bl -> bl.getRegistryName().getNamespace().equals(SteamAdvantage.MODID))
		.forEach(event.getRegistry()::register);
	}

	@SubscribeEvent
	public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
		Materials.DEFAULT.getItems().stream()
		.map(ItemStack::getItem)
		.filter( it -> it.getRegistryName().getNamespace().equals(SteamAdvantage.MODID))
		.filter( it -> it instanceof ItemBlock )
		.forEach(event.getRegistry()::register);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void registerItemRenders(final ModelRegistryEvent event) {
		Materials.DEFAULT.getBlocks().stream()
		.filter(bl -> bl.getRegistryName().getNamespace().equals(SteamAdvantage.MODID))
		.forEach(bl -> ModelLoader.setCustomModelResourceLocation(net.minecraft.item.Item.getItemFromBlock(bl), 0, new ModelResourceLocation(bl.getRegistryName(), "inventory")));
	}
}
