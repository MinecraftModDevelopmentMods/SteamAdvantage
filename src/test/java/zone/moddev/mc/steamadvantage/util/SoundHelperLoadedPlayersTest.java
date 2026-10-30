package zone.moddev.mc.steamadvantage.util;

import java.io.InputStream;
import net.minecraft.util.math.AxisAlignedBB;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import static org.junit.Assert.*;

public class SoundHelperLoadedPlayersTest {
    @Test public void soundDispatchNeverScansEntitiesOrLoadsChunks() throws Exception {
        int loadedPlayerIterations = 0;
        for (MethodNode method : soundHelper().methods) {
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode field = (FieldInsnNode) instruction;
                    if (field.getOpcode() == Opcodes.GETFIELD && field.owner.equals("net/minecraft/world/World")
                            && field.name.equals("playerEntities")) loadedPlayerIterations++;
                }
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                assertFalse("Entity query reintroduced in " + method.name,
                        call.name.equals("getEntitiesWithinAABB") || call.name.equals("getEntitiesWithinAABBExcludingEntity")
                                || call.name.equals("getEntities"));
                assertFalse("Chunk query reintroduced in " + method.name,
                        call.name.startsWith("getChunk") || call.name.equals("getLoadedChunk"));
            }
        }
        assertEquals("Both sound ranges must iterate the existing loaded-player list", 2, loadedPlayerIterations);
    }

    @Test public void bothRangesKeepBoundsServerPlayerAndConnectionGuards() throws Exception {
        verifyDispatch("playSoundAtPosition", 16);
        verifyDispatch("playBigSoundAtPosition", 50);
    }

    @Test public void bothSoundRangesExplicitlyExcludeSpectatorsBeforeSending() throws Exception {
        int checked = 0;
        for (MethodNode method : soundHelper().methods) {
            if (!method.name.equals("playSoundAtPosition") && !method.name.equals("playBigSoundAtPosition")) continue;
            int predicate = -1, tested = -1, packet = -1;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode field = (FieldInsnNode) instruction;
                    if (field.getOpcode() == Opcodes.GETSTATIC && field.owner.equals("net/minecraft/util/EntitySelectors")
                            && field.name.equals("NOT_SPECTATING")) predicate = method.instructions.indexOf(instruction);
                } else if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (call.owner.equals("java/util/function/Predicate") && call.name.equals("test")) {
                        tested = method.instructions.indexOf(instruction);
                        AbstractInsnNode guard = instruction.getNext();
                        while (guard != null && guard.getOpcode() < 0) guard = guard.getNext();
                        assertTrue(method.name + " rejects a false native spectator predicate",
                                guard instanceof JumpInsnNode && guard.getOpcode() == Opcodes.IFEQ);
                    }
                    if (call.owner.equals("net/minecraft/network/play/server/SPacketCustomSound") && call.name.equals("<init>")) {
                        packet = method.instructions.indexOf(instruction);
                    }
                }
            }
            assertTrue(method.name + " applies native NOT_SPECTATING before creating the sound packet",
                    predicate >= 0 && tested > predicate && packet > tested);
            checked++;
        }
        assertEquals("Both sound ranges preserve spectator exclusion", 2, checked);
    }

    @Test public void rangeSelectionKeepsStrictAabbOverlapNotDistanceOrPlayerCenter() {
        for (double radius : new double[]{16, 50}) {
            AxisAlignedBB area = new AxisAlignedBB(-radius, -radius, -radius, radius, radius, radius);
            assertTrue("An overlapping player body is included even with its center outside the cube",
                    area.intersects(new AxisAlignedBB(radius - 0.1, -0.5, -0.3, radius + 0.5, 1.3, 0.3)));
            assertTrue("Corner overlap remains included, not replaced with a spherical radius",
                    area.intersects(new AxisAlignedBB(radius - 0.1, radius - 0.1, radius - 0.1,
                            radius + 0.5, radius + 1.7, radius + 0.5)));
            assertFalse("Merely touching the positive boundary is not an intersection",
                    area.intersects(new AxisAlignedBB(radius, -0.5, -0.3, radius + 0.6, 1.3, 0.3)));
            assertFalse("Merely touching the negative boundary is not an intersection",
                    area.intersects(new AxisAlignedBB(-radius - 0.6, -0.5, -0.3, -radius, 1.3, 0.3)));
        }
    }

    private static void verifyDispatch(String name, double radius) throws Exception {
        MethodNode method = soundHelper().methods.stream().filter(candidate -> candidate.name.equals(name))
                .findFirst().orElseThrow(() -> new AssertionError("Missing sound dispatch " + name));
        int playerLists = 0, bounds = 0, intersections = 0, soundPackets = 0;
        boolean correctRadius = false, serverPlayer = false, connection = false, nullGuard = false;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.name.equals("getBoundingBox")) bounds++;
                if (call.owner.equals("net/minecraft/util/math/AxisAlignedBB") && call.name.equals("intersects")
                        && call.desc.equals("(Lnet/minecraft/util/math/AxisAlignedBB;)Z")) intersections++;
                if (call.owner.equals("net/minecraft/network/play/server/SPacketCustomSound") && call.name.equals("<init>")) soundPackets++;
            } else if (instruction instanceof LdcInsnNode) {
                Object value = ((LdcInsnNode) instruction).cst;
                if (value instanceof Double && ((Double) value).doubleValue() == radius) correctRadius = true;
            } else if (instruction instanceof TypeInsnNode) {
                TypeInsnNode type = (TypeInsnNode) instruction;
                if (type.getOpcode() == Opcodes.INSTANCEOF && type.desc.equals("net/minecraft/entity/player/EntityPlayerMP")) serverPlayer = true;
            } else if (instruction instanceof FieldInsnNode) {
                FieldInsnNode field = (FieldInsnNode) instruction;
                if (field.getOpcode() == Opcodes.GETFIELD && field.owner.equals("net/minecraft/world/World")
                        && field.name.equals("playerEntities")) playerLists++;
                if (field.getOpcode() == Opcodes.GETFIELD && field.name.equals("connection")) connection = true;
            } else if (instruction instanceof JumpInsnNode && instruction.getOpcode() == Opcodes.IFNULL) nullGuard = true;
        }
        assertEquals(name, 1, playerLists);
        assertEquals(name, 1, bounds);
        assertEquals(name, 1, intersections);
        assertEquals(name, 1, soundPackets);
        assertTrue(name + " keeps its exact range", correctRadius);
        assertTrue(name + " requires a server player", serverPlayer);
        assertTrue(name + " guards the connection before sending", connection && nullGuard);
    }

    private static ClassNode soundHelper() throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = SoundHelperLoadedPlayersTest.class.getResourceAsStream(
                "/zone/moddev/mc/steamadvantage/util/SoundHelper.class")) {
            assertNotNull("Compiled SoundHelper is available", input);
            new ClassReader(input).accept(node, 0);
        }
        return node;
    }
}
