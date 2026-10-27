package zone.moddev.mc.steamadvantage.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import cyano.poweradvantage.api.migration.LegacyMigrationApi;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import org.junit.Test;

public class SteamLegacyMigrationsTest {
    @Test public void preservesAllSixDrillFacings() {
        for (Direction facing : Direction.values()) {
            assertEquals(facing, SteamLegacyMigrations.legacyDrillFacing(facing.get3DDataValue()));
        }
    }

    @Test public void clampsLegacyPlatformHeight() {
        assertEquals(0, SteamLegacyMigrations.legacyPlatformHeight(0));
        assertEquals(3, SteamLegacyMigrations.legacyPlatformHeight(3));
        assertEquals(4, SteamLegacyMigrations.legacyPlatformHeight(15));
    }

    @Test public void repairsLegacyPumpAndGeothermalKeysWithoutOverwritingCanonicalState() {
        CompoundNBT pump = new CompoundNBT();
        pump.putString("id", "steamadvantage.steam_pump");
        CompoundNBT oldTank = new CompoundNBT();
        oldTank.putInt("Amount", 1250);
        pump.put("TankOut", oldTank);
        assertTrue(SteamLegacyMigrations.repairPersistenceKeys(pump));
        assertEquals(1250, pump.getCompound("Tank").getInt("Amount"));

        CompoundNBT boiler = new CompoundNBT();
        boiler.putString("id", "steamadvantage:steam_boiler_geothermal");
        boiler.putFloat("Tempoerature", 412.5F);
        assertTrue(SteamLegacyMigrations.repairPersistenceKeys(boiler));
        assertEquals(412.5F, boiler.getFloat("Temperature"), 0.0F);

        CompoundNBT canonical = new CompoundNBT();
        canonical.putString("id", "steamadvantage:steam_boiler_geothermal");
        canonical.putFloat("Temperature", 500.0F);
        canonical.putFloat("Tempoerature", 100.0F);
        assertFalse(SteamLegacyMigrations.repairPersistenceKeys(canonical));
        assertEquals(500.0F, canonical.getFloat("Temperature"), 0.0F);
    }

    @Test public void aliasesLegacyTileIds() {
        SteamLegacyMigrations.register();
        assertEquals("steamadvantage:steam_pump",
                LegacyMigrationApi.tileTarget("steamadvantage.steam_pump"));
        assertEquals("steamadvantage:steam_drill",
                LegacyMigrationApi.tileTarget("steamadvantage.steam_drill"));
        assertEquals("steamadvantage:steam_drill",
                LegacyMigrationApi.tileTarget("minecraft:steamadvantage.steam_drill"));
    }
}
