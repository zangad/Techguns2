package techguns.items.guns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import techguns.TGItems;
import techguns.TGPackets;
import techguns.TGSounds;
import techguns.Techguns;
import techguns.api.damagesystem.DamageType;
import techguns.api.guns.GunHandType;
import techguns.api.guns.IGenericGun;
import techguns.api.render.IItemTGRenderer;
import techguns.capabilities.TGExtendedPlayer;
import techguns.client.ClientProxy;
import techguns.client.ShooterValues;
import techguns.client.audio.TGSoundCategory;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.entities.ai.EntityAIRangedAttack;
import techguns.entities.npcs.NPCTurret;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GenericProjectile;
import techguns.items.GenericItem;
import techguns.items.armors.GenericArmor;
import techguns.items.armors.ICamoChangeable;
import techguns.items.armors.TGArmorBonus;
import techguns.items.guns.ammo.AmmoType;
import techguns.items.guns.ammo.AmmoTypes;
import techguns.items.guns.ammo.DamageModifier;
import techguns.packets.GunFiredMessage;
import techguns.packets.ReloadStartedMessage;
import techguns.plugins.crafttweaker.EnumGunStat;
import techguns.util.InventoryUtil;
import techguns.util.MathUtil;
import techguns.util.SoundUtil;
import techguns.util.TextUtil;

public class GenericGun extends GenericItem implements IGenericGun, IItemTGRenderer, ICamoChangeable {
	public static final float SOUND_DISTANCE=4.0f;
	
	// Weapon Stats
	boolean semiAuto=false; // = false;
	int minFiretime=4; // = 0;
	int clipsize=10; // = 10;
	int reloadtime=40; // = 40; //ticks
	float damage = 2.0f; // = 10;
	SoundEvent firesound = TGSounds.M4_FIRE;
	SoundEvent reloadsound = TGSounds.M4_RELOAD;
	SoundEvent firesoundStart = TGSounds.M4_FIRE;
	SoundEvent rechamberSound = null;
	int ammoCount; // ammo per reload
	// float recoil = 25.0f;
	float zoomMult = 1.0f;
	boolean canZoom = false;
	boolean toggleZoom = false;
	boolean fireCenteredZoomed=false;
	int ticksToLive = 40;
	float speed = 2.0f;

	float damageMin=1.0f;
	float damageDropStart=20f;
	float damageDropEnd=40f;
	float penetration = 0.0f;

	AmmoType ammoType = AmmoTypes.PISTOL_ROUNDS;

	boolean shotgun = false;
	boolean burst = false;
	float spread = 0.015f;
	int bulletcount = 7;
	float accuracy = 0.0f;

	float projectileForwardOffset = 0.0f;
	
	int maxLoopDelay = 0;
	int recoiltime = 5; // ticks;

	int muzzleFlashtime = 5;
	boolean silenced = false;

	boolean checkRecoil = false;
	boolean checkMuzzleFlash = false;
	
	float AI_attackRange = 15.0F; //sqrt of actual distance
	int AI_attackTime = 60;
	int AI_burstCount = 0;
	int AI_burstAttackTime = 0;
	
	int camoCount=1;
	
	int lockOnTicks = 0; //MaximumLockOnTime
	int lockOnPersistTicks = 0;
	
	EnumCrosshairStyle crossHairStyle = EnumCrosshairStyle.GUN_DYNAMIC;
	
	public ArrayList<ResourceLocation> textures;
	
	//protected IProjectileFactory projectile;
	protected ProjectileSelector projectile_selector; 
	
	GunHandType handType = GunHandType.TWO_HANDED; 
	//Ammount of bullets the gun gets per bullet item
	//protected float shotsPerBullet;
	
	int miningAmmoConsumption = 1;
	
	float meleeDamagePwr = 6.0f;
	float meleeDamageEmpty = 2.0f;
	float digSpeed=1.0f;
	
	/**
	 * spread multiplier while shooting zoomed
	 */
	float zoombonus=1.0f;
	
	float radius=1.0f;
	double gravity=0.0f;

	boolean shootWithLeftClick=true;
	
	public float turretPosOffsetX=0.0f;
	public float turretPosOffsetY=0.0f;
	public float turretPosOffsetZ=0.0f;
	
	public static ArrayList<GenericGun> guns = new ArrayList<>();
	
	/**
	 * Lightpulse parameters
	 */
	public int light_lifetime =2;
	public float light_radius_start = 3.0f;
	public float light_radius_end = 3.0f;
	public float light_r = 1f;
	public float light_g = 0.9f;
	public float light_b = 0.2f;
	protected boolean muzzelight=true;
	
	boolean hasAimedBowAnim=true;
	
	boolean hasAmbientEffect=false;
	/**
	 * Should bind the texture on rendering?
	 */
	public boolean hasCustomTexture=true;

	protected RangeTooltipType rangeTooltipType = RangeTooltipType.DROP;
	
	private GenericGun(String name) {
		super(name,false);
		this.setMaxStackSize(1);
		this.setNoRepair();
	}

	public GenericGun(String name, ProjectileSelector projectileSelector, boolean semiAuto, int minFiretime, int clipsize, int reloadtime, float damage, SoundEvent firesound, SoundEvent reloadsound, int TTL, float accuracy){
		this(true, name,projectileSelector, semiAuto, minFiretime, clipsize, reloadtime, damage, firesound, reloadsound, TTL, accuracy);
	}
	
	public GenericGun setMuzzleLight(float r, float g, float b) {
		this.light_r=r;
		this.light_g=g;
		this.light_b=b;
		return this;
	}
	
	public GenericGun setNoMuzzleLight() {
		this.muzzelight=false;
		return this;
	}
	
	public GenericGun setMuzzleLight(int lifetime, float radius_start, float radius_end, float r, float g, float b) {
		this.light_lifetime = lifetime;
		this.light_radius_start= radius_start;
		this.light_radius_end= radius_end;
		this.setMuzzleLight(r, g, b);
		return this;
	}
	
	public GenericGun setRangeTooltipType(RangeTooltipType type) {
		this.rangeTooltipType=type;
		return this;
	}
	
	public GenericGun setHasAmbient() {
		this.hasAmbientEffect=true;
		return this;
	}
	
	public boolean hasAmbientEffect() {
		return this.hasAmbientEffect;
	}
	
	public GenericGun setNoCustomTexture() {
		this.hasCustomTexture=false;
		return this;
	}
	
	public GenericGun setNoBowAnim() {
		this.hasAimedBowAnim=false;
		return this;
	}
	
	public boolean hasBowAnim() {
		return this.hasAimedBowAnim;
	}
	
