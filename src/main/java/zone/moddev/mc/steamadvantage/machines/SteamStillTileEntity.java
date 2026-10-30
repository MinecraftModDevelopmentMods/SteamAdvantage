package zone.moddev.mc.steamadvantage.machines;

import zone.moddev.mc.poweradvantage.util.FluidIdHelper;

import zone.moddev.mc.steamadvantage.util.LegacyFluidHandler;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerRequest;
import cyano.poweradvantage.api.fluid.FluidRequest;
import zone.moddev.mc.poweradvantage.conduitnetwork.ConduitRegistry;
import zone.moddev.mc.poweradvantage.init.Fluids;
import zone.moddev.mc.poweradvantage.registry.still.recipe.DistillationRecipe;
import zone.moddev.mc.poweradvantage.registry.still.recipe.DistillationRecipeRegistry;
import zone.moddev.mc.steamadvantage.init.Power;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.fluid.Fluid;
import cyano.poweradvantage.api.fluid.FluidStack;
import cyano.poweradvantage.api.fluid.FluidTank;
import cyano.poweradvantage.api.fluid.FluidTankInfo;

import static zone.moddev.mc.steamadvantage.util.SoundHelper.playSoundAtTileEntity;

@SuppressWarnings("deprecation")
public class SteamStillTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine implements LegacyFluidHandler{

	private final FluidTank outputTank;
	private final FluidTank inputTank;
	private final int speed = 2;
	private final float steamPerDistill = 1.5f;


	private final int[] dataSyncArray = new int[5];

	public SteamStillTileEntity() {
		super(zone.moddev.mc.steamadvantage.init.Entities.STEAM_STILL, new ConduitType[]{Power.steam_power, Fluids.fluidConduit_general}, new float[]{100,1000}, "tile.steamadvantage.steam_still.name");
		outputTank = new FluidTank(FluidTank.BUCKET_VOLUME );
		inputTank = new FluidTank(FluidTank.BUCKET_VOLUME);
	}

	private boolean redstone = true;

