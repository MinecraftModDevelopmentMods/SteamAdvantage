package zone.moddev.mc.steamadvantage.items;

import static zone.moddev.mc.steamadvantage.util.SoundHelper.playBigSoundAtPosition;
import static zone.moddev.mc.steamadvantage.util.SoundHelper.playSoundAtPosition;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import zone.moddev.mc.steamadvantage.SteamAdvantage;
import zone.moddev.mc.steamadvantage.init.Enchantments;
import zone.moddev.mc.steamadvantage.init.Power;
import net.minecraft.block.TNTBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.UseAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.Explosion;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.tags.ItemTags;

@SuppressWarnings("deprecation")
public class MusketItem extends net.minecraft.item.Item{
	
	
	public static final String NBT_DATA_KEY_LOADED = "loaded";
	public static final double MAX_RANGE = 64;
	private static final int maxUseTime = 7200;

	private static final ResourceLocation BLACKPOWDER_AMMO_TAG = new ResourceLocation("forge", "ammo/blackpowder");
	private static final ResourceLocation STEEL_INGOT_TAG = new ResourceLocation("forge", "ingots/steel");

	public MusketItem(Item.Properties properties){
		super(properties.defaultDurability(250));
	}
	

	public static int getReloadTime(){
		return SteamAdvantage.MUSKET_RELOAD;
	}
	public static float getShotDamage(){
		return SteamAdvantage.MUSKET_DAMAGE;
	}
	public static float getMeleeDamage(){
		return 3;
	}
	
	@Override
	public int getEnchantmentValue(){
		return 1;
	}
	
