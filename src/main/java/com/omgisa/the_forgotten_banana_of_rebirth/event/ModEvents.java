package com.omgisa.the_forgotten_banana_of_rebirth.event;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import com.omgisa.the_forgotten_banana_of_rebirth.block.ModBlocks;
import com.omgisa.the_forgotten_banana_of_rebirth.block.custom.TombstoneBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
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

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        // Server only
        var server = player.getServer();
        if (server == null /*|| !server.isHardcore() */)
            return;

        ServerLevel level = player.level();

        // 1) Place a tombstone where they died (or just above if occupied)
        BlockPos placePos = player.blockPosition();
        if (!level.isEmptyBlock(placePos))
            placePos = placePos.above();
        if (!level.isEmptyBlock(placePos))
            placePos = placePos.above();

        // Build a randomized tombstone state facing the player (so its front faces death spot)
        TombstoneBlock tombstoneBlock = (TombstoneBlock) ModBlocks.TOMBSTONE.get();
        int variant = level.random.nextInt(3);
        BlockState tombstoneState = tombstoneBlock.defaultBlockState()
                                                  .setValue(TombstoneBlock.FACING, player.getDirection().getOpposite())
                                                  .setValue(TombstoneBlock.VARIANT, variant);
        level.setBlockAndUpdate(placePos, tombstoneState);

        // 2) Ensure the deaths scoreboard objective exists (DEATH_COUNT automatically increments)
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
            // Make sure it's visible like on join
            scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, objective);
            scoreboard.setDisplayObjective(DisplaySlot.BELOW_NAME, objective);
        }
        // Note: DEATH_COUNT objectives increment automatically; do not modify manually to avoid double counting.

        // 3) Remove one heart (2 health) from player's max health, clamped to minimum of 1 heart (2.0)
        var maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            double newBase = Math.max(2.0, maxHealthAttr.getBaseValue() - 2.0);
            maxHealthAttr.setBaseValue(newBase);
        }
    }
}
