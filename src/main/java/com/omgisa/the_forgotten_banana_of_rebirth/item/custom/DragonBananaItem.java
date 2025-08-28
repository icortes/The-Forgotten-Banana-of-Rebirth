package com.omgisa.the_forgotten_banana_of_rebirth.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class DragonBananaItem extends BananaItem {
    public DragonBananaItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canRevive(ServerPlayer clicker, ServerPlayer deadPlayer, int deaths) {
        // Works for players with 5 deaths or more
        return deaths >= 5;
    }

    @Override
    public Component getRestrictionMessage(int currentDeaths) {
        return Component.literal("This item only works when the dead player has 5 deaths or more.");
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.the_forgotten_banana_of_rebirth.dragon_banana.tooltip"));
    }
}
