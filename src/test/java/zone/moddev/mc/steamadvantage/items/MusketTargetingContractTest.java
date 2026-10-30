package zone.moddev.mc.steamadvantage.items;

import java.io.InputStream;
import java.util.Optional;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
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
        net.minecraft.util.registry.Bootstrap.bootStrap();
    }

    @Test public void nearerTerrainRejectsTargetsBehindItOnEveryAxis() {
        Vector3d origin = Vector3d.ZERO;
        for (Direction direction : Direction.values()) {
            Vector3d ray = new Vector3d(direction.getStepX(), direction.getStepY(), direction.getStepZ());
            assertFalse(direction.toString(), MusketItem.rayHitBeforeTerrain(origin, ray.scale(3), around(ray.scale(5))).isPresent());
            Optional<Vector3d> hit = MusketItem.rayHitBeforeTerrain(origin, ray.scale(8), around(ray.scale(5)));
            assertTrue(direction.toString(), hit.isPresent());
            assertEquals(4.5 * 4.5, origin.distanceToSqr(hit.get()), 0.000001);
        }
    }

    @Test public void terrainWinsAnExactTieWithTheEntitySurface() {
        assertFalse(MusketItem.rayHitBeforeTerrain(Vector3d.ZERO, new Vector3d(0, 0, 4.5),
                around(new Vector3d(0, 0, 5))).isPresent());
    }

    @Test public void finiteSegmentRejectsOffRayBehindAndBeyondRangeTargets() {
        Vector3d end = new Vector3d(0, 0, 64);
        assertFalse(MusketItem.rayHitBeforeTerrain(Vector3d.ZERO, end, around(new Vector3d(2, 0, 5))).isPresent());
        assertFalse(MusketItem.rayHitBeforeTerrain(Vector3d.ZERO, end, around(new Vector3d(0, 0, -5))).isPresent());
        assertFalse(MusketItem.rayHitBeforeTerrain(Vector3d.ZERO, end, around(new Vector3d(0, 0, 65))).isPresent());
    }

    @Test public void overlappingEntityStartsAtZeroDistance() {
        Optional<Vector3d> hit = MusketItem.rayHitBeforeTerrain(Vector3d.ZERO, new Vector3d(0, 0, 8), around(Vector3d.ZERO));
        assertTrue(hit.isPresent());
        assertEquals(Vector3d.ZERO, hit.get());
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
            if (call.name.equals("clip")) terrain = true;
            if (call.owner.equals("net/minecraft/world/server/ServerWorld") && call.name.equals("getAllEntities")) {
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

    private static AxisAlignedBB around(Vector3d center) {
        return new AxisAlignedBB(center.x - 0.5, center.y - 0.5, center.z - 0.5,
                center.x + 0.5, center.y + 0.5, center.z + 0.5);
    }
}
