package com.omgisa.the_forgotten_banana_of_rebirth.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class HardenedDiamondBananaItem extends BananaItem {
    public HardenedDiamondBananaItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getRequiredDeaths() {
        return 3;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.the_forgotten_banana_of_rebirth.hardened_diamond_banana.tooltip"));
    }
}

