package com.omgisa.the_forgotten_banana_of_rebirth.block;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import com.omgisa.the_forgotten_banana_of_rebirth.block.custom.TombstoneBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TheForgottenBananaOfRebirth.MOD_ID);

    public static final DeferredBlock<Block> TOMBSTONE =
            BLOCKS.registerBlock("tombstone", (properties) -> new TombstoneBlock(
                    // Make the tombstone unbreakable and explosion-proof
                    properties.noOcclusion().strength(-1.0F, 3_600_000.0F)
            ));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
