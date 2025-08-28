package com.omgisa.the_forgotten_banana_of_rebirth.item;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import com.omgisa.the_forgotten_banana_of_rebirth.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheForgottenBananaOfRebirth.MOD_ID);

    public static final Supplier<CreativeModeTab> BANANA_ITEMS_TAB =
            CREATIVE_MODE_TAB.register("banana_items_tab",
                                       () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BANANA.get()))
                                                            .title(Component.translatable("creativetab.the_forgotten_banana_of_rebirth.banana_items"))
                                                            .displayItems((itemDisplayParameters, output) -> {
                                                                output.accept(ModItems.BANANA);
                                                                output.accept(ModItems.DIAMOND_BANANA);
                                                                output.accept(ModItems.HARDENED_DIAMOND_BANANA);
                                                                output.accept(ModItems.NETHERITE_BANANA);
                                                                output.accept(ModItems.DRAGON_BANANA);
                                                            }).build());

    public static final Supplier<CreativeModeTab> BANANA_BLOCKS_TAB =
            CREATIVE_MODE_TAB.register("banana_blocks_tab",
                                       () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.TOMBSTONE.get()))
                                                            .withTabsBefore(ResourceLocation.fromNamespaceAndPath(TheForgottenBananaOfRebirth.MOD_ID, "banana_items_tab"))
                                                            .title(Component.translatable("creativetab.the_forgotten_banana_of_rebirth.banana_blocks"))
                                                            .displayItems((itemDisplayParameters, output) -> {
                                                                output.accept(ModBlocks.TOMBSTONE);
                                                            }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
