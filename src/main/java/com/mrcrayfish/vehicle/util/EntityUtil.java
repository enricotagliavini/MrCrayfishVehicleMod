package com.mrcrayfish.vehicle.util;

import com.mrcrayfish.vehicle.block.VehicleCrateBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.function.BiFunction;

/**
 * Author: MrCrayfish
 */
public class EntityUtil
{
    public static <T extends Entity> EntityType<T> buildVehicleType(ResourceLocation id, BiFunction<EntityType<T>, World, T> function, float width, float height)
    {
        return buildVehicleType(id, function, width, height, true);
    }

    public static <T extends Entity> EntityType<T> buildVehicleType(ResourceLocation id, BiFunction<EntityType<T>, World, T> function, float width, float height, boolean registerCrate)
    {
        EntityType<T> type = EntityType.Builder.of(function::apply, EntityClassification.MISC).sized(width, height).setTrackingRange(256).setUpdateInterval(1).fireImmune().setShouldReceiveVelocityUpdates(true).build(id.toString());
        if(registerCrate)
        {
            VehicleCrateBlock.registerVehicle(id);
        }
        return type;
    }
}
