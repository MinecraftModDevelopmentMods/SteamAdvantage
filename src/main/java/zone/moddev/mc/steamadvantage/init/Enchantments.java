package zone.moddev.mc.steamadvantage.init;

import zone.moddev.mc.steamadvantage.SteamAdvantage;
import zone.moddev.mc.steamadvantage.enchantments.HighExplosiveEnchantment;
import zone.moddev.mc.steamadvantage.enchantments.PowderlessEnchantment;
import zone.moddev.mc.steamadvantage.enchantments.RapidReloadEnchantment;
import zone.moddev.mc.steamadvantage.enchantments.RecoilEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = SteamAdvantage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Enchantments {

	public static Enchantment rapid_reload;
	public static Enchantment powderless;
	public static Enchantment high_explosive;
	public static Enchantment recoil;

	public static void init(){
		createEnchantments();
	}

	@SubscribeEvent
	public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
		createEnchantments();
		event.getRegistry().registerAll(high_explosive, powderless, rapid_reload, recoil);
	}

	private static void createEnchantments() {
		if(high_explosive != null) return;
		high_explosive = new HighExplosiveEnchantment();
		powderless = new PowderlessEnchantment();
		rapid_reload = new RapidReloadEnchantment();
		recoil = new RecoilEnchantment();

		high_explosive.setRegistryName(new ResourceLocation(SteamAdvantage.MODID, "high_explosive"));
		powderless.setRegistryName(new ResourceLocation(SteamAdvantage.MODID, "powderless"));
		rapid_reload.setRegistryName(new ResourceLocation(SteamAdvantage.MODID, "rapid_reload"));
		recoil.setRegistryName(new ResourceLocation(SteamAdvantage.MODID, "recoil"));
	}
}
