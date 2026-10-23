package com.mcmoddev.steamadvantage.machines;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.*;

public class SteamDrillPersistenceTest {
    @Test public void retainsLegacyProgressBeforeWorldAttachment() {
        SteamDrillTileEntity drill = new SteamDrillTileEntity();
        drill.readFromNBT(target(17));
        drill.prepareDataFieldsForSync();
        assertEquals(17, drill.getDataFieldArray()[1]);
    }

    @Test public void untargetedDataClearsPendingWork() throws Exception {
        SteamDrillTileEntity drill = new SteamDrillTileEntity();
        drill.readFromNBT(target(17));
        drill.readFromNBT(new NBTTagCompound());
        drill.prepareDataFieldsForSync();
        assertEquals(0, drill.getDataFieldArray()[1]);
        java.lang.reflect.Field target = SteamDrillTileEntity.class.getDeclaredField("targetBlockCoord");
        target.setAccessible(true);
        assertNull(target.get(drill));
    }

    private static NBTTagCompound target(int progress) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setShort("progress", (short) progress);
        tag.setInteger("targetX", 2048);
        tag.setInteger("targetY", 80);
        tag.setInteger("targetZ", 2049);
        return tag;
    }
}
