package zone.moddev.mc.steamadvantage.items;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumActionResult;
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
import org.objectweb.asm.tree.VarInsnNode;
import static org.junit.Assert.*;

/** Executes the compiled use-method guard, without inventing a world or a player. */
public class MusketLoadedCartridgeTest {
    @BeforeClass public static void bootstrap() {
        zone.moddev.mc.steamadvantage.support.SteamNativeTestBootstrap.register();
    }

    @Test public void lastLoadedCartridgeCanBeAimedWithoutSpareAmmunition() throws Exception {
        ItemStack stack = new ItemStack(Items.STICK);
        MusketItem.finishLoad(stack);
        assertEquals("USE", guardResult(stack, false, EnumHand.MAIN_HAND));
        assertTrue("Aiming must not unload the cartridge", MusketItem.isLoaded(stack));
    }

    @Test public void unfinishedReloadCanBeCompletedWithoutAnotherCartridge() throws Exception {
        ItemStack stack = new ItemStack(Items.STICK);
        MusketItem.startLoad(stack);
        assertEquals("USE", guardResult(stack, false, EnumHand.MAIN_HAND));
        assertTrue(MusketItem.isAlmostLoaded(stack));
    }

    @Test public void unloadedMusketStillRejectsMissingAmmunition() throws Exception {
        ItemStack stack = new ItemStack(Items.STICK);
        assertEquals("FAIL", guardResult(stack, false, EnumHand.MAIN_HAND));
        assertTrue(MusketItem.isNotLoaded(stack));
    }

    @Test public void offHandDoesNotStartAimingOrConsumeTheLoadedCartridge() throws Exception {
        ItemStack stack = new ItemStack(Items.STICK);
        MusketItem.finishLoad(stack);
        assertEquals("PASS", guardResult(stack, false, EnumHand.OFF_HAND));
        assertTrue(MusketItem.isLoaded(stack));
    }

    private static String guardResult(ItemStack stack, boolean hasAmmo, EnumHand hand) throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = MusketLoadedCartridgeTest.class.getResourceAsStream(
                "/zone/moddev/mc/steamadvantage/items/MusketItem.class")) {
            assertNotNull(input);
            new ClassReader(input).accept(node, 0);
        }
        MethodNode use = node.methods.stream().filter(method -> method.name.equals("onItemRightClick"))
                .findFirst().orElseThrow(() -> new AssertionError("Missing native use method"));
        Object[] locals = {new Object(), new Object(), new Object(), hand, null};
        List<Object> operands = new ArrayList<>();
        int steps = 0;
        for (AbstractInsnNode instruction = use.instructions.getFirst(); instruction != null;) {
            assertTrue("The finite use guard must terminate", steps++ < 150);
            AbstractInsnNode next = instruction.getNext();
            int opcode = instruction.getOpcode();
            if (opcode < 0) {
                instruction = next;
                continue;
            }
            if (instruction instanceof VarInsnNode) {
                int variable = ((VarInsnNode) instruction).var;
                if (opcode == Opcodes.ALOAD) operands.add(locals[variable]);
                else if (opcode == Opcodes.ASTORE) locals[variable] = pop(operands);
                else fail("Unsupported guard variable opcode " + opcode);
            } else if (instruction instanceof FieldInsnNode) {
                FieldInsnNode field = (FieldInsnNode) instruction;
                assertEquals(Opcodes.GETSTATIC, opcode);
                if (field.owner.equals("net/minecraft/util/EnumHand")) {
                    assertEquals("MAIN_HAND", field.name);
                    operands.add(EnumHand.MAIN_HAND);
                } else if (field.owner.equals("net/minecraft/util/EnumActionResult")) {
                    operands.add(EnumActionResult.valueOf(field.name));
                } else throw new AssertionError("Unexpected constant before the aiming decision: " + field.owner + "." + field.name);
            } else if (instruction instanceof JumpInsnNode) {
                boolean jump;
                switch (opcode) {
                    case Opcodes.GOTO: jump = true; break;
                    case Opcodes.IFEQ: jump = !((Boolean) pop(operands)); break;
                    case Opcodes.IFNE: jump = (Boolean) pop(operands); break;
                    case Opcodes.IF_ACMPEQ: jump = pop(operands) == pop(operands); break;
                    case Opcodes.IF_ACMPNE: jump = pop(operands) != pop(operands); break;
                    default: throw new AssertionError("Unsupported guard jump " + opcode);
                }
                if (jump) next = ((JumpInsnNode) instruction).label;
            } else if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.name.equals("getHeldItem")) {
                    assertSame(hand, pop(operands));
                    assertSame(locals[2], pop(operands));
                    operands.add(stack);
                } else if (call.name.equals("isNotLoaded")) {
                    assertEquals("zone/moddev/mc/steamadvantage/items/MusketItem", call.owner);
                    operands.add(MusketItem.isNotLoaded((ItemStack) pop(operands)));
                } else if (call.name.equals("hasAmmo")) {
                    assertSame(stack, pop(operands));
                    assertSame(locals[2], pop(operands));
                    operands.add(hasAmmo);
                } else if (call.name.equals("newResult")) {
                    assertEquals("net/minecraft/util/ActionResult", call.owner);
                    assertSame(stack, pop(operands));
                    return ((EnumActionResult) pop(operands)).name();
                } else if (call.name.equals("setActiveHand")) return "USE";
                else throw new AssertionError("Unexpected call before the aiming decision: " + call.owner + "." + call.name);
            } else throw new AssertionError("Unsupported guard instruction " + opcode);
            instruction = next;
        }
        throw new AssertionError("Use method never reached an observable aiming decision");
    }

    private static Object pop(List<Object> stack) {
        assertFalse("Compiled guard operand stack is not empty", stack.isEmpty());
        return stack.remove(stack.size() - 1);
    }
}
