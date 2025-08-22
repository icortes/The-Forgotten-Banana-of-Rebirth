package com.omgisa.the_forgotten_banana_of_rebirth.event;

import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import com.omgisa.the_forgotten_banana_of_rebirth.block.ModBlocks;
import com.omgisa.the_forgotten_banana_of_rebirth.block.custom.TombstoneBlock;
import com.omgisa.the_forgotten_banana_of_rebirth.block.entity.TombstoneBlockEntity;
import com.omgisa.the_forgotten_banana_of_rebirth.item.ModItems;
import com.omgisa.the_forgotten_banana_of_rebirth.item.custom.BananaItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.*;

@EventBusSubscriber(modid = TheForgottenBananaOfRebirth.MOD_ID)
public class ModEvents {

    // Simple per-player cooldown to avoid spamming heartbeat every tick
    private static final Map<UUID, Integer> HEARTBEAT_COOLDOWN_TICK = new HashMap<>();
    // Track which players have the Darkness effect applied by this mod's low-health logic
    private static final Set<UUID> LOW_HEALTH_DARKNESS_OWNER = new HashSet<>();
    // Heartbeat tuning constants
    // Interval range in ticks: faster when health is lower
    private static final int HEARTBEAT_INTERVAL_MIN_TICKS = 10;   // ~0.5s at 20 TPS
    private static final int HEARTBEAT_INTERVAL_MAX_TICKS = 40;   // ~2.0s at 20 TPS
    // Volume range: louder when health is lower
    private static final float HEARTBEAT_VOLUME_MIN = 0.15f;
    private static final float HEARTBEAT_VOLUME_MAX = 1.0f;
    // Minimum health (in health points, not hearts) at or below which low-health effects trigger
    private static final float LOW_HEALTH_THRESHOLD = 6.0f; // 3 hearts
    // Minimum remaining max health in hearts; player max health will not drop below this value
    private static final double MIN_MAX_HEALTH_HEARTS = 5.0; // 5 hearts => 10.0 health points
    // Test toggle: when true, requires hardcore mode for death logic; when false, always runs regardless of hardcore.
    public static boolean REQUIRE_HARDCORE_FOR_DEATH_LOGIC = true;

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

        // Broadcast death coordinates to all players
        var server = player.getServer();
        if (server != null) {
            BlockPos deathPos = player.blockPosition();
            Component msg = Component.literal(player.getGameProfile().getName() + " died at "
                                                      + deathPos.getX() + ", " + deathPos.getY() + ", " + deathPos.getZ());
            server.getPlayerList().broadcastSystemMessage(msg, false);
        }

        // Server only and optional hardcore mode check (controlled by REQUIRE_HARDCORE_FOR_DEATH_LOGIC)
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

        // Only transfer inventory when keepInventory gamerule is false to avoid duplication on respawn
        boolean keepInv = level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        if (!keepInv) {
            BlockEntity be = level.getBlockEntity(placePos);
            if (be instanceof TombstoneBlockEntity tombstoneBE) {
                tombstoneBE.depositFrom(player);
            }
        }

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

        // 3) Remove one heart (2 health) from player's max health, clamped to minimum of configured hearts
        var maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            double minHealthPoints = MIN_MAX_HEALTH_HEARTS * 2.0; // convert hearts to health points
            double newBase = Math.max(minHealthPoints, maxHealthAttr.getBaseValue() - 2.0);
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
        var existing = player.getEffect(MobEffects.DARKNESS);

        // Treat long-duration Darkness as external (e.g., Warden/Sculk). Our effect is ~60t; add buffer.
        boolean externalDarknessActive = existing != null && (!LOW_HEALTH_DARKNESS_OWNER.contains(id) || existing.getDuration() > 80);

        // Trigger when health is at or below the configured threshold (health points)
        if (health < LOW_HEALTH_THRESHOLD) {
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
                existing = player.getEffect(MobEffects.DARKNESS);
            }

