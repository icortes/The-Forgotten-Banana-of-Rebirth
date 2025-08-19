package com.omgisa.the_forgotten_banana_of_rebirth.event;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import com.omgisa.the_forgotten_banana_of_rebirth.block.ModBlocks;
import com.omgisa.the_forgotten_banana_of_rebirth.block.custom.TombstoneBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = TheForgottenBananaOfRebirth.MOD_ID)
public class ModEvents {

    // Simple per-player cooldown to avoid spamming heartbeat every tick
    private static final Map<UUID, Integer> HEARTBEAT_COOLDOWN_TICK = new HashMap<>();
    // Track which players have the Darkness effect applied by this mod's low-health logic
    private static final Set<UUID> LOW_HEALTH_DARKNESS_OWNER = new HashSet<>();
    // Test toggle: when true, requires hardcore mode for death logic; when false, always runs regardless of hardcore.
    public static boolean REQUIRE_HARDCORE_FOR_DEATH_LOGIC = false;

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

        // Cleanup any tracking for this player
        UUID id = player.getUUID();
        HEARTBEAT_COOLDOWN_TICK.remove(id);
        LOW_HEALTH_DARKNESS_OWNER.remove(id);

        // Server only and optional hardcore mode check (controlled by REQUIRE_HARDCORE_FOR_DEATH_LOGIC)
        var server = player.getServer();
        if (server == null || (REQUIRE_HARDCORE_FOR_DEATH_LOGIC && !server.isHardcore()))
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

    // Also clear tracking when a player logs out
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        UUID id = player.getUUID();
        HEARTBEAT_COOLDOWN_TICK.remove(id);
        LOW_HEALTH_DARKNESS_OWNER.remove(id);
    }

    @SubscribeEvent
    public static void onPlayerHealthBelowFourHearts(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        if (!player.isAlive())
            return;

        float health = player.getHealth();
        UUID id = player.getUUID();
        boolean weOwn = LOW_HEALTH_DARKNESS_OWNER.contains(id);
        var existing = player.getEffect(MobEffects.DARKNESS);

        // Treat long-duration Darkness as external (e.g., Warden/Sculk). Our effect is ~60t; add buffer.
        boolean externalDarknessActive = existing != null && (!weOwn || existing.getDuration() > 80);

        // 4 hearts = 8.0 health
        if (health < 8.0F) {
            if (externalDarknessActive) {
                // Defer to external effect and stop our ownership/heartbeat to avoid interference
                LOW_HEALTH_DARKNESS_OWNER.remove(id);
                HEARTBEAT_COOLDOWN_TICK.remove(id);
                return;
            }

            // Only apply our Darkness if none is present
            if (existing == null) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, true, false, true));
                LOW_HEALTH_DARKNESS_OWNER.add(id);
                weOwn = true;
                existing = player.getEffect(MobEffects.DARKNESS);
            }

            // Refresh only if ours is present and running low
            if (weOwn && existing != null && existing.getDuration() <= 30) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, true, false, true));
                existing = player.getEffect(MobEffects.DARKNESS);
            }

            // Heartbeat only when our short Darkness is active
            if (weOwn && existing != null && existing.getDuration() <= 80) {
                int tick = player.tickCount;
                int nextAllowed = HEARTBEAT_COOLDOWN_TICK.getOrDefault(id, 0);
                if (tick >= nextAllowed) {
                    ServerLevel level = player.level();
                    level.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.WARDEN_HEARTBEAT,
                            SoundSource.PLAYERS,
                            0.7f,
                            1.0f
                    );
                    HEARTBEAT_COOLDOWN_TICK.put(id, tick + 40);
                }
            }
        } else {
            // Recovered: stop owning without force-removing the Darkness effect
            LOW_HEALTH_DARKNESS_OWNER.remove(id);
            HEARTBEAT_COOLDOWN_TICK.remove(id);
        }
    }
}
