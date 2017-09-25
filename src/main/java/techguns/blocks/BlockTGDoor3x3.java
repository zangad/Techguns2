package techguns.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.registry.GameRegistry;
import techguns.TGItems;
import techguns.Techguns;
import techguns.items.ItemTGDoor3x3;
import techguns.tileentities.Door3x3TileEntity;

public class BlockTGDoor3x3<T extends Enum<T> & IStringSerializable> extends GenericBlock {

	public static final PropertyBool MASTER = PropertyBool.create("master");
	public static final PropertyBool ZPLANE = PropertyBool.create("zplane");
	public static final PropertyBool OPENED = PropertyBool.create("open");
	
	protected Class<T> clazz;
	protected BlockStateContainer blockStateOverride;
	
	protected ItemTGDoor3x3<T> placer;
	
	protected static final double size = 0.3125D;
	
	protected static final AxisAlignedBB BB_X = new AxisAlignedBB(size, 0, 0, 1-size, 1, 1);
	protected static final AxisAlignedBB BB_Z = new AxisAlignedBB(0, 0, size, 1, 1, 1-size);
	
	
	public BlockTGDoor3x3(String name,  Class<T> clazz, ItemTGDoor3x3<T> doorplacer) {
		super(name, Material.IRON);
		this.setSoundType(SoundType.METAL);
		this.clazz=clazz;
		this.blockStateOverride = new BlockStateContainer.Builder(this).add(MASTER).add(ZPLANE).add(OPENED).build();
		this.setDefaultState(this.getBlockState().getBaseState().withProperty(MASTER, false).withProperty(ZPLANE, false).withProperty(OPENED, false));
		this.placer=doorplacer;
		this.placer.setBlock(this);
		
		setHardness(0.25f);
	}
	
    @Override
	public void registerBlock(Register<Block> event) {
		super.registerBlock(event);
		GameRegistry.registerTileEntity(Door3x3TileEntity.class, Techguns.MODID+":"+"door3x3tileent");
	}

	public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
	
