package com.mrcrayfish.vehicle.item;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.entity.EngineType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ItemEngine extends ItemPart implements SubItems
{
    public ItemEngine(String id)
    {
        super(id);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        EngineType type = EngineType.getType(stack.getMetadata());
        tooltip.add(type.getTierColor() + TextFormatting.BOLD.toString() + type.getTierName() + " Tier");
        tooltip.add(TextFormatting.YELLOW + "Acceleration: " + TextFormatting.RESET + type.getAccelerationMultiplier() + "x");
        tooltip.add(TextFormatting.YELLOW + "Additional Max Speed: " + TextFormatting.RESET + (type.getAdditionalMaxSpeed() * 3.6) + "kph");
        tooltip.add(TextFormatting.YELLOW + "Fuel Consumption: " + TextFormatting.RESET + type.getFuelConsumption() + "pt");
    }

    @Override
    public NonNullList<ResourceLocation> getModels()
    {
        NonNullList<ResourceLocation> modelLocations = NonNullList.create();
        for(EngineType type : EngineType.values())
        {
            modelLocations.add(new ResourceLocation(Reference.MOD_ID, getUnlocalizedName().substring(5) + "/" + type.toString().toLowerCase()));
        }
        return modelLocations;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        EngineType type = EngineType.getType(stack.getMetadata());
        return super.getUnlocalizedName(stack) + "." + type.toString().toLowerCase();
    }
}
