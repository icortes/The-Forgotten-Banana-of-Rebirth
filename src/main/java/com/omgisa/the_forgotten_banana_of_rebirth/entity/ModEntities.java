package com.omgisa.the_forgotten_banana_of_rebirth.entity;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import com.omgisa.the_forgotten_banana_of_rebirth.entity.custom.TombstoneEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, TheForgottenBananaOfRebirth.MOD_ID);

    public static ResourceKey<EntityType<?>> TOMBSTONE_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace("tombstone_entity"));

    public static final Supplier<EntityType<TombstoneEntity>> TOMBSTONE_ENTITY =
            ENTITY_TYPES.register("tombstone_entity", () -> EntityType.Builder.of(TombstoneEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).build(TOMBSTONE_KEY));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
