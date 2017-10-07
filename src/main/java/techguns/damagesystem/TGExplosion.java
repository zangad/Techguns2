package techguns.damagesystem;

import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.entities.projectiles.GenericProjectile;

public class TGExplosion {
	
	 /** whether or not the explosion sets fire to blocks around it */
    //private final boolean causesFire;
    /** whether or not this explosion spawns smoke particles */
    boolean damagesTerrain;
    Random random;
    World world;
    double x;
    double y;
    double z;
    Entity exploder;
    Entity projectile;
    /** A list of ChunkPositions of blocks affected by this explosion */
    List<BlockPos> affectedBlockPositions;
    
    float[][][] dmgVolume;
    
    /** Maps players to the knockback vector applied by the explosion, to send to the client */
    //Map<EntityPlayer, Vec3d> playerKnockbackMap;
    Vec3d position;

    double primaryRadius;
    double primaryDamage;
    double secondaryRadius;
    double secondaryDamage;
    double blockDamageFactor;
    float blockDropChance = 0.5f;
    
    Explosion explosionDummy;
    
     
//    @SideOnly(Side.CLIENT)
//    public TGExplosion(World worldIn, Entity entityIn, double x, double y, double z, float size, List<BlockPos> affectedPositions)
//    {
//        this(worldIn, entityIn, x, y, z, size, false, true, affectedPositions);
//    }
//
//    @SideOnly(Side.CLIENT)
//    public TGExplosion(World worldIn, Entity entityIn, double x, double y, double z, float size, boolean causesFire, boolean damagesTerrain, List<BlockPos> affectedPositions)
//    {
//        this(worldIn, entityIn, x, y, z, size, causesFire, damagesTerrain);
//        this.affectedBlockPositions.addAll(affectedPositions);
//    }

    public TGExplosion(World world, Entity exploder, Entity projectile, double x, double y, double z, double primaryDamage, double secondaryDamage, double primaryRadius, double secondaryRadius, double blockDamageFactor)
    {
        this.random = new Random();
        this.affectedBlockPositions = Lists.<BlockPos>newArrayList();
        //this.playerKnockbackMap = Maps.<EntityPlayer, Vec3d>newHashMap();
        this.world = world;
        this.exploder = exploder;
        this.projectile = projectile;
        
        this.x = x;
        this.y = y;
        this.z = z;

        this.primaryDamage = primaryDamage;
        this.secondaryDamage = secondaryDamage;
        this.primaryRadius = primaryRadius;
        this.secondaryRadius = secondaryRadius;

        this.blockDamageFactor = blockDamageFactor;
        this.damagesTerrain = (blockDamageFactor > 0.01);
        
        this.position = new Vec3d(this.x, this.y, this.z);
        
        //TODO: CRASH WHEN YOU EXPLODE TNT
        this.explosionDummy = new Explosion(world, exploder, x, y, z, (float)Math.max(primaryRadius, secondaryRadius), false, this.damagesTerrain);
    }

