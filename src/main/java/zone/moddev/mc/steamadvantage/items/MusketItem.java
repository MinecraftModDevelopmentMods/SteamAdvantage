package zone.moddev.mc.steamadvantage.items;

import static zone.moddev.mc.steamadvantage.util.SoundHelper.playBigSoundAtPosition;
import static zone.moddev.mc.steamadvantage.util.SoundHelper.playSoundAtPosition;

import java.util.List;
import java.util.Iterator;
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
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.UseAction;
import net.minecraft.item.IItemPropertyGetter;
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
import net.minecraft.util.math.Vec3d;
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
		super(properties.defaultMaxDamage(250));
		this.addPropertyOverride(new ResourceLocation("loaded"), new IItemPropertyGetter()
		{
			@OnlyIn(Dist.CLIENT)
			public float call(ItemStack item, World w, LivingEntity e)
			{
				return isLoaded(item) ? 1.0F : 0.0F;
			}
		});
		this.addPropertyOverride(new ResourceLocation("loading"), new IItemPropertyGetter()
		{
			@OnlyIn(Dist.CLIENT)
			public float call(ItemStack item, World w, LivingEntity e)
			{
				return e != null && e.isHandActive() && e.getActiveItemStack() == item ? 1.0F : 0.0F;
			}
		});
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
	public int getItemEnchantability(){
		return 1;
	}
	
	@Override
	public UseAction getUseAction(ItemStack stack){
		if(isLoaded(stack)){
			return UseAction.BOW;
		} else {
			return UseAction.BLOCK;
		}
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity playerEntity, Hand hand){
		ItemStack srcItemStack = playerEntity.getHeldItem(hand);
		if(hand != Hand.MAIN_HAND) return ActionResult.resultPass(srcItemStack);
		if(isNotLoaded(srcItemStack) && !hasAmmo(playerEntity, srcItemStack)) return ActionResult.resultFail(srcItemStack);
		if(isNotLoaded(srcItemStack) && EnchantmentHelper.getEnchantmentLevel(Enchantments.rapid_reload, srcItemStack) > 0){
			// instant reload
			decrementAmmo(playerEntity,srcItemStack);
			startLoad(srcItemStack);
			playSound(SoundEvents.BLOCK_LEVER_CLICK,world,playerEntity);
			finishLoad(srcItemStack);
			return ActionResult.resultSuccess(srcItemStack);
		}
		playerEntity.setActiveHand(hand);
		return ActionResult.resultSuccess(srcItemStack);
	}
	
	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count){
		if(player.world.isRemote && count % 7 == 3 && isNotLoaded(stack)){
			// indicator to player that the gun is loading
			player.playSound(SoundType.STONE.getStepSound(), 0.5f, 1.0f);
		}
	}
	/**
	 * This method is invoked after the item has been used for an amount of time equal to the duration 
	 * provided to the PlayerEntity.setItemInUse(stack, duration).
	 */
	@Override
	public ItemStack onItemUseFinish (ItemStack srcItemStack, World world, LivingEntity playerEntity)
	{ // 
		if(isNotLoaded(srcItemStack) && hasAmmo(playerEntity,srcItemStack)){
			decrementAmmo(playerEntity,srcItemStack);
			startLoad(srcItemStack);
			playSound(SoundEvents.BLOCK_LEVER_CLICK,world,playerEntity);
		}
		return srcItemStack;
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity playerIn, int timeLeft) {
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
			srcItemStack.damageItem(1, playerEntity, entity -> entity.sendBreakAnimation(EquipmentSlotType.MAINHAND));
		}
		Vec3d lookVector = playerEntity.getLookVec();
		spawnParticle(ParticleTypes.EXPLOSION_EMITTER, world,
				playerEntity.getPosX()+lookVector.x, playerEntity.getPosY()+playerEntity.getEyeHeight()+lookVector.y, playerEntity.getPosZ() + lookVector.z);
		if(world.isRemote){
			playerEntity.rotationPitch -= 15;
			return;
		}
		playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE,world,playerEntity);
		playBigSoundAtPosition(playerEntity.getPosX(),playerEntity.getPosY(),playerEntity.getPosZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST,SoundCategory.PLAYERS,2F,0.5F,world);
		
		Vec3d start = new Vec3d(playerEntity.getPosX(), playerEntity.getPosY()+playerEntity.getEyeHeight(),playerEntity.getPosZ());
		start.add(MAX_RANGE * lookVector.x, MAX_RANGE * lookVector.y, MAX_RANGE * lookVector.z);
		RayTraceResult rayTrace = rayTraceBlocksAndEntities(world,MAX_RANGE,playerEntity);
		if(rayTrace == null){
			// no collisions
			return;
		}
		final float explodeFactor = 0.75f;
		if(rayTrace instanceof EntityRayTraceResult){
			Entity e = ((EntityRayTraceResult) rayTrace).getEntity();
			e.attackEntityFrom(Power.musket_damage, getShotDamage());
			if(EnchantmentHelper.getEnchantmentLevel(Enchantments.high_explosive, srcItemStack) > 0){
				int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.high_explosive, srcItemStack);
				world.createExplosion(playerEntity, e.getPosX(), e.getPosY()+0.5, e.getPosZ(),
						explodeFactor * lvl, 
						Explosion.Mode.BREAK);
				AxisAlignedBB fireArea = new AxisAlignedBB(e.getPosX()-lvl, e.getPosY()-lvl, e.getPosZ()-lvl,
						e.getPosX()+lvl, e.getPosY()+lvl, e.getPosZ()+lvl);
				List<LivingEntity> collateralDamage = world.getEntitiesWithinAABB(LivingEntity.class, fireArea);
				for(LivingEntity victim : collateralDamage){
					victim.setFire(lvl);
				}
			}
		} else if(rayTrace.getType() == RayTraceResult.Type.BLOCK){
			playSoundAtPosition(rayTrace.getHitVec().x, rayTrace.getHitVec().y, rayTrace.getHitVec().z,
					SoundEvents.BLOCK_GRAVEL_HIT, SoundCategory.BLOCKS, 1f, 1f, world);
			BlockPos coord = ((BlockRayTraceResult) rayTrace).getPos();
			if(coord.getY()>= 0 && coord.getY() <= 255 && !world.isAirBlock(coord)){
				BlockState bs = world.getBlockState(coord);
				Material blockMat = bs.getBlock().getMaterial(bs);
				if(blockMat == net.minecraft.block.material.Material.GLASS
						|| blockMat == net.minecraft.block.material.Material.LEAVES
						|| blockMat == net.minecraft.block.material.Material.MISCELLANEOUS
						|| blockMat == net.minecraft.block.material.Material.PLANTS
						|| blockMat == net.minecraft.block.material.Material.TALL_PLANTS
						|| blockMat == net.minecraft.block.material.Material.WEB){
					world.destroyBlock(coord, true);
				} else if (blockMat == net.minecraft.block.material.Material.TNT){
					if(world.getBlockState(coord).getBlock() instanceof TNTBlock){
						TNTBlock.explode(world, coord);
						world.removeBlock(coord, false);
					}
				}
			}
			if(EnchantmentHelper.getEnchantmentLevel(Enchantments.high_explosive, srcItemStack) > 0){
				int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.high_explosive, srcItemStack);
				world.createExplosion(playerEntity, rayTrace.getHitVec().x, rayTrace.getHitVec().y, rayTrace.getHitVec().z,
						explodeFactor * lvl, 
						Explosion.Mode.BREAK);
				AxisAlignedBB fireArea = new AxisAlignedBB(rayTrace.getHitVec().x-lvl, rayTrace.getHitVec().y-lvl, rayTrace.getHitVec().z-lvl,
						rayTrace.getHitVec().x+lvl, rayTrace.getHitVec().y+lvl, rayTrace.getHitVec().z+lvl);
				List<LivingEntity> collateralDamage = world.getEntitiesWithinAABB(LivingEntity.class, fireArea);
				for(LivingEntity victim : collateralDamage){
					victim.setFire(lvl);
				}
			}
		}
		if(rayTrace.getType() != RayTraceResult.Type.MISS){
			spawnParticle(ParticleTypes.LARGE_SMOKE, world,
					rayTrace.getHitVec().x, rayTrace.getHitVec().y, rayTrace.getHitVec().z);
			spawnParticle(ParticleTypes.EXPLOSION, world,
					rayTrace.getHitVec().x, rayTrace.getHitVec().y, rayTrace.getHitVec().z);
		}
		
		// recoil enchantment kicks you back
		int recoil = EnchantmentHelper.getEnchantmentLevel(Enchantments.recoil, srcItemStack);
		if(recoil > 0){
			double c = -0.3;
			double lateral = 2.0;
			playerEntity.addVelocity(c * recoil * lookVector.x * lateral, 
					c * recoil * lookVector.y, 
					c * recoil * lookVector.z * lateral);
			if(playerEntity instanceof ServerPlayerEntity){
				// send update packet from server
				((ServerPlayerEntity) playerEntity).connection.sendPacket(new SEntityVelocityPacket(playerEntity));
			}
		}
	}


	private void spawnParticle(IParticleData p, World w, double x, double y, double z) {
		if(w instanceof ServerWorld) {
			((ServerWorld) w).spawnParticle(p, x, y, z, 1, 0, 0, 0, 0);
		} else {
			w.addParticle(p, x, y, z, 0, 0, 0);
		}
	}
	
	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker)
	{
		stack.damageItem(1, attacker, entity -> entity.sendBreakAnimation(EquipmentSlotType.MAINHAND));
		return true;
	}
	
	/** plays a sound at the player location */
	protected void playSound(SoundEvent sound, World world, Entity e){
		if (!world.isRemote)
		{
			playSoundAtPosition(e.getPosX(), e.getPosY(), e.getPosZ(), sound,SoundCategory.PLAYERS,1F,1F,world);
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
					|| EnchantmentHelper.getEnchantmentLevel(net.minecraft.enchantment.Enchantments.INFINITY, musket) > 0
					|| EnchantmentHelper.getEnchantmentLevel(Enchantments.powderless, musket) > 0) return true;
			for (int i = 0; i < playerEntity.inventory.mainInventory.size(); i++) {
				ItemStack inventoryStack = playerEntity.inventory.mainInventory.get(i);
				if (inventoryStack.isEmpty()) continue;
				if (ItemTags.getCollection().getOrCreate(BLACKPOWDER_AMMO_TAG).contains(inventoryStack.getItem())) return true;
			}
		}
		return false;
	}
	

	public static void decrementAmmo(LivingEntity e, ItemStack musket) {
		if(e instanceof PlayerEntity) {
			PlayerEntity playerEntity = (PlayerEntity) e;
			if (playerEntity.isCreative()
					|| EnchantmentHelper.getEnchantmentLevel(net.minecraft.enchantment.Enchantments.INFINITY, musket) > 0
					|| EnchantmentHelper.getEnchantmentLevel(Enchantments.powderless, musket) > 0) return;
			for (int i = 0; i < playerEntity.inventory.mainInventory.size(); i++) {
				ItemStack inventoryStack = playerEntity.inventory.mainInventory.get(i);
				if (inventoryStack.isEmpty()) continue;
				if (ItemTags.getCollection().getOrCreate(BLACKPOWDER_AMMO_TAG).contains(inventoryStack.getItem())) {
					inventoryStack.shrink(1);
					if (inventoryStack.isEmpty()) playerEntity.inventory.mainInventory.set(i, ItemStack.EMPTY);
					return;
				}
			}
		}
	}
	
	
	public static RayTraceResult rayTraceBlocksAndEntities(World w, double maxRange, LivingEntity source){
		Vec3d rayOrigin = new Vec3d(source.getPosX(), source.getPosY() + source.getEyeHeight(), source.getPosZ());
		Vec3d rayDirection = source.getLookVec();
		BlockRayTraceResult terrainHit = w.rayTraceBlocks(new RayTraceContext(rayOrigin, rayOrigin.add(rayDirection.scale(maxRange)),
				RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.SOURCE_ONLY, source));
		Vec3d rayEnd = terrainHit.getHitVec();
		AxisAlignedBB aoi = new AxisAlignedBB(rayOrigin, rayEnd).grow(1.0D);
		// Select existing server entities without a spatial query that can load distant chunks.
		Iterator<Entity> entities = w instanceof ServerWorld ? ((ServerWorld)w).getEntities().iterator()
				: w.getEntitiesWithinAABBExcludingEntity(source, aoi).iterator();
		double closestDistSqr = rayOrigin.squareDistanceTo(rayEnd);
		Entity closestEntity = null;
		Vec3d closestHit = null;
		while(entities.hasNext()){
			Entity e = entities.next();
			if(e == source || e.removed || e.isSpectator() || !aoi.intersects(e.getBoundingBox())) continue;
			Optional<Vec3d> hit = rayHitBeforeTerrain(rayOrigin, rayEnd, e.getBoundingBox());
			if(hit.isPresent()){
				double distSqr = rayOrigin.squareDistanceTo(hit.get());
				if(distSqr < closestDistSqr){
					closestDistSqr = distSqr;
					closestEntity = e;
					closestHit = hit.get();
				}
			}
		}
		return closestEntity == null ? terrainHit : new EntityRayTraceResult(closestEntity, closestHit);
	}

	static Optional<Vec3d> rayHitBeforeTerrain(Vec3d origin, Vec3d terrainEnd, AxisAlignedBB target){
		Optional<Vec3d> hit = target.contains(origin) ? Optional.of(origin) : target.rayTrace(origin, terrainEnd);
		return hit.filter(point -> origin.squareDistanceTo(point) < origin.squareDistanceTo(terrainEnd));
	}

	public static boolean rayIntersectsBoundingBox(Vec3d rayOrigin, Vec3d rayDirection, AxisAlignedBB box){
		if(box == null) return false;
		// algorithm from http://gamedev.stackexchange.com/questions/18436/most-efficient-aabb-vs-ray-collision-algorithms
		Vec3d inverse = new Vec3d(1.0 / rayDirection.x, 1.0 / rayDirection.y, 1.0 / rayDirection.z);
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
	@Override public boolean getIsRepairable(ItemStack srcItemStack, ItemStack rawMaterial)
	{
		// repair with steel ingots
		return ItemTags.getCollection().getOrCreate(STEEL_INGOT_TAG).contains(rawMaterial.getItem());
	}
	
	@Override
	public void addInformation(ItemStack stack, World world, List<ITextComponent> list, net.minecraft.client.util.ITooltipFlag flag){
		super.addInformation(stack,world,list,flag);
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
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack)
	{
		Multimap<String, AttributeModifier> multimap = HashMultimap.create();
		if(slot != EquipmentSlotType.MAINHAND) return multimap;
		multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double)getMeleeDamage(), AttributeModifier.Operation.ADDITION));
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
