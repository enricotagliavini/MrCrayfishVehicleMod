package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrcrayfish.vehicle.client.EntityRayTracer;
import com.mrcrayfish.vehicle.client.RayTraceFunction;
import com.mrcrayfish.vehicle.client.model.SpecialModels;
import com.mrcrayfish.vehicle.client.render.AbstractRenderVehicle;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.common.ItemLookup;
import com.mrcrayfish.vehicle.common.Seat;
import com.mrcrayfish.vehicle.entity.VehicleProperties;
import com.mrcrayfish.vehicle.entity.vehicle.MiniBikeEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class RenderMiniBike extends AbstractRenderVehicle<MiniBikeEntity>
{
    @Override
    public void render(MiniBikeEntity entity, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(entity, SpecialModels.MINI_BIKE_BODY.getModel(), matrixStack, renderTypeBuffer, light);

        //Render the handles bars
        matrixStack.pushPose();

        matrixStack.translate(0.0, 0.0, 10.5 * 0.0625);
        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-22.5F));

        float wheelScale = 1.65F;
        float wheelAngle = entity.prevRenderWheelAngle + (entity.renderWheelAngle - entity.prevRenderWheelAngle) * partialTicks;
        float wheelAngleNormal = wheelAngle / 45F;
        float turnRotation = wheelAngleNormal * 25F;

        matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees(turnRotation));
        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(22.5F));
        matrixStack.translate(0.0, 0.0, -10.5 * 0.0625);

        this.renderDamagedPart(entity, SpecialModels.MINI_BIKE_HANDLES.getModel(), matrixStack, renderTypeBuffer, light);

        if(entity.hasWheels())
        {
            matrixStack.pushPose();
            matrixStack.translate(0, -0.5 + 1.7 * 0.0625, 13 * 0.0625);
            float frontWheelSpin = entity.prevFrontWheelRotation + (entity.frontWheelRotation - entity.prevFrontWheelRotation) * partialTicks;
            if(entity.isMoving())
            {
                matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-frontWheelSpin));
            }
            matrixStack.scale(wheelScale, wheelScale, wheelScale);
            matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees(180F));
            RenderUtil.renderColoredModel(RenderUtil.getModel(ItemLookup.getWheel(entity)), ItemCameraTransforms.TransformType.NONE, false, matrixStack, renderTypeBuffer, -1, light, OverlayTexture.NO_OVERLAY);
            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    @Override
    public void applyPlayerModel(MiniBikeEntity entity, PlayerEntity player, PlayerModel model, float partialTicks)
    {
        float wheelAngle = entity.prevRenderWheelAngle + (entity.renderWheelAngle - entity.prevRenderWheelAngle) * partialTicks;
        float wheelAngleNormal = wheelAngle / 45F;
        float turnRotation = wheelAngleNormal * 8F;
        model.rightArm.xRot = (float) Math.toRadians(-55F - turnRotation);
        model.leftArm.xRot = (float) Math.toRadians(-55F + turnRotation);
        //model.bipedRightArm.offsetZ = -0.1F * wheelAngleNormal;
        //model.bipedLeftArm.offsetZ = 0.1F * wheelAngleNormal;
        model.rightLeg.xRot = (float) Math.toRadians(-65F);
        model.rightLeg.yRot = (float) Math.toRadians(30F);
        model.leftLeg.xRot = (float) Math.toRadians(-65F);
        model.leftLeg.yRot = (float) Math.toRadians(-30F);
    }

    @Override
    public void applyPlayerRender(MiniBikeEntity entity, PlayerEntity player, float partialTicks, MatrixStack matrixStack, IVertexBuilder builder)
    {
        int index = entity.getSeatTracker().getSeatIndex(player.getUUID());
        if(index != -1)
        {
            VehicleProperties properties = entity.getProperties();
            Seat seat = properties.getSeats().get(index);
            Vector3d seatVec = seat.getPosition().add(0, properties.getAxleOffset() + properties.getWheelOffset(), 0).scale(properties.getBodyPosition().getScale()).scale(0.0625);
            double scale = 32.0 / 30.0;
            double offsetX = seatVec.x * scale;
            double offsetY = (seatVec.y + player.getMyRidingOffset()) * scale + 24 * 0.0625; //Player is 2 blocks high tall but renders at 1.8 blocks tall
            double offsetZ = -seatVec.z * scale;
            matrixStack.translate(offsetX, offsetY, offsetZ);
            float currentSpeedNormal = (entity.prevCurrentSpeed + (entity.currentSpeed - entity.prevCurrentSpeed) * partialTicks) / entity.getMaxSpeed();
            float turnAngleNormal = (entity.prevTurnAngle + (entity.turnAngle - entity.prevTurnAngle) * partialTicks) / 45F;
            matrixStack.mulPose(Axis.POSITIVE_Z.rotationDegrees(turnAngleNormal * currentSpeedNormal * 20F));
            matrixStack.translate(-offsetX, -offsetY, -offsetZ);
        }
    }

    @Nullable
    @Override
    public EntityRayTracer.IRayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) ->
        {
            EntityRayTracer.createTransformListForPart(SpecialModels.MINI_BIKE_BODY, parts, transforms);
            EntityRayTracer.createTransformListForPart(SpecialModels.MINI_BIKE_HANDLES, parts, transforms);
            EntityRayTracer.createPartTransforms(ModItems.WOOD_SMALL_ENGINE.get(), VehicleProperties.getProperties(ModEntities.MINI_BIKE.get()).getEnginePosition(), parts, transforms, RayTraceFunction.FUNCTION_FUELING);
        };
    }
}
