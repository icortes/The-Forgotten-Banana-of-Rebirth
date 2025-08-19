package com.omgisa.the_forgotten_banana_of_rebirth.event;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TheForgottenBananaOfRebirth.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        ServerLevel level = player.level();
        Scoreboard scoreboard = level.getScoreboard();
        String deathsObjective = "deaths";
        Objective objective = scoreboard.getObjective(deathsObjective);
        if (objective == null) {
            objective = scoreboard.addObjective(
                    deathsObjective,
                    ObjectiveCriteria.DEATH_COUNT,
                    Component.literal("Deaths").withStyle(ChatFormatting.RED),
                    ObjectiveCriteria.RenderType.INTEGER,
                    true,
                    null
            );
        }
        // Add player to scoreboard if not present
        var scoreAccess = scoreboard.getOrCreatePlayerScore(player, objective);
        if (scoreAccess.get() == 0) {
            scoreAccess.set(0);
        }
        // Set the deaths objective as the display for all players in the sidebar
        scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, objective);

        // Set the deaths objective as the display below the name
        scoreboard.setDisplayObjective(DisplaySlot.BELOW_NAME, objective);
    }
}
