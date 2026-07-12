package com.cubr.fargo_bosses.fargo_bosses;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = FargoBosses.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    private static final ForgeConfigSpec.IntValue ROTATING_SOUL_SHIELD_PARTICLE_INTERVAL = BUILDER
            .comment("Client-side tick interval between Rotating Soul Shield particle bursts. Higher values mean fewer particles.")
            .defineInRange("rotatingSoulShieldParticleInterval", 5, 1, 200);

    private static final ForgeConfigSpec.IntValue ROTATING_SOUL_SHIELD_PARTICLES_PER_BURST = BUILDER
            .comment("Client-side ghostly soul particles emitted by each Rotating Soul Shield burst.")
            .defineInRange("rotatingSoulShieldParticlesPerBurst", 1, 0, 10);

    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_HP;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_BOSS_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_BORDER_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_LASER_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Boolean>> ABOMINATION_ENABLE_SOUL_FRACTURE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_PHASE_1_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DICE_DELAY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DICE_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DICE_PHASE_1_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DICE_PHASE_2_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_CHIPS_DELAY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_CHIPS_CIRCLE_DELAY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_CHIPS_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DAGGER_WAVES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DAGGERS_PER_WAVE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DAGGER_ALIGN_TIME;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DAGGER_WAVE_INTERVAL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DAGGER_REST_TIME;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DAGGER_DRIFT_INTERVAL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_DAGGER_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_DAGGER_SPEED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DAGGER_RETURN_TICK;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DASH_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DASH_COUNT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DASH_WINDUP;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DASH_TIME;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_DASH_RECOVER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_DASH_SPEED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LINEAR_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LINEAR_WINDUP;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LINEAR_COUNT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LINEAR_INTERVAL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LINEAR_FINAL_INTERVAL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LINEAR_DISTANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_ROSE_HEALTH;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_ROSE_ARMOR;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_SUMMON_PHASE_1_COUNT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_SUMMON_PHASE_2_COUNT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_SUMMON_REST_TIME;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_SUMMON_RADIUS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_BOMB_PHASE_1_ROUNDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_BOMB_PHASE_2_ROUNDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_BOMB_MINE_COUNT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_BOMB_INTERVAL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_BOMB_DETONATION_GRACE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_BOMB_SAFETY_RADIUS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_BOMB_SAFETY_DISTANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_BOMB_BOSS_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_BOMB_BOSS_SPEED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_BOMB_SAFETY_PARTICLES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_BOMB_SAFETY_PARTICLE_INTERVAL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LASER_ALIGN_TIME;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_LASER_MOVE_SPEED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_LASER_START_REACH_DISTANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LASER_CHARGE_TIME;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LASER_SWEEP_TIME;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LASER_BEAM_END_BUFFER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LASER_REPEATS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_LASER_MAX_HEALTH_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_LASER_RING_CLEARANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_LASER_SIDE_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LASER_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LASER_THUNDER_ROUNDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_LASER_THUNDER_WARMUP;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_LASER_THUNDER_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_LASER_THUNDER_JITTER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ABOMINATION_RESTRICTION_SHIELDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> ABOMINATION_RESTRICTION_RADIUS;

    static {
        BUILDER.push("Abomination Boss");
        ABOMINATION_HP = intList("Boss HP", "HP", 200, 300, 500);
        ABOMINATION_BOSS_DAMAGE = doubleList("Damage dealt by standard Abomination attacks", "Boss Damage", 22, 33, 44);
        ABOMINATION_BORDER_DAMAGE = doubleList("Restriction boundary damage", "Border Damage", 33, 66, 88);
        ABOMINATION_LASER_DAMAGE = doubleList("Laser damage", "Laser Damage", 44, 88, 132);
        ABOMINATION_ENABLE_SOUL_FRACTURE = booleanList("Whether daggers and shields apply Soul Fracture", "Enable Soul Fracture", false, false, true);
        ABOMINATION_PHASE_1_THRESHOLD = doubleList("Phase 1 health threshold", "Phase 1 Threshold", 0.5, 2.0 / 3.0, 2.0 / 3.0);
        ABOMINATION_DICE_DELAY = intList("Dice attack delay", "Dice Delay", 30, 15, 10);
        ABOMINATION_DICE_COOLDOWN = intList("Dice attack cooldown", "Dice Cooldown", 40, 20, 0);
        ABOMINATION_DICE_PHASE_1_AMOUNT = intList("Phase 1 dice directions", "Dice Phase 1 Amount", 6, 8, 8);
        ABOMINATION_DICE_PHASE_2_AMOUNT = intList("Phase 2 dice directions", "Dice Phase 2 Amount", 8, 12, 12);
        ABOMINATION_CHIPS_DELAY = intList("Chips attack delay", "Chips Delay", 40, 20, 10);
        ABOMINATION_CHIPS_CIRCLE_DELAY = intList("Chips circle delay", "Chips Circle Delay", 20, 10, 10);
        ABOMINATION_CHIPS_COOLDOWN = intList("Chips attack cooldown", "Chips Cooldown", 40, 20, 0);
        ABOMINATION_DAGGER_WAVES = intList("Dagger waves", "Dagger Waves", 3, 4, 5);
        ABOMINATION_DAGGERS_PER_WAVE = intList("Daggers per wave", "Daggers Per Wave", 8, 12, 12);
        ABOMINATION_DAGGER_ALIGN_TIME = intList("Dagger alignment time", "Dagger Align Time", 10, 10, 10);
        ABOMINATION_DAGGER_WAVE_INTERVAL = intList("Dagger wave interval", "Dagger Wave Interval", 30, 20, 16);
        ABOMINATION_DAGGER_REST_TIME = intList("Dagger rest time", "Dagger Rest Time", 200, 160, 160);
        ABOMINATION_DAGGER_DRIFT_INTERVAL = intList("Dagger drift interval", "Dagger Drift Interval", 15, 15, 15);
        ABOMINATION_DAGGER_DAMAGE = doubleList("Dagger base damage", "Dagger Damage", 6, 6, 6);
        ABOMINATION_DAGGER_SPEED = doubleList("Dagger speed", "Dagger Speed", 0.7, 0.7, 0.6);
        ABOMINATION_DAGGER_RETURN_TICK = intList("Dagger return tick", "Dagger Return Tick", 90, 80, 70);
        ABOMINATION_DASH_COOLDOWN = intList("Dash cooldown", "Dash Cooldown", 40, 20, 0);
        ABOMINATION_DASH_COUNT = intList("Dash count", "Dash Count", 5, 5, 5);
        ABOMINATION_DASH_WINDUP = intList("Dash windup", "Dash Windup", 10, 5, 3);
        ABOMINATION_DASH_TIME = intList("Dash duration", "Dash Time", 10, 10, 10);
        ABOMINATION_DASH_RECOVER = intList("Dash recovery", "Dash Recover", 10, 5, 3);
        ABOMINATION_DASH_SPEED = doubleList("Dash speed", "Dash Speed", 1.5, 2, 2.5);
        ABOMINATION_LINEAR_AMOUNT = intList("Linear attack half-width", "Linear Amount", 5, 10, 10);
        ABOMINATION_LINEAR_WINDUP = intList("Linear attack windup", "Linear Windup", 40, 30, 20);
        ABOMINATION_LINEAR_COUNT = intList("Linear attack volley count", "Linear Count", 15, 20, 25);
        ABOMINATION_LINEAR_INTERVAL = intList("Linear attack initial interval", "Linear Interval", 40, 30, 30);
        ABOMINATION_LINEAR_FINAL_INTERVAL = intList("Linear attack final interval", "Linear Final Interval", 20, 10, 8);
        ABOMINATION_LINEAR_DISTANCE = intList("Linear attack distance", "Linear Distance", 7, 7, 7);
        ABOMINATION_ROSE_HEALTH = doubleList("Summoned Rose Spirit health", "Rose Health", 20, 40, 60);
        ABOMINATION_ROSE_ARMOR = doubleList("Summoned Rose Spirit armor", "Rose Armor", 0, 5, 5);
        ABOMINATION_SUMMON_PHASE_1_COUNT = intList("Phase 1 Rose Spirit count", "Summon Phase 1 Count", 2, 3, 3);
        ABOMINATION_SUMMON_PHASE_2_COUNT = intList("Phase 2 Rose Spirit count", "Summon Phase 2 Count", 4, 6, 6);
        ABOMINATION_SUMMON_REST_TIME = intList("Summon rest time", "Summon Rest Time", 300, 300, 300);
        ABOMINATION_SUMMON_RADIUS = doubleList("Summon radius", "Summon Radius", 3, 3, 3);
        ABOMINATION_BOMB_PHASE_1_ROUNDS = intList("Phase 1 bomb rounds", "Bomb Phase 1 Rounds", 1, 1, 1);
        ABOMINATION_BOMB_PHASE_2_ROUNDS = intList("Phase 2 bomb rounds", "Bomb Phase 2 Rounds", 2, 3, 3);
        ABOMINATION_BOMB_MINE_COUNT = intList("Mines per bomb round", "Bomb Mine Count", 12, 12, 12);
        ABOMINATION_BOMB_INTERVAL = intList("Bomb safety-circle duration", "Bomb Interval", 100, 80, 70);
        ABOMINATION_BOMB_DETONATION_GRACE = intList("Bomb detonation safety grace time", "Bomb Detonation Grace Time", 20, 20, 20);
        ABOMINATION_BOMB_SAFETY_RADIUS = doubleList("Bomb safety-circle radius", "Bomb Safety Radius", 4, 3, 2);
        ABOMINATION_BOMB_SAFETY_DISTANCE = doubleList("Minimum distance between bomb safety circles", "Bomb Safety Distance", 10, 12, 13);
        ABOMINATION_BOMB_BOSS_OFFSET = doubleList("Boss vertical offset from bomb safety circle", "Bomb Boss Offset", 2, 2, 2);
        ABOMINATION_BOMB_BOSS_SPEED = doubleList("Boss speed toward bomb safety circle", "Bomb Boss Speed", 2, 2, 2.5);
        ABOMINATION_BOMB_SAFETY_PARTICLES = intList("Safety-circle particles", "Bomb Safety Particles", 16, 16, 16);
        ABOMINATION_BOMB_SAFETY_PARTICLE_INTERVAL = intList("Safety-circle particle interval", "Bomb Safety Particle Interval", 5, 5, 5);
        ABOMINATION_LASER_ALIGN_TIME = intList("Laser alignment time", "Laser Align Time", 40, 20, 20);
        ABOMINATION_LASER_MOVE_SPEED = doubleList("Laser move speed", "Laser Move Speed", 3, 3, 3);
        ABOMINATION_LASER_START_REACH_DISTANCE = doubleList("Laser start reach distance", "Laser Start Reach Distance", 0.5, 0.5, 0.5);
        ABOMINATION_LASER_CHARGE_TIME = intList("Laser charge time", "Laser Charge Time", 20, 10, 10);
        ABOMINATION_LASER_SWEEP_TIME = intList("Laser sweep time", "Laser Sweep Time", 120, 80, 60);
        ABOMINATION_LASER_BEAM_END_BUFFER = intList("Laser beam end buffer", "Laser Beam End Buffer", 0, 0, 0);
        ABOMINATION_LASER_REPEATS = intList("Laser sweep repeats", "Laser Repeats", 2, 2, 3);
        ABOMINATION_LASER_MAX_HEALTH_DAMAGE = doubleList("Laser max-health damage fraction", "Laser Max Health Damage", 0, 0, 0);
        ABOMINATION_LASER_RING_CLEARANCE = doubleList("Laser clearance from restriction ring", "Laser Ring Clearance", 8, 8, 7);
        ABOMINATION_LASER_SIDE_OFFSET = doubleList("Laser side offset", "Laser Side Offset", 2, 2, 2);
        ABOMINATION_LASER_COOLDOWN = intList("Laser cooldown", "Laser Cooldown", 40, 20, 0);
        ABOMINATION_LASER_THUNDER_ROUNDS = intList("Lightning rows per laser", "Laser Thunder Rounds", 7, 7, 9);
        ABOMINATION_LASER_THUNDER_WARMUP = intList("Lightning warmup", "Laser Thunder Warmup", 0, 0, 0);
        ABOMINATION_LASER_THUNDER_DAMAGE = doubleList("Lightning damage", "Laser Thunder Damage", 5, 5, 5);
        ABOMINATION_LASER_THUNDER_JITTER = doubleList("Lightning position randomness", "Laser Thunder Jitter", 2, 4, 4);
        ABOMINATION_RESTRICTION_SHIELDS = intList("Restriction shield count", "Restriction Shields", 24, 24, 24);
        ABOMINATION_RESTRICTION_RADIUS = doubleList("Restriction radius", "Restriction Radius", 20, 20, 15);
        BUILDER.pop();
    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int abominationInt(ForgeConfigSpec.ConfigValue<List<? extends Integer>> value, int difficulty) {
        List<? extends Integer> values = value.get();
        return values.get(Math.min(Math.max(difficulty, 0), values.size() - 1));
    }

    public static double abominationDouble(ForgeConfigSpec.ConfigValue<List<? extends Double>> value, int difficulty) {
        List<? extends Double> values = value.get();
        return values.get(Math.min(Math.max(difficulty, 0), values.size() - 1));
    }

    public static boolean abominationBoolean(ForgeConfigSpec.ConfigValue<List<? extends Boolean>> value, int difficulty) {
        List<? extends Boolean> values = value.get();
        return values.get(Math.min(Math.max(difficulty, 0), values.size() - 1));
    }

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;
    public static int rotatingSoulShieldParticleInterval;
    public static int rotatingSoulShieldParticlesPerBurst;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    private static ForgeConfigSpec.ConfigValue<List<? extends Integer>> intList(String comment, String name, Integer... values) {
        return BUILDER.comment(comment).defineList(name, List.of(values), value -> value instanceof Integer);
    }

    private static ForgeConfigSpec.ConfigValue<List<? extends Double>> doubleList(String comment, String name, Number... values) {
        return BUILDER.comment(comment).defineList(name, java.util.Arrays.stream(values).map(Number::doubleValue).toList(), value -> value instanceof Double);
    }

    private static ForgeConfigSpec.ConfigValue<List<? extends Boolean>> booleanList(String comment, String name, Boolean... values) {
        return BUILDER.comment(comment).defineList(name, List.of(values), value -> value instanceof Boolean);
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
        rotatingSoulShieldParticleInterval = ROTATING_SOUL_SHIELD_PARTICLE_INTERVAL.get();
        rotatingSoulShieldParticlesPerBurst = ROTATING_SOUL_SHIELD_PARTICLES_PER_BURST.get();
    }
}
