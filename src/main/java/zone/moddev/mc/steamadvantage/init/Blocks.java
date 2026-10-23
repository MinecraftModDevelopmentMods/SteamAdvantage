package zone.moddev.mc.steamadvantage.init;

import java.util.LinkedHashMap;
import java.util.Map;

import cyano.poweradvantage.api.GUIBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zone.moddev.mc.poweradvantage.blocks.BlockPowerSwitch;
import zone.moddev.mc.poweradvantage.init.ItemGroups;
import zone.moddev.mc.steamadvantage.SteamAdvantage;
import zone.moddev.mc.steamadvantage.blocks.DrillBitBlock;
import zone.moddev.mc.steamadvantage.blocks.PlatformBlock;
import zone.moddev.mc.steamadvantage.blocks.PumpPipeBlock;
import zone.moddev.mc.steamadvantage.blocks.SteamPipeBlock;
import zone.moddev.mc.steamadvantage.blocks.SteamTrackBlock;
import zone.moddev.mc.steamadvantage.machines.BlastFurnaceBlock;
import zone.moddev.mc.steamadvantage.machines.CoalBoilerBlock;
import zone.moddev.mc.steamadvantage.machines.ElectricBoilerBlock;
import zone.moddev.mc.steamadvantage.machines.GeothermalBoilerBlock;
import zone.moddev.mc.steamadvantage.machines.OilBoilerBlock;
import zone.moddev.mc.steamadvantage.machines.RockCrusherBlock;
import zone.moddev.mc.steamadvantage.machines.SteamDrillBlock;
import zone.moddev.mc.steamadvantage.machines.SteamElevatorBlock;
import zone.moddev.mc.steamadvantage.machines.SteamPumpBlock;
import zone.moddev.mc.steamadvantage.machines.SteamStillBlock;
import zone.moddev.mc.steamadvantage.machines.SteamTankBlock;

@Mod.EventBusSubscriber(modid = SteamAdvantage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Blocks {
    private static final Map<String, Block> ALL_BLOCKS = new LinkedHashMap<>();

    public static GUIBlock steam_boiler_coal;
    public static GUIBlock steam_boiler_electric;
    public static GUIBlock steam_boiler_geothermal;
    public static GUIBlock steam_crusher;
    public static GUIBlock steam_furnace;
    public static GUIBlock steam_tank;
    public static GUIBlock steam_drill;
    public static GUIBlock steam_elevator;
    public static Block steam_elevator_platform;
    public static IBlockState[] steam_elevator_platforms;
    public static Block steam_pipe;
    public static Block drillbit;
    public static Block steam_track;
    public static Block pump_pipe_steam;
    public static Block steam_switch;
    public static GUIBlock steam_still;
    public static GUIBlock steam_pump;
    public static GUIBlock steam_boiler_oil;

    private static boolean initialized;
    private Blocks() {}

    public static synchronized void init() {
        if (initialized) return;
        ItemGroups.init();
        steam_pipe = add(new SteamPipeBlock(), "steam_pipe");
        steam_track = add(new SteamTrackBlock(), "steam_track");
        steam_boiler_coal = (GUIBlock) add(new CoalBoilerBlock(), "steam_boiler_coal");
        steam_boiler_electric = (GUIBlock) add(new ElectricBoilerBlock(), "steam_boiler_electric");
        steam_boiler_geothermal = (GUIBlock) add(new GeothermalBoilerBlock(), "steam_boiler_geothermal");
        steam_tank = (GUIBlock) add(new SteamTankBlock(), "steam_tank");
        steam_furnace = (GUIBlock) add(new BlastFurnaceBlock(), "steam_furnace");
        steam_crusher = (GUIBlock) add(new RockCrusherBlock(), "steam_crusher");
        steam_drill = (GUIBlock) add(new SteamDrillBlock(), "steam_drill");
        drillbit = add(new DrillBitBlock(), "drillbit");
        steam_elevator_platform = add(new PlatformBlock(), "platform");
        steam_elevator_platforms = new IBlockState[5];
        for (int i = 0; i < steam_elevator_platforms.length; i++) {
            steam_elevator_platforms[i] = steam_elevator_platform.getDefaultState().with(PlatformBlock.HEIGHT, i);
        }
        steam_elevator = (GUIBlock) add(new SteamElevatorBlock(), "steam_elevator");
        steam_switch = add(new BlockPowerSwitch(Power.steam_power), "steam_switch");
        pump_pipe_steam = add(new PumpPipeBlock(), "pump_pipe_steam");
        steam_still = (GUIBlock) add(new SteamStillBlock(), "steam_still");
        steam_pump = (GUIBlock) add(new SteamPumpBlock(), "steam_pump");
        steam_boiler_oil = (GUIBlock) add(new OilBoilerBlock(), "steam_boiler_oil");
        initialized = true;
    }

    private static Block add(Block block, String id) {
        block.setRegistryName(SteamAdvantage.MODID, id);
        ALL_BLOCKS.put(id, block);
        return block;
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        init();
        event.getRegistry().registerAll(ALL_BLOCKS.values().toArray(new Block[0]));
    }

    @SubscribeEvent
    public static void registerBlockItems(RegistryEvent.Register<Item> event) {
        init();
        for (Map.Entry<String, Block> entry : ALL_BLOCKS.entrySet()) {
            Item.Properties properties = new Item.Properties();
            if (!"pump_pipe_steam".equals(entry.getKey()) && !"drillbit".equals(entry.getKey())) {
                properties.group(ItemGroups.tab_powerAdvantage);
            }
            ItemBlock item = new ItemBlock(entry.getValue(), properties);
            item.setRegistryName(SteamAdvantage.MODID, entry.getKey());
            event.getRegistry().register(item);
        }
    }
}
