package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.EngineType;
import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import com.mrcrayfish.vehicle.entity.WheelType;
import com.mrcrayfish.vehicle.init.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * Author: MrCrayfish
 */
public class DuneBuggyEntity extends LandVehicleEntity
{
    public DuneBuggyEntity(EntityType<? extends DuneBuggyEntity> type, World worldIn)
    {
        super(type, worldIn);
        this.setMaxSpeed(10);
        this.maxUpStep = 0.5F;
        this.setFuelCapacity(5000F);
    }

    @Override
    public void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.set(WHEEL_TYPE, WheelType.PLASTIC.ordinal());
        this.entityData.set(COLOR, 0xF2B116);
    }

    @Override
    public SoundEvent getMovingSound()
    {
        return ModSounds.ELECTRIC_ENGINE_MONO.get();
    }

    @Override
    public SoundEvent getRidingSound()
    {
        return ModSounds.ELECTRIC_ENGINE_STEREO.get();
    }

    @Override
    public EngineType getEngineType()
    {
        return EngineType.ELECTRIC_MOTOR;
    }

    @Override
    public boolean isLockable()
    {
        return false;
    }
}
