package cyano.steamadvantage.init;

import cyano.poweradvantage.PowerAdvantage;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.items.MusketItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber
@GameRegistry.ObjectHolder(SteamAdvantage.MODID)
public abstract class Items {

	
	private static final Map<String,Item> allItems = new HashMap<>();

	public static Item steam_governor;
	public static Item steam_drill_bit;
	public static Item blackpowder_cartridge;
	public static Item blackpowder_musket;
	
	
	private static boolean initDone = false;

	@SubscribeEvent
	public static void init(RegistryEvent.Register<Item> event) {
		if(initDone) return;
		Blocks.init();

		steam_governor = addItem(event, "steam_governor",new Item(),"governor","governorBrass");
		steam_drill_bit = addItem(event, "steam_drill_bit",new Item());
		blackpowder_cartridge = addItem(event, "blackpowder_cartridge",new Item(),"ammoBlackpowder");
		blackpowder_musket = addItem(event, "musket",new MusketItem(),"gun");
		
		initDone = true;
	}

	private static Item addItem(RegistryEvent.Register<Item> event, String unlocalizedName, Item i,String... oreDictNames){
		Item n = addItem(event, unlocalizedName, i);
		for(String oreDictName : oreDictNames){
			OreDictionary.registerOre(oreDictName, n);
		}
		return n;
	}
	@SuppressWarnings("deprecation")
	private static Item addItem(RegistryEvent.Register<Item> event, String unlocalizedName, Item i){
		i.setUnlocalizedName(SteamAdvantage.MODID+"."+unlocalizedName);
		i.setRegistryName(SteamAdvantage.MODID, unlocalizedName);
		event.getRegistry().register(i);
		i.setCreativeTab(cyano.poweradvantage.init.ItemGroups.tab_powerAdvantage);
		allItems.put(unlocalizedName, i);
		return i;
	}
	
	public static Item getItemByName(String unlocalizedName){
		return allItems.get(unlocalizedName);
	}
	
	public static Set<Map.Entry<String, Item>> getAllRegisteredItems(){
		return allItems.entrySet();
	}

	@SideOnly(Side.CLIENT)
	public static void registerItemRenders(FMLInitializationEvent event) {
		for(Map.Entry<String, Item> e :  getAllRegisteredItems()){
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
			.register(e.getValue(), 0, 
					new ModelResourceLocation(SteamAdvantage.MODID+":"+e.getKey(), "inventory"));
		}
	}
	
}
