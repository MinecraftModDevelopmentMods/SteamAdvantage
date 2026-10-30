package zone.moddev.mc.steamadvantage.graphics;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import zone.moddev.mc.steamadvantage.SteamAdvantage;
import zone.moddev.mc.steamadvantage.blocks.DrillBitTileEntity;

@OnlyIn(Dist.CLIENT)
public class DrillBitRenderer extends TileEntityRenderer<DrillBitTileEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(SteamAdvantage.MODID, "textures/entity/drill_bit.png");

    public DrillBitRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(DrillBitTileEntity tile, float partialTick, MatrixStack matrices,
            IRenderTypeBuffer buffers, int light, int overlay) {
        renderGeometry(tile.getDirection(), tile.rotation + DrillBitTileEntity.ROTATION_PER_TICK * partialTick,
                matrices, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), light, overlay);
    }

    static void renderGeometry(Direction.Axis axis, float rotation, MatrixStack matrices,
            IVertexBuilder vertices, int light, int overlay) {
        matrices.pushPose();
        try {
            matrices.translate(0.5D, 0.5D, 0.5D);
            if (axis != Direction.Axis.Y) {
                matrices.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                if (axis != Direction.Axis.Z) matrices.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
            }
            matrices.mulPose(Vector3f.YP.rotationDegrees(rotation));
            MatrixStack.Entry transform = matrices.last();
            final float radius = 0.25F;
            // Retain the original six quads and UV layout; native buffers supply lighting and overlay.
            vertex(transform, vertices, radius, 0.5F, -radius, 0, 0, 0, 0, -1, light, overlay);
            vertex(transform, vertices, radius, -0.5F, -radius, 0, 1, 0, 0, -1, light, overlay);
            vertex(transform, vertices, -radius, -0.5F, -radius, 0.5F, 1, 0, 0, -1, light, overlay);
            vertex(transform, vertices, -radius, 0.5F, -radius, 0.5F, 0, 0, 0, -1, light, overlay);

            vertex(transform, vertices, -radius, 0.5F, radius, 0, 0, 0, 0, 1, light, overlay);
            vertex(transform, vertices, -radius, -0.5F, radius, 0, 1, 0, 0, 1, light, overlay);
            vertex(transform, vertices, radius, -0.5F, radius, 0.5F, 1, 0, 0, 1, light, overlay);
            vertex(transform, vertices, radius, 0.5F, radius, 0.5F, 0, 0, 0, 1, light, overlay);

            vertex(transform, vertices, -radius, 0.5F, -radius, 0, 0, -1, 0, 0, light, overlay);
            vertex(transform, vertices, -radius, -0.5F, -radius, 0, 1, -1, 0, 0, light, overlay);
            vertex(transform, vertices, -radius, -0.5F, radius, 0.5F, 1, -1, 0, 0, light, overlay);
            vertex(transform, vertices, -radius, 0.5F, radius, 0.5F, 0, -1, 0, 0, light, overlay);

            vertex(transform, vertices, radius, 0.5F, radius, 0, 0, 1, 0, 0, light, overlay);
            vertex(transform, vertices, radius, -0.5F, radius, 0, 1, 1, 0, 0, light, overlay);
            vertex(transform, vertices, radius, -0.5F, -radius, 0.5F, 1, 1, 0, 0, light, overlay);
            vertex(transform, vertices, radius, 0.5F, -radius, 0.5F, 0, 1, 0, 0, light, overlay);

            vertex(transform, vertices, -radius, 0.5F, -radius, 0.5F, 0, 0, 1, 0, light, overlay);
            vertex(transform, vertices, -radius, 0.5F, radius, 0.5F, 0.5F, 0, 1, 0, light, overlay);
            vertex(transform, vertices, radius, 0.5F, radius, 1, 0.5F, 0, 1, 0, light, overlay);
            vertex(transform, vertices, radius, 0.5F, -radius, 1, 0, 0, 1, 0, light, overlay);

            vertex(transform, vertices, -radius, -0.5F, radius, 0.5F, 0, 0, -1, 0, light, overlay);
            vertex(transform, vertices, -radius, -0.5F, -radius, 0.5F, 0.5F, 0, -1, 0, light, overlay);
            vertex(transform, vertices, radius, -0.5F, -radius, 1, 0.5F, 0, -1, 0, light, overlay);
            vertex(transform, vertices, radius, -0.5F, radius, 1, 0, 0, -1, 0, light, overlay);
        } finally {
            matrices.popPose();
        }
    }

    private static void vertex(MatrixStack.Entry transform, IVertexBuilder vertices,
            float x, float y, float z, float u, float v,
            float normalX, float normalY, float normalZ, int light, int overlay) {
        vertices.vertex(transform.pose(), x, y, z).color(255, 255, 255, 255).uv(u, v)
                .overlayCoords(overlay).uv2(light)
                .normal(transform.normal(), normalX, normalY, normalZ).endVertex();
    }
}
