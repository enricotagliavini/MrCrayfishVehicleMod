package com.mrcrayfish.vehicle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.client.render.RenderVehicleWrapper;
import com.mrcrayfish.vehicle.client.render.VehicleRenderRegistry;
import com.mrcrayfish.vehicle.common.entity.PartPosition;
import com.mrcrayfish.vehicle.entity.EngineType;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleProperties;
import com.mrcrayfish.vehicle.inventory.container.EditVehicleContainer;
import com.mrcrayfish.vehicle.util.CommonUtils;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Collections;

/**
 * Author: MrCrayfish
 */
public class EditVehicleScreen extends ContainerScreen<EditVehicleContainer>
{
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("vehicle:textures/gui/edit_vehicle.png");

    private final PlayerInventory playerInventory;
    private final IInventory vehicleInventory;
    private final PoweredVehicleEntity vehicle;

    private boolean showHelp = true;
    private int windowZoom = 10;
    private int windowX, windowY;
    private float windowRotationX, windowRotationY;
    private boolean mouseGrabbed;
    private int mouseGrabbedButton;
    private int mouseClickedX, mouseClickedY;

    public EditVehicleScreen(EditVehicleContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.vehicleInventory = container.getVehicleInventory();
        this.vehicle = container.getVehicle();
        this.imageHeight = 184;
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(GUI_TEXTURES);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, left, top, 0, 0, this.imageWidth, this.imageHeight);

        if(this.vehicle.getEngineType() != EngineType.NONE)
        {
            if(this.vehicleInventory.getItem(0).isEmpty())
            {
                this.blit(matrixStack, left + 8, top + 17, 176, 0, 16, 16);
            }
        }
        else if(this.vehicleInventory.getItem(0).isEmpty())
        {
            this.blit(matrixStack, left + 8, top + 17, 176, 32, 16, 16);
        }

        if(this.vehicle.canChangeWheels())
        {
            if(this.vehicleInventory.getItem(1).isEmpty())
            {
                this.blit(matrixStack, left + 8, top + 35, 176, 16, 16, 16);
            }
        }
        else if(this.vehicleInventory.getItem(1).isEmpty())
        {
            this.blit(matrixStack, left + 8, top + 35, 176, 32, 16, 16);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.font.draw(matrixStack, this.title.getString(), 8, 6, 4210752);
        minecraft.font.draw(matrixStack, this.playerInventory.getDisplayName().getString(), 8, this.imageHeight - 96 + 2, 4210752);

        RenderVehicleWrapper wrapper = VehicleRenderRegistry.getRenderWrapper((EntityType<? extends VehicleEntity>) vehicle.getType());
        if(wrapper != null)
        {
            int startX = (this.width - this.imageWidth) / 2;
            int startY = (this.height - this.imageHeight) / 2;

            RenderSystem.pushMatrix();
            RenderSystem.translatef(96, 78, 1050.0F);
            RenderSystem.scalef(-1.0F, -1.0F, -1.0F);

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            RenderUtil.scissor(startX + 26, startY + 17, 142, 70);

            matrixStack.pushPose();
            matrixStack.translate(0.0, 0.0, 1000.0);
            matrixStack.translate(windowX - (mouseGrabbed && mouseGrabbedButton == 0 ? mouseX - mouseClickedX : 0), 0, 0);
            matrixStack.translate(0, windowY - (mouseGrabbed && mouseGrabbedButton == 0 ? mouseY - mouseClickedY : 0), 0);

            Quaternion quaternion = Axis.POSITIVE_X.rotationDegrees(-10F);
            quaternion.mul(Axis.POSITIVE_X.rotationDegrees(windowRotationY - (mouseGrabbed && mouseGrabbedButton == 1 ? mouseY - mouseClickedY : 0)));
            quaternion.mul(Axis.POSITIVE_Y.rotationDegrees(windowRotationX + (mouseGrabbed && mouseGrabbedButton == 1 ? mouseX - mouseClickedX : 0)));
            quaternion.mul(Axis.POSITIVE_Y.rotationDegrees(135F));
            matrixStack.mulPose(quaternion);

            matrixStack.scale(windowZoom / 10F, windowZoom / 10F, windowZoom / 10F);
            matrixStack.scale(22F, 22F, 22F);

            VehicleProperties properties = vehicle.getProperties();
            PartPosition position = PartPosition.DEFAULT;
            if(properties != null)
            {
                position = properties.getDisplayPosition();
            }
            matrixStack.scale((float) position.getScale(), (float) position.getScale(), (float) position.getScale());
            matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees((float) position.getRotX()));
            matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees((float) position.getRotY()));
            matrixStack.mulPose(Axis.POSITIVE_Z.rotationDegrees((float) position.getRotZ()));
            matrixStack.translate(position.getX(), position.getY(), position.getZ());