	@Override
	public UseAction getUseAnimation(ItemStack stack){
		if(isLoaded(stack)){
			return UseAction.BOW;
		} else {
			return UseAction.BLOCK;
		}
	}
	
	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand){
		ItemStack srcItemStack = playerEntity.getItemInHand(hand);
		if(hand != Hand.MAIN_HAND) return ActionResult.pass(srcItemStack);
		if(isNotLoaded(srcItemStack) && !hasAmmo(playerEntity, srcItemStack)) return ActionResult.fail(srcItemStack);
		if(isNotLoaded(srcItemStack) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.rapid_reload, srcItemStack) > 0){
			// instant reload
			decrementAmmo(playerEntity,srcItemStack);
			startLoad(srcItemStack);
			playSound(SoundEvents.LEVER_CLICK,world,playerEntity);
			finishLoad(srcItemStack);
			return ActionResult.success(srcItemStack);
		}
		playerEntity.startUsingItem(hand);
		return ActionResult.success(srcItemStack);
	}
	
	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count){
		if(player.level.isClientSide && count % 7 == 3 && isNotLoaded(stack)){
			// indicator to player that the gun is loading
			player.playSound(SoundType.STONE.getStepSound(), 0.5f, 1.0f);
		}
	}
	/**
	 * This method is invoked after the item has been used for an amount of time equal to the duration 
	 * provided to the PlayerEntity.setItemInUse(stack, duration).
	 */
	@Override
	public ItemStack finishUsingItem (ItemStack srcItemStack, World world, LivingEntity playerEntity)
	{ // 
		if(isNotLoaded(srcItemStack) && hasAmmo(playerEntity,srcItemStack)){
			decrementAmmo(playerEntity,srcItemStack);
			startLoad(srcItemStack);
			playSound(SoundEvents.LEVER_CLICK,world,playerEntity);
		}
		return srcItemStack;
	}
	
	@Override
	public void releaseUsing(ItemStack stack, World worldIn, LivingEntity playerIn, int timeLeft) {
		if(isAlmostLoaded(stack)){
			finishLoad(stack);
		} else if(isLoaded(stack)){
			fire(stack,playerIn,worldIn);
		}
	}
	
	protected void fire(ItemStack srcItemStack, LivingEntity playerEntity,
			World world) {
		final boolean isPlayer = playerEntity instanceof PlayerEntity;
		unload(srcItemStack);
		if(isPlayer && !((PlayerEntity)playerEntity).isCreative()){
			srcItemStack.hurtAndBreak(1, playerEntity, entity -> entity.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
		}
		Vector3d lookVector = playerEntity.getLookAngle();
		spawnParticle(ParticleTypes.EXPLOSION_EMITTER, world,
				playerEntity.getX()+lookVector.x, playerEntity.getY()+playerEntity.getEyeHeight()+lookVector.y, playerEntity.getZ() + lookVector.z);
		if(world.isClientSide){
			playerEntity.xRot -= 15;
			return;
		}
		playSound(SoundEvents.FLINTANDSTEEL_USE,world,playerEntity);
		playBigSoundAtPosition(playerEntity.getX(),playerEntity.getY(),playerEntity.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST,SoundCategory.PLAYERS,2F,0.5F,world);
		
		Vector3d start = new Vector3d(playerEntity.getX(), playerEntity.getY()+playerEntity.getEyeHeight(),playerEntity.getZ());
		start.add(MAX_RANGE * lookVector.x, MAX_RANGE * lookVector.y, MAX_RANGE * lookVector.z);
		RayTraceResult rayTrace = rayTraceBlocksAndEntities(world,MAX_RANGE,playerEntity);
		if(rayTrace == null){
			// no collisions
			return;
		}
		final float explodeFactor = 0.75f;
		if(rayTrace instanceof EntityRayTraceResult){
			Entity e = ((EntityRayTraceResult) rayTrace).getEntity();
			e.hurt(Power.musket_damage, getShotDamage());
			if(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.high_explosive, srcItemStack) > 0){
				int lvl = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.high_explosive, srcItemStack);
				world.explode(playerEntity, e.getX(), e.getY()+0.5, e.getZ(),
						explodeFactor * lvl, 
						Explosion.Mode.BREAK);
				AxisAlignedBB fireArea = new AxisAlignedBB(e.getX()-lvl, e.getY()-lvl, e.getZ()-lvl,
						e.getX()+lvl, e.getY()+lvl, e.getZ()+lvl);
				List<LivingEntity> collateralDamage = world.getEntitiesOfClass(LivingEntity.class, fireArea);
				for(LivingEntity victim : collateralDamage){
					victim.setSecondsOnFire(lvl);
				}
			}
		} else if(rayTrace.getType() == RayTraceResult.Type.BLOCK){
			playSoundAtPosition(rayTrace.getLocation().x, rayTrace.getLocation().y, rayTrace.getLocation().z,
					SoundEvents.GRAVEL_HIT, SoundCategory.BLOCKS, 1f, 1f, world);
			BlockPos coord = ((BlockRayTraceResult) rayTrace).getBlockPos();
			if(coord.getY()>= 0 && coord.getY() <= 255 && !world.isEmptyBlock(coord)){
				BlockState bs = world.getBlockState(coord);
				Material blockMat = bs.getMaterial();
				if(blockMat == net.minecraft.block.material.Material.GLASS
						|| blockMat == net.minecraft.block.material.Material.LEAVES
						|| blockMat == net.minecraft.block.material.Material.DECORATION
						|| blockMat == net.minecraft.block.material.Material.PLANT
						|| blockMat == net.minecraft.block.material.Material.REPLACEABLE_PLANT
						|| blockMat == net.minecraft.block.material.Material.WEB){
					world.destroyBlock(coord, true);
				} else if (blockMat == net.minecraft.block.material.Material.EXPLOSIVE){
					if(world.getBlockState(coord).getBlock() instanceof TNTBlock){
						TNTBlock.explode(world, coord);
						world.removeBlock(coord, false);
					}
				}
			}
			if(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.high_explosive, srcItemStack) > 0){
				int lvl = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.high_explosive, srcItemStack);
				world.explode(playerEntity, rayTrace.getLocation().x, rayTrace.getLocation().y, rayTrace.getLocation().z,
						explodeFactor * lvl, 
						Explosion.Mode.BREAK);
				AxisAlignedBB fireArea = new AxisAlignedBB(rayTrace.getLocation().x-lvl, rayTrace.getLocation().y-lvl, rayTrace.getLocation().z-lvl,
						rayTrace.getLocation().x+lvl, rayTrace.getLocation().y+lvl, rayTrace.getLocation().z+lvl);
				List<LivingEntity> collateralDamage = world.getEntitiesOfClass(LivingEntity.class, fireArea);
				for(LivingEntity victim : collateralDamage){
					victim.setSecondsOnFire(lvl);
				}
			}
		}
		if(rayTrace.getType() != RayTraceResult.Type.MISS){
			spawnParticle(ParticleTypes.LARGE_SMOKE, world,
					rayTrace.getLocation().x, rayTrace.getLocation().y, rayTrace.getLocation().z);
			spawnParticle(ParticleTypes.EXPLOSION, world,
					rayTrace.getLocation().x, rayTrace.getLocation().y, rayTrace.getLocation().z);
		}
		
		// recoil enchantment kicks you back
		int recoil = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.recoil, srcItemStack);
		if(recoil > 0){
			double c = -0.3;
			double lateral = 2.0;
			playerEntity.push(c * recoil * lookVector.x * lateral,
					c * recoil * lookVector.y, 
					c * recoil * lookVector.z * lateral);
			if(playerEntity instanceof ServerPlayerEntity){
				// send update packet from server
				((ServerPlayerEntity) playerEntity).connection.send(new SEntityVelocityPacket(playerEntity));
			}
		}
	}


	private void spawnParticle(IParticleData p, World w, double x, double y, double z) {
		if(w instanceof ServerWorld) {
			((ServerWorld) w).sendParticles(p, x, y, z, 1, 0, 0, 0, 0);
		} else {
			w.addParticle(p, x, y, z, 0, 0, 0);
		}
	}
	
	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)
	{
		stack.hurtAndBreak(1, attacker, entity -> entity.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
		return true;
	}
	
	/** plays a sound at the player location */
	protected void playSound(SoundEvent sound, World world, Entity e){
		if (!world.isClientSide)
		{
			playSoundAtPosition(e.getX(), e.getY(), e.getZ(), sound,SoundCategory.PLAYERS,1F,1F,world);
		}
	}
	
	public static void startLoad(ItemStack musket){
		CompoundNBT data;
		if(musket.hasTag()){
			data = musket.getTag();
		} else {
			data = new CompoundNBT();
		}
		data.putByte(NBT_DATA_KEY_LOADED, (byte)2);
		musket.setTag(data);
	}
	
	public static void finishLoad(ItemStack musket){
		CompoundNBT data;
		if(musket.hasTag()){
			data = musket.getTag();
		} else {
			data = new CompoundNBT();
		}
		data.putByte(NBT_DATA_KEY_LOADED, (byte)1);
		musket.setTag(data);
	}
	
	public static void unload(ItemStack musket){
		CompoundNBT data;
		if(musket.hasTag()){
			data = musket.getTag();
		} else {
			data = new CompoundNBT();
		}
		data.putByte(NBT_DATA_KEY_LOADED, (byte)0);
		musket.setTag(data);
	}
	
	public static boolean isLoaded(ItemStack musket){
		CompoundNBT data = musket.getTag();
		if(data == null) return false;
		return data.contains(NBT_DATA_KEY_LOADED) && data.getByte(NBT_DATA_KEY_LOADED) == 1;
	}
	
	public static boolean isNotLoaded(ItemStack musket){
		CompoundNBT data = musket.getTag();
		if(data == null || !data.contains(NBT_DATA_KEY_LOADED)) return true;
		return data.contains(NBT_DATA_KEY_LOADED) && data.getByte(NBT_DATA_KEY_LOADED) == 0;
	}
	
	public static boolean isAlmostLoaded(ItemStack musket){
		CompoundNBT data = musket.getTag();
		if(data == null) return false;
		return data.contains(NBT_DATA_KEY_LOADED) && data.getByte(NBT_DATA_KEY_LOADED) == 2;
	}


	
	public static boolean hasAmmo(LivingEntity e, ItemStack musket) {
		if(e instanceof PlayerEntity) {
			PlayerEntity playerEntity = (PlayerEntity) e;
			if (playerEntity.isCreative()
					|| EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.enchantment.Enchantments.INFINITY_ARROWS, musket) > 0
					|| EnchantmentHelper.getItemEnchantmentLevel(Enchantments.powderless, musket) > 0) return true;
			for (int i = 0; i < playerEntity.inventory.items.size(); i++) {
				ItemStack inventoryStack = playerEntity.inventory.items.get(i);
				if (inventoryStack.isEmpty()) continue;
				if (ItemTags.getAllTags().getTagOrEmpty(BLACKPOWDER_AMMO_TAG).contains(inventoryStack.getItem())) return true;
			}
		}
		return false;
	}
	

	public static void decrementAmmo(LivingEntity e, ItemStack musket) {
		if(e instanceof PlayerEntity) {
			PlayerEntity playerEntity = (PlayerEntity) e;
			if (playerEntity.isCreative()
					|| EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.enchantment.Enchantments.INFINITY_ARROWS, musket) > 0
					|| EnchantmentHelper.getItemEnchantmentLevel(Enchantments.powderless, musket) > 0) return;
			for (int i = 0; i < playerEntity.inventory.items.size(); i++) {
				ItemStack inventoryStack = playerEntity.inventory.items.get(i);
				if (inventoryStack.isEmpty()) continue;
				if (ItemTags.getAllTags().getTagOrEmpty(BLACKPOWDER_AMMO_TAG).contains(inventoryStack.getItem())) {
					inventoryStack.shrink(1);
					if (inventoryStack.isEmpty()) playerEntity.inventory.items.set(i, ItemStack.EMPTY);
					return;
				}
			}
		}
	}
	
	
	public static RayTraceResult rayTraceBlocksAndEntities(World w, double maxRange, LivingEntity source){
		Vector3d rayOrigin = new Vector3d(source.getX(), source.getY() + source.getEyeHeight(), source.getZ());
		Vector3d rayDirection = source.getLookAngle();
		BlockRayTraceResult terrainHit = w.clip(new RayTraceContext(rayOrigin, rayOrigin.add(rayDirection.scale(maxRange)),
				RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.SOURCE_ONLY, source));
		Vector3d rayEnd = terrainHit.getLocation();
		AxisAlignedBB aoi = new AxisAlignedBB(rayOrigin, rayEnd).inflate(1.0D);
		// Server spatial entity queries can synchronously generate chunks around the shooter.
		Iterable<Entity> entities = w instanceof ServerWorld ? ((ServerWorld)w).getAllEntities() : w.getEntities(source, aoi);
		double closestDistSqr = rayOrigin.distanceToSqr(rayEnd);
		Entity closestEntity = null;
		Vector3d closestHit = null;
		for(Entity e : entities){
			if(e == source || e.removed || e.isSpectator() || !aoi.intersects(e.getBoundingBox())) continue;
			Optional<Vector3d> hit = rayHitBeforeTerrain(rayOrigin, rayEnd, e.getBoundingBox());
			if(hit.isPresent()){
				double distSqr = rayOrigin.distanceToSqr(hit.get());
				if(distSqr < closestDistSqr){
					closestDistSqr = distSqr;
					closestEntity = e;
					closestHit = hit.get();
				}
			}
		}
		return closestEntity == null ? terrainHit : new EntityRayTraceResult(closestEntity, closestHit);
	}

	static Optional<Vector3d> rayHitBeforeTerrain(Vector3d origin, Vector3d terrainEnd, AxisAlignedBB target){
		Optional<Vector3d> hit = target.contains(origin) ? Optional.of(origin) : target.clip(origin, terrainEnd);
		return hit.filter(point -> origin.distanceToSqr(point) < origin.distanceToSqr(terrainEnd));
	}

	public static boolean rayIntersectsBoundingBox(Vector3d rayOrigin, Vector3d rayDirection, AxisAlignedBB box){
		if(box == null) return false;
		// algorithm from http://gamedev.stackexchange.com/questions/18436/most-efficient-aabb-vs-ray-collision-algorithms
		Vector3d inverse = new Vector3d(1.0 / rayDirection.x, 1.0 / rayDirection.y, 1.0 / rayDirection.z);
		double t1 = (box.minX - rayOrigin.x)*inverse.x;
		double t2 = (box.maxX- rayOrigin.x)*inverse.x;
		double t3 = (box.minY - rayOrigin.y)*inverse.y;
		double t4 = (box.maxY - rayOrigin.y)*inverse.y;
		double t5 = (box.minZ - rayOrigin.z)*inverse.z;
		double t6 = (box.maxZ - rayOrigin.z)*inverse.z;

		double tmin = max(max(min(t1, t2), min(t3, t4)), min(t5, t6));
		double tmax = min(min(max(t1, t2), max(t3, t4)), max(t5, t6));

		// if tmax < 0, ray (line) is intersecting AABB, but whole AABB is behing us
		if (tmax < 0)
		{
			return false;
		}

		// if tmin > tmax, ray doesn't intersect AABB
		if (tmin > tmax)
		{
			return false;
		}

		return true;
	}
	
	
	private static double max(double a, double b){
		return Math.max(a, b);
	}
	private static double min(double a, double b){
		return Math.min(a, b);
	}
	
	/**
	 * Return whether this item is repairable in an anvil.
	 */
	@Override public boolean isValidRepairItem(ItemStack srcItemStack, ItemStack rawMaterial)
	{
		// repair with steel ingots
		return ItemTags.getAllTags().getTagOrEmpty(STEEL_INGOT_TAG).contains(rawMaterial.getItem());
	}
	
	@Override
	public void appendHoverText(ItemStack stack, World world, List<ITextComponent> list, net.minecraft.client.util.ITooltipFlag flag){
		super.appendHoverText(stack,world,list,flag);
		list.add(new TranslationTextComponent("tooltip.musket.damage", (int)MusketItem.getShotDamage()));
		if(isLoaded(stack)){
			list.add(new TranslationTextComponent("tooltip.musket.loaded"));
		} else {
			list.add(new TranslationTextComponent("tooltip.musket.unloaded"));
			list.add(new TranslationTextComponent("tooltip.musket.ammo", new TranslationTextComponent("item.steamadvantage.blackpowder_cartridge")));
		}
	}

	/** Sets melee attack damage */
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack)
	{
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
		if(slot != EquipmentSlotType.MAINHAND) return multimap;
		multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double)getMeleeDamage(), AttributeModifier.Operation.ADDITION));
		return multimap;
	}
	
	@Override
	public int getUseDuration(ItemStack stack)
	{
		if(isLoaded(stack)){
			return maxUseTime;
		} else {
			return getReloadTime();
		}
	}
}
