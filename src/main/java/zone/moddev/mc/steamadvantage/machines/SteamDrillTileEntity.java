package zone.moddev.mc.steamadvantage.machines;

import cyano.poweradvantage.api.ConduitType;
import zone.moddev.mc.poweradvantage.util.InventoryWrapper;
import zone.moddev.mc.steamadvantage.blocks.DrillBitTileEntity;
import zone.moddev.mc.steamadvantage.compat.BaseMetalsCompat;
import zone.moddev.mc.steamadvantage.init.Blocks;
import zone.moddev.mc.steamadvantage.init.Power;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Arrays;
import java.util.List;

import static zone.moddev.mc.steamadvantage.util.SoundHelper.playSoundAtPosition;
import static zone.moddev.mc.steamadvantage.util.SoundHelper.playSoundAtTileEntity;

@SuppressWarnings("deprecation")
public class SteamDrillTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine{
	
	public static final int MAX_RANGE = 64;
	public static final float ENERGY_COST_DRILLBIT = 2.5f;
	public static final float ENERGY_COST_MOVE = 10f;
	public static final float ENERGY_COST_PROGRESS_TICK = 1f;
	public static float MINING_TIME_FACTOR = 12.0f;
	
	private final ItemStack[] inventory = new ItemStack[5];
	private final int[] dataSyncArray = new int[3];
	private int progress = 0;
	private int progressGoal = 0;
	private BlockPos targetBlockCoord = null;
	private BlockState targetBlockState = null;
	private CompoundNBT savedTargetState = null;
	private Block targetBlock = null;
	private List<ItemStack> targetBlockItems = null;
	private Direction oldDir = null;
	private Direction savedFacing = null;

	
	private boolean deferred = false;
	
	public SteamDrillTileEntity() {
		super(zone.moddev.mc.steamadvantage.init.Entities.STEAM_DRILL, Power.steam_power, 50, "tile.steamadvantage.steam_drill.name");
	}

	
	private boolean redstone = true;
	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			if(deferred){
				restoreTargetBlock();
			}
			Direction face = this.getFacing();
			if(oldDir == null){
				// oldDir becomes null on rotation and new TileEntity creation
				// destroy drillbits in all directions but the current one
				oldDir = face;
				for(Direction d : Direction.values()){
					if(d == face) continue;
					destroyDrillBit(d);
				}
			}
			
