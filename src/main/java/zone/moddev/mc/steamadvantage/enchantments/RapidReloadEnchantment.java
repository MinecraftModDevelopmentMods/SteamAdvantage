package zone.moddev.mc.steamadvantage.enchantments;

import zone.moddev.mc.steamadvantage.init.Items;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class RapidReloadEnchantment extends net.minecraft.enchantment.Enchantment{

	public RapidReloadEnchantment() {
		super(Rarity.RARE,EnchantmentType.BREAKABLE,new EquipmentSlotType[]{EquipmentSlotType.MAINHAND});
	}

	
	@Override
	public boolean canEnchant(ItemStack item){
		return item.getItem() == Items.blackpowder_musket;
	}
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack item){
		return canEnchant(item);
	}
	


	@Override
	public int getMinCost(final int lvl) {
		return 15;
	}

	@Override
	public int getMaxCost(final int lvl) {
		return 50;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}
}
