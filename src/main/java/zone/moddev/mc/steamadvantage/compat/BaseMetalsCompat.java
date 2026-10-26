package zone.moddev.mc.steamadvantage.compat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import zone.moddev.mc.basemetals.recipe.CrushingRecipe;

/** Accesses Base Metals' data-driven 1.13 crushing recipes without embedding it. */
public final class BaseMetalsCompat {
    private BaseMetalsCompat() {}

    public static boolean isLoaded() { return ModList.get().isLoaded("basemetals"); }

    /** 1.13 additions are supplied as JSON crushing recipes, not mutable runtime registrations. */
    public static void addCrusherRecipe(String input, ItemStack output) {}
    public static void addCrusherRecipe(Item input, ItemStack output) {}
    public static void addCrusherRecipe(Block input, ItemStack output) {}

    public static boolean hasCrusherRecipe(World world, ItemStack input) {
        return getCrusherRecipeOutput(world, input) != null;
    }

    public static ItemStack getCrusherRecipeOutput(World world, ItemStack input) {
        if (world == null || input == null || input.isEmpty()) return null;
        if (isLoaded()) {
            Inventory inventory = new Inventory(1);
            inventory.setInventorySlotContents(0, input.copy());
            for (IRecipe candidate : world.getRecipeManager().getRecipes()) {
                if (candidate instanceof CrushingRecipe && candidate.matches(inventory, world)) {
                    return candidate.getRecipeOutput().copy();
                }
            }
        }
        return getVanillaCrusherOutput(input);
    }

    public static ItemStack getCrusherRecipeOutput(World world, BlockState input) {
        return input == null ? null : getCrusherRecipeOutput(world, new ItemStack(input.getBlock()));
    }

    private static ItemStack getVanillaCrusherOutput(ItemStack input) {
        Item item = input.getItem();
        if (item == net.minecraft.block.Blocks.STONE.asItem()) return new ItemStack(net.minecraft.block.Blocks.COBBLESTONE);
        if (item == net.minecraft.block.Blocks.COBBLESTONE.asItem()) return new ItemStack(net.minecraft.block.Blocks.GRAVEL);
        if (item == net.minecraft.block.Blocks.GRAVEL.asItem()) return new ItemStack(net.minecraft.block.Blocks.SAND);
        if (item == net.minecraft.block.Blocks.SANDSTONE.asItem()) return new ItemStack(net.minecraft.block.Blocks.SAND, 4);
        if (item == net.minecraft.block.Blocks.RED_SANDSTONE.asItem()) return new ItemStack(net.minecraft.block.Blocks.RED_SAND, 4);
        if (item == net.minecraft.block.Blocks.GLASS.asItem()) return new ItemStack(net.minecraft.block.Blocks.SAND);
        if (item == net.minecraft.block.Blocks.GLOWSTONE.asItem()) return new ItemStack(net.minecraft.item.Items.GLOWSTONE_DUST, 4);
        if (item == net.minecraft.block.Blocks.SUGAR_CANE.asItem()) return new ItemStack(net.minecraft.item.Items.SUGAR, 2);
        if (item == net.minecraft.item.Items.BONE) return new ItemStack(net.minecraft.item.Items.BONE_MEAL, 3);
        if (item == net.minecraft.item.Items.BLAZE_ROD) return new ItemStack(net.minecraft.item.Items.BLAZE_POWDER, 2);
        return null;
    }
}