    @Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}

	public BlockFaceShape getBlockFaceShape(IBlockAccess p_193383_1_, IBlockState p_193383_2_, BlockPos p_193383_3_, EnumFacing p_193383_4_)
    {
        return BlockFaceShape.UNDEFINED;
    }
    
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	
	public boolean hasTileEntity(IBlockState state)
    {
       return state.getValue(MASTER);
    }
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		if(state.getValue(MASTER)) {
			return new Door3x3TileEntity();
		}
		return null;
	}

	protected static Vec3i[] pos_z = {
			new Vec3i(0, 1, 0),
			new Vec3i(0, -1, 0),
			new Vec3i(0, 0, -1),
			new Vec3i(0, 0, 1),
			new Vec3i(0, -1, -1),
			new Vec3i(0, -1, 1),
			new Vec3i(0, 1, -1),
			new Vec3i(0, 1, 1)
	};
	
	protected static Vec3i[] pos_x = {
			new Vec3i(0, 1, 0),
			new Vec3i(0, -1, 0),
			new Vec3i(-1, 0, 0),
			new Vec3i(1, 0, 0),
			new Vec3i(-1, -1, 0),
			new Vec3i(1, -1, 0),
			new Vec3i(-1, 1, 0),
			new Vec3i(1, 1, 0)
	};
	
	protected BlockPos findMaster(IBlockAccess w, BlockPos pos, IBlockState state) {
		if(state.getValue(MASTER)) {
			return pos;
		}
		Vec3i[] offsets = pos_x;
		if(state.getValue(ZPLANE)) {
			offsets=pos_z;
		}
		
		for(int i=0;i<offsets.length; i++) {
			BlockPos p = pos.add(offsets[i]);
			IBlockState s = w.getBlockState(p);
			if(s.getBlock()==this && s.getValue(MASTER)) {
				return p;
			}
		}
		return pos;
	}
	
	public static AxisAlignedBB NO_COLLIDE=new AxisAlignedBB(0, 0, 0, 0, 0, 0);
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if(!state.getValue(OPENED)) {
			return getBBforPlane(state);
		} else {
			BlockPos master = findMaster(source, pos, state);
			if(state.getValue(MASTER) || master.getY()>= pos.getY()) {
				return NO_COLLIDE;
			} else {
				return getBBforPlane(state);
			}
		}
	}
	
	protected AxisAlignedBB getBBforPlane(IBlockState state) {
		if(state.getValue(ZPLANE)) {
			return BB_X;
		} else {
			return BB_Z;
		}
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		if(blockState.getValue(OPENED)) {
			return NULL_AABB;
		} else {
			return getBBforPlane(blockState);
		}
	}

	  private int getCloseSound()
	    {
	        return this.blockMaterial == Material.IRON ? 1011 : 1012;
	    }

	    private int getOpenSound()
	    {
	        return this.blockMaterial == Material.IRON ? 1005 : 1006;
	    }
	
	public void toggleState(World w, BlockPos masterPos) {
		IBlockState masterstate = w.getBlockState(masterPos);
		boolean opened = !masterstate.getValue(OPENED);
		
		Vec3i[] offsets = pos_x;
		if(masterstate.getValue(ZPLANE)) {
			offsets=pos_z;
		}
		
		for(int i=0;i<offsets.length; i++) {
			BlockPos p = masterPos.add(offsets[i]);
			this.setOpenedStateForBlock(w, p, opened);
		}
		this.setOpenedStateForBlock(w, masterPos, opened);
		 w.playEvent((EntityPlayer)null, opened ? this.getOpenSound() : this.getCloseSound(), masterPos, 0);
		 
		 TileEntity tile = w.getTileEntity(masterPos);
		 if(tile!=null && tile instanceof Door3x3TileEntity) {
			 Door3x3TileEntity door = (Door3x3TileEntity) tile;
			 if(!w.isRemote) {
				 door.changeStateServerSide();
			 } else {
				 door.setLastStateChangeTime(System.currentTimeMillis());
			 }
		 } 
	}
	
	public void setOpenedStateForBlock(World w, BlockPos p, boolean b) {
		IBlockState state = w.getBlockState(p);
		if(state.getBlock()==this) {
			IBlockState newstate = state.withProperty(OPENED,b);
			w.setBlockState(p, newstate, 3);
		}
	}
	
	@Override
	public BlockStateContainer getBlockState() {
		return this.blockStateOverride;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(OPENED, (meta&2)>0).withProperty(ZPLANE, (meta&4)>0).withProperty(MASTER, (meta&8)>0);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int meta=0;
		if(state.getValue(MASTER)) {
			meta+=8;
		}
		if(state.getValue(ZPLANE)) {
			meta+=4;
		}
		if(state.getValue(OPENED)) {
			meta+=2;
		}
		return meta;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0; //TODO
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return this.placer;
	}

	public boolean canPlaceDoor(World worldIn, BlockPos pos, EnumFacing lookdirection) {
		int xmin = 0;
		int xmax = 1;
		int zmin = 0;
		int zmax = 1;
		
		if (lookdirection == EnumFacing.NORTH || lookdirection == EnumFacing.SOUTH) {
			xmin = -1;
			xmax = 2;
		} else {
			zmin = -1;
			zmax = 2;
		}
		
		for (int x =xmin; x<xmax;x++) for (int y=0;y<3;y++) for (int z=zmin; z<zmax; z++) {
			
			if (!this.canPlaceBlockAt(worldIn, pos.add(x, y, z))) {
				return false;
			}
			
		}
		return true;
	}

	public Class<T> getEnumClazz() {
		return clazz;
	}

	protected void breakSlave(World w, BlockPos p) {
		IBlockState state = w.getBlockState(p);
		if(state.getBlock() == this) {
			w.setBlockToAir(p);
		}
	}
	
	protected void checkBreakMaster(World w, BlockPos p) {
		IBlockState state = w.getBlockState(p);
		if(state.getBlock()==this && state.getValue(MASTER)) {
			w.setBlockToAir(p);
		}
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		IBlockState state = worldIn.getBlockState(pos);
		return state.getValue(OPENED);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		BlockPos master = this.findMaster(worldIn, pos, state);
		this.toggleState(worldIn, master);
		
		return true;
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (state.getValue(MASTER)) {
			breakSlave(worldIn,pos.up());
			breakSlave(worldIn,pos.down());
			
			if(state.getValue(ZPLANE)) {
				breakSlave(worldIn,pos.north());
				breakSlave(worldIn,pos.north().up());
				breakSlave(worldIn,pos.north().down());
				
				breakSlave(worldIn,pos.south());
				breakSlave(worldIn,pos.south().up());
				breakSlave(worldIn,pos.south().down());
			} else {
				breakSlave(worldIn,pos.east());
				breakSlave(worldIn,pos.east().up());
				breakSlave(worldIn,pos.east().down());
				
				breakSlave(worldIn,pos.west());
				breakSlave(worldIn,pos.west().up());
				breakSlave(worldIn,pos.west().down());
			}
		} else {
			checkBreakMaster(worldIn,pos.up());
			checkBreakMaster(worldIn,pos.down());
			
			if(state.getValue(ZPLANE)) {
				checkBreakMaster(worldIn,pos.north());
				checkBreakMaster(worldIn,pos.north().up());
				checkBreakMaster(worldIn,pos.north().down());
				
				checkBreakMaster(worldIn,pos.south());
				checkBreakMaster(worldIn,pos.south().up());
				checkBreakMaster(worldIn,pos.south().down());
			} else {
				checkBreakMaster(worldIn,pos.east());
				checkBreakMaster(worldIn,pos.east().up());
				checkBreakMaster(worldIn,pos.east().down());
				
				checkBreakMaster(worldIn,pos.west());
				checkBreakMaster(worldIn,pos.west().up());
				checkBreakMaster(worldIn,pos.west().down());
			}
		}
		

		super.breakBlock(worldIn, pos, state);
	}	
	
	
}
