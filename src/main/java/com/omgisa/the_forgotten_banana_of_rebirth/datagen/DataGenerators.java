package com.omgisa.the_forgotten_banana_of_rebirth.datagen;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = TheForgottenBananaOfRebirth.MOD_ID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Client assets
        generator.addProvider(true, new ModModelProvider(packOutput));
        // Add recipes here too so runData generates them in this environment
        generator.addProvider(true, new ModRecipeProvider.Runner(packOutput, lookupProvider));
    }

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Server data: recipes, tags, loot, etc.
        generator.addProvider(true, new ModRecipeProvider.Runner(packOutput, lookupProvider));
        generator.addProvider(true, new ModModelProvider(packOutput));
    }

}
