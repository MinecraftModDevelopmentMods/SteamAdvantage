package zone.moddev.mc.steamadvantage.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class SoundHelper {

	public static void playSoundAtTileEntity(SoundEvent sound, SoundCategory soundType, float volume, float pitch, TileEntity te){
		final BlockPos pos = te.getBlockPos();
		final Vector3d center = new Vector3d(pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5);
		playSoundAtPosition(center.x, center.y, center.z, sound, soundType, volume, pitch, te.getLevel());
	}

	public static void playSoundAtPosition(double x, double y, double z, SoundEvent sound, SoundCategory soundType, float volume, float pitch, World serverWorld){
		if(serverWorld.isClientSide) return;
		final double range = 16;
		final AxisAlignedBB area = new AxisAlignedBB(
				x - range, y - range, z - range,
				x + range, y + range, z + range);
		for(PlayerEntity candidate : serverWorld.players()){
			if(!(candidate instanceof ServerPlayerEntity) || !EntityPredicates.NO_SPECTATORS.test(candidate)
					|| !area.intersects(candidate.getBoundingBox())) continue;
			ServerPlayerEntity player = (ServerPlayerEntity) candidate;
			if(player.connection != null) player.connection.send(new SPlaySoundPacket(sound.getRegistryName(), soundType,
					new Vector3d(x, y, z), volume, pitch));
		}
	}

	public static void playBigSoundAtPosition(double x, double y, double z, SoundEvent sound, SoundCategory soundType, float volume, float pitch, World serverWorld){
		if(serverWorld.isClientSide) return;
		final double range = 50;
		final AxisAlignedBB area = new AxisAlignedBB(
				x - range, y - range, z - range,
				x + range, y + range, z + range);
		for(PlayerEntity candidate : serverWorld.players()){
			if(!(candidate instanceof ServerPlayerEntity) || !EntityPredicates.NO_SPECTATORS.test(candidate)
					|| !area.intersects(candidate.getBoundingBox())) continue;
			ServerPlayerEntity player = (ServerPlayerEntity) candidate;
			if(player.connection != null) player.connection.send(new SPlaySoundPacket(sound.getRegistryName(), soundType,
					new Vector3d(x, y, z), volume, pitch));
		}
	}
}
