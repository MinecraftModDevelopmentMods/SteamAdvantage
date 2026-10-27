package zone.moddev.mc.steamadvantage.machines;

import cyano.poweradvantage.api.fluid.FluidStack;
import java.util.function.Supplier;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.steamadvantage.init.Entities;
import zone.moddev.mc.steamadvantage.init.Power;
import static org.junit.Assert.*;

public class SteamMachinePersistenceTest {
    @BeforeClass public static void bootstrapNativeTypes() {
        net.minecraft.util.registry.Bootstrap.bootStrap();
        Entities.STEAM_BOILER_COAL = type("steam_boiler_coal", CoalBoilerTileEntity::new);
        Entities.STEAM_BOILER_ELECTRIC = type("steam_boiler_electric", ElectricBoilerTileEntity::new);
        Entities.STEAM_BOILER_GEOTHERMAL = type("steam_boiler_geothermal", GeothermalBoilerTileEntity::new);
        Entities.STEAM_PUMP = type("steam_pump", SteamPumpTileEntity::new);
        Entities.STEAM_STILL = type("steam_still", SteamStillTileEntity::new);
        Entities.STEAM_TANK = type("steam_tank", SteamTankTileEntity::new);
    }

    @Test public void levelsAndDecayRetainExactBinaryFractions() {
        CoalBoilerTileEntity coal = new CoalBoilerTileEntity();
        coal.setEnergy(375.0F, Power.steam_power);
        coal.getTank().fill(new FluidStack(net.minecraft.fluid.Fluids.WATER, 1500), true);
        assertEquals(0.375F, coal.getSteamLevel(), 0.0F);
        assertEquals(0.375F, coal.getWaterLevel(), 0.0F);
        ElectricBoilerTileEntity electric = new ElectricBoilerTileEntity();
        electric.setEnergy(37.5F, Power.steam_power);
        electric.setEnergy(60.0F, ElectricBoilerTileEntity.ELECTRIC_POWER);
        assertEquals(0.375F, electric.getSteamLevel(), 0.0F);
        assertEquals(0.375F, electric.getElectricityLevel(), 0.0F);
        SteamTankTileEntity tank = new SteamTankTileEntity();
        tank.setEnergy(3750.0F, Power.steam_power);
        assertEquals(0.375F, tank.getSteamLevel(), 0.0F);
        tank.tickUpdate(true);
        assertEquals(3749.9375F, tank.getEnergy(Power.steam_power), 0.0F);
        assertEquals(0.0625F, Power.ENERGY_LOST_PER_TICK, 0.0F);
        assertEquals(0.03125F, ElectricBoilerTileEntity.ELECTRICITY_TO_STEAM, 0.0F);
        assertEquals(2.5F, SteamDrillTileEntity.ENERGY_COST_DRILLBIT, 0.0F);
        assertEquals(10.0F, SteamDrillTileEntity.ENERGY_COST_MOVE, 0.0F);
        assertEquals(1.0F, SteamDrillTileEntity.ENERGY_COST_PROGRESS_TICK, 0.0F);
        assertEquals(32.0F, SteamElevatorTileEntity.STEAM_PER_ELEVATOR_MOVE, 0.0F);
        assertEquals(125, RockCrusherTileEntity.TICKS_PER_ACTION);
    }

    @Test public void coalAndElectricStateSurviveNativeSaveLoad() {
        CompoundNBT tag = state(37.5F);
        tag.put("Tank", water(1500));
        tag.putInt("BurnTime", 127);
        tag.putInt("BurnTimeTotal", 1600);
        CoalBoilerTileEntity coal = new CoalBoilerTileEntity();
        coal.load(Blocks.AIR.defaultBlockState(), tag);
        CompoundNBT saved = coal.save(new CompoundNBT());
        assertEquals("steamadvantage:steam_boiler_coal", saved.getString("id"));
        assertEquals(127, saved.getInt("BurnTime"));
        assertEquals(1600, saved.getInt("BurnTimeTotal"));
        CoalBoilerTileEntity restored = new CoalBoilerTileEntity();
        restored.load(Blocks.AIR.defaultBlockState(), saved);
        assertEquals(37.5F, restored.getEnergy(Power.steam_power), 0.0F);
        assertEquals(1500, restored.getTank().getFluidAmount());
        tag.putFloat("Electricity", 60.0F);
        ElectricBoilerTileEntity electric = new ElectricBoilerTileEntity();
        electric.load(Blocks.AIR.defaultBlockState(), tag);
        ElectricBoilerTileEntity electricRestored = new ElectricBoilerTileEntity();
        electricRestored.load(Blocks.AIR.defaultBlockState(), electric.save(new CompoundNBT()));
        assertEquals(37.5F, electricRestored.getEnergy(Power.steam_power), 0.0F);
        assertEquals(60.0F, electricRestored.getEnergy(ElectricBoilerTileEntity.ELECTRIC_POWER), 0.0F);
        assertEquals(1500, electricRestored.getTank().getFluidAmount());
    }

