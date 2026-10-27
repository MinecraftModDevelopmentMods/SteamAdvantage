package zone.moddev.mc.steamadvantage.resources;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.resources.VanillaPack;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.tags.TagCollectionReader;
import net.minecraft.tags.TagRegistryManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.ForgeHooks;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.steamadvantage.util.FurnaceFuelHelper;
import static org.junit.Assert.*;

public class NativeFuelContractTest {
    @BeforeClass public static void bootstrapNativeFuel() {
        net.minecraft.util.registry.Bootstrap.bootStrap();
        // Standalone tests need the actual vanilla item tags before the native fuel map.
        try (SimpleReloadableResourceManager resources =
                     new SimpleReloadableResourceManager(ResourcePackType.SERVER_DATA)) {
            resources.add(new VanillaPack("minecraft"));
            TagCollectionReader<Item> reader =
                    new TagCollectionReader<>(Registry.ITEM::getOptional, "tags/items", "item");
            ITagCollection<Item> items = reader.load(reader.prepare(resources, Runnable::run).join());
            assertTrue(items.getAllTags().containsKey(new ResourceLocation("minecraft:non_flammable_wood")));
            ITagCollectionSupplier current = TagCollectionManager.getInstance();
            TagRegistryManager.get(new ResourceLocation("minecraft:item")).reset(
                    ITagCollectionSupplier.of(current.getBlocks(), items,
                            current.getFluids(), current.getEntityTypes()));
        }
        ForgeHooks.updateBurns();
    }

    @Test public void keepsExactVanillaAndEmptyFuelValues() {
        assertEquals(0, FurnaceFuelHelper.getBurnTime(ItemStack.EMPTY));
        assertEquals(1600, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.COAL)));
        assertEquals(20000, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.LAVA_BUCKET)));
    }

    @Test public void retainsTheForgeItemBurnTimeOverride() {
        Item item = new Item(new Item.Properties()) {
            @Override public int getBurnTime(ItemStack stack, IRecipeType<?> recipeType) {
                assertNull(recipeType);
                return 237;
            }
        };
        assertEquals(237, FurnaceFuelHelper.getBurnTime(new ItemStack(item)));
    }
}
