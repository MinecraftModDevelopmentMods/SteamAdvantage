package zone.moddev.mc.steamadvantage.blocks;

import zone.moddev.mc.steamadvantage.init.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.List;

public class DrillBitTileEntity extends TileEntity implements ITickableTileEntity{

	public final static float ROTATION_PER_TICK = 360f / 20f; // in degrees, 20 ticks per revolution
	public float rotation = 0;
	private Direction direction = Direction.DOWN;
	
	public DrillBitTileEntity(){
		super(zone.moddev.mc.steamadvantage.init.Entities.DRILLBIT);
	}
	
	@Override
	public void tick(){
		if(hasLevel() && getLevel().isClientSide){
			rotation = ROTATION_PER_TICK * (getLevel().getGameTime() % 20);
		}
	}
	public void setDirection(Direction dir){
		if(dir == null || dir == this.direction) return;
		this.direction = dir;
		syncDirection();
	}
	
	public Direction.Axis getDirection(){
		return direction.getAxis();
	}
	
	public static void createDrillBitBlock(World w, BlockPos coord, Direction dir){
		w.setBlockAndUpdate(coord, zone.moddev.mc.steamadvantage.init.Blocks.drillbit.defaultBlockState());
		TileEntity tile = w.getBlockEntity(coord);
		if(tile instanceof DrillBitTileEntity) ((DrillBitTileEntity) tile).setDirection(dir);
	}
	
	/**
	 * Destroys all drillbits connected to this one
	 */
	public void destroyLine(){
		destroyLine(true);
	}

	public void destroyLine(boolean removeOrigin){
		if(!hasLevel() || getLevel().isClientSide) return;
		List<BlockPos> line = new ArrayList<>();
		line.add(getBlockPos());
		collectLine(line, direction);
		collectLine(line, direction.getOpposite());
		// Detach all helpers before block replacement callbacks can re-enter this line.
		for(BlockPos coord : line) getLevel().removeBlockEntity(coord);
		for(BlockPos coord : line) {
			if(removeOrigin || !coord.equals(getBlockPos())) getLevel().removeBlock(coord, false);
		}
	}
	/**
	 * destroys upstream drillbit
	 */
	private void collectLine(List<BlockPos> line, Direction f){
		BlockPos coord = this.getBlockPos().relative(f);
		while(getLevel().hasChunkAt(coord) && getLevel().getBlockState(coord).getBlock() == Blocks.drillbit){
			line.add(coord);
			coord = coord.relative(f);
		}
	}
	
	@Override
	public CompoundNBT save(CompoundNBT root){
		super.save(root);
		writeDirectionToNBT(root);
		return root;
	}
	

	@Override
	public void load(net.minecraft.block.BlockState state, CompoundNBT root){
		super.load(state, root);
		readDirectionFromNBT(root);
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return save(new CompoundNBT());
	}

	/**
	 * Turns the data field NBT into a network packet
	 */
	@Override 
	public SUpdateTileEntityPacket getUpdatePacket(){
		CompoundNBT nbtTag = new CompoundNBT();
		writeDirectionToNBT(nbtTag);
		return new SUpdateTileEntityPacket(this.getBlockPos(), 0, nbtTag);
	}
	/**
	 * Receives the network packet made by <code>getDescriptionPacket()</code>
	 */
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
		readDirectionFromNBT(packet.getTag());
	}

	private void writeDirectionToNBT(CompoundNBT root) {
		root.putByte("dir", (byte)direction.get3DDataValue());
	}

	private void readDirectionFromNBT(CompoundNBT root) {
		if(root.contains("dir")){
			this.direction = decodeDirection(root.getByte("dir"));
		} else if(root.contains("d")){
			this.direction = decodeDirection(root.getByte("d"));
		}
	}

	private static Direction decodeDirection(int index) {
		return index >= 0 && index < Direction.values().length ? Direction.from3DDataValue(index) : Direction.DOWN;
	}

	private void syncDirection() {
		if(hasLevel() && !getLevel().isClientSide) {
			setChanged();
			BlockState state = getLevel().getBlockState(getBlockPos());
			getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);
		}
	}
}
