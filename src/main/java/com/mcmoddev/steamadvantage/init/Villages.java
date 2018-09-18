package com.mcmoddev.steamadvantage.init;

import com.mcmoddev.basemetals.util.VillagerTradeHelper;
import com.mcmoddev.lib.init.Materials;
import com.mcmoddev.steamadvantage.data.SteamAdvantageNames;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

public abstract class Villages extends com.mcmoddev.lib.init.VillagerTrades {
	// TODO: add machinist villager

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Item governor = Materials.DEFAULT.getItem(SteamAdvantageNames.STEAM_GOVERNOR);
		Item pipe = Materials.DEFAULT.getBlockItemStack(SteamAdvantageNames.STEAM_PIPE).getItem();
		Item cartridge = Materials.DEFAULT.getItem(SteamAdvantageNames.BLACKPOWDER_CARTRIDGE);
		Item musket = Materials.DEFAULT.getItem(SteamAdvantageNames.MUSKET);
		try {
			VillagerTradeHelper.insertTrades(3, 3, 1, new EntityVillager.ListItemForEmeralds(governor, new EntityVillager.PriceInfo(-4, -1)));
			VillagerTradeHelper.insertTrades(3, 2, 1, new EntityVillager.ListItemForEmeralds(governor, new EntityVillager.PriceInfo(-4, -1)));
			VillagerTradeHelper.insertTrades(3, 1, 1, new EntityVillager.ListItemForEmeralds(governor, new EntityVillager.PriceInfo(-4, -1)));
			VillagerTradeHelper.insertTrades(1, 1, 1, new EntityVillager.ListItemForEmeralds(governor, new EntityVillager.PriceInfo(-4, -1)));
			VillagerTradeHelper.insertTrades(1, 1, 1, new EntityVillager.ListItemForEmeralds(pipe, new EntityVillager.PriceInfo(-8, -4)));
			VillagerTradeHelper.insertTrades(3, 2, 1, new EntityVillager.ListItemForEmeralds(cartridge, new EntityVillager.PriceInfo(-7, -5)));
			VillagerTradeHelper.insertTrades(3, 2, 1, new EntityVillager.ListItemForEmeralds(musket, new EntityVillager.PriceInfo(10, 15)));
			VillagerTradeHelper.insertTrades(3, 3, 1, new EntityVillager.ListItemForEmeralds(cartridge, new EntityVillager.PriceInfo(-7, -5)));
			VillagerTradeHelper.insertTrades(3, 3, 1, new EntityVillager.ListItemForEmeralds(musket, new EntityVillager.PriceInfo(10, 15)));
			VillagerTradeHelper.insertTrades(2, 1, 3,
					new EntityVillager.ListEnchantedItemForEmeralds(musket, new EntityVillager.PriceInfo(15, 25)));
//		} catch (NoSuchFieldException | IllegalAccessException e) {
//			FMLLog.log(Level.ERROR, e, "Failed to add trades to villagers");
//		}
		} finally {

		}
		
		initDone = true;
	}
}
