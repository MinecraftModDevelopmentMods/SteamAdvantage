package zone.moddev.mc.steamadvantage.blocks;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import org.junit.Test;
import static org.junit.Assert.*;

public class DrillBitPersistenceTest {
    @Test public void allSixDirectionsSurviveNativeUpdatePackets() {
        for (Direction direction : Direction.values()) {
            DrillBitTileEntity tile = new DrillBitTileEntity();
            tile.setDirection(direction);
            CompoundNBT packet = tile.getUpdatePacket().getNbtCompound();
            assertEquals(direction.getIndex(), packet.getByte("dir"));
            DrillBitTileEntity restored = new DrillBitTileEntity();
            restored.onDataPacket(null, tile.getUpdatePacket());
            assertEquals(direction.getAxis(), restored.getDirection());
            assertEquals(direction.getIndex(), restored.getUpdatePacket().getNbtCompound().getByte("dir"));
        }
    }

    @Test public void oldDirectionKeyRemainsReadableAndCanonicalKeyWins() {
        CompoundNBT tag = new CompoundNBT();
        tag.putByte("d", (byte) Direction.EAST.getIndex());
        DrillBitTileEntity tile = new DrillBitTileEntity();
        tile.read(tag);
        assertEquals(Direction.Axis.X, tile.getDirection());
        tag.putByte("dir", (byte) Direction.NORTH.getIndex());
        tile.read(tag);
        assertEquals(Direction.Axis.Z, tile.getDirection());
    }

    @Test public void invalidDirectionFallsBackToDown() {
        CompoundNBT tag = new CompoundNBT();
        tag.putByte("dir", (byte) 127);
        DrillBitTileEntity tile = new DrillBitTileEntity();
        tile.read(tag);
        assertEquals(Direction.DOWN.getIndex(), tile.getUpdatePacket().getNbtCompound().getByte("dir"));
    }
}