    @Test public void pumpAliasAndGeothermalMisspellingRemainReadable() {
        CompoundNBT tag = state(37.5F);
        tag.put("TankOut", water(750));
        tag.putByte("NextPump", (byte) 11);
        SteamPumpTileEntity pump = new SteamPumpTileEntity();
        pump.load(Blocks.AIR.defaultBlockState(), tag);
        CompoundNBT saved = pump.save(new CompoundNBT());
        assertEquals(750, saved.getCompound("Tank").getInt("Amount"));
        assertEquals(11, saved.getByte("NextPump"));
        SteamPumpTileEntity pumpRestored = new SteamPumpTileEntity();
        pumpRestored.load(Blocks.AIR.defaultBlockState(), saved);
        assertEquals(750, pumpRestored.getTank().getFluidAmount());
        assertEquals(37.5F, pumpRestored.getEnergy(Power.steam_power), 0.0F);
        tag.putFloat("Tempoerature", 412.5F);
        GeothermalBoilerTileEntity geothermal = new GeothermalBoilerTileEntity();
        geothermal.load(Blocks.AIR.defaultBlockState(), tag);
        assertEquals(412.5F, geothermal.save(new CompoundNBT()).getFloat("Temperature"), 0.0F);
        tag.putFloat("Temperature", 500.0F);
        geothermal.load(Blocks.AIR.defaultBlockState(), tag);
        assertEquals(500.0F, geothermal.save(new CompoundNBT()).getFloat("Temperature"), 0.0F);
    }

    @Test public void stillKeepsSeparateTanksAndExplicitlyEmptyReloads() {
        CompoundNBT tag = state(37.5F);
        tag.put("TankIn", water(625));
        tag.put("TankOut", water(375));
        SteamStillTileEntity still = new SteamStillTileEntity();
        still.load(Blocks.AIR.defaultBlockState(), tag);
        SteamStillTileEntity restored = new SteamStillTileEntity();
        restored.load(Blocks.AIR.defaultBlockState(), still.save(new CompoundNBT()));
        assertEquals(625, restored.getInputTank().getFluidAmount());
        assertEquals(375, restored.getOutputTank().getFluidAmount());
        assertEquals(37.5F, restored.getEnergy(Power.steam_power), 0.0F);
        tag.put("TankIn", water(0));
        tag.put("TankOut", water(0));
        restored.load(Blocks.AIR.defaultBlockState(), tag);
        assertEquals(0, restored.getInputTank().getFluidAmount());
        assertEquals(0, restored.getOutputTank().getFluidAmount());
    }

    private static CompoundNBT state(float steam) {
        CompoundNBT tag = new CompoundNBT();
        tag.putIntArray("Energy", new int[] {Float.floatToIntBits(steam)});
        return tag;
    }

    private static CompoundNBT water(int amount) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("FluidName", "minecraft:water");
        tag.putInt("Amount", amount);
        return tag;
    }

    private static <T extends TileEntity> TileEntityType<T> type(String id, Supplier<T> factory) {
        ForgeRegistry<TileEntityType<?>> registry =
                (ForgeRegistry<TileEntityType<?>>) ForgeRegistries.TILE_ENTITIES;
        boolean locked = registry.isLocked();
        registry.unfreeze();
        try {
            TileEntityType<T> type = TileEntityType.Builder.of(factory, Blocks.STONE).build(null);
            type.setRegistryName(new ResourceLocation("steamadvantage", id));
            registry.register(type);
            return type;
        } finally {
            if (locked) registry.freeze();
        }
    }
}
