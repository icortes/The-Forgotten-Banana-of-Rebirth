package com.omgisa.the_forgotten_banana_of_rebirth.event;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import com.omgisa.the_forgotten_banana_of_rebirth.block.ModBlocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = TheForgottenBananaOfRebirth.MOD_ID)
public class ModCommonEvents {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().is(ModBlocks.TOMBSTONE.get())) {
            event.setCanceled(true);
        }
    }
}

