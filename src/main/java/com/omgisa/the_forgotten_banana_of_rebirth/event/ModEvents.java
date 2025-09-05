package com.omgisa.the_forgotten_banana_of_rebirth.event;

import com.mojang.authlib.GameProfile;
import com.omgisa.the_forgotten_banana_of_rebirth.Config;
import com.omgisa.the_forgotten_banana_of_rebirth.TheForgottenBananaOfRebirth;
import com.omgisa.the_forgotten_banana_of_rebirth.block.ModBlocks;
import com.omgisa.the_forgotten_banana_of_rebirth.block.custom.TombstoneBlock;
import com.omgisa.the_forgotten_banana_of_rebirth.block.entity.TombstoneBlockEntity;
import com.omgisa.the_forgotten_banana_of_rebirth.item.ModItems;
import com.omgisa.the_forgotten_banana_of_rebirth.item.custom.BananaItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
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
        // Hide sidebar, show deaths under names and in TAB list
        scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, null);
        scoreboard.setDisplayObjective(DisplaySlot.BELOW_NAME, objective);
        scoreboard.setDisplayObjective(DisplaySlot.LIST, objective);
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
            ResourceKey<Level> dimKey = player.level().dimension();
            ResourceLocation dimLoc = dimKey.location();
            String dimId = dimLoc.toString();
            String friendlyDim;
            if (dimKey == Level.OVERWORLD)
                friendlyDim = "Overworld";
            else if (dimKey == Level.NETHER)
                friendlyDim = "Nether";
            else if (dimKey == Level.END)
                friendlyDim = "End";
            else
                friendlyDim = dimLoc.getNamespace() + ":" + dimLoc.getPath();

            int x = deathPos.getX();
            int y = deathPos.getY();
            int z = deathPos.getZ();

            // Base message
            MutableComponent base = Component.literal(player.getGameProfile().getName())
                                             .withStyle(ChatFormatting.GOLD)
                                             .append(Component.literal(" died at ").withStyle(ChatFormatting.GRAY))
                                             .append(Component.literal(x + ", " + y + ", " + z).withStyle(ChatFormatting.WHITE))
                                             .append(Component.literal(" in ").withStyle(ChatFormatting.GRAY))
                                             .append(Component.literal(friendlyDim).withStyle(ChatFormatting.AQUA));

            // Clickable helpers
            MutableComponent copyCoords = Component.literal(" [Copy]")
                                                   .withStyle(style -> style.withColor(ChatFormatting.YELLOW)
                                                                            .withUnderlined(true)
                                                                            .withClickEvent(new ClickEvent.CopyToClipboard(x + " " + y + " " + z))
                                                                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Copy coordinates to clipboard"))));

            String suggestTpCmd = "execute in " + dimId + " run tp @s " + x + " " + y + " " + z;
            MutableComponent suggestTp = Component.literal(" [Suggest TP]")
                                                  .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                                                                           .withUnderlined(true)
                                                                           .withClickEvent(new ClickEvent.SuggestCommand(suggestTpCmd))
                                                                           .withHoverEvent(new HoverEvent.ShowText(Component.literal("Suggest /tp to this location (requires permission)"))));

            MutableComponent msg = base.append(copyCoords).append(suggestTp);
            server.getPlayerList().broadcastSystemMessage(msg, false);
        }

        // Server only and optional hardcore mode check (now configurable)
        if (server == null || (Config.requireHardcoreForDeathLogic && !server.isHardcore()))
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
        }
        // Display deaths under names and in TAB list; keep sidebar hidden
        scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, null);
        scoreboard.setDisplayObjective(DisplaySlot.BELOW_NAME, objective);
        scoreboard.setDisplayObjective(DisplaySlot.LIST, objective);

        // 3) Remove one heart (2 health) from player's max health, clamped to minimum of configured hearts
        var maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            double minHealthPoints = Config.minMaxHealthHearts * 2.0; // convert hearts to health points
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
    public static void onPlayerHealthBelowThreshold(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        if (!player.isAlive())
            return;

        float health = player.getHealth();
        UUID id = player.getUUID();
        var existing = player.getEffect(MobEffects.DARKNESS);

        // Treat long-duration Darkness as external (e.g., Warden/Sculk). Our effect uses configured duration; add small buffer.
        int ourDarknessDuration = Math.max(1, Config.darknessDurationTicks);
        boolean externalDarknessActive = existing != null && (!LOW_HEALTH_DARKNESS_OWNER.contains(id) || existing.getDuration() > ourDarknessDuration + 20);

        // Configurable low-health threshold in hearts -> convert to health points
        float thresholdPoints = (float) (Config.lowHealthThreshold * 2.0);

        // Trigger when health is at or below the configured threshold (health points)
        if (health < thresholdPoints) {
            if (externalDarknessActive) {
                // Defer to external effect and stop our ownership/heartbeat to avoid interference
                LOW_HEALTH_DARKNESS_OWNER.remove(id);
                HEARTBEAT_COOLDOWN_TICK.remove(id);
                return;
            }

            // Only apply our Darkness if none is present
            if (existing == null) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, ourDarknessDuration, 0, true, false, true));
                LOW_HEALTH_DARKNESS_OWNER.add(id);
                existing = player.getEffect(MobEffects.DARKNESS);
            }

            // Refresh only if ours is present and running low (<= half of configured duration)
            int refreshAt = Math.max(1, ourDarknessDuration / 2);
            if (existing != null && LOW_HEALTH_DARKNESS_OWNER.contains(id) && existing.getDuration() <= refreshAt) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, ourDarknessDuration, 0, true, false, true));
                existing = player.getEffect(MobEffects.DARKNESS);
            }

            // Compute dynamic interval based on health (lower health -> faster heartbeat)
            float denom = Math.max(0.001f, thresholdPoints);
            float clamped = Math.max(0f, Math.min(denom, health));
            int intervalMin = Math.max(1, Config.heartbeatIntervalMinTicks);
            int intervalMax = Math.max(intervalMin, Config.heartbeatIntervalMaxTicks);
            int interval = intervalMin + (int) ((intervalMax - intervalMin) * (clamped / denom));
            // Compute dynamic volume based on health (lower health -> louder) using configurable min/max
            float volumeMin = (float) Config.heartbeatVolumeMin;
            float volumeMax = (float) Config.heartbeatVolumeMax;
            float lowFactor = 1.0f - (clamped / denom); // 0 at threshold, 1 near 0 health
            float volume = volumeMin + (volumeMax - volumeMin) * lowFactor;
            float pitch = (float) Math.max(0.5f, Math.min(2.0f, Config.heartbeatPitch));

            // Heartbeat only when our Darkness is active (ownership check); also allow faster reschedule if health dropped
            if (existing != null && LOW_HEALTH_DARKNESS_OWNER.contains(id)) {
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
                            pitch
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
    public static void onPlayerRightClicksTombstoneEmptyHanded(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer clicker))
            return;
        ServerLevel level = (ServerLevel) event.getLevel();
        if (level.isClientSide)
            return;

        // Only when empty-handed
        if (!event.getItemStack().isEmpty())
            return;

        BlockEntity be = level.getBlockEntity(event.getPos());
        if (!(be instanceof TombstoneBlockEntity tombstone))
            return;

        Optional<UUID> ownerIdOpt = tombstone.getOwnerUuid();
        if (ownerIdOpt.isEmpty()) {
            clicker.sendSystemMessage(Component.literal("This tombstone has no owner."));
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        UUID ownerId = ownerIdOpt.get();
        var server = level.getServer();
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);

        // Resolve owner's name even if offline (null-safe cache access)
        String ownerName;
        if (owner != null) {
            ownerName = owner.getGameProfile().getName();
        } else {
            var cache = server.getProfileCache();
            ownerName = cache != null ? cache.get(ownerId).map(GameProfile::getName).orElse("Unknown") : "Unknown";
        }

        // Read deaths from scoreboard
        Scoreboard scoreboard = level.getScoreboard();
        Objective objective = scoreboard.getObjective("deaths");
        Integer deaths = null;
        if (objective != null) {
            try {
                if (owner != null) {
                    deaths = scoreboard.getOrCreatePlayerScore(owner, objective).get();
                } else if (!"Unknown".equals(ownerName)) {
                    var holder = ScoreHolder.forNameOnly(ownerName);
                    deaths = scoreboard.getOrCreatePlayerScore(holder, objective).get();
                }
            } catch (Exception ignored) {
            }
        }

        Component advice;
        if (deaths != null) {
            ItemStack required;
            if (deaths <= 1) {
                required = new ItemStack(ModItems.BANANA.get());
            } else if (deaths == 2) {
                required = new ItemStack(ModItems.DIAMOND_BANANA.get());
            } else if (deaths == 3) {
                required = new ItemStack(ModItems.HARDENED_DIAMOND_BANANA.get());
            } else if (deaths == 4) {
                required = new ItemStack(ModItems.NETHERITE_BANANA.get());
            } else {
                required = new ItemStack(ModItems.DRAGON_BANANA.get());
            }
            advice = Component.literal("To revive ")
                              .append(Component.literal(ownerName).withStyle(ChatFormatting.GOLD))
                              .append(Component.literal(" (deaths: " + deaths + "), use: "))
                              .append(required.getHoverName().copy().withStyle(ChatFormatting.YELLOW));
        } else {
            advice = Component.literal("Tomb of ")
                              .append(Component.literal(ownerName).withStyle(ChatFormatting.GOLD))
                              .append(Component.literal(": use Banana (1), Diamond Banana (2), Hardened Diamond Banana (3), Netherite Banana (4), Dragon Banana (5+)."));
        }

        clicker.sendSystemMessage(advice);
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

        // Master level (5) trade: Farmer sells 1 Durian for 30-60 emeralds, very limited use, solid XP
        List<VillagerTrades.ItemListing> level5 = trades.computeIfAbsent(5, k -> new ArrayList<>());
        level5.add((entity, random) -> {
            // 25% chance to appear
            if (random.nextFloat() >= 0.25f)
                return null;

            int emeraldCost = 30 + random.nextInt(31); // 30-60 emeralds
            ItemCost price = new ItemCost(Items.EMERALD, emeraldCost);
            ItemStack result = new ItemStack(ModItems.DURIAN.get(), 1);
            int maxUses = 3;  // limited stock
            int xp = 30;      // solid XP for a master-level premium trade
            float priceMultiplier = 0.02f; // lower demand-based fluctuation for high-cost item
            return new MerchantOffer(price, result, maxUses, xp, priceMultiplier);
        });
    }
}

