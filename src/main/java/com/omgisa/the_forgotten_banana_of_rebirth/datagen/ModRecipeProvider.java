package com.omgisa.the_forgotten_banana_of_rebirth.datagen;

import com.omgisa.the_forgotten_banana_of_rebirth.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        var items = this.registries.lookupOrThrow(Registries.ITEM);
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.DIAMOND_BANANA.get())
                           .define('D', Items.DIAMOND)
                           .define('B', ModItems.BANANA.get())
                           .pattern("DDD")
                           .pattern("DBD")
                           .pattern("DDD")
                           .unlockedBy(getHasName(ModItems.BANANA.get()), has(ModItems.BANANA.get()))
                           .save(this.output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.HARDENED_DIAMOND_BANANA.get())
                           .define('D', Items.DIAMOND_BLOCK)
                           .define('B', ModItems.DIAMOND_BANANA.get())
                           .pattern("DDD")
                           .pattern("DBD")
                           .pattern("DDD")
                           .unlockedBy(getHasName(ModItems.DIAMOND_BANANA.get()), has(ModItems.DIAMOND_BANANA.get()))
                           .save(this.output);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider) {
            super(packOutput, provider);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider provider, @NotNull RecipeOutput recipeOutput) {
            return new ModRecipeProvider(provider, recipeOutput);
        }

        @Override
        public @NotNull String getName() {
            return "The Forgotten Banana of Rebirth: Recipes";
        }
    }
}
