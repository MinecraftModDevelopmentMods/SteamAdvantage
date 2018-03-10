package cyano.steamadvantage.init;

import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.enchantments.HighExplosiveEnchantment;
import cyano.steamadvantage.enchantments.PowderlessEnchantment;
import cyano.steamadvantage.enchantments.RapidReloadEnchantment;
import cyano.steamadvantage.enchantments.RecoilEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber
public class Enchantments {

	public static Enchantment rapid_reload;
	public static Enchantment powderless;
	public static Enchantment high_explosive;
	public static Enchantment recoil;
	
	private static boolean initDone = false;

	@SubscribeEvent
	public static void init(RegistryEvent.Register<Enchantment> event){
		if(initDone) return;

		high_explosive = new HighExplosiveEnchantment();
		high_explosive.setRegistryName(new ResourceLocation(SteamAdvantage.MODID+":"+"high_explosive"));
		event.getRegistry().register(high_explosive);
		powderless = new PowderlessEnchantment();
		powderless.setRegistryName(new ResourceLocation(SteamAdvantage.MODID+":"+"powderless"));
		event.getRegistry().register(powderless);
		rapid_reload = new RapidReloadEnchantment();
		rapid_reload.setRegistryName(new ResourceLocation(SteamAdvantage.MODID+":"+"rapid_reload"));
		event.getRegistry().register(rapid_reload);
		recoil = new RecoilEnchantment();
		recoil.setRegistryName(new ResourceLocation(SteamAdvantage.MODID+":"+"recoil"));
		event.getRegistry().register(recoil);
		initDone = true;
	}

	
	private static final Set<Integer> reservedIDs = new HashSet<>();
	private static int getNextEnchantmentID(){
		for(int i = 0; i < 255; i++){
			if(Enchantment.REGISTRY.getObjectById(i) == null && reservedIDs.contains(i) == false){
				reservedIDs.add(i);
				return i;
			}
		}
		FMLLog.severe("Failed to find free enchantment ID!");
		return 255;
	}
}
