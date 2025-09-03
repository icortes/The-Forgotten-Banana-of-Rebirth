package com.omgisa.the_forgotten_banana_of_rebirth.item;

import net.minecraft.world.food.FoodProperties;

public class ModFoodProperties {
    public static final FoodProperties BANANA = new FoodProperties.Builder().nutrition(3).saturationModifier(0.25f).build();
    // Durian: stronger than banana, slightly weaker than golden carrot (6, 1.2f)
    public static final FoodProperties DURIAN = new FoodProperties.Builder().nutrition(5).saturationModifier(1.0f).build();
}
