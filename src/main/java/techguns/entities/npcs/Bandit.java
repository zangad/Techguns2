package techguns.entities.npcs;

import java.util.Random;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import techguns.TGArmors;
import techguns.TGItems;
import techguns.TGuns;
import techguns.Techguns;

public class Bandit extends GenericNPC {

	public static final ResourceLocation LOOT = new ResourceLocation(Techguns.MODID, "entities/Bandit");
	
	public Bandit(World world) {
		super(world);
		setTGArmorStats(5.0f, 0f);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
	}

	@Override
	protected void addRandomArmor(int difficulty) {

		// Armors
		
		this.setItemStackToSlot(EntityEquipmentSlot.CHEST,new ItemStack(TGArmors.t1_scout_Chestplate));
		this.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(TGArmors.t1_scout_Leggings));
		this.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(TGArmors.t1_scout_Boots));
		
		double chance = 0.5;
		if (Math.random() <= chance) {
			this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(TGArmors.t1_scout_Helmet));
		}			 

		// Weapons
		Random r = new Random();
		Item weapon = null;
		switch (r.nextInt(11)) {
		case 0:
			weapon = TGuns.pistol;
			break;
		case 1:
			weapon = TGuns.ak47;
			break;
		case 2:
			weapon = TGuns.sawedoff;
			break;
		case 3:
			weapon = TGuns.thompson;
			break;
		case 4:
			weapon = TGuns.revolver;
			break;
		case 5:
		case 6:
			weapon = TGuns.handcannon;
			break;
		case 7:
			weapon = TGuns.boltaction;
			break;
		case 8:
		case 9:
			weapon = TGItems.COMBAT_KNIFE;
			break;
		case 10:
			weapon = TGItems.CROWBAR;
			break;
		default:
			weapon = TGuns.boltaction;
			break;
		}
		if (weapon != null) this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(weapon));
	}
	
	@Override
	protected ResourceLocation getLootTable() {
		return LOOT;
	}
}