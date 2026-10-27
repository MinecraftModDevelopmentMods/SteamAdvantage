package zone.moddev.mc.steamadvantage.blocks;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import org.junit.Test;
import static org.junit.Assert.*;

public class DrillBitPersistenceTest {
    @org.junit.BeforeClass public static void bootstrapNativeRegistries() {
        net.minecraft.util.registry.Bootstrap.bootStrap();
    }

    @Test public void allSixDirectionsSurviveNativeUpdatePackets() {
        for (Direction direction : Direction.values()) {
            DrillBitTileEntity tile = new DrillBitTileEntity();
            tile.setDirection(direction);
            CompoundNBT packet = tile.getUpdatePacket().getTag();
            assertEquals(direction.get3DDataValue(), packet.getByte("dir"));
            DrillBitTileEntity restored = new DrillBitTileEntity();
            restored.onDataPacket(null, tile.getUpdatePacket());
            assertEquals(direction.getAxis(), restored.getDirection());
            assertEquals(direction.get3DDataValue(), restored.getUpdatePacket().getTag().getByte("dir"));
        }
    }

    @Test public void oldDirectionKeyRemainsReadableAndCanonicalKeyWins() {
        CompoundNBT tag = new CompoundNBT();
        tag.putByte("d", (byte) Direction.EAST.get3DDataValue());
        DrillBitTileEntity tile = new DrillBitTileEntity();
        tile.load(net.minecraft.block.Blocks.AIR.defaultBlockState(), tag);
        assertEquals(Direction.Axis.X, tile.getDirection());
        tag.putByte("dir", (byte) Direction.NORTH.get3DDataValue());
        tile.load(net.minecraft.block.Blocks.AIR.defaultBlockState(), tag);
        assertEquals(Direction.Axis.Z, tile.getDirection());
    }

    @Test public void invalidDirectionFallsBackToDown() {
        CompoundNBT tag = new CompoundNBT();
        tag.putByte("dir", (byte) 127);
        DrillBitTileEntity tile = new DrillBitTileEntity();
        tile.load(net.minecraft.block.Blocks.AIR.defaultBlockState(), tag);
        assertEquals(Direction.DOWN.get3DDataValue(), tile.getUpdatePacket().getTag().getByte("dir"));
    }
}
