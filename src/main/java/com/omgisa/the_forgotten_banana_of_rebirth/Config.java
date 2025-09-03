package com.omgisa.the_forgotten_banana_of_rebirth;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

@EventBusSubscriber(modid = TheForgottenBananaOfRebirth.MOD_ID)
public class Config {
    // COMMON spec (client/server-agnostic): lightweight, e.g., visual/audio defaults shared
    public static final ModConfigSpec COMMON_SPEC;
    private static final Common COMMON;

    // SERVER spec (per-world authoritative gameplay values)
    public static final ModConfigSpec SERVER_SPEC;
    private static final Server SERVER;

    static {
        Pair<Common, ModConfigSpec> commonConfigured = new ModConfigSpec.Builder().configure(Common::new);
        COMMON = commonConfigured.getLeft();
        COMMON_SPEC = commonConfigured.getRight();

        Pair<Server, ModConfigSpec> serverConfigured = new ModConfigSpec.Builder().configure(Server::new);
        SERVER = serverConfigured.getLeft();
        SERVER_SPEC = serverConfigured.getRight();
    }

    // Cached COMMON values (with sane defaults)
    public static double heartbeatVolumeMin = 0.15D;
    public static double heartbeatVolumeMax = 1.0D;
    public static double heartbeatPitch = 1.0D;

    // Cached SERVER values (with sane defaults)
    public static int heartbeatIntervalMinTicks = 10;
    public static int heartbeatIntervalMaxTicks = 40;
    public static double lowHealthThreshold = 3.0D;
    public static double minMaxHealthHearts = 5.0D;
    public static boolean requireHardcoreForDeathLogic = true;
    public static int darknessDurationTicks = 60;
    // New: Cap for max hearts achievable via permanent increases
    public static double maxHeartsCapHearts = 20.0D;

    // Handle COMMON config load/reload
    @SubscribeEvent
    static void onCommonLoad(final ModConfigEvent event) {
        if (event.getConfig() == null || event.getConfig().getSpec() != COMMON_SPEC) {
            return;
        }
        heartbeatVolumeMin = COMMON.HEARTBEAT_VOLUME_MIN.get();
        heartbeatVolumeMax = COMMON.HEARTBEAT_VOLUME_MAX.get();
        if (heartbeatVolumeMin > heartbeatVolumeMax) {
            double t = heartbeatVolumeMin;
            heartbeatVolumeMin = heartbeatVolumeMax;
            heartbeatVolumeMax = t;
        }
        heartbeatPitch = COMMON.HEARTBEAT_PITCH.get();
    }

    // Handle SERVER config load/reload
    @SubscribeEvent
    static void onServerLoad(final ModConfigEvent event) {
        if (event.getConfig() == null || event.getConfig().getSpec() != SERVER_SPEC) {
            return;
        }
        heartbeatIntervalMinTicks = SERVER.HEARTBEAT_INTERVAL_MIN_TICKS.get();
        heartbeatIntervalMaxTicks = SERVER.HEARTBEAT_INTERVAL_MAX_TICKS.get();
        if (heartbeatIntervalMinTicks > heartbeatIntervalMaxTicks) {
            int t = heartbeatIntervalMinTicks;
            heartbeatIntervalMinTicks = heartbeatIntervalMaxTicks;
            heartbeatIntervalMaxTicks = t;
        }
        lowHealthThreshold = SERVER.LOW_HEALTH_THRESHOLD_HEARTS.get(); // hearts
        minMaxHealthHearts = SERVER.MIN_MAX_HEALTH_HEARTS.get();
        requireHardcoreForDeathLogic = SERVER.REQUIRE_HARDCORE_FOR_DEATH_LOGIC.get();
        darknessDurationTicks = SERVER.DARKNESS_DURATION_TICKS.get();
        maxHeartsCapHearts = SERVER.MAX_HEARTS_CAP_HEARTS.get();
    }