	public GenericGun(boolean addToGunList,String name, ProjectileSelector projectile_selector, boolean semiAuto, int minFiretime, int clipsize, int reloadtime, float damage, SoundEvent firesound, SoundEvent reloadsound, int TTL, float accuracy){
		this(name);
		setMaxDamage(clipsize);
		
		this.ammoType = projectile_selector.ammoType;
		this.semiAuto = semiAuto;
		this.minFiretime = minFiretime;
		this.clipsize = clipsize;
		this.reloadtime = reloadtime;
		this.damage = damage;
		this.firesound = firesound;
		this.reloadsound = reloadsound;

		this.ammoCount=1;
		this.ticksToLive=TTL;
		this.accuracy =accuracy;
		
		//Defaults to no drop
		this.damageDropStart=TTL;
		this.damageDropEnd=TTL;
		this.damageMin=damage;
		
		this.projectile_selector = projectile_selector;
		
		if (addToGunList) {
			guns.add(this);
		}
	}
	
	public GenericGun setGravity(double grav) {
		this.gravity=grav;
		return this;
	}
	
	public GenericGun setAmmoCount(int count) {
		this.ammoCount=count;
		return this;
	}

	public GenericGun setZoom(float mult, boolean toggle, float bonus, boolean fireCenteredWhileZooming){
		this.canZoom=true;
		this.zoomMult = mult;
		this.toggleZoom = toggle;
		this.zoombonus=bonus;
		this.fireCenteredZoomed=fireCenteredWhileZooming;
		return this;
	}
	
	
	public GenericGun setShotgunSpred(int count, float spread){
		return this.setShotgunSpread(count, spread, false);
	}
	
	public GenericGun setShotgunSpread(int count, float spread, boolean burst){
		this.shotgun=true;
		this.spread=spread;
		this.bulletcount=count;
		this.burst=burst;
		return this;
	}
	
	public GenericGun setForwardOffset(float offset) {
		this.projectileForwardOffset = offset;
		return this;
	}
	
	public GenericGun setBulletSpeed(float speed){
		this.speed=speed;
		return this;
	}
	
	public GenericGun setPenetration(float pen){
		this.penetration = pen;
		return this;
	}
	
	public GenericGun setMuzzleFlashTime(int time) {
		this.muzzleFlashtime = time;
		return this;
	}

	public GenericGun setFiresoundStart(SoundEvent firesoundStart) {
		this.firesoundStart = firesoundStart;
		return this;
	}
	
	public GenericGun setMaxLoopDelay(int maxLoopDelay) {
		this.maxLoopDelay = maxLoopDelay;
		return this;
	}
	
	public GenericGun setRecoiltime(int recoiltime){
		this.recoiltime = recoiltime;
		return this;
	}
	
	public GenericGun setRechamberSound(SoundEvent rechamberSound) {
		this.rechamberSound = rechamberSound;
		return this;
	}
	
	/**
	 * @param x - Offset sideways, +right -left
	 * @param y - Offset height
	 * @param z - offset forward/backward
	 * @return
	 */
	public GenericGun setTurretPosOffset(float x, float y, float z){
		this.turretPosOffsetX=x;
		this.turretPosOffsetY=y;
		this.turretPosOffsetZ=z;
		return this;
	}
	
	protected int getScaledTTL(){
		return (int) Math.ceil(this.ticksToLive/this.speed);
	}
	
	/**
	 * @param ticks Time required to lock target
	 * @param lockExtraTicks Extra time that is added when the lock is complete
	 */
	public GenericGun setLockOn(int ticks, int lockExtraTicks) {
		this.lockOnTicks = ticks;
		this.lockOnPersistTicks = lockExtraTicks;
		return this;
	}
	

	public int getLockOnTicks() {
		return lockOnTicks;
	}

	public int getLockOnPersistTicks() {
		return lockOnPersistTicks;
	}

