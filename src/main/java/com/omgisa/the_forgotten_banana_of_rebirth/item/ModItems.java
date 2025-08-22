package com.omgisa.the_forgotten_banana_of_rebirth.item;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import com.omgisa.the_forgotten_banana_of_rebirth.block.ModBlocks;
import com.omgisa.the_forgotten_banana_of_rebirth.item.custom.BananaItem;
import com.omgisa.the_forgotten_banana_of_rebirth.item.custom.DiamondBananaItem;
import com.omgisa.the_forgotten_banana_of_rebirth.item.custom.HardenedDiamondBananaItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheForgottenBananaOfRebirth.MOD_ID);

    public static final DeferredItem<Item> BANANA = ITEMS.registerItem("banana", (properties) -> new BananaItem(properties.food(ModFoodProperties.BANANA)));
    public static final DeferredItem<Item> DIAMOND_BANANA = ITEMS.registerItem("diamond_banana", DiamondBananaItem::new);
    public static final DeferredItem<Item> HARDENED_DIAMOND_BANANA = ITEMS.registerItem("hardened_diamond_banana", HardenedDiamondBananaItem::new);
    public static final DeferredItem<Item> TOMBSTONE = ITEMS.registerItem("tombstone", (properties) -> new BlockItem(ModBlocks.TOMBSTONE.get(), properties));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}