    // Holder for COMMON config entries
    public static class Common {
        public final ModConfigSpec.DoubleValue HEARTBEAT_VOLUME_MIN;
        public final ModConfigSpec.DoubleValue HEARTBEAT_VOLUME_MAX;
        public final ModConfigSpec.DoubleValue HEARTBEAT_PITCH;

        public Common(ModConfigSpec.Builder builder) {
            builder.push("common").comment("Common Settings");
            HEARTBEAT_VOLUME_MIN = builder.comment("Minimum volume for low-health heartbeat (0.0-1.0). Default: 0.15")
                                          .defineInRange("heartbeatVolumeMin", 0.15D, 0.0D, 1.0D);
            HEARTBEAT_VOLUME_MAX = builder.comment("Maximum volume for low-health heartbeat (0.0-1.0). Default: 1.0")
                                          .defineInRange("heartbeatVolumeMax", 1.0D, 0.0D, 1.0D);
            HEARTBEAT_PITCH = builder.comment("Pitch for low-health heartbeat sound (0.5-2.0). Default: 1.0")
                                     .defineInRange("heartbeatPitch", 1.0D, 0.5D, 2.0D);
            builder.pop();
        }
    }

    // Holder for SERVER config entries
    public static class Server {
        public final ModConfigSpec.IntValue HEARTBEAT_INTERVAL_MIN_TICKS;
        public final ModConfigSpec.IntValue HEARTBEAT_INTERVAL_MAX_TICKS;
        public final ModConfigSpec.DoubleValue LOW_HEALTH_THRESHOLD_HEARTS;
        public final ModConfigSpec.DoubleValue MIN_MAX_HEALTH_HEARTS;
        public final ModConfigSpec.BooleanValue REQUIRE_HARDCORE_FOR_DEATH_LOGIC;
        public final ModConfigSpec.IntValue DARKNESS_DURATION_TICKS;
        public final ModConfigSpec.DoubleValue MAX_HEARTS_CAP_HEARTS;

        public Server(ModConfigSpec.Builder builder) {
            builder.push("server");
            HEARTBEAT_INTERVAL_MIN_TICKS = builder.comment("Minimum interval (ticks) between heartbeats when low on health. Default: 10 (~0.5s)")
                                                  .defineInRange("heartbeatIntervalMinTicks", 10, 1, 200);
            HEARTBEAT_INTERVAL_MAX_TICKS = builder.comment("Maximum interval (ticks) at threshold. Default: 40 (~2.0s)")
                                                  .defineInRange("heartbeatIntervalMaxTicks", 40, 1, 400);
            LOW_HEALTH_THRESHOLD_HEARTS = builder.comment("Health threshold in hearts at or below which low-health effects trigger. Default: 3.0 hearts")
                                                 .defineInRange("lowHealthThresholdHearts", 3.0D, 0.0D, 40.0D);
            MIN_MAX_HEALTH_HEARTS = builder.comment("Minimum remaining max health in hearts. Default: 5.0")
                                           .defineInRange("minMaxHealthHearts", 5.0D, 1.0D, 40.0D);
            REQUIRE_HARDCORE_FOR_DEATH_LOGIC = builder.comment("If true, tombstone/heart-reduction logic runs only in Hardcore worlds. Default: true")
                                                      .define("requireHardcoreForDeathLogic", true);
            DARKNESS_DURATION_TICKS = builder.comment("Duration (ticks) for Darkness effect at low health. Default: 60t (3s)")
                                             .defineInRange("darknessDurationTicks", 60, 1, 1200);
            MAX_HEARTS_CAP_HEARTS = builder.comment("Maximum hearts a player can reach via permanent increases (e.g., Enchanted Durian). Default: 20.0 hearts")
                                           .defineInRange("maxHeartsCapHearts", 20.0D, 1.0D, 100.0D);
            builder.pop();
        }
    }
}
