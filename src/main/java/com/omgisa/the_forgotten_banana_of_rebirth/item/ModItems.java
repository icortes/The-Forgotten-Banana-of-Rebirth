package com.omgisa.the_forgotten_banana_of_rebirth.item;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import com.omgisa.the_forgotten_banana_of_rebirth.block.ModBlocks;
import com.omgisa.the_forgotten_banana_of_rebirth.item.custom.BananaItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheForgottenBananaOfRebirth.MOD_ID);

    public static final DeferredItem<Item> BANANA = ITEMS.registerItem("banana", (properties) -> new BananaItem(properties.food(ModFoodProperties.BANANA)));
    public static final DeferredItem<Item> TOMBSTONE = ITEMS.registerItem("tombstone", (properties) -> new BlockItem(ModBlocks.TOMBSTONE.get(), properties));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}