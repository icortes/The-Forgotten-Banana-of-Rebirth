package com.omgisa.the_forgotten_banana_of_rebirth.block.entity;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import com.omgisa.the_forgotten_banana_of_rebirth.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, TheForgottenBananaOfRebirth.MOD_ID);

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }    public static final Supplier<BlockEntityType<TombstoneBlockEntity>> TOMBSTONE_BE =
            BLOCK_ENTITIES.register("tombstone", () -> new BlockEntityType<>(TombstoneBlockEntity::new, Set.of(ModBlocks.TOMBSTONE.get()), true));


}