			// disabled by redstone
			if(redstone){
				if (progress > 0){
					progress = 0;
				}
			} else {
				if(targetBlockCoord != null){
					// mining time
					if(getEnergy(Power.steam_power) > ENERGY_COST_PROGRESS_TICK && hasSpaceForItems(targetBlockItems) && canMine(targetBlockCoord)){
						this.subtractEnergy(ENERGY_COST_PROGRESS_TICK, Power.steam_power);
						progress++;
						if(progress >= progressGoal){
							// Mined it!
							playSoundAtPosition(targetBlockCoord.getX()+0.5, targetBlockCoord.getY()+0.5, targetBlockCoord.getZ()+0.5, targetBlock.getSoundType(targetBlockState, powerAdvantageWorld(), targetBlockCoord, null).getPlaceSound(), SoundCategory.BLOCKS, 0.5f, 1f, this.powerAdvantageWorld());
							playSoundAtTileEntity( SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.5f, 1f, this);
							powerAdvantageWorld().removeBlock(targetBlockCoord, false);
							for(ItemStack item : targetBlockItems){
								addItem(item);
							}
							untargetBlock();
						}
					}
				}
				
			}
			// lost steam
			energyDecay();
		}
		
	}
	
	private boolean hasSpaceForItems(List<ItemStack> items) {
		if(items == null || items.isEmpty()) return true;
		boolean success = true;
		for(int i = 0; i < items.size(); i++){
			boolean canFit = false;
			for(int n = 0; n < inventory.length; n++){
				if(canStack(items.get(i),inventory[n])) {
					canFit = true;
					break;
				}
			}
			success = success && canFit;
			
		}
		return success;
	}

	private boolean canStack(ItemStack newItem, ItemStack slot) {
		if(slot == null || newItem == null) return true;
		return ItemStack.areItemsEqual(newItem, slot) 
				&& ItemStack.areItemStackTagsEqual(newItem, slot) 
				&& (newItem.getCount() + slot.getCount() <= slot.getItem().getItemStackLimit(slot));
	}
	
	public ItemStack addItem(ItemStack in){
		for(int i = 0; i < inventory.length; i++){
			if(inventory[i] == null){
				inventory[i] = in;
				return null;
			} else if(ItemStack.areItemsEqual(in, inventory[i]) 
					&& ItemStack.areItemStackTagsEqual(in, inventory[i])){
				// are stackable
				if(inventory[i].getCount() >= inventory[i].getItem().getItemStackLimit(inventory[i])){
					continue;
				} else if(in.getCount() + inventory[i].getCount() <= inventory[i].getItem().getItemStackLimit(inventory[i])){
					inventory[i].grow(in.getCount());
					return null;
				} else {
					int delta = inventory[i].getItem().getItemStackLimit(inventory[i]) - inventory[i].getCount();
					inventory[i].grow(delta);
					in.shrink(delta);
				}
			}
		}
		// return the remainder, if any
		return in;
	}

	private Direction trackDirection(Direction drillDirection){
		for(Direction dir : Direction.values()){
			if(dir == drillDirection) continue;
			if(powerAdvantageWorld().getBlockState(powerAdvantagePos().offset(dir)).getBlock() == Blocks.steam_track){
				return dir;
			}
		}
		return null;
	}
	
	private boolean followTrack(){
		if(this.getEnergy(Power.steam_power) < ENERGY_COST_MOVE) return false;
		Direction drillDirection = getFacing();
		Direction trackDir = trackDirection(drillDirection);
		if(trackDir == null) return false;
		
		// clone this block into neighboring block
		this.untargetBlock();
		World w = powerAdvantageWorld();
		BlockPos currentPos = powerAdvantagePos();
		BlockPos nextPos = currentPos.offset(trackDir);
		BlockState trackState = w.getBlockState(nextPos);
		w.setBlockState(nextPos, w.getBlockState(currentPos), 2);
		TileEntity movedTile = w.getTileEntity(nextPos);
		if(!(movedTile instanceof SteamDrillTileEntity)){
			w.setBlockState(nextPos, trackState, 2);
			return false;
		}
		SteamDrillTileEntity te = (SteamDrillTileEntity)movedTile;
		CompoundNBT dataTransfer = new CompoundNBT();
		this.write(dataTransfer);
		dataTransfer.putInt("x", nextPos.getX());
		dataTransfer.putInt("y", nextPos.getY());
		dataTransfer.putInt("z", nextPos.getZ());
		te.read(dataTransfer);
		te.setPos(nextPos);
		te.validate();
		te.subtractEnergy(ENERGY_COST_MOVE, Power.steam_power);
		te.markDirty();
		Arrays.fill(this.getInventory(), null);
		
		// replace this block with steam pipe
		destroyDrillBit(drillDirection);
		w.setBlockState(currentPos.offset(drillDirection), zone.moddev.mc.poweradvantage.init.Blocks.steel_frame.getDefaultState());
		w.setBlockState(currentPos, Blocks.steam_pipe.getDefaultState(), 2);
		return true;
	}
	
	@Override
	protected ItemStack[] getInventory() {
		return inventory;
	}

	@Override
	public int[] getDataFieldArray() {
		return dataSyncArray;
	}

	@Override
	public void prepareDataFieldsForSync() {
		dataSyncArray[0] = Float.floatToRawIntBits(this.getEnergy(Power.steam_power));
		dataSyncArray[1] = progress;
		dataSyncArray[2] = progressGoal;
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataSyncArray[0]), Power.steam_power);
		this.progress = dataSyncArray[1];
		progressGoal = dataSyncArray[2];
	}
	private float oldEnergy = 0;
	private int oldProgress = 0;
	@Override public void powerUpdate(){
		if(deferred){
			restoreTargetBlock();
		}
		super.powerUpdate();
		boolean flagSync = progress != oldProgress || oldEnergy != getEnergy(Power.steam_power);
		oldProgress = progress;
		oldEnergy = getEnergy(Power.steam_power);
		
		redstone = hasRedstoneSignal();
		
		
		Direction f = getFacing();
		BlockPos n = powerAdvantagePos().offset(f);
		
		// manage drill bits and find next block
		if(redstone || this.getEnergy(Power.steam_power) <= 0){
			// no power, destroy drill
			destroyDrillBit(f);
			flagSync = true;
			this.untargetBlock();
		} else {
			// drill baby drill!
			if(targetBlockCoord == null ){
				// find new block
				boolean hitEnd = false;
				for(int i = 0; i <= MAX_RANGE ; i++){
					if(i == MAX_RANGE){
						hitEnd = true;
						break;
					}
					if(powerAdvantageWorld().getBlockState(n).getBlock() != Blocks.drillbit){
						if(powerAdvantageWorld().isAirBlock(n) || powerAdvantageWorld().getBlockState(n).getMaterial().isReplaceable()){
							// this block is not worth mining, replace it
							DrillBitTileEntity.createDrillBitBlock(powerAdvantageWorld(), n, f);
							this.subtractEnergy(ENERGY_COST_DRILLBIT, Power.steam_power);
							flagSync = true;
							break;
						} else {
							// found a block!
							if(canMine(n)){
								targetBlock(n);
							} else {
								hitEnd = true;
							}
							flagSync = true;
							break;
						}
					}
					n = n.offset(f);
					if(n.getY() == 0 || n.getY() == 255){
						hitEnd = true;
						break;
					}
				}
				// if hit end of range, move along track
				if(hitEnd){
					boolean moved = followTrack();
					if(moved){
						zone.moddev.mc.poweradvantage.conduitnetwork.ConduitRegistry.getInstance()
								.conduitBlockRemovedEvent(powerAdvantageWorld(), powerAdvantageWorld().dimension.getType().getId(), powerAdvantagePos(), Power.steam_power);
						return;
					}
				}
			} else {
				// currently drilling a block
				// block validation
				if(powerAdvantageWorld().isAirBlock(targetBlockCoord) || powerAdvantageWorld().getBlockState(targetBlockCoord).getBlock() != targetBlock){
					// Block changed! invalidate!
					untargetBlock();
					flagSync = true;
				}
			}
		}
		
		if(flagSync){
			this.sync();
		}
		

		// push inventory to adjacent chest
		BlockPos adj = powerAdvantagePos().offset(f.getOpposite());
		if(!redstone && !powerAdvantageWorld().isAirBlock(adj)){
			inventoryTransfer(adj,f);
		}
	}

	private void destroyDrillBit(Direction f) {
		BlockPos n = powerAdvantagePos().offset(f);
		while(powerAdvantageWorld().getBlockState(n).getBlock() == zone.moddev.mc.steamadvantage.init.Blocks.drillbit){
			powerAdvantageWorld().removeBlock(n, false);
			n = n.offset(f);
		}
	}

	private void targetBlock (BlockPos n){
		progress = 0;
		targetBlockCoord = n;
		progressGoal = this.getBlockStrength(n);
		targetBlockState = powerAdvantageWorld().getBlockState(n);
		savedTargetState = NBTUtil.writeBlockState(targetBlockState);
		targetBlock = targetBlockState.getBlock();
		ItemStack crusherOutput = BaseMetalsCompat.getCrusherRecipeOutput(powerAdvantageWorld(), targetBlockState);
		if(crusherOutput != null){
			targetBlockItems = Arrays.asList(crusherOutput);
		} else {
			NonNullList<ItemStack> drops = NonNullList.create();
			drops.addAll(Block.getDrops(targetBlockState, (ServerWorld) powerAdvantageWorld(), n,
					powerAdvantageWorld().getTileEntity(n)));
			targetBlockItems = drops;
		}
		deferred = false;
	}

	private void restoreTargetBlock(){
		if(powerAdvantageWorld() == null || powerAdvantageWorld().isRemote
				|| !powerAdvantageWorld().isBlockLoaded(targetBlockCoord)) return;
		int savedProgress = progress;
		if(savedTargetState != null && !savedTargetState.equals(
				NBTUtil.writeBlockState(powerAdvantageWorld().getBlockState(targetBlockCoord)))){
			untargetBlock();
			return;
		}
		// Rebuild drops and hardness without discarding work saved for this target.
		targetBlock(targetBlockCoord);
		progress = Math.max(0, Math.min(savedProgress, progressGoal));
		redstone = hasRedstoneSignal();
	}
	
	private void deferredTargetBlock (BlockPos n){
		targetBlockCoord = n;
		deferred = true;
	}
	
	private void untargetBlock(){
		deferred = false;
		progress = 0;
		progressGoal = 0;
		targetBlockCoord = null;
		targetBlock = null;
		targetBlockState = null;
		savedTargetState = null;
		targetBlockItems = null;
	}
	
	private Direction getFacing(){
		BlockState state = powerAdvantageWorld().getBlockState(powerAdvantagePos());
		if(state.has(SteamDrillBlock.FACING)){
			savedFacing = state.get(SteamDrillBlock.FACING);
			return savedFacing;
		}
		if(savedFacing == null) savedFacing = inferFacing();
		return savedFacing;
	}

	private Direction inferFacing(){
		if(targetBlockCoord != null){
			int dx = targetBlockCoord.getX() - powerAdvantagePos().getX();
			int dy = targetBlockCoord.getY() - powerAdvantagePos().getY();
			int dz = targetBlockCoord.getZ() - powerAdvantagePos().getZ();
			if(dx != 0 && dy == 0 && dz == 0) return dx > 0 ? Direction.EAST : Direction.WEST;
			if(dy != 0 && dx == 0 && dz == 0) return dy > 0 ? Direction.UP : Direction.DOWN;
			if(dz != 0 && dx == 0 && dy == 0) return dz > 0 ? Direction.SOUTH : Direction.NORTH;
		}
		for(Direction dir : Direction.values()){
			if(powerAdvantageWorld().getBlockState(powerAdvantagePos().offset(dir)).getBlock() == Blocks.drillbit){
				return dir;
			}
		}
		return oldDir == null ? Direction.DOWN : oldDir;
	}

	private void energyDecay() {
		if(getEnergy(Power.steam_power) > 0){
			subtractEnergy(Power.ENERGY_LOST_PER_TICK,Power.steam_power);
		}
	}
	
	private boolean hasRedstoneSignal() {
		return powerAdvantageWorld().isBlockPowered(powerAdvantagePos());
	}
	
	private boolean canMine(BlockPos coord){
		Block b = powerAdvantageWorld().getBlockState(coord).getBlock();
		return !(b == net.minecraft.block.Blocks.BEDROCK || b == net.minecraft.block.Blocks.BARRIER);
	}
	
	private int getBlockStrength(BlockPos coord){
		if(powerAdvantageWorld().isAirBlock(coord)){
			return 0;
		}
		BlockState bs = powerAdvantageWorld().getBlockState(coord);
		Block block = bs.getBlock();
		return (int)(Math.max(MINING_TIME_FACTOR * block.getBlockHardness(bs,powerAdvantageWorld(), coord),0.5f * MINING_TIME_FACTOR));
	}
	
	

	private void inventoryTransfer(BlockPos adj, Direction otherFace) {
		TileEntity te = powerAdvantageWorld().getTileEntity(adj);
		if(te instanceof IInventory ){
			ISidedInventory inv = InventoryWrapper.wrap((IInventory)te);
			int[] accessibleSlots = inv.getSlotsForFace(otherFace);
			if(accessibleSlots.length == 0) return;
			for(int mySlot = 0; mySlot < this.inventory.length; mySlot++){
				if(this.inventory[mySlot] == null || this.inventory[mySlot].isEmpty()) continue;
				for(int i = 0; i < accessibleSlots.length; i++){
					int theirSlot = accessibleSlots[i];
					ItemStack theirItem = inv.getStackInSlot(theirSlot);
					if(inv.canInsertItem(theirSlot, inventory[mySlot], otherFace)){
						if(theirItem == null || theirItem.isEmpty()){
							ItemStack newItem = inventory[mySlot].copy();
							newItem.setCount(1);
							inv.setInventorySlotContents(theirSlot, newItem);
							inventory[mySlot].shrink(1);
							if(inventory[mySlot].isEmpty()) inventory[mySlot] = null;
							inv.markDirty();
							this.markDirty();
							return;
						} else if(ItemStack.areItemsEqual(theirItem, inventory[mySlot]) 
								&& ItemStack.areItemStackTagsEqual(theirItem, inventory[mySlot])
								&& theirItem.getCount() < theirItem.getMaxStackSize()
								&& theirItem.getCount() < inv.getInventoryStackLimit()){
							theirItem.grow(1);
							inventory[mySlot].shrink(1);
							if(inventory[mySlot].isEmpty()) inventory[mySlot] = null;
							inv.markDirty();
							this.markDirty();
							return;
						}
					}
				}
			}
			
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT tagRoot){
		super.write(tagRoot);
		tagRoot.putShort("progress",(short)progress);
		// getFacing refreshes this during normal ticks. World lookups here can
		// synchronously reload a chunk while its unload-save is in progress.
		if(savedFacing != null) tagRoot.putByte("Facing", (byte)savedFacing.getIndex());
		if(targetBlockCoord != null){
			tagRoot.putInt("targetX", targetBlockCoord.getX());
			tagRoot.putInt("targetY", targetBlockCoord.getY());
			tagRoot.putInt("targetZ", targetBlockCoord.getZ());
			if(savedTargetState != null) tagRoot.put("TargetState", savedTargetState.copy());
		}
		return tagRoot;
	}
	
	@Override
	public void read(CompoundNBT tagRoot){
		super.read(tagRoot);
		untargetBlock();
		savedFacing = null;
		oldDir = null;
		if(tagRoot.contains("Facing")){
			int facingIndex = tagRoot.getByte("Facing");
			if(facingIndex >= 0 && facingIndex < Direction.values().length){
				savedFacing = Direction.byIndex(facingIndex);
			}
		}
		if(tagRoot.contains("progress")){
			progress = tagRoot.getShort("progress");
		}
		if(tagRoot.contains("targetX") && tagRoot.contains("targetY") && tagRoot.contains("targetZ")){
			int x = tagRoot.getInt("targetX");
			int y = tagRoot.getInt("targetY");
			int z = tagRoot.getInt("targetZ");
			savedTargetState = tagRoot.contains("TargetState", 10) ? tagRoot.getCompound("TargetState").copy() : null;
			// Loot and hardness are restored on the server after the world is attached.
			this.deferredTargetBlock(new BlockPos(x,y,z));
		}
	}


	
	public float getProgressLevel(){
		if(progressGoal > 0) 
			return (float)progress / (float)progressGoal;
		return 0;
	}
	
	public float getSteamLevel(){
		return this.getEnergy(Power.steam_power) / this.getEnergyCapacity(Power.steam_power);
	}
	
	public int getComparatorOutput() {
		int sum = 0;
		for(int n = 0; n < inventory.length; n++){
			if(inventory[n] != null){
				sum += inventory[n].getCount() * 64 / inventory[n].getMaxStackSize();
			}
		}
		if(sum == 0) return 0;
		return Math.min(Math.max(15 * sum / (64 * (inventory.length - 1)),1),15);
	}

	@Override
	public boolean isPowerSink(ConduitType conduitType) {
		return true;
	}

	@Override
	public boolean isPowerSource(ConduitType conduitType) {
		return false;
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return true;
	}
}
