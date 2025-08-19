package com.omgisa.the_forgotten_banana_of_rebirth.item;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
            }).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
