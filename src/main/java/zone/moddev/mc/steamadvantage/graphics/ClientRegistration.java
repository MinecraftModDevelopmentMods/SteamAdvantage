package zone.moddev.mc.steamadvantage.graphics;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import zone.moddev.mc.steamadvantage.init.Items;
import zone.moddev.mc.steamadvantage.items.MusketItem;
import zone.moddev.mc.steamadvantage.init.Blocks;
import zone.moddev.mc.steamadvantage.init.Entities;

/** Client-only renderer registration. */
public final class ClientRegistration {
    private ClientRegistration() {}

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientRegistry.bindTileEntityRenderer(Entities.DRILLBIT, DrillBitRenderer::new);
            RenderTypeLookup.setRenderLayer(Blocks.steam_track, RenderType.cutout());
            ItemModelsProperties.register(Items.blackpowder_musket, new ResourceLocation("loaded"),
                    (item, world, entity) -> MusketItem.isLoaded(item) ? 1.0F : 0.0F);
            ItemModelsProperties.register(Items.blackpowder_musket, new ResourceLocation("loading"),
                    (item, world, entity) -> entity != null && entity.isUsingItem()
                            && entity.getUseItem() == item ? 1.0F : 0.0F);
        });
    }
}
