package techguns.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import techguns.util.BlockUtils;

public class BlockTGCamoNet extends GenericBlockMetaEnum<EnumCamoNetType> {

	public static PropertyEnum<EnumConnectionType> CONNECTION = PropertyEnum.create("connection", EnumConnectionType.class);
	
	public BlockTGCamoNet(String name) {
		super(name, Material.CLOTH, EnumCamoNetType.class);
		this.blockStateOverride = new BlockStateContainer.Builder(this).add(TYPE).add(CONNECTION).build();
		this.setDefaultState(this.getBlockState().getBaseState());
		this.setSoundType(SoundType.CLOTH);
	}

	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	
	/*protected static final AxisAlignedBB[] bounding_boxes = {
	        new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, height, 1.0f),
	        new AxisAlignedBB(0.0F, 0.0F, 1-width, 1.0F, height, 1.0f),
	        new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, height, width),
	        new AxisAlignedBB(0.0F, 0.0F, 1-width2, 1.0F, height, width2),
	        new AxisAlignedBB(1-width, 0.0F, 0.0F, 1.0F, height, 1.0f),
	        new AxisAlignedBB(1-width, 0.0F, 1-width, 1.0F, height, 1.0f),
	        new AxisAlignedBB(1-width, 0.0F, 0, 1.0F, height, width),
	        new AxisAlignedBB(1-width, 0.0F, 1-width2, 1.0F, height, width2),
	        new AxisAlignedBB(0.0F, 0.0F, 0.0F, width, height, 1.0f),
	        
	        new AxisAlignedBB(0.0F, 0.0F, 1-width, width, height, 1.0f),
	        new AxisAlignedBB(0.0F, 0.0F, 0.0F, width, height, width),
	        new AxisAlignedBB(0.0F, 0.0F, 1-width2, width, height, width2),
	        new AxisAlignedBB(1-width2, 0.0F, 0.0F, width2, height, 1.0f),
	        new AxisAlignedBB(1-width2, 0.0F, 1-width, width2, height, 1.0f),
	        new AxisAlignedBB(1-width2, 0.0F, 0, width2, height, width),
	        
	        new AxisAlignedBB(0.25F, 0.0F, 0.25F, 0.75F, height, 0.75f)
	};
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		boolean north = canConnectTo(worldIn, pos, EnumFacing.NORTH);
    	boolean east = canConnectTo(worldIn, pos, EnumFacing.EAST);
    	boolean south = canConnectTo(worldIn, pos, EnumFacing.SOUTH);
    	boolean west = canConnectTo(worldIn, pos, EnumFacing.WEST);

	        if (north && south && east && west) return bounding_boxes[0];
	        else if (north && south && east && !west) return bounding_boxes[1];
	        else if (north && south && !east && west) return bounding_boxes[2];
	        else if (north && south && !east && !west) return bounding_boxes[3];
	        else if (north && !south && east && west) return bounding_boxes[4];
	        else if (north && !south && east && !west) return bounding_boxes[5];
	        else if (north && !south && !east && west) return bounding_boxes[6];
	        else if (north && !south && !east && !west) return bounding_boxes[7];
	        else if (!north && south && east && west) return bounding_boxes[8];
	        
	        else if (!north && south && east && !west) return bounding_boxes[9];
	        else if (!north && south && !east && west) return bounding_boxes[10];
	        else if (!north && south && !east && !west) return bounding_boxes[11];
	        else if (!north && !south && east && west) return bounding_boxes[12];
	        else if (!north && !south && east && !west) return bounding_boxes[13];
	        else if (!north && !south && !east && west) return bounding_boxes[14];
	         
	        else return bounding_boxes[15];
	}*/

	
	
	
	/**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
    	boolean n = canConnectTo(worldIn, pos, EnumFacing.NORTH);
    	boolean e = canConnectTo(worldIn, pos, EnumFacing.EAST);
    	boolean s = canConnectTo(worldIn, pos, EnumFacing.SOUTH);
    	boolean w = canConnectTo(worldIn, pos, EnumFacing.WEST);
    	
        return state.withProperty(CONNECTION, EnumConnectionType.get(n, e, s, w));
    }
    
    protected static final AxisAlignedBB COLLIDE_CENTER = new AxisAlignedBB(7/16d, 0, 7/16d, 9/16d, 1d, 9/16d);
    protected static final AxisAlignedBB COLLIDE_SOUTH = new AxisAlignedBB(7/16d,0,9/16d,9/16d,1d,1d);
    protected static final AxisAlignedBB COLLIDE_NORTH = new AxisAlignedBB(7/16d,0,0,9/16d,1d,7/16d);
    
    protected static final AxisAlignedBB COLLIDE_EAST = new AxisAlignedBB(9/16d,0,7/16d,1d,1d,9/16d);
    protected static final AxisAlignedBB COLLIDE_WEST = new AxisAlignedBB(0,0,7/16d,7/16d,1d,9/16d);
    
    @Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return COLLIDE_CENTER;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
			List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185477_7_) {

    	super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
    	
    	boolean n = canConnectTo(worldIn, pos, EnumFacing.NORTH);
    	boolean e = canConnectTo(worldIn, pos, EnumFacing.EAST);
    	boolean s = canConnectTo(worldIn, pos, EnumFacing.SOUTH);
    	boolean w = canConnectTo(worldIn, pos, EnumFacing.WEST);
    	
    	if(n) addCollisionBoxToList(pos, entityBox, collidingBoxes, COLLIDE_NORTH);
    	if(e) addCollisionBoxToList(pos, entityBox, collidingBoxes, COLLIDE_EAST);
    	if(s) addCollisionBoxToList(pos, entityBox, collidingBoxes, COLLIDE_SOUTH);
    	if(w) addCollisionBoxToList(pos, entityBox, collidingBoxes, COLLIDE_WEST);
    	
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {

		float f = 0.4375F;
		float f1 = 0.5625F;
		float f2 = 0.4375F;
		float f3 = 0.5625F;
		boolean north = canConnectTo(worldIn, pos, EnumFacing.NORTH);
		boolean east = canConnectTo(worldIn, pos, EnumFacing.EAST);
		boolean south = canConnectTo(worldIn, pos, EnumFacing.SOUTH);
		boolean west = canConnectTo(worldIn, pos, EnumFacing.WEST);

		if (!(north || east || south || west)) {
			return new AxisAlignedBB(f, 0.0F, f2, f1, 1.0F, f3);
		}

		if ((!west || !east) && (west || east || north || south)) {
			if (west && !east) {
				f = 0.0F;
			} else if (!west && east) {
				f1 = 1.0F;
			}
		} else {
			f = 0.0F;
			f1 = 1.0F;
		}

		if ((!north || !south) && (west || east || north || south)) {
			if (north && !south) {
				f2 = 0.0F;
			} else if (!north && south) {
				f3 = 1.0F;
			}
		} else {
			f2 = 0.0F;
			f3 = 1.0F;
		}
		return new AxisAlignedBB(f, 0.0F, f2, f1, 1.0F, f3);
	}

	private boolean canConnectTo(IBlockAccess world, BlockPos pos, EnumFacing facing)
    {
        BlockPos other = pos.offset(facing);
        Block block = world.getBlockState(other).getBlock();
        return block instanceof BlockTGCamoNet;
    }

	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public void registerItemBlockModels() {
		for(int i = 0; i< clazz.getEnumConstants().length;i++) {
			ModelLoader.setCustomModelResourceLocation(this.itemblock, i, new ModelResourceLocation(getRegistryName()+"_inventory","type="+clazz.getEnumConstants()[i].getName()));
		}
	}
    
	
}
