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

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.NETHERITE_BANANA.get())
                           .define('N', Items.NETHERITE_INGOT)
                           .define('G', Items.GOLD_BLOCK)
                           .define('B', ModItems.HARDENED_DIAMOND_BANANA.get())
                           .pattern("NGN")
                           .pattern("GBG")
                           .pattern("NGN")
                           .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                           .save(this.output);

        // Dragon Banana: center is Netherite Banana, include one Dragon Head and one Dragon Egg, rest Purpur Blocks
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.DRAGON_BANANA.get())
                           .define('P', Items.PURPUR_BLOCK)
                           .define('H', Items.DRAGON_HEAD)
                           .define('E', Items.DRAGON_EGG)
                           .define('B', ModItems.NETHERITE_BANANA.get())
                           .pattern("PHP")
                           .pattern("PBP")
                           .pattern("PEP")
                           .unlockedBy(getHasName(ModItems.NETHERITE_BANANA.get()), has(ModItems.NETHERITE_BANANA.get()))
                           .save(this.output);

        // Enchanted Durian: Shaped 3x3 pattern using Ghast Tears, Enchanted Golden Apples, Nether Star, Durian, and Dragon's Breath
        ShapedRecipeBuilder.shaped(items, RecipeCategory.FOOD, ModItems.ENCHANTED_DURIAN.get())
                           .define('T', Items.GHAST_TEAR)
                           .define('A', Items.ENCHANTED_GOLDEN_APPLE)
                           .define('N', Items.NETHER_STAR)
                           .define('D', ModItems.DURIAN.get())
                           .define('B', Items.DRAGON_BREATH)
                           .pattern("TAT")
                           .pattern("NDB")
                           .pattern("TAT")
                           .unlockedBy(getHasName(ModItems.DURIAN.get()), has(ModItems.DURIAN.get()))
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