    /**
     * Does the first part of the explosion (destroy blocks)
     */
    public void doExplosion(boolean playSound)
    {
    	//TODO: Move Sound to different
    	if (playSound) this.world.playSound((EntityPlayer)null, this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
    	
        Set<BlockPos> set = Sets.<BlockPos>newHashSet();
        double totalRadius = Math.max(primaryRadius, secondaryRadius);
        int radius = (int) Math.ceil(totalRadius);
        
        double stepOffset = 0.30000001192092896D;
        int steps = (int)Math.ceil((double)radius/stepOffset);

        //System.out.println(String.format("Radius = %d, VolumeSize = %d", radius, s));
        
        for (int j = -radius; j < radius; ++j)
        {
            for (int k = -radius; k < radius; ++k)
            {
                for (int l = -radius; l < radius; ++l)
                {
                    if (j == -radius || j == radius-1 || k == -radius || k == radius-1 || l == -radius || l == radius-1)
                    {
                        double dx = (double)((float)j / (float)radius);
                        double dy = (double)((float)k / (float)radius);
                        double dz = (double)((float)l / (float)radius);
                        //normalize
                        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        dx = dx / length;
                        dy = dy / length;
                        dz = dz / length;
                        //float f = this.size * (0.7F + this.world.rand.nextFloat() * 0.6F);
                        double px = 0; //this.x;
                        double py = 0; //this.y;
                        double pz = 0; //this.z;
                        
                        float dmgFactor = 1.0f;
                        
                        //for (float f = (float) totalRadius; f > 0.0F; f -= stepOffset) //0.22500001F)
                        
                        double explosionDamping = 0.0;
                        double explosionPower = this.primaryDamage;
                        
                        BlockPos prevPos = null;
                        
                        for (int i = 0; i < steps && explosionPower > 0.0; i++)
                        {
                            BlockPos blockpos = new BlockPos(x +px, y +py, z +pz);
                            IBlockState iblockstate = this.world.getBlockState(blockpos);
                            
                            double distance = this.position.distanceTo(new Vec3d(blockpos.getX()+0.5,blockpos.getY()+0.5,blockpos.getZ()+0.5));
                            if (distance <= primaryRadius) explosionPower = primaryDamage;
                            else if (distance <= secondaryRadius) explosionPower = secondaryDamage + ((distance-primaryRadius)/(secondaryRadius-primaryRadius)) * (primaryDamage-secondaryDamage);
                            else explosionPower = 0.0;
                            
                            float resistance = 0.0f;
                            if (iblockstate.getMaterial() != Material.AIR)
                            {
                            	resistance = iblockstate.getBlock().getExplosionResistance(world, blockpos, exploder, explosionDummy);
//                                float f2 = this.exploder != null ? this.exploder.getExplosionResistance(this, this.world, blockpos, iblockstate) : iblockstate.getBlock().getExplosionResistance(world, blockpos, (Entity)null, this);
//                                f -= (f2 + 0.3F) * 0.3F;
                            	
                            	if (explosionPower-explosionDamping > 0.0f && resistance < (explosionPower-explosionDamping)*blockDamageFactor && (this.exploder == null || this.exploder.canExplosionDestroyBlock(explosionDummy, this.world, blockpos, iblockstate, (float)explosionPower)))
                                {
                                    set.add(blockpos);
                                    if (prevPos == null || !(prevPos.getX() == blockpos.getX() && prevPos.getY() == blockpos.getY() && prevPos.getZ() == blockpos.getZ())) {
                                    	explosionDamping += resistance;
                                    }   
                                }else {
                                	explosionPower = 0.0;
                                }
                            }
                            
                            px += dx * stepOffset;
                            py += dy * stepOffset;
                            pz += dz * stepOffset;
                            
                            prevPos = blockpos;
                        }
                    }
                }
            }
        }
        
        //<debug>
        //System.out.println(String.format("min = (%d, %d, %d);  max = (%d, %d, %d)",min[0], min[1], min[2], max[0], max[1], max[2]));        
        //</debug>

        this.affectedBlockPositions.addAll(set);
        float f3 = (float) (totalRadius);
        int k1 = MathHelper.floor(this.x - (double)f3 - 1.0D);
        int l1 = MathHelper.floor(this.x + (double)f3 + 1.0D);
        int i2 = MathHelper.floor(this.y - (double)f3 - 1.0D);
        int i1 = MathHelper.floor(this.y + (double)f3 + 1.0D);
        int j2 = MathHelper.floor(this.z - (double)f3 - 1.0D);
        int j1 = MathHelper.floor(this.z + (double)f3 + 1.0D);
        List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this.projectile, new AxisAlignedBB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, explosionDummy, list, f3);
        Vec3d vec3d = new Vec3d(this.x, this.y, this.z);
        
        breakBlocks();

    	TGDamageSource tgs = TGDamageSource.causeExplosionDamage(projectile,exploder, DeathType.GORE);
        for (int k2 = 0; k2 < list.size(); ++k2)
        {
            Entity entity = list.get(k2);

            //System.out.println("Check entity:"+entity);
            
            if (!entity.isImmuneToExplosions() && GenericProjectile.BULLET_TARGETS.apply(entity))
            {
            
	            double damage;     
	            
	            //Check distance
	            double distance = this.position.distanceTo(new Vec3d(entity.posX, entity.posY+entity.getEyeHeight(), entity.posZ));
	            if (distance <= primaryRadius) damage = primaryDamage;
	            else if (distance <= secondaryRadius) damage = secondaryDamage + ((distance-primaryRadius)/(secondaryRadius-primaryRadius)) * (primaryDamage-secondaryDamage);
	            else damage = 0.0;
	            
            	//System.out.println("Distance: "+ distance);
            	//System.out.println("Damage: "+ damage);
            	
	            //trace blocks
	            if (damage > 0.0) {
	            	Vec3d start = this.position;
	            	Vec3d end = new Vec3d(entity.posX, entity.posY+entity.getEyeHeight()*0.5, entity.posZ);
	            
	            	RayTraceResult rtr = world.rayTraceBlocks(start, end);
	            	if (rtr != null && rtr.typeOfHit == Type.BLOCK) damage = 0.0;
	            }
	
	            
	            if (damage > 0.0) {
	            	//System.out.println("Attack Damage: "+ damage +" against "+entity);
	            	entity.attackEntityFrom(tgs,  (float)Math.max(0, damage));        	         	
	            }

            }
        }
    }
    
    private void breakBlocks() {
    	if (this.damagesTerrain)
        {
            for (BlockPos blockpos : this.affectedBlockPositions)
            {
                IBlockState iblockstate = this.world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();


                if (iblockstate.getMaterial() != Material.AIR)
                {
                    if (block.canDropFromExplosion(explosionDummy))
                    {
                        block.dropBlockAsItemWithChance(this.world, blockpos, this.world.getBlockState(blockpos), blockDropChance, 0);
                    }

                    block.onBlockExploded(this.world, blockpos, this.explosionDummy);
                }
            }
        }

    }

}
