package zone.moddev.mc.steamadvantage.graphics;

import zone.moddev.mc.steamadvantage.SteamAdvantage;
import zone.moddev.mc.steamadvantage.blocks.DrillBitTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT) // This is needed for classes that extend client-only classes
public class DrillBitRenderer extends TileEntityRenderer<DrillBitTileEntity>{


	private final ResourceLocation texture = new ResourceLocation(SteamAdvantage.MODID+":textures/entity/drill_bit.png");

	public DrillBitRenderer() {
	}


	@Override
	public void render(final DrillBitTileEntity te, final double x, final double y, final double z, final float partialTick, final int destroyStage) {
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		render(te,te.getWorld(),te.getPos(),partialTick);
		GlStateManager.popMatrix();
	}

	private void render(DrillBitTileEntity e, World world, BlockPos pos, float partialTick){
		final Tessellator instance = Tessellator.getInstance();
		final BufferBuilder worldRenderer = instance.getBuffer();

		this.bindTexture(texture);

		//This will make your block brightness dependent from surroundings lighting.
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        if (Minecraft.isAmbientOcclusionEnabled())
        {
            GlStateManager.shadeModel(7425);
        }
        else
        {
            GlStateManager.shadeModel(7424);
        }

		final float sideU0 = 0;
		final float sideU1 = 0.5f;
		final float sideV0 = 0;
		final float sideV1 = 1f;
		final float endU0 = 0.5f;
		final float endU1 = 1;
		final float endV0 = 0;
		final float endV1 = 0.5f;
		final float radius = 0.25f;

		GlStateManager.translatef(0.5f, 0.5f, 0.5f);
		if(e.getDirection() != EnumFacing.Axis.Y){
			GlStateManager.rotatef(90, 1.0f, 0.0f, 0.0f);
			if(e.getDirection() != EnumFacing.Axis.Z){
				GlStateManager.rotatef(90, 0.0f, 0.0f, 1.0f);
			}
		}
		GlStateManager.rotatef(e.rotation + DrillBitTileEntity.ROTATION_PER_TICK * partialTick, 0.0f, 1.0f, 0.0f);

		worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldRenderer.pos( radius, 0.5f, -radius).tex( sideU0, sideV0).endVertex();
		worldRenderer.pos( radius,-0.5f, -radius).tex( sideU0, sideV1).endVertex();
		worldRenderer.pos(-radius,-0.5f, -radius).tex( sideU1, sideV1).endVertex();
		worldRenderer.pos(-radius, 0.5f, -radius).tex( sideU1, sideV0).endVertex();

		worldRenderer.pos(-radius, 0.5f,  radius).tex( sideU0, sideV0).endVertex();
		worldRenderer.pos(-radius,-0.5f,  radius).tex( sideU0, sideV1).endVertex();
		worldRenderer.pos( radius,-0.5f,  radius).tex( sideU1, sideV1).endVertex();
		worldRenderer.pos( radius, 0.5f,  radius).tex( sideU1, sideV0).endVertex();

		worldRenderer.pos(-radius, 0.5f, -radius).tex( sideU0, sideV0).endVertex();
		worldRenderer.pos(-radius,-0.5f, -radius).tex( sideU0, sideV1).endVertex();
		worldRenderer.pos(-radius,-0.5f,  radius).tex( sideU1, sideV1).endVertex();
		worldRenderer.pos(-radius, 0.5f,  radius).tex( sideU1, sideV0).endVertex();

		worldRenderer.pos( radius, 0.5f,  radius).tex( sideU0, sideV0).endVertex();
		worldRenderer.pos( radius,-0.5f,  radius).tex( sideU0, sideV1).endVertex();
		worldRenderer.pos( radius,-0.5f, -radius).tex( sideU1, sideV1).endVertex();
		worldRenderer.pos( radius, 0.5f, -radius).tex( sideU1, sideV0).endVertex();

		worldRenderer.pos(-radius, 0.5f, -radius).tex( endU0, endV0).endVertex();
		worldRenderer.pos(-radius, 0.5f,  radius).tex( endU0, endV1).endVertex();
		worldRenderer.pos( radius, 0.5f,  radius).tex( endU1, endV1).endVertex();
		worldRenderer.pos( radius, 0.5f, -radius).tex( endU1, endV0).endVertex();

		worldRenderer.pos(-radius,-0.5f,  radius).tex( endU0, endV0).endVertex();
		worldRenderer.pos(-radius,-0.5f, -radius).tex( endU0, endV1).endVertex();
		worldRenderer.pos( radius,-0.5f, -radius).tex( endU1, endV1).endVertex();
		worldRenderer.pos( radius,-0.5f,  radius).tex( endU1, endV0).endVertex();

		instance.draw();
        RenderHelper.enableStandardItemLighting();

	}


}
