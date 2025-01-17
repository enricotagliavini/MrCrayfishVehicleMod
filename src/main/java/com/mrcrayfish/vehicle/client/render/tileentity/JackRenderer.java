package com.mrcrayfish.vehicle.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrcrayfish.vehicle.client.model.SpecialModels;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.client.render.RenderVehicleWrapper;
import com.mrcrayfish.vehicle.client.render.VehicleRenderRegistry;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.tileentity.JackTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

/**
 * Author: MrCrayfish
 */
public class JackRenderer extends TileEntityRenderer<JackTileEntity>
{
    public JackRenderer(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public void render(JackTileEntity jack, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light, int i1)
    {
        if(!jack.hasLevel())
            return;

        matrixStack.pushPose();

        BlockPos pos = jack.getBlockPos();
        BlockState state = jack.getLevel().getBlockState(pos);

        matrixStack.pushPose();
        {
            matrixStack.translate(0.5, 0.0, 0.5);
            matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees(180F));
            matrixStack.translate(-0.5, 0.0, -0.5);
            BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            IBakedModel model = dispatcher.getBlockModel(state);
            IVertexBuilder builder = renderTypeBuffer.getBuffer(RenderType.cutout());
            dispatcher.getModelRenderer().tesselateBlock(jack.getLevel(), model, state, pos, matrixStack, builder, true, new Random(), state.getSeed(pos), OverlayTexture.NO_OVERLAY);
        }
        matrixStack.popPose();

        matrixStack.pushPose();
        {
            matrixStack.translate(0, -2 * 0.0625, 0);
            float progress = (jack.prevLiftProgress + (jack.liftProgress - jack.prevLiftProgress) * partialTicks) / (float) JackTileEntity.MAX_LIFT_PROGRESS;
            matrixStack.translate(0, 0.5 * progress, 0);

            //Render the head
            BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            IBakedModel model = SpecialModels.JACK_PISTON_HEAD.getModel();
            IVertexBuilder builder = renderTypeBuffer.getBuffer(RenderType.cutout());
            dispatcher.getModelRenderer().tesselateBlock(jack.getLevel(), model, state, pos, matrixStack, builder, false, new Random(), state.getSeed(pos), OverlayTexture.NO_OVERLAY);
        }
        matrixStack.popPose();

        matrixStack.pushPose();
        {
            Entity jackEntity = jack.getJack();
            if(jackEntity != null && jackEntity.getPassengers().size() > 0)
            {
                Entity passenger = jackEntity.getPassengers().get(0);
                if(passenger instanceof VehicleEntity && passenger.isAlive())
                {
                    matrixStack.translate(0.5, 0.5, 0.5);
                    matrixStack.translate(0, -1 * 0.0625, 0);
                    float progress = (jack.prevLiftProgress + (jack.liftProgress - jack.prevLiftProgress) * partialTicks) / (float) JackTileEntity.MAX_LIFT_PROGRESS;
                    matrixStack.translate(0, 0.5 * progress, 0);

                    VehicleEntity vehicle = (VehicleEntity) passenger;
                    Vector3d heldOffset = vehicle.getProperties().getHeldOffset().yRot(passenger.yRot * 0.017453292F);
                    matrixStack.translate(-heldOffset.z * 0.0625, -heldOffset.y * 0.0625, -heldOffset.x * 0.0625);
                    matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees(-passenger.yRot));

                    RenderVehicleWrapper wrapper = VehicleRenderRegistry.getRenderWrapper((EntityType<? extends VehicleEntity>) vehicle.getType());
                    if(wrapper != null)
                    {
                        wrapper.render(vehicle, matrixStack, renderTypeBuffer, partialTicks, light);
                    }
                }
            }
        }
        matrixStack.popPose();

        matrixStack.popPose();
    }
}
