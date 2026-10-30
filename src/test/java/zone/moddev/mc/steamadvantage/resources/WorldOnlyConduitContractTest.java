package zone.moddev.mc.steamadvantage.resources;

import java.io.InputStream;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import static org.junit.Assert.*;

public class WorldOnlyConduitContractTest {
    @Test public void everyDrillCallbackUsesTheWorldOnlyParentOverload() throws Exception {
        int callbacks = 0;
        for (String type : new String[] {"SteamDrillBlock", "SteamDrillTileEntity"}) {
            ClassNode node = new ClassNode();
            try (InputStream input = getClass().getResourceAsStream(
                    "/zone/moddev/mc/steamadvantage/machines/" + type + ".class")) {
                assertNotNull(input);
                new ClassReader(input).accept(node, 0);
            }
            for (MethodNode method : node.methods) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (!call.owner.equals("zone/moddev/mc/poweradvantage/conduitnetwork/ConduitRegistry")
                            || !call.name.startsWith("conduitBlock")) continue;
                    assertTrue(type + "." + method.name + call.desc,
                            call.desc.startsWith("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"));
                    callbacks++;
                }
            }
        }
        assertEquals(4, callbacks);
    }
}
