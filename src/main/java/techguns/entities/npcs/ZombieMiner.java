package techguns.entities.npcs;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import techguns.TGArmors;
import techguns.TGItems;
import techguns.TGuns;
import techguns.Techguns;
import techguns.items.armors.GenericArmorMultiCamo;
import techguns.items.tools.TGSword;

public class ZombieMiner extends GenericNPCUndead {
	public static final ResourceLocation LOOT = new ResourceLocation(Techguns.MODID, "entities/zombieminer");
	
	public ZombieMiner(World world) {
		super(world);
		setTGArmorStats(0f, 0f);
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0D);
	}

	@Override
	protected void addRandomArmor(int difficulty) {

		// Armors
		double chance = 0.5;
		
		int camo = GenericArmorMultiCamo.getRandomCamoIndexFor((GenericArmorMultiCamo) TGArmors.t1_miner_Chestplate);
		
		this.setItemStackToSlot(EntityEquipmentSlot.HEAD, GenericArmorMultiCamo.getNewWithCamo(
				TGArmors.t1_miner_Helmet,camo));
		if(Math.random() <= chance)
			this.setItemStackToSlot(EntityEquipmentSlot.CHEST, GenericArmorMultiCamo.getNewWithCamo(
					TGArmors.t1_miner_Chestplate,camo));
		if (Math.random() <= chance)
			this.setItemStackToSlot(EntityEquipmentSlot.LEGS, GenericArmorMultiCamo.getNewWithCamo(
					TGArmors.t1_miner_Leggings,camo));
		if (Math.random() <= chance)
			this.setItemStackToSlot(EntityEquipmentSlot.FEET, GenericArmorMultiCamo.getNewWithCamo(
					TGArmors.t1_miner_Boots,camo));
		

		// Weapons
		Random r = new Random();
		Item weapon = null;
		switch (r.nextInt(7)) {
		case 0:
			weapon = Items.STONE_PICKAXE;
			break;
		case 1:
			weapon = Items.IRON_PICKAXE;
			break;
		case 2:
			weapon = TGuns.revolver;
			break;
		case 3:
			weapon = TGuns.sawedoff;
			break;
		case 4: 
			weapon = Items.IRON_HOE;
			break;
		case 5:
			weapon = TGuns.handcannon;
			break;
		case 6:
			weapon = Items.STONE_SHOVEL;
			break;
		default:
			weapon = TGuns.handcannon;
			break;
		}
		if (weapon != null) this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(weapon));
	}
	
	@Override
	public SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
	}

	@Override
	public SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_ZOMBIE_HURT;
	}

	@Override
	public SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_ZOMBIE_DEATH;
	}

	public SoundEvent getStepSound() {
		return SoundEvents.ENTITY_ZOMBIE_STEP;
	}
	
	@Override
    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }
	
	@Override
	protected ResourceLocation getLootTable() {
		return LOOT;
	}

}
