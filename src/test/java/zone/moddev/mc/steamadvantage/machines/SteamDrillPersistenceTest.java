package zone.moddev.mc.steamadvantage.machines;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SteamDrillPersistenceTest {
    @Test public void retainsLegacyProgressUntilWorldIsAttached() {
        SteamDrillTileEntity drill = new SteamDrillTileEntity();
        drill.read(target(94));
        drill.prepareDataFieldsForSync();
        assertEquals(94, drill.getDataFieldArray()[1]);
    }

    @Test public void clearsPreviousProgressOnReadingAnUntargetedTile() {
        SteamDrillTileEntity drill = new SteamDrillTileEntity();
        drill.read(target(94));
        drill.read(new NBTTagCompound());
        drill.prepareDataFieldsForSync();
        assertEquals(0, drill.getDataFieldArray()[1]);
    }

    private static NBTTagCompound target(int progress) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setShort("progress", (short) progress);
        tag.setInt("targetX", 2048);
        tag.setInt("targetY", 80);
        tag.setInt("targetZ", 2049);
        return tag;
    }
}
