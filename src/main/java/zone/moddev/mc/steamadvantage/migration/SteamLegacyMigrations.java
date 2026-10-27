package zone.moddev.mc.steamadvantage.migration;

import cyano.poweradvantage.api.migration.LegacyMigrationApi;
import cyano.poweradvantage.api.simple.BlockSimplePowerMachine;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import zone.moddev.mc.poweradvantage.blocks.BlockPowerSwitch;
import zone.moddev.mc.steamadvantage.SteamAdvantage;
import zone.moddev.mc.steamadvantage.blocks.PlatformBlock;
import zone.moddev.mc.steamadvantage.machines.SteamDrillBlock;

/** Registers SteamAdvantage's 1.10 and 1.12 identities with PowerAdvantage's shared migration hook. */
public final class SteamLegacyMigrations {
    private static boolean registered;

    private SteamLegacyMigrations() {}

    public static synchronized void register() {
        if (registered) return;

        LegacyMigrationApi.registerNamespace(SteamAdvantage.MODID);
        for (String path : new String[] {
                "steam_pipe", "steam_track", "steam_boiler_coal", "steam_boiler_electric",
                "steam_boiler_geothermal", "steam_tank", "steam_furnace", "steam_crusher",
                "steam_drill", "drillbit", "platform", "steam_elevator", "steam_switch",
                "pump_pipe_steam", "steam_still", "steam_pump", "steam_boiler_oil" }) {
            LegacyMigrationApi.registerBlockStateCodec(id(path), SteamLegacyMigrations::decodeState);
        }

        registerTile("steam_boiler_coal");
        registerTile("steam_boiler_electric");
        registerTile("steam_boiler_geothermal");
        registerTile("steam_tank");
        registerTile("steam_furnace");
        registerTile("steam_crusher");
        registerTile("steam_drill");
        registerTile("steam_elevator");
        registerTile("drillbit");
        registerTile("steam_still");
        registerTile("steam_pump");
        registerTile("steam_boiler_oil");

        LegacyMigrationApi.registerDurableItem(id("musket"));
        LegacyMigrationApi.registerNbtMigration("steamadvantage:persistence_keys",
                SteamLegacyMigrations::repairPersistenceKeys);
        registered = true;
    }

    private static void registerTile(String path) {
        String current = id(path).toString();
        LegacyMigrationApi.registerTileEntityAlias(current, current);
        LegacyMigrationApi.registerTileEntityAlias(SteamAdvantage.MODID + "." + path, current);
        LegacyMigrationApi.registerTileEntityAlias("minecraft:" + SteamAdvantage.MODID + "." + path, current);
    }

    private static BlockState decodeState(Block block, int metadata) {
        if (block instanceof SteamDrillBlock) {
            return block.defaultBlockState().setValue(SteamDrillBlock.FACING, legacyDrillFacing(metadata));
        }
        if (block instanceof PlatformBlock) {
            return block.defaultBlockState().setValue(PlatformBlock.HEIGHT,
                    Integer.valueOf(legacyPlatformHeight(metadata)));
        }
        if (block instanceof BlockPowerSwitch) {
            return ((BlockPowerSwitch) block).getStateFromMeta(metadata);
        }
        if (block instanceof BlockSimplePowerMachine) {
            return ((BlockSimplePowerMachine) block).getStateFromMeta(metadata);
        }
        return block.defaultBlockState();
    }

    static Direction legacyDrillFacing(int metadata) {
        return Direction.from3DDataValue(metadata % Direction.values().length);
    }

    static int legacyPlatformHeight(int metadata) {
        return Math.min(metadata & 15, 4);
    }

    static boolean repairPersistenceKeys(CompoundNBT compound) {
        String id = compound.getString("id");
        boolean changed = false;
        if (isTile(id, "steam_pump") && !compound.contains("Tank", 10) && compound.contains("TankOut", 10)) {
            compound.put("Tank", compound.getCompound("TankOut").copy());
            changed = true;
        }
        if (isTile(id, "steam_boiler_geothermal")
                && !compound.contains("Temperature", 99) && compound.contains("Tempoerature", 99)) {
            compound.putFloat("Temperature", compound.getFloat("Tempoerature"));
            changed = true;
        }
        return changed;
    }

    private static boolean isTile(String value, String path) {
        return id(path).toString().equalsIgnoreCase(value)
                || (SteamAdvantage.MODID + "." + path).equalsIgnoreCase(value);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(SteamAdvantage.MODID, path);
    }
}