	/**
	 * Called only clientside!, requires packets for actions other than zoom (clientside
	 * @param player
	 * @param stack
	 */
	public boolean gunSecondaryAction(EntityPlayer player, ItemStack stack) {
		if (player.world.isRemote && canZoom  && this.toggleZoom && !ShooterValues.getPlayerIsReloading(player, false)) {
			ClientProxy cp = ClientProxy.get();
			if (cp.player_zoom != 1.0f) {
				cp.player_zoom= 1.0f;
			} else {
				cp.player_zoom = this.zoomMult;
			}
			return true;
    	}
		return false; //(this.getGunHandType() == GunHandType.TWO_HANDED); //Block the mouse click if two-handed gun
	}

	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
		if(this.shootWithLeftClick){
			return true;
		} else {
			if(this.getCurrentAmmo(stack)>=this.miningAmmoConsumption){
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		this.gunSecondaryAction(playerIn, playerIn.getHeldItem(handIn));
		return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
	}

	public ItemStack[] getReloadItem(ItemStack stack) {
		return this.ammoType.getAmmo(this.getCurrentAmmoVariant(stack));
	}
	
	public int getAmmoCount() {
		return ammoCount;
	}
	
	public int getAmmoLeftCountTooltip(ItemStack item){
		int ammo = this.getCurrentAmmo(item);
		if(this.burst){
			return ammo*(this.bulletcount+1);
		} else {
			return ammo;
		}
	}

	public int getClipsizeTooltip() {
		if(this.burst){
			return clipsize*(this.bulletcount+1);
		} else {
			return clipsize;
		}
	}
	
	public int getAmmoLeftCount(int mags){
		int count=(this.clipsize/this.ammoCount) * mags; 
		if(this.burst){
			return count*(this.bulletcount+1);
		} else {
			return count;
		}
	}

	protected void spawnProjectile(final World world, final EntityLivingBase player, final ItemStack itemstack, float spread, float offset, float damagebonus, EnumBulletFirePos firePos, Entity target) {
		
		IProjectileFactory<GenericProjectile> projectile = this.projectile_selector.getFactoryForType(this.getCurrentAmmoVariantKey(itemstack));
		
		GenericProjectile proj = projectile.createProjectile(this, world, player, damage * damagebonus, speed, this.getScaledTTL(), spread, this.damageDropStart,
				this.damageDropEnd, this.damageMin * damagebonus, this.penetration, getDoBlockDamage(player), firePos, radius, gravity);

		
		float f=1.0f;
		if(this.muzzelight) {
			Techguns.proxy.createLightPulse(proj.posX+player.getLookVec().x*f, proj.posY+player.getLookVec().y*f, proj.posZ+player.getLookVec().z*f, this.light_lifetime, this.light_radius_start, this.light_radius_end, this.light_r, this.light_g, this.light_b);
		}
		if (silenced) {
			proj.setSilenced();
		}
		if (offset > 0.0f) {
			proj.shiftForward(offset/speed); // System.out.println("Shifted
										// by"+offset);
		}
		world.spawnEntity(proj);
	}

	public static boolean getDoBlockDamage(EntityLivingBase elb) {
		boolean blockdamage = false;
		if (elb instanceof EntityPlayer){
			TGExtendedPlayer caps = TGExtendedPlayer.get((EntityPlayer) elb);
			if(caps!=null){ blockdamage=!caps.enableSafemode; }
		}
		
		return blockdamage;
	}

	@Override
	public boolean isShootWithLeftClick() {
		return this.shootWithLeftClick;
	}

	@Override
	public boolean isSemiAuto() {
		return this.semiAuto;
	}
	
	public GenericGun setCheckRecoil(){
		this.checkRecoil=true;
		return this;
	}
	
	public GenericGun setCheckMuzzleFlash(){
		this.checkMuzzleFlash=true;
		return this;
	}

	@Override
	public boolean isZooming() {
		return ClientProxy.get().player_zoom==this.zoomMult;
	}

	@Override
	public void shootGunPrimary(ItemStack stack, World world, EntityPlayer player, boolean zooming, EnumHand hand, Entity target) {
	
    		int ammo = this.getCurrentAmmo(stack);
    	
    		//System.out.println("Shoot gun:"+stack+" Hand:"+hand);
    		
    		byte ATTACK_TYPE = 0;
    		TGExtendedPlayer extendedPlayer = TGExtendedPlayer.get(player);
    		if (ammo>0) {
    			//bullets left
		    	int firedelay = extendedPlayer.getFireDelay(hand);

		    	if (firedelay <=0) {
		  
		    		extendedPlayer.setFireDelay(hand,this.minFiretime);
		    
		    		if (!player.capabilities.isCreativeMode) {
	    				this.useAmmo(stack, 1);
		   
		    		}
		    		
			        if (!world.isRemote)
			        {
			        	/*
			        	 * If SERVER, create projectile
			        	 */
			        	
			        	float accuracybonus = MathUtil.clamp(1.0f-GenericArmor.getArmorBonusForPlayer(player, TGArmorBonus.GUN_ACCURACY,false), 0f,1f);
			        	
			        	//add spread penalty when shooting while blocking
			        	if(hand== EnumHand.MAIN_HAND && player.isActiveItemStackBlocking()) {
			        		if(this.handType==GunHandType.ONE_HANDED) {
			        			accuracybonus *= 4.0f;
			        		} else {
			        			accuracybonus *= 8.0f;
			        		}
			        	}
			        	
			        	EnumBulletFirePos firePos;
			        	if ((hand == EnumHand.MAIN_HAND && player.getPrimaryHand() == EnumHandSide.RIGHT) || (hand == EnumHand.OFF_HAND && player.getPrimaryHand() == EnumHandSide.LEFT)) {
			        		firePos = EnumBulletFirePos.RIGHT;
			        	}else {
			        		firePos = EnumBulletFirePos.LEFT;
			        	}
			        	
			        	if(zooming){
			        		accuracybonus*=this.zoombonus;
			        		if (fireCenteredZoomed) {
			        			firePos=EnumBulletFirePos.CENTER;
			        		}
			        	}
			        	this.shootGun(world, player,stack, accuracybonus,1.0f,ATTACK_TYPE, hand,firePos, target);
			        	        	
			        } else {
			        	/*
			        	 * If CLIENT, do Effects
			        	 */	
			          	int recoiltime_l = (int) ((((float)recoiltime/20.0f)*1000.0f));
			        	int muzzleFlashtime_l = (int) ((((float)muzzleFlashtime/20.0f)*1000.0f));

			        	
			        	if (!checkRecoil || !ShooterValues.isStillRecoiling(player,hand==EnumHand.OFF_HAND, ATTACK_TYPE) ){
			        		//System.out.println("SettingRecoilTime");
				        	ShooterValues.setRecoiltime(player, hand==EnumHand.OFF_HAND,System.currentTimeMillis() + recoiltime_l, recoiltime_l,ATTACK_TYPE);
				        	
			        	}
			        	
			        	ClientProxy cp = ClientProxy.get();
			        	if (!checkMuzzleFlash || ShooterValues.getMuzzleFlashTime(player, hand==EnumHand.OFF_HAND) <= System.currentTimeMillis()) {
			        		ShooterValues.setMuzzleFlashTime(player, hand==EnumHand.OFF_HAND, System.currentTimeMillis() + muzzleFlashtime_l, muzzleFlashtime_l);
			        		Random rand = world.rand;
			        		cp.muzzleFlashJitterX = 1.0f-(rand.nextFloat()*2.0f);
			        		cp.muzzleFlashJitterY = 1.0f-(rand.nextFloat()*2.0f);
			        		cp.muzzleFlashJitterScale = 1.0f-(rand.nextFloat()*2.0f);
			        		cp.muzzleFlashJitterAngle = 1.0f-(rand.nextFloat()*2.0f);
			        	}
			        	
				        client_weaponFired();
			        }
			        /*
			         * Do sounds on Client & Server
			         */
		        	if (maxLoopDelay > 0 && extendedPlayer.getLoopSoundDelay(hand)<=0) {

		        		SoundUtil.playSoundOnEntityGunPosition(world, player, this.firesoundStart, SOUND_DISTANCE, 1.0F, false, false, TGSoundCategory.GUN_FIRE);

		        		extendedPlayer.setLoopSoundDelay(hand,this.maxLoopDelay);
		        		
		        	}else {
	
		        		SoundUtil.playSoundOnEntityGunPosition(world, player, this.firesound, SOUND_DISTANCE, 1.0F, false, false, TGSoundCategory.GUN_FIRE);
		        		if (this.maxLoopDelay>0){
		        			extendedPlayer.setLoopSoundDelay(hand,this.maxLoopDelay);
		        		}
		        	}
		        	
		        	if (!(rechamberSound==null)) {

		        		SoundUtil.playSoundOnEntityGunPosition(world, player, rechamberSound, 1.0F, 1.0F, false, false, TGSoundCategory.RELOAD);
		        	}
			        
		    	} else {
		    		//System.out.println(Thread.currentThread().toString()+": Skip shot, can't fire yet");
		    	}
		    	
    		} else {
    			//mag empty, reload needed
    			
    			//look for ammo
    			if (InventoryUtil.consumeAmmoPlayer(player,this.ammoType.getAmmo(this.getCurrentAmmoVariant(stack)))) {
    			
    				Arrays.stream(this.ammoType.getEmptyMag()).forEach( e -> {
    					if (!e.isEmpty()){
        					//player.inventory.addItemStackToInventory(new ItemStack(emptyMag.getItem(),1,emptyMag.getItemDamage()));
        					int amount=InventoryUtil.addAmmoToPlayerInventory(player, TGItems.newStack(e, 1));
        					if(amount >0 && !world.isRemote){
        						player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY, player.posZ, TGItems.newStack(e, amount)));
        					}
        				}
    				});
    				
    				
    				
    				//stop toggle zooming when reloading
    				if (world.isRemote) {
    					if (canZoom  && this.toggleZoom) {
    						ClientProxy cp = ClientProxy.get();
    		    			if (cp.player_zoom != 1.0f) {
    		    				cp.player_zoom= 1.0f;
    		    			}
    		    		}
    				}
    				
	    			//START RELOAD

    				extendedPlayer.setFireDelay(hand, this.reloadtime-this.minFiretime);
    						    			
	    			if (ammoCount >1) {
	    				int i =1;
	    				while (i<ammoCount && InventoryUtil.consumeAmmoPlayer(player,this.ammoType.getAmmo(this.getCurrentAmmoVariant(stack)))){
	    					i++;
	    				}

	    				this.reloadAmmo(stack, i);
	    			} else {
	    				this.reloadAmmo(stack);
	    			}
	    			
	    			SoundUtil.playReloadSoundOnEntity(world,player,reloadsound, 1.0F, 1.0F, false, true, TGSoundCategory.RELOAD);

	    			
    				if (world.isRemote) {
    					
    					int time = (int) (((float)reloadtime/20.0f)*1000);
    					ShooterValues.setReloadtime(player,hand==EnumHand.OFF_HAND,System.currentTimeMillis()+time, time, ATTACK_TYPE);
    					
    					client_startReload();
    				} else{
    					//send reloadpacket
    					//send pakets to clients
    					
				    	int msg_reloadtime = ((int)(((float)reloadtime/20.0f)*1000.0f));
				    	TGPackets.network.sendToAllAround(new ReloadStartedMessage(player,hand,msg_reloadtime,ATTACK_TYPE), TGPackets.targetPointAroundEnt(player, 100.0));
    			    	//
    				}

    			} else {
    			
    				
    				if (!world.isRemote)
			        {
    					//TODO emptySound
	    				//world.playSoundAtEntity(player, "mob.villager.idle", 1.0F, 1.0F );
			        }
    			}
    		}
	}
	
	/**
	 * for extra actions in subclass
	 */
	protected void client_weaponFired() {	
	}

	/**
	 * for extra actions in subclass
	 */
	protected void client_startReload() {	
	}
	
	public GenericGun setAIStats(float attackRange, int attackTime, int burstCount, int burstAttackTime) {
		this.AI_attackRange = attackRange;
		this.AI_attackTime = attackTime;
		this.AI_burstCount = burstCount;
		this.AI_burstAttackTime = burstAttackTime;
		return this;
	}
	
	public GenericGun setTexture(String path){
		return setTextures(path,1);
	}
	
	public GenericGun setTextures(String path, int variations){
		Techguns.proxy.setGunTextures(this, path, variations);
		this.camoCount=variations;
		return this;
	}
	
	public GenericGun setTexture(ResourceLocation path){
		return setTextures(path,1);
	}
	
	public GenericGun setTextures(ResourceLocation path, int variations){
		Techguns.proxy.setGunTextures(this, path, variations);
		this.camoCount=variations;
		return this;
	}
	
	@Override
	public ResourceLocation getCurrentTexture(ItemStack stack) {
		int camo = this.getCurrentCamoIndex(stack);
		if (camo < this.textures.size()) {
			return textures.get(camo);
		}
		return this.textures.get(0);
	}

	protected void shootGun(World world, EntityLivingBase player,ItemStack itemstack,float accuracybonus,float damagebonus, int attackType, EnumHand hand, EnumBulletFirePos firePos, Entity target){
		
		//boolean leftGun = (hand == EnumHand.OFF_HAND) != (player.getPrimaryHand() == EnumHandSide.LEFT);
		
		//send pakets to clients
		if (!world.isRemote){
	    	int msg_recoiltime = ((int)(((float)recoiltime/20.0f)*1000.0f));
	    	int msg_muzzleflashtime = ((int)(((float)muzzleFlashtime/20.0f)*1000.0f));
	    	TGPackets.network.sendToAllAround(new GunFiredMessage(player,msg_recoiltime,msg_muzzleflashtime,attackType,checkRecoil,hand==EnumHand.OFF_HAND), TGPackets.targetPointAroundEnt(player, 100.0f));
		}
    	//
		spawnProjectile(world, player,itemstack, accuracy*accuracybonus, projectileForwardOffset,damagebonus, firePos, target);
		
        if (shotgun){
        	float offset=0;
        	if (this.burst){
        		offset = this.speed/(this.bulletcount);
        	}
        	
        	for (int i=0; i<bulletcount; i++) {
        		spawnProjectile(world, player,itemstack, spread*accuracybonus,projectileForwardOffset+offset*(i+1.0f),damagebonus, firePos, target);
        	}
        }		
	}
		
	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) {
		//super.onCreated(stack, world, player);
		NBTTagCompound tags = stack.getTagCompound();
		if(tags==null){
			tags=new NBTTagCompound();
			stack.setTagCompound(tags);
		
			int dmg = stack.getItemDamage();
			tags.setByte("camo", (byte) 0);
			tags.setString("ammovariant", AmmoTypes.TYPE_DEFAULT);
			tags.setShort("ammo", dmg==0 ? (short)this.clipsize : (short)(this.clipsize-dmg));
			stack.setItemDamage(0);
			this.addInitialTags(tags);
		} else {
			stack.setItemDamage(0);
		}
	}
	
	/**
	 * Add subclass tags here
	 * @param tags
	 */
	protected void addInitialTags(NBTTagCompound tags) {
		
	}
	
	public int getCurrentAmmo(ItemStack stack){
		NBTTagCompound tags = stack.getTagCompound();
		if(tags==null){
			this.onCreated(stack, null, null); //world and player are not needed
			tags = stack.getTagCompound();
		}
		return tags.getShort("ammo");
	}
	
	public int getCurrentAmmoVariant(ItemStack stack){
		String variant = this.getCurrentAmmoVariantKey(stack);
		return this.getAmmoType().getIDforVariantKey(variant);
	}
	
	public String getCurrentAmmoVariantKey(ItemStack stack){
		NBTTagCompound tags = stack.getTagCompound();
		if(tags==null){
			this.onCreated(stack, null, null); //world and player are not needed
			tags = stack.getTagCompound();
		}
		String var = tags.getString("ammovariant");
		if(var==null || var.equals("")) return AmmoTypes.TYPE_DEFAULT;
		return var;
	}
	
	/**
	 * Reduces ammo by amount, use positive values! amount=3 means ammo-=3;
	 * @param stack
	 * @param amount
	 * @return actually consumed ammo
	 */
	public int useAmmo(ItemStack stack, int amount){
		int ammo = this.getCurrentAmmo(stack);
		NBTTagCompound tags = stack.getTagCompound();
		if(ammo-amount>=0){
			tags.setShort("ammo", (short) (ammo-amount));
			return amount;
		} else {
			tags.setShort("ammo", (short) 0);
			return ammo;
		}
	}
	
	public void reloadAmmo(ItemStack stack){
		this.reloadAmmo(stack, this.clipsize);
	}
	
	public void reloadAmmo(ItemStack stack, int amount){
		int ammo = this.getCurrentAmmo(stack);
		NBTTagCompound tags = stack.getTagCompound();
		tags.setShort("ammo", (short) (ammo+amount));
	}
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)){
			ItemStack gun = new ItemStack(this, 1,0);
			this.onCreated(gun, null, null);
			items.add(gun);
		}
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1.0-getPercentAmmoLeft(stack);
	}

	public double getPercentAmmoLeft(ItemStack stack) {
		return ((double)this.getCurrentAmmo(stack))/((double)this.clipsize);
	}
	
	@Override
	public int getAmmoLeft(ItemStack stack) {
		return this.getCurrentAmmo(stack);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		
		return slotChanged || !oldStack.isItemEqual(newStack); 
		/*if (!b && oldStack!=newStack) {
			Techguns.proxy.fixReequipAnim(oldStack, newStack);
		}*/
		//return b;
	}
	
	public GenericGun setDamageDrop(float start, float end, float minDamage){
		this.damageDropStart=start;
		this.damageDropEnd=end;
		this.damageMin=minDamage;
		return this;
	}
	
	public GenericGun setSilenced(boolean s){
		this.silenced=s;
		return this;
	}

	@Override
	public GunHandType getGunHandType() {
		return this.handType;
	}
	
	public GenericGun setHandType(GunHandType type) {
		this.handType=type;
		return this;
	}

	@Override
	public boolean isHoldZoom() {
		return !this.toggleZoom;
	}

	@Override
	public float getZoomMult() {
		return this.zoomMult;
	}

	protected String getTooltipTextDmg(ItemStack stack, boolean expanded) {
		
		DamageModifier mod = this.projectile_selector.getFactoryForType(this.getCurrentAmmoVariantKey(stack)).getDamageModifier();
		float dmg = mod.getDamage(this.damage);
		if(dmg==this.damage) {
			return ""+this.damage+(this.damageMin<this.damage?"-"+this.damageMin:"");
		} else {
			float dmgmin = mod.getDamage(this.damageMin);
			ChatFormatting prefix=ChatFormatting.GREEN;
			String sgn="+";
			if(dmg<this.damage) {
				prefix = ChatFormatting.RED;
				sgn="-";
			}
			

			String suffix="";

			if(expanded) {
				if(mod.getDmgMul()!=1f) {
					float f = mod.getDmgMul()-1f;
					String x = String.format("%.0f", f*100f);
					suffix += " ("+sgn+x+"%)";
				}
				if(mod.getDmgAdd()!=0f) {
					float add = mod.getDmgAdd();
					suffix += " ("+(add>0?"+":"")+String.format("%.1f", add)+")";
				}
			}
			
			String sd = String.format("%.1f", dmg);
			String sm = String.format("%.1f", dmgmin);
			
			return prefix+""+sd+(dmgmin<dmg?"-"+sm:"")+suffix;
		}
		
	}
	
	protected String getTooltipTextRange(ItemStack stack) {
		DamageModifier mod = this.projectile_selector.getFactoryForType(this.getCurrentAmmoVariantKey(stack)).getDamageModifier();
		
		int ttl = mod.getTTL(this.ticksToLive);
		float rangeStart = mod.getRange(this.damageDropStart);
		float rangeEnd = mod.getRange(this.damageDropEnd);
		
		String prefix = "";	
		String suffix="";
		if(rangeStart != this.damageDropStart) {

			String sgn="+";
			if (rangeStart > this.damageDropStart) {
				prefix = ChatFormatting.GREEN.toString();
			} else {
				prefix = ChatFormatting.RED.toString();
				sgn="-";
			}
			
			if(mod.getRangeMul()!=1f) {
				float f = mod.getRangeMul()-1f;
				String x = String.format("%.0f", f*100f);
				suffix += " ("+sgn+x+"%)";
			}
			if(mod.getRangeAdd()!=0f) {
				float add = mod.getRangeAdd();
				suffix += " ("+(add>0?"+":"")+String.format("%.1f", add)+")";
			}
	
		} 
			
		String sStart = String.format("%.1f", rangeStart);
		String sEnd = String.format("%.1f", rangeEnd);
		
		if(this.rangeTooltipType == RangeTooltipType.DROP) {
			return TextUtil.trans("techguns.gun.tooltip.range")+": "+prefix+sStart+","+sEnd+","+ttl+suffix;
		} else if ( this.rangeTooltipType == RangeTooltipType.NO_DROP) {
			return TextUtil.trans("techguns.gun.tooltip.range")+": "+prefix+sStart+suffix;
		} else {
			return TextUtil.trans("techguns.gun.tooltip.radius")+": "+prefix+sStart+"-"+sEnd+suffix;
		}
	
	}
	
	protected String getTooltipTextVelocity(ItemStack stack) {
		DamageModifier mod = this.projectile_selector.getFactoryForType(this.getCurrentAmmoVariantKey(stack)).getDamageModifier();
		
		float velocity = mod.getVelocity(this.speed);
		
		String prefix = "";	
		String suffix="";
		if(velocity != this.speed) {

			String sgn="+";
			if (velocity >= this.speed) {
				prefix = ChatFormatting.GREEN.toString();
			} else {
				prefix = ChatFormatting.RED.toString();
				sgn="";
			}
			
			if(mod.getVelocityMul()!=1f) {
				float f = mod.getVelocityMul()-1f;
				String x = String.format("%.0f", f*100f);
				suffix += " ("+sgn+x+"%)";
			}
			if(mod.getVelocityAdd()!=0f) {
				float add = mod.getVelocityAdd();
				suffix += " ("+(add>0?"+":"")+String.format("%.1f", add)+")";
			}
	
		} 
			
		String sVelocity = String.format("%.1f", velocity);
		
		return TextUtil.trans("techguns.gun.tooltip.velocity")+": "+prefix+sVelocity+suffix;
	
	}
		
	protected void addMiningTooltip(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn, boolean longTooltip) {}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, list, flagIn);
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
			list.add(TextUtil.trans("techguns.gun.tooltip.handtype")+": "+this.getGunHandType().toString());
			
			ItemStack[] ammo = this.ammoType.getAmmo(this.getCurrentAmmoVariant(stack));
			for(int i=0;i< ammo.length;i++) {
				list.add(TextUtil.trans("techguns.gun.tooltip.ammo")+": "+(this.ammoCount>1 ? this.ammoCount+"x " : "")+ChatFormatting.WHITE+TextUtil.trans(ammo[i].getUnlocalizedName()+".name"));
			}
			this.addMiningTooltip(stack, worldIn, list, flagIn, true);
			list.add(TextUtil.trans("techguns.gun.tooltip.damageType")+": "+this.getDamageType(stack).toString());
			list.add(TextUtil.trans("techguns.gun.tooltip.damage")+(this.shotgun ? ("(x"+ (this.bulletcount+1)+")") : "" )+": "+getTooltipTextDmg(stack,true));
			//list.add(TextUtil.trans("techguns.gun.tooltip.range")+": "+this.damageDropStart+","+this.damageDropEnd+","+this.ticksToLive);
			list.add(getTooltipTextRange(stack));
			//list.add(TextUtil.trans("techguns.gun.tooltip.velocity")+": "+this.speed);
			list.add(getTooltipTextVelocity(stack));
			list.add(TextUtil.trans("techguns.gun.tooltip.spread")+": "+this.accuracy + (this.zoombonus!=1.0 ? (" Z:"+this.zoombonus*this.accuracy) : ""));
			list.add(TextUtil.trans("techguns.gun.tooltip.clipsize")+": "+this.clipsize);
			list.add(TextUtil.trans("techguns.gun.tooltip.reloadTime")+": "+this.reloadtime*0.05f+"s");
			if (this.penetration>0.0f){
				list.add(TextUtil.trans("techguns.gun.tooltip.armorPen")+": "+String.format("%.1f", this.penetration));
			}
			if (this.canZoom) {
				list.add(TextUtil.trans("techguns.gun.tooltip.zoom")+":"+(this.toggleZoom ? "("+TextUtil.trans("techguns.gun.tooltip.zoom.toogle")+")":"("+TextUtil.trans("techguns.gun.tooltip.zoom.hold")+")")+" "+TextUtil.trans("techguns.gun.tooltip.zoom.multiplier")+":"+this.zoomMult);
			}
			
		} else {
			ItemStack[] ammo = this.ammoType.getAmmo(this.getCurrentAmmoVariant(stack));
			for(int i=0;i< ammo.length;i++) {
				list.add(TextUtil.trans("techguns.gun.tooltip.ammo")+": "+(this.ammoCount>1 ? this.ammoCount+"x " : "")+ChatFormatting.WHITE+TextUtil.trans(ammo[i].getUnlocalizedName()+".name"));
			}
			this.addMiningTooltip(stack, worldIn, list, flagIn, false);
			list.add(TextUtil.trans("techguns.gun.tooltip.damage")+(this.shotgun ? ("(x"+ (this.bulletcount+1)+")") : "" )+": "+getTooltipTextDmg(stack,false));
			list.add(TextUtil.trans("techguns.gun.tooltip.shift1")+" "+ChatFormatting.GREEN+TextUtil.trans("techguns.gun.tooltip.shift2")+" "+ChatFormatting.GRAY+TextUtil.trans("techguns.gun.tooltip.shift3"));
		}
		//} else {
		//	list.add("Sneak to view stats");
		//}
	}

	public DamageType getDamageType(ItemStack stack) {
		return this.projectile_selector.getFactoryForType(this.getCurrentAmmoVariantKey(stack)).getDamageType();
	}

	@Override
	public int getCamoCount() {
		return this.camoCount;
	}

	@Override
	public String getCurrentCamoName(ItemStack item) {
		NBTTagCompound tags = item.getTagCompound();
		byte camoID=0;
		if (tags!=null && tags.hasKey("camo")){
			camoID=tags.getByte("camo");
		}
		if(camoID>0){
			return TextUtil.trans(this.getUnlocalizedName()+".camoname."+camoID);
		} else {
			return TextUtil.trans("techguns.item.defaultcamo");
		}
	}

	/**
	 * Should this weapon shoot with left click instead of mine, defaults to true
	 * @param shootWithLeftClick
	 */
	public GenericGun setShootWithLeftClick(boolean shootWithLeftClick) {
		this.shootWithLeftClick = shootWithLeftClick;
		return this;
	}
	
	public GenericGun setMiningAmmoConsumption(int ammo) {
		this.miningAmmoConsumption = ammo;
		return this;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity targetEntity) {
		
		if (this.shootWithLeftClick) {
			return true;
		} else {
			if (player.world.isRemote) {

				int time = (int) (((float) this.recoiltime / 20.0f) * 1000);
				//ClientProxy.get().setplayerRecoiltime(player, System.currentTimeMillis() + time, time, (byte) 0);
				ShooterValues.setRecoiltime(player, false, System.currentTimeMillis() + time, time, (byte) 0);
			}

			/**
			 * COPY FROM ENTITYPLAYER
			 */
			if (targetEntity.canBeAttackedWithItem() && GenericProjectile.BULLET_TARGETS.apply(targetEntity)) {
				if (!targetEntity.hitByEntity(player)) {
					float f = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
					float f1;

					if (targetEntity instanceof EntityLivingBase) {
						f1 = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), ((EntityLivingBase) targetEntity).getCreatureAttribute());
					} else {
						f1 = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
					}

					float f2 = player.getCooledAttackStrength(0.5F);
					f = f * (0.2F + f2 * f2 * 0.8F);
					f1 = f1 * f2;
					player.resetCooldown();

					if (f > 0.0F || f1 > 0.0F) {
						boolean flag = f2 > 0.9F;
						boolean flag1 = false;
						int i = 0;
						i = i + EnchantmentHelper.getKnockbackModifier(player);

						if (player.isSprinting() && flag) {
							player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK,
									player.getSoundCategory(), 1.0F, 1.0F);
							++i;
							flag1 = true;
						}

						boolean flag2 = flag && player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater()
								&& !player.isPotionActive(MobEffects.BLINDNESS) && !player.isRiding() && targetEntity instanceof EntityLivingBase;
						flag2 = flag2 && !player.isSprinting();

						if (flag2) {
							f *= 1.5F;
						}

						f = f + f1;
						boolean flag3 = false;
						double d0 = (double) (player.distanceWalkedModified - player.prevDistanceWalkedModified);

						if (flag && !flag2 && !flag1 && player.onGround && d0 < (double) player.getAIMoveSpeed()) {
							ItemStack itemstack = player.getHeldItem(EnumHand.MAIN_HAND);

							flag3= this.hasSwordSweep() && this.getAmmoLeft(stack)>0; //if (itemstack.getItem() instanceof ItemSword) {
							//	flag3 = true;
							//}
						}

						float f4 = 0.0F;
						boolean flag4 = false;
						int j = EnchantmentHelper.getFireAspectModifier(player);

						if (targetEntity instanceof EntityLivingBase) {
							f4 = ((EntityLivingBase) targetEntity).getHealth();

							if (j > 0 && !targetEntity.isBurning()) {
								flag4 = true;
								targetEntity.setFire(1);
							}
						}

						double d1 = targetEntity.motionX;
						double d2 = targetEntity.motionY;
						double d3 = targetEntity.motionZ;
						
						//EDIT: return parameter workaround for attackEntityFrom
						TGDamageSource src = getMeleeDamageSource(player,stack);
						targetEntity.attackEntityFrom(src, f);
						boolean flag5 = src.wasSuccessful();

						if (flag5) {
							this.consumeAmmoOnMeleeHit(player, stack);
							
							if (i > 0) {
								if (targetEntity instanceof EntityLivingBase) {
									((EntityLivingBase) targetEntity).knockBack(player, (float) i * 0.5F, (double) MathHelper.sin(player.rotationYaw * 0.017453292F),
											(double) (-MathHelper.cos(player.rotationYaw * 0.017453292F)));
								} else {
									targetEntity.addVelocity((double) (-MathHelper.sin(player.rotationYaw * 0.017453292F) * (float) i * 0.5F), 0.1D,
											(double) (MathHelper.cos(player.rotationYaw * 0.017453292F) * (float) i * 0.5F));
								}

								player.motionX *= 0.6D;
								player.motionZ *= 0.6D;
								player.setSprinting(false);
							}

							if (flag3) {
								float f3 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * f;

								for (EntityLivingBase entitylivingbase : player.world.getEntitiesWithinAABB(EntityLivingBase.class,
										targetEntity.getEntityBoundingBox().grow(1.0D, 0.25D, 1.0D))) {
									if (entitylivingbase != player && entitylivingbase != targetEntity && !player.isOnSameTeam(entitylivingbase)
											&& player.getDistanceSq(entitylivingbase) < 9.0D) {
										entitylivingbase.knockBack(player, 0.4F, (double) MathHelper.sin(player.rotationYaw * 0.017453292F),
												(double) (-MathHelper.cos(player.rotationYaw * 0.017453292F)));
										TGDamageSource dmgsrc = getMeleeDamageSource(player,stack);
										entitylivingbase.attackEntityFrom(dmgsrc, f3);
										if(dmgsrc.wasSuccessful()) {
											this.onMeleeHitTarget(stack, entitylivingbase);
										}
									}
								}

								/*player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
										player.getSoundCategory(), 1.0F, 1.0F);
								player.spawnSweepParticles();*/
								this.doSweepAttackEffect(player);
							}

							/**
							 * Extra hit effect 
							 */
							this.onMeleeHitTarget(stack, targetEntity);
							
							if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
								((EntityPlayerMP) targetEntity).connection.sendPacket(new SPacketEntityVelocity(targetEntity));
								targetEntity.velocityChanged = false;
								targetEntity.motionX = d1;
								targetEntity.motionY = d2;
								targetEntity.motionZ = d3;
							}

							if (flag2) {
								player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(),
										1.0F, 1.0F);
								player.onCriticalHit(targetEntity);
							}

							if (!flag2 && !flag3) {
								if (flag) {
									player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
											player.getSoundCategory(), 1.0F, 1.0F);
								} else {
									player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK,
											player.getSoundCategory(), 1.0F, 1.0F);
								}
							}

							if (f1 > 0.0F) {
								player.onEnchantmentCritical(targetEntity);
							}

							player.setLastAttackedEntity(targetEntity);

							if (targetEntity instanceof EntityLivingBase) {
								EnchantmentHelper.applyThornEnchantments((EntityLivingBase) targetEntity, player);
							}

							EnchantmentHelper.applyArthropodEnchantments(player, targetEntity);
							ItemStack itemstack1 = player.getHeldItemMainhand();
							Entity entity = targetEntity;

							if (targetEntity instanceof MultiPartEntityPart) {
								IEntityMultiPart ientitymultipart = ((MultiPartEntityPart) targetEntity).parent;

								if (ientitymultipart instanceof EntityLivingBase) {
									entity = (EntityLivingBase) ientitymultipart;
								}
							}

							if (!itemstack1.isEmpty() && entity instanceof EntityLivingBase) {
								ItemStack beforeHitCopy = itemstack1.copy();
								itemstack1.hitEntity((EntityLivingBase) entity, player);

								if (itemstack1.isEmpty()) {
									net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, beforeHitCopy, EnumHand.MAIN_HAND);
									player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
								}
							}

							if (targetEntity instanceof EntityLivingBase) {
								float f5 = f4 - ((EntityLivingBase) targetEntity).getHealth();
								player.addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));

								if (j > 0) {
									targetEntity.setFire(j * 4);
								}

								if (player.world instanceof WorldServer && f5 > 2.0F) {
									int k = (int) ((double) f5 * 0.5D);
									((WorldServer) player.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, targetEntity.posX,
											targetEntity.posY + (double) (targetEntity.height * 0.5F), targetEntity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D);
								}
							}

							player.addExhaustion(0.1F);
						} else {
							player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(),
									1.0F, 1.0F);

							if (flag4) {
								targetEntity.extinguish();
							}
						}
					}
				}
			}

			return true;
		}
	}
	
	protected void onMeleeHitTarget(ItemStack stack, Entity target) {
	}
	
	protected void consumeAmmoOnMeleeHit(EntityLivingBase elb, ItemStack stack) {
		if(elb instanceof EntityPlayer) {
			EntityPlayer ply = (EntityPlayer) elb;
			if(ply.capabilities.isCreativeMode) return;
		}
		this.useAmmo(stack, 1);
	}
	
	protected void doSweepAttackEffect(EntityPlayer player) {
        if(!player.world.isRemote) {
			double d0 = (double)(-MathHelper.sin(player.rotationYaw * 0.017453292F));
	        double d1 = (double)MathHelper.cos(player.rotationYaw * 0.017453292F);
        	double x = player.posX+d0;
        	double y = player.posY+player.height*0.8d;
        	double z = player.posZ+d1;
        	this.spawnSweepParticle(player.world, x, y, z, d0, 0, d1);
        	this.playSweepSoundEffect(player);
        }
	}

	protected void playSweepSoundEffect(EntityPlayer player) {
		player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
				player.getSoundCategory(), 1.0F, 1.0F);
	}
	
	protected void spawnSweepParticle(World w, double x, double y, double z, double motionX, double motionY, double motionZ) {
		
	}
	
	/**
	 * Override in Subclass to define damagesource used for player melee attacks, only relevant if "shootWithLeftClick" is not set
	 * @return
	 */
	protected TGDamageSource getMeleeDamageSource(EntityPlayer player, ItemStack stack){
		TGDamageSource src = new TGDamageSource("player", player, player, DamageType.PHYSICAL, DeathType.GORE);
		return src;
	}
	
	protected boolean hasSwordSweep() {
		return true;
	}
	
	public int getMiningAmmoConsumption() {
		return miningAmmoConsumption;
	}

	@Override
	public boolean isModelBase(ItemStack stack) {
		return this.hasCustomTexture;
	}
	
	public EntityAIRangedAttack getAIAttack(IRangedAttackMob shooter) {
		return new EntityAIRangedAttack(shooter, 1.0D, this.AI_attackTime/3, this.AI_attackTime, this.AI_attackRange, this.AI_burstCount, this.AI_burstAttackTime);
	}
	
	 public AmmoType getAmmoType() {
		return ammoType;
	}

	public float getAI_attackRange() {
		return AI_attackRange;
	}
	 
	public boolean isFullyLoaded(ItemStack stack){
		return this.clipsize == this.getCurrentAmmo(stack);
	}
	
	public boolean hasRightClickAction() {
		return this.getGunHandType()==GunHandType.TWO_HANDED && this.canZoom;
	}
	
	/**
     * Weapon is used by NPC
     */
    public void fireWeaponFromNPC(EntityLivingBase shooter, float dmgscale, float accscale) {
    	
    	SoundUtil.playSoundOnEntityGunPosition(shooter.world, shooter ,firesound, SOUND_DISTANCE, 1.0F, false, false, TGSoundCategory.GUN_FIRE);
    	
    	EnumBulletFirePos firePos = EnumBulletFirePos.RIGHT;
    	
    	if (shooter instanceof NPCTurret){
    		//dmgscale=1.0f;
    		//accscale=1.0f;
    		firePos=EnumBulletFirePos.CENTER;
    	}
    	
    	if (!shooter.world.isRemote){
    		this.shootGun(shooter.world, shooter, shooter.getHeldItemMainhand(), this.zoombonus*accscale,dmgscale,0, EnumHand.MAIN_HAND, firePos, null);
    	}
    }
    
    /**
     * Get all ammo and magazines the gun currently holds is retrievable
     * @param stack
     * @return
     */
    public List<ItemStack> getAmmoOnUnload(ItemStack stack){
    	List<ItemStack> items = new ArrayList<>();
    	
    	int ammo = this.getCurrentAmmo(stack);
    	
    	if(this.ammoCount>1 && this.getAmmoLeft(stack)>0) {
    		for(ItemStack s : this.getAmmoType().getBullet(this.getCurrentAmmoVariant(stack))) {
    			items.add(TGItems.newStack(s,this.getAmmoLeft(stack)));
    		}
    	} else {
    		if (!this.isFullyLoaded(stack)) {
    			int amount = this.ammoType.getEmptyMag().length;
    			
    			for(int i=0;i<amount;i++) {
			    	int bulletsBack = (int) Math.floor(ammo/this.ammoType.getShotsPerBullet(clipsize, ammo));
					if (bulletsBack>0){
						items.add(TGItems.newStack(this.getAmmoType().getBullet(this.getCurrentAmmoVariant(stack))[i],bulletsBack));
					}
			    	if(!this.ammoType.getEmptyMag()[i].isEmpty()) {
			    		items.add(TGItems.newStack(this.ammoType.getEmptyMag()[i], 1));
			    	}
    			}
    		} else {
    			int amount = this.ammoType.getEmptyMag().length;	
    			for(int i=0;i<amount;i++) {
    				items.add(TGItems.newStack(this.ammoType.getAmmo(this.getCurrentAmmoVariant(stack))[i], 1));
    			}
    		}
    	}
    	return items;
    }
    
    /**
	 * try to force reload the gun, might lose some ammo
	 */
	public void tryForcedReload(ItemStack item, World world,EntityPlayer player, EnumHand hand){
		TGExtendedPlayer extendedPlayer = TGExtendedPlayer.get(player);
		
		if (extendedPlayer.getFireDelay(hand)<=0 && !this.isFullyLoaded(item)){
			
			int oldAmmo = this.getCurrentAmmo(item);
			
			//look for ammo
			if (InventoryUtil.consumeAmmoPlayer(player,this.getReloadItem(item))) {
			
				//empty gun and do reload if we can't put in ammo individual
				if (ammoCount <= 1){
					this.useAmmo(item, oldAmmo);
				}
				
				int ammos = this.getAmmoType().getEmptyMag().length;
				for (int i=0;i<ammos;i++) {
					if (!this.ammoType.getEmptyMag()[i].isEmpty()){
						
						int amount=InventoryUtil.addAmmoToPlayerInventory(player, TGItems.newStack(this.ammoType.getEmptyMag()[i], 1));
						if(amount >0 && !world.isRemote){
							player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY, player.posZ, TGItems.newStack(this.ammoType.getEmptyMag()[i], amount)));
						}
						
						int bulletsBack = (int) Math.floor(oldAmmo/this.ammoType.getShotsPerBullet(clipsize, oldAmmo));
						if (bulletsBack>0){
							int amount2=InventoryUtil.addAmmoToPlayerInventory(player, TGItems.newStack(this.ammoType.getBullet(this.getCurrentAmmoVariant(item))[i], bulletsBack));
							if(amount2 >0 && !world.isRemote){
								player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY, player.posZ, TGItems.newStack(this.ammoType.getBullet(this.getCurrentAmmoVariant(item))[i], amount2)));
							}
						}
						
					}
				}
				
				//stop toggle zooming when reloading
				if (world.isRemote) {
					if (canZoom  && this.toggleZoom) {
						ClientProxy cp = ClientProxy.get();
		    			if (cp.player_zoom != 1.0f) {
		    				cp.player_zoom= 1.0f;
		    			}
		    		}
				}
				
				extendedPlayer.setFireDelay(hand, this.reloadtime-this.minFiretime);
    			//System.out.println(Thread.currentThread().toString()+": reloadtime:"+reloadtime);
						    			
    			if (ammoCount >1) {
    				int i =1;
    				while (i<(ammoCount-oldAmmo) && InventoryUtil.consumeAmmoPlayer(player,this.ammoType.getAmmo(this.getCurrentAmmoVariant(item)))){
    					i++;
    				}
    				
    				//item.setItemDamage(ammoCount-i);
    				this.reloadAmmo(item, i);
    			} else {
    				this.reloadAmmo(item);
    			}
    			SoundUtil.playReloadSoundOnEntity(world,player,reloadsound, 1.0F, 1.0F, false, true, TGSoundCategory.RELOAD);

				if (world.isRemote) {

					int time = (int) (((float)reloadtime/20.0f)*1000);
					
					ShooterValues.setReloadtime(player, hand==EnumHand.OFF_HAND, System.currentTimeMillis()+time, time, (byte)0);
					
					client_startReload();
				} else{
					//send reloadpacket
					//send pakets to clients
					
			    	int msg_reloadtime = ((int)(((float)reloadtime/20.0f)*1000.0f));
			    	TGPackets.network.sendToAllAround(new ReloadStartedMessage(player,hand, msg_reloadtime,0), new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 100.0f));
			    	//
				}

			} else {

				//TODO: "can't reload" sound
				/*if (!world.isRemote)
		        {
    				world.playSoundAtEntity(player, "mob.villager.idle", 1.0F, 1.0F );
		        }*/
			}

			
		}
		
	}

	public int getClipsize() {
		return clipsize;
	}
	
	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return !(this.handType == GunHandType.TWO_HANDED);
	}

	public boolean canClickBlock(World world, EntityPlayer player, EnumHand hand) {
		 //ItemStack itemstack = player.getHeldItem(hand);
	     RayTraceResult raytraceresult = this.rayTrace(world, player, false);
	    
	     if (raytraceresult==null || raytraceresult.typeOfHit!=RayTraceResult.Type.BLOCK) {
	    	 return false;
	     }
	    
	     return true;
	}
	
	public boolean setGunStat(EnumGunStat stat, float value) {
		switch(stat) {
		case DAMAGE:
			this.damage=value;
			return true;
		case DAMAGE_MIN:
			this.damageMin=value;
			return true;
		case DAMAGE_DROP_START:
			this.damageDropStart=value;
			return true;
		case DAMAGE_DROP_END:
			this.damageDropEnd=value;
			return true;
		case BULLET_SPEED:
			this.speed=value;
			return true;
		case BULLET_DISTANCE:
			this.ticksToLive= (int)value;
			return true;
		case GRAVITY:
			this.gravity=value;
			return true;
		case SPREAD:
			this.spread=value;
			return true;
		default:
			return false;
		}
	}

	public float getSpread() {
		return spread;
	}

	public GunHandType getHandType() {
		return handType;
	}

	public float getZoombonus() {
		return zoombonus;
	}

	public EnumCrosshairStyle getCrossHairStyle() {
		return this.crossHairStyle;
	}
	
	public GenericGun setCrossHair(EnumCrosshairStyle crosshair) {
		this.crossHairStyle = crosshair;
		return this;
	}
}
