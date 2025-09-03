package com.omgisa.the_forgotten_banana_of_rebirth.item.custom;

import com.omgisa.the_forgotten_banana_of_rebirth.Config;
import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public class EnchantedDurianItem extends DurianItem {
    public EnchantedDurianItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                // Convert hearts cap to health points (HP)
                double capHp = Config.maxHeartsCapHearts * 2.0D;

                // Current value after all mods
                double currentHp = maxHealth.getValue();

                // If already at/above cap, do nothing
                if (currentHp < capHp) {
                    // Amount we want to add this eat
                    double addHp = 2.0D; // +1 heart
                    // Clamp so we never exceed cap
                    double allowedAdd = Math.min(addHp, capHp - currentHp);
                    if (allowedAdd > 0.0D) {
                        String idPath = "enchanted_durian_boost_" + UUID.randomUUID();
                        AttributeModifier boost = new AttributeModifier(ResourceLocation.fromNamespaceAndPath(TheForgottenBananaOfRebirth.MOD_ID, idPath), allowedAdd, AttributeModifier.Operation.ADD_VALUE);
                        maxHealth.addPermanentModifier(boost);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.the_forgotten_banana_of_rebirth.enchanted_durian.tooltip"));
    }
}
