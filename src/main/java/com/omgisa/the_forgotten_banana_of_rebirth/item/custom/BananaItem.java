package com.omgisa.the_forgotten_banana_of_rebirth.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class BananaItem extends Item {
    public BananaItem(Properties properties) {
        super(properties);
    }

    /**
     * The required number of deaths a dead player must have for this banana to work.
     * Default banana works at exactly 1 death.
     */
    public int getRequiredDeaths() {
        return 1;
    }

    /**
     * Returns whether this banana can revive the given dead player based on their current deaths score.
     * Override in subclasses for custom gating logic.
     */
    public boolean canRevive(ServerPlayer clicker, ServerPlayer deadPlayer, int deaths) {
        return deaths == getRequiredDeaths();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.the_forgotten_banana_of_rebirth.banana.tooltip"));
    }
}
