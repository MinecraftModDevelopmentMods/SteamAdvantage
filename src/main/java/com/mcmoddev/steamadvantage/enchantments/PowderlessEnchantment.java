package com.mcmoddev.steamadvantage.enchantments;

import com.mcmoddev.lib.init.Materials;
import com.mcmoddev.steamadvantage.data.SteamAdvantageNames;
import com.mcmoddev.steamadvantage.init.Items;

import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PowderlessEnchantment extends net.minecraft.enchantment.Enchantment{
	private static final Item musket = Materials.DEFAULT.getItem(SteamAdvantageNames.MUSKET);

	public PowderlessEnchantment() {
		super(Rarity.VERY_RARE,EnumEnchantmentType.ALL,new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND});
		this.setName("powderless");
	}

	
	@Override
	public boolean canApply(ItemStack item){
		return item.getItem() == musket;
	}
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack item){
		return canApply(item);
	}
	


	@Override
	public int getMinEnchantability(final int lvl) {
		return 20;
	}

	@Override
	public int getMaxEnchantability(final int lvl) {
		return 50;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}
}
