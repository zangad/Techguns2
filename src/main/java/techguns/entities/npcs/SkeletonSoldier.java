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

public class SkeletonSoldier extends GenericNPCUndead {

	public static final ResourceLocation LOOT = new ResourceLocation(Techguns.MODID, "entities/skeletonsoldier");
	
	public SkeletonSoldier(World world) {
		super(world);
		this.height=1.95f;
		setTGArmorStats(0f, 0f);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(25);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0D);
	}

	@Override
	public float getWeaponPosX() {
		return -0.06f;
	}

	@Override
	public float getWeaponPosY() {
		return -0.06f;
	}

	@Override
	protected void addRandomArmor(int difficulty) {

		// Armors
		
		double chance = 0.5;
		if (Math.random() <= chance) {
			this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(TGArmors.t1_scout_Helmet));
		} else {
			this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(TGArmors.t1_combat_Helmet));
		};
		if (Math.random() <= chance) {
			this.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(TGArmors.t1_scout_Boots));
		} else {
			this.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(TGArmors.t1_combat_Boots));
		};
		
		// Weapons
		Random r = new Random();
		Item weapon = null;
		Item shield = null;
		switch (r.nextInt(8)) {
		case 0:
			weapon = TGuns.handcannon;
			break;
		case 1:
			weapon = TGuns.thompson;
			break;
		case 2:
			weapon = TGuns.revolver;
			break;
		case 3:
			weapon = TGuns.sawedoff;
			break;
		case 4:
			weapon = TGItems.COMBAT_KNIFE;
			break;
		case 5:
			weapon = TGItems.CROWBAR;
			break;
		case 6:
			weapon = TGuns.pistol;
			break;
		case 7:
			weapon = TGuns.pistol;
			shield = TGArmors.riot_shield;
			break;
		default:
			weapon = Items.STONE_SHOVEL;
			break;
		}
		if (weapon != null) this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(weapon));
		if (shield != null) this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(shield));
	}
	
	@Override
	public SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_SKELETON_AMBIENT;
	}

	@Override
	public SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_SKELETON_HURT;
	}

	@Override
	public SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_SKELETON_DEATH;
	}

	public SoundEvent getStepSound() {
		return SoundEvents.ENTITY_SKELETON_STEP;
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
