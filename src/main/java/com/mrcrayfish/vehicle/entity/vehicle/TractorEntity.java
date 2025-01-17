package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.EngineType;
import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import com.mrcrayfish.vehicle.init.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity.FuelPortType;

/**
 * Author: MrCrayfish
 */
public class TractorEntity extends LandVehicleEntity
{
    public TractorEntity(EntityType<? extends TractorEntity> type, World worldIn)
    {
        super(type, worldIn);
        this.setMaxSpeed(6);
        this.setTurnSensitivity(3);
    }

    @Override
    public SoundEvent getMovingSound()
    {
        return ModSounds.TRACTOR_ENGINE_MONO.get();
    }

    @Override
    public SoundEvent getRidingSound()
    {
        return ModSounds.TRACTOR_ENGINE_STEREO.get();
    }

    @Override
    public float getMinEnginePitch()
    {
        return 0.8F;
    }

    @Override
    public float getMaxEnginePitch()
    {
        return 1.6F;
    }

    @Override
    public EngineType getEngineType()
    {
        return EngineType.LARGE_MOTOR;
    }

    @Override
    public FuelPortType getFuelPortType()
    {
        return FuelPortType.DEFAULT;
    }

    @Override
    public boolean shouldRenderEngine()
    {
        return true;
    }

    @Override
    public boolean shouldShowEngineSmoke()
    {
        return true;
    }

    @Override
    public Vector3d getEngineSmokePosition()
    {
        return new Vector3d(-0.125, 1.9375, 1.125);
    }

    @Override
    public boolean canTowTrailer()
    {
        return true;
    }

    @Override
    public boolean canMountTrailer()
    {
        return false;
    }

    @Override
    public boolean canBeColored()
    {
        return true;
    }
}
