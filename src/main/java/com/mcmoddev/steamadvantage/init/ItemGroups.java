package com.mcmoddev.steamadvantage.init;

import com.mcmoddev.lib.data.SharedStrings;
import com.mcmoddev.lib.init.Materials;
import com.mcmoddev.lib.material.MMDMaterial;

import com.mcmoddev.steamadvantage.data.SteamAdvantageNames;
import com.mcmoddev.steamadvantage.SteamAdvantage;

/**
 * This class initializes all item groups in Steam Advantage.
 *
 * @author Jasmine Iwanek
 *
 */
public final class ItemGroups extends com.mcmoddev.lib.init.ItemGroups {
	private ItemGroups() {
		throw new IllegalAccessError(SharedStrings.NOT_INSTANTIABLE);
	}

	/**
	 * Initializer.
	 */
	public static void init() {
		// Blank Initializer
	}

	/**
	 * Sets up icons for a CreativeTab.
	 */
	public static void setupIcons() {
		final MMDMaterial material = Materials.DEFAULT;

		getTab(SteamAdvantage.MODID, SteamAdvantageNames.TabNames.TAB_MACHINES).setTabIconItem(material.getBlock(SteamAdvantageNames.STEAM_CRUSHER));
		getTab(SteamAdvantage.MODID, SharedStrings.TAB_BLOCKS).setTabIconItem(material.getBlock(SteamAdvantageNames.STEAM_PIPE));
		getTab(SteamAdvantage.MODID, SharedStrings.TAB_COMBAT).setTabIconItem(material.getItem(SteamAdvantageNames.MUSKET));
		getTab(SteamAdvantage.MODID, SharedStrings.TAB_ITEMS).setTabIconItem(material.getItem(SteamAdvantageNames.STEAM_GOVERNOR));
	}
}