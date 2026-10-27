package zone.moddev.mc.steamadvantage.graphics;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Direction;
import org.junit.Test;
import static org.junit.Assert.*;

public class DrillBitRendererTest {
    private static final int LIGHT = 0x00D000A0;
    private static final int OVERLAY = 0x00090007;
    private static final double EPSILON = 0.000001D;

    @Test public void everyFacingKeepsLegacyDimensionsUvsAndNativeVertexAttributes() {
        for (Direction facing : Direction.values()) {
            RecordingBuffer buffer = new RecordingBuffer();
            MatrixStack matrices = new MatrixStack();
            MatrixStack.Entry caller = matrices.getLast();
            DrillBitRenderer.renderGeometry(facing.getAxis(), 0, matrices, buffer, LIGHT, OVERLAY);
            assertSame(caller, matrices.getLast());
            assertEquals(24, buffer.vertices.size());
            double[] minimum = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
            double[] maximum = { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
            for (int i = 0; i < buffer.vertices.size(); i++) {
                Vertex vertex = buffer.vertices.get(i);
                for (int coordinate = 0; coordinate < 3; coordinate++) {
                    minimum[coordinate] = Math.min(minimum[coordinate], vertex.position[coordinate]);
                    maximum[coordinate] = Math.max(maximum[coordinate], vertex.position[coordinate]);
                }
                int corner = i % 4;
                float expectedU = (i < 16 ? 0 : 0.5F) + (corner >= 2 ? 0.5F : 0);
                float expectedV = (corner == 1 || corner == 2) ? (i < 16 ? 1 : 0.5F) : 0;
                assertEquals(expectedU, vertex.u, 0);
                assertEquals(expectedV, vertex.v, 0);
                assertEquals(LIGHT, vertex.light);
                assertEquals(OVERLAY, vertex.overlay);
                assertArrayEquals(new int[] { 255, 255, 255, 255 }, vertex.color);
                double normalLength = 0;
                for (float component : vertex.normal) normalLength += component * component;
                assertEquals(1, normalLength, EPSILON);
            }
            for (int coordinate = 0; coordinate < 3; coordinate++) {
                boolean alongDrill = Direction.Axis.values()[coordinate] == facing.getAxis();
                assertEquals(alongDrill ? 0 : 0.25D, minimum[coordinate], EPSILON);
                assertEquals(alongDrill ? 1 : 0.75D, maximum[coordinate], EPSILON);
            }
        }
    }

    @Test public void rotationTransformsBothPositionsAndNormals() {
        RecordingBuffer buffer = new RecordingBuffer();
        DrillBitRenderer.renderGeometry(Direction.Axis.Y, 90, new MatrixStack(), buffer, LIGHT, OVERLAY);
        Vertex first = buffer.vertices.get(0);
        assertArrayEquals(new double[] { 0.25D, 1, 0.25D }, first.position, EPSILON);
        assertArrayEquals(new float[] { -1, 0, 0 }, first.normal, (float) EPSILON);
    }

    @Test public void failedVertexSubmissionRestoresTheCallerMatrix() {
        MatrixStack matrices = new MatrixStack();
        MatrixStack.Entry caller = matrices.getLast();
        try {
            DrillBitRenderer.renderGeometry(Direction.Axis.X, 37, matrices, new RecordingBuffer() {
                @Override public void endVertex() { throw new IllegalStateException("submission failed"); }
            }, LIGHT, OVERLAY);
            fail("Expected vertex submission failure");
        } catch (IllegalStateException expected) {
            assertEquals("submission failed", expected.getMessage());
        }
        assertSame(caller, matrices.getLast());
    }

    private static final class Vertex {
        final double[] position = new double[3];
        final int[] color = new int[4];
        final float[] normal = new float[3];
        float u, v;
        int overlay, light;
    }

    private static class RecordingBuffer implements IVertexBuilder {
        final List<Vertex> vertices = new ArrayList<>();
        private Vertex vertex = new Vertex();

        @Override public IVertexBuilder pos(double x, double y, double z) {
            vertex.position[0] = x; vertex.position[1] = y; vertex.position[2] = z;
            return this;
        }
        @Override public IVertexBuilder color(int red, int green, int blue, int alpha) {
            vertex.color[0] = red; vertex.color[1] = green; vertex.color[2] = blue; vertex.color[3] = alpha;
            return this;
        }
        @Override public IVertexBuilder tex(float u, float v) { vertex.u = u; vertex.v = v; return this; }
        @Override public IVertexBuilder overlay(int u, int v) { vertex.overlay = u | v << 16; return this; }
        @Override public IVertexBuilder lightmap(int u, int v) { vertex.light = u | v << 16; return this; }
        @Override public IVertexBuilder normal(float x, float y, float z) {
            vertex.normal[0] = x; vertex.normal[1] = y; vertex.normal[2] = z;
            return this;
        }
        @Override public void endVertex() { vertices.add(vertex); vertex = new Vertex(); }
    }
}
