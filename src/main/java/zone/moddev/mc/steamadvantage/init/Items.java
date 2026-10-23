package zone.moddev.mc.steamadvantage.init;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zone.moddev.mc.poweradvantage.init.ItemGroups;
import zone.moddev.mc.steamadvantage.SteamAdvantage;
import zone.moddev.mc.steamadvantage.items.MusketItem;

@Mod.EventBusSubscriber(modid = SteamAdvantage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Items {
    public static final Map<String, Item> allItems = new LinkedHashMap<>();
    public static Item steam_governor;
    public static Item steam_drill_bit;
    public static Item blackpowder_cartridge;
    public static Item blackpowder_musket;
    private static boolean initialized;

    private Items() {}

    public static synchronized void init() {
        if (initialized) return;
        Blocks.init();
        steam_governor = add("steam_governor", new Item(properties()));
        steam_drill_bit = add("steam_drill_bit", new Item(properties()));
        blackpowder_cartridge = add("blackpowder_cartridge", new Item(properties()));
        blackpowder_musket = add("musket", new MusketItem(properties().maxStackSize(1)));
        initialized = true;
    }

    private static Item.Properties properties() {
        return new Item.Properties().group(ItemGroups.tab_powerAdvantage);
    }

    private static Item add(String id, Item item) {
        item.setRegistryName(SteamAdvantage.MODID, id);
        allItems.put(id, item);
        return item;
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Item> event) {
        init();
        event.getRegistry().registerAll(allItems.values().toArray(new Item[0]));
    }

    public static Item getItemByName(String id) { return allItems.get(id); }
    public static Set<Map.Entry<String, Item>> getAllRegisteredItems() { return allItems.entrySet(); }
}
