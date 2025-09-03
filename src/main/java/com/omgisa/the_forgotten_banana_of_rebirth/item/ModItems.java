package com.omgisa.the_forgotten_banana_of_rebirth.item;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import com.omgisa.the_forgotten_banana_of_rebirth.block.ModBlocks;
import com.omgisa.the_forgotten_banana_of_rebirth.item.custom.*;
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
    public static final DeferredItem<Item> NETHERITE_BANANA = ITEMS.registerItem("netherite_banana", NetheriteBananaItem::new);
    public static final DeferredItem<Item> DRAGON_BANANA = ITEMS.registerItem("dragon_banana", DragonBananaItem::new);
    public static final DeferredItem<Item> TOMBSTONE = ITEMS.registerItem("tombstone", (properties) -> new BlockItem(ModBlocks.TOMBSTONE.get(), properties));
    // Durian stacks to 16
    public static final DeferredItem<Item> DURIAN = ITEMS.registerItem("durian", (properties) -> new DurianItem(properties.stacksTo(16).food(ModFoodProperties.DURIAN)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}