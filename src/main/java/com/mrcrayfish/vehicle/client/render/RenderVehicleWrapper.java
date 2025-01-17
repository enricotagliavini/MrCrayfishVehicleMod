package com.mrcrayfish.vehicle.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.vehicle.client.EntityRayTracer;
import com.mrcrayfish.vehicle.client.model.SpecialModels;
import com.mrcrayfish.vehicle.common.entity.PartPosition;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleProperties;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class RenderVehicleWrapper<T extends VehicleEntity & EntityRayTracer.IEntityRayTraceable, R extends AbstractRenderVehicle<T>>
{
    protected final R renderVehicle;

    public RenderVehicleWrapper(R renderVehicle)
    {
        this.renderVehicle = renderVehicle;
    }

    public R getRenderVehicle()
    {
        return renderVehicle;
    }

    public void render(T entity, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, float partialTicks, int light)
    {
        if(!entity.isAlive())
            return;

        matrixStack.pushPose();

        VehicleProperties properties = entity.getProperties();
        PartPosition bodyPosition = properties.getBodyPosition();
        matrixStack.mulPose(Vector3f.XP.rotationDegrees((float) bodyPosition.getRotX()));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) bodyPosition.getRotY()));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) bodyPosition.getRotZ()));

        if(entity.canTowTrailer())
        {
            matrixStack.pushPose();
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(180F));
            Vector3d towBarOffset = properties.getTowBarPosition();
            matrixStack.translate(towBarOffset.x * 0.0625, towBarOffset.y * 0.0625 + 0.5, -towBarOffset.z * 0.0625);
            RenderUtil.renderColoredModel(SpecialModels.TOW_BAR.getModel(), ItemCameraTransforms.TransformType.NONE, false, matrixStack, renderTypeBuffer, -1, light, OverlayTexture.NO_OVERLAY);
            matrixStack.popPose();
        }

        matrixStack.translate(bodyPosition.getX(), bodyPosition.getY(), bodyPosition.getZ());
        matrixStack.scale((float) bodyPosition.getScale(), (float) bodyPosition.getScale(), (float) bodyPosition.getScale());
        matrixStack.translate(0.0, 0.5, 0.0);
        matrixStack.translate(0.0, properties.getAxleOffset() * 0.0625, 0.0);
        matrixStack.translate(0.0, properties.getWheelOffset() * 0.0625, 0.0);
        this.renderVehicle.render(entity, matrixStack, renderTypeBuffer, partialTicks, light);

        matrixStack.popPose();
    }

    /**
     *
     * @param entity
     * @param partialTicks
     */
    public void applyPreRotations(T entity, MatrixStack stack, float partialTicks) {}

    /**
     * Renders a part (ItemStack) on the vehicle using the specified PartPosition. The rendering
     * will be cancelled if the PartPosition parameter is null.
     *
     * @param position the render definitions to construct to the part
     * @param model the part to render onto the vehicle
     */
    protected void renderPart(@Nullable PartPosition position, IBakedModel model, MatrixStack matrixStack, IRenderTypeBuffer buffer, int color, int lightTexture, int overlayTexture)
    {
        if(position == null)
            return;

        matrixStack.pushPose();
        matrixStack.translate(position.getX() * 0.0625, position.getY() * 0.0625, position.getZ() * 0.0625);
        matrixStack.translate(0.0, -0.5, 0.0);
        matrixStack.scale((float) position.getScale(), (float) position.getScale(), (float) position.getScale());
        matrixStack.translate(0.0, 0.5, 0.0);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees((float) position.getRotX()));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY()));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) position.getRotZ()));
        RenderUtil.renderColoredModel(model, ItemCameraTransforms.TransformType.NONE, false, matrixStack, buffer, color, lightTexture, overlayTexture);
        matrixStack.popPose();
    }

    protected void renderKey(@Nullable PartPosition position, ItemStack stack, IBakedModel model, MatrixStack matrixStack, IRenderTypeBuffer buffer, int color, int lightTexture, int overlayTexture)
    {
        if(position == null)
            return;

        matrixStack.pushPose();
        matrixStack.translate(position.getX() * 0.0625, position.getY() * 0.0625, position.getZ() * 0.0625);
        matrixStack.translate(0.0, -0.25, 0.0);
        matrixStack.scale((float) position.getScale(), (float) position.getScale(), (float) position.getScale());
        matrixStack.mulPose(Vector3f.XP.rotationDegrees((float) position.getRotX()));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) position.getRotY()));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) position.getRotZ()));
        matrixStack.translate(0.0, 0.0, -0.05);
        RenderUtil.renderModel(stack, ItemCameraTransforms.TransformType.NONE, false, matrixStack, buffer, lightTexture, overlayTexture, model);

        matrixStack.popPose();
    }


    /**
     * Renders the engine (ItemStack) on the vehicle using the specified PartPosition. It adds a
     * subtle shake to the render to simulate it being powered.
     *
     * @param position the render definitions to construct to the part
     */
    protected void renderEngine(PoweredVehicleEntity entity, @Nullable PartPosition position, IBakedModel model, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light)
    {
        matrixStack.pushPose();
        if(entity.isEnginePowered() && entity.getControllingPassenger() != null)
        {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(0.5F * (entity.tickCount % 2)));
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(0.5F * (entity.tickCount % 2)));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-0.5F * (entity.tickCount % 2)));
        }
        this.renderPart(position, model, matrixStack, buffer, -1, light, OverlayTexture.NO_OVERLAY);
        matrixStack.popPose();
    }
}
