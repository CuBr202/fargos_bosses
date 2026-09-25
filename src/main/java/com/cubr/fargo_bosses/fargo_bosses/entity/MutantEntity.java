package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.cubr.fargo_bosses.fargo_bosses.Config;
import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.client.sound.MutantMusic;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModEffects;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModEntities;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModSounds;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Mine_Entity;
import com.github.L_Ender.cataclysm.entity.effect.Cm_Falling_Block_Entity;
import com.github.L_Ender.cataclysm.entity.effect.Flame_Strike_Entity;
import com.github.L_Ender.cataclysm.entity.effect.Sandstorm_Entity;
import com.github.L_Ender.cataclysm.entity.effect.Void_Vortex_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Phantom_Arrow_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Void_Howitzer_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Void_Rune_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Water_Spear_Entity;
import com.github.L_Ender.cataclysm.init.ModParticle;
import com.github.L_Ender.cataclysm.init.ModItems;
import com.github.alexmodguy.alexscaves.server.entity.item.WaveEntity;
import com.github.alexmodguy.alexscaves.server.entity.item.DarkArrowEntity;
import com.github.alexmodguy.alexscaves.server.entity.item.ThrownWasteDrumEntity;
import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import lykrast.meetyourfight.entity.BossEntity;
import lykrast.meetyourfight.entity.ai.MoveFrontOfTarget;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class MutantEntity extends BossEntity implements PowerableMob {

    // Constants. DO NOT change this part.
    private static final EntityDataAccessor<Integer> MUSIC_PHASE = SynchedEntityData.defineId(MutantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BOSS_DIFFICULTY = SynchedEntityData.defineId(MutantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> POWERED = SynchedEntityData.defineId(MutantEntity.class, EntityDataSerializers.BOOLEAN);
    public static final int PHASE_1 = 0, PHASE_1_TRANSITION = 1, PHASE_2 = 2, PHASE_2_TRANSITION = 3,
            PHASE_3 = 4, PHASE_3_TRANSITION = 5, PHASE_4 = 6, DEATH_PHASE = 7;
    private static final int PHASE_TRANSITION_MIN_AGE = 20;

    private static final int ATTACK_NORMAL_SHOOTING = 0;
    private static final int ATTACK_NORMAL_DASH = 1;
    private static final int ATTACK_NORMAL_HOWITZER = 2;
    private static final int ATTACK_PHASE_TWO_MAGNETIC = 0;
    private static final int ATTACK_PHASE_TWO_WATER = 1;
    private static final int ATTACK_PHASE_TWO_PEPPERMINT = 2;
    private static final int ATTACK_PHASE_TWO_DINOSAUR = 3;
    private static final int ATTACK_PHASE_TWO_ARROW = 4;
    private static final int PHASE_TWO_ATTACK_COUNT = ATTACK_PHASE_TWO_ARROW + 1;
    private static final int ATTACK_PHASE_THREE_WATER_SPEAR_CENTER = 0;
    private static final int ATTACK_PHASE_THREE_WATER_SPEAR_PARALLEL = 1;
    private static final int ATTACK_PHASE_THREE_LASER_SPREAD = 2;
    private static final int ATTACK_PHASE_THREE_LASER_MISSILE = 3;
    private static final int ATTACK_PHASE_THREE_HALBERD = 4;
    private static final int ATTACK_PHASE_THREE_VOID_RUNE = 5;
    private static final int ATTACK_PHASE_THREE_ABYSSAL = 6;
    private static final int ATTACK_PHASE_THREE_FLARE = 7;
    private static final int ATTACK_PHASE_THREE_IGNIS = 8;
    private static final int ATTACK_PHASE_THREE_SANDSTORM = 9;
    private static final int PHASE_THREE_ATTACK_COUNT = ATTACK_PHASE_THREE_SANDSTORM + 1;
    private static final int ATTACK_PHASE_FOUR_PHANTOM_ARROW = 0;
    private static final int ATTACK_PHASE_FOUR_SUPER_WATER_SPEAR = 1;
    private static final int ATTACK_PHASE_FOUR_LASER_SPREAD = 2;
    private static final int ATTACK_PHASE_FOUR_FINAL_SPARK = 3;
    private static final int ATTACK_HISTORY_SIZE = 3;

    // Difficulty-backed settings loaded from Config.

    public int HP = 750;
    private int NORMAL_ATTACK_DAMAGE = 66;

    private int RESTRICTION_BOUNDARY_DAMAGE = 99;
    private float GREAT_LASER_DAMAGE = 132;
    private int TRANSITION_TIME = 2 * 20;
    private int RESTRICTION_BOUNDARY_SHIELDS = 24;
    private int RESTRICTION_BOUNDARY_RADIUS = 25;
    private int RESTRICTION_BOUNDARY_ORBIT_TICKS = 160;
    private int MAGNETIC_ATTACK_DURATION = 200;
    private int MAGNETIC_ATTACK_PREPARE_TIME = 30;
    private int MAGNETIC_ATTACK_DAMAGE = NORMAL_ATTACK_DAMAGE;
    private int MAGNETIC_WEAPON_HEIGHT = 1;
    private int MAGNETIC_WEAPON_SPAWN_INTERVAL = 10;
    private int MAGNETIC_WEAPONS_PER_SPAWN = 2;
    private float MAGNETIC_WEAPON_SPAWN_OFFSET = 2.0F;
    private float MAGNETIC_WEAPON_SPEED = 0.65F;
    private float MAGNETIC_PLAYER_PULL_PER_BLOCK = 0.003F;
    private float MAGNETIC_PLAYER_MAX_PULL = 0.12F;
    private static final List<Item> MAGNETIC_WEAPON_ITEMS = List.of(
            Items.IRON_SWORD, Items.IRON_AXE, Items.GOLDEN_SWORD, Items.GOLDEN_AXE,
            Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.NETHERITE_SWORD, Items.NETHERITE_AXE);
    private int WATER_ATTACK_PREPARE_TIME = 30;
    private int WATER_DASH_COUNT = 4;
    private int WATER_DASH_DURATION = 10;
    private int WATER_DASH_RECOVER_TIME = 10;
    private float WATER_DASH_SPEED = 2.5F;
    private float WATER_DASH_CONTACT_RANGE = 1.0F;
    private int WATER_WAVE_INTERVAL = 2;
    private int WATER_WAVE_LIFESPAN = 10;
    private float WATER_WAVE_SCALE = 1.0F;
    private float WATER_WAVE_SIDE_ANGLE = 60.0F;
    private float WATER_WAVE_SIDE_OFFSET = 0.5F;
    private float WATER_RETURN_SPEED = 1.5F;
    private int PEPPERMINT_ATTACK_DURATION = 300;
    private int PEPPERMINT_ATTACK_PREPARE_TIME = 30;
    private int PEPPERMINT_SMALL_RING_COUNT = 3;
    private float PEPPERMINT_SMALL_RING_RADIUS = 2.0F;
    private int PEPPERMINT_LARGE_RING_COUNT = 9;
    private float PEPPERMINT_LARGE_RING_RADIUS = 13.0F;
    private float PEPPERMINT_SPIN_SPEED = 7.0F;
    private int PEPPERMINT_STRAIGHT_ROUNDS = 10;
    private int PEPPERMINT_STRAIGHT_INTERVAL = 30;
    private int PEPPERMINT_STRAIGHT_COUNT = 3;
    private float PEPPERMINT_STRAIGHT_SPACING = 3.0F;
    private float PEPPERMINT_STRAIGHT_START_SHIFT = 1.0F;
    private float PEPPERMINT_DAMAGE = NORMAL_ATTACK_DAMAGE;
    private float PEPPERMINT_HIT_RANGE = 0.5F;
    private float PEPPERMINT_FOLLOW_SPEED = 1.0F;
    private int DINOSAUR_ATTACK_PREPARE_TIME = 40;
    private int DINOSAUR_THROW_COUNT = 3;
    private float DINOSAUR_MIN_TOSS_ANGLE_DIFFERENCE = 120.0F;
    private float DINOSAUR_TOSS_HEIGHT = 6.0F;
    private float DINOSAUR_TOSS_DISTANCE = 12.0F;
    private float DINOSAUR_TOSS_MOVE_SPEED = 1.0F;
    private float DINOSAUR_TOSS_POSITION_TOLERANCE = 2.0F;
    private float DINOSAUR_SPEAR_SPEED = 2.5F;
    private int DINOSAUR_SPEAR_WINDUP_TIME = 20;
    private int DINOSAUR_SPEAR_MAX_FLIGHT_TIME = 100;
    private float DINOSAUR_RETURN_SPEED = 3.0F;
    private int ARROW_ATTACK_POSITIONS = 4;
    private int ARROW_ATTACK_DISTANCE = 12;
    private int ARROW_ATTACK_Y_OFFSET = 1;
    private int ARROW_ATTACK_VOLLEYS_PER_POSITION = 3;
    private int ARROW_ATTACK_ARROWS_PER_VOLLEY = 3;
    private int ARROW_ATTACK_PREPARE_TIME = 30;
    private int ARROW_ATTACK_VOLLEY_INTERVAL = 5;
    private int ARROW_ATTACK_SPREAD_DEGREES = 30;
    private float ARROW_ATTACK_DAMAGE = NORMAL_ATTACK_DAMAGE;
    private float ARROW_ATTACK_SPEED = 3.0F;
    private float ARROW_ATTACK_INACCURACY = 0.0F;
    private int ARROW_ATTACK_RECOVERY_TIME = 20;
    private int ARROW_ATTACK_DIFFICULTY_TWO_REST_TIME = 30;
    private int ARROW_ATTACK_DRUM_ROUNDS = 3;
    private int ARROW_ATTACK_DRUM_INTERVAL = 80;
    private int ARROW_ATTACK_DRUM_COUNT = 4;
    private float ARROW_ATTACK_DRUM_THROW_SPEED = 1.5F;
    private float ARROW_ATTACK_DRUM_THROW_HEIGHT = 0.8F;
    private int WATER_SPEAR_CENTER_WAVES = 8;
    private int WATER_SPEAR_CENTER_SPEARS_PER_WAVE = 8;
    private int WATER_SPEAR_CENTER_INTERVAL = 30;
    private int WATER_SPEAR_CENTER_RANGE = 15;
    private float WATER_SPEAR_CENTER_DAMAGE = NORMAL_ATTACK_DAMAGE;
    private float WATER_SPEAR_CENTER_ACCELERATION = 1.0F;
    private int WATER_SPEAR_PARALLEL_WAVES = 12;
    private int WATER_SPEAR_PARALLEL_SPEARS_PER_WAVE = 13;
    private int WATER_SPEAR_PARALLEL_INTERVAL = 20;
    private int WATER_SPEAR_PARALLEL_RANGE = 15;
    private float WATER_SPEAR_PARALLEL_SPACING = 3F;
    private float WATER_SPEAR_PARALLEL_MIN_ANGLE_DEGREES = 60.0F;
    private float WATER_SPEAR_PARALLEL_DAMAGE = NORMAL_ATTACK_DAMAGE;
    private float WATER_SPEAR_PARALLEL_ACCELERATION = 1.0F;
    private int WATER_SPEAR_PARALLEL_PREDICTION_INTERVAL = 2;
    private int WATER_SPEAR_PARALLEL_PREDICTION_POINTS = 30;
    private int WATER_SPEAR_PARALLEL_PREDICTION_LENGTH = 30;
    private int LASER_SPREAD_BURSTS = 50;
    private int LASER_SPREAD_ARMS = 4;
    private int LASER_SPREAD_INTERVAL = 2;
    private float LASER_SPREAD_ORIGIN_HEIGHT = 0.5F;

    private float LASER_SPREAD_INITIAL_SPEED = 0.01F;
    private float LASER_SPREAD_DAMAGE = NORMAL_ATTACK_DAMAGE;
    private int LASER_MISSILE_MISSILE_BURSTS = 8;
    private int LASER_MISSILE_MISSILE_INTERVAL = 5;
    private int LASER_MISSILE_REST_TIME = 60;
    private float LASER_MISSILE_MISSILE_SIDE_ANGLE = 45.0F;
    private int LASER_MISSILE_MISSILE_SIDE = 1;
    private float LASER_MISSILE_ANGLE_RANDOMNESS = 3.0F;
    private float LASER_MISSILE_ORIGIN_HEIGHT = 1.5F;
    private float LASER_MISSILE_MISSILE_DAMAGE = NORMAL_ATTACK_DAMAGE; // 1.5, 1.5, 1.5
    private float LASER_MISSILE_MISSILE_SPEED = 1.0F;
    private int LASER_MISSILE_MISSILE_LAUNCH_TIME = 10;
    private int HALBERD_ATTACK_ROUNDS = 3;
    private int HALBERD_MARK_TIME = 20;
    private int HALBERD_REST_TIME = 20;
    private float HALBERD_MARK_RADIUS = 3.0F;
    private int HALBERD_MARK_PARTICLE_COUNT = 8;
    private float HALBERD_MOVE_SPEED = 3.0F;
    private float HALBERD_SMASH_HEIGHT = 10.0F;
    private float HALBERD_SMASH_SPEED = 6.0F;
    private float HALBERD_SMASH_DAMAGE = NORMAL_ATTACK_DAMAGE*2;
    private float HALBERD_SMASH_HITBOX_RADIUS = 4.0F;
    private int HALBERD_SPIRAL_ARMS = 8;
    private int HALBERD_SPIRAL_PER_ARM = 18;
    private float HALBERD_SPIRAL_RADIUS = 108.0F;
    private int HALBERD_SPIRAL_WARMUP = 0;
    private float HALBERD_SPIRAL_DAMAGE = NORMAL_ATTACK_DAMAGE;
    private float HALBERD_ARENA_EDGE_PADDING = 4.0F;
    private float HALBERD_MIN_MARK_ANGLE_DIFFERENCE = 120.0F;
    private int VOID_RUNE_ATTACK_PREPARE_TIME = 40;
    private int VOID_RUNE_ATTACK_ROUNDS = 60;
    private int VOID_RUNE_ATTACK_INTERVAL = 2;
    private int VOID_RUNE_LINE_LENGTH = RESTRICTION_BOUNDARY_RADIUS;
    private int VOID_RUNE_ROTATION_PERIOD = 23;
    private int VOID_RUNE_WARMUP = 0;
    private float VOID_RUNE_DAMAGE = NORMAL_ATTACK_DAMAGE;
    private int ABYSSAL_MINE_COUNT = 40;
    private float ABYSSAL_MINE_ARENA_PADDING = 2.0F;
    private int ABYSSAL_MINE_WARMUP = 0;
    private float ABYSSAL_INITIAL_ANGLE_DEGREES = 30.0F;
    private int ABYSSAL_PREDICTION_TIME = 40;
    private int ABYSSAL_PREDICTION_PARTICLES = 30;
    private float ABYSSAL_PREDICTION_PARTICLE_SPACING = 1.0F;
    private float ABYSSAL_PREDICTION_WALL_HEIGHT = 5.0F;
    private float ABYSSAL_PREDICTION_WALL_VERTICAL_SPACING = 1.0F;
    private int ABYSSAL_PREDICTION_WALL_REFRESH_INTERVAL = 2;
    private int ABYSSAL_LASER_ROTATION_TIME = 200;
    private float ABYSSAL_LASER_TURN_DEGREES_PER_SECOND = 54.0F;
    private float ABYSSAL_LASER_TURN_DEGREES_PER_TICK = ABYSSAL_LASER_TURN_DEGREES_PER_SECOND / 20.0F;
    private float ABYSSAL_LASER_DAMAGE = GREAT_LASER_DAMAGE; // 66, 132, 198
    private float ABYSSAL_LASER_HP_DAMAGE = 0.0F;
    private int FLARE_ATTACK_BOMBS = 12;
    private int FLARE_ATTACK_INTERVAL = 20;
    private float FLARE_STRAFE_RADIUS = 12.0F;
    private float FLARE_STRAFE_ANGULAR_SPEED = 0.08F;
    private float FLARE_STRAFE_MOVE_SPEED = 1.0F;
    private float FLARE_BOMB_SPEED = 1.25F;
    private float FLARE_BOMB_INACCURACY = 0.0F;
    public int FLARE_BOMB_JET_DIRECTIONS = 6;
    public int FLARE_BOMB_JETS_PER_DIRECTION = 12;
    public float FLARE_BOMB_JET_SPACING = 1.0F;
    public int FLARE_BOMB_JET_DELAY_PER_BLOCK = 2;
    public float FLARE_BOMB_JET_DAMAGE = NORMAL_ATTACK_DAMAGE;
    private int IGNIS_WAVE_INTERVAL = 40;
    private int IGNIS_FALLING_BLOCK_DURATION = 10;
    private float IGNIS_FALLING_BLOCK_UPWARD_SPEED = 0.2F;
    private float IGNIS_FALLING_BLOCK_UPWARD_VARIANCE = 0.15F;
    private float IGNIS_FALLING_BLOCK_DAMAGE = NORMAL_ATTACK_DAMAGE;
    private int IGNIS_FLAME_STRIKE_DURATION = 60;
    private int IGNIS_FLAME_STRIKE_WAIT_TIME = 0;
    private int IGNIS_FLAME_STRIKE_DELAY = 0;
    private float IGNIS_FLAME_STRIKE_RADIUS = 3.0F;
    private float IGNIS_FLAME_STRIKE_DAMAGE = NORMAL_ATTACK_DAMAGE;
    private float IGNIS_FLAME_STRIKE_HP_DAMAGE = 0.0F;
    private int SANDSTORM_ATTACK_DURATION = 240;
    private int SANDSTORM_COUNT = 3;
    private float SANDSTORM_RADIUS = 8.0F;
    private float SANDSTORM_ORBIT_SPEED = 0.04F;
    private int SANDSTORM_VOID_VORTEX_COUNT = 8;
    private float SANDSTORM_VOID_VORTEX_RADIUS = RESTRICTION_BOUNDARY_RADIUS * 0.75F;
    private float SANDSTORM_FOLLOW_SPEED = 1.0F;
    private float SANDSTORM_CONTACT_DAMAGE = NORMAL_ATTACK_DAMAGE;
    private int NORMAL_SHOOTING_INTERVAL = 2;
    private int NORMAL_SHOOTING_PREPARE_TIME = 40;
    private int NORMAL_SHOOTING_MIN_SHOTS = 30;
    private int NORMAL_SHOOTING_MAX_SHOTS = 40;
    private float NORMAL_SHOOTING_LASER_INITIAL_SPEED = 0.2F;
    private int NORMAL_DASH_COUNT = 3;
    private int NORMAL_DASH_WINDUP = 10;
    private int NORMAL_DASH_DURATION = 10;
    private int NORMAL_DASH_RECOVER = 10;
    private float NORMAL_DASH_SPEED = 2F;
    private int NORMAL_DASH_HOLD_SPEED = 1;
    private int NORMAL_DASH_CONTACT_RANGE = 1;
    private float NORMAL_DASH_RECOVER_SLOWDOWN = 0.6F;
    private int NORMAL_DASH_TRAIL_INTERVAL = 2;
    private int NORMAL_DASH_HALBERD_WARMUP = 0;
    private int NORMAL_DASH_PARTICLE_COUNT = 1;
    private int NORMAL_ATTACK_LOOK_LIMIT = 30;
    private int NORMAL_PROJECTILE_HEIGHT = 1;
    private int HOWITZER_ATTACK_PREPARE_TIME = 40;
    private float HOWITZER_ATTACK_SPEED = 1.5F;
    private float HOWITZER_ATTACK_INACCURACY = 0.0F;
    private int PHASE_THREE_PREPARE_TIME = 40;
    private int PHASE_FOUR_PHANTOM_ARROW_WAVES = 4;
    private int PHASE_FOUR_PHANTOM_ARROW_ARROWS_PER_GROUP = 36;
    private int PHASE_FOUR_PHANTOM_ARROW_WARNING_TIME = 15;
    private int PHASE_FOUR_PHANTOM_ARROW_FIRST_GROUP_DELAY = 12;
    private int PHASE_FOUR_PHANTOM_ARROW_GROUP_INTERVAL = 6;
    private int PHASE_FOUR_PHANTOM_ARROW_NEXT_WAVE_DELAY = 12;
    private int PHASE_FOUR_PHANTOM_ARROW_WARNING_POINTS = 10;
    private float PHASE_FOUR_PHANTOM_ARROW_WARNING_SPACING = 1.0F;
    private float PHASE_FOUR_PHANTOM_ARROW_SPAWN_RADIUS = 1.5F;
    private float PHASE_FOUR_PHANTOM_ARROW_SPAWN_HEIGHT = 1.0F;
    private float PHASE_FOUR_PHANTOM_ARROW_SPEED = 8.0F;
    private float PHASE_FOUR_PHANTOM_ARROW_DAMAGE = NORMAL_ATTACK_DAMAGE;
    private int PHASE_FOUR_ATTACK_PREPARE_TIME = 40;
    private int PHASE_FOUR_SUPER_WATER_SPEAR_ROUNDS = 8;
    private int PHASE_FOUR_SUPER_WATER_SPEAR_INTERVAL = 20;
    private int FINAL_SPARK_PEPPERMINT_INTERVAL = 45;
    private int FINAL_SPARK_PEPPERMINT_COUNT = 6;
    private float FINAL_SPARK_PEPPERMINT_SPIN_SPEED = 3.0F;
    private float FINAL_SPARK_PEPPERMINT_RADIUS_GROWTH = 0.2F;
    private int FINAL_SPARK_FIRST_LASER_WAIT_TIME = 40;
    private int FINAL_SPARK_FIRST_LASER_TURN_TIME = 200;
    private float FINAL_SPARK_FIRST_LASER_TURN_DEGREES_PER_SECOND = 36.0F;
    private int FINAL_SPARK_SECOND_LASER_WAIT_TIME = 40;
    private int FINAL_SPARK_SECOND_LASER_TURN_TIME = 400;
    private float FINAL_SPARK_SECOND_LASER_TURN_DEGREES_PER_SECOND = 54.0F;
    private float FINAL_SPARK_LASER_DAMAGE =GREAT_LASER_DAMAGE;
    private float FINAL_SPARK_LASER_HP_DAMAGE = 0.0F;
    private int FINAL_SPARK_LASER_LIFESPAN_BUFFER = 20;
    private int MUTANT_LEVIATHAN_LASER_PORTAL_LIFESPAN_BUFFER = 40;

    // End of settings.


    // Mutable variables. DO NOT change this.
    public int phase;
    private int phaseTimer;
    private int pendingActivePhase = -1;
    private int phaseStartAlignmentState;
    private double phaseStartGroundY;
    private int nextPhaseOneAttack = ATTACK_NORMAL_SHOOTING;
    private int nextPhaseTwoAttack = ATTACK_PHASE_TWO_MAGNETIC;
    private int nextPhaseThreeAttack = ATTACK_PHASE_THREE_WATER_SPEAR_CENTER;
    private int nextPhaseFourAttack = ATTACK_PHASE_FOUR_PHANTOM_ARROW;
    private final List<Integer> phaseTwoAttackHistory = new ArrayList<>();
    private final List<Integer> phaseThreeAttackHistory = new ArrayList<>();
    private boolean restrictionBoundaryCreated;
    private boolean waterArenaDetachedForPeppermint;
    private Vec3 waterArenaCenter = Vec3.ZERO;
    private boolean waitingForFinalHit;
    private boolean phaseHealthProtectionEnabled;
    private int finalHitLandingState;
    private double finalHitGroundY;

    public MutantEntity(EntityType<? extends MutantEntity> type, Level level) {
        super(type, level);
        BossBarUtil.useNotchedTwelve(this);
        moveControl = new VexMovementController(this);
        xpReward = 50000;
        phaseHealthProtectionEnabled = true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MUSIC_PHASE, PHASE_1 + 1);
        entityData.define(BOSS_DIFFICULTY, 0);
        entityData.define(POWERED, false);
    }

    public void setBossDifficulty(int difficulty) {
        entityData.set(BOSS_DIFFICULTY, Mth.clamp(difficulty, 0, 2));
        applyDifficultySettings();
        applyDifficultyHealth(false);
    }

    public int getBossDifficulty() {
        return entityData.get(BOSS_DIFFICULTY);
    }

    @Override
    public boolean isPowered() {
        return entityData.get(POWERED);
    }

    public void setPowered(boolean powered) {
        entityData.set(POWERED, powered);
        if (powered) {
            setInvulnerable(true);
        }
    }

    public int getMusicPhase() {
        return entityData.get(MUSIC_PHASE);
    }
    private int configInt(String key) {
        return Config.mutantInt(key, getBossDifficulty());
    }

    private float configFloat(String key) {
        return (float) Config.mutantDouble(key, getBossDifficulty());
    }

    private void applyDifficultySettings() {
        HP = configInt("HP");
        NORMAL_ATTACK_DAMAGE = configInt("NORMAL_ATTACK_DAMAGE");
        RESTRICTION_BOUNDARY_DAMAGE = configInt("RESTRICTION_BOUNDARY_DAMAGE");
        GREAT_LASER_DAMAGE = configFloat("GREAT_LASER_DAMAGE");
        TRANSITION_TIME = configInt("TRANSITION_TIME");
        RESTRICTION_BOUNDARY_SHIELDS = configInt("RESTRICTION_BOUNDARY_SHIELDS");
        RESTRICTION_BOUNDARY_RADIUS = configInt("RESTRICTION_BOUNDARY_RADIUS");
        RESTRICTION_BOUNDARY_ORBIT_TICKS = configInt("RESTRICTION_BOUNDARY_ORBIT_TICKS");
        MAGNETIC_ATTACK_DURATION = configInt("MAGNETIC_ATTACK_DURATION");
        MAGNETIC_ATTACK_PREPARE_TIME = configInt("MAGNETIC_ATTACK_PREPARE_TIME");
        MAGNETIC_WEAPON_HEIGHT = configInt("MAGNETIC_WEAPON_HEIGHT");
        MAGNETIC_WEAPON_SPAWN_INTERVAL = configInt("MAGNETIC_WEAPON_SPAWN_INTERVAL");
        MAGNETIC_WEAPONS_PER_SPAWN = configInt("MAGNETIC_WEAPONS_PER_SPAWN");
        MAGNETIC_WEAPON_SPAWN_OFFSET = configFloat("MAGNETIC_WEAPON_SPAWN_OFFSET");
        MAGNETIC_WEAPON_SPEED = configFloat("MAGNETIC_WEAPON_SPEED");
        MAGNETIC_PLAYER_PULL_PER_BLOCK = configFloat("MAGNETIC_PLAYER_PULL_PER_BLOCK");
        MAGNETIC_PLAYER_MAX_PULL = configFloat("MAGNETIC_PLAYER_MAX_PULL");
        WATER_ATTACK_PREPARE_TIME = configInt("WATER_ATTACK_PREPARE_TIME");
        WATER_DASH_COUNT = configInt("WATER_DASH_COUNT");
        WATER_DASH_DURATION = configInt("WATER_DASH_DURATION");
        WATER_DASH_RECOVER_TIME = configInt("WATER_DASH_RECOVER_TIME");
        WATER_DASH_SPEED = configFloat("WATER_DASH_SPEED");
        WATER_DASH_CONTACT_RANGE = configFloat("WATER_DASH_CONTACT_RANGE");
        WATER_WAVE_INTERVAL = configInt("WATER_WAVE_INTERVAL");
        WATER_WAVE_LIFESPAN = configInt("WATER_WAVE_LIFESPAN");
        WATER_WAVE_SCALE = configFloat("WATER_WAVE_SCALE");
        WATER_WAVE_SIDE_ANGLE = configFloat("WATER_WAVE_SIDE_ANGLE");
        WATER_WAVE_SIDE_OFFSET = configFloat("WATER_WAVE_SIDE_OFFSET");
        WATER_RETURN_SPEED = configFloat("WATER_RETURN_SPEED");
        PEPPERMINT_ATTACK_DURATION = configInt("PEPPERMINT_ATTACK_DURATION");
        PEPPERMINT_ATTACK_PREPARE_TIME = configInt("PEPPERMINT_ATTACK_PREPARE_TIME");
        PEPPERMINT_SMALL_RING_COUNT = configInt("PEPPERMINT_SMALL_RING_COUNT");
        PEPPERMINT_SMALL_RING_RADIUS = configFloat("PEPPERMINT_SMALL_RING_RADIUS");
        PEPPERMINT_LARGE_RING_COUNT = configInt("PEPPERMINT_LARGE_RING_COUNT");
        PEPPERMINT_LARGE_RING_RADIUS = configFloat("PEPPERMINT_LARGE_RING_RADIUS");
        PEPPERMINT_SPIN_SPEED = configFloat("PEPPERMINT_SPIN_SPEED");
        PEPPERMINT_STRAIGHT_ROUNDS = configInt("PEPPERMINT_STRAIGHT_ROUNDS");
        PEPPERMINT_STRAIGHT_INTERVAL = configInt("PEPPERMINT_STRAIGHT_INTERVAL");
        PEPPERMINT_STRAIGHT_COUNT = configInt("PEPPERMINT_STRAIGHT_COUNT");
        PEPPERMINT_STRAIGHT_SPACING = configFloat("PEPPERMINT_STRAIGHT_SPACING");
        PEPPERMINT_STRAIGHT_START_SHIFT = configFloat("PEPPERMINT_STRAIGHT_START_SHIFT");
        PEPPERMINT_HIT_RANGE = configFloat("PEPPERMINT_HIT_RANGE");
        PEPPERMINT_FOLLOW_SPEED = configFloat("PEPPERMINT_FOLLOW_SPEED");
        DINOSAUR_ATTACK_PREPARE_TIME = configInt("DINOSAUR_ATTACK_PREPARE_TIME");
        DINOSAUR_THROW_COUNT = configInt("DINOSAUR_THROW_COUNT");
        DINOSAUR_MIN_TOSS_ANGLE_DIFFERENCE = configFloat("DINOSAUR_MIN_TOSS_ANGLE_DIFFERENCE");
        DINOSAUR_TOSS_HEIGHT = configFloat("DINOSAUR_TOSS_HEIGHT");
        DINOSAUR_TOSS_DISTANCE = configFloat("DINOSAUR_TOSS_DISTANCE");
        DINOSAUR_TOSS_MOVE_SPEED = configFloat("DINOSAUR_TOSS_MOVE_SPEED");
        DINOSAUR_TOSS_POSITION_TOLERANCE = configFloat("DINOSAUR_TOSS_POSITION_TOLERANCE");
        DINOSAUR_SPEAR_SPEED = configFloat("DINOSAUR_SPEAR_SPEED");
        DINOSAUR_SPEAR_WINDUP_TIME = configInt("DINOSAUR_SPEAR_WINDUP_TIME");
        DINOSAUR_SPEAR_MAX_FLIGHT_TIME = configInt("DINOSAUR_SPEAR_MAX_FLIGHT_TIME");
        DINOSAUR_RETURN_SPEED = configFloat("DINOSAUR_RETURN_SPEED");
        ARROW_ATTACK_POSITIONS = configInt("ARROW_ATTACK_POSITIONS");
        ARROW_ATTACK_DISTANCE = configInt("ARROW_ATTACK_DISTANCE");
        ARROW_ATTACK_Y_OFFSET = configInt("ARROW_ATTACK_Y_OFFSET");
        ARROW_ATTACK_VOLLEYS_PER_POSITION = configInt("ARROW_ATTACK_VOLLEYS_PER_POSITION");
        ARROW_ATTACK_ARROWS_PER_VOLLEY = configInt("ARROW_ATTACK_ARROWS_PER_VOLLEY");
        ARROW_ATTACK_PREPARE_TIME = configInt("ARROW_ATTACK_PREPARE_TIME");
        ARROW_ATTACK_VOLLEY_INTERVAL = configInt("ARROW_ATTACK_VOLLEY_INTERVAL");
        ARROW_ATTACK_SPREAD_DEGREES = configInt("ARROW_ATTACK_SPREAD_DEGREES");
        ARROW_ATTACK_SPEED = configFloat("ARROW_ATTACK_SPEED");
        ARROW_ATTACK_INACCURACY = configFloat("ARROW_ATTACK_INACCURACY");
        ARROW_ATTACK_RECOVERY_TIME = configInt("ARROW_ATTACK_RECOVERY_TIME");
        ARROW_ATTACK_DIFFICULTY_TWO_REST_TIME = configInt("ARROW_ATTACK_DIFFICULTY_TWO_REST_TIME");
        ARROW_ATTACK_DRUM_ROUNDS = configInt("ARROW_ATTACK_DRUM_ROUNDS");
        ARROW_ATTACK_DRUM_INTERVAL = configInt("ARROW_ATTACK_DRUM_INTERVAL");
        ARROW_ATTACK_DRUM_COUNT = configInt("ARROW_ATTACK_DRUM_COUNT");
        ARROW_ATTACK_DRUM_THROW_SPEED = configFloat("ARROW_ATTACK_DRUM_THROW_SPEED");
        ARROW_ATTACK_DRUM_THROW_HEIGHT = configFloat("ARROW_ATTACK_DRUM_THROW_HEIGHT");
        WATER_SPEAR_CENTER_WAVES = configInt("WATER_SPEAR_CENTER_WAVES");
        WATER_SPEAR_CENTER_SPEARS_PER_WAVE = configInt("WATER_SPEAR_CENTER_SPEARS_PER_WAVE");
        WATER_SPEAR_CENTER_INTERVAL = configInt("WATER_SPEAR_CENTER_INTERVAL");
        WATER_SPEAR_CENTER_RANGE = configInt("WATER_SPEAR_CENTER_RANGE");
        WATER_SPEAR_CENTER_ACCELERATION = configFloat("WATER_SPEAR_CENTER_ACCELERATION");
        WATER_SPEAR_PARALLEL_WAVES = configInt("WATER_SPEAR_PARALLEL_WAVES");
        WATER_SPEAR_PARALLEL_SPEARS_PER_WAVE = configInt("WATER_SPEAR_PARALLEL_SPEARS_PER_WAVE");
        WATER_SPEAR_PARALLEL_INTERVAL = configInt("WATER_SPEAR_PARALLEL_INTERVAL");
        WATER_SPEAR_PARALLEL_RANGE = configInt("WATER_SPEAR_PARALLEL_RANGE");
        WATER_SPEAR_PARALLEL_SPACING = configFloat("WATER_SPEAR_PARALLEL_SPACING");
        WATER_SPEAR_PARALLEL_MIN_ANGLE_DEGREES = configFloat("WATER_SPEAR_PARALLEL_MIN_ANGLE_DEGREES");
        WATER_SPEAR_PARALLEL_ACCELERATION = configFloat("WATER_SPEAR_PARALLEL_ACCELERATION");
        WATER_SPEAR_PARALLEL_PREDICTION_INTERVAL = configInt("WATER_SPEAR_PARALLEL_PREDICTION_INTERVAL");
        WATER_SPEAR_PARALLEL_PREDICTION_POINTS = configInt("WATER_SPEAR_PARALLEL_PREDICTION_POINTS");
        WATER_SPEAR_PARALLEL_PREDICTION_LENGTH = configInt("WATER_SPEAR_PARALLEL_PREDICTION_LENGTH");
        LASER_SPREAD_BURSTS = configInt("LASER_SPREAD_BURSTS");
        LASER_SPREAD_ARMS = configInt("LASER_SPREAD_ARMS");
        LASER_SPREAD_INTERVAL = configInt("LASER_SPREAD_INTERVAL");
        LASER_SPREAD_ORIGIN_HEIGHT = configFloat("LASER_SPREAD_ORIGIN_HEIGHT");
        LASER_SPREAD_INITIAL_SPEED = configFloat("LASER_SPREAD_INITIAL_SPEED");
        LASER_MISSILE_MISSILE_BURSTS = configInt("LASER_MISSILE_MISSILE_BURSTS");
        LASER_MISSILE_MISSILE_INTERVAL = configInt("LASER_MISSILE_MISSILE_INTERVAL");
        LASER_MISSILE_REST_TIME = configInt("LASER_MISSILE_REST_TIME");
        LASER_MISSILE_MISSILE_SIDE_ANGLE = configFloat("LASER_MISSILE_MISSILE_SIDE_ANGLE");
        LASER_MISSILE_MISSILE_SIDE = configInt("LASER_MISSILE_MISSILE_SIDE");
        LASER_MISSILE_ANGLE_RANDOMNESS = configFloat("LASER_MISSILE_ANGLE_RANDOMNESS");
        LASER_MISSILE_ORIGIN_HEIGHT = configFloat("LASER_MISSILE_ORIGIN_HEIGHT");
        LASER_MISSILE_MISSILE_DAMAGE = configFloat("LASER_MISSILE_MISSILE_DAMAGE");
        LASER_MISSILE_MISSILE_SPEED = configFloat("LASER_MISSILE_MISSILE_SPEED");
        LASER_MISSILE_MISSILE_LAUNCH_TIME = configInt("LASER_MISSILE_MISSILE_LAUNCH_TIME");
        HALBERD_ATTACK_ROUNDS = configInt("HALBERD_ATTACK_ROUNDS");
        HALBERD_MARK_TIME = configInt("HALBERD_MARK_TIME");
        HALBERD_REST_TIME = configInt("HALBERD_REST_TIME");
        HALBERD_MARK_RADIUS = configFloat("HALBERD_MARK_RADIUS");
        HALBERD_MARK_PARTICLE_COUNT = configInt("HALBERD_MARK_PARTICLE_COUNT");
        HALBERD_MOVE_SPEED = configFloat("HALBERD_MOVE_SPEED");
        HALBERD_SMASH_HEIGHT = configFloat("HALBERD_SMASH_HEIGHT");
        HALBERD_SMASH_SPEED = configFloat("HALBERD_SMASH_SPEED");
        HALBERD_SMASH_HITBOX_RADIUS = configFloat("HALBERD_SMASH_HITBOX_RADIUS");
        HALBERD_SPIRAL_ARMS = configInt("HALBERD_SPIRAL_ARMS");
        HALBERD_SPIRAL_PER_ARM = configInt("HALBERD_SPIRAL_PER_ARM");
        HALBERD_SPIRAL_RADIUS = configFloat("HALBERD_SPIRAL_RADIUS");
        HALBERD_SPIRAL_WARMUP = configInt("HALBERD_SPIRAL_WARMUP");
        HALBERD_ARENA_EDGE_PADDING = configFloat("HALBERD_ARENA_EDGE_PADDING");
        HALBERD_MIN_MARK_ANGLE_DIFFERENCE = configFloat("HALBERD_MIN_MARK_ANGLE_DIFFERENCE");
        VOID_RUNE_ATTACK_PREPARE_TIME = configInt("VOID_RUNE_ATTACK_PREPARE_TIME");
        VOID_RUNE_ATTACK_ROUNDS = configInt("VOID_RUNE_ATTACK_ROUNDS");
        VOID_RUNE_ATTACK_INTERVAL = configInt("VOID_RUNE_ATTACK_INTERVAL");
        VOID_RUNE_ROTATION_PERIOD = configInt("VOID_RUNE_ROTATION_PERIOD");
        VOID_RUNE_WARMUP = configInt("VOID_RUNE_WARMUP");
        ABYSSAL_MINE_COUNT = configInt("ABYSSAL_MINE_COUNT");
        ABYSSAL_MINE_ARENA_PADDING = configFloat("ABYSSAL_MINE_ARENA_PADDING");
        ABYSSAL_MINE_WARMUP = configInt("ABYSSAL_MINE_WARMUP");
        ABYSSAL_INITIAL_ANGLE_DEGREES = configFloat("ABYSSAL_INITIAL_ANGLE_DEGREES");
        ABYSSAL_PREDICTION_TIME = configInt("ABYSSAL_PREDICTION_TIME");
        ABYSSAL_PREDICTION_PARTICLES = configInt("ABYSSAL_PREDICTION_PARTICLES");
        ABYSSAL_PREDICTION_PARTICLE_SPACING = configFloat("ABYSSAL_PREDICTION_PARTICLE_SPACING");
        ABYSSAL_PREDICTION_WALL_HEIGHT = configFloat("ABYSSAL_PREDICTION_WALL_HEIGHT");
        ABYSSAL_PREDICTION_WALL_VERTICAL_SPACING = configFloat("ABYSSAL_PREDICTION_WALL_VERTICAL_SPACING");
        ABYSSAL_PREDICTION_WALL_REFRESH_INTERVAL = configInt("ABYSSAL_PREDICTION_WALL_REFRESH_INTERVAL");
        ABYSSAL_LASER_ROTATION_TIME = configInt("ABYSSAL_LASER_ROTATION_TIME");
        ABYSSAL_LASER_TURN_DEGREES_PER_SECOND = configFloat("ABYSSAL_LASER_TURN_DEGREES_PER_SECOND");
        ABYSSAL_LASER_DAMAGE = configFloat("ABYSSAL_LASER_DAMAGE");
        ABYSSAL_LASER_HP_DAMAGE = configFloat("ABYSSAL_LASER_HP_DAMAGE");
        FLARE_ATTACK_BOMBS = configInt("FLARE_ATTACK_BOMBS");
        FLARE_ATTACK_INTERVAL = configInt("FLARE_ATTACK_INTERVAL");
        FLARE_STRAFE_RADIUS = configFloat("FLARE_STRAFE_RADIUS");
        FLARE_STRAFE_ANGULAR_SPEED = configFloat("FLARE_STRAFE_ANGULAR_SPEED");
        FLARE_STRAFE_MOVE_SPEED = configFloat("FLARE_STRAFE_MOVE_SPEED");
        FLARE_BOMB_SPEED = configFloat("FLARE_BOMB_SPEED");
        FLARE_BOMB_INACCURACY = configFloat("FLARE_BOMB_INACCURACY");
        FLARE_BOMB_JET_DIRECTIONS = configInt("FLARE_BOMB_JET_DIRECTIONS");
        FLARE_BOMB_JETS_PER_DIRECTION = configInt("FLARE_BOMB_JETS_PER_DIRECTION");
        FLARE_BOMB_JET_SPACING = configFloat("FLARE_BOMB_JET_SPACING");
        FLARE_BOMB_JET_DELAY_PER_BLOCK = configInt("FLARE_BOMB_JET_DELAY_PER_BLOCK");
        IGNIS_WAVE_INTERVAL = configInt("IGNIS_WAVE_INTERVAL");
        IGNIS_FALLING_BLOCK_DURATION = configInt("IGNIS_FALLING_BLOCK_DURATION");
        IGNIS_FALLING_BLOCK_UPWARD_SPEED = configFloat("IGNIS_FALLING_BLOCK_UPWARD_SPEED");
        IGNIS_FALLING_BLOCK_UPWARD_VARIANCE = configFloat("IGNIS_FALLING_BLOCK_UPWARD_VARIANCE");
        IGNIS_FLAME_STRIKE_DURATION = configInt("IGNIS_FLAME_STRIKE_DURATION");
        IGNIS_FLAME_STRIKE_WAIT_TIME = configInt("IGNIS_FLAME_STRIKE_WAIT_TIME");
        IGNIS_FLAME_STRIKE_DELAY = configInt("IGNIS_FLAME_STRIKE_DELAY");
        IGNIS_FLAME_STRIKE_RADIUS = configFloat("IGNIS_FLAME_STRIKE_RADIUS");
        IGNIS_FLAME_STRIKE_HP_DAMAGE = configFloat("IGNIS_FLAME_STRIKE_HP_DAMAGE");
        SANDSTORM_ATTACK_DURATION = configInt("SANDSTORM_ATTACK_DURATION");
        SANDSTORM_COUNT = configInt("SANDSTORM_COUNT");
        SANDSTORM_RADIUS = configFloat("SANDSTORM_RADIUS");
        SANDSTORM_ORBIT_SPEED = configFloat("SANDSTORM_ORBIT_SPEED");
        SANDSTORM_VOID_VORTEX_COUNT = configInt("SANDSTORM_VOID_VORTEX_COUNT");
        SANDSTORM_FOLLOW_SPEED = configFloat("SANDSTORM_FOLLOW_SPEED");
        NORMAL_SHOOTING_INTERVAL = configInt("NORMAL_SHOOTING_INTERVAL");
        NORMAL_SHOOTING_PREPARE_TIME = configInt("NORMAL_SHOOTING_PREPARE_TIME");
        NORMAL_SHOOTING_MIN_SHOTS = configInt("NORMAL_SHOOTING_MIN_SHOTS");
        NORMAL_SHOOTING_MAX_SHOTS = configInt("NORMAL_SHOOTING_MAX_SHOTS");
        NORMAL_SHOOTING_LASER_INITIAL_SPEED = configFloat("NORMAL_SHOOTING_LASER_INITIAL_SPEED");
        NORMAL_DASH_COUNT = configInt("NORMAL_DASH_COUNT");
        NORMAL_DASH_WINDUP = configInt("NORMAL_DASH_WINDUP");
        NORMAL_DASH_DURATION = configInt("NORMAL_DASH_DURATION");
        NORMAL_DASH_RECOVER = configInt("NORMAL_DASH_RECOVER");
        NORMAL_DASH_SPEED = configFloat("NORMAL_DASH_SPEED");
        NORMAL_DASH_HOLD_SPEED = configInt("NORMAL_DASH_HOLD_SPEED");
        NORMAL_DASH_CONTACT_RANGE = configInt("NORMAL_DASH_CONTACT_RANGE");
        NORMAL_DASH_RECOVER_SLOWDOWN = configFloat("NORMAL_DASH_RECOVER_SLOWDOWN");
        NORMAL_DASH_TRAIL_INTERVAL = configInt("NORMAL_DASH_TRAIL_INTERVAL");
        NORMAL_DASH_HALBERD_WARMUP = configInt("NORMAL_DASH_HALBERD_WARMUP");
        NORMAL_DASH_PARTICLE_COUNT = configInt("NORMAL_DASH_PARTICLE_COUNT");
        NORMAL_ATTACK_LOOK_LIMIT = configInt("NORMAL_ATTACK_LOOK_LIMIT");
        NORMAL_PROJECTILE_HEIGHT = configInt("NORMAL_PROJECTILE_HEIGHT");
        HOWITZER_ATTACK_PREPARE_TIME = configInt("HOWITZER_ATTACK_PREPARE_TIME");
        HOWITZER_ATTACK_SPEED = configFloat("HOWITZER_ATTACK_SPEED");
        HOWITZER_ATTACK_INACCURACY = configFloat("HOWITZER_ATTACK_INACCURACY");
        PHASE_THREE_PREPARE_TIME = configInt("PHASE_THREE_PREPARE_TIME");
        PHASE_FOUR_PHANTOM_ARROW_WAVES = configInt("PHASE_FOUR_PHANTOM_ARROW_WAVES");
        PHASE_FOUR_PHANTOM_ARROW_ARROWS_PER_GROUP = configInt("PHASE_FOUR_PHANTOM_ARROW_ARROWS_PER_GROUP");
        PHASE_FOUR_PHANTOM_ARROW_WARNING_TIME = configInt("PHASE_FOUR_PHANTOM_ARROW_WARNING_TIME");
        PHASE_FOUR_PHANTOM_ARROW_FIRST_GROUP_DELAY = configInt("PHASE_FOUR_PHANTOM_ARROW_FIRST_GROUP_DELAY");
        PHASE_FOUR_PHANTOM_ARROW_GROUP_INTERVAL = configInt("PHASE_FOUR_PHANTOM_ARROW_GROUP_INTERVAL");
        PHASE_FOUR_PHANTOM_ARROW_NEXT_WAVE_DELAY = configInt("PHASE_FOUR_PHANTOM_ARROW_NEXT_WAVE_DELAY");
        PHASE_FOUR_PHANTOM_ARROW_WARNING_POINTS = configInt("PHASE_FOUR_PHANTOM_ARROW_WARNING_POINTS");
        PHASE_FOUR_PHANTOM_ARROW_WARNING_SPACING = configFloat("PHASE_FOUR_PHANTOM_ARROW_WARNING_SPACING");
        PHASE_FOUR_PHANTOM_ARROW_SPAWN_RADIUS = configFloat("PHASE_FOUR_PHANTOM_ARROW_SPAWN_RADIUS");
        PHASE_FOUR_PHANTOM_ARROW_SPAWN_HEIGHT = configFloat("PHASE_FOUR_PHANTOM_ARROW_SPAWN_HEIGHT");
        PHASE_FOUR_PHANTOM_ARROW_SPEED = configFloat("PHASE_FOUR_PHANTOM_ARROW_SPEED");
        PHASE_FOUR_ATTACK_PREPARE_TIME = configInt("PHASE_FOUR_ATTACK_PREPARE_TIME");
        PHASE_FOUR_SUPER_WATER_SPEAR_ROUNDS = configInt("PHASE_FOUR_SUPER_WATER_SPEAR_ROUNDS");
        PHASE_FOUR_SUPER_WATER_SPEAR_INTERVAL = configInt("PHASE_FOUR_SUPER_WATER_SPEAR_INTERVAL");
        FINAL_SPARK_PEPPERMINT_INTERVAL = configInt("FINAL_SPARK_PEPPERMINT_INTERVAL");
        FINAL_SPARK_PEPPERMINT_COUNT = configInt("FINAL_SPARK_PEPPERMINT_COUNT");
        FINAL_SPARK_PEPPERMINT_SPIN_SPEED = configFloat("FINAL_SPARK_PEPPERMINT_SPIN_SPEED");
        FINAL_SPARK_PEPPERMINT_RADIUS_GROWTH = configFloat("FINAL_SPARK_PEPPERMINT_RADIUS_GROWTH");
        FINAL_SPARK_FIRST_LASER_WAIT_TIME = configInt("FINAL_SPARK_FIRST_LASER_WAIT_TIME");
        FINAL_SPARK_FIRST_LASER_TURN_TIME = configInt("FINAL_SPARK_FIRST_LASER_TURN_TIME");
        FINAL_SPARK_FIRST_LASER_TURN_DEGREES_PER_SECOND = configFloat("FINAL_SPARK_FIRST_LASER_TURN_DEGREES_PER_SECOND");
        FINAL_SPARK_SECOND_LASER_WAIT_TIME = configInt("FINAL_SPARK_SECOND_LASER_WAIT_TIME");
        FINAL_SPARK_SECOND_LASER_TURN_TIME = configInt("FINAL_SPARK_SECOND_LASER_TURN_TIME");
        FINAL_SPARK_SECOND_LASER_TURN_DEGREES_PER_SECOND = configFloat("FINAL_SPARK_SECOND_LASER_TURN_DEGREES_PER_SECOND");
        FINAL_SPARK_LASER_HP_DAMAGE = configFloat("FINAL_SPARK_LASER_HP_DAMAGE");
        FINAL_SPARK_LASER_LIFESPAN_BUFFER = configInt("FINAL_SPARK_LASER_LIFESPAN_BUFFER");
        MUTANT_LEVIATHAN_LASER_PORTAL_LIFESPAN_BUFFER = configInt("MUTANT_LEVIATHAN_LASER_PORTAL_LIFESPAN_BUFFER");
        MAGNETIC_ATTACK_DAMAGE = NORMAL_ATTACK_DAMAGE;
        PEPPERMINT_DAMAGE = NORMAL_ATTACK_DAMAGE;
        ARROW_ATTACK_DAMAGE = NORMAL_ATTACK_DAMAGE;
        WATER_SPEAR_CENTER_DAMAGE = NORMAL_ATTACK_DAMAGE;
        WATER_SPEAR_PARALLEL_DAMAGE = NORMAL_ATTACK_DAMAGE;
        LASER_SPREAD_DAMAGE = NORMAL_ATTACK_DAMAGE;
        HALBERD_SMASH_DAMAGE = NORMAL_ATTACK_DAMAGE * 2.0F;
        HALBERD_SPIRAL_DAMAGE = NORMAL_ATTACK_DAMAGE;
        VOID_RUNE_LINE_LENGTH = RESTRICTION_BOUNDARY_RADIUS;
        VOID_RUNE_DAMAGE = NORMAL_ATTACK_DAMAGE;
        ABYSSAL_LASER_TURN_DEGREES_PER_TICK = ABYSSAL_LASER_TURN_DEGREES_PER_SECOND / 20.0F;
        FLARE_BOMB_JET_DAMAGE = NORMAL_ATTACK_DAMAGE;
        IGNIS_FALLING_BLOCK_DAMAGE = NORMAL_ATTACK_DAMAGE;
        IGNIS_FLAME_STRIKE_DAMAGE = NORMAL_ATTACK_DAMAGE;
        SANDSTORM_VOID_VORTEX_RADIUS = RESTRICTION_BOUNDARY_RADIUS * 0.75F;
        SANDSTORM_CONTACT_DAMAGE = NORMAL_ATTACK_DAMAGE;
        PHASE_FOUR_PHANTOM_ARROW_DAMAGE = NORMAL_ATTACK_DAMAGE;
        FINAL_SPARK_LASER_DAMAGE = GREAT_LASER_DAMAGE;
    }

    private void applyDifficultyHealth(boolean refillHealth) {
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(HP);
            if (refillHealth) heal(getMaxHealth());
        }
    }

    public int getFlareBombJetDirections() {
        return FLARE_BOMB_JET_DIRECTIONS;
    }

    public int getFlareBombJetsPerDirection() {
        return FLARE_BOMB_JETS_PER_DIRECTION;
    }

    public float getFlareBombJetSpacing() {
        return FLARE_BOMB_JET_SPACING;
    }

    public int getFlareBombJetDelayPerBlock() {
        return FLARE_BOMB_JET_DELAY_PER_BLOCK;
    }

    public float getFlareBombJetDamage() {
        return FLARE_BOMB_JET_DAMAGE;
    }

    public float getWaterWaveDamage() {
        return NORMAL_ATTACK_DAMAGE;
    }

    @Override
    public void move(MoverType typeIn, Vec3 pos) {
        super.move(typeIn, pos);
        checkInsideBlocks();
    }

    @Override
    public void tick() {
        noPhysics = true;
        super.tick();
        noPhysics = false;
        setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new FinalHitWaitGoal());
        goalSelector.addGoal(1, new NormalShootingAttack(this));
        goalSelector.addGoal(2, new NormalDashAttack(this));
        goalSelector.addGoal(3, new HowitzerAttack(this));
        goalSelector.addGoal(3, new MagneticAttack(this));
        goalSelector.addGoal(4, new WaterAttack(this));
        goalSelector.addGoal(5, new PeppermintAttack(this));
        goalSelector.addGoal(6, new DinosaurAttack(this));
        goalSelector.addGoal(7, new ArrowAttack(this));
        goalSelector.addGoal(3, new WaterSpearCenterAttack(this));
        goalSelector.addGoal(3, new WaterSpearParallelAttack(this));
        goalSelector.addGoal(3, new LaserSpreadAttack(this));
        goalSelector.addGoal(3, new LaserMissileAttack(this));
        goalSelector.addGoal(3, new HalberdAttack(this));
        goalSelector.addGoal(3, new VoidRuneAttack(this));
        goalSelector.addGoal(3, new AbyssalAttack(this));
        goalSelector.addGoal(3, new FlareAttack(this));
        goalSelector.addGoal(3, new IgnisAttack(this));
        goalSelector.addGoal(3, new SandstormAttack(this));
        goalSelector.addGoal(3, new SuperWaterSpearAttack(this));
        goalSelector.addGoal(3, new FinalSparkAttack(this));
        goalSelector.addGoal(3, new PhantomArrowAttack(this));
        goalSelector.addGoal(8, new MoveFrontOfTarget(this, 1));
        goalSelector.addGoal(9, new VexMoveRandomGoal(this, 0.25));
        goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        goalSelector.addGoal(11, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200)
                .add(Attributes.FOLLOW_RANGE, 64);
    }


    public static void spawn(Player player, Level world) {
        RandomSource rand = player.getRandom();
        MutantEntity mutant = ModEntities.MUTANT.get().create(world);
        if (mutant == null) {
            return;
        }

        mutant.moveTo(player.getX() + rand.nextInt(15) - 7, player.getY() + rand.nextInt(9) - 1, player.getZ() + rand.nextInt(15) - 7, rand.nextFloat() * 360 - 180, 0);
        if (!player.getAbilities().instabuild) {
            mutant.setTarget(player);
        }
        mutant.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));

        ForgeEventFactory.onFinalizeSpawn(mutant, (ServerLevel) world, world.getCurrentDifficultyAt(mutant.blockPosition()), MobSpawnType.EVENT, null, null);
        world.addFreshEntity(mutant);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 2, 1);
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        SpawnGroupData result = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        phase = PHASE_1;
        phaseTimer = 0;
        pendingActivePhase = -1;
        phaseStartAlignmentState = 0;
        entityData.set(MUSIC_PHASE, PHASE_1 + 1);
        restrictionBoundaryCreated = false;
        waterArenaDetachedForPeppermint = false;
        nextPhaseOneAttack = ATTACK_NORMAL_SHOOTING;
        phaseTwoAttackHistory.clear();
        phaseThreeAttackHistory.clear();
        nextPhaseTwoAttack = ATTACK_PHASE_TWO_MAGNETIC;
        nextPhaseThreeAttack = ATTACK_PHASE_THREE_WATER_SPEAR_CENTER;
        nextPhaseFourAttack = ATTACK_PHASE_FOUR_PHANTOM_ARROW;
        waitingForFinalHit = false;
        finalHitLandingState = 0;
        applyDifficultySettings();
        applyDifficultyHealth(true);
        setInvulnerable(false);
        setPowered(false);
        return result;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.GENERIC_KILL)) {
            phase = DEATH_PHASE;
            pendingActivePhase = -1;
            setInvulnerable(false);
            setPowered(false);
            return super.hurt(source, amount);
        }
        if (phase == PHASE_1_TRANSITION || phase == PHASE_2_TRANSITION || phase == PHASE_3_TRANSITION
                || (phase == PHASE_4 && !waitingForFinalHit)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void setHealth(float health) {
        if (phaseHealthProtectionEnabled && !level().isClientSide && phase != DEATH_PHASE && !waitingForFinalHit) {
            if (Float.isNaN(health)) {
                health = getHealth();
            }
            if (phase == PHASE_1_TRANSITION || phase == PHASE_2_TRANSITION || phase == PHASE_3_TRANSITION
                    || phase == PHASE_4) {
                health = Math.max(health, Math.max(1.0F, getHealth()));
            }
            else if (tickCount < PHASE_TRANSITION_MIN_AGE) {
                // Keep lethal hits survivable until phase transitions are allowed.
                health = Math.max(1.0F, health);
            }
            // Check the final health, after Forge damage events, armor, and resistance.
            else if (phase == PHASE_1 && health <= getMaxHealth() * (2.0F / 3.0F)) {
                health = getMaxHealth() * (2.0F / 3.0F);
                beginPhase1Transition();
            }
            else if (phase == PHASE_2 && health <= getMaxHealth() / 3.0F) {
                health = getMaxHealth() / 3.0F;
                beginPhase2Transition();
            }
            else if (phase == PHASE_3 && health <= 1.0F) {
                health = 1.0F;
                beginPhase3Transition();
            }
        }
        super.setHealth(health);
    }

    @Override
    public void die(DamageSource source) {
        if (source.is(DamageTypes.GENERIC_KILL)) {
            phase = DEATH_PHASE;
            pendingActivePhase = -1;
            setInvulnerable(false);
            setPowered(false);
            super.setHealth(0.0F);
        }
        else if (phaseHealthProtectionEnabled && !level().isClientSide && phase != DEATH_PHASE && !waitingForFinalHit) {
            // Some weapons call die directly instead of going through hurt.
            setHealth(0.0F);
            return;
        }
        super.die(source);
    }

    @Override
    public void customServerAiStep() {
        updatePoweredState();
        if (waitingForFinalHit || tickCount < PHASE_TRANSITION_MIN_AGE) {
            super.customServerAiStep();
            return;
        }
        if (phase == PHASE_1 && getHealth() <= getMaxHealth() * (2.0F / 3.0F)) {
            beginPhase1Transition();
        }
        else if (phase == PHASE_2 && getHealth() <= getMaxHealth() / 3.0F) {
            beginPhase2Transition();
        }
        else if (phase == PHASE_3 && getHealth() <= 1.0F) {
            beginPhase3Transition();
        }

        if (phase == PHASE_1_TRANSITION || phase == PHASE_2_TRANSITION || phase == PHASE_3_TRANSITION) {
            if (pendingActivePhase >= 0) {
                updatePhaseStartAlignment();
            }
            else if (--phaseTimer <= 0) {
                finishTransition();
            }
        }
        super.customServerAiStep();
    }

    private void updatePoweredState() {
        boolean powered = phase == PHASE_1_TRANSITION || phase == PHASE_2_TRANSITION
                || phase == PHASE_3_TRANSITION || (phase == PHASE_4 && !waitingForFinalHit);
        setPowered(powered);
        setInvulnerable(powered);
    }

    private void beginPhase1Transition() {
        beginTransition(PHASE_1_TRANSITION);
    }

    private void beginPhase2Transition() {
        beginTransition(PHASE_2_TRANSITION);
    }

    private void beginPhase3Transition() {
        beginTransition(PHASE_3_TRANSITION);
    }

    private void beginTransition(int transitionPhase) {
        if (tickCount < PHASE_TRANSITION_MIN_AGE) {
            return;
        }
        phase = transitionPhase;
        phaseTimer = TRANSITION_TIME;
        pendingActivePhase = -1;
        setInvulnerable(true);
        setPowered(true);
    }

    private void finishTransition() {
        if (phase == PHASE_1_TRANSITION) {
            beginPhaseStartAlignment(PHASE_2);
        }
        else if (phase == PHASE_2_TRANSITION) {
            beginPhaseStartAlignment(PHASE_3);
        }
        else if (phase == PHASE_3_TRANSITION) {
            phase = PHASE_4;
            phaseTimer = 0;
            setHealth(1.0F);
            setInvulnerable(true);
            setPowered(true);
            entityData.set(MUSIC_PHASE, 4);
            nextPhaseFourAttack = ATTACK_PHASE_FOUR_PHANTOM_ARROW;
        }
    }

    private void beginPhaseStartAlignment(int nextPhase) {
        pendingActivePhase = nextPhase;
        phaseStartAlignmentState = 0;
        phaseTimer = 0;
        setDeltaMovement(Vec3.ZERO);
    }

    private void updatePhaseStartAlignment() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            activatePendingPhase();
            return;
        }

        if (phaseStartAlignmentState == 0) {
            if (moveToAttackHeight(target.getY())) {
                phaseStartGroundY = findGroundY();
                phaseStartAlignmentState = 1;
            }
            return;
        }

        if (moveToAttackHeight(phaseStartGroundY)) {
            activatePendingPhase();
        }
    }

    private void activatePendingPhase() {
        int nextPhase = pendingActivePhase;
        pendingActivePhase = -1;
        phaseStartAlignmentState = 0;
        phase = nextPhase;
        phaseTimer = 0;
        setHealth(getMaxHealth());
        setInvulnerable(false);
        setPowered(false);
        entityData.set(MUSIC_PHASE, nextPhase == PHASE_2 ? 2 : 3);

        if (nextPhase == PHASE_2) {
            createRestrictionBoundary();
            selectNextPhaseTwoAttack(-1);
        }
        else if (nextPhase == PHASE_3) {
            selectNextPhaseThreeAttack(-1);
        }
    }

    private void finishPhaseFourAttack() {
        if (phase != PHASE_4 || waitingForFinalHit) {
            return;
        }

        waitingForFinalHit = true;
        finalHitLandingState = 0;
        nextPhaseFourAttack = -1;
        setDeltaMovement(Vec3.ZERO);
        setInvulnerable(false);
        setPowered(false);
    }

    private void updateFinalHitLanding() {
        LivingEntity target = getTarget();
        if (finalHitLandingState == 0 && target != null && target.isAlive()) {
            if (moveToAttackHeight(target.getY())) {
                finalHitGroundY = findGroundY();
                finalHitLandingState = 1;
            }
            return;
        }

        if (finalHitLandingState <= 1) {
            if (finalHitLandingState == 0) {
                finalHitGroundY = findGroundY();
                finalHitLandingState = 1;
            }
            if (moveToAttackHeight(finalHitGroundY)) {
                finalHitLandingState = 2;
            }
            return;
        }

        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
    }

    private class FinalHitWaitGoal extends Goal {
        private FinalHitWaitGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return waitingForFinalHit;
        }

        @Override
        public boolean canContinueToUse() {
            return waitingForFinalHit;
        }

        @Override
        public void tick() {
            updateFinalHitLanding();
        }
    }

    private void createRestrictionBoundary() {
        if (level().isClientSide || restrictionBoundaryCreated) {
            return;
        }

        restrictionBoundaryCreated = true;
        float orbitSpeed = (float) (Math.PI * 2.0D / RESTRICTION_BOUNDARY_ORBIT_TICKS);
        for (int i = 0; i < RESTRICTION_BOUNDARY_SHIELDS; i++) {
            float angle = i / (float) RESTRICTION_BOUNDARY_SHIELDS * (float) (Math.PI * 2.0D);
            RotatingSoulShieldEntity shield = new RotatingSoulShieldEntity(level(), this, 0.0D,
                    RESTRICTION_BOUNDARY_RADIUS, angle, orbitSpeed, RESTRICTION_BOUNDARY_DAMAGE, true);
            shield.setManualLifetime(-1);
            shield.setOrbitAroundCaster(RESTRICTION_BOUNDARY_RADIUS, angle, orbitSpeed, 0.0D);
            shield.setPos(getX() + Math.cos(angle) * RESTRICTION_BOUNDARY_RADIUS, getY(),
                    getZ() + Math.sin(angle) * RESTRICTION_BOUNDARY_RADIUS);
            shield.faceRadiusFrom(getX(), getY(), getZ());
            level().addFreshEntity(shield);
        }
    }

    private void selectNextPhaseTwoAttack(int completedAttack) {
        nextPhaseTwoAttack = selectNextAttack(PHASE_TWO_ATTACK_COUNT, phaseTwoAttackHistory, completedAttack);
    }

    private void selectNextPhaseThreeAttack(int completedAttack) {
        nextPhaseThreeAttack = selectNextAttack(PHASE_THREE_ATTACK_COUNT, phaseThreeAttackHistory, completedAttack);
    }

    private int selectNextAttack(int attackCount, List<Integer> history, int completedAttack) {
        if (completedAttack < 0) {
            history.clear();
        }
        else {
            history.add(completedAttack);
            if (history.size() > ATTACK_HISTORY_SIZE) {
                history.remove(0);
            }
        }

        List<Integer> candidates = new ArrayList<>();
        for (int attack = 0; attack < attackCount; attack++) {
            if (!history.contains(attack)) {
                candidates.add(attack);
            }
        }
        return candidates.get(getRandom().nextInt(candidates.size()));
    }

    private double findGroundY() {
        return findGroundYAt(getX(), getY(), getZ());
    }

    private double findGroundYAt(double x, double y, double z) {
        double halfWidth = getBbWidth() * 0.5D;
        for (int blockY = Math.min(Mth.floor(y), level().getMaxBuildHeight() - 1);
             blockY >= level().getMinBuildHeight(); blockY--) {
            AABB layer = new AABB(x - halfWidth, blockY, z - halfWidth,
                    x + halfWidth, blockY + 1.0D, z + halfWidth).deflate(1.0E-5D);
            double surfaceY = Double.NEGATIVE_INFINITY;
            for (VoxelShape shape : level().getBlockCollisions(this, layer)) {
                surfaceY = Math.max(surfaceY, shape.max(Direction.Axis.Y));
            }
            if (surfaceY != Double.NEGATIVE_INFINITY) {
                return clearGroundPlacement(x, surfaceY, z);
            }
        }
        return y;
    }

    private double clearGroundPlacement(double x, double y, double z) {
        // A descent can start inside terrain. Lift the entire body out before landing.
        while (y < level().getMaxBuildHeight()) {
            AABB body = getBoundingBox().move(x - getX(), y - getY(), z - getZ()).deflate(1.0E-5D);
            double clearY = y;
            for (VoxelShape shape : level().getBlockCollisions(this, body)) {
                clearY = Math.max(clearY, shape.max(Direction.Axis.Y));
            }
            if (clearY <= y) {
                break;
            }
            y = clearY;
        }
        return y;
    }

    private boolean moveToAttackHeight(double y) {
        return moveToAttackPosition(getX(), y, getZ());
    }

    private boolean moveToAttackPosition(double x, double y, double z) {
        return moveToAttackPosition(x, y, z, 1.0D, getBoundingBox().getSize());
    }

    private boolean moveToAttackPosition(double x, double y, double z, double speed, double tolerance) {
        double safeY = clearGroundPlacement(x, y, z);
        getMoveControl().setWantedPosition(x, safeY, z, speed);
        double arrivalDistance = Math.max(tolerance, getBoundingBox().getSize());
        if (position().distanceToSqr(new Vec3(x, safeY, z)) > arrivalDistance * arrivalDistance) {
            return false;
        }
        setPos(x, safeY, z);
        setDeltaMovement(Vec3.ZERO);
        getMoveControl().setWantedPosition(x, safeY, z, 1.0D);
        return true;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        boolean protectHealth = phaseHealthProtectionEnabled;
        phaseHealthProtectionEnabled = false;
        try {
            super.readAdditionalSaveData(compound);
        }
        finally {
            phaseHealthProtectionEnabled = protectHealth;
        }
        phase = compound.getInt("Phase");
        entityData.set(BOSS_DIFFICULTY, Mth.clamp(compound.getInt("BossDifficulty"), 0, 2));
        applyDifficultySettings();
        applyDifficultyHealth(false);
        phaseTimer = compound.getInt("PhaseTimer");
        pendingActivePhase = compound.contains("PendingActivePhase") ? compound.getInt("PendingActivePhase") : -1;
        phaseStartAlignmentState = compound.getInt("PhaseStartAlignmentState");
        phaseStartGroundY = compound.getDouble("PhaseStartGroundY");
        restrictionBoundaryCreated = compound.getBoolean("RestrictionBoundaryCreated");
        waterArenaDetachedForPeppermint = compound.getBoolean("WaterArenaDetachedForPeppermint");
        waitingForFinalHit = compound.getBoolean("WaitingForFinalHit");
        finalHitLandingState = compound.getInt("FinalHitLandingState");
        finalHitGroundY = compound.getDouble("FinalHitGroundY");
        nextPhaseOneAttack = compound.getInt("NextPhaseOneAttack");
        nextPhaseTwoAttack = compound.getInt("NextPhaseTwoAttack");
        nextPhaseThreeAttack = compound.getInt("NextPhaseThreeAttack");
        nextPhaseFourAttack = compound.getInt("NextPhaseFourAttack");
        phaseTwoAttackHistory.clear();
        for (int attack : compound.getIntArray("PhaseTwoAttackHistory")) {
            phaseTwoAttackHistory.add(attack);
        }
        phaseThreeAttackHistory.clear();
        for (int attack : compound.getIntArray("PhaseThreeAttackHistory")) {
            phaseThreeAttackHistory.add(attack);
        }
        waterArenaCenter = new Vec3(compound.getDouble("WaterArenaCenterX"), compound.getDouble("WaterArenaCenterY"), compound.getDouble("WaterArenaCenterZ"));
        entityData.set(MUSIC_PHASE, getMusicPhaseForStage());
        updatePoweredState();
        setHealth(getHealth());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Phase", phase);
        compound.putInt("BossDifficulty", getBossDifficulty());
        compound.putInt("PhaseTimer", phaseTimer);
        compound.putInt("PendingActivePhase", pendingActivePhase);
        compound.putInt("PhaseStartAlignmentState", phaseStartAlignmentState);
        compound.putDouble("PhaseStartGroundY", phaseStartGroundY);
        compound.putBoolean("RestrictionBoundaryCreated", restrictionBoundaryCreated);
        compound.putBoolean("WaterArenaDetachedForPeppermint", waterArenaDetachedForPeppermint);
        compound.putBoolean("WaitingForFinalHit", waitingForFinalHit);
        compound.putInt("FinalHitLandingState", finalHitLandingState);
        compound.putDouble("FinalHitGroundY", finalHitGroundY);
        compound.putInt("NextPhaseOneAttack", nextPhaseOneAttack);
        compound.putInt("NextPhaseTwoAttack", nextPhaseTwoAttack);
        compound.putInt("NextPhaseThreeAttack", nextPhaseThreeAttack);
        compound.putInt("NextPhaseFourAttack", nextPhaseFourAttack);
        compound.putIntArray("PhaseTwoAttackHistory", phaseTwoAttackHistory.stream().mapToInt(Integer::intValue).toArray());
        compound.putIntArray("PhaseThreeAttackHistory", phaseThreeAttackHistory.stream().mapToInt(Integer::intValue).toArray());
        compound.putDouble("WaterArenaCenterX", waterArenaCenter.x);
        compound.putDouble("WaterArenaCenterY", waterArenaCenter.y);
        compound.putDouble("WaterArenaCenterZ", waterArenaCenter.z);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return MYFSounds.bellringerIdle.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return MYFSounds.bellringerHurt.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return MYFSounds.bellringerDeath.get();
    }

    @Override
    protected SoundEvent getMusic() {
        return switch (getMusicPhase()) {
            case 2 -> ModSounds.MUTANT_PHASE_2.get();
            case 3 -> ModSounds.MUTANT_PHASE_3.get();
            case 4 -> ModSounds.MUTANT_PHASE_4.get();
            default -> ModSounds.MUTANT_PHASE_1.get();
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readSpawnData(FriendlyByteBuf additionalData) {
        MutantMusic.start(this);
    }

    private int getMusicPhaseForStage() {
        if (phase == PHASE_4) {
            return 4;
        }
        if (phase == PHASE_3 || phase == PHASE_3_TRANSITION) {
            return 3;
        }
        if (phase == PHASE_2 || phase == PHASE_2_TRANSITION) {
            return 2;
        }
        return 1;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return FargoBosses.rl("entities/bellringer");
    }

    private class NormalShootingAttack extends Goal {
        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        private int shotsRemaining;
        private int shotTimer;
        private int preparationTimer;

        private NormalShootingAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_1 && mutant.nextPhaseOneAttack == ATTACK_NORMAL_SHOOTING
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_1 && target != null && target.isAlive()
                    && (preparationTimer > 0 || shotsRemaining > 0);
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            shotsRemaining = NORMAL_SHOOTING_MIN_SHOTS
                    + mutant.getRandom().nextInt(NORMAL_SHOOTING_MAX_SHOTS - NORMAL_SHOOTING_MIN_SHOTS + 1);
            shotTimer = 0;
            preparationTimer = NORMAL_SHOOTING_PREPARE_TIME;
        }

        @Override
        public void tick() {
            if (target == null) {
                return;
            }

            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (preparationTimer > 0) {
                preparationTimer--;
                return;
            }
            if (--shotTimer <= 0) {
                fireLaserAtTarget();
                shotsRemaining--;
                shotTimer = NORMAL_SHOOTING_INTERVAL;
            }
        }

        private void fireLaserAtTarget() {
            Vec3 start = new Vec3(mutant.getX(), mutant.getY() + NORMAL_PROJECTILE_HEIGHT, mutant.getZ());
            Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
            if (direction.lengthSqr() < 1.0E-4D) {
                direction = mutant.getLookAngle().normalize();
            }

            MutantLaserBeamEntity laser = new MutantLaserBeamEntity(ModEntities.MUTANT_LASER_BEAM.get(), mutant,
                    start.x, start.y, start.z, direction, NORMAL_ATTACK_DAMAGE, mutant.level());
            laser.setInitialSpeed(NORMAL_SHOOTING_LASER_INITIAL_SPEED);
            float yaw = (float) (Math.atan2(direction.z, direction.x) * 180.0D / Math.PI) + 90.0F;
            float pitch = (float) -Math.toDegrees(Math.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));
            laser.moveTo(start.x, start.y, start.z, yaw, pitch);
            mutant.level().addFreshEntity(laser);
        }

        @Override
        public void stop() {
            mutant.nextPhaseOneAttack = ATTACK_NORMAL_DASH;
        }
    }

    private class NormalDashAttack extends Goal {
        private static final int PHASE_WINDUP = 0;
        private static final int PHASE_DASH = 1;
        private static final int PHASE_RECOVER = 2;
        private static final int PHASE_DONE = 3;

        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        private Vec3 dashDirection = Vec3.ZERO;
        private int dashesRemaining;
        private int timer;
        private int attackPhase;

        private NormalDashAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_1 && mutant.nextPhaseOneAttack == ATTACK_NORMAL_DASH
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_1 && target != null && target.isAlive() && attackPhase != PHASE_DONE;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            dashesRemaining = NORMAL_DASH_COUNT;
            beginWindup();
        }

        @Override
        public void tick() {
            if (target == null) {
                attackPhase = PHASE_DONE;
                return;
            }

            timer--;
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (attackPhase == PHASE_WINDUP) {
                mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), NORMAL_DASH_HOLD_SPEED);
                if (timer <= 0) {
                    beginDash();
                }
            }
            else if (attackPhase == PHASE_DASH) {
                mutant.setDeltaMovement(dashDirection.scale(NORMAL_DASH_SPEED));
                hurtDashContacts();
                if (timer % NORMAL_DASH_TRAIL_INTERVAL == 0) {
                    leaveDashTrail();
                }
                if (timer <= 0) {
                    beginRecover();
                }
            }
            else if (attackPhase == PHASE_RECOVER) {
                mutant.setDeltaMovement(mutant.getDeltaMovement().scale(NORMAL_DASH_RECOVER_SLOWDOWN));
                if (timer <= 0) {
                    if (dashesRemaining > 0) {
                        beginWindup();
                    }
                    else {
                        attackPhase = PHASE_DONE;
                    }
                }
            }
        }

        private void beginWindup() {
            attackPhase = PHASE_WINDUP;
            timer = NORMAL_DASH_WINDUP;
            mutant.setDeltaMovement(Vec3.ZERO);
        }

        private void beginDash() {
            Vec3 start = new Vec3(mutant.getX(), mutant.getY() + NORMAL_PROJECTILE_HEIGHT, mutant.getZ());
            Vec3 destination = target.getBoundingBox().getCenter();
            dashDirection = destination.subtract(start).normalize();
            if (dashDirection.lengthSqr() < 1.0E-4D) {
                dashDirection = mutant.getLookAngle().normalize();
            }

            dashesRemaining--;
            attackPhase = PHASE_DASH;
            timer = NORMAL_DASH_DURATION;
            leaveDashTrail();
        }

        private void beginRecover() {
            attackPhase = PHASE_RECOVER;
            timer = NORMAL_DASH_RECOVER;
            mutant.setDeltaMovement(Vec3.ZERO);
        }

        private void hurtDashContacts() {
            for (LivingEntity entity : mutant.level().getEntitiesOfClass(LivingEntity.class,
                    mutant.getBoundingBox().inflate(NORMAL_DASH_CONTACT_RANGE), entity -> entity != mutant && entity.isAlive() && !mutant.isAlliedTo(entity))) {
                entity.hurt(mutant.damageSources().mobAttack(mutant), NORMAL_ATTACK_DAMAGE);
            }
        }

        private void leaveDashTrail() {
            float rotation = (float) Math.atan2(dashDirection.z, dashDirection.x);
            Phantom_Halberd_Entity halberd = new Phantom_Halberd_Entity(mutant.level(), mutant.getX(), mutant.getY(), mutant.getZ(),
                    rotation, NORMAL_DASH_HALBERD_WARMUP, mutant, NORMAL_ATTACK_DAMAGE);
            mutant.level().addFreshEntity(halberd);

            if (mutant.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ModParticle.CURSED_FLAME.get(), mutant.getX(), mutant.getY() + NORMAL_PROJECTILE_HEIGHT, mutant.getZ(),
                        NORMAL_DASH_PARTICLE_COUNT, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }

        @Override
        public void stop() {
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.nextPhaseOneAttack = ATTACK_NORMAL_HOWITZER;
        }
    }

    private class HowitzerAttack extends Goal {
        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        private int preparationTimer;
        private boolean fired;

        private HowitzerAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_1 && mutant.nextPhaseOneAttack == ATTACK_NORMAL_HOWITZER
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_1 && target != null && target.isAlive() && !fired;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            preparationTimer = HOWITZER_ATTACK_PREPARE_TIME;
            fired = false;
        }

        @Override
        public void tick() {
            if (target == null) {
                fired = true;
                return;
            }

            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (preparationTimer-- > 0) {
                return;
            }

            Vec3 start = new Vec3(mutant.getX(), mutant.getEyeY(), mutant.getZ());
            Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
            if (direction.lengthSqr() < 1.0E-4D) {
                direction = mutant.getLookAngle().normalize();
            }

            Void_Howitzer_Entity howitzer = new Void_Howitzer_Entity(
                    com.github.L_Ender.cataclysm.init.ModEntities.VOID_HOWITZER.get(), mutant.level(), mutant);
            howitzer.setPos(start.x, start.y, start.z);
            howitzer.shoot(direction.x, direction.y, direction.z, HOWITZER_ATTACK_SPEED, HOWITZER_ATTACK_INACCURACY);
            mutant.level().addFreshEntity(howitzer);
            fired = true;
        }

        @Override
        public void stop() {
            mutant.nextPhaseOneAttack = ATTACK_NORMAL_SHOOTING;
            target = null;
        }
    }

    private class MagneticAttack extends Goal {
        private final MutantEntity mutant;
        private final List<MutantMagneticWeaponEntity> weapons = new ArrayList<>();
        @Nullable
        private LivingEntity target;
        private Vec3 stationaryPosition = Vec3.ZERO;
        private int timer;
        private int preparationTimer;
        private int spawnTimer;
        private double groundY;
        private boolean yAligned;
        private boolean grounded;

        private MagneticAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_2 && mutant.nextPhaseTwoAttack == ATTACK_PHASE_TWO_MAGNETIC
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_2 && target != null && target.isAlive() && timer > 0;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            timer = MAGNETIC_ATTACK_DURATION;
            preparationTimer = MAGNETIC_ATTACK_PREPARE_TIME;
            spawnTimer = 0;
            weapons.clear();
            stationaryPosition = mutant.position();
            groundY = mutant.getY();
            yAligned = false;
            grounded = false;
            mutant.getNavigation().stop();
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
        }

        private void spawnWeapons() {
            double radius = RESTRICTION_BOUNDARY_RADIUS + Math.max(1.0D, MAGNETIC_WEAPON_SPAWN_OFFSET);
            for (int i = 0; i < MAGNETIC_WEAPONS_PER_SPAWN; i++) {
                double angle = mutant.getRandom().nextDouble() * Math.PI * 2.0D;
                Item item = MAGNETIC_WEAPON_ITEMS.get(mutant.getRandom().nextInt(MAGNETIC_WEAPON_ITEMS.size()));
                MutantMagneticWeaponEntity weapon = new MutantMagneticWeaponEntity(
                        ModEntities.MUTANT_MAGNETIC_WEAPON.get(), mutant.level());
                weapon.setItemStack(new ItemStack(item));
                weapon.setMutantController(mutant);
                weapon.configureFlight(MAGNETIC_WEAPON_SPEED, MAGNETIC_ATTACK_DAMAGE, MAGNETIC_WEAPON_HEIGHT, timer);
                weapon.setPos(stationaryPosition.x + Math.cos(angle) * radius,
                        stationaryPosition.y + MAGNETIC_WEAPON_HEIGHT, stationaryPosition.z + Math.sin(angle) * radius);
                mutant.level().addFreshEntity(weapon);
                weapons.add(weapon);
            }
        }

        @Override
        public void tick() {
            if (target == null) {
                return;
            }
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (!yAligned) {
                if (mutant.moveToAttackPosition(stationaryPosition.x, target.getY(), stationaryPosition.z)) {
                    yAligned = true;
                    groundY = mutant.findGroundY();
                }
                return;
            }
            if (!grounded) {
                if (mutant.moveToAttackPosition(stationaryPosition.x, groundY, stationaryPosition.z)) {
                    stationaryPosition = mutant.position();
                    grounded = true;
                    mutant.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }

            mutant.setPos(stationaryPosition);
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(stationaryPosition.x, stationaryPosition.y, stationaryPosition.z, 1.0D);
            if (preparationTimer > 0) {
                preparationTimer--;
                return;
            }

            pullPlayers();
            weapons.removeIf(MutantMagneticWeaponEntity::isRemoved);
            if (--spawnTimer <= 0) {
                spawnWeapons();
                spawnTimer = Math.max(1, MAGNETIC_WEAPON_SPAWN_INTERVAL);
            }
            timer--;
        }

        private void pullPlayers() {
            double radius = RESTRICTION_BOUNDARY_RADIUS;
            AABB arena = new AABB(stationaryPosition, stationaryPosition).inflate(radius);
            for (Player player : mutant.level().getEntitiesOfClass(Player.class, arena,
                    player -> player.isAlive() && !player.isSpectator() && !player.getAbilities().invulnerable
                            && !mutant.isAlliedTo(player))) {
                Vec3 towardBoss = stationaryPosition.subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
                double distance = towardBoss.length();
                if (distance < 1.0E-4D || distance > radius) {
                    continue;
                }
                double strength = Math.min(distance * Math.max(0.0F, MAGNETIC_PLAYER_PULL_PER_BLOCK),
                        Math.max(0.0F, MAGNETIC_PLAYER_MAX_PULL));
                if (strength > 0.0D) {
                    player.setDeltaMovement(player.getDeltaMovement().add(towardBoss.scale(strength / distance)));
                    player.hurtMarked = true;
                }
            }
        }

        @Override
        public void stop() {
            for (MutantMagneticWeaponEntity weapon : weapons) {
                if (!weapon.isRemoved()) {
                    weapon.discard();
                }
            }
            weapons.clear();
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
            target = null;
            mutant.selectNextPhaseTwoAttack(ATTACK_PHASE_TWO_MAGNETIC);
        }
    }

    private class WaterAttack extends Goal {
        private static final int PHASE_ALIGN = 0;
        private static final int PHASE_DESCEND = 1;
        private static final int PHASE_PREPARE = 2;
        private static final int PHASE_DASH = 3;
        private static final int PHASE_RECOVER = 4;
        private static final int PHASE_RETURN = 5;
        private static final int PHASE_DONE = 6;

        private final MutantEntity mutant;
        private final List<WaveEntity> spawnedWaves = new ArrayList<>();
        @Nullable
        private LivingEntity target;
        private Vec3 arenaCenter = Vec3.ZERO;
        private Vec3 dashDirection = Vec3.ZERO;
        private Vec3 previousDashPosition = Vec3.ZERO;
        private ItemStack previousMainHand = ItemStack.EMPTY;
        private double groundY;
        private int attackPhase;
        private int timer;
        private int waveTimer;
        private int dashesRemaining;
        private boolean ringDetached;

        private WaterAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_2 && mutant.nextPhaseTwoAttack == ATTACK_PHASE_TWO_WATER
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_2 && target != null && target.isAlive() && attackPhase != PHASE_DONE;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            arenaCenter = mutant.position();
            groundY = mutant.getY();
            attackPhase = PHASE_ALIGN;
            timer = WATER_ATTACK_PREPARE_TIME;
            dashesRemaining = Math.max(0, WATER_DASH_COUNT);
            ringDetached = false;
            spawnedWaves.clear();
            previousMainHand = mutant.getMainHandItem().copy();
            mutant.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ACItemRegistry.ORTHOLANCE.get()));
            mutant.getNavigation().stop();
            holdStill();
        }

        @Override
        public void tick() {
            if (target == null) {
                attackPhase = PHASE_DONE;
                return;
            }

            spawnedWaves.removeIf(WaveEntity::isRemoved);
            switch (attackPhase) {
                case PHASE_ALIGN -> alignToTargetY();
                case PHASE_DESCEND -> descendToGround();
                case PHASE_PREPARE -> prepare();
                case PHASE_DASH -> dash();
                case PHASE_RECOVER -> recover();
                case PHASE_RETURN -> returnToArena();
                default -> {
                }
            }
        }

        private void alignToTargetY() {
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (mutant.moveToAttackPosition(arenaCenter.x, target.getY(), arenaCenter.z)) {
                groundY = mutant.findGroundY();
                attackPhase = PHASE_DESCEND;
            }
        }

        private void descendToGround() {
            if (mutant.moveToAttackPosition(arenaCenter.x, groundY, arenaCenter.z)) {
                arenaCenter = mutant.position();
                holdStill();
                detachRestrictionRing();
                attackPhase = PHASE_PREPARE;
            }
        }

        private void prepare() {
            holdStill();
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (--timer <= 0) {
                beginDash();
            }
        }

        private void beginDash() {
            if (dashesRemaining <= 0) {
                attackPhase = PHASE_RETURN;
                return;
            }
            dashDirection = target.position().subtract(mutant.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
            if (dashDirection.lengthSqr() < 1.0E-4D) {
                dashDirection = mutant.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
                if (dashDirection.lengthSqr() < 1.0E-4D) {
                    dashDirection = new Vec3(0.0D, 0.0D, 1.0D);
                }
            }
            previousDashPosition = mutant.position();
            dashesRemaining--;
            timer = Math.max(1, WATER_DASH_DURATION);
            waveTimer = 0;
            attackPhase = PHASE_DASH;
            mutant.playSound(ACSoundRegistry.ORTHOLANCE_WAVE.get(), 2.0F, 1.0F);
        }

        private void dash() {
            hurtDashContacts();
            if (timer <= 0) {
                holdStill();
                timer = Math.max(0, WATER_DASH_RECOVER_TIME);
                attackPhase = PHASE_RECOVER;
                return;
            }

            mutant.setDeltaMovement(dashDirection.scale(WATER_DASH_SPEED));
            float yaw = (float) -Math.toDegrees(Math.atan2(dashDirection.x, dashDirection.z));
            mutant.setYRot(yaw);
            mutant.setYHeadRot(yaw);
            mutant.yBodyRot = yaw;
            mutant.getLookControl().setLookAt(mutant.getX() + dashDirection.x * 10.0D, mutant.getEyeY(),
                    mutant.getZ() + dashDirection.z * 10.0D, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (--waveTimer <= 0) {
                spawnSideWaves(yaw);
                waveTimer = Math.max(1, WATER_WAVE_INTERVAL);
            }
            timer--;
        }

        private void hurtDashContacts() {
            Vec3 end = mutant.getBoundingBox().getCenter();
            Vec3 movement = mutant.position().subtract(previousDashPosition);
            Vec3 start = end.subtract(movement);
            double contactRange = Math.max(0.0F, WATER_DASH_CONTACT_RANGE);
            AABB searchArea = mutant.getBoundingBox().inflate(contactRange).expandTowards(movement.scale(-1.0D));
            for (LivingEntity entity : mutant.level().getEntitiesOfClass(LivingEntity.class, searchArea,
                    entity -> entity != mutant && entity.isAlive() && !mutant.isAlliedTo(entity))) {
                AABB contactBox = entity.getBoundingBox().inflate(mutant.getBbWidth() * 0.5D + contactRange,
                        mutant.getBbHeight() * 0.5D + contactRange, mutant.getBbWidth() * 0.5D + contactRange);
                if (contactBox.contains(start) || contactBox.clip(start, end).isPresent()) {
                    entity.hurt(mutant.damageSources().mobAttack(mutant), NORMAL_ATTACK_DAMAGE);
                }
            }
            previousDashPosition = mutant.position();
        }

        private void spawnSideWaves(float dashYaw) {
            Vec3 left = new Vec3(-dashDirection.z, 0.0D, dashDirection.x);
            double sideOffset = mutant.getBbWidth() * 0.5D + Math.max(0.0F, WATER_WAVE_SIDE_OFFSET);
            // Ortholance's paired waves use opposite yaw offsets from the dash direction.
            for (int side = -1; side <= 1; side += 2) {
                Vec3 position = mutant.position().add(left.scale(side * sideOffset));
                WaveEntity wave = new WaveEntity(mutant.level(), mutant);
                wave.setPos(position);
                wave.setLifespan(Math.max(1, WATER_WAVE_LIFESPAN));
                wave.setWaveScale(Math.max(0.1F, WATER_WAVE_SCALE));
                wave.setYRot(dashYaw + side * WATER_WAVE_SIDE_ANGLE);
                mutant.level().addFreshEntity(wave);
                spawnedWaves.add(wave);
            }
        }

        private void recover() {
            holdStill();
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (timer > 0) {
                timer--;
                return;
            }
            if (dashesRemaining > 0) {
                beginDash();
            }
            else if (spawnedWaves.isEmpty()) {
                attackPhase = PHASE_RETURN;
            }
        }

        private void returnToArena() {
            if (mutant.moveToAttackPosition(arenaCenter.x, arenaCenter.y, arenaCenter.z,
                    Math.max(0.1F, WATER_RETURN_SPEED), mutant.getBoundingBox().getSize())) {
                holdStill();
                attackPhase = PHASE_DONE;
            }
        }

        private void holdStill() {
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
        }

        private void detachRestrictionRing() {
            arenaCenter = new Vec3(arenaCenter.x,
                    mutant.clearGroundPlacement(arenaCenter.x, arenaCenter.y, arenaCenter.z), arenaCenter.z);
            AABB searchArea = new AABB(arenaCenter, arenaCenter).inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
            for (RotatingSoulShieldEntity shield : mutant.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == mutant)) {
                shield.switchToAroundPosition(arenaCenter.x, arenaCenter.y, arenaCenter.z);
            }
            ringDetached = true;
        }

        private void restoreRestrictionRing() {
            if (!ringDetached) {
                return;
            }

            AABB searchArea = new AABB(arenaCenter, arenaCenter).inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
            for (RotatingSoulShieldEntity shield : mutant.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == mutant)) {
                shield.switchToAroundCaster();
            }
            ringDetached = false;
        }

        @Override
        public void stop() {
            mutant.selectNextPhaseTwoAttack(ATTACK_PHASE_TWO_WATER);
            boolean handOffToPeppermint = ringDetached && mutant.isAlive() && mutant.phase == PHASE_2
                    && target != null && target.isAlive()
                    && mutant.nextPhaseTwoAttack == ATTACK_PHASE_TWO_PEPPERMINT;
            if (handOffToPeppermint) {
                mutant.waterArenaDetachedForPeppermint = true;
                mutant.waterArenaCenter = arenaCenter;
            }
            else {
                mutant.waterArenaDetachedForPeppermint = false;
                restoreRestrictionRing();
            }
            for (WaveEntity wave : spawnedWaves) {
                if (!wave.isRemoved()) {
                    wave.discard();
                }
            }
            spawnedWaves.clear();
            holdStill();
            mutant.setItemSlot(EquipmentSlot.MAINHAND, previousMainHand);
            target = null;
        }
    }

    private class PeppermintAttack extends Goal {
        private final MutantEntity mutant;
        private final List<MutantSpinningPeppermintEntity> peppermints = new ArrayList<>();
        @Nullable
        private LivingEntity target;
        private Vec3 arenaCenter = Vec3.ZERO;
        private Vec3 preparationPosition = Vec3.ZERO;
        private int timer;
        private int preparationTimer;
        private int straightTimer;
        private int straightRounds;
        private boolean ringDetached;
        private boolean attackStarted;

        private PeppermintAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_2 && mutant.nextPhaseTwoAttack == ATTACK_PHASE_TWO_PEPPERMINT
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_2 && target != null && target.isAlive() && timer > 0;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            PEPPERMINT_STRAIGHT_COUNT = configInt("PEPPERMINT_STRAIGHT_COUNT");
            int interval = Math.max(1, PEPPERMINT_STRAIGHT_INTERVAL);
            long firingDuration = (long) Math.max(0, PEPPERMINT_STRAIGHT_ROUNDS) * interval;
            timer = (int) Math.min(Integer.MAX_VALUE, Math.max(PEPPERMINT_ATTACK_DURATION, firingDuration));
            preparationTimer = PEPPERMINT_ATTACK_PREPARE_TIME;
            straightTimer = interval - 1;
            straightRounds = 0;
            attackStarted = false;
            peppermints.clear();
            preparationPosition = choosePreparationPosition();

            if (mutant.waterArenaDetachedForPeppermint) {
                arenaCenter = mutant.waterArenaCenter;
                ringDetached = true;
                mutant.waterArenaDetachedForPeppermint = false;
            }
            else {
                arenaCenter = mutant.position();
                ringDetached = false;
                detachRestrictionRing();
            }
        }

        @Override
        public void tick() {
            if (target == null) {
                return;
            }

            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (preparationTimer > 0) {
                preparationTimer--;
                mutant.setDeltaMovement(Vec3.ZERO);
                mutant.getMoveControl().setWantedPosition(preparationPosition.x, preparationPosition.y, preparationPosition.z,
                        PEPPERMINT_FOLLOW_SPEED);
                return;
            }

            if (!attackStarted) {
                attackStarted = true;
                spawnOrbitRing(PEPPERMINT_SMALL_RING_COUNT, PEPPERMINT_SMALL_RING_RADIUS);
                spawnOrbitRing(PEPPERMINT_LARGE_RING_COUNT, PEPPERMINT_LARGE_RING_RADIUS);
            }

            mutant.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), PEPPERMINT_FOLLOW_SPEED);
            for (MutantSpinningPeppermintEntity peppermint : peppermints) {
                if (!peppermint.isRemoved() && !peppermint.isStraight()) {
                    peppermint.setOrbitCenter(mutant.position());
                    peppermint.updateOrbitPosition();
                }
            }

            if (straightRounds < PEPPERMINT_STRAIGHT_ROUNDS
                    && ++straightTimer >= Math.max(1, PEPPERMINT_STRAIGHT_INTERVAL)) {
                straightTimer = 0;
                fireParallelPeppermints();
                straightRounds++;
            }
            timer--;
        }

        private Vec3 choosePreparationPosition() {
            double angle = mutant.getRandom().nextDouble() * Math.PI * 2.0D;
            double distance = PEPPERMINT_SMALL_RING_RADIUS
                    + mutant.getRandom().nextDouble() * (PEPPERMINT_LARGE_RING_RADIUS - PEPPERMINT_SMALL_RING_RADIUS);
            return new Vec3(target.getX() + Math.cos(angle) * distance, target.getY(),
                    target.getZ() + Math.sin(angle) * distance);
        }

        private void spawnOrbitRing(int count, float radius) {
            for (int i = 0; i < count; i++) {
                float startAngle = i * 360.0F / count;
                MutantSpinningPeppermintEntity peppermint = new MutantSpinningPeppermintEntity(
                        ModEntities.MUTANT_SPINNING_PEPPERMINT.get(), mutant.level());
                peppermint.configure(radius, PEPPERMINT_SPIN_SPEED, startAngle, timer,
                        PEPPERMINT_DAMAGE, PEPPERMINT_HIT_RANGE);
                peppermint.setOwner(mutant);
                peppermint.setOrbitCenter(mutant.position());
                Vec3 offset = new Vec3(0.0D, 0.0D, radius).yRot((float) -Math.toRadians(startAngle));
                peppermint.setPos(mutant.position().add(offset));
                mutant.level().addFreshEntity(peppermint);
                peppermints.add(peppermint);
            }
        }

        private void fireParallelPeppermints() {
            double radius = RESTRICTION_BOUNDARY_RADIUS;
            if (PEPPERMINT_STRAIGHT_COUNT <= 0 || radius <= 0.0D) {
                return;
            }

            Vec3 targetPosition = target.getBoundingBox().getCenter();
            Vec3 northeast = new Vec3(1.0D, 0.0D, -1.0D).normalize();
            Vec3 lateral = new Vec3(-northeast.z, 0.0D, northeast.x);
            double shiftRange = Math.min(Math.max(0.0F, PEPPERMINT_STRAIGHT_START_SHIFT), radius * 0.25D);
            double startShift = (mutant.getRandom().nextDouble() * 2.0D - 1.0D) * shiftRange;
            Vec3 launchCenter = new Vec3(arenaCenter.x, targetPosition.y, arenaCenter.z)
                    .add(northeast.scale(Math.sqrt(radius * radius - startShift * startShift)))
                    .add(lateral.scale(startShift));
            Vec3 direction = targetPosition.subtract(launchCenter);
            if (direction.lengthSqr() < 1.0E-4D) {
                direction = northeast.scale(-1.0D);
            }
            direction = direction.normalize();
            float yaw = 180.0F - (float) Math.toDegrees(Math.atan2(direction.x, direction.z));

            int centerPeppermint = PEPPERMINT_STRAIGHT_COUNT / 2;
            int maximumLaneOffset = Math.max(centerPeppermint, PEPPERMINT_STRAIGHT_COUNT - 1 - centerPeppermint);
            // Keep the entire volley near the northeast rim, even with oversized config values.
            double spacing = Math.min(Math.max(0.0F, PEPPERMINT_STRAIGHT_SPACING),
                    (radius * 0.5D - shiftRange) / (maximumLaneOffset + 1.0D));
            for (int i = 0; i < PEPPERMINT_STRAIGHT_COUNT; i++) {
                double laneOffset = startShift + (i - centerPeppermint) * spacing;
                double distanceToRim = Math.sqrt(Math.max(0.0D, radius * radius - laneOffset * laneOffset));
                Vec3 start = new Vec3(arenaCenter.x, targetPosition.y, arenaCenter.z)
                        .add(northeast.scale(distanceToRim)).add(lateral.scale(laneOffset));
                MutantSpinningPeppermintEntity peppermint = new MutantSpinningPeppermintEntity(
                        ModEntities.MUTANT_SPINNING_PEPPERMINT.get(), mutant.level());
                peppermint.configure(PEPPERMINT_SMALL_RING_RADIUS, PEPPERMINT_SPIN_SPEED, 0.0F, timer,
                        PEPPERMINT_DAMAGE, PEPPERMINT_HIT_RANGE);
                peppermint.setOwner(mutant);
                peppermint.setStraight(true);
                peppermint.setNoGravity(true);
                peppermint.setPos(start);
                peppermint.setYRot(yaw);
                mutant.level().addFreshEntity(peppermint);
                peppermints.add(peppermint);
            }
        }

        private void detachRestrictionRing() {
            arenaCenter = new Vec3(arenaCenter.x,
                    mutant.clearGroundPlacement(arenaCenter.x, arenaCenter.y, arenaCenter.z), arenaCenter.z);
            AABB searchArea = new AABB(arenaCenter, arenaCenter).inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
            for (RotatingSoulShieldEntity shield : mutant.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == mutant)) {
                shield.switchToAroundPosition(arenaCenter.x, arenaCenter.y, arenaCenter.z);
            }
            ringDetached = true;
        }

        private void restoreRestrictionRing() {
            if (!ringDetached) {
                return;
            }

            AABB searchArea = new AABB(arenaCenter, arenaCenter).inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
            for (RotatingSoulShieldEntity shield : mutant.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == mutant)) {
                shield.switchToAroundCaster();
            }
            ringDetached = false;
        }

        @Override
        public void stop() {
            restoreRestrictionRing();
            for (MutantSpinningPeppermintEntity peppermint : peppermints) {
                if (!peppermint.isRemoved()) {
                    peppermint.discard();
                }
            }
            peppermints.clear();
            mutant.selectNextPhaseTwoAttack(ATTACK_PHASE_TWO_PEPPERMINT);
            target = null;
        }
    }

    private class DinosaurAttack extends Goal {
        private static final int PHASE_PREPARE = 0;
        private static final int PHASE_MOVE_TO_TOSS = 1;
        private static final int PHASE_SPEAR_WINDUP = 2;
        private static final int PHASE_WAIT_FOR_SPEAR = 3;
        private static final int PHASE_RETURN_TO_RING = 4;
        private static final int PHASE_DONE = 5;

        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        @Nullable
        private MutantExtinctionSpearEntity spear;
        private Vec3 arenaCenter = Vec3.ZERO;
        private Vec3 tossPosition = Vec3.ZERO;
        private Vec3 spearTargetPosition = Vec3.ZERO;
        private double previousTossAngle;
        private int throwsRemaining;
        private int attackPhase;
        private int timer;
        private boolean ringDetached;

        private DinosaurAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_2 && mutant.nextPhaseTwoAttack == ATTACK_PHASE_TWO_DINOSAUR
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_2 && target != null && target.isAlive() && attackPhase != PHASE_DONE;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            attackPhase = PHASE_PREPARE;
            timer = DINOSAUR_ATTACK_PREPARE_TIME;
            throwsRemaining = Math.max(1, DINOSAUR_THROW_COUNT);
            previousTossAngle = Double.NaN;
            ringDetached = false;
            spear = null;
            mutant.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ACItemRegistry.EXTINCTION_SPEAR.get()));
        }

        @Override
        public void tick() {
            if (target == null) {
                attackPhase = PHASE_DONE;
                return;
            }

            switch (attackPhase) {
                case PHASE_PREPARE -> prepare();
                case PHASE_MOVE_TO_TOSS -> moveToTossPosition();
                case PHASE_SPEAR_WINDUP -> windUpSpearThrow();
                case PHASE_WAIT_FOR_SPEAR -> waitForSpear();
                case PHASE_RETURN_TO_RING -> returnToRing();
                default -> {
                }
            }
        }

        private void prepare() {
            boolean aligned = mutant.moveToAttackPosition(mutant.getX(), target.getY(), mutant.getZ(),
                    DINOSAUR_TOSS_MOVE_SPEED, mutant.getBoundingBox().getSize());
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (timer > 0) {
                timer--;
            }
            if (timer <= 0 && aligned) {
                beginToss();
            }
        }

        private void beginToss() {
            if (!ringDetached) {
                arenaCenter = mutant.position();
                detachRestrictionRing();
            }
            double angle = chooseTossAngle();
            previousTossAngle = angle;
            double radius = Math.max(1.0D, DINOSAUR_TOSS_DISTANCE);
            tossPosition = new Vec3(arenaCenter.x + Math.cos(angle) * radius,
                    target.getY() + DINOSAUR_TOSS_HEIGHT, arenaCenter.z + Math.sin(angle) * radius);
            attackPhase = PHASE_MOVE_TO_TOSS;
        }

        private double chooseTossAngle() {
            if (Double.isNaN(previousTossAngle)) {
                return mutant.getRandom().nextDouble() * Math.PI * 2.0D;
            }
            // Advance strictly more than the minimum, but less than a half-turn.
            double minimumStep = Math.toRadians(Mth.clamp(DINOSAUR_MIN_TOSS_ANGLE_DIFFERENCE, 0.0F, 179.0F) + 0.001D);
            double maximumStep = Math.toRadians(179.999D);
            double step = minimumStep + mutant.getRandom().nextDouble() * (maximumStep - minimumStep);
            return (previousTossAngle + step) % (Math.PI * 2.0D);
        }

        private void moveToTossPosition() {
            mutant.getMoveControl().setWantedPosition(tossPosition.x, tossPosition.y, tossPosition.z, DINOSAUR_TOSS_MOVE_SPEED);
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (mutant.position().distanceToSqr(tossPosition)
                    <= DINOSAUR_TOSS_POSITION_TOLERANCE * DINOSAUR_TOSS_POSITION_TOLERANCE) {
                beginSpearWindup();
            }
        }

        private void beginSpearWindup() {
            mutant.setPos(tossPosition.x, tossPosition.y, tossPosition.z);
            mutant.getMoveControl().setWantedPosition(tossPosition.x, tossPosition.y, tossPosition.z, 0.0D);
            mutant.setDeltaMovement(Vec3.ZERO);
            spearTargetPosition = new Vec3(target.getX(), target.getBoundingBox().getCenter().y, target.getZ());
            timer = DINOSAUR_SPEAR_WINDUP_TIME;
            attackPhase = PHASE_SPEAR_WINDUP;
        }

        private void windUpSpearThrow() {
            mutant.setDeltaMovement(Vec3.ZERO);
            if (--timer <= 0) {
                tossSpear();
            }
        }

        private void tossSpear() {
            Vec3 start = new Vec3(mutant.getX(), mutant.getEyeY(), mutant.getZ());
            Vec3 direction = spearTargetPosition.subtract(start).normalize();
            if (direction.lengthSqr() < 1.0E-4D) {
                direction = mutant.getLookAngle().normalize();
            }
            spear = new MutantExtinctionSpearEntity(ModEntities.MUTANT_EXTINCTION_SPEAR.get(), mutant.level());
            spear.setOwner(mutant);
            spear.setPos(start);
            spear.shoot(direction.x, direction.y, direction.z, DINOSAUR_SPEAR_SPEED, 0.0F);
            mutant.level().addFreshEntity(spear);
            throwsRemaining--;
            timer = DINOSAUR_SPEAR_MAX_FLIGHT_TIME;
            attackPhase = PHASE_WAIT_FOR_SPEAR;
        }

        private void waitForSpear() {
            mutant.setDeltaMovement(Vec3.ZERO);
            if (spear == null || spear.isRemoved() || --timer <= 0) {
                if (spear != null && !spear.isRemoved()) {
                    spear.discard();
                }
                spear = null;
                if (throwsRemaining > 0) {
                    beginToss();
                }
                else {
                    attackPhase = PHASE_RETURN_TO_RING;
                }
            }
        }

        private void returnToRing() {
            if (mutant.moveToAttackPosition(arenaCenter.x, arenaCenter.y, arenaCenter.z,
                    DINOSAUR_RETURN_SPEED, DINOSAUR_TOSS_POSITION_TOLERANCE)) {
                attackPhase = PHASE_DONE;
            }
        }

        private void detachRestrictionRing() {
            arenaCenter = new Vec3(arenaCenter.x,
                    mutant.clearGroundPlacement(arenaCenter.x, arenaCenter.y, arenaCenter.z), arenaCenter.z);
            AABB searchArea = new AABB(arenaCenter, arenaCenter).inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
            for (RotatingSoulShieldEntity shield : mutant.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == mutant)) {
                shield.switchToAroundPosition(arenaCenter.x, arenaCenter.y, arenaCenter.z);
            }
            ringDetached = true;
        }

        private void restoreRestrictionRing() {
            if (!ringDetached) {
                return;
            }
            AABB searchArea = new AABB(arenaCenter, arenaCenter).inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
            for (RotatingSoulShieldEntity shield : mutant.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == mutant)) {
                shield.switchToAroundCaster();
            }
            ringDetached = false;
        }

        @Override
        public void stop() {
            if (spear != null && !spear.isRemoved()) {
                spear.discard();
            }
            restoreRestrictionRing();
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            mutant.selectNextPhaseTwoAttack(ATTACK_PHASE_TWO_DINOSAUR);
            target = null;
        }
    }

    private class ArrowAttack extends Goal {
        private final MutantEntity mutant;
        private final List<DarkArrowEntity> spawnedArrows = new ArrayList<>();
        @Nullable
        private LivingEntity target;
        private Vec3 arenaCenter = Vec3.ZERO;
        @Nullable
        private Vec3 previousTeleportPosition;
        private int positionsRemaining;
        private int volleysRemaining;
        private int preparationTimer;
        private int volleyTimer;
        private int postAttackRestTimer;
        private int drumRounds;
        private int drumTimer;
        private boolean drumsStarted;
        private boolean arrowsFinished;
        private boolean recovering;
        private boolean finished;
        private boolean ringDetached;

        private ArrowAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_2 && mutant.nextPhaseTwoAttack == ATTACK_PHASE_TWO_ARROW
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_2 && target != null && target.isAlive() && !finished;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            arenaCenter = mutant.position();
            positionsRemaining = ARROW_ATTACK_POSITIONS;
            previousTeleportPosition = null;
            postAttackRestTimer = 0;
            drumRounds = 0;
            drumTimer = 0;
            drumsStarted = false;
            arrowsFinished = false;
            recovering = false;
            finished = false;
            ringDetached = false;
            spawnedArrows.clear();
            detachRestrictionRing();
            if (!beginNextPosition()) {
                arrowsFinished = true;
                preparationTimer = Math.max(0, ARROW_ATTACK_PREPARE_TIME);
            }
        }

        @Override
        public void tick() {
            if (target == null) {
                finished = true;
                return;
            }

            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);

            if (recovering) {
                if (--postAttackRestTimer <= 0) {
                    finished = true;
                }
                return;
            }
            if (!drumsStarted && preparationTimer <= 0) {
                drumsStarted = true;
            }
            if (drumsStarted) {
                updateDrumThrows();
            }
            if (preparationTimer > 0) {
                preparationTimer--;
                return;
            }
            if (arrowsFinished) {
                if (drumThrowsFinished()) {
                    beginRecovery();
                }
                return;
            }
            if (--volleyTimer > 0) {
                return;
            }

            fireVolley();
            if (--volleysRemaining > 0) {
                volleyTimer = Math.max(1, ARROW_ATTACK_VOLLEY_INTERVAL);
            }
            else if (!beginNextPosition()) {
                arrowsFinished = true;
                if (drumThrowsFinished()) {
                    beginRecovery();
                }
            }
        }

        private boolean drumThrowsFinished() {
            return ARROW_ATTACK_DRUM_COUNT <= 0 || drumRounds >= Math.max(0, ARROW_ATTACK_DRUM_ROUNDS);
        }

        private void updateDrumThrows() {
            if (drumThrowsFinished()) {
                return;
            }
            if (--drumTimer <= 0) {
                throwWasteDrums();
                drumRounds++;
                drumTimer = Math.max(1, ARROW_ATTACK_DRUM_INTERVAL);
            }
        }

        private void throwWasteDrums() {
            Vec3 forward = mutant.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
            if (forward.lengthSqr() < 1.0E-4D) {
                forward = new Vec3(0.0D, 0.0D, 1.0D);
            }
            for (int i = 0; i < ARROW_ATTACK_DRUM_COUNT; i++) {
                Vec3 direction = forward.yRot((float) (i * Math.PI * 2.0D / ARROW_ATTACK_DRUM_COUNT));
                ThrownWasteDrumEntity drum = new ThrownWasteDrumEntity(ACEntityRegistry.THROWN_WASTE_DRUM.get(), mutant.level());
                drum.setPos(arenaCenter.x, arenaCenter.y + NORMAL_PROJECTILE_HEIGHT, arenaCenter.z);
                drum.setDeltaMovement(direction.scale(ARROW_ATTACK_DRUM_THROW_SPEED)
                        .add(0.0D, ARROW_ATTACK_DRUM_THROW_HEIGHT, 0.0D));
                // Keep the native lifetime so each drum can clean up its temporary acid block.
                mutant.level().addFreshEntity(drum);
            }
        }

        private boolean beginNextPosition() {
            if (positionsRemaining <= 0 || ARROW_ATTACK_VOLLEYS_PER_POSITION <= 0) {
                return false;
            }

            Vec3 position = findNextTeleportPosition();
            mutant.teleportTo(position.x, position.y, position.z);
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(position.x, position.y, position.z, 1.0D);
            previousTeleportPosition = position;
            positionsRemaining--;
            volleysRemaining = ARROW_ATTACK_VOLLEYS_PER_POSITION;
            preparationTimer = ARROW_ATTACK_PREPARE_TIME;
            volleyTimer = 0;
            return true;
        }

        private Vec3 findNextTeleportPosition() {
            double minimumDistance = 2.0D * Math.sin(Math.toRadians(60.0D)) * ARROW_ATTACK_DISTANCE;
            double minimumDistanceSqr = minimumDistance * minimumDistance;
            double y = target.getY() + ARROW_ATTACK_Y_OFFSET;
            if (previousTeleportPosition == null) {
                double angle = mutant.getRandom().nextDouble() * Math.PI * 2.0D;
                return new Vec3(target.getX() + Math.cos(angle) * ARROW_ATTACK_DISTANCE, y,
                        target.getZ() + Math.sin(angle) * ARROW_ATTACK_DISTANCE);
            }

            for (int attempt = 0; attempt < 32; attempt++) {
                double angle = mutant.getRandom().nextDouble() * Math.PI * 2.0D;
                Vec3 candidate = new Vec3(target.getX() + Math.cos(angle) * ARROW_ATTACK_DISTANCE, y,
                        target.getZ() + Math.sin(angle) * ARROW_ATTACK_DISTANCE);
                if (candidate.subtract(previousTeleportPosition).horizontalDistanceSqr() >= minimumDistanceSqr) {
                    return candidate;
                }
            }

            Vec3 fromTargetToPrevious = previousTeleportPosition.subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
            if (fromTargetToPrevious.lengthSqr() > 1.0E-4D) {
                // Preserve the teleport radius if a moving player makes the separation impossible.
                return new Vec3(target.getX(), y, target.getZ())
                        .subtract(fromTargetToPrevious.normalize().scale(ARROW_ATTACK_DISTANCE));
            }

            double angle = mutant.getRandom().nextDouble() * Math.PI * 2.0D;
            return new Vec3(target.getX() + Math.cos(angle) * ARROW_ATTACK_DISTANCE, y,
                    target.getZ() + Math.sin(angle) * ARROW_ATTACK_DISTANCE);
        }

        private void fireVolley() {
            int centerArrow = ARROW_ATTACK_ARROWS_PER_VOLLEY / 2;
            for (int i = 0; i < ARROW_ATTACK_ARROWS_PER_VOLLEY; i++) {
                DarkArrowEntity arrow = new DarkArrowEntity(mutant.level(), mutant);
                arrow.setShadowArrowDamage(ARROW_ATTACK_DAMAGE);
                Vec3 direction = target.getBoundingBox().getCenter().subtract(arrow.position()).normalize();
                if (direction.lengthSqr() < 1.0E-4D) {
                    direction = mutant.getLookAngle().normalize();
                }
                double angle = (i - centerArrow) * ARROW_ATTACK_SPREAD_DEGREES;
                Vec3 arrowDirection = direction.yRot((float) Math.toRadians(angle));
                arrow.setNoGravity(true);
                arrow.shoot(arrowDirection.x, arrowDirection.y, arrowDirection.z,
                        ARROW_ATTACK_SPEED, ARROW_ATTACK_INACCURACY);
                mutant.level().addFreshEntity(arrow);
                spawnedArrows.add(arrow);
            }
        }

        private void beginRecovery() {
            // Let the final volley travel before stop() clears the remaining arrows.
            recovering = true;
            postAttackRestTimer = Math.max(0, ARROW_ATTACK_RECOVERY_TIME);
            if (mutant.getBossDifficulty() == 2) {
                postAttackRestTimer += Math.max(0, ARROW_ATTACK_DIFFICULTY_TWO_REST_TIME);
            }
            if (postAttackRestTimer == 0) {
                finished = true;
            }
        }

        private void detachRestrictionRing() {
            arenaCenter = new Vec3(arenaCenter.x,
                    mutant.clearGroundPlacement(arenaCenter.x, arenaCenter.y, arenaCenter.z), arenaCenter.z);
            AABB searchArea = new AABB(arenaCenter, arenaCenter).inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
            for (RotatingSoulShieldEntity shield : mutant.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == mutant)) {
                shield.switchToAroundPosition(arenaCenter.x, arenaCenter.y, arenaCenter.z);
            }
            ringDetached = true;
        }

        private void restoreRestrictionRing() {
            if (!ringDetached) {
                return;
            }
            AABB searchArea = new AABB(arenaCenter, arenaCenter).inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
            for (RotatingSoulShieldEntity shield : mutant.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == mutant)) {
                shield.switchToAroundCaster();
            }
            ringDetached = false;
        }

        @Override
        public void stop() {
            mutant.selectNextPhaseTwoAttack(ATTACK_PHASE_TWO_ARROW);
            boolean handOffToPeppermint = mutant.phase == PHASE_2 && target != null && target.isAlive()
                    && mutant.nextPhaseTwoAttack == ATTACK_PHASE_TWO_PEPPERMINT;
            if (handOffToPeppermint) {
                mutant.waterArenaDetachedForPeppermint = true;
                mutant.waterArenaCenter = arenaCenter;
            }
            else {
                mutant.waterArenaDetachedForPeppermint = false;
                restoreRestrictionRing();
            }
            for (DarkArrowEntity arrow : spawnedArrows) {
                if (!arrow.isRemoved()) {
                    arrow.discard();
                }
            }
            spawnedArrows.clear();
            mutant.setDeltaMovement(Vec3.ZERO);
            target = null;
        }
    }

    private class WaterSpearCenterAttack extends Goal {
        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        private int wavesRemaining;
        private int waveTimer;
        private int preparationTimer;
        private double groundY;
        private boolean yAligned;
        private boolean grounded;

        private WaterSpearCenterAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_3 && mutant.nextPhaseThreeAttack == ATTACK_PHASE_THREE_WATER_SPEAR_CENTER
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_3 && target != null && target.isAlive()
                    && (preparationTimer > 0 || wavesRemaining > 0);
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            wavesRemaining = WATER_SPEAR_CENTER_WAVES;
            waveTimer = 0;
            preparationTimer = PHASE_THREE_PREPARE_TIME;
            groundY = mutant.getY();
            yAligned = false;
            grounded = false;
        }

        @Override
        public void tick() {
            if (!yAligned) {
                if (mutant.moveToAttackHeight(target.getY())) {
                    yAligned = true;
                    groundY = mutant.findGroundY();
                    mutant.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }
            if (!grounded) {
                if (mutant.moveToAttackHeight(groundY)) {
                    grounded = true;
                    mutant.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (preparationTimer > 0) {
                preparationTimer--;
                return;
            }
            if (--waveTimer <= 0) {
                spawnWaterSpearWave();
                wavesRemaining--;
                waveTimer = WATER_SPEAR_CENTER_INTERVAL;
            }
        }

        private void spawnWaterSpearWave() {
            double angleOffset = mutant.getRandom().nextDouble() * Math.PI * 2.0D;
            for (int i = 0; i < WATER_SPEAR_CENTER_SPEARS_PER_WAVE; i++) {
                double angle = angleOffset + i * Math.PI * 2.0D / WATER_SPEAR_CENTER_SPEARS_PER_WAVE;
                Vec3 direction = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
                double x = target.getX() - direction.x * WATER_SPEAR_CENTER_RANGE;
                double y = target.getY();
                double z = target.getZ() - direction.z * WATER_SPEAR_CENTER_RANGE;
                Water_Spear_Entity spear = new Water_Spear_Entity(mutant, direction, mutant.level(),
                        WATER_SPEAR_CENTER_DAMAGE, WATER_SPEAR_CENTER_ACCELERATION);
                spear.setYRot((float) (Math.atan2(direction.z, direction.x) * 180.0D / Math.PI) + 90.0F);
                spear.setXRot(0.0F);
                spear.setPosRaw(x, y, z);
                mutant.level().addFreshEntity(spear);
            }
        }

        @Override
        public void stop() {
            if (mutant.phase == PHASE_3 && wavesRemaining <= 0) {
                mutant.selectNextPhaseThreeAttack(ATTACK_PHASE_THREE_WATER_SPEAR_CENTER);
            }
            mutant.setDeltaMovement(Vec3.ZERO);
            target = null;
        }
    }

    private class WaterSpearParallelAttack extends Goal {
        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        private int wavesRemaining;
        private int waveTimer;
        private int preparationTimer;
        private double lastAngle;
        private double pendingAngle;
        private Vec3 pendingTargetPosition = Vec3.ZERO;
        private double groundY;
        private boolean yAligned;
        private boolean grounded;

        private WaterSpearParallelAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_3 && mutant.nextPhaseThreeAttack == ATTACK_PHASE_THREE_WATER_SPEAR_PARALLEL
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_3 && target != null && target.isAlive()
                    && (preparationTimer > 0 || wavesRemaining > 0);
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            wavesRemaining = WATER_SPEAR_PARALLEL_WAVES;
            waveTimer = 0;
            preparationTimer = PHASE_THREE_PREPARE_TIME;
            lastAngle = Double.NaN;
            pendingAngle = Double.NaN;
            groundY = mutant.getY();
            yAligned = false;
            grounded = false;
        }

        @Override
        public void tick() {
            if (!yAligned) {
                if (mutant.moveToAttackHeight(target.getY())) {
                    yAligned = true;
                    groundY = mutant.findGroundY();
                    mutant.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }
            if (!grounded) {
                if (mutant.moveToAttackHeight(groundY)) {
                    grounded = true;
                    mutant.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }

            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (preparationTimer > 0) {
                preparationTimer--;
                return;
            }
            if (Double.isNaN(pendingAngle)) {
                pendingAngle = chooseWaveAngle();
                pendingTargetPosition = target.position();
                waveTimer = WATER_SPEAR_PARALLEL_INTERVAL;
            }
            showPredictionLines();
            if (--waveTimer <= 0) {
                spawnWaterSpearWave(pendingAngle, pendingTargetPosition);
                wavesRemaining--;
                pendingAngle = Double.NaN;
            }
        }

        private void spawnWaterSpearWave(double angle, Vec3 targetPosition) {
            Vec3 direction = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
            Vec3 side = new Vec3(-direction.z, 0.0D, direction.x);
            Vec3 startingPoint = new Vec3(targetPosition.x - direction.x * WATER_SPEAR_PARALLEL_RANGE,
                    targetPosition.y, targetPosition.z - direction.z * WATER_SPEAR_PARALLEL_RANGE);
            int centerIndex = WATER_SPEAR_PARALLEL_SPEARS_PER_WAVE / 2;
            for (int i = 0; i < WATER_SPEAR_PARALLEL_SPEARS_PER_WAVE; i++) {
                Vec3 spawnPosition = startingPoint.add(side.scale((i - centerIndex) * WATER_SPEAR_PARALLEL_SPACING));
                Water_Spear_Entity spear = new Water_Spear_Entity(mutant, direction, mutant.level(),
                        WATER_SPEAR_PARALLEL_DAMAGE, WATER_SPEAR_PARALLEL_ACCELERATION);
                spear.setYRot((float) (Math.atan2(direction.z, direction.x) * 180.0D / Math.PI) + 90.0F);
                spear.setXRot(0.0F);
                spear.setPosRaw(spawnPosition.x, spawnPosition.y, spawnPosition.z);
                mutant.level().addFreshEntity(spear);
            }
            lastAngle = angle;
        }

        private void showPredictionLines() {
            if (waveTimer % WATER_SPEAR_PARALLEL_PREDICTION_INTERVAL != 0 || !(mutant.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            Vec3 direction = new Vec3(Math.cos(pendingAngle), 0.0D, Math.sin(pendingAngle));
            Vec3 side = new Vec3(-direction.z, 0.0D, direction.x);
            Vec3 startingPoint = new Vec3(pendingTargetPosition.x - direction.x * WATER_SPEAR_PARALLEL_RANGE,
                    pendingTargetPosition.y + 0.25D, pendingTargetPosition.z - direction.z * WATER_SPEAR_PARALLEL_RANGE);
            int centerIndex = WATER_SPEAR_PARALLEL_SPEARS_PER_WAVE / 2;
            for (int spearIndex = 0; spearIndex < WATER_SPEAR_PARALLEL_SPEARS_PER_WAVE; spearIndex++) {
                Vec3 lineStart = startingPoint.add(side.scale((spearIndex - centerIndex) * WATER_SPEAR_PARALLEL_SPACING));
                for (int point = 0; point < WATER_SPEAR_PARALLEL_PREDICTION_POINTS; point++) {
                    double distance = point * WATER_SPEAR_PARALLEL_PREDICTION_LENGTH
                            / (double) (WATER_SPEAR_PARALLEL_PREDICTION_POINTS - 1);
                    Vec3 particlePosition = lineStart.add(direction.scale(distance));
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, particlePosition.x, particlePosition.y, particlePosition.z,
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }

        private double chooseWaveAngle() {
            double minimumDifference = Math.toRadians(WATER_SPEAR_PARALLEL_MIN_ANGLE_DEGREES);
            if (Double.isNaN(lastAngle)) {
                return mutant.getRandom().nextDouble() * Math.PI * 2.0D;
            }
            for (int attempt = 0; attempt < 32; attempt++) {
                double candidate = mutant.getRandom().nextDouble() * Math.PI * 2.0D;
                double difference = Math.abs(candidate - lastAngle) % (Math.PI * 2.0D);
                difference = Math.min(difference, Math.PI * 2.0D - difference);
                if (difference >= minimumDifference) {
                    return candidate;
                }
            }
            return (lastAngle + Math.PI) % (Math.PI * 2.0D);
        }

        @Override
        public void stop() {
            if (mutant.phase == PHASE_3 && wavesRemaining <= 0) {
                mutant.selectNextPhaseThreeAttack(ATTACK_PHASE_THREE_WATER_SPEAR_PARALLEL);
            }
            mutant.setDeltaMovement(Vec3.ZERO);
            target = null;
        }
    }

    private class LaserSpreadAttack extends Goal {
        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        private int burstsRemaining;
        private int burstTimer;
        private int preparationTimer;
        private double groundY;
        private boolean yAligned;
        private boolean grounded;

        private LaserSpreadAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            boolean selectedForPhaseThree = mutant.phase == PHASE_3
                    && mutant.nextPhaseThreeAttack == ATTACK_PHASE_THREE_LASER_SPREAD;
            boolean selectedForPhaseFour = mutant.phase == PHASE_4
                    && mutant.nextPhaseFourAttack == ATTACK_PHASE_FOUR_LASER_SPREAD;
            return (selectedForPhaseThree || selectedForPhaseFour)
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return (mutant.phase == PHASE_3 || mutant.phase == PHASE_4) && target != null && target.isAlive()
                    && (preparationTimer > 0 || burstsRemaining > 0);
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            burstsRemaining = LASER_SPREAD_BURSTS;
            burstTimer = 0;
            preparationTimer = mutant.phase == PHASE_4 ? PHASE_FOUR_ATTACK_PREPARE_TIME : PHASE_THREE_PREPARE_TIME;
            groundY = mutant.getY();
            yAligned = false;
            grounded = false;
        }

        @Override
        public void tick() {
            if (!yAligned) {
                if (mutant.moveToAttackHeight(target.getY())) {
                    yAligned = true;
                    groundY = mutant.findGroundY();
                    mutant.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }
            if (!grounded) {
                if (mutant.moveToAttackHeight(groundY)) {
                    grounded = true;
                    mutant.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
            if (preparationTimer > 0) {
                preparationTimer--;
                return;
            }
            if (--burstTimer <= 0) {
                fireLaserBurst();
                burstsRemaining--;
                burstTimer = LASER_SPREAD_INTERVAL;
            }
        }

        private void fireLaserBurst() {
            double shift = mutant.level().getGameTime();
            Vec3 start = new Vec3(mutant.getX(), mutant.getY() + LASER_SPREAD_ORIGIN_HEIGHT, mutant.getZ());
            for (int arm = 0; arm < LASER_SPREAD_ARMS; arm++) {
                double angle = arm * Math.PI * 2.0D / LASER_SPREAD_ARMS + shift;
                Vec3 direction = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
                MutantLaserBeamEntity laser = new MutantLaserBeamEntity(ModEntities.MUTANT_LASER_BEAM.get(), mutant,
                        start.x, start.y, start.z, direction, LASER_SPREAD_DAMAGE, mutant.level());
                laser.setDeltaMovement(direction);
                laser.setInitialSpeed(LASER_SPREAD_INITIAL_SPEED);
                laser.setYRot((float) (Math.atan2(direction.z, direction.x) * 180.0D / Math.PI) + 90.0F);
                laser.setXRot(0.0F);
                mutant.level().addFreshEntity(laser);
            }
        }

        @Override
        public void stop() {
            if (mutant.phase == PHASE_3 && burstsRemaining <= 0) {
                mutant.selectNextPhaseThreeAttack(ATTACK_PHASE_THREE_LASER_SPREAD);
            }
            else if (mutant.phase == PHASE_4 && burstsRemaining <= 0) {
                mutant.nextPhaseFourAttack = ATTACK_PHASE_FOUR_FINAL_SPARK;
            }
            mutant.setDeltaMovement(Vec3.ZERO);
            target = null;
        }
    }

    private class LaserMissileAttack extends Goal {
        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        private int missileBurstsRemaining;
        private int missileTimer;
        private int restTimer;
        private int preparationTimer;
        private double groundY;
        private boolean yAligned;
        private boolean grounded;

        private LaserMissileAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_3 && mutant.nextPhaseThreeAttack == ATTACK_PHASE_THREE_LASER_MISSILE
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_3 && target != null && target.isAlive()
                    && (preparationTimer > 0 || missileBurstsRemaining > 0 || restTimer > 0);
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            missileBurstsRemaining = LASER_MISSILE_MISSILE_BURSTS;
            missileTimer = 0;
            restTimer = 0;
            preparationTimer = PHASE_THREE_PREPARE_TIME;
            groundY = mutant.getY();
            yAligned = false;
            grounded = false;
        }

        @Override
        public void tick() {
            if (!yAligned) {
                if (mutant.moveToAttackHeight(target.getY())) {
                    yAligned = true;
                    groundY = mutant.findGroundY();
                    mutant.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }
            if (!grounded) {
                if (mutant.moveToAttackHeight(groundY)) {
                    grounded = true;
                    mutant.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (preparationTimer > 0) {
                preparationTimer--;
                return;
            }
            if (missileBurstsRemaining > 0 && --missileTimer <= 0) {
                fireMissile();
                missileBurstsRemaining--;
                missileTimer = LASER_MISSILE_MISSILE_INTERVAL;
                if (missileBurstsRemaining == 0) {
                    restTimer = LASER_MISSILE_REST_TIME;
                }
            }
            else if (restTimer > 0) {
                restTimer--;
            }
        }

        private void fireMissile() {
            Vec3 start = new Vec3(mutant.getX(), mutant.getY() + LASER_MISSILE_ORIGIN_HEIGHT, mutant.getZ());
            Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
            Vec3 shotDirection = rotateWithRandomness(direction,
                    LASER_MISSILE_MISSILE_SIDE * LASER_MISSILE_MISSILE_SIDE_ANGLE);
            MutantWitherMissileEntity missile = new MutantWitherMissileEntity(mutant, shotDirection,
                    mutant.level(), LASER_MISSILE_MISSILE_DAMAGE, target, LASER_MISSILE_MISSILE_SPEED,
                    LASER_MISSILE_MISSILE_LAUNCH_TIME);
            missile.setPosRaw(start.x, start.y, start.z);
            missile.setYRot((float) (Math.atan2(shotDirection.z, shotDirection.x) * 180.0D / Math.PI) + 90.0F);
            missile.setXRot((float) -Math.toDegrees(Math.atan2(shotDirection.y,
                    Math.sqrt(shotDirection.x * shotDirection.x + shotDirection.z * shotDirection.z))));
            mutant.level().addFreshEntity(missile);
        }

        private Vec3 rotateWithRandomness(Vec3 direction, float baseAngle) {
            float randomAngle = (mutant.getRandom().nextFloat() * 2.0F - 1.0F) * LASER_MISSILE_ANGLE_RANDOMNESS;
            return direction.yRot((float) Math.toRadians(baseAngle + randomAngle)).normalize();
        }

        @Override
        public void stop() {
            if (mutant.phase == PHASE_3 && missileBurstsRemaining <= 0 && restTimer <= 0) {
                mutant.selectNextPhaseThreeAttack(ATTACK_PHASE_THREE_LASER_MISSILE);
            }
            mutant.setDeltaMovement(Vec3.ZERO);
            target = null;
        }
    }

    private class HalberdAttack extends Goal {
        private static final int PHASE_ALIGN = 0;
        private static final int PHASE_DESCEND = 1;
        private static final int PHASE_MARK = 2;
        private static final int PHASE_FLY_TO_SMASH = 3;
        private static final int PHASE_SMASH = 4;
        private static final int PHASE_REST = 5;
        private static final int PHASE_DONE = 6;

        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        private Vec3 arenaCenter = Vec3.ZERO;
        private Vec3 slamPosition = Vec3.ZERO;
        private int attackPhase;
        private int phaseTimer;
        private int roundsCompleted;
        private double previousSlamAngle;
        private double groundY;
        private boolean ringDetached;

        private HalberdAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_3 && mutant.nextPhaseThreeAttack == ATTACK_PHASE_THREE_HALBERD
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_3 && target != null && target.isAlive() && attackPhase != PHASE_DONE;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            ringDetached = false;
            roundsCompleted = 0;
            previousSlamAngle = Double.NaN;
            groundY = mutant.getY();
            attackPhase = PHASE_ALIGN;
        }

        @Override
        public void tick() {
            if (target == null) {
                attackPhase = PHASE_DONE;
                return;
            }

            switch (attackPhase) {
                case PHASE_ALIGN -> alignToTargetY();
                case PHASE_DESCEND -> descendToGround();
                case PHASE_MARK -> markSlamPosition();
                case PHASE_FLY_TO_SMASH -> flyToSlamPosition();
                case PHASE_SMASH -> smashGround();
                case PHASE_REST -> rest();
                default -> {
                }
            }
        }

        private void alignToTargetY() {
            if (mutant.moveToAttackHeight(target.getY())) {
                groundY = mutant.findGroundY();
                mutant.setDeltaMovement(Vec3.ZERO);
                attackPhase = PHASE_DESCEND;
            }
        }

        private void descendToGround() {
            if (mutant.moveToAttackHeight(groundY)) {
                mutant.setDeltaMovement(Vec3.ZERO);
                arenaCenter = mutant.position();
                detachRestrictionRing();
                beginMark();
            }
        }

        private void beginMark() {
            double angle = chooseSlamAngle();
            double maximumDistance = RESTRICTION_BOUNDARY_RADIUS - HALBERD_ARENA_EDGE_PADDING;
            double distance = Math.sqrt(mutant.getRandom().nextDouble()) * maximumDistance;
            double x = arenaCenter.x + Math.cos(angle) * distance;
            double z = arenaCenter.z + Math.sin(angle) * distance;
            double y = mutant.findGroundYAt(x, arenaCenter.y, z);
            slamPosition = new Vec3(x, y, z);
            previousSlamAngle = angle;
            phaseTimer = HALBERD_MARK_TIME;
            attackPhase = PHASE_MARK;
        }

        private double chooseSlamAngle() {
            if (Double.isNaN(previousSlamAngle)) {
                return mutant.getRandom().nextDouble() * Math.PI * 2.0D;
            }

            double minimumDifference = Math.toRadians(HALBERD_MIN_MARK_ANGLE_DIFFERENCE);
            for (int attempt = 0; attempt < 32; attempt++) {
                double candidate = mutant.getRandom().nextDouble() * Math.PI * 2.0D;
                double difference = Math.abs(candidate - previousSlamAngle) % (Math.PI * 2.0D);
                difference = Math.min(difference, Math.PI * 2.0D - difference);
                if (difference >= minimumDifference) {
                    return candidate;
                }
            }
            return (previousSlamAngle + Math.PI) % (Math.PI * 2.0D);
        }

        private void markSlamPosition() {
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
            if (mutant.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ModParticle.CURSED_FLAME.get(), slamPosition.x, slamPosition.y + 0.1D, slamPosition.z,
                        HALBERD_MARK_PARTICLE_COUNT, HALBERD_MARK_RADIUS, 0.05D, HALBERD_MARK_RADIUS, 0.0D);
            }
            if (--phaseTimer <= 0) {
                attackPhase = PHASE_FLY_TO_SMASH;
            }
        }

        private void flyToSlamPosition() {
            Vec3 aboveSlam = slamPosition.add(0.0D, HALBERD_SMASH_HEIGHT, 0.0D);
            mutant.getMoveControl().setWantedPosition(aboveSlam.x, aboveSlam.y, aboveSlam.z, HALBERD_MOVE_SPEED);
            if (mutant.position().distanceToSqr(aboveSlam) <= mutant.getBoundingBox().getSize() * mutant.getBoundingBox().getSize()) {
                mutant.teleportTo(aboveSlam.x, aboveSlam.y, aboveSlam.z);
                mutant.setDeltaMovement(Vec3.ZERO);
                attackPhase = PHASE_SMASH;
            }
        }

        private void smashGround() {
            mutant.setDeltaMovement(0.0D, -HALBERD_SMASH_SPEED, 0.0D);
            if (mutant.getY() <= slamPosition.y) {
                mutant.teleportTo(slamPosition.x, slamPosition.y, slamPosition.z);
                mutant.setDeltaMovement(Vec3.ZERO);
                hurtSlamContacts();
                spawnHalberdSpiral();
                roundsCompleted++;
                if (roundsCompleted >= HALBERD_ATTACK_ROUNDS) {
                    attackPhase = PHASE_DONE;
                }
                else {
                    phaseTimer = HALBERD_REST_TIME;
                    attackPhase = PHASE_REST;
                }
            }
        }

        private void hurtSlamContacts() {
            AABB slamArea = new AABB(slamPosition, slamPosition).inflate(HALBERD_SMASH_HITBOX_RADIUS);
            for (LivingEntity entity : mutant.level().getEntitiesOfClass(LivingEntity.class, slamArea,
                    entity -> entity != mutant && entity.isAlive() && !mutant.isAlliedTo(entity))) {
                entity.hurt(mutant.damageSources().mobAttack(mutant), HALBERD_SMASH_DAMAGE);
            }
        }

        private void spawnHalberdSpiral() {
            for (int arm = 0; arm < HALBERD_SPIRAL_ARMS; arm++) {
                double theta = arm * 360.0D / HALBERD_SPIRAL_ARMS;
                for (int index = 1; index <= HALBERD_SPIRAL_PER_ARM; index++) {
                    double phi = 60.0D * index / HALBERD_SPIRAL_PER_ARM;
                    double angle = 90.0D - phi / 2.0D + theta;
                    double length = HALBERD_SPIRAL_RADIUS * Math.sin(Math.toRadians(phi / 2.0D));
                    double radians = Math.toRadians(angle);
                    double x = slamPosition.x + Math.cos(radians) * length;
                    double z = slamPosition.z + Math.sin(radians) * length;
                    Phantom_Halberd_Entity halberd = new Phantom_Halberd_Entity(mutant.level(), x, slamPosition.y, z,
                            (float) radians, HALBERD_SPIRAL_WARMUP, mutant, HALBERD_SPIRAL_DAMAGE);
                    mutant.level().addFreshEntity(halberd);
                }
            }
        }

        private void rest() {
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
            if (--phaseTimer <= 0) {
                beginMark();
            }
        }

        private void detachRestrictionRing() {
            arenaCenter = new Vec3(arenaCenter.x,
                    mutant.clearGroundPlacement(arenaCenter.x, arenaCenter.y, arenaCenter.z), arenaCenter.z);
            AABB searchArea = new AABB(arenaCenter, arenaCenter).inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
            for (RotatingSoulShieldEntity shield : mutant.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == mutant)) {
                shield.switchToAroundPosition(arenaCenter.x, arenaCenter.y, arenaCenter.z);
            }
            ringDetached = true;
        }

        private void restoreRestrictionRing() {
            if (!ringDetached) {
                return;
            }
            AABB searchArea = new AABB(arenaCenter, arenaCenter).inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
            for (RotatingSoulShieldEntity shield : mutant.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == mutant)) {
                shield.switchToAroundCaster();
            }
            ringDetached = false;
        }

        @Override
        public void stop() {
            if (mutant.phase == PHASE_3 && roundsCompleted >= HALBERD_ATTACK_ROUNDS) {
                mutant.selectNextPhaseThreeAttack(ATTACK_PHASE_THREE_HALBERD);
            }
            restoreRestrictionRing();
            mutant.setDeltaMovement(Vec3.ZERO);
            target = null;
        }
    }

    private class VoidRuneAttack extends Goal {
        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        private Vec3 runeOrigin = Vec3.ZERO;
        private int preparationTimer;
        private int roundTimer;
        private int roundsRemaining;
        private double groundY;
        private boolean yAligned;
        private boolean grounded;

        private VoidRuneAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_3 && mutant.nextPhaseThreeAttack == ATTACK_PHASE_THREE_VOID_RUNE
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_3 && target != null && target.isAlive() && roundsRemaining > 0;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            runeOrigin = Vec3.ZERO;
            preparationTimer = VOID_RUNE_ATTACK_PREPARE_TIME;
            roundTimer = 0;
            roundsRemaining = VOID_RUNE_ATTACK_ROUNDS;
            groundY = mutant.getY();
            yAligned = false;
            grounded = false;
        }

        @Override
        public void tick() {
            if (!yAligned) {
                if (mutant.moveToAttackHeight(target.getY())) {
                    yAligned = true;
                    groundY = mutant.findGroundY();
                    mutant.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }
            if (!grounded) {
                if (mutant.moveToAttackHeight(groundY)) {
                    grounded = true;
                    runeOrigin = mutant.position();
                    mutant.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }

            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(runeOrigin.x, runeOrigin.y, runeOrigin.z, 1.0D);
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (preparationTimer > 0) {
                preparationTimer--;
                return;
            }
            if (--roundTimer <= 0) {
                spawnRuneLine();
                roundsRemaining--;
                roundTimer = VOID_RUNE_ATTACK_INTERVAL;
            }
        }

        private void spawnRuneLine() {
            double angle = (mutant.level().getGameTime() % VOID_RUNE_ROTATION_PERIOD)
                    / (double) VOID_RUNE_ROTATION_PERIOD * Math.PI * 2.0D;
            for (int index = 1; index <= VOID_RUNE_LINE_LENGTH; index++) {
                double distance = index + 1.0D;
                double x = runeOrigin.x + Math.cos(angle) * distance;
                double z = runeOrigin.z + Math.sin(angle) * distance;
                Void_Rune_Entity rune = new Void_Rune_Entity(mutant.level(), x, runeOrigin.y, z, (float) angle,
                        VOID_RUNE_WARMUP, VOID_RUNE_DAMAGE, mutant);
                mutant.level().addFreshEntity(rune);
            }
        }

        @Override
        public void stop() {
            if (mutant.phase == PHASE_3 && roundsRemaining <= 0) {
                mutant.selectNextPhaseThreeAttack(ATTACK_PHASE_THREE_VOID_RUNE);
            }
            mutant.setDeltaMovement(Vec3.ZERO);
            target = null;
        }
    }

    private class AbyssalAttack extends Goal {
        private static final int PHASE_ALIGN = 0;
        private static final int PHASE_DESCEND = 1;
        private static final int PHASE_PREDICT = 2;
        private static final int PHASE_ROTATE = 3;
        private static final int PHASE_WAIT_FOR_LASER = 4;
        private static final int PHASE_RECOVER = 5;
        private static final int PHASE_DONE = 6;
        private static final int POST_LASER_RECOVERY_TIME = 20;

        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        @Nullable
        private MutantLeviathanLaserEntity laser;
        private Vec3 arenaCenter = Vec3.ZERO;
        private double groundY;
        private float initialYaw;
        private float lockedYaw;
        private int phaseTimer;
        private int attackPhase;

        private AbyssalAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_3 && mutant.nextPhaseThreeAttack == ATTACK_PHASE_THREE_ABYSSAL
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_3 && target != null && target.isAlive() && attackPhase != PHASE_DONE;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            groundY = mutant.getY();
            phaseTimer = 0;
            attackPhase = PHASE_ALIGN;
            laser = null;
        }

        @Override
        public void tick() {
            switch (attackPhase) {
                case PHASE_ALIGN -> alignToTarget();
                case PHASE_DESCEND -> descendToGround();
                case PHASE_PREDICT -> predictLaser();
                case PHASE_ROTATE -> rotateLaserTowardTarget();
                case PHASE_WAIT_FOR_LASER -> waitForLaser();
                case PHASE_RECOVER -> recoverAfterLaser();
                default -> {
                }
            }
        }

        private void alignToTarget() {
            if (mutant.moveToAttackHeight(target.getY())) {
                groundY = mutant.findGroundY();
                mutant.setDeltaMovement(Vec3.ZERO);
                attackPhase = PHASE_DESCEND;
            }
        }

        private void descendToGround() {
            if (mutant.moveToAttackHeight(groundY)) {
                mutant.setDeltaMovement(Vec3.ZERO);
                arenaCenter = mutant.position();
                spawnMines();
                float offset = mutant.getRandom().nextBoolean() ? ABYSSAL_INITIAL_ANGLE_DEGREES : -ABYSSAL_INITIAL_ANGLE_DEGREES;
                initialYaw = Mth.wrapDegrees(yawTowardsTarget() + offset);
                setFacing(initialYaw);
                phaseTimer = ABYSSAL_PREDICTION_TIME;
                attackPhase = PHASE_PREDICT;
            }
        }

        private void spawnMines() {
            double maximumDistance = RESTRICTION_BOUNDARY_RADIUS - ABYSSAL_MINE_ARENA_PADDING;
            for (int index = 0; index < ABYSSAL_MINE_COUNT; index++) {
                double angle = mutant.getRandom().nextDouble() * Math.PI * 2.0D;
                double distance = Math.sqrt(mutant.getRandom().nextDouble()) * maximumDistance;
                double x = arenaCenter.x + Math.cos(angle) * distance;
                double z = arenaCenter.z + Math.sin(angle) * distance;
                double y = mutant.findGroundYAt(x, arenaCenter.y, z);
                Abyss_Mine_Entity mine = new Abyss_Mine_Entity(mutant.level(), x, y, z, (float) angle,
                        ABYSSAL_MINE_WARMUP, mutant);
                mutant.level().addFreshEntity(mine);
            }
        }

        private void predictLaser() {
            holdPosition();
            setFacing(initialYaw);
            lockLookToYaw(initialYaw);
            spawnPredictionWall();
            if (--phaseTimer <= 0) {
                laser = new MutantLeviathanLaserEntity(ModEntities.MUTANT_LEVIATHAN_LASER.get(),
                        mutant.level(), mutant);
                laser.setPos(mutant.getX(), mutant.getY() + mutant.getBbHeight() * 0.5D, mutant.getZ());
                laser.setLifespan(ABYSSAL_LASER_ROTATION_TIME + MUTANT_LEVIATHAN_LASER_PORTAL_LIFESPAN_BUFFER);
                laser.setBeamDuration(ABYSSAL_LASER_ROTATION_TIME);
                laser.setBeamDamage(ABYSSAL_LASER_DAMAGE, ABYSSAL_LASER_HP_DAMAGE);
                if (!mutant.level().addFreshEntity(laser)) {
                    laser.discardWithBeam();
                }
                phaseTimer = ABYSSAL_LASER_ROTATION_TIME;
                attackPhase = PHASE_ROTATE;
            }
        }

        private void rotateLaserTowardTarget() {
            holdPosition();
            float yawDifference = Mth.wrapDegrees(yawTowardsTarget() - mutant.getYHeadRot());
            float nextYaw = mutant.getYHeadRot() + Mth.clamp(yawDifference, -ABYSSAL_LASER_TURN_DEGREES_PER_TICK,
                    ABYSSAL_LASER_TURN_DEGREES_PER_TICK);
            setFacing(nextYaw);
            lockLookToYaw(nextYaw);
            if (--phaseTimer <= 0) {
                lockedYaw = Mth.wrapDegrees(nextYaw);
                attackPhase = PHASE_WAIT_FOR_LASER;
            }
        }

        private void waitForLaser() {
            holdFinalFacing();
            // The beam outlives the rotation timer and can also outlive its portal.
            if (laser == null || !laser.isLaserActive()) {
                phaseTimer = POST_LASER_RECOVERY_TIME;
                attackPhase = PHASE_RECOVER;
            }
        }

        private void recoverAfterLaser() {
            holdFinalFacing();
            if (--phaseTimer <= 0) {
                attackPhase = PHASE_DONE;
            }
        }

        private void holdFinalFacing() {
            holdPosition();
            setFacing(lockedYaw);
            lockLookToYaw(lockedYaw);
        }

        private void holdPosition() {
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(arenaCenter.x, arenaCenter.y, arenaCenter.z, 1.0D);
        }

        private float yawTowardsTarget() {
            Vec3 direction = target.position().subtract(mutant.position());
            return (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F;
        }

        private void setFacing(float yaw) {
            float wrappedYaw = Mth.wrapDegrees(yaw);
            mutant.setYRot(wrappedYaw);
            mutant.setYHeadRot(wrappedYaw);
            mutant.setYBodyRot(wrappedYaw);
            mutant.setXRot(0.0F);
        }

        private void lockLookToYaw(float yaw) {
            double yawRadians = Math.toRadians(yaw);
            double lookX = mutant.getX() - Math.sin(yawRadians) * 16.0D;
            double lookZ = mutant.getZ() + Math.cos(yawRadians) * 16.0D;
            mutant.getLookControl().setLookAt(lookX, mutant.getEyeY(), lookZ, 360.0F, 360.0F);
        }


        private void spawnPredictionWall() {
            if (phaseTimer % ABYSSAL_PREDICTION_WALL_REFRESH_INTERVAL != 0
                    || !(mutant.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            double yawRadians = Math.toRadians(initialYaw);
            double directionX = -Math.sin(yawRadians);
            double directionZ = Math.cos(yawRadians);
            double startY = mutant.getY();
            for (int index = 1; index <= ABYSSAL_PREDICTION_PARTICLES; index++) {
                double distance = index * ABYSSAL_PREDICTION_PARTICLE_SPACING;
                for (double height = 0.0D; height <= ABYSSAL_PREDICTION_WALL_HEIGHT;
                     height += ABYSSAL_PREDICTION_WALL_VERTICAL_SPACING) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            mutant.getX() + directionX * distance, startY + height,
                            mutant.getZ() + directionZ * distance,
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }

        @Override
        public void stop() {
            if (mutant.phase == PHASE_3 && attackPhase == PHASE_DONE) {
                mutant.selectNextPhaseThreeAttack(ATTACK_PHASE_THREE_ABYSSAL);
            }
            if (laser != null) {
                laser.discardWithBeam();
                laser = null;
            }
            clearAbyssMines();
            mutant.setDeltaMovement(Vec3.ZERO);
            target = null;
        }

        private void clearAbyssMines() {
            AABB searchArea = new AABB(arenaCenter, arenaCenter).inflate(RESTRICTION_BOUNDARY_RADIUS + 4.0D);
            for (Abyss_Mine_Entity mine : mutant.level().getEntitiesOfClass(Abyss_Mine_Entity.class, searchArea,
                    mine -> mine.getCaster() == mutant)) {
                mine.discard();
            }
        }
    }

    private class FlareAttack extends Goal {
        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        private int bombsRemaining;
        private int bombTimer;
        private int preparationTimer;
        private float orbitAngle;
        private float orbitDirection;
        private double groundY;
        private boolean yAligned;
        private boolean grounded;

        private FlareAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_3 && mutant.nextPhaseThreeAttack == ATTACK_PHASE_THREE_FLARE
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_3 && target != null && target.isAlive()
                    && (preparationTimer > 0 || bombsRemaining > 0);
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            bombsRemaining = FLARE_ATTACK_BOMBS;
            bombTimer = 0;
            preparationTimer = PHASE_THREE_PREPARE_TIME;
            orbitAngle = (float) Math.atan2(mutant.getZ() - target.getZ(), mutant.getX() - target.getX());
            orbitDirection = mutant.getRandom().nextBoolean() ? 1.0F : -1.0F;
            groundY = mutant.getY();
            yAligned = false;
            grounded = false;
        }

        @Override
        public void tick() {
            if (!yAligned) {
                if (mutant.moveToAttackHeight(target.getY())) {
                    yAligned = true;
                    groundY = mutant.findGroundY();
                    mutant.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }
            if (!grounded) {
                if (mutant.moveToAttackHeight(groundY)) {
                    grounded = true;
                    mutant.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }
            if (preparationTimer > 0) {
                mutant.setDeltaMovement(Vec3.ZERO);
                mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
                mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
                preparationTimer--;
                return;
            }
            orbitAngle += orbitDirection * FLARE_STRAFE_ANGULAR_SPEED;
            double x = target.getX() + Math.cos(orbitAngle) * FLARE_STRAFE_RADIUS;
            double z = target.getZ() + Math.sin(orbitAngle) * FLARE_STRAFE_RADIUS;
            mutant.getMoveControl().setWantedPosition(x, target.getY(), z, FLARE_STRAFE_MOVE_SPEED);
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (--bombTimer <= 0) {
                throwFlareBomb();
                bombsRemaining--;
                bombTimer = FLARE_ATTACK_INTERVAL;
            }
        }

        private void throwFlareBomb() {
            Vec3 start = new Vec3(mutant.getX(), mutant.getEyeY(), mutant.getZ());
            Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
            MutantFlareBombEntity bomb = new MutantFlareBombEntity(mutant.level(), mutant);
            bomb.setPos(start);
            bomb.shoot(direction.x, direction.y, direction.z, FLARE_BOMB_SPEED, FLARE_BOMB_INACCURACY);
            mutant.level().addFreshEntity(bomb);
        }

        @Override
        public void stop() {
            if (mutant.phase == PHASE_3 && bombsRemaining <= 0) {
                mutant.selectNextPhaseThreeAttack(ATTACK_PHASE_THREE_FLARE);
            }
            target = null;
        }
    }

    private class IgnisAttack extends Goal {
        private static final int PHASE_ALIGN = 0;
        private static final int PHASE_DESCEND = 1;
        private static final int PHASE_PREPARE = 2;
        private static final int PHASE_WAIT_FOR_MIDDLE_WAVE = 3;
        private static final int PHASE_WAIT_FOR_OUTER_WAVE = 4;
        private static final int PHASE_DONE = 5;

        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        private Vec3 arenaCenter = Vec3.ZERO;
        private double groundY;
        private int phaseTimer;
        private int attackPhase;

        private IgnisAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_3 && mutant.nextPhaseThreeAttack == ATTACK_PHASE_THREE_IGNIS
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_3 && target != null && target.isAlive() && attackPhase != PHASE_DONE;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            groundY = mutant.getY();
            phaseTimer = 0;
            attackPhase = PHASE_ALIGN;
            mutant.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.THE_INCINERATOR.get()));
        }

        @Override
        public void tick() {
            switch (attackPhase) {
                case PHASE_ALIGN -> alignToTargetY();
                case PHASE_DESCEND -> descendToGround();
                case PHASE_PREPARE -> prepareAttack();
                case PHASE_WAIT_FOR_MIDDLE_WAVE -> waitForMiddleWave();
                case PHASE_WAIT_FOR_OUTER_WAVE -> waitForOuterWave();
                default -> {
                }
            }
        }

        private void alignToTargetY() {
            if (mutant.moveToAttackHeight(target.getY())) {
                groundY = mutant.findGroundY();
                mutant.setDeltaMovement(Vec3.ZERO);
                attackPhase = PHASE_DESCEND;
            }
        }

        private void descendToGround() {
            if (mutant.moveToAttackHeight(groundY)) {
                mutant.setDeltaMovement(Vec3.ZERO);
                arenaCenter = mutant.position();
                phaseTimer = PHASE_THREE_PREPARE_TIME;
                attackPhase = PHASE_PREPARE;
            }
        }

        private void prepareAttack() {
            holdPosition();
            if (--phaseTimer <= 0) {
                spawnFallingBlockBand(0.0D, RESTRICTION_BOUNDARY_RADIUS / 3.0D);
                spawnFlameStrike();
                phaseTimer = IGNIS_WAVE_INTERVAL;
                attackPhase = PHASE_WAIT_FOR_MIDDLE_WAVE;
            }
        }

        private void waitForMiddleWave() {
            holdPosition();
            if (--phaseTimer <= 0) {
                spawnFallingBlockBand(RESTRICTION_BOUNDARY_RADIUS / 3.0D,
                        RESTRICTION_BOUNDARY_RADIUS * 2.0D / 3.0D);
                phaseTimer = IGNIS_WAVE_INTERVAL;
                attackPhase = PHASE_WAIT_FOR_OUTER_WAVE;
            }
        }

        private void waitForOuterWave() {
            holdPosition();
            if (--phaseTimer <= 0) {
                spawnFallingBlockBand(RESTRICTION_BOUNDARY_RADIUS * 2.0D / 3.0D,
                        RESTRICTION_BOUNDARY_RADIUS);
                spawnFallingBlockBand(0.0D, RESTRICTION_BOUNDARY_RADIUS / 3.0D);
                attackPhase = PHASE_DONE;
            }
        }

        private void holdPosition() {
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(arenaCenter.x, arenaCenter.y, arenaCenter.z, 1.0D);
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
        }

        private void spawnFlameStrike() {
            Flame_Strike_Entity flameStrike = new Flame_Strike_Entity(mutant.level(), arenaCenter.x, arenaCenter.y,
                    arenaCenter.z, 0.0F, IGNIS_FLAME_STRIKE_DURATION, IGNIS_FLAME_STRIKE_WAIT_TIME,
                    IGNIS_FLAME_STRIKE_DELAY, IGNIS_FLAME_STRIKE_RADIUS, IGNIS_FLAME_STRIKE_DAMAGE,
                    IGNIS_FLAME_STRIKE_HP_DAMAGE, false, mutant);
            mutant.level().addFreshEntity(flameStrike);
        }

        private void spawnFallingBlockBand(double minimumRadius, double maximumRadius) {
            int firstRadius = Math.max(0, Mth.ceil(minimumRadius));
            int lastRadius = Mth.floor(maximumRadius);
            for (int radius = firstRadius; radius <= lastRadius; radius++) {
                if (radius == 0) {
                    spawnFallingBlockAt(arenaCenter.x, arenaCenter.z);
                    continue;
                }
                int points = Mth.ceil(radius * Math.PI * 2.0D);
                for (int point = 0; point < points; point++) {
                    double angle = point * Math.PI * 2.0D / points;
                    spawnFallingBlockAt(arenaCenter.x + Math.cos(angle) * radius,
                            arenaCenter.z + Math.sin(angle) * radius);
                }
            }
            damageFallingBlockBand(minimumRadius, maximumRadius);
        }

        private void damageFallingBlockBand(double minimumRadius, double maximumRadius) {
            double minimumRadiusSqr = minimumRadius * minimumRadius;
            double maximumRadiusSqr = maximumRadius * maximumRadius;
            AABB area = new AABB(arenaCenter, arenaCenter).inflate(maximumRadius, maximumRadius, maximumRadius);
            for (LivingEntity entity : mutant.level().getEntitiesOfClass(LivingEntity.class, area,
                    entity -> entity != mutant && entity.isAlive() && !mutant.isAlliedTo(entity))) {
                double xDifference = entity.getX() - arenaCenter.x;
                double zDifference = entity.getZ() - arenaCenter.z;
                double horizontalDistanceSqr = xDifference * xDifference + zDifference * zDifference;
                if (horizontalDistanceSqr >= minimumRadiusSqr && horizontalDistanceSqr <= maximumRadiusSqr) {
                    entity.hurt(mutant.damageSources().mobAttack(mutant), IGNIS_FALLING_BLOCK_DAMAGE);
                }
            }
        }

        private void spawnFallingBlockAt(double x, double z) {
            int hitX = Mth.floor(x);
            int hitY = Mth.floor(arenaCenter.y - 0.5D);
            int hitZ = Mth.floor(z);
            BlockPos position = new BlockPos(hitX, hitY, hitZ);
            BlockState block = mutant.level().getBlockState(position);
            for (int depth = 0; depth < 256 && block.getRenderShape() != RenderShape.MODEL; depth++) {
                position = position.below();
                block = mutant.level().getBlockState(position);
            }
            if (block.getRenderShape() != RenderShape.MODEL) {
                block = Blocks.AIR.defaultBlockState();
            }
            Cm_Falling_Block_Entity fallingBlock = new Cm_Falling_Block_Entity(mutant.level(), hitX + 0.5D, hitY + 1.0D,
                    hitZ + 0.5D, block, IGNIS_FALLING_BLOCK_DURATION);
            fallingBlock.push(0.0D, IGNIS_FALLING_BLOCK_UPWARD_SPEED
                    + mutant.getRandom().nextGaussian() * IGNIS_FALLING_BLOCK_UPWARD_VARIANCE, 0.0D);
            mutant.level().addFreshEntity(fallingBlock);
        }

        @Override
        public void stop() {
            if (mutant.phase == PHASE_3 && attackPhase == PHASE_DONE) {
                mutant.selectNextPhaseThreeAttack(ATTACK_PHASE_THREE_IGNIS);
            }
            mutant.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            mutant.setDeltaMovement(Vec3.ZERO);
            target = null;
        }
    }

    private class SandstormAttack extends Goal {
        private static final int PHASE_ALIGN = 0;
        private static final int PHASE_DESCEND = 1;
        private static final int PHASE_PREPARE = 2;
        private static final int PHASE_ACTIVE = 3;
        private static final int PHASE_DONE = 4;

        private final MutantEntity mutant;
        private final List<Sandstorm_Entity> sandstorms = new ArrayList<>();
        private final List<Void_Vortex_Entity> voidVortexes = new ArrayList<>();
        @Nullable
        private LivingEntity target;
        private Vec3 arenaCenter = Vec3.ZERO;
        private double groundY;
        private int phaseTimer;
        private int attackPhase;
        private boolean ringDetached;

        private SandstormAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_3 && mutant.nextPhaseThreeAttack == ATTACK_PHASE_THREE_SANDSTORM
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_3 && target != null && target.isAlive() && attackPhase != PHASE_DONE;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            groundY = mutant.getY();
            phaseTimer = 0;
            attackPhase = PHASE_ALIGN;
            ringDetached = false;
            sandstorms.clear();
            voidVortexes.clear();
            mutant.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.GAUNTLET_OF_MAELSTROM.get()));
        }

        @Override
        public void tick() {
            switch (attackPhase) {
                case PHASE_ALIGN -> alignToTargetY();
                case PHASE_DESCEND -> descendToGround();
                case PHASE_PREPARE -> prepareAttack();
                case PHASE_ACTIVE -> followTarget();
                default -> {
                }
            }
        }

        private void alignToTargetY() {
            if (mutant.moveToAttackHeight(target.getY())) {
                groundY = mutant.findGroundY();
                mutant.setDeltaMovement(Vec3.ZERO);
                attackPhase = PHASE_DESCEND;
            }
        }

        private void descendToGround() {
            if (mutant.moveToAttackHeight(groundY)) {
                mutant.setDeltaMovement(Vec3.ZERO);
                arenaCenter = mutant.position();
                phaseTimer = PHASE_THREE_PREPARE_TIME;
                attackPhase = PHASE_PREPARE;
            }
        }

        private void prepareAttack() {
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(arenaCenter.x, arenaCenter.y, arenaCenter.z, 1.0D);
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            if (--phaseTimer <= 0) {
                detachRestrictionRing();
                spawnSandstorms();
                spawnVoidVortexes();
                phaseTimer = SANDSTORM_ATTACK_DURATION;
                attackPhase = PHASE_ACTIVE;
            }
        }

        private void followTarget() {
            mutant.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), SANDSTORM_FOLLOW_SPEED);
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
            orbitSandstormsAroundMutant();
            hurtCollisionContacts();
            applyMutantInversion();
            if (--phaseTimer <= 0) {
                attackPhase = PHASE_DONE;
            }
        }

        private void applyMutantInversion() {
            if (mutant.getBossDifficulty() != 2 || mutant.tickCount % 20 != 0) {
                return;
            }
            double range = RESTRICTION_BOUNDARY_RADIUS;
            int duration = Math.max(20, phaseTimer + 20);
            for (Player player : mutant.level().getEntitiesOfClass(Player.class,
                    mutant.getBoundingBox().inflate(range), player -> player.isAlive() && !player.isSpectator())) {
                player.addEffect(new MobEffectInstance(ModEffects.MUTANT_INVERSION.get(), duration, 0), mutant);
            }
        }

        private void orbitSandstormsAroundMutant() {
            float currentAngle = mutant.tickCount * SANDSTORM_ORBIT_SPEED;
            for (Sandstorm_Entity sandstorm : sandstorms) {
                if (sandstorm.isRemoved()) {
                    continue;
                }
                float angle = currentAngle + sandstorm.getOffset();
                double x = mutant.getX() + Math.cos(angle) * SANDSTORM_RADIUS;
                double z = mutant.getZ() + Math.sin(angle) * SANDSTORM_RADIUS;
                sandstorm.moveTo(x, mutant.getY(), z);
            }
        }

        private void hurtCollisionContacts() {
            AABB bodyBox = mutant.getBoundingBox();
            AABB contactBox = bodyBox.inflate(bodyBox.getXsize() * 0.5D, bodyBox.getYsize() * 0.5D,
                    bodyBox.getZsize() * 0.5D);
            for (LivingEntity entity : mutant.level().getEntitiesOfClass(LivingEntity.class, contactBox,
                    entity -> entity != mutant && entity.isAlive() && !mutant.isAlliedTo(entity))) {
                entity.hurt(mutant.damageSources().mobAttack(mutant), SANDSTORM_CONTACT_DAMAGE);
            }
        }

        private void spawnSandstorms() {
            for (int index = 0; index < SANDSTORM_COUNT; index++) {
                float angle = (float) (index * Math.PI * 2.0D / SANDSTORM_COUNT);
                double x = arenaCenter.x + Math.cos(angle) * SANDSTORM_RADIUS;
                double z = arenaCenter.z + Math.sin(angle) * SANDSTORM_RADIUS;
                Sandstorm_Entity sandstorm = new Sandstorm_Entity(mutant.level(), x, arenaCenter.y, z,
                        SANDSTORM_ATTACK_DURATION, angle, mutant);
                sandstorms.add(sandstorm);
                mutant.level().addFreshEntity(sandstorm);
            }
        }

        private void spawnVoidVortexes() {
            for (int index = 0; index < SANDSTORM_VOID_VORTEX_COUNT; index++) {
                float angle = mutant.getRandom().nextFloat() * (float) (Math.PI * 2.0D);
                double radius = Math.sqrt(mutant.getRandom().nextDouble()) * SANDSTORM_VOID_VORTEX_RADIUS;
                double x = arenaCenter.x + Math.cos(angle) * radius;
                double z = arenaCenter.z + Math.sin(angle) * radius;
                double y = mutant.findGroundYAt(x, arenaCenter.y, z);
                Void_Vortex_Entity vortex = new Void_Vortex_Entity(mutant.level(), x, y, z, angle, mutant,
                        SANDSTORM_ATTACK_DURATION);
                voidVortexes.add(vortex);
                mutant.level().addFreshEntity(vortex);
            }
        }

        private void detachRestrictionRing() {
            arenaCenter = new Vec3(arenaCenter.x,
                    mutant.clearGroundPlacement(arenaCenter.x, arenaCenter.y, arenaCenter.z), arenaCenter.z);
            AABB searchArea = new AABB(arenaCenter, arenaCenter).inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
            for (RotatingSoulShieldEntity shield : mutant.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == mutant)) {
                shield.switchToAroundPosition(arenaCenter.x, arenaCenter.y, arenaCenter.z);
            }
            ringDetached = true;
        }

        private void restoreRestrictionRing() {
            if (!ringDetached) {
                return;
            }
            AABB searchArea = new AABB(arenaCenter, arenaCenter).inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
            for (RotatingSoulShieldEntity shield : mutant.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == mutant)) {
                shield.switchToAroundCaster();
            }
            ringDetached = false;
        }

        private void clearSpawnedEntities() {
            for (Sandstorm_Entity sandstorm : sandstorms) {
                if (!sandstorm.isRemoved()) {
                    sandstorm.discard();
                }
            }
            for (Void_Vortex_Entity vortex : voidVortexes) {
                if (!vortex.isRemoved()) {
                    vortex.discard();
                }
            }
            sandstorms.clear();
            voidVortexes.clear();
        }

        @Override
        public void stop() {
            if (mutant.phase == PHASE_3 && attackPhase == PHASE_DONE) {
                mutant.selectNextPhaseThreeAttack(ATTACK_PHASE_THREE_SANDSTORM);
            }
            restoreRestrictionRing();
            clearSpawnedEntities();
            mutant.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            mutant.setDeltaMovement(Vec3.ZERO);
            target = null;
        }
    }

    private class SuperWaterSpearAttack extends Goal {
        private static final int PHASE_ALIGN = 0;
        private static final int PHASE_DESCEND = 1;
        private static final int PHASE_PREPARE = 2;
        private static final int PHASE_PARALLEL_WARNING = 3;
        private static final int PHASE_NEXT_CENTER = 4;
        private static final int PHASE_DONE = 5;

        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        private Vec3 parallelTargetPosition = Vec3.ZERO;
        private double parallelAngle;
        private double groundY;
        private int attackPhase;
        private int phaseTimer;
        private int roundsCompleted;

        private SuperWaterSpearAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_4 && mutant.nextPhaseFourAttack == ATTACK_PHASE_FOUR_SUPER_WATER_SPEAR
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_4 && attackPhase != PHASE_DONE;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            groundY = mutant.getY();
            attackPhase = PHASE_ALIGN;
            phaseTimer = 0;
            roundsCompleted = 0;
        }

        @Override
        public void tick() {
            if (target == null || !target.isAlive()) {
                attackPhase = PHASE_DONE;
                return;
            }

            switch (attackPhase) {
                case PHASE_ALIGN -> alignToTargetY();
                case PHASE_DESCEND -> descendToGround();
                case PHASE_PREPARE -> prepareAttack();
                case PHASE_PARALLEL_WARNING -> warnAndReleaseParallelSpears();
                case PHASE_NEXT_CENTER -> waitForNextCenterWave();
                default -> {
                }
            }
        }

        private void alignToTargetY() {
            if (mutant.moveToAttackHeight(target.getY())) {
                groundY = mutant.findGroundY();
                mutant.setDeltaMovement(Vec3.ZERO);
                attackPhase = PHASE_DESCEND;
            }
        }

        private void descendToGround() {
            if (mutant.moveToAttackHeight(groundY)) {
                mutant.setDeltaMovement(Vec3.ZERO);
                phaseTimer = PHASE_FOUR_ATTACK_PREPARE_TIME;
                attackPhase = PHASE_PREPARE;
            }
        }

        private void prepareAttack() {
            holdPosition();
            if (--phaseTimer <= 0) {
                beginCenterWave();
            }
        }

        private void beginCenterWave() {
            Vec3 centerTargetPosition = target.position();
            spawnCenteredWaterSpears(centerTargetPosition);
            parallelTargetPosition = target.position();
            parallelAngle = mutant.getRandom().nextDouble() * Math.PI * 2.0D;
            phaseTimer = PHASE_FOUR_SUPER_WATER_SPEAR_INTERVAL;
            attackPhase = PHASE_PARALLEL_WARNING;
        }

        private void warnAndReleaseParallelSpears() {
            holdPosition();
            showParallelPredictionLines();
            if (--phaseTimer <= 0) {
                spawnParallelWaterSpears(parallelAngle, parallelTargetPosition);
                phaseTimer = PHASE_FOUR_SUPER_WATER_SPEAR_INTERVAL;
                attackPhase = PHASE_NEXT_CENTER;
            }
        }

        private void waitForNextCenterWave() {
            holdPosition();
            if (--phaseTimer <= 0) {
                roundsCompleted++;
                if (roundsCompleted >= PHASE_FOUR_SUPER_WATER_SPEAR_ROUNDS) {
                    attackPhase = PHASE_DONE;
                }
                else {
                    beginCenterWave();
                }
            }
        }

        private void holdPosition() {
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
        }

        private void spawnCenteredWaterSpears(Vec3 targetPosition) {
            double angleOffset = mutant.getRandom().nextDouble() * Math.PI * 2.0D;
            for (int index = 0; index < WATER_SPEAR_CENTER_SPEARS_PER_WAVE; index++) {
                double angle = angleOffset + index * Math.PI * 2.0D / WATER_SPEAR_CENTER_SPEARS_PER_WAVE;
                Vec3 direction = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
                Water_Spear_Entity spear = new Water_Spear_Entity(mutant, direction, mutant.level(),
                        WATER_SPEAR_CENTER_DAMAGE, WATER_SPEAR_CENTER_ACCELERATION);
                spear.setYRot((float) (Math.atan2(direction.z, direction.x) * 180.0D / Math.PI) + 90.0F);
                spear.setXRot(0.0F);
                spear.setPosRaw(targetPosition.x - direction.x * WATER_SPEAR_CENTER_RANGE, targetPosition.y,
                        targetPosition.z - direction.z * WATER_SPEAR_CENTER_RANGE);
                mutant.level().addFreshEntity(spear);
            }
        }

        private void spawnParallelWaterSpears(double angle, Vec3 targetPosition) {
            Vec3 direction = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
            Vec3 side = new Vec3(-direction.z, 0.0D, direction.x);
            Vec3 startingPoint = new Vec3(targetPosition.x - direction.x * WATER_SPEAR_PARALLEL_RANGE,
                    targetPosition.y, targetPosition.z - direction.z * WATER_SPEAR_PARALLEL_RANGE);
            int centerIndex = WATER_SPEAR_PARALLEL_SPEARS_PER_WAVE / 2;
            for (int index = 0; index < WATER_SPEAR_PARALLEL_SPEARS_PER_WAVE; index++) {
                Vec3 spawnPosition = startingPoint.add(side.scale((index - centerIndex) * WATER_SPEAR_PARALLEL_SPACING));
                Water_Spear_Entity spear = new Water_Spear_Entity(mutant, direction, mutant.level(),
                        WATER_SPEAR_PARALLEL_DAMAGE, WATER_SPEAR_PARALLEL_ACCELERATION);
                spear.setYRot((float) (Math.atan2(direction.z, direction.x) * 180.0D / Math.PI) + 90.0F);
                spear.setXRot(0.0F);
                spear.setPosRaw(spawnPosition.x, spawnPosition.y, spawnPosition.z);
                mutant.level().addFreshEntity(spear);
            }
        }

        private void showParallelPredictionLines() {
            if (phaseTimer % WATER_SPEAR_PARALLEL_PREDICTION_INTERVAL != 0
                    || !(mutant.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            Vec3 direction = new Vec3(Math.cos(parallelAngle), 0.0D, Math.sin(parallelAngle));
            Vec3 side = new Vec3(-direction.z, 0.0D, direction.x);
            Vec3 startingPoint = new Vec3(parallelTargetPosition.x - direction.x * WATER_SPEAR_PARALLEL_RANGE,
                    parallelTargetPosition.y + 0.25D, parallelTargetPosition.z - direction.z * WATER_SPEAR_PARALLEL_RANGE);
            int centerIndex = WATER_SPEAR_PARALLEL_SPEARS_PER_WAVE / 2;
            for (int spearIndex = 0; spearIndex < WATER_SPEAR_PARALLEL_SPEARS_PER_WAVE; spearIndex++) {
                Vec3 lineStart = startingPoint.add(side.scale((spearIndex - centerIndex) * WATER_SPEAR_PARALLEL_SPACING));
                for (int point = 0; point < WATER_SPEAR_PARALLEL_PREDICTION_POINTS; point++) {
                    double distance = point * WATER_SPEAR_PARALLEL_PREDICTION_LENGTH
                            / (double) (WATER_SPEAR_PARALLEL_PREDICTION_POINTS - 1);
                    Vec3 particlePosition = lineStart.add(direction.scale(distance));
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, particlePosition.x, particlePosition.y,
                            particlePosition.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }

        @Override
        public void stop() {
            if (attackPhase == PHASE_DONE) {
                mutant.nextPhaseFourAttack = ATTACK_PHASE_FOUR_LASER_SPREAD;
            }
            mutant.setDeltaMovement(Vec3.ZERO);
            target = null;
        }
    }

    private class FinalSparkAttack extends Goal {
        private static final int PHASE_ALIGN = 0;
        private static final int PHASE_DESCEND = 1;
        private static final int PHASE_WAIT_FIRST_LASER = 2;
        private static final int PHASE_ROTATE_FIRST_LASER = 3;
        private static final int PHASE_WAIT_SECOND_LASER = 4;
        private static final int PHASE_ROTATE_SECOND_LASER = 5;
        private static final int PHASE_DONE = 6;

        private final MutantEntity mutant;
        private final List<MutantSpinningPeppermintEntity> peppermints = new ArrayList<>();
        private final List<MutantLeviathanLaserEntity> lasers = new ArrayList<>();
        @Nullable
        private LivingEntity target;
        private double groundY;
        private int attackPhase;
        private int phaseTimer;
        private int peppermintTimer;

        private FinalSparkAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_4 && mutant.nextPhaseFourAttack == ATTACK_PHASE_FOUR_FINAL_SPARK
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_4 && attackPhase != PHASE_DONE;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            groundY = mutant.getY();
            attackPhase = PHASE_ALIGN;
            phaseTimer = 0;
            peppermintTimer = 0;
            peppermints.clear();
            lasers.clear();
        }

        @Override
        public void tick() {
            if (target == null || !target.isAlive()) {
                attackPhase = PHASE_DONE;
                return;
            }

            switch (attackPhase) {
                case PHASE_ALIGN -> alignToTargetY();
                case PHASE_DESCEND -> descendToGround();
                case PHASE_WAIT_FIRST_LASER -> waitForFirstLaser();
                case PHASE_ROTATE_FIRST_LASER -> rotateFirstLaser();
                case PHASE_WAIT_SECOND_LASER -> waitForSecondLaser();
                case PHASE_ROTATE_SECOND_LASER -> rotateSecondLaser();
                default -> {
                }
            }
        }

        private void alignToTargetY() {
            if (mutant.moveToAttackHeight(target.getY())) {
                groundY = mutant.findGroundY();
                mutant.setDeltaMovement(Vec3.ZERO);
                attackPhase = PHASE_DESCEND;
            }
        }

        private void descendToGround() {
            if (mutant.moveToAttackHeight(groundY)) {
                mutant.setDeltaMovement(Vec3.ZERO);
                phaseTimer = FINAL_SPARK_FIRST_LASER_WAIT_TIME;
                attackPhase = PHASE_WAIT_FIRST_LASER;
            }
        }

        private void waitForFirstLaser() {
            holdPosition();
            spawnPeppermintsOnInterval();
            if (--phaseTimer <= 0) {
                spawnLeviathanLaser(1, FINAL_SPARK_FIRST_LASER_TURN_TIME);
                phaseTimer = FINAL_SPARK_FIRST_LASER_TURN_TIME;
                attackPhase = PHASE_ROTATE_FIRST_LASER;
            }
        }

        private void rotateFirstLaser() {
            holdPosition();
            spawnPeppermintsOnInterval();
            rotateFacingTowardTarget(FINAL_SPARK_FIRST_LASER_TURN_DEGREES_PER_SECOND / 20.0F);
            if (--phaseTimer <= 0) {
                phaseTimer = FINAL_SPARK_SECOND_LASER_WAIT_TIME;
                attackPhase = PHASE_WAIT_SECOND_LASER;
            }
        }

        private void waitForSecondLaser() {
            holdPosition();
            spawnPeppermintsOnInterval();
            if (--phaseTimer <= 0) {
                spawnLeviathanLaser(-1, FINAL_SPARK_SECOND_LASER_TURN_TIME);
                phaseTimer = FINAL_SPARK_SECOND_LASER_TURN_TIME;
                attackPhase = PHASE_ROTATE_SECOND_LASER;
            }
        }

        private void rotateSecondLaser() {
            holdPosition();
            spawnPeppermintsOnInterval();
            rotateFacingTowardTarget(FINAL_SPARK_SECOND_LASER_TURN_DEGREES_PER_SECOND / 20.0F);
            if (--phaseTimer <= 0) {
                attackPhase = PHASE_DONE;
            }
        }

        private void holdPosition() {
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
        }

        private void spawnPeppermintsOnInterval() {
            if (--peppermintTimer > 0) {
                return;
            }

            int lifetime = FINAL_SPARK_FIRST_LASER_WAIT_TIME + FINAL_SPARK_FIRST_LASER_TURN_TIME
                    + FINAL_SPARK_SECOND_LASER_WAIT_TIME + FINAL_SPARK_SECOND_LASER_TURN_TIME
                    + FINAL_SPARK_LASER_LIFESPAN_BUFFER;
            for (int index = 0; index < FINAL_SPARK_PEPPERMINT_COUNT; index++) {
                MutantSpinningPeppermintEntity peppermint = new MutantSpinningPeppermintEntity(
                        ModEntities.MUTANT_SPINNING_PEPPERMINT.get(), mutant.level());
                int groupSize = FINAL_SPARK_PEPPERMINT_COUNT / 2;
                int groupIndex = index % groupSize;
                float direction = index < groupSize ? 1.0F : -1.0F;
                float groupOffset = direction > 0.0F ? 0.0F : 180.0F / groupSize;
                float startAngle = groupIndex * 360.0F / groupSize + groupOffset;
                peppermint.configure(0.0F, FINAL_SPARK_PEPPERMINT_SPIN_SPEED, startAngle, lifetime,
                        PEPPERMINT_DAMAGE, PEPPERMINT_HIT_RANGE, direction);
                peppermint.setOwner(mutant);
                peppermint.startSpiralFromCaster(RESTRICTION_BOUNDARY_RADIUS, FINAL_SPARK_PEPPERMINT_RADIUS_GROWTH);
                mutant.level().addFreshEntity(peppermint);
                peppermints.add(peppermint);
            }
            peppermintTimer = FINAL_SPARK_PEPPERMINT_INTERVAL;
        }

        private void spawnLeviathanLaser(int perpendicularDirection, int turnTime) {
            setFacing(yawTowardsTarget() + perpendicularDirection * 90.0F);
            MutantLeviathanLaserEntity laser = new MutantLeviathanLaserEntity(ModEntities.MUTANT_LEVIATHAN_LASER.get(),
                    mutant.level(), mutant);
            laser.setPos(mutant.getX(), mutant.getY() + mutant.getBbHeight() * 0.5D, mutant.getZ());
            laser.setLifespan(turnTime + MUTANT_LEVIATHAN_LASER_PORTAL_LIFESPAN_BUFFER);
            laser.setBeamDuration(turnTime);
            laser.setBeamDamage(FINAL_SPARK_LASER_DAMAGE, FINAL_SPARK_LASER_HP_DAMAGE);
            mutant.level().addFreshEntity(laser);
            lasers.add(laser);
        }

        private float yawTowardsTarget() {
            Vec3 direction = target.position().subtract(mutant.position());
            return (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F;
        }

        private void rotateFacingTowardTarget(float maximumTurnPerTick) {
            float yawDifference = Mth.wrapDegrees(yawTowardsTarget() - mutant.getYHeadRot());
            setFacing(mutant.getYHeadRot() + Mth.clamp(yawDifference, -maximumTurnPerTick, maximumTurnPerTick));
        }

        private void setFacing(float yaw) {
            float wrappedYaw = Mth.wrapDegrees(yaw);
            mutant.setYRot(wrappedYaw);
            mutant.setYHeadRot(wrappedYaw);
            mutant.setYBodyRot(wrappedYaw);
            mutant.setXRot(0.0F);
        }

        private void clearFinalSparkEntities() {
            for (MutantSpinningPeppermintEntity peppermint : peppermints) {
                if (!peppermint.isRemoved()) {
                    peppermint.discard();
                }
            }
            for (MutantLeviathanLaserEntity laser : lasers) {
                if (!laser.isRemoved()) {
                    laser.discard();
                }
            }
            AABB searchArea = mutant.getBoundingBox().inflate(RESTRICTION_BOUNDARY_RADIUS * 4.0D);
            for (MutantPortalAbyssBlastEntity beam : mutant.level().getEntitiesOfClass(MutantPortalAbyssBlastEntity.class,
                    searchArea, beam -> beam.belongsTo(mutant))) {
                beam.discard();
            }
            peppermints.clear();
            lasers.clear();
        }

        @Override
        public void stop() {
            clearFinalSparkEntities();
            if (attackPhase == PHASE_DONE) {
                mutant.finishPhaseFourAttack();
            }
            mutant.setDeltaMovement(Vec3.ZERO);
            target = null;
        }
    }

    private class PhantomArrowAttack extends Goal {
        private static final int PHASE_ALIGN = 0;
        private static final int PHASE_DESCEND = 1;
        private static final int PHASE_PREPARE = 2;
        private static final int PHASE_WARNING = 3;
        private static final int PHASE_FIRST_GROUP = 4;
        private static final int PHASE_SECOND_GROUP = 5;
        private static final int PHASE_DONE = 6;

        private final MutantEntity mutant;
        @Nullable
        private LivingEntity target;
        private double groundY;
        private int attackPhase;
        private int phaseTimer;
        private int wavesCompleted;

        private PhantomArrowAttack(MutantEntity mutant) {
            this.mutant = mutant;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return mutant.phase == PHASE_4 && mutant.nextPhaseFourAttack == ATTACK_PHASE_FOUR_PHANTOM_ARROW
                    && mutant.getTarget() != null && mutant.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return mutant.phase == PHASE_4 && attackPhase != PHASE_DONE;
        }

        @Override
        public void start() {
            target = mutant.getTarget();
            groundY = mutant.getY();
            phaseTimer = 0;
            wavesCompleted = 0;
            attackPhase = PHASE_ALIGN;
        }

        @Override
        public void tick() {
            if (target == null || !target.isAlive()) {
                attackPhase = PHASE_DONE;
                return;
            }

            switch (attackPhase) {
                case PHASE_ALIGN -> alignToTargetY();
                case PHASE_DESCEND -> descendToGround();
                case PHASE_PREPARE -> prepareAttack();
                case PHASE_WARNING -> showWarning();
                case PHASE_FIRST_GROUP -> fireFirstGroup();
                case PHASE_SECOND_GROUP -> fireSecondGroup();
                default -> {
                }
            }
        }

        private void alignToTargetY() {
            if (mutant.moveToAttackHeight(target.getY())) {
                groundY = mutant.findGroundY();
                mutant.setDeltaMovement(Vec3.ZERO);
                attackPhase = PHASE_DESCEND;
            }
        }

        private void descendToGround() {
            if (mutant.moveToAttackHeight(groundY)) {
                mutant.setDeltaMovement(Vec3.ZERO);
                phaseTimer = PHASE_FOUR_ATTACK_PREPARE_TIME;
                attackPhase = PHASE_PREPARE;
            }
        }

        private void prepareAttack() {
            holdPosition();
            if (--phaseTimer <= 0) {
                phaseTimer = PHASE_FOUR_PHANTOM_ARROW_WARNING_TIME;
                attackPhase = PHASE_WARNING;
            }
        }

        private void showWarning() {
            holdPosition();
            if (--phaseTimer <= 0) {
                spawnWarningParticles();
                phaseTimer = PHASE_FOUR_PHANTOM_ARROW_FIRST_GROUP_DELAY;
                attackPhase = PHASE_FIRST_GROUP;
            }
        }

        private void fireFirstGroup() {
            holdPosition();
            if (--phaseTimer <= 0) {
                spawnArrowGroup(3);
                phaseTimer = PHASE_FOUR_PHANTOM_ARROW_GROUP_INTERVAL;
                attackPhase = PHASE_SECOND_GROUP;
            }
        }

        private void fireSecondGroup() {
            holdPosition();
            if (--phaseTimer <= 0) {
                spawnArrowGroup(-3);
                wavesCompleted++;
                if (wavesCompleted >= PHASE_FOUR_PHANTOM_ARROW_WAVES) {
                    attackPhase = PHASE_DONE;
                }
                else {
                    phaseTimer = PHASE_FOUR_PHANTOM_ARROW_NEXT_WAVE_DELAY;
                    attackPhase = PHASE_WARNING;
                }
            }
        }

        private void holdPosition() {
            mutant.setDeltaMovement(Vec3.ZERO);
            mutant.getMoveControl().setWantedPosition(mutant.getX(), mutant.getY(), mutant.getZ(), 1.0D);
            mutant.getLookControl().setLookAt(target, NORMAL_ATTACK_LOOK_LIMIT, NORMAL_ATTACK_LOOK_LIMIT);
        }

        private void spawnWarningParticles() {
            if (!(mutant.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            float offset = waveOffset(15);
            for (int arrowIndex = 0; arrowIndex < PHASE_FOUR_PHANTOM_ARROW_ARROWS_PER_GROUP; arrowIndex++) {
                double angle = (arrowIndex + offset) / PHASE_FOUR_PHANTOM_ARROW_ARROWS_PER_GROUP * Math.PI * 2.0D;
                for (int point = 1; point <= PHASE_FOUR_PHANTOM_ARROW_WARNING_POINTS; point++) {
                    double distance = point * PHASE_FOUR_PHANTOM_ARROW_WARNING_SPACING;
                    serverLevel.sendParticles(ModParticle.PHANTOM_WING_FLAME.get(),
                            mutant.getX() + Math.cos(angle) * distance,
                            mutant.getY() + PHASE_FOUR_PHANTOM_ARROW_SPAWN_HEIGHT,
                            mutant.getZ() + Math.sin(angle) * distance,
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }

        private void spawnArrowGroup(int timeOffset) {
            float offset = waveOffset(timeOffset);
            for (int arrowIndex = 0; arrowIndex < PHASE_FOUR_PHANTOM_ARROW_ARROWS_PER_GROUP; arrowIndex++) {
                double angle = (arrowIndex + offset) / PHASE_FOUR_PHANTOM_ARROW_ARROWS_PER_GROUP * Math.PI * 2.0D;
                Vec3 direction = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
                double x = mutant.getX() + direction.x * PHASE_FOUR_PHANTOM_ARROW_SPAWN_RADIUS;
                double y = mutant.getY() + PHASE_FOUR_PHANTOM_ARROW_SPAWN_HEIGHT;
                double z = mutant.getZ() + direction.z * PHASE_FOUR_PHANTOM_ARROW_SPAWN_RADIUS;
                Phantom_Arrow_Entity arrow = new Phantom_Arrow_Entity(mutant.level(), mutant);
                arrow.setPos(x, y, z);
                arrow.setOwner(mutant);
                arrow.setBaseDamage(PHASE_FOUR_PHANTOM_ARROW_DAMAGE);
                arrow.shoot(direction.x, direction.y, direction.z, PHASE_FOUR_PHANTOM_ARROW_SPEED, 0.0F);
                mutant.level().addFreshEntity(arrow);
            }
        }

        private float waveOffset(int timeOffset) {
            return (float) ((mutant.level().getGameTime() + timeOffset) % 4L) / 4.0F;
        }

        @Override
        public void stop() {
            if (attackPhase == PHASE_DONE) {
                mutant.nextPhaseFourAttack = ATTACK_PHASE_FOUR_SUPER_WATER_SPEAR;
            }
            mutant.setDeltaMovement(Vec3.ZERO);
            target = null;
        }
    }

}