            EntityRendererManager renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
            renderManager.setRenderShadow(false);
            renderManager.overrideCameraOrientation(quaternion);
            IRenderTypeBuffer.Impl renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
            wrapper.render(vehicle, matrixStack, renderTypeBuffer, Minecraft.getInstance().getFrameTime(), 15728880);
            renderTypeBuffer.endBatch();
            renderManager.setRenderShadow(true);

            matrixStack.popPose();

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            RenderSystem.popMatrix();
        }

        if(showHelp)
        {
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.5F, 0.5F, 0.5F);
            minecraft.font.draw(matrixStack, I18n.get("container.edit_vehicle.window_help"), 56, 38, 0xFFFFFF);
            RenderSystem.popMatrix();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;
        if(CommonUtils.isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 142, 70))
        {
            if(scroll < 0 && this.windowZoom > 0)
            {
                this.showHelp = false;
                this.windowZoom--;
            }
            else if(scroll > 0)
            {
                this.showHelp = false;
                this.windowZoom++;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(CommonUtils.isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 142, 70))
        {
            if(!this.mouseGrabbed && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
            {
                this.mouseGrabbed = true;
                this.mouseGrabbedButton = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? 1 : 0;
                this.mouseClickedX = (int) mouseX;
                this.mouseClickedY = (int) mouseY;
                this.showHelp = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if(this.mouseGrabbed)
        {
            if(this.mouseGrabbedButton == 0 && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                this.mouseGrabbed = false;
                this.windowX -= (mouseX - this.mouseClickedX);
                this.windowY -= (mouseY - this.mouseClickedY);
            }
            else if(mouseGrabbedButton == 1 && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                this.mouseGrabbed = false;
                this.windowRotationX += (mouseX - this.mouseClickedX);
                this.windowRotationY -= (mouseY - this.mouseClickedY);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);

        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(this.vehicleInventory.getItem(0).isEmpty())
        {
            if(CommonUtils.isMouseWithin(mouseX, mouseY, startX + 7, startY + 16, 18, 18))
            {
                if(vehicle.getEngineType() != EngineType.NONE)
                {
                    this.renderTooltip(matrixStack, Lists.transform(Collections.singletonList(new StringTextComponent("Engine")), ITextComponent::getVisualOrderText), mouseX, mouseY); //TODO localise
                }
                else
                {
                    this.renderTooltip(matrixStack, Lists.transform(Arrays.asList(new StringTextComponent("Engine"), new StringTextComponent(TextFormatting.GRAY + "Not applicable")), ITextComponent::getVisualOrderText), mouseX, mouseY); //TODO localise
                }
            }
        }

        if(this.vehicleInventory.getItem(1).isEmpty())
        {
            if(CommonUtils.isMouseWithin(mouseX, mouseY, startX + 7, startY + 34, 18, 18))
            {
                if(vehicle.canChangeWheels())
                {
                    this.renderTooltip(matrixStack, Lists.transform(Collections.singletonList(new StringTextComponent("Wheels")), ITextComponent::getVisualOrderText), mouseX, mouseY);
                }
                else
                {
                    this.renderTooltip(matrixStack, Lists.transform(Arrays.asList(new StringTextComponent("Wheels"), new StringTextComponent(TextFormatting.GRAY + "Not applicable")), ITextComponent::getVisualOrderText), mouseX, mouseY);
                }
            }
        }
    }
}
