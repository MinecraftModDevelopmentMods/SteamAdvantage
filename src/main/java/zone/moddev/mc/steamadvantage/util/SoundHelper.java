package zone.moddev.mc.steamadvantage.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SoundHelper {

	public static void playSoundAtTileEntity(SoundEvent sound, SoundCategory soundType, float volume, float pitch, TileEntity te){
		final BlockPos pos = te.getPos();
		final Vec3d center = new Vec3d(pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5);
		playSoundAtPosition(center.x, center.y, center.z, sound, soundType, volume, pitch, te.getWorld());
	}

	public static void playSoundAtPosition(double x, double y, double z, SoundEvent sound, SoundCategory soundType, float volume, float pitch, World serverWorld){
		if(serverWorld.isRemote) return;
		final double range = 16;
		final AxisAlignedBB area = new AxisAlignedBB(
				x - range, y - range, z - range,
				x + range, y + range, z + range);
		for(EntityPlayer candidate : serverWorld.playerEntities){
			if(!(candidate instanceof EntityPlayerMP) || !EntitySelectors.NOT_SPECTATING.test(candidate)
					|| !area.intersects(candidate.getBoundingBox())) continue;
			EntityPlayerMP player = (EntityPlayerMP) candidate;
			if(player.connection != null) player.connection.sendPacket(new SPacketCustomSound(sound.getRegistryName(), soundType,
					new Vec3d(x, y, z), volume, pitch));
		}
	}

	public static void playBigSoundAtPosition(double x, double y, double z, SoundEvent sound, SoundCategory soundType, float volume, float pitch, World serverWorld){
		if(serverWorld.isRemote) return;
		final double range = 50;
		final AxisAlignedBB area = new AxisAlignedBB(
				x - range, y - range, z - range,
				x + range, y + range, z + range);
		for(EntityPlayer candidate : serverWorld.playerEntities){
			if(!(candidate instanceof EntityPlayerMP) || !EntitySelectors.NOT_SPECTATING.test(candidate)
					|| !area.intersects(candidate.getBoundingBox())) continue;
			EntityPlayerMP player = (EntityPlayerMP) candidate;
			if(player.connection != null) player.connection.sendPacket(new SPacketCustomSound(sound.getRegistryName(), soundType,
					new Vec3d(x, y, z), volume, pitch));
		}
	}
}
