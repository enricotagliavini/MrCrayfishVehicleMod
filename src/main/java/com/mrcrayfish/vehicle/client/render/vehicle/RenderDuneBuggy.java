package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.vehicle.client.EntityRayTracer;
import com.mrcrayfish.vehicle.client.model.SpecialModels;
import com.mrcrayfish.vehicle.client.render.AbstractRenderVehicle;
import com.mrcrayfish.vehicle.entity.vehicle.DuneBuggyEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class RenderDuneBuggy extends AbstractRenderVehicle<DuneBuggyEntity>
{
    @Override
    public void render(DuneBuggyEntity entity, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(entity, SpecialModels.DUNE_BUGGY_BODY.getModel(), matrixStack, renderTypeBuffer, light);

        float wheelAngle = entity.prevRenderWheelAngle + (entity.renderWheelAngle - entity.prevRenderWheelAngle) * partialTicks;
        double wheelScale = 1.0F;

        //Render the handles bars
        matrixStack.pushPose();

        matrixStack.translate(0.0, 0.0, 3.125 * 0.0625);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-22.5F));
        float wheelAngleNormal = wheelAngle / 45F;
        float turnRotation = wheelAngleNormal * 15F;
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(turnRotation));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(22.5F));
        matrixStack.translate(0.0, 0.0, -0.2);

        this.renderDamagedPart(entity, SpecialModels.DUNE_BUGGY_HANDLES.getModel(), matrixStack, renderTypeBuffer, light);

        if(entity.hasWheels())
        {
            matrixStack.pushPose();
            matrixStack.translate(0.0, -0.355, 0.33);
            float frontWheelSpin = entity.prevFrontWheelRotation + (entity.frontWheelRotation - entity.prevFrontWheelRotation) * partialTicks;
            if(entity.isMoving())
            {
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(-frontWheelSpin));
            }
            matrixStack.scale((float) wheelScale, (float) wheelScale, (float) wheelScale);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(180F));
            IBakedModel wheelModel = RenderUtil.getWheelModel(entity);
            RenderUtil.renderColoredModel(wheelModel, ItemCameraTransforms.TransformType.NONE, false, matrixStack, renderTypeBuffer, entity.getWheelColor(), light, OverlayTexture.NO_OVERLAY);
            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    @Override
    public void applyPlayerModel(DuneBuggyEntity entity, PlayerEntity player, PlayerModel model, float partialTicks)
    {
        float wheelAngle = entity.prevRenderWheelAngle + (entity.renderWheelAngle - entity.prevRenderWheelAngle) * partialTicks;
        float wheelAngleNormal = wheelAngle / 45F;
        float turnRotation = wheelAngleNormal * 8F;
        model.rightArm.xRot = (float) Math.toRadians(-50F - turnRotation);
        model.leftArm.xRot = (float) Math.toRadians(-50F + turnRotation);
        model.rightLeg.xRot = (float) Math.toRadians(-65F);
        model.rightLeg.yRot = (float) Math.toRadians(30F);
        model.leftLeg.xRot = (float) Math.toRadians(-65F);
        model.leftLeg.yRot = (float) Math.toRadians(-30F);
    }

    @Nullable
    @Override
    public EntityRayTracer.IRayTraceTransforms getRayTraceTransforms()
    {
        return (entityRayTracer, transforms, parts) ->
        {
            EntityRayTracer.createTransformListForPart(SpecialModels.DUNE_BUGGY_BODY, parts, transforms);
            EntityRayTracer.createTransformListForPart(SpecialModels.DUNE_BUGGY_HANDLES, parts, transforms,
                    EntityRayTracer.MatrixTransformation.createTranslation(0.0F, 0.0F, -0.0046875F));
            EntityRayTracer.createFuelPartTransforms(ModEntities.DUNE_BUGGY.get(), SpecialModels.FUEL_DOOR_CLOSED, parts, transforms);
        };
    }
}
