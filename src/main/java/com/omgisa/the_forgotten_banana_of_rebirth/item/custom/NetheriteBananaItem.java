package com.omgisa.the_forgotten_banana_of_rebirth.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class NetheriteBananaItem extends BananaItem {
    public NetheriteBananaItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getRequiredDeaths() {
        return 4;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.the_forgotten_banana_of_rebirth.netherite_banana.tooltip"));
    }
}

