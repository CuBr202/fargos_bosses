package com.cubr.fargo_bosses.fargo_bosses;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = FargoBosses.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue ROTATING_SOUL_SHIELD_PARTICLE_INTERVAL = BUILDER
            .comment("Client-side tick interval between Rotating Soul Shield particle bursts. Higher values mean fewer particles.")
            .defineInRange("rotatingSoulShieldParticleInterval", 5, 1, 200);

    private static final ForgeConfigSpec.IntValue ROTATING_SOUL_SHIELD_PARTICLES_PER_BURST = BUILDER
            .comment("Client-side ghostly soul particles emitted by each Rotating Soul Shield burst.")
            .defineInRange("rotatingSoulShieldParticlesPerBurst", 1, 0, 10);

    private static final Map<String, ForgeConfigSpec.ConfigValue<List<? extends Integer>>> MUTANT_INT_SETTINGS = new HashMap<>();
    private static final Map<String, ForgeConfigSpec.ConfigValue<List<? extends Double>>> MUTANT_DOUBLE_SETTINGS = new HashMap<>();

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
        ABOMINATION_RESTRICTION_RADIUS = doubleList("Restriction radius", "Restriction Radius", 20, 20, 18);
        BUILDER.pop();
    }


    static {
        BUILDER.push("Mutant Boss");
        mutantIntList("HP", 500, 750, 1000);
        mutantIntList("NORMAL_ATTACK_DAMAGE", 44, 66, 99);
        mutantIntList("RESTRICTION_BOUNDARY_DAMAGE", 66, 99, 132);
        mutantDoubleList("GREAT_LASER_DAMAGE", 99, 132, 198);
        mutantIntList("TRANSITION_TIME", 40, 40, 40);
        mutantIntList("RESTRICTION_BOUNDARY_SHIELDS", 24, 24, 24);
        mutantIntList("RESTRICTION_BOUNDARY_RADIUS", 25, 25, 20);
        mutantIntList("RESTRICTION_BOUNDARY_ORBIT_TICKS", 160, 160, 160);
        mutantIntList("MAGNETIC_ATTACK_DURATION", 150, 200, 200);
        mutantIntList("MAGNETIC_ATTACK_PREPARE_TIME", 50, 30, 10);
        mutantIntList("MAGNETIC_WEAPON_HEIGHT", 1, 1, 1);
        mutantIntList("MAGNETIC_WEAPON_SPAWN_INTERVAL", 15, 10, 8);
        mutantIntList("MAGNETIC_WEAPONS_PER_SPAWN", 1, 2, 3);
        mutantDoubleList("MAGNETIC_WEAPON_SPAWN_OFFSET", 2.0, 2.0, 2.0);
        mutantDoubleList("MAGNETIC_WEAPON_SPEED", 0.5, 0.65, 0.8);
        mutantDoubleList("MAGNETIC_PLAYER_PULL_PER_BLOCK", 0.002, 0.003, 0.004);
        mutantDoubleList("MAGNETIC_PLAYER_MAX_PULL", 0.08, 0.12, 0.16);
        mutantIntList("WATER_ATTACK_PREPARE_TIME", 50, 30, 20);
        mutantIntList("WATER_DASH_COUNT", 3, 4, 6);
        mutantIntList("WATER_DASH_DURATION", 10, 10, 8);
        mutantIntList("WATER_DASH_RECOVER_TIME", 20, 12, 7);
        mutantDoubleList("WATER_DASH_SPEED", 2.0, 2.5, 3.0);
        mutantDoubleList("WATER_DASH_CONTACT_RANGE", 1.0, 1.0, 1.0);
        mutantIntList("WATER_WAVE_INTERVAL", 3, 2, 2);
        mutantIntList("WATER_WAVE_LIFESPAN", 8, 10, 12);
        mutantDoubleList("WATER_WAVE_SCALE", 1.0, 1.0, 1.0);
        mutantDoubleList("WATER_WAVE_SIDE_ANGLE", 60.0, 60.0, 60.0);
        mutantDoubleList("WATER_WAVE_SIDE_OFFSET", 0.5, 0.5, 0.5);
        mutantDoubleList("WATER_RETURN_SPEED", 1.5, 1.5, 1.5);
        mutantIntList("PEPPERMINT_ATTACK_DURATION", 240, 300, 360);
        mutantIntList("PEPPERMINT_ATTACK_PREPARE_TIME", 50, 30, 10);
        mutantIntList("PEPPERMINT_SMALL_RING_COUNT", 3, 3, 3);
        mutantDoubleList("PEPPERMINT_SMALL_RING_RADIUS", 1, 2, 3);
        mutantIntList("PEPPERMINT_LARGE_RING_COUNT", 9, 9, 9);
        mutantDoubleList("PEPPERMINT_LARGE_RING_RADIUS", 15, 13, 11);
        mutantDoubleList("PEPPERMINT_SPIN_SPEED", 7.0, 7.0, 7.0);
        mutantIntList("PEPPERMINT_STRAIGHT_ROUNDS", 6, 9, 12);
        mutantIntList("PEPPERMINT_STRAIGHT_INTERVAL", 50, 40, 30);
        mutantIntList("PEPPERMINT_STRAIGHT_COUNT", 8, 10, 13);
        mutantDoubleList("PEPPERMINT_STRAIGHT_SPACING", 7.0, 6.0, 5.0);
        mutantDoubleList("PEPPERMINT_STRAIGHT_START_SHIFT", 1.0, 1.0, 1.0);
        mutantDoubleList("PEPPERMINT_HIT_RANGE", 0.5, 0.5, 0.5);
        mutantDoubleList("PEPPERMINT_FOLLOW_SPEED", 0.75, 1.0, 1.25);
        mutantIntList("DINOSAUR_ATTACK_PREPARE_TIME", 60, 40, 20);
        mutantIntList("DINOSAUR_THROW_COUNT", 3, 3, 3);
        mutantDoubleList("DINOSAUR_MIN_TOSS_ANGLE_DIFFERENCE", 120.0, 120.0, 120.0);
        mutantDoubleList("DINOSAUR_TOSS_HEIGHT", 6.0, 6.0, 9.0);
        mutantDoubleList("DINOSAUR_TOSS_DISTANCE", 12.0, 12.0, 18.0);
        mutantDoubleList("DINOSAUR_TOSS_MOVE_SPEED", 1.0, 1.0, 2.0);
        mutantDoubleList("DINOSAUR_TOSS_POSITION_TOLERANCE", 2.0, 2.0, 2.0);
        mutantDoubleList("DINOSAUR_SPEAR_SPEED", 1.5, 2.5, 4.0);
        mutantIntList("DINOSAUR_SPEAR_WINDUP_TIME", 30, 20, 10);
        mutantIntList("DINOSAUR_SPEAR_MAX_FLIGHT_TIME", 100, 100, 100);
        mutantDoubleList("DINOSAUR_RETURN_SPEED", 3.0, 3.0, 3.0);
        mutantIntList("ARROW_ATTACK_POSITIONS", 3, 4, 5);
        mutantIntList("ARROW_ATTACK_DISTANCE", 22, 17, 17);
        mutantIntList("ARROW_ATTACK_Y_OFFSET", 1, 1, 1);
        mutantIntList("ARROW_ATTACK_VOLLEYS_PER_POSITION", 3, 3, 3);
        mutantIntList("ARROW_ATTACK_ARROWS_PER_VOLLEY", 10, 15, 12);
        mutantIntList("ARROW_ATTACK_PREPARE_TIME", 50, 40, 30);
        mutantIntList("ARROW_ATTACK_VOLLEY_INTERVAL", 2, 1, 1);
        mutantIntList("ARROW_ATTACK_SPREAD_DEGREES", 45, 37, 30);
        mutantDoubleList("ARROW_ATTACK_SPEED", 3.0, 3.0, 4.0);
        mutantDoubleList("ARROW_ATTACK_INACCURACY", 0.0, 0.0, 0.0);
        mutantIntList("ARROW_ATTACK_RECOVERY_TIME", 20, 20, 20);
        mutantIntList("ARROW_ATTACK_DIFFICULTY_TWO_REST_TIME", 0, 0, 30);
        mutantIntList("ARROW_ATTACK_DRUM_ROUNDS", 3, 3, 2);
        mutantIntList("ARROW_ATTACK_DRUM_INTERVAL", 120, 80, 80);
        mutantIntList("ARROW_ATTACK_DRUM_COUNT", 4, 4, 4);
        mutantDoubleList("ARROW_ATTACK_DRUM_THROW_SPEED", 2.0, 1.5, 1.2);
        mutantDoubleList("ARROW_ATTACK_DRUM_THROW_HEIGHT", 0.8, 0.8, 0.8);
        mutantIntList("WATER_SPEAR_CENTER_WAVES", 6, 8, 8);
        mutantIntList("WATER_SPEAR_CENTER_SPEARS_PER_WAVE", 6, 8, 7);
        mutantIntList("WATER_SPEAR_CENTER_INTERVAL", 40, 30, 25);
        mutantIntList("WATER_SPEAR_CENTER_RANGE", 20, 15, 20);
        mutantDoubleList("WATER_SPEAR_CENTER_ACCELERATION", 1.0, 1.0, 1.5);
        mutantIntList("WATER_SPEAR_PARALLEL_WAVES", 8, 12, 24);
        mutantIntList("WATER_SPEAR_PARALLEL_SPEARS_PER_WAVE", 13, 13, 13);
        mutantIntList("WATER_SPEAR_PARALLEL_INTERVAL", 30, 20, 10);
        mutantIntList("WATER_SPEAR_PARALLEL_RANGE", 15, 15, 15);
        mutantDoubleList("WATER_SPEAR_PARALLEL_SPACING", 4, 3, 2.5);
        mutantDoubleList("WATER_SPEAR_PARALLEL_MIN_ANGLE_DEGREES", 60.0, 60.0, 60.0);
        mutantDoubleList("WATER_SPEAR_PARALLEL_ACCELERATION", 1.0, 1.0, 1.5);
        mutantIntList("WATER_SPEAR_PARALLEL_PREDICTION_INTERVAL", 2, 2, 2);
        mutantIntList("WATER_SPEAR_PARALLEL_PREDICTION_POINTS", 30, 30, 30);
        mutantIntList("WATER_SPEAR_PARALLEL_PREDICTION_LENGTH", 30, 30, 30);
        mutantIntList("LASER_SPREAD_BURSTS", 30, 50, 80);
        mutantIntList("LASER_SPREAD_ARMS", 3, 4, 5);
        mutantIntList("LASER_SPREAD_INTERVAL", 3, 2, 2);
        mutantDoubleList("LASER_SPREAD_ORIGIN_HEIGHT", 0.5, 0.5, 0.5);
        mutantDoubleList("LASER_SPREAD_INITIAL_SPEED", 0.008, 0.01, 0.015);
        mutantIntList("LASER_MISSILE_MISSILE_BURSTS", 4, 8, 12);
        mutantIntList("LASER_MISSILE_MISSILE_INTERVAL", 5, 5, 4);
        mutantIntList("LASER_MISSILE_REST_TIME", 80, 60, 60);
        mutantDoubleList("LASER_MISSILE_MISSILE_SIDE_ANGLE", 45, 45, 30);
        mutantIntList("LASER_MISSILE_MISSILE_SIDE", 1, 1, 1);
        mutantDoubleList("LASER_MISSILE_ANGLE_RANDOMNESS", 0.0, 3.0, 6.0);
        mutantDoubleList("LASER_MISSILE_ORIGIN_HEIGHT", 1.5, 1.5, 1.5);
        mutantDoubleList("LASER_MISSILE_MISSILE_DAMAGE", 1.5, 1.5, 1.5);
        mutantDoubleList("LASER_MISSILE_MISSILE_SPEED", 0.8, 1.0, 1.2);
        mutantIntList("LASER_MISSILE_MISSILE_LAUNCH_TIME", 10, 10, 8);
        mutantIntList("HALBERD_ATTACK_ROUNDS", 3, 3, 4);
        mutantIntList("HALBERD_MARK_TIME", 30, 20, 10);
        mutantIntList("HALBERD_REST_TIME", 40, 20, 0);
        mutantDoubleList("HALBERD_MARK_RADIUS", 3, 3, 3);
        mutantIntList("HALBERD_MARK_PARTICLE_COUNT", 8, 8, 8);
        mutantDoubleList("HALBERD_MOVE_SPEED", 2.0, 3.0, 6.0);
        mutantDoubleList("HALBERD_SMASH_HEIGHT", 12, 10, 8);
        mutantDoubleList("HALBERD_SMASH_SPEED", 4.0, 6.0, 12.0);
        mutantDoubleList("HALBERD_SMASH_HITBOX_RADIUS", 3.0, 4.0, 5.0);
        mutantIntList("HALBERD_SPIRAL_ARMS", 6, 8, 12);
        mutantIntList("HALBERD_SPIRAL_PER_ARM", 12, 18, 24);
        mutantDoubleList("HALBERD_SPIRAL_RADIUS", 54.0, 108.0, 108.0);
        mutantIntList("HALBERD_SPIRAL_WARMUP", 0, 0, 0);
        mutantDoubleList("HALBERD_ARENA_EDGE_PADDING", 4.0, 4.0, 4.0);
        mutantDoubleList("HALBERD_MIN_MARK_ANGLE_DIFFERENCE", 120.0, 120.0, 120.0);
        mutantIntList("VOID_RUNE_ATTACK_PREPARE_TIME", 60, 40, 10);
        mutantIntList("VOID_RUNE_ATTACK_ROUNDS", 40, 60, 120);
        mutantIntList("VOID_RUNE_ATTACK_INTERVAL", 3, 2, 2);
        mutantIntList("VOID_RUNE_ROTATION_PERIOD", 23, 23, 37);
        mutantIntList("VOID_RUNE_WARMUP", 0, 0, 0);
        mutantIntList("ABYSSAL_MINE_COUNT", 30, 40, 50);
        mutantDoubleList("ABYSSAL_MINE_ARENA_PADDING", 2.0, 2.0, 2.0);
        mutantIntList("ABYSSAL_MINE_WARMUP", 0, 0, 0);
        mutantDoubleList("ABYSSAL_INITIAL_ANGLE_DEGREES", 30, 30, 30);
        mutantIntList("ABYSSAL_PREDICTION_TIME", 60, 40, 20);
        mutantIntList("ABYSSAL_PREDICTION_PARTICLES", 30, 30, 30);
        mutantDoubleList("ABYSSAL_PREDICTION_PARTICLE_SPACING", 1.0, 1.0, 1.0);
        mutantDoubleList("ABYSSAL_PREDICTION_WALL_HEIGHT", 5.0, 5.0, 5.0);
        mutantDoubleList("ABYSSAL_PREDICTION_WALL_VERTICAL_SPACING", 1.0, 1.0, 1.0);
        mutantIntList("ABYSSAL_PREDICTION_WALL_REFRESH_INTERVAL", 2, 2, 2);
        mutantIntList("ABYSSAL_LASER_ROTATION_TIME", 150, 200, 300);
        mutantDoubleList("ABYSSAL_LASER_TURN_DEGREES_PER_SECOND", 36.0, 54.0, 72.0);
        mutantDoubleList("ABYSSAL_LASER_DAMAGE", 66, 132, 198);
        mutantDoubleList("ABYSSAL_LASER_HP_DAMAGE", 0.0, 0.0, 0.0);
        mutantIntList("FLARE_ATTACK_BOMBS", 8, 12, 16);
        mutantIntList("FLARE_ATTACK_INTERVAL", 30, 20, 15);
        mutantDoubleList("FLARE_STRAFE_RADIUS", 12.0, 12.0, 16.0);
        mutantDoubleList("FLARE_STRAFE_ANGULAR_SPEED", 0.06, 0.08, 0.1);
        mutantDoubleList("FLARE_STRAFE_MOVE_SPEED", 0.8, 1.0, 1.2);
        mutantDoubleList("FLARE_BOMB_SPEED", 1.0, 1.25, 1.5);
        mutantDoubleList("FLARE_BOMB_INACCURACY", 0.0, 0.0, 0.5);
        mutantIntList("FLARE_BOMB_JET_DIRECTIONS", 4, 6, 8);
        mutantIntList("FLARE_BOMB_JETS_PER_DIRECTION", 8, 12, 24);
        mutantDoubleList("FLARE_BOMB_JET_SPACING", 1.0, 1.0, 1.0);
        mutantIntList("FLARE_BOMB_JET_DELAY_PER_BLOCK", 2, 2, 1);
        mutantIntList("IGNIS_WAVE_INTERVAL", 60, 40, 20);
        mutantIntList("IGNIS_FALLING_BLOCK_DURATION", 10, 10, 10);
        mutantDoubleList("IGNIS_FALLING_BLOCK_UPWARD_SPEED", 0.2, 0.2, 0.2);
        mutantDoubleList("IGNIS_FALLING_BLOCK_UPWARD_VARIANCE", 0.15, 0.15, 0.15);
        mutantIntList("IGNIS_FLAME_STRIKE_DURATION", 80, 60, 60);
        mutantIntList("IGNIS_FLAME_STRIKE_WAIT_TIME", 0, 0, 0);
        mutantIntList("IGNIS_FLAME_STRIKE_DELAY", 0, 0, 0);
        mutantDoubleList("IGNIS_FLAME_STRIKE_RADIUS", 3.0, 3.0, 5.0);
        mutantDoubleList("IGNIS_FLAME_STRIKE_HP_DAMAGE", 0.0, 0.0, 0.0);
        mutantIntList("SANDSTORM_ATTACK_DURATION", 180, 240, 240);
        mutantIntList("SANDSTORM_COUNT", 2, 3, 3);
        mutantDoubleList("SANDSTORM_RADIUS", 8.0, 8.0, 8.0);
        mutantDoubleList("SANDSTORM_ORBIT_SPEED", 0.02, 0.04, 0.08);
        mutantIntList("SANDSTORM_VOID_VORTEX_COUNT", 6, 8, 8);
        mutantDoubleList("SANDSTORM_FOLLOW_SPEED", 0.8, 1.0, 1.0);
        mutantIntList("NORMAL_SHOOTING_INTERVAL", 4, 2, 1);
        mutantIntList("NORMAL_SHOOTING_PREPARE_TIME", 60, 40, 10);
        mutantIntList("NORMAL_SHOOTING_MIN_SHOTS", 15, 30, 60);
        mutantIntList("NORMAL_SHOOTING_MAX_SHOTS", 20, 40, 80);
        mutantDoubleList("NORMAL_SHOOTING_LASER_INITIAL_SPEED", 0.1, 0.2, 0.3);
        mutantIntList("NORMAL_DASH_COUNT", 3, 3, 4);
        mutantIntList("NORMAL_DASH_WINDUP", 20, 10, 5);
        mutantIntList("NORMAL_DASH_DURATION", 10, 10, 10);
        mutantIntList("NORMAL_DASH_RECOVER", 20, 10, 5);
        mutantDoubleList("NORMAL_DASH_SPEED", 1.5, 2, 2);
        mutantIntList("NORMAL_DASH_HOLD_SPEED", 1, 1, 1);
        mutantIntList("NORMAL_DASH_CONTACT_RANGE", 1, 1, 1);
        mutantDoubleList("NORMAL_DASH_RECOVER_SLOWDOWN", 0.6, 0.6, 0.6);
        mutantIntList("NORMAL_DASH_TRAIL_INTERVAL", 2, 2, 2);
        mutantIntList("NORMAL_DASH_HALBERD_WARMUP", 0, 0, 0);
        mutantIntList("NORMAL_DASH_PARTICLE_COUNT", 1, 1, 1);
        mutantIntList("NORMAL_ATTACK_LOOK_LIMIT", 30, 30, 30);
        mutantIntList("NORMAL_PROJECTILE_HEIGHT", 1, 1, 1);
        mutantIntList("HOWITZER_ATTACK_PREPARE_TIME", 60, 40, 10);
        mutantDoubleList("HOWITZER_ATTACK_SPEED", 1, 1.5, 2);
        mutantDoubleList("HOWITZER_ATTACK_INACCURACY", 0, 0, 0);
        mutantIntList("PHASE_THREE_PREPARE_TIME", 60, 40, 20);
        mutantIntList("PHASE_FOUR_PHANTOM_ARROW_WAVES", 3, 4, 4);
        mutantIntList("PHASE_FOUR_PHANTOM_ARROW_ARROWS_PER_GROUP", 18, 30, 42);
        mutantIntList("PHASE_FOUR_PHANTOM_ARROW_WARNING_TIME", 20, 15, 15);
        mutantIntList("PHASE_FOUR_PHANTOM_ARROW_FIRST_GROUP_DELAY", 18, 12, 6);
        mutantIntList("PHASE_FOUR_PHANTOM_ARROW_GROUP_INTERVAL", 8, 6, 6);
        mutantIntList("PHASE_FOUR_PHANTOM_ARROW_NEXT_WAVE_DELAY", 18, 12, 6);
        mutantIntList("PHASE_FOUR_PHANTOM_ARROW_WARNING_POINTS", 20, 20, 15);
        mutantDoubleList("PHASE_FOUR_PHANTOM_ARROW_WARNING_SPACING", 1.0, 1.0, 1.0);
        mutantDoubleList("PHASE_FOUR_PHANTOM_ARROW_SPAWN_RADIUS", 1.5, 1.5, 1.5);
        mutantDoubleList("PHASE_FOUR_PHANTOM_ARROW_SPAWN_HEIGHT", 1.0, 1.0, 1.0);
        mutantDoubleList("PHASE_FOUR_PHANTOM_ARROW_SPEED", 8.0, 8.0, 8.0);
        mutantIntList("PHASE_FOUR_ATTACK_PREPARE_TIME", 60, 40, 20);
        mutantIntList("PHASE_FOUR_SUPER_WATER_SPEAR_ROUNDS", 6, 8, 12);
        mutantIntList("PHASE_FOUR_SUPER_WATER_SPEAR_INTERVAL", 30, 20, 10);
        mutantIntList("FINAL_SPARK_PEPPERMINT_INTERVAL", 60, 45, 30);
        mutantIntList("FINAL_SPARK_PEPPERMINT_COUNT", 6, 6, 6);
        mutantDoubleList("FINAL_SPARK_PEPPERMINT_SPIN_SPEED", 2.0, 3.0, 4.0);
        mutantDoubleList("FINAL_SPARK_PEPPERMINT_RADIUS_GROWTH", 0.15, 0.2, 0.25);
        mutantIntList("FINAL_SPARK_FIRST_LASER_WAIT_TIME", 60, 40, 20);
        mutantIntList("FINAL_SPARK_FIRST_LASER_TURN_TIME", 200, 200, 200);
        mutantDoubleList("FINAL_SPARK_FIRST_LASER_TURN_DEGREES_PER_SECOND", 27, 36, 54);
        mutantIntList("FINAL_SPARK_SECOND_LASER_WAIT_TIME", 60, 40, 20);
        mutantIntList("FINAL_SPARK_SECOND_LASER_TURN_TIME", 200, 400, 600);
        mutantDoubleList("FINAL_SPARK_SECOND_LASER_TURN_DEGREES_PER_SECOND", 36, 54, 72);
        mutantDoubleList("FINAL_SPARK_LASER_HP_DAMAGE", 0.0, 0.0, 0.0);
        mutantIntList("FINAL_SPARK_LASER_LIFESPAN_BUFFER", 20, 20, 20);
        mutantIntList("MUTANT_LEVIATHAN_LASER_PORTAL_LIFESPAN_BUFFER", 40, 40, 40);
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

    public static int mutantInt(String key, int difficulty) {
        ForgeConfigSpec.ConfigValue<List<? extends Integer>> value = MUTANT_INT_SETTINGS.get(key);
        if (value == null) throw new IllegalArgumentException("Unknown Mutant integer setting: " + key);
        List<? extends Integer> values = value.get();
        return values.get(Math.min(Math.max(difficulty, 0), values.size() - 1));
    }

    public static double mutantDouble(String key, int difficulty) {
        ForgeConfigSpec.ConfigValue<List<? extends Double>> value = MUTANT_DOUBLE_SETTINGS.get(key);
        if (value == null) throw new IllegalArgumentException("Unknown Mutant decimal setting: " + key);
        List<? extends Double> values = value.get();
        return values.get(Math.min(Math.max(difficulty, 0), values.size() - 1));
    }

    private static void mutantIntList(String key, Integer... values) {
        String name = mutantSettingName(key);
        String comment = key.equals("HP") ? "Boss HP" : name;
        MUTANT_INT_SETTINGS.put(key, intList(comment, name, values));
    }

    private static void mutantDoubleList(String key, Number... values) {
        String name = mutantSettingName(key);
        MUTANT_DOUBLE_SETTINGS.put(key, doubleList(name, name, values));
    }

    private static String mutantSettingName(String key) {
        StringBuilder name = new StringBuilder();
        for (String word : key.split("_")) {
            if (!name.isEmpty()) name.append(' ');
            if (word.length() <= 2) name.append(word);
            else name.append(word.charAt(0)).append(word.substring(1).toLowerCase());
        }
        return name.toString();
    }

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

        rotatingSoulShieldParticleInterval = ROTATING_SOUL_SHIELD_PARTICLE_INTERVAL.get();
        rotatingSoulShieldParticlesPerBurst = ROTATING_SOUL_SHIELD_PARTICLES_PER_BURST.get();
    }
}