	private int timeSinceSound = 0;

	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			// server-side logic
			if(!redstone){
				if(getInputTank().getFluidAmount() > 0
						&& getEnergy(Power.steam_power) > steamPerDistill
						&& canDistill(getInputTank().getFluid()) && distill()){
					this.subtractEnergy(steamPerDistill, Power.steam_power);
					if(timeSinceSound > 200){
						playSoundAtTileEntity(SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.AMBIENT, 0.3f, 1.5f, this);
						timeSinceSound = 0;
					}
					timeSinceSound++;
				}
			}
			energyDecay();
		}
	}



	private boolean hasRedstoneSignal() {
		return getWorld().isBlockPowered(getPos());
	}


	private void energyDecay() {
		if(getEnergy(Power.steam_power) > 0){
			subtractEnergy(Power.ENERGY_LOST_PER_TICK,Power.steam_power);
		}
	}



	private float oldEnergy = 0;
	private int oldFluidIn = 0;
	private int oldFluidOut = 0;
	@Override
	public void powerUpdate(){
		// deliberately NOT calling super.powerUpdate()
		if(this.getOutputTank().getFluidAmount() > 0){
			ConduitType type = Fluids.fluidToConduitType(getOutputTank().getFluid().getFluid());
			float availableAmount = getOutputTank().getFluidAmount();
			float delta =  ConduitRegistry.transmitPowerToConsumers(availableAmount, zone.moddev.mc.poweradvantage.init.Fluids.fluidConduit_general, type,
					this.getMinimumSinkPriority(), getWorld(), getPos(), this);
			if(delta > 0){
				getOutputTank().drain(Math.max((int)delta,1),true); // no free energy!
			}
		}
		// powerUpdate occurs once every 8 world ticks and is scheduled such that neighboring
		// machines don't powerUpdate in the same world tick. To reduce network congestion,
		// I'm doing the synchonization logic here instead of in the tickUpdate method
		boolean updateFlag = false;

		if(oldEnergy != getEnergy(Power.steam_power)){
			oldEnergy = getEnergy(Power.steam_power);
			updateFlag = true;
		}
		if(oldFluidIn != getInputTank().getFluidAmount()){
			oldFluidIn = getInputTank().getFluidAmount();
			updateFlag = true;
		}
		if(oldFluidOut != getOutputTank().getFluidAmount()){
			oldFluidOut = getOutputTank().getFluidAmount();
			updateFlag = true;
		}

		redstone = hasRedstoneSignal();

		if(updateFlag){
			super.sync();
		}
	}

	@Override
	protected byte getMinimumSinkPriority(){
		return PowerRequest.LAST_PRIORITY;
	}


	public float getSteamLevel(){
		return this.getEnergy(Power.steam_power) / this.getEnergyCapacity(Power.steam_power);
	}


	public FluidTank getOutputTank(){
		return outputTank;
	}
	public FluidTank getInputTank(){
		return inputTank;
	}

	@Override
	protected ItemStack[] getInventory() {
		return null;
	}

	@Override
	public int[] getDataFieldArray() {
		return dataSyncArray;
	}

	@Override
	public void prepareDataFieldsForSync() {
		dataSyncArray[0] = Float.floatToRawIntBits(this.getEnergy(Power.steam_power));
		dataSyncArray[1] = this.getOutputTank().getFluidAmount();
		dataSyncArray[2] = FluidIdHelper.getFluidId(this.getOutputTank().getFluidAmount() > 0 ? this.getOutputTank().getFluid().getFluid() : net.minecraft.init.Fluids.WATER);
		dataSyncArray[3] = inputTank.getFluidAmount();
		dataSyncArray[4] = FluidIdHelper.getFluidId(inputTank.getFluidAmount() > 0 ? inputTank.getFluid().getFluid() : net.minecraft.init.Fluids.WATER);
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataSyncArray[0]), Power.steam_power);
		this.getOutputTank().setFluid(new FluidStack(FluidIdHelper.getFluid(dataSyncArray[2]),dataSyncArray[1]));
		this.getInputTank().setFluid(new FluidStack(FluidIdHelper.getFluid(dataSyncArray[4]),dataSyncArray[3]));
	}


	/**
	 * Handles data saving and loading
	 * @param tagRoot An NBT tag
	 */
	@Override
	public NBTTagCompound write(final NBTTagCompound tagRoot) {
		super.write(tagRoot);
		NBTTagCompound tankTag = new NBTTagCompound();
		this.getOutputTank().writeToNBT(tankTag);
		tagRoot.setTag("TankOut", tankTag);
		NBTTagCompound tankTag2 = new NBTTagCompound();
		this.getInputTank().writeToNBT(tankTag2);
		tagRoot.setTag("TankIn", tankTag2);
		return tagRoot;
	}
	/**
	 * Handles data saving and loading
	 * @param tagRoot An NBT tag
	 */
	@Override
	public void read(final NBTTagCompound tagRoot) {
		super.read(tagRoot);
		if (tagRoot.hasKey("TankOut")) {
			NBTTagCompound tankTag = tagRoot.getCompound("TankOut");
			getOutputTank().readFromNBT(tankTag);
			if(tankTag.hasKey("Empty")){
				// empty the tank if NBT says its empty (not default behavior of Tank.read(...) )
				getOutputTank().setFluid(null);
			}
		}

		if (tagRoot.hasKey("TankIn")) {
			NBTTagCompound tankTag = tagRoot.getCompound("TankIn");
			getInputTank().readFromNBT(tankTag);
			if(tankTag.hasKey("Empty")){
				// empty the tank if NBT says its empty (not default behavior of Tank.read(...) )
				getInputTank().setFluid(null);
			}
		}
	}

	public int getComparatorOutput() {
		return 15 * getInputTank().getFluidAmount() / getInputTank().getCapacity();
	}

	///// Overrides to make this a multi-type block /////

	@Override
	public boolean isPowerSink(ConduitType type){
		return true;
	}

	@Override
	public boolean isPowerSource(ConduitType type){
		return Fluids.isFluidType(type);
	}

	/**
	 * Adds "energy" as a fluid to the FluidTank returned by getTank(). This implementation ignores
	 * all non-fluid energy types.
	 * @param amount amount of energy/fluid to add
	 * @param type the type of energy/fluid being added.
	 * @return The amount that was actually added
	 */
	@Override
	public float addEnergy(float amount, ConduitType type){
		if(Fluids.isFluidType(type)){
			if(amount > 0){
				if(this.canFill(null, Fluids.conduitTypeToFluid(type))){
					return this.fill(null, new FluidStack(Fluids.conduitTypeToFluid(type),(int)amount), true);
				} else {
					return 0;
				}
			} else {
				if(this.canDrain(null, Fluids.conduitTypeToFluid(type))){
					return -1*this.drain(null, (int)amount, true).amount;
				} else {
					return 0;
				}
			}
		}
		return super.addEnergy(amount, type);
	}

	/**
	 * Subtracts "energy" as a fluid to the FluidTank returned by getTank(). This implementation
	 * ignores all non-fluid energy types.
	 * @param amount amount of energy/fluid to add
	 * @param type the type of energy/fluid being added.
	 * @return The amount that was actually added
	 */
	@Override
	public float subtractEnergy(float amount, ConduitType type){
		return addEnergy(-1*amount,type);
	}

	@Override
	public PowerRequest getPowerRequest(ConduitType offer) {
		if(redstone) return PowerRequest.REQUEST_NOTHING;
		if(Fluids.isFluidType(offer)
				&& DistillationRecipeRegistry.getInstance().getDistillationRecipeForFluid( Fluids.conduitTypeToFluid(offer)) != null){
			if(canDistill(Fluids.conduitTypeToFluid(offer))){
				if(getInputTank().getFluidAmount() > 0
						&& Fluids.conduitTypeToFluid(offer).equals(getInputTank().getFluid().getFluid()) == false) {
					// check that the existing fluid is compatible
					return PowerRequest.REQUEST_NOTHING;
				}
				PowerRequest request = new FluidRequest(FluidRequest.MEDIUM_PRIORITY-1,
						(getInputTank().getCapacity() - getInputTank().getFluidAmount()),
						this);
				return request;
			} else {
				return PowerRequest.REQUEST_NOTHING;
			}
		} else if(ConduitType.areSameType(offer, Power.steam_power)){
			return new PowerRequest(PowerRequest.MEDIUM_PRIORITY,this.getEnergyCapacity(Power.steam_power) - this.getEnergy(Power.steam_power), this);
		}
		return PowerRequest.REQUEST_NOTHING;
	}

	private boolean canDistill(Fluid f) {
		if(f == null) return false;
		FluidStack fs = new FluidStack(f,getInputTank().getCapacity());
		return canDistill(fs);
	}

	private boolean canDistill(FluidStack fs) {
		if(fs == null) return false;
		DistillationRecipe recipe = DistillationRecipeRegistry.getInstance().getDistillationRecipeForFluid(fs.getFluid());
		if(recipe == null) return false;
		return recipe.isValidInput(fs) && (outputTank.getFluidAmount() == 0
				|| recipe.isValidOutput(outputTank.getFluid()));
	}

	private int distillationApplications(DistillationRecipe recipe, FluidStack input) {
		if(!recipe.isValidInput(input)) return 0;
		if(outputTank.getFluidAmount() > 0 && !recipe.isValidOutput(outputTank.getFluid())) return 0;
		// Preview on a copy so blocked output cannot consume input or lose part of a batch.
		for(int applications = speed; applications > 0; applications--){
			FluidStack output = recipe.applyRecipe(input.copy(), applications);
			if(output != null && outputTank.fill(output, false) == output.amount) return applications;
		}
		return 0;
	}

	private boolean distill(){
		DistillationRecipe recipe = DistillationRecipeRegistry.getInstance()
				.getDistillationRecipeForFluid(getInputTank().getFluid().getFluid());
		int applications = distillationApplications(recipe, inputTank.getFluid());
		if(applications > 0){
			FluidStack output = recipe.applyRecipe(inputTank.getFluid(), applications);
			getOutputTank().fill(output, true);
			return true;
		}
		return false;
	}



	/**
	 * Determines whether this conduit is compatible with an adjacent one
	 * @param type The type of energy in the conduit
	 * @param blockFace The side through-which the energy is flowing
	 * @return true if this conduit can flow the given energy type through the given face, false
	 * otherwise
	 */
	public boolean canAcceptType(ConduitType type, EnumFacing blockFace){
		return ConduitType.areSameType(Power.steam_power, type) || ConduitType.areSameType(Fluids.fluidConduit_general, type);
	}
	/**
	 * Determines whether this conduit is compatible with a type of energy through any side
	 * @param type The type of energy in the conduit
	 * @return true if this conduit can flow the given energy type through one or more of its block
	 * faces, false otherwise
	 */
	public boolean canAcceptType(ConduitType type){
		return ConduitType.areSameType(Power.steam_power, type) || ConduitType.areSameType(Fluids.fluidConduit_general, type);
	}
	///// end multi-type overrides /////



	///// IFluidHandler /////

	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 * @param forReal if true, then the fluid in the tank will change
	 */
	@Override
	public int fill(EnumFacing face, FluidStack fluid, boolean forReal) {
		if(fluid == null) return 0;
		if(getInputTank().getFluidAmount() <= 0 || getInputTank().getFluid().getFluid().equals(fluid.getFluid())){
			if(canDistill(fluid)){
				return getInputTank().fill(fluid, forReal);
			}
		}
		return 0;
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 * @param forReal if true, then the fluid in the tank will change
	 */
	@Override
	public FluidStack drain(EnumFacing face, FluidStack fluid, boolean forReal) {
		if(getOutputTank().getFluidAmount() > 0 && getOutputTank().getFluid().getFluid().equals(fluid.getFluid())){
			return getOutputTank().drain(fluid.amount,forReal);
		} else {
			return new FluidStack(getOutputTank().getFluid().getFluid(),0);
		}
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param amount The amount of fluid being added/removed
	 * @param forReal if true, then the fluid in the tank will change
	 */
	@Override
	public FluidStack drain(EnumFacing face, int amount, boolean forReal) {
		if(getOutputTank().getFluidAmount() > 0 ){
			return getOutputTank().drain(amount,forReal);
		} else {
			return null;
		}
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 */
	@Override
	public boolean canFill(EnumFacing face, Fluid fluid) {
		if(fluid == null) return false;
		if(canDistill(fluid) == false) return false;
		if(getInputTank().getFluidAmount() <= 0) return true;
		return getInputTank().getFluidAmount() <= getInputTank().getCapacity() && fluid.equals(getInputTank().getFluid().getFluid());
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 */
	@Override
	public boolean canDrain(EnumFacing face, Fluid fluid) {
		if(fluid == null) return false;
		return getOutputTank().getFluidAmount() > 0 && fluid.equals(getOutputTank().getFluid().getFluid());
	}

	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @return array of FluidTankInfo describing all of the FluidTanks
	 */
	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing face) {
		FluidTankInfo[] arr = new FluidTankInfo[2];
		arr[0] = getInputTank().getInfo();
		arr[1] = getOutputTank().getInfo();
		return arr;
	}

	///// end of IFluidHandler methods /////

	@Override
	public boolean isItemValidForSlot(final int slot, final ItemStack item) {
		return false; // no inventory
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}
}
