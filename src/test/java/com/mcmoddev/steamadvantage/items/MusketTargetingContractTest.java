package com.mcmoddev.steamadvantage.items;

import java.io.InputStream;
import java.util.Optional;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import static org.junit.Assert.*;

public class MusketTargetingContractTest {
    @BeforeClass public static void bootstrap() {
        net.minecraft.init.Bootstrap.register();
    }

    @Test public void nearerTerrainRejectsTargetsBehindItOnEveryAxis() {
        Vec3d origin = Vec3d.ZERO;
        for (EnumFacing direction : EnumFacing.values()) {
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

    @Test public void targetingUsesThePortableSixCoordinateAabbConstructor() throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = getClass().getResourceAsStream("/com/mcmoddev/steamadvantage/items/MusketItem.class")) {
            assertNotNull(input);
            new ClassReader(input).accept(node, 0);
        }
        MethodNode trace = node.methods.stream().filter(method -> method.name.equals("rayTraceBlocksAndEntities"))
                .findFirst().orElseThrow(() -> new AssertionError("Missing targeting method"));
        requirePortableBounds(trace);
    }

    @Test public void constructorAuditRejectsTheClientOverloadAndMissingBounds() {
        MethodNode synthetic = new MethodNode();
        synthetic.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,
                "net/minecraft/util/math/AxisAlignedBB", "<init>",
                "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)V", false));
        assertThrows("Reject the constructor that broke packaged server targeting", AssertionError.class,
                () -> requirePortableBounds(synthetic));
        synthetic.instructions.clear();
        assertThrows("An absent bounds check cannot pass vacuously", AssertionError.class,
                () -> requirePortableBounds(synthetic));
        synthetic.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,
                "net/minecraft/util/math/AxisAlignedBB", "<init>", "(DDDDDD)V", false));
        requirePortableBounds(synthetic);
    }

    @Test public void targetingUsesLoadedServerEntitiesAndTerrainBeforeEntitySelection() throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = getClass().getResourceAsStream("/com/mcmoddev/steamadvantage/items/MusketItem.class")) {
            assertNotNull(input);
            new ClassReader(input).accept(node, 0);
        }
        MethodNode trace = node.methods.stream().filter(method -> method.name.equals("rayTraceBlocksAndEntities"))
                .findFirst().orElseThrow(() -> new AssertionError("Missing targeting method"));
        boolean terrain = false, loadedEntities = false, boundedSelection = false;
        boolean spectatorPredicate = false, spectatorFilter = false;
        for (AbstractInsnNode instruction : trace.instructions.toArray()) {
            if (instruction instanceof FieldInsnNode) {
                FieldInsnNode field = (FieldInsnNode) instruction;
                if (field.getOpcode() == Opcodes.GETFIELD
                        && field.owner.equals("net/minecraft/world/World") && field.name.equals("loadedEntityList")) {
                    assertTrue("Terrain must be traced before selecting entities", terrain);
                    loadedEntities = true;
                }
                if (field.getOpcode() == Opcodes.GETSTATIC
                        && field.owner.equals("net/minecraft/util/EntitySelectors") && field.name.equals("NOT_SPECTATING")) {
                    spectatorPredicate = true;
                }
            }
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.name.equals("rayTraceBlocks")) terrain = true;
            if (call.name.equals("rayHitBeforeTerrain")) boundedSelection = true;
            if (spectatorPredicate && call.owner.equals("com/google/common/base/Predicate") && call.name.equals("apply")) {
                AbstractInsnNode guard = instruction.getNext();
                while (guard != null && guard.getOpcode() < 0) guard = guard.getNext();
                assertTrue("The native spectator predicate must reject a false result",
                        guard instanceof JumpInsnNode && guard.getOpcode() == Opcodes.IFEQ);
                spectatorFilter = true;
            }
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

    private static void requirePortableBounds(MethodNode method) {
        int boxes = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!call.owner.equals("net/minecraft/util/math/AxisAlignedBB") || !call.name.equals("<init>")) continue;
            assertEquals("Server targeting must use the six-coordinate constructor", "(DDDDDD)V", call.desc);
            boxes++;
        }
        assertTrue("Targeting retains real finite query bounds", boxes > 0);
    }

    private static AxisAlignedBB around(Vec3d center) {
        return new AxisAlignedBB(center.x - 0.5, center.y - 0.5, center.z - 0.5,
                center.x + 0.5, center.y + 0.5, center.z + 0.5);
    }
}