            // Refresh only if ours is present and running low
            if (existing != null && existing.getDuration() <= 30) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, true, false, true));
                existing = player.getEffect(MobEffects.DARKNESS);
            }

            // Compute dynamic interval based on health (lower health -> faster heartbeat)
            float denom = Math.max(0.001f, LOW_HEALTH_THRESHOLD);
            float clamped = Math.max(0f, Math.min(denom, health));
            int interval = HEARTBEAT_INTERVAL_MIN_TICKS + (int) ((HEARTBEAT_INTERVAL_MAX_TICKS - HEARTBEAT_INTERVAL_MIN_TICKS) * (clamped / denom));
            // Compute dynamic volume based on health (lower health -> louder)
            float lowFactor = 1.0f - (clamped / denom); // 0 at threshold, 1 near 0 health
            float volume = HEARTBEAT_VOLUME_MIN + (HEARTBEAT_VOLUME_MAX - HEARTBEAT_VOLUME_MIN) * lowFactor;

            // Heartbeat only when our short Darkness is active; also allow faster reschedule if health dropped
            if (existing != null && existing.getDuration() <= 80) {
                int tick = player.tickCount;
                int nextAllowed = HEARTBEAT_COOLDOWN_TICK.getOrDefault(id, 0);
                // If health just dropped, pull nextAllowed closer so cadence speeds up immediately
                int desiredNext = tick + interval;
                if (nextAllowed > desiredNext) {
                    nextAllowed = desiredNext;
                    HEARTBEAT_COOLDOWN_TICK.put(id, nextAllowed);
                }
                if (tick >= nextAllowed) {
                    ServerLevel level = player.level();
                    level.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.WARDEN_HEARTBEAT,
                            SoundSource.PLAYERS,
                            volume,
                            1.0f
                    );
                    HEARTBEAT_COOLDOWN_TICK.put(id, tick + interval);
                }
            }
        } else {
            // Recovered: stop owning without force-removing the Darkness effect
            LOW_HEALTH_DARKNESS_OWNER.remove(id);
            HEARTBEAT_COOLDOWN_TICK.remove(id);
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClicksTombstoneWithBanana(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer clicker))
            return;
        ServerLevel level = (ServerLevel) event.getLevel();
        if (level.isClientSide)
            return;

        ItemStack held = event.getItemStack();
        if (held.isEmpty())
            return;

        if (!(held.getItem() instanceof BananaItem bananaItem))
            return;

        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TombstoneBlockEntity tombstone))
            return;

        Optional<UUID> ownerIdOpt = tombstone.getOwnerUuid();
        if (ownerIdOpt.isEmpty()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        UUID ownerId = ownerIdOpt.get();
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (!owner.isSpectator()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        Scoreboard scoreboard = level.getScoreboard();
        Objective objective = scoreboard.getObjective("deaths");
        if (objective == null) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
        int deaths = scoreboard.getOrCreatePlayerScore(owner, objective).get();

        if (!bananaItem.canRevive(clicker, owner, deaths)) {
            clicker.sendSystemMessage(bananaItem.getRestrictionMessage(deaths));
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        var server = level.getServer();
        var source = server.createCommandSourceStack().withPermission(4);
        server.getCommands().performPrefixedCommand(source, "gamemode survival " + owner.getGameProfile().getName());
        owner.setGameMode(GameType.SURVIVAL);

        owner.sendSystemMessage(Component.literal("You have been revived and set to Survival."));
        String itemName = held.getHoverName().getString();
        clicker.sendSystemMessage(Component.literal("You revived " + owner.getGameProfile().getName() + " with a " + itemName + "."));

        owner.setCamera(owner);
        owner.stopRiding();
        owner.stopUsingItem();
        owner.setNoGravity(false);
        owner.setInvulnerable(false);
        owner.setInvisible(false);
        owner.setDeltaMovement(0, 0, 0);
        owner.setSprinting(false);
        owner.setShiftKeyDown(false);
        owner.refreshDimensions();
        owner.onUpdateAbilities();

        float targetHealth = Math.max(8.0f, owner.getHealth());
        owner.setHealth(targetHealth);

        held.shrink(1);

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.FARMER)
            return;

        // Novice level (1) trade: Farmer sells 1 Banana for 16-23 emeralds, single use, high XP
        var trades = event.getTrades();
        List<VillagerTrades.ItemListing> level1 = trades.computeIfAbsent(1, k -> new ArrayList<>());
        level1.add((entity, random) -> {
            // 50% chance to appear
            if (random.nextFloat() >= 0.50f)
                return null;

            int emeraldCost = 16 + random.nextInt(8); // 16-23 emeralds
            ItemCost price = new ItemCost(Items.EMERALD, emeraldCost);
            ItemStack result = new ItemStack(ModItems.BANANA.get(), 1);
            int maxUses = 1; // very limited supply
            int xp = 30;     // generous experience to signify rarity
            float priceMultiplier = 0.05f;
            return new MerchantOffer(price, result, maxUses, xp, priceMultiplier);
        });
    }
}
