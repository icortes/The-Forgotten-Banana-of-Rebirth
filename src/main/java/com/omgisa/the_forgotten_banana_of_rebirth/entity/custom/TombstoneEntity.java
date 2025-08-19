package com.omgisa.the_forgotten_banana_of_rebirth.entity.custom;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class TombstoneEntity extends Entity {
    public TombstoneEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {

    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {

    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {

    }
}
