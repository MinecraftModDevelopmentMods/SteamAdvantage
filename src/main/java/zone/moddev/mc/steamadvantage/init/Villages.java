package zone.moddev.mc.steamadvantage.init;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import zone.moddev.mc.steamadvantage.SteamAdvantage;

public abstract class Villages {
	// TODO: add machinist villager
	private static final ResourceLocation[] PROFESSION_LIST = {
			new ResourceLocation("minecraft:farmer"),
			new ResourceLocation("minecraft:librarian"),
			new ResourceLocation("minecraft:priest"),
			new ResourceLocation("minecraft:smith"),
			new ResourceLocation("minecraft:butcher")
	};

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Entities.init();

		try {
			insertTrades(3, 3, 1, new EntityVillager.ListItemForEmeralds(Items.steam_governor, new EntityVillager.PriceInfo(-4,-1)));
			insertTrades(3, 2, 1, new EntityVillager.ListItemForEmeralds(Items.steam_governor, new EntityVillager.PriceInfo(-4,-1)));
			insertTrades(3, 1, 1, new EntityVillager.ListItemForEmeralds(Items.steam_governor, new EntityVillager.PriceInfo(-4,-1)));
			insertTrades(1, 1, 1, new EntityVillager.ListItemForEmeralds(Items.steam_governor, new EntityVillager.PriceInfo(-4,-1)));
			insertTrades(1, 1, 1, new EntityVillager.ListItemForEmeralds(Item.getItemFromBlock(Blocks.steam_pipe), new EntityVillager.PriceInfo(-8,-4)));
			insertTrades(3, 2, 1, new EntityVillager.ListItemForEmeralds(Items.blackpowder_cartridge, new EntityVillager.PriceInfo(-7,-5)));
			insertTrades(3, 2, 1, new EntityVillager.ListItemForEmeralds(Items.blackpowder_musket, new EntityVillager.PriceInfo(10,15)));
			insertTrades(3, 3, 1, new EntityVillager.ListItemForEmeralds(Items.blackpowder_cartridge, new EntityVillager.PriceInfo(-7,-5)));
			insertTrades(3, 3, 1, new EntityVillager.ListItemForEmeralds(Items.blackpowder_musket, new EntityVillager.PriceInfo(10,15)));
			insertTrades(2, 1, 3,
					new EntityVillager.ListEnchantedItemForEmeralds(Items.blackpowder_musket, new EntityVillager.PriceInfo(15, 25)));
		} catch (RuntimeException e) {
			SteamAdvantage.LOGGER.error("Failed to add trades to villagers", e);
		}
		
		initDone = true;
	}

	private static void insertTrades(int professionID, int careerID, int tradeLevel, EntityVillager.ITradeList... trades) {
		VillagerRegistry.VillagerProfession profession = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(PROFESSION_LIST[professionID]);
		if (profession == null) {
			throw new IllegalStateException("No villager profession registered for " + PROFESSION_LIST[professionID]);
		}
		VillagerRegistry.VillagerCareer career = profession.getCareer(careerID - 1);
		career.addTrade(tradeLevel, trades);
	}
}
