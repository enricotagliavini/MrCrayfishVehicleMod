package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.tileentity.FluidPipeTileEntity;
import com.mrcrayfish.vehicle.tileentity.FluidPumpTileEntity;
import com.mrcrayfish.vehicle.util.VoxelShapeHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class FluidPumpBlock extends FluidPipeBlock
{
    //TODO add collisions
    private final VoxelShape[][] PUMP_BOX = new VoxelShape[][]{
            {Block.box(3, 0, 3, 13, 3, 13), Block.box(4.5, 3, 4.5, 11.5, 4, 11.5)},
            {Block.box(3, 16, 3, 13, 13, 13), Block.box(4.5, 13, 4.5, 11.5, 12, 11.5)},
            {Block.box(3, 3, 0, 13, 13, 3), Block.box(4.5, 4.5, 3, 11.5, 11.5, 4)},
            {Block.box(3, 3, 16, 13, 13, 13), Block.box(4.5, 4.5, 13, 11.5, 11.5, 12)},
            {Block.box(0, 3, 3, 3, 13, 13), Block.box(3, 4.5, 4.5, 4, 11.5, 11.5)},
            {Block.box(16, 3, 3, 13, 13, 13), Block.box(13, 4.5, 4.5, 12, 11.5, 11.5)}
    };

    @Override
    protected VoxelShape getPipeShape(BlockState state, IBlockReader worldIn, BlockPos pos)
    {
        List<VoxelShape> shapes = new ArrayList<>();
        shapes.add(super.getPipeShape(state, worldIn, pos));
        Collections.addAll(shapes, PUMP_BOX[this.getCollisionFacing(state).get3DDataValue()]);
        return VoxelShapeHelper.combineAll(shapes);
    }

    @Override
    protected Direction getCollisionFacing(BlockState state)
    {
        return state.getValue(DIRECTION).getOpposite();
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result)
    {
        if(super.use(state, world, pos, player, hand, result) == ActionResultType.SUCCESS)
        {
            return ActionResultType.SUCCESS;
        }
        FluidPipeTileEntity pipe = getPipeTileEntity(world, pos);
        AxisAlignedBB housingBox = this.getHousingBox(pos, state, player, hand, result.getLocation().add(-pos.getX(), -pos.getY(), -pos.getZ()), pipe);
        if(pipe != null && housingBox != null)
        {
            if(!world.isClientSide)
            {
                ((FluidPumpTileEntity) pipe).cyclePowerMode(player);
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LEVER_CLICK, SoundCategory.BLOCKS, 1.0F, 0.5F);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.SUCCESS;
    }

    @Nullable
    public AxisAlignedBB getHousingBox(BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Vector3d hitVec, @Nullable FluidPipeTileEntity pipe)
    {
        if(!(pipe instanceof FluidPumpTileEntity) || player.getItemInHand(hand).getItem() != ModItems.WRENCH.get())
        {
            return null;
        }

        VoxelShape[] boxesHousing = this.PUMP_BOX[getCollisionFacing(state).get3DDataValue()];
        for(VoxelShape box : boxesHousing)
        {
            AxisAlignedBB boundingBox = box.bounds();
            if(boundingBox.inflate(0.001).contains(hitVec))
            {
                for(VoxelShape box2 : boxesHousing)
                {
                    boundingBox = boundingBox.minmax(box2.bounds());
                }
                return boundingBox.move(pos);
            }
        }
        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, IWorld world, BlockPos pos, BlockPos neighbourPos)
    {
        return this.getPumpState(world, pos, state, state.getValue(DIRECTION));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        BlockState state = super.getStateForPlacement(context).setValue(DIRECTION, context.getClickedFace());
        return this.getPumpState(context.getLevel(), context.getClickedPos(), state, context.getClickedFace());
    }

    private BlockState getPumpState(IWorld world, BlockPos pos, BlockState state, Direction originalFacing)
    {
        FluidPipeTileEntity pipe = getPipeTileEntity(world, pos);
        boolean[] disabledConnections = FluidPipeTileEntity.getDisabledConnections(pipe);
        for(Direction facing : Direction.values())
        {
            if(facing == originalFacing.getOpposite()) continue;

            state = state.setValue(CONNECTED_PIPES[facing.get3DDataValue()], false);

            BlockPos adjacentPos = pos.relative(facing);
            BlockState adjacentState = world.getBlockState(adjacentPos);
            boolean enabled = !disabledConnections[facing.get3DDataValue()];
            if(adjacentState.getBlock() == ModBlocks.FLUID_PIPE.get())
            {
                state = state.setValue(CONNECTED_PIPES[facing.get3DDataValue()], enabled);
            }
            else if(adjacentState.getBlock() == Blocks.LEVER)
            {
                Direction leverFacing = adjacentState.getValue(LeverBlock.FACING).getOpposite();
                if(adjacentPos.relative(leverFacing).equals(pos))
                {
                    state = state.setValue(CONNECTED_PIPES[facing.get3DDataValue()], true);
                    if(pipe != null)
                    {
                        pipe.setConnectionDisabled(facing, false);
                    }
                }
            }
            else if(adjacentState.getBlock() != this)
            {
                TileEntity tileEntity = world.getBlockEntity(adjacentPos);
                state = state.setValue(CONNECTED_PIPES[facing.get3DDataValue()], enabled && tileEntity != null && tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()).isPresent());
            }
        }
        return state;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new FluidPumpTileEntity();
    }
}
