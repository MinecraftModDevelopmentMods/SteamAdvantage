package com.mcmoddev.steamadvantage.init;

import com.mcmoddev.lib.data.SharedStrings;
import com.mcmoddev.lib.events.MMDLibRegisterItems;
import com.mcmoddev.lib.init.Materials;
import com.mcmoddev.lib.util.Oredicts;

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
import com.mcmoddev.steamadvantage.data.SteamAdvantageNames;
import com.mcmoddev.steamadvantage.items.MusketItem;

@Mod.EventBusSubscriber
public abstract class Items extends com.mcmoddev.lib.init.Items {	
	@SubscribeEvent
	public static void init(final MMDLibRegisterItems event) {
		event.setActive(SteamAdvantage.MODID);
		addItem(new Item(), SteamAdvantageNames.STEAM_GOVERNOR, ItemGroups.getTab(SteamAdvantage.MODID, SharedStrings.TAB_ITEMS));
		addItem(new Item(), SteamAdvantageNames.STEAM_DRILL_BIT, ItemGroups.getTab(SteamAdvantage.MODID, SharedStrings.TAB_TOOLS));
		addItem(new Item(), SteamAdvantageNames.BLACKPOWDER_CARTRIDGE, ItemGroups.getTab(SteamAdvantage.MODID, SharedStrings.TAB_COMBAT));
		addItem(new MusketItem(), SteamAdvantageNames.MUSKET, ItemGroups.getTab(SteamAdvantage.MODID, SharedStrings.TAB_COMBAT));
		Oredicts.registerOre("governor", Materials.DEFAULT.getItem(SteamAdvantageNames.STEAM_GOVERNOR));
		Oredicts.registerOre("governorBrass", Materials.DEFAULT.getItem(SteamAdvantageNames.STEAM_GOVERNOR));
		Oredicts.registerOre("ammoBlackpowder", Materials.DEFAULT.getItem(SteamAdvantageNames.BLACKPOWDER_CARTRIDGE));
		Oredicts.registerOre("gun", Materials.DEFAULT.getItem(SteamAdvantageNames.MUSKET));
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		Materials.DEFAULT.getItems().stream()
		.map(ItemStack::getItem)
		.filter(it -> it.getRegistryName().getNamespace().equals(SteamAdvantage.MODID))
		.filter( it -> !(it instanceof ItemBlock) )
		.forEach(event.getRegistry()::register);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void registerItemRenders(final ModelRegistryEvent event) {
		Materials.DEFAULT.getItems().stream()
		.map(ItemStack::getItem)
		.filter(it -> it.getRegistryName().getNamespace().equals(SteamAdvantage.MODID))
		.forEach(it -> ModelLoader.setCustomModelResourceLocation(it, 0, new ModelResourceLocation(it.getRegistryName(), "inventory")));
	}
	
}
