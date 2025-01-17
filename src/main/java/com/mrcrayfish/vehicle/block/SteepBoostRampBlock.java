package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.tileentity.BoostTileEntity;
import com.mrcrayfish.vehicle.util.Bounds;
import com.mrcrayfish.vehicle.util.RenderUtil;
import com.mrcrayfish.vehicle.util.StateHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.block.AbstractBlock;

/**
 * Author: MrCrayfish
 */
public class SteepBoostRampBlock extends RotatedObjectBlock
{
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");

    private static final AxisAlignedBB COLLISION_BASE = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.0625, 1.0);
    private static final AxisAlignedBB[] COLLISION_ONE = new Bounds(1, 1, 0, 16, 2, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_TWO = new Bounds(2, 2, 0, 16, 3, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_THREE = new Bounds(3, 3, 0, 16, 4, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_FOUR = new Bounds(4, 4, 0, 16, 5, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_FIVE = new Bounds(5, 5, 0, 16, 6, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_SIX = new Bounds(6, 6, 0, 16, 7, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_SEVEN = new Bounds(7, 7, 0, 16, 8, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_EIGHT = new Bounds(8, 9, 0, 16, 9, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_NINE = new Bounds(9, 10, 0, 16, 10, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_TEN = new Bounds(10, 11, 0, 16, 11, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_ELEVEN = new Bounds(11, 12, 0, 16, 12, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_TWELVE = new Bounds(12, 13, 0, 16, 13, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_THIRTEEN = new Bounds(13, 14, 0, 16, 14, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_FOURTEEN = new Bounds(14, 15, 0, 16, 15, 16).getRotatedBounds();
    private static final AxisAlignedBB[] COLLISION_FIFTEEN = new Bounds(15, 15, 0, 16, 16, 16).getRotatedBounds();

    public SteepBoostRampBlock()
    {
        super(AbstractBlock.Properties.of(Material.STONE).strength(1.0F));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader reader, List<ITextComponent> list, ITooltipFlag advanced)
    {
        if(Screen.hasShiftDown())
        {
            list.addAll(RenderUtil.lines(new TranslationTextComponent(this.getDescriptionId() + ".info"), 150));
        }
        else
        {
            list.add(new TranslationTextComponent("vehicle.info_help").withStyle(TextFormatting.YELLOW));
        }
    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity)
    {
        if(entity instanceof PoweredVehicleEntity && entity.getControllingPassenger() != null)
        {
            Direction facing = state.getValue(DIRECTION);
            if(facing == entity.getDirection())
            {
                float speedMultiplier = 0.0F;
                TileEntity tileEntity = world.getBlockEntity(pos);
                if(tileEntity instanceof BoostTileEntity)
                {
                    speedMultiplier = ((BoostTileEntity) tileEntity).getSpeedMultiplier();
                }

                PoweredVehicleEntity poweredVehicle = (PoweredVehicleEntity) entity;
                if(!poweredVehicle.isBoosting())
                {
                    world.playSound(null, pos, ModSounds.BOOST_PAD.get(), SoundCategory.BLOCKS, 2.0F, 0.5F);
                }
                poweredVehicle.setBoosting(true);
                poweredVehicle.setLaunching(3);
                poweredVehicle.currentSpeed = poweredVehicle.getActualMaxSpeed();
                poweredVehicle.speedMultiplier = speedMultiplier;
                Vector3d motion = poweredVehicle.getDeltaMovement();
                poweredVehicle.setDeltaMovement(new Vector3d(motion.x, poweredVehicle.currentSpeed / 20F + 0.1, motion.z));
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, IWorld world, BlockPos pos, BlockPos neighbourPos)
    {
        return this.getRampState(state, world, pos, state.getValue(DIRECTION));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return this.getRampState(this.defaultBlockState(), context.getLevel(), context.getClickedPos(), context.getHorizontalDirection());
    }

    private BlockState getRampState(BlockState state, IWorld world, BlockPos pos, Direction facing)
    {
        state = state.setValue(LEFT, false);
        state = state.setValue(RIGHT, false);
        if(StateHelper.getBlock(world, pos, facing, StateHelper.RelativeDirection.LEFT) == this)
        {
            if(StateHelper.getRotation(world, pos, facing, StateHelper.RelativeDirection.LEFT) == StateHelper.RelativeDirection.DOWN)
            {
                state = state.setValue(RIGHT, true);
            }
        }
        if(StateHelper.getBlock(world, pos, facing, StateHelper.RelativeDirection.RIGHT) == this)
        {
            if(StateHelper.getRotation(world, pos, facing, StateHelper.RelativeDirection.RIGHT) == StateHelper.RelativeDirection.DOWN)
            {
                state = state.setValue(LEFT, true);
            }
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LEFT);
        builder.add(RIGHT);
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new BoostTileEntity(1.0F);
    }
}
