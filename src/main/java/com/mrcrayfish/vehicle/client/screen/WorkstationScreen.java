package com.mrcrayfish.vehicle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.common.entity.PartPosition;
import com.mrcrayfish.vehicle.crafting.RecipeType;
import com.mrcrayfish.vehicle.crafting.VehicleRecipe;
import com.mrcrayfish.vehicle.crafting.VehicleRecipes;
import com.mrcrayfish.vehicle.entity.EngineType;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleProperties;
import com.mrcrayfish.vehicle.inventory.container.WorkstationContainer;
import com.mrcrayfish.vehicle.item.EngineItem;
import com.mrcrayfish.vehicle.item.WheelItem;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageCraftVehicle;
import com.mrcrayfish.vehicle.tileentity.WorkstationTileEntity;
import com.mrcrayfish.vehicle.util.CommonUtils;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class WorkstationScreen extends ContainerScreen<WorkstationContainer>
{
    private static final ResourceLocation GUI = new ResourceLocation("vehicle:textures/gui/workstation.png");

    private List<EntityType<?>> vehicleTypes;
    private List<MaterialItem> materials;
    private List<MaterialItem> filteredMaterials;
    private static int currentVehicle = 0;
    private static int prevCurrentVehicle = 0;
    private static boolean showRemaining = false;
    private VehicleEntity[] cachedVehicle;
    private PlayerInventory playerInventory;
    private WorkstationTileEntity workstation;
    private Button btnCraft;
    private CheckBox checkBoxMaterials;
    private boolean validEngine;
    private boolean transitioning;
    private int vehicleScale = 30;
    private int prevVehicleScale = 30;

    public WorkstationScreen(WorkstationContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.workstation = container.getTileEntity();
        this.imageWidth = 289;
        this.imageHeight = 202;
        this.materials = new ArrayList<>();
        this.vehicleTypes = this.getVehicleTypes(playerInventory.player.level);
        this.vehicleTypes.sort(Comparator.comparing(type -> type.getRegistryName().getPath()));
        this.cachedVehicle = new VehicleEntity[this.vehicleTypes.size()];
    }

    private List<EntityType<?>> getVehicleTypes(World world)
    {
        return world.getRecipeManager().getRecipes().stream().filter(recipe -> recipe.getType() == RecipeType.CRAFTING).map(recipe -> (VehicleRecipe) recipe).map(VehicleRecipe::getVehicle).collect(Collectors.toList());
    }

    @Override
    public void init()
    {
        super.init();
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        this.addButton(new Button(startX, startY, 15, 20, new StringTextComponent("<"), button -> {
            this.loadVehicle(Math.floorMod(currentVehicle - 1,  this.vehicleTypes.size()));
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }));

        this.addButton(new Button(startX + 161, startY, 15, 20, new StringTextComponent(">"), button -> {
            this.loadVehicle(Math.floorMod(currentVehicle + 1,  this.vehicleTypes.size()));
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }));

        this.btnCraft = this.addButton(new Button(startX + 186, startY + 6, 97, 20, new StringTextComponent("Craft"), button -> {
            ResourceLocation registryName = this.cachedVehicle[currentVehicle].getType().getRegistryName();
            Objects.requireNonNull(registryName, "Vehicle registry name must not be null!");
            PacketHandler.instance.sendToServer(new MessageCraftVehicle(registryName.toString(), this.workstation.getBlockPos()));
        }));

        this.btnCraft.active = false;
        this.checkBoxMaterials = this.addButton(new CheckBox(startX + 186, startY + 51, new StringTextComponent("Show Remaining")));
        this.checkBoxMaterials.setToggled(WorkstationScreen.showRemaining);
        this.loadVehicle(currentVehicle);
    }

    @Override
    public void tick()
    {
        super.tick();

        this.validEngine = true;

        for(MaterialItem material : this.materials)
        {
            material.update();
        }

        boolean canCraft = true;
        for(MaterialItem material : this.materials)
        {
            if(!material.isEnabled())
            {
                canCraft = false;
                break;
            }
        }

        if(this.cachedVehicle[currentVehicle] instanceof PoweredVehicleEntity)
        {
            PoweredVehicleEntity entityPoweredVehicle = (PoweredVehicleEntity) this.cachedVehicle[currentVehicle];
            if(entityPoweredVehicle.getEngineType() != EngineType.NONE)
            {
                ItemStack engine = this.workstation.getItem(1);
                if(!engine.isEmpty() && engine.getItem() instanceof EngineItem)
                {
                    EngineItem engineItem = (EngineItem) engine.getItem();
                    EngineType engineType = engineItem.getEngineType();
                    if(entityPoweredVehicle.getEngineType() != engineType)
                    {
                        canCraft = false;
                        this.validEngine = false;
                        entityPoweredVehicle.setEngine(false);
                    }
                    else
                    {
                        entityPoweredVehicle.setEngineTier(engineItem.getEngineTier());
                        entityPoweredVehicle.setEngine(true);
                        entityPoweredVehicle.onSyncedDataUpdated(PoweredVehicleEntity.ENGINE_TIER);
                    }
                }
                else
                {
                    canCraft = false;
                    this.validEngine = false;
                    entityPoweredVehicle.setEngine(false);
                }
            }

            if(entityPoweredVehicle.canChangeWheels())
            {
                ItemStack wheels = this.workstation.getItem(2);
                if(!wheels.isEmpty() && wheels.getItem() instanceof WheelItem)
                {
                    if(wheels.getTag() != null)
                    {
                        CompoundNBT tagCompound = wheels.getTag();
                        if(tagCompound.contains("Color", Constants.NBT.TAG_INT))
                        {
                            entityPoweredVehicle.setWheelColor(tagCompound.getInt("Color"));
                        }
                    }
                    WheelItem wheelItem = (WheelItem) wheels.getItem();
                    entityPoweredVehicle.setWheelType(wheelItem.getWheelType());
                    entityPoweredVehicle.setWheels(true);
                    entityPoweredVehicle.onSyncedDataUpdated(PoweredVehicleEntity.WHEEL_COLOR);
                }
                else
                {
                    entityPoweredVehicle.setWheels(false);
                    canCraft = false;
                }
            }
        }
        this.btnCraft.active = canCraft;

        this.prevVehicleScale = this.vehicleScale;
        if(this.transitioning)
        {
            if(this.vehicleScale > 0)
            {
                this.vehicleScale = Math.max(0, this.vehicleScale - 6);
            }
            else
            {
                this.transitioning = false;
            }
        }
        else if(this.vehicleScale < 30)
        {
            this.vehicleScale = Math.min(30, this.vehicleScale + 6);
        }

        if(this.cachedVehicle[currentVehicle].canBeColored())
        {
            if(!this.workstation.getItem(0).isEmpty())
            {
                ItemStack stack = this.workstation.getItem(0);
                if(stack.getItem() instanceof DyeItem)
                {
                    DyeItem dyeItem = (DyeItem) stack.getItem();
                    this.cachedVehicle[currentVehicle].setColor(dyeItem.getDyeColor().getColorValue());
                }
                else
                {
                    this.cachedVehicle[currentVehicle].setColor(VehicleEntity.DYE_TO_COLOR[0]);
                }
            }
            else
            {
                this.cachedVehicle[currentVehicle].setColor(VehicleEntity.DYE_TO_COLOR[0]);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        boolean result = super.mouseClicked(mouseX, mouseY, mouseButton);
        WorkstationScreen.showRemaining = this.checkBoxMaterials.isToggled();
        return result;
    }

    private void loadVehicle(int index)
    {
        prevCurrentVehicle = currentVehicle;

        if(this.cachedVehicle[index] == null)
        {
            VehicleEntity vehicle = (VehicleEntity) this.vehicleTypes.get(index).create(this.minecraft.level);
            java.util.List<EntityDataManager.DataEntry<?>> entryList = vehicle.getEntityData().getAll();
            if(entryList != null)
            {
                entryList.forEach(dataEntry -> vehicle.onSyncedDataUpdated(dataEntry.getAccessor()));
            }
            if(vehicle instanceof PoweredVehicleEntity)
            {
                ((PoweredVehicleEntity) vehicle).setEngine(false);
                ((PoweredVehicleEntity) vehicle).setWheels(false);
            }
            this.cachedVehicle[index] = vehicle;
        }

        this.materials.clear();
        VehicleRecipe recipe = VehicleRecipes.getRecipe(this.cachedVehicle[index].getType(), this.minecraft.level);
        if(recipe != null)
        {
            for(int i = 0; i < recipe.getMaterials().size(); i++)
            {
                MaterialItem item = new MaterialItem(recipe.getMaterials().get(i).copy());
                item.update();
                this.materials.add(item);
            }
        }

        currentVehicle = index;

        if(Config.CLIENT.workstationAnimation.get() && prevCurrentVehicle != currentVehicle)
        {
            this.transitioning = true;
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);

        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;
        for(int i = 0; i < filteredMaterials.size(); i++)
        {
            int itemX = startX + 186;
            int itemY = startY + i * 19 + 6 + 57;
            if(CommonUtils.isMouseWithin(mouseX, mouseY, itemX, itemY, 80, 19))
            {
                MaterialItem materialItem = this.filteredMaterials.get(i);
                if(!materialItem.getStack().isEmpty())
                {
                    this.renderTooltip(matrixStack, materialItem.getStack(), mouseX, mouseY);
                }
            }
        }

        VehicleEntity vehicle = this.cachedVehicle[currentVehicle];
        if(vehicle.canBeColored())
        {
            //TODO optional should have AQUA text formatting
            this.drawSlotTooltip(matrixStack, Lists.newArrayList(new TranslationTextComponent("vehicle.tooltip.optional"), new TranslationTextComponent("vehicle.tooltip.paint_color")), startX, startY, 186, 29, mouseX, mouseY, 0);
        }
        else
        {
            this.drawSlotTooltip(matrixStack, Lists.newArrayList(new TranslationTextComponent("vehicle.tooltip.paint_color"), new TranslationTextComponent("vehicle.tooltip.not_applicable")), startX, startY, 186, 29, mouseX, mouseY, 0);
        }

        if(vehicle instanceof PoweredVehicleEntity && ((PoweredVehicleEntity) vehicle).getEngineType() != EngineType.NONE)
        {
            String engineName = ((PoweredVehicleEntity) vehicle).getEngineType().getEngineName();
            this.drawSlotTooltip(matrixStack, Lists.newArrayList(new TranslationTextComponent("vehicle.tooltip.required"), new TranslationTextComponent(engineName)), startX, startY, 206, 29, mouseX, mouseY, 1);
        }
        else
        {
            this.drawSlotTooltip(matrixStack, Lists.newArrayList(new TranslationTextComponent("vehicle.tooltip.engine"), new TranslationTextComponent("vehicle.tooltip.not_applicable")), startX, startY, 206, 29, mouseX, mouseY, 1);
        }

        if(vehicle instanceof PoweredVehicleEntity && ((PoweredVehicleEntity) vehicle).canChangeWheels())
        {
            this.drawSlotTooltip(matrixStack, Lists.newArrayList(new TranslationTextComponent("vehicle.tooltip.required"), new TranslationTextComponent("vehicle.tooltip.wheels")), startX, startY, 226, 29, mouseX, mouseY, 2);
        }
        else
        {
            this.drawSlotTooltip(matrixStack, Lists.newArrayList(new TranslationTextComponent("vehicle.tooltip.wheels"), new TranslationTextComponent("vehicle.tooltip.not_applicable")), startX, startY, 226, 29, mouseX, mouseY, 2);
        }
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        /* Fixes partial ticks to use percentage from 0 to 1 */
        partialTicks = this.minecraft.getFrameTime();

        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        RenderSystem.enableBlend();

        this.minecraft.getTextureManager().bind(GUI);
        this.blit(matrixStack, startX, startY + 80, 0, 134, 176, 122);
        this.blit(matrixStack, startX + 180, startY, 176, 54, 6, 208);
        this.blit(matrixStack, startX + 186, startY, 182, 54, 57, 208);
        this.blit(matrixStack, startX + 186 + 57, startY, 220, 54, 23, 208);
        this.blit(matrixStack, startX + 186 + 57 + 23, startY, 220, 54, 3, 208);
        this.blit(matrixStack, startX + 186 + 57 + 23 + 3, startY, 236, 54, 20, 208);

        /* Slots */
        this.drawSlot(matrixStack, startX, startY, 186, 29, 80, 0, 0, false, this.cachedVehicle[currentVehicle].canBeColored());
        boolean needsEngine = this.cachedVehicle[currentVehicle] instanceof PoweredVehicleEntity && ((PoweredVehicleEntity) this.cachedVehicle[currentVehicle]).getEngineType() != EngineType.NONE;
        this.drawSlot(matrixStack, startX, startY, 206, 29, 80, 16, 1, !this.validEngine, needsEngine);
        boolean needsWheels = this.cachedVehicle[currentVehicle] instanceof PoweredVehicleEntity && ((PoweredVehicleEntity) this.cachedVehicle[currentVehicle]).canChangeWheels();
        this.drawSlot(matrixStack, startX, startY, 226, 29, 80, 32, 2, needsWheels && this.workstation.getItem(2).isEmpty(), needsWheels);

        this.drawCenteredString(matrixStack, this.font, this.cachedVehicle[currentVehicle].getName().getString(), startX + 88, startY + 6, Color.WHITE.getRGB());

        this.filteredMaterials = this.getMaterials();
        for(int i = 0; i < this.filteredMaterials.size(); i++)
        {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bind(GUI);

            MaterialItem materialItem = this.filteredMaterials.get(i);
            ItemStack stack = materialItem.stack;
            if(stack.isEmpty())
            {
                RenderHelper.turnOff();
                this.blit(matrixStack, startX + 186, startY + i * 19 + 6 + 57, 0, 19, 80, 19);
            }
            else
            {
                RenderHelper.turnOff();
                if(materialItem.isEnabled())
                {
                    this.blit(matrixStack, startX + 186, startY + i * 19 + 6 + 57, 0, 0, 80, 19);
                }
                else
                {
                    this.blit(matrixStack, startX + 186, startY + i * 19 + 6 + 57, 0, 38, 80, 19);
                }

                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                String name = stack.getHoverName().getString();
                if(this.font.width(name) > 55)
                {
                    name = this.font.plainSubstrByWidth(stack.getHoverName().getString(), 50).trim() + "...";
                }
                this.font.draw(matrixStack, name, startX + 186 + 22, startY + i * 19 + 6 + 6 + 57, Color.WHITE.getRGB());

                Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(stack, startX + 186 + 2, startY + i * 19 + 6 + 1 + 57);

                if(this.checkBoxMaterials.isToggled())
                {
                    int count = InventoryUtil.getItemStackAmount(this.minecraft.player, stack);
                    stack = stack.copy();
                    stack.setCount(stack.getCount() - count);
                }

                Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(this.font, stack, startX + 186 + 2, startY + i * 19 + 6 + 1 + 57, null);
            }
        }

        this.drawVehicle(startX + 88, startY + 90, partialTicks);
    }

    private void drawVehicle(int x, int y, float partialTicks)
    {
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x, (float) y, 1050.0F);
        RenderSystem.scalef(-1.0F, -1.0F, -1.0F);

        MatrixStack matrixStack = new MatrixStack();
        matrixStack.translate(0.0D, 0.0D, 1000.0D);

        float scale = this.prevVehicleScale + (this.vehicleScale - this.prevVehicleScale) * partialTicks;
        matrixStack.scale(scale, scale, scale);

        Quaternion quaternion = Axis.POSITIVE_X.rotationDegrees(-5F);
        Quaternion quaternion1 = Axis.POSITIVE_Y.rotationDegrees(-(this.minecraft.player.tickCount + partialTicks));
        quaternion.mul(quaternion1);
        matrixStack.mulPose(quaternion);

        int vehicleIndex = this.transitioning ? prevCurrentVehicle : currentVehicle;
        VehicleProperties properties = this.cachedVehicle[vehicleIndex].getProperties();
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
        RenderSystem.runAsFancy(() -> {
            renderManager.render(this.cachedVehicle[vehicleIndex], 0.0, 0.0, 0.0, 0.0F, 1.0F, matrixStack, renderTypeBuffer, 15728880);
        });
        renderTypeBuffer.endBatch();
        renderManager.setRenderShadow(true);

        matrixStack.popPose();

        RenderSystem.popMatrix();
    }

    private void drawSlot(MatrixStack matrixStack, int startX, int startY, int x, int y, int iconX, int iconY, int slot, boolean required, boolean applicable)
    {
        int textureOffset = required ? 18 : 0;
        this.blit(matrixStack, startX + x, startY + y, 128 + textureOffset, 0, 18, 18);
        if(this.workstation.getItem(slot).isEmpty())
        {
            if(applicable)
            {
                this.blit(matrixStack, startX + x + 1, startY + y + 1, iconX + (required ? 16 : 0), iconY, 16, 16);
            }
            else
            {
                this.blit(matrixStack, startX + x + 1, startY + y + 1, iconX + (required ? 16 : 0), 48, 16, 16);
            }
        }
    }

    private void drawSlotTooltip(MatrixStack matrixStack, List<ITextComponent> text, int startX, int startY, int x, int y, int mouseX, int mouseY, int slot)
    {
        if(this.workstation.getItem(slot).isEmpty())
        {
            if(CommonUtils.isMouseWithin(mouseX, mouseY, startX + x, startY + y, 18, 18))
            {
                this.renderTooltip(matrixStack, Lists.transform(text, ITextComponent::getVisualOrderText), mouseX, mouseY);
            }
        }
    }

    private List<MaterialItem> getMaterials()
    {
        List<MaterialItem> materials = NonNullList.withSize(7, new MaterialItem(ItemStack.EMPTY));
        List<MaterialItem> filteredMaterials = this.materials.stream().filter(materialItem -> this.checkBoxMaterials.isToggled() ? !materialItem.isEnabled() : !materialItem.stack.isEmpty()).collect(Collectors.toList());
        for(int i = 0; i < filteredMaterials.size() && i < materials.size(); i++)
        {
            materials.set(i, filteredMaterials.get(i));
        }
        return materials;
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY)
    {
        this.font.draw(matrixStack, this.playerInventory.getDisplayName().getString(), 8, 109, 4210752);
    }

    public static class MaterialItem
    {
        public static final MaterialItem EMPTY = new MaterialItem();

        private boolean enabled = false;
        private ItemStack stack = ItemStack.EMPTY;

        public MaterialItem()
        {
        }

        public MaterialItem(ItemStack stack)
        {
            this.stack = stack;
        }

        public ItemStack getStack()
        {
            return stack;
        }

        public void update()
        {
            if(!this.stack.isEmpty())
            {
                this.enabled = InventoryUtil.hasItemStack(Minecraft.getInstance().player, this.stack);
            }
        }

        public boolean isEnabled()
        {
            return this.stack.isEmpty() || this.enabled;
        }
    }
}
