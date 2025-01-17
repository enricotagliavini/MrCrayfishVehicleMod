package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.vehicle.client.EntityRayTracer;
import com.mrcrayfish.vehicle.client.model.SpecialModels;
import com.mrcrayfish.vehicle.client.render.AbstractRenderVehicle;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.entity.vehicle.TractorEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class RenderTractor extends AbstractRenderVehicle<TractorEntity>
{
    @Override
    public void render(TractorEntity entity, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(entity, SpecialModels.TRACTOR.getModel(), matrixStack, renderTypeBuffer, light);

        //Render the handles bars
        matrixStack.pushPose();
        matrixStack.translate(0, 0.66, -0.475);
        matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(-67.5F));
        matrixStack.translate(0, -0.02, 0);
        matrixStack.scale(0.9F, 0.9F, 0.9F);
        float wheelAngle = entity.prevRenderWheelAngle + (entity.renderWheelAngle - entity.prevRenderWheelAngle) * partialTicks;
        float wheelAngleNormal = wheelAngle / 45F;
        float turnRotation = wheelAngleNormal * 25F;
        matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees(turnRotation));
        this.renderDamagedPart(entity, SpecialModels.GO_KART_STEERING_WHEEL.getModel(), matrixStack, renderTypeBuffer, light);
        matrixStack.popPose();
    }

    @Override
    public void applyPlayerModel(TractorEntity entity, PlayerEntity player, PlayerModel model, float partialTicks)
    {
        model.rightLeg.xRot = (float) Math.toRadians(-75F);
        model.rightLeg.yRot = (float) Math.toRadians(20F);
        model.leftLeg.xRot = (float) Math.toRadians(-75F);
        model.leftLeg.yRot = (float) Math.toRadians(-20F);

        float wheelAngle = entity.prevRenderWheelAngle + (entity.renderWheelAngle - entity.prevRenderWheelAngle) * partialTicks;
        float wheelAngleNormal = wheelAngle / 45F;
        float turnRotation = wheelAngleNormal * 6F;

        model.rightArm.xRot = (float) Math.toRadians(-55F - turnRotation);
        model.rightArm.yRot = (float) Math.toRadians(-10F);
        model.leftArm.xRot = (float) Math.toRadians(-55F + turnRotation);
        model.leftArm.yRot = (float) Math.toRadians(10F);
    }

    @Nullable
    @Override
    public EntityRayTracer.IRayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) ->
        {
            EntityRayTracer.createTransformListForPart(SpecialModels.TRACTOR, parts, transforms);
            EntityRayTracer.createTransformListForPart(SpecialModels.GO_KART_STEERING_WHEEL, parts, transforms,
                    EntityRayTracer.MatrixTransformation.createTranslation(0.0F, 0.66F, -0.475F),
                    EntityRayTracer.MatrixTransformation.createRotation(Axis.POSITIVE_X, -67.5F),
                    EntityRayTracer.MatrixTransformation.createTranslation(0.0F, -0.02F, 0.0F),
                    EntityRayTracer.MatrixTransformation.createScale(0.9F));
            EntityRayTracer.createFuelPartTransforms(ModEntities.TRACTOR.get(), SpecialModels.FUEL_DOOR_CLOSED, parts, transforms);
            EntityRayTracer.createKeyPortTransforms(ModEntities.TRACTOR.get(), parts, transforms);
        };
    }
}
