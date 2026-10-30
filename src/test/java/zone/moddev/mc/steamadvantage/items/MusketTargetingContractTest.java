package zone.moddev.mc.steamadvantage.items;

import java.io.InputStream;
import java.util.Optional;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import static org.junit.Assert.*;

public class MusketTargetingContractTest {
    @BeforeClass public static void bootstrap() {
        net.minecraft.util.registry.Bootstrap.register();
    }

    @Test public void nearerTerrainRejectsTargetsBehindItOnEveryAxis() {
        Vec3d origin = Vec3d.ZERO;
        for (Direction direction : Direction.values()) {
            Vec3d ray = new Vec3d(direction.getXOffset(), direction.getYOffset(), direction.getZOffset());
            assertFalse(direction.toString(), hit(origin, ray.scale(3), around(ray.scale(5))).isPresent());
            Optional<Vec3d> hit = hit(origin, ray.scale(8), around(ray.scale(5)));
            assertTrue(direction.toString(), hit.isPresent());
            assertEquals(4.5 * 4.5, origin.squareDistanceTo(hit.get()), 0.000001);
        }
    }

    @Test public void terrainWinsAnExactTieWithTheEntitySurface() {
        assertFalse(hit(Vec3d.ZERO, new Vec3d(0, 0, 4.5),
                around(new Vec3d(0, 0, 5))).isPresent());
    }

    @Test public void finiteSegmentRejectsOffRayBehindAndBeyondRangeTargets() {
        Vec3d end = new Vec3d(0, 0, 64);
        assertFalse(hit(Vec3d.ZERO, end, around(new Vec3d(2, 0, 5))).isPresent());
        assertFalse(hit(Vec3d.ZERO, end, around(new Vec3d(0, 0, -5))).isPresent());
        assertFalse(hit(Vec3d.ZERO, end, around(new Vec3d(0, 0, 65))).isPresent());
    }

    @Test public void overlappingEntityStartsAtZeroDistance() {
        Optional<Vec3d> hit = hit(Vec3d.ZERO, new Vec3d(0, 0, 8), around(Vec3d.ZERO));
        assertTrue(hit.isPresent());
        assertEquals(Vec3d.ZERO, hit.get());
    }

    @Test public void targetingUsesLoadedServerEntitiesAndTerrainBeforeEntitySelection() throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = getClass().getResourceAsStream("/zone/moddev/mc/steamadvantage/items/MusketItem.class")) {
            assertNotNull(input);
            new ClassReader(input).accept(node, 0);
        }
        MethodNode trace = node.methods.stream().filter(method -> method.name.equals("rayTraceBlocksAndEntities"))
                .findFirst().orElseThrow(() -> new AssertionError("Missing targeting method"));
        boolean terrain = false, loadedEntities = false, boundedSelection = false, spectatorFilter = false;
        for (AbstractInsnNode instruction : trace.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.name.equals("rayTraceBlocks")) terrain = true;
            if (call.owner.equals("net/minecraft/world/server/ServerWorld") && call.name.equals("getEntities")) {
                assertTrue("Terrain must be traced before selecting entities", terrain);
                loadedEntities = true;
            }
            if (call.name.equals("rayHitBeforeTerrain")) boundedSelection = true;
            if (call.name.equals("isSpectator")) spectatorFilter = true;
        }
        assertTrue("Server targeting must use the loaded-entity collection", loadedEntities);
        assertTrue("Entity selection must be bounded by the actual terrain hit", boundedSelection);
        assertTrue("Loaded-entity iteration must preserve the previous spectator exclusion", spectatorFilter);
    }

    @SuppressWarnings("unchecked")
    private static Optional<Vec3d> hit(Vec3d origin, Vec3d end, AxisAlignedBB target) {
        try {
            java.lang.reflect.Method method = MusketItem.class.getDeclaredMethod(
                    "rayHitBeforeTerrain", Vec3d.class, Vec3d.class, AxisAlignedBB.class);
            method.setAccessible(true);
            return (Optional<Vec3d>) method.invoke(null, origin, end, target);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Musket targeting must be bounded by terrain and range", failure);
        }
    }

    private static AxisAlignedBB around(Vec3d center) {
        return new AxisAlignedBB(center.x - 0.5, center.y - 0.5, center.z - 0.5,
                center.x + 0.5, center.y + 0.5, center.z + 0.5);
    }
}
