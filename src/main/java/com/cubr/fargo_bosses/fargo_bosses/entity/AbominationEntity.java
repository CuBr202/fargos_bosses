package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.cubr.fargo_bosses.fargo_bosses.Config;
import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.client.sound.AbominationMusic;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModEntities;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModSounds;
import com.mojang.logging.LogUtils;
import lykrast.meetyourfight.entity.BossEntity;
import lykrast.meetyourfight.entity.ProjectileLineEntity;
import lykrast.meetyourfight.entity.ProjectileTargetedEntity;
import lykrast.meetyourfight.entity.RoseSpiritEntity;
import lykrast.meetyourfight.entity.SwampMineEntity;
import lykrast.meetyourfight.entity.ai.MoveFrontOfTarget;
import lykrast.meetyourfight.entity.ai.StationaryAttack;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.registry.MYFSounds;
import lykrast.meetyourfight.registry.MYFEntities;
import net.miauczel.legendary_monsters.Particle.ModParticles;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.Projectile.LightningBoltEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class AbominationEntity extends BossEntity implements PowerableMob {

    public static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityDataAccessor<Integer> BOSS_DIFFICULTY = SynchedEntityData.defineId(AbominationEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> POWERED = SynchedEntityData.defineId(AbominationEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PHASE_TWO_MUSIC = SynchedEntityData.defineId(AbominationEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int PHASE_1 = 0, PHASE_1_TRANSITION = 1, PHASE_2 = 2, PHASE_2_TRANSITION = 3, PHASE_3 = 4, DEATH_PHASE = 5;
    private static final int TRANSITION_TIME = 2 * 20;

    public static final int ATK_DICE = 0, ATK_CHIPS_CIRCLE = 1, ATK_DASH = 2, ATK_DAGGER = 3, ATK_LINEAR = 4, ATK_LASER = 5, ATK_SUMMON = 6, ATK_BOMB = 7;

    // Settings
    private static final int DICE_ATTACK_DELAY = 15; // 30, 15, 10
    private static final int DICE_ATTACK_AFTER_COOLDOWN = 20; // 40, 20, 0

    private static final int DICE_AMOUNT_PHASE_1 = 8; // 6, 8, 8
    private static final int DICE_AMOUNT_PHASE_2 = 12; // 8, 12, 12

    private static final int CHIPS_ATTACK_DELAY = 20; // 40, 20, 10

    private static final int CHIPS_CIRCLE_DELAY = 10; // 20, 10, 10
    private static final int CHIPS_ATTACK_AFTER_COOLDOWN = 20; // 40, 20, 0

    private static final int DAGGER_WAVES = 4; // 3, 4, 5
    private static final int DAGGERS_PER_WAVE = 12; // 8, 12, 12
    private static final int DAGGER_ALIGN_TIME = 10; // 10, 10, 10
    private static final int DAGGER_WAVE_INTERVAL = 20; // 30, 20, 16
    private static final int DAGGER_REST_TIME = 160; // 200, 160, 160
    private static final int DAGGER_DRIFT_INTERVAL = 15; // 15, 15, 15
    private static final float DAGGER_DAMAGE = 6.0F; // 6.0, 6.0, 6.0
    private static final float DAGGER_SPEED = 0.7F; // 0.7, 0.7, 0.6
    private static final int DAGGER_RETURN_TICK = 80; // 90, 80, 70

    private static final int DASH_ATTACK_AFTER_COOLDOWN = 20; // 40, 20, 0
    private static final int DASH_ATTACK_COUNT = 5; // 5, 5, 5
    private static final int DASH_WINDUP = 5; // 10, 5, 3
    private static final int DASH_TIME = 10; // 10, 10, 10
    private static final int DASH_RECOVER = 5; // 10, 5, 3
    private static final double DASH_SPEED = 2; // 1.5, 2, 2.5

    private static final int LINEAR_ATTACK_AMOUNT = 10; // 5, 10, 10
    private static final int LINEAR_ATTACK_WINDUP = 30; // 40, 30, 20
    private static final int LINEAR_ATTACK_COUNT = 20; // 15, 20, 25
    private static final int LINEAR_ATTACK_INTERVAL = 30; // 40, 30, 30
    private static final int LINEAR_ATTACK_INTERVAL_FINAL = 10; // 20, 10, 8
    private static final int LINEAR_ATTACK_DISTANCE = 7; // 7, 7, 7

    public static final double SUMMONED_ROSE_SPIRIT_HEALTH = 40.0D; // 20, 40, 60
    public static final double SUMMONED_ROSE_SPIRIT_ARMOR = 5.0D; // 0, 5, 5
    public static final String SUMMONED_ROSE_SPIRIT_DAMAGE_TAG = "fargo_summoned_rose_damage";
    public static final String MYF_PROJECTILE_DAMAGE_TAG = "fargo_myf_projectile_damage";
    private static final int SUMMON_ATTACK_PHASE_1_COUNT = 3; // 2, 3, 3
    private static final int SUMMON_ATTACK_PHASE_2_COUNT = 6; // 4, 6, 6
    private static final int SUMMON_ATTACK_REST_TIME = 15 * 20; // 300, 300, 300
    private static final double SUMMON_ATTACK_RADIUS = 3.0D; // 3, 3, 3
    private static final int BOMB_ATTACK_PHASE_1_ROUNDS = 1; // 1, 1, 1
    private static final int BOMB_ATTACK_PHASE_2_ROUNDS = 3; // 2, 3, 3
    private static final int BOMB_ATTACK_MINE_COUNT = 12; // 12, 12, 12
    private static final int BOMB_ATTACK_ROUND_INTERVAL = 80; // 100, 80, 70
    private static final int BOMB_ATTACK_DETONATION_GRACE_TIME = 20; // 20, 20, 20
    private static final double BOMB_ATTACK_SAFETY_RADIUS = 3.0D; // 4.0, 3.0, 2.0
    private static final double BOMB_ATTACK_SAFETY_MIN_DISTANCE = 12.0D; // 10.0, 12.0, 13.0
    private static final int BOMB_ATTACK_SAFETY_PARTICLES = 16; // 16, 16, 16
    private static final int BOMB_ATTACK_SAFETY_PARTICLE_INTERVAL = 5; // 5, 5, 5
    private static final double BOMB_ATTACK_BOSS_ABOVE_OFFSET = 2.0D; // 2, 2, 2
    private static final double BOMB_ATTACK_BOSS_MOVE_SPEED = 2.0D; // 2, 2, 2.5

    private static final int LASER_ALIGN_TIME = 20; // 40, 20, 20
    private static final double LASER_MOVE_SPEED = 3.0D; // 3.0, 3.0, 3.0
    private static final double LASER_START_REACH_DISTANCE = 0.5D; // 0.5, 0.5, 0.5
    private static final int LASER_MIN_REPEATS = 2; // 2, 2, 3
    private static final int LASER_MAX_REPEATS = 2; // 2, 2, 3
    private static final float LASER_MAX_HEALTH_DAMAGE = 0.0F; // 0, 0, 0
    private static final double LASER_RING_CLEARANCE = 8.0D; // 8.0, 8.0, 7.0
    private static final float LASER_SIDE_OFFSET = 2.0F;  // 2.0, 2.0, 2.0
    private static final int LASER_ATTACK_AFTER_COOLDOWN = 20; // 40, 20, 0
    private static final int LASER_ATTACK_THUNDER_ROUNDS = 7; // 7, 7, 9
    private static final int LASER_ATTACK_THUNDER_WARMUP = 0; // 0, 0, 0
    private static final int LASER_ATTACK_THUNDER_MAX_LIFETIME = Integer.MAX_VALUE;
    private static final float LASER_ATTACK_THUNDER_DAMAGE = 5.0F; // 5.0, 5.0, 5.0
    private static final double LASER_ATTACK_THUNDER_JITTER = 4.0D; // 2.0, 4.0, 4.0


    private static final float RESTRICTION_BOUNDARY_ORBIT_SPEED = Mth.TWO_PI / 160.0F;

    private int phase, phaseTimer, attackCooldown, nextAttack;
    private boolean restrictionBoundaryCreated;
    private double bombSafetyX, bombSafetyZ;
    private int bombSafetyTicks;
    private boolean bombArenaDetachedForLaser;
    private double bombArenaCenterX, bombArenaCenterY, bombArenaCenterZ;
    private boolean laserAttackActive;
    private boolean waitingForFinalHit;
    private int finalHitLandingState;
    private double finalHitGroundY;

    public AbominationEntity(EntityType<? extends AbominationEntity> type, Level level) {
        super(type, level);
        BossBarUtil.useNotchedTwelve(this);
        moveControl = new VexMovementController(this);
        xpReward = 10000;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BOSS_DIFFICULTY, 0);
        entityData.define(POWERED, false);
        entityData.define(PHASE_TWO_MUSIC, false);
    }

    public void setBossDifficulty(int difficulty) {
        entityData.set(BOSS_DIFFICULTY, Mth.clamp(difficulty, 0, 2));
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
    }

    public boolean isUsingPhaseTwoMusic() {
        return entityData.get(PHASE_TWO_MUSIC);
    }

    private void applyDifficultyHealth(boolean refillHealth) {
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(configInt(Config.ABOMINATION_HP));
            if (refillHealth) heal(getMaxHealth());
        }
    }

    private int configInt(net.minecraftforge.common.ForgeConfigSpec.ConfigValue<List<? extends Integer>> value) {
        return Config.abominationInt(value, getBossDifficulty());
    }

    private double configDouble(net.minecraftforge.common.ForgeConfigSpec.ConfigValue<List<? extends Double>> value) {
        return Config.abominationDouble(value, getBossDifficulty());
    }

    private float configFloat(net.minecraftforge.common.ForgeConfigSpec.ConfigValue<List<? extends Double>> value) {
        return (float) configDouble(value);
    }

    public float getBossDamage() {
        return configFloat(Config.ABOMINATION_BOSS_DAMAGE);
    }

    public float getBorderDamage() {
        return configFloat(Config.ABOMINATION_BORDER_DAMAGE);
    }

    public float getLaserDamage() {
        return configFloat(Config.ABOMINATION_LASER_DAMAGE);
    }

    public boolean isSoulFractureEnabled() {
        return Config.abominationBoolean(Config.ABOMINATION_ENABLE_SOUL_FRACTURE, getBossDifficulty());
    }

    private int restrictionBoundaryShieldCount() {
        return configInt(Config.ABOMINATION_RESTRICTION_SHIELDS);
    }

    private float restrictionBoundaryRadius() {
        return configFloat(Config.ABOMINATION_RESTRICTION_RADIUS);
    }

    private double bombAttackMineRadius() {
        return restrictionBoundaryRadius() - 1.0D;
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
    public void aiStep() {
        super.aiStep();
        if (tickCount % 20 == 0) {
            for (RoseSpiritEntity spirit : level().getEntitiesOfClass(RoseSpiritEntity.class, getBoundingBox().inflate(32.0D))) {
                spirit.setOwner(this);
            }
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new FinalHitWaitGoal());
        goalSelector.addGoal(1, new DiceAttack(this));
        goalSelector.addGoal(2, new ChipsAttack(this));
        goalSelector.addGoal(3, new PhantomDaggerAttack(this));
        goalSelector.addGoal(4, new DashAttack(this));
        goalSelector.addGoal(5, new LinearAttack(this));
        goalSelector.addGoal(6, new LaserAttack(this));
        goalSelector.addGoal(6, new SummonAttack(this));
        goalSelector.addGoal(6, new BombAttack(this));
        goalSelector.addGoal(7, new MoveFrontOfTarget(this, 1));
        goalSelector.addGoal(8, new VexMoveRandomGoal(this, 0.25));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200)
                .add(Attributes.FOLLOW_RANGE, 64)
                .add(Attributes.ATTACK_DAMAGE, 10);
    }

    public static void spawn(Player player, Level world, int difficulty) {
        RandomSource rand = player.getRandom();
        AbominationEntity abomination = ModEntities.ABOMINATION.get().create(world);
        if (abomination == null) {
            return;
        }

        abomination.moveTo(player.getX() + rand.nextInt(15) - 7, player.getY() + rand.nextInt(9) - 1, player.getZ() + rand.nextInt(15) - 7, rand.nextFloat() * 360 - 180, 0);
        if (!player.getAbilities().instabuild) {
            abomination.setTarget(player);
        }
        abomination.setBossDifficulty(difficulty);



        ForgeEventFactory.onFinalizeSpawn(abomination, (ServerLevel) world, world.getCurrentDifficultyAt(abomination.blockPosition()), MobSpawnType.EVENT, null, null);


        world.addFreshEntity(abomination);

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 2, 1);
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        SpawnGroupData result = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        phase = PHASE_1;
        phaseTimer = 0;
        restrictionBoundaryCreated = false;
        waitingForFinalHit = false;
        finalHitLandingState = 0;
        setInvulnerable(false);
        setPowered(false);
        entityData.set(PHASE_TWO_MUSIC, false);
        setBossDifficulty(getBossDifficulty());
        heal(getMaxHealth());
        return result;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {


        if (source.is(DamageTypes.GENERIC_KILL)) {
            phase = DEATH_PHASE;
            laserAttackActive = false;
            setPowered(false);
            setInvulnerable(false);
            return super.hurt(source, amount);
        }
        if (isDamageImmune()) {
            return false;
        }
        if (isInvulnerable()) {
            setInvulnerable(false);
        }
        if (phase == PHASE_2 && getHealth() - amount <= 1.0F) {
            setHealth(1.0F);
            beginPhase2Transition();
            return false;
        }
        return super.hurt(source, amount);
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
        return isUsingPhaseTwoMusic() ? ModSounds.ABOM_PHASE_2.get() : ModSounds.ABOM_PHASE_1.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readSpawnData(FriendlyByteBuf additionalData) {
        AbominationMusic.start(this);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return FargoBosses.rl("entities/bellringer");
    }

    public boolean isAttackPhase() {
        return !waitingForFinalHit && (phase == PHASE_1 || phase == PHASE_2 || phase == PHASE_3);
    }


    private void setCooldownNextAttack(int cooldown) {
        attackCooldown = cooldown;
    }

    private void rollNextAttack(int ignore) {
        if (phase == PHASE_3) {
            if (ignore == ATK_DICE) {
                nextAttack = ATK_LASER;
            }
            else if (ignore == ATK_LASER) {
                nextAttack = ATK_LINEAR;
            }
            else nextAttack = ATK_DICE;
        }
        else if (ignore == ATK_DICE && phase == PHASE_1) {
            nextAttack = ATK_CHIPS_CIRCLE;
        }
        else if (ignore == ATK_DICE && phase == PHASE_2) {
            nextAttack = ATK_DAGGER;
        }
        else if (ignore == ATK_DASH) {
            rollSummonStyleAttack();
        }
        else if (ignore == ATK_SUMMON || ignore == ATK_BOMB) {
            if (phase == PHASE_2) nextAttack = random.nextBoolean() ? ATK_LINEAR : ATK_LASER;
            else nextAttack = ATK_DICE;
        }
        else if ((ignore == ATK_LINEAR || ignore == ATK_LASER) && phase == PHASE_2) {
            nextAttack = ATK_DICE;
        }
        else if (ignore == ATK_CHIPS_CIRCLE || ignore == ATK_DAGGER || ignore == ATK_DICE) {
            nextAttack = ATK_DASH;
        }
        else {
            nextAttack = ATK_DICE;
        }
    }

    private void rollSummonStyleAttack() {
        switch (random.nextInt(2)) {
            case 0 -> nextAttack = ATK_SUMMON;
            default -> nextAttack = ATK_BOMB;
        }
    }

    private ProjectileTargetedEntity readyTargeted() {
        ProjectileTargetedEntity proj = new ProjectileTargetedEntity(level(), this);
        proj.setOwner(this);
        proj.setPos(getX(), getEyeY() + 1, getZ());
        setMyfProjectileDamage(proj, getBossDamage());
        return proj;
    }

    private static void setMyfProjectileDamage(Entity projectile, float damage) {
        projectile.getPersistentData().putFloat(MYF_PROJECTILE_DAMAGE_TAG, damage);
    }

    private ProjectileLineEntity readyLineProjectile() {
        ProjectileLineEntity ghost = new ProjectileLineEntity(level(), this);
        ghost.setOwner(this);
        ghost.setPos(getX(), getY() + 0.75, getZ());
        ghost.setVariant(ProjectileLineEntity.VAR_BELLRINGER);
        return ghost;
    }

    @Override
    public void customServerAiStep() {
        if (attackCooldown > 0) attackCooldown--;
        if (bombSafetyTicks > 0) bombSafetyTicks--;
        updatePoweredState();
        if (waitingForFinalHit) {
            super.customServerAiStep();
            return;
        }
        if (phase == PHASE_1 && tickCount > 10 && getHealth() <= getMaxHealth() * configFloat(Config.ABOMINATION_PHASE_1_THRESHOLD)) {
            beginPhase1Transition();
        }
        else if (phase == PHASE_2 && getHealth() <= 1.0F) {
            beginPhase2Transition();
        }

        if (phase == PHASE_1_TRANSITION || phase == PHASE_2_TRANSITION) {
            emitTransitionSoulEruption();
            phaseTimer--;
            if (phaseTimer <= 0) {
                finishTimedPhase();
            }
        }
        super.customServerAiStep();
    }

    private void updatePoweredState() {
        boolean phaseInvulnerable = phase == PHASE_1_TRANSITION || phase == PHASE_2_TRANSITION
                || (phase == PHASE_3 && !waitingForFinalHit);
        setPowered(isDamageImmune());
        setInvulnerable(phaseInvulnerable);
    }

    private boolean isDamageImmune() {
        if (waitingForFinalHit) {
            return false;
        }
        if (phase == PHASE_1_TRANSITION || phase == PHASE_2_TRANSITION
                || (phase == PHASE_3 && !waitingForFinalHit) || laserAttackActive) {
            return true;
        }

        return getBossDifficulty() == 2 && !level().getEntitiesOfClass(RoseSpiritEntity.class,
                getBoundingBox().inflate(32.0D), spirit -> spirit.isAlive() && spirit.getOwner() == this
                        && spirit.getPersistentData().contains(SUMMONED_ROSE_SPIRIT_DAMAGE_TAG)).isEmpty();
    }

    private void beginPhase1Transition() {
        phase = PHASE_1_TRANSITION;
        phaseTimer = TRANSITION_TIME;
        attackCooldown = TRANSITION_TIME;
        setInvulnerable(true);
        setPowered(true);
        emitTransitionSoulBurst();
        createRestrictionBoundary();
    }

    private void createRestrictionBoundary() {
        if (level().isClientSide || restrictionBoundaryCreated) {
            return;
        }

        restrictionBoundaryCreated = true;
        int shieldCount = restrictionBoundaryShieldCount();
        float radius = restrictionBoundaryRadius();
        for (int i = 0; i < shieldCount; i++) {
            float angle = i / (float) shieldCount * Mth.TWO_PI;
            RotatingSoulShieldEntity shield = new RotatingSoulShieldEntity(level(), this, 0.0D, radius, angle, RESTRICTION_BOUNDARY_ORBIT_SPEED, getBorderDamage(), true);
            shield.setManualLifetime(-1);
            shield.setOrbitAroundCaster(radius, angle, RESTRICTION_BOUNDARY_ORBIT_SPEED, 0.0D);
            shield.setPos(getX() + Math.cos(angle) * radius, getY(), getZ() + Math.sin(angle) * radius);
            shield.faceRadiusFrom(getX(), getY(), getZ());
            level().addFreshEntity(shield);
        }
    }

    private void beginPhase2Transition() {
        phase = PHASE_2_TRANSITION;
        phaseTimer = TRANSITION_TIME;
        attackCooldown = TRANSITION_TIME;
        setHealth(1.0F);
        setInvulnerable(true);
        setPowered(true);
        emitTransitionSoulBurst();
    }

    private void emitTransitionSoulBurst() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        serverLevel.sendParticles(ModParticles.GROUNDSOUL_RED.get(), getX(), getY() + 0.1D, getZ(), 12,
                1.1D, 0.15D, 1.1D, 0.05D);
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 0.5D, getZ(), 24,
                0.8D, 0.3D, 0.8D, 0.08D);
    }

    private void emitTransitionSoulEruption() {
        if (phaseTimer % 2 != 0 || !(level() instanceof ServerLevel serverLevel)) return;

        serverLevel.sendParticles(ModParticles.GHOSTLY_SOUL_RED.get(), getX(), getY() + 0.2D, getZ(), 8,
                0.7D, 0.15D, 0.7D, 0.12D);
        if (phaseTimer % 10 == 0) {
            serverLevel.sendParticles(ModParticles.GROUNDSOUL_RED.get(), getX(), getY() + 0.1D, getZ(), 3,
                    0.65D, 0.05D, 0.65D, 0.02D);
        }
    }

    private void finishTimedPhase() {
        if (phase == PHASE_1_TRANSITION) {
            phase = PHASE_2;
            entityData.set(PHASE_TWO_MUSIC, true);
            phaseTimer = 0;
            attackCooldown = 20;
            setHealth(getMaxHealth());
            setInvulnerable(false);
            setPowered(false);
            rollNextAttack(-1);
        }
        else if (phase == PHASE_2_TRANSITION) {
            phase = PHASE_3;
            phaseTimer = 0;
            attackCooldown = 0;
            setHealth(1.0F);
            setInvulnerable(true);
            setPowered(true);
            nextAttack = ATK_DICE;
        }
    }

    private void finishPhase3() {
        if (phase != PHASE_3 || waitingForFinalHit) {
            return;
        }

        waitingForFinalHit = true;
        finalHitLandingState = 0;
        nextAttack = -1;
        attackCooldown = 0;
        laserAttackActive = false;
        setDeltaMovement(Vec3.ZERO);
        setPowered(false);
        setInvulnerable(false);
    }

    private void updateFinalHitLanding() {
        LivingEntity target = getTarget();
        if (finalHitLandingState == 0 && target != null && target.isAlive()) {
            getMoveControl().setWantedPosition(getX(), target.getY(), getZ(), 1.0D);
            if (Math.abs(getY() - target.getY()) <= getBbHeight()) {
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
            getMoveControl().setWantedPosition(getX(), finalHitGroundY, getZ(), 1.0D);
            if (Math.abs(getY() - finalHitGroundY) <= getBbHeight()) {
                finalHitLandingState = 2;
            }
            return;
        }

        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
    }

    private double findGroundY() {
        BlockPos position = BlockPos.containing(getX(), getY(), getZ());
        while (position.getY() > level().getMinBuildHeight()) {
            if (level().getBlockState(position.below()).blocksMotion()) {
                return position.getY();
            }
            position = position.below();
        }
        return getY();
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

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        phase = compound.getInt("Phase");
        entityData.set(PHASE_TWO_MUSIC, phase >= PHASE_2);
        phaseTimer = compound.getInt("PhaseTimer");
        attackCooldown = compound.getInt("AttackCooldown");
        nextAttack = compound.getInt("NxtAt");
        entityData.set(BOSS_DIFFICULTY, Mth.clamp(compound.getInt("BossDifficulty"), 0, 2));
        applyDifficultyHealth(false);
        restrictionBoundaryCreated = compound.getBoolean("RestrictionBoundaryCreated");
        bombSafetyX = compound.getDouble("BombSafetyX");
        bombSafetyZ = compound.getDouble("BombSafetyZ");
        bombSafetyTicks = compound.getInt("BombSafetyTicks");
        bombArenaDetachedForLaser = compound.getBoolean("BombArenaDetachedForLaser");
        bombArenaCenterX = compound.getDouble("BombArenaCenterX");
        bombArenaCenterY = compound.getDouble("BombArenaCenterY");
        bombArenaCenterZ = compound.getDouble("BombArenaCenterZ");
        waitingForFinalHit = compound.getBoolean("WaitingForFinalHit");
        finalHitLandingState = compound.getInt("FinalHitLandingState");
        finalHitGroundY = compound.getDouble("FinalHitGroundY");
        setInvulnerable(phase == PHASE_1_TRANSITION || phase == PHASE_2_TRANSITION
                || (phase == PHASE_3 && !waitingForFinalHit));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Phase", phase);
        compound.putInt("PhaseTimer", phaseTimer);
        compound.putInt("AttackCooldown", attackCooldown);
        compound.putInt("NxtAt", nextAttack);
        compound.putInt("BossDifficulty", getBossDifficulty());
        compound.putBoolean("RestrictionBoundaryCreated", restrictionBoundaryCreated);
        compound.putDouble("BombSafetyX", bombSafetyX);
        compound.putDouble("BombSafetyZ", bombSafetyZ);
        compound.putInt("BombSafetyTicks", bombSafetyTicks);
        compound.putBoolean("BombArenaDetachedForLaser", bombArenaDetachedForLaser);
        compound.putDouble("BombArenaCenterX", bombArenaCenterX);
        compound.putDouble("BombArenaCenterY", bombArenaCenterY);
        compound.putDouble("BombArenaCenterZ", bombArenaCenterZ);
        compound.putBoolean("WaitingForFinalHit", waitingForFinalHit);
        compound.putInt("FinalHitLandingState", finalHitLandingState);
        compound.putDouble("FinalHitGroundY", finalHitGroundY);
    }

    public boolean isInsideBombSafetyCircle(LivingEntity entity) {
        double radius = configDouble(Config.ABOMINATION_BOMB_SAFETY_RADIUS);
        return bombSafetyTicks > 0 && entity.distanceToSqr(bombSafetyX, entity.getY(), bombSafetyZ) <= radius * radius;
    }

    //Regular attacks below
    private static class DiceAttack extends StationaryAttack {
        private AbominationEntity abom;
        private LivingEntity target;
        private int attackRemaining, attackDelay;
        private float headTargetYaw;

        public DiceAttack(AbominationEntity abom) {
            super(abom);
            this.abom = abom;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return abom.nextAttack == ATK_DICE && abom.isAttackPhase() && abom.attackCooldown <= 0 && abom.getTarget() != null && abom.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return abom.nextAttack == ATK_DICE && (attackDelay > 0 || attackRemaining > 0) && target.isAlive() && abom.isAttackPhase();
        }

        @Override
        public void start() {
            super.start();
            target = abom.getTarget();
            headTargetYaw = abom.yHeadRot;
            //abom.setAnimation(ANIM_DICE_WINDUP);
            attackDelay = getPhaseDelay(abom.configInt(Config.ABOMINATION_DICE_DELAY));
            attackRemaining = getAttackCount();
            abom.playSound(MYFSounds.dameFortunaAttack.get(), abom.getSoundVolume(), abom.getVoicePitch());
        }

        private int getAttackCount() {
            if (abom.phase == PHASE_1) return 6;
                //Phase 1
            else if(abom.phase == PHASE_2) return 7;
            return 14;
        }

        private int getSplitDirectionCount() {
            if (abom.phase == PHASE_1) return abom.configInt(Config.ABOMINATION_DICE_PHASE_1_AMOUNT);
            //Phase 1
            else if(abom.phase == PHASE_2) return abom.configInt(Config.ABOMINATION_DICE_PHASE_2_AMOUNT);
            return abom.configInt(Config.ABOMINATION_DICE_PHASE_2_AMOUNT);
        }

        private int getPhaseDelay(int normalDelay) {
            return abom.phase == PHASE_3 ? Math.max(1, normalDelay* 2/3) : normalDelay;
        }

        @Override
        public void tick() {
            super.tick();
            abom.yHeadRot = Mth.rotLerp(0.25F, abom.yHeadRot, headTargetYaw);
            attackDelay--;
            if (attackDelay <= 0 && attackRemaining > 0) {
                attackRemaining--;
                //abom.setAnimation(ANIM_DICE_LAUNCH);
                performAttack();
            }
            //if (attackDelay == (abom.phase == PHASE_3 ? 8 : 10) && attackRemaining > 0) abom.setAnimation(ANIM_DICE_WINDUP);
        }

        private void performAttack() {
            double tx = target.getX();
            double ty = target.getY();
            double tz = target.getZ();
            //Dice bombs
            attackDelay = attackRemaining == 0 ? getPhaseDelay(40) : getPhaseDelay(20);

            //abom.shuffleAttackWait = Math.max(abom.shuffleAttackWait, 30);

            //don't want the bombs to be thrown behind the target, so we aim a 60° cone
            //don't think the yrot can get negative values so wrap it is
            Vec3 offset = new Vec3(abom.getX() - tx,0, abom.getZ() - tz).normalize().yRot(Mth.wrapDegrees(abom.random.nextFloat()*60 - 30)*Mth.DEG_TO_RAD);
            double bombX = tx + offset.x*3;
            double bombY = ty + 0.5;
            double bombZ = tz + offset.z*3;
            if (target.onGround()) bombY += 0.75;
            FreeFortunaBombEntity bomb = new FreeFortunaBombEntity(abom.level(), abom.getX(), abom.getY() + 2, abom.getZ(), abom);
            bomb.setExplosionDirectionsUniformly(getSplitDirectionCount());
            bomb.setProjectileDamage(abom.getBossDamage());

            int dettime = abom.phase == PHASE_1 ? 0 : abom.random.nextInt(11);
            bomb.setup(25 + dettime, 15 + dettime, bombX, bombY, bombZ);
            abom.level().addFreshEntity(bomb);
            headTargetYaw = Mth.wrapDegrees(headTargetYaw + (abom.random.nextBoolean() ? 90.0F : -90.0F));
            abom.playSound(MYFSounds.dameFortunaShoot.get(), 2.0F, (abom.random.nextFloat() - abom.random.nextFloat()) * 0.2F + 1.0F);
        }

        @Override
        public void stop() {
            abom.yHeadRot = abom.yBodyRot;
            if (abom.phase == PHASE_3) {
                if (target == null || !target.isAlive()) {
                    abom.finishPhase3();
                }
                else {
                    abom.rollNextAttack(ATK_DICE);
                }
                return;
            }
            abom.setCooldownNextAttack(abom.configInt(Config.ABOMINATION_DICE_COOLDOWN));
            abom.rollNextAttack(ATK_DICE);
            //if we going to shuffle phase, let that handle the next animation
            //if (abom.isAttackPhase()) abom.setAnimation(ANIM_IDLE);
        }

    }

    private static class ChipsAttack extends StationaryAttack {
        private final AbominationEntity abom;
        private LivingEntity target;
        private int attackRemaining, attackDelay, circleDelay, circleDirection, clapTime;

        public ChipsAttack(AbominationEntity abom) {
            super(abom);
            this.abom = abom;
        }

        @Override
        public boolean canUse() {
            return abom.nextAttack == ATK_CHIPS_CIRCLE && abom.phase == PHASE_1 && abom.attackCooldown <= 0 && abom.getTarget() != null && abom.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return abom.nextAttack == ATK_CHIPS_CIRCLE && (attackDelay > 0 || attackRemaining > 0) && target != null && target.isAlive() && abom.phase == PHASE_1;
        }

        @Override
        public void start() {
            super.start();
            target = abom.getTarget();
            //abom.setAnimation(ANIM_CHIPS_WINDUP);
            attackDelay = abom.configInt(Config.ABOMINATION_CHIPS_DELAY);
            attackRemaining = 3;
            circleDelay = abom.configInt(Config.ABOMINATION_CHIPS_CIRCLE_DELAY);
            circleDirection = abom.random.nextBoolean() ? 1 : -1;
            clapTime = 12;
            abom.playSound(MYFSounds.dameFortunaAttack.get(), abom.getSoundVolume(), abom.getVoicePitch());
        }

        @Override
        public void tick() {
            super.tick();
            attackDelay--;
            if (attackRemaining > 0) {
                if (attackDelay <= 0) {
                    attackRemaining--;
                    //abom.setAnimation(ANIM_CHIPS_LAUNCH);
                    performAttack();
                }
                else if (attackDelay == 15) {
                    //abom.setAnimation(ANIM_CHIPS_WINDUP);
                }
            }
            else {
                if (attackDelay == clapTime) {
                    //abom.setAnimation(ANIM_CLAP);
                }
                else if (attackDelay == clapTime - 2) {
                    abom.playSound(MYFSounds.dameFortunaClap.get(), 2.0F, (abom.random.nextFloat() - abom.random.nextFloat()) * 0.1F + 1.0F);
                }
            }
        }

        private void performAttack() {
            attackDelay = attackRemaining > 0 ? abom.configInt(Config.ABOMINATION_CHIPS_DELAY) : abom.configInt(Config.ABOMINATION_CHIPS_DELAY) + 10;
            fireChipsCircle(8, 1, circleDelay + 5 * attackRemaining);
            if (attackRemaining > 0) {
                rotateAroundTarget();
            }
        }

        private void rotateAroundTarget() {
            float angle = (abom.random.nextInt(4) + 4) * 10.0F * Mth.DEG_TO_RAD * circleDirection;
            Vec3 offset = new Vec3(abom.getX() - target.getX(), 0, abom.getZ() - target.getZ()).normalize().yRot(angle);
            double distance = abom.random.nextDouble() * 2 + 4;

            abom.getMoveControl().setWantedPosition(
                    target.getX() + offset.x * distance,
                    target.getY() + 1 + abom.random.nextDouble() * 2,
                    target.getZ() + offset.z * distance,
                    3);
        }

        private void fireChipsCircle(int chips, int circles, int delay) {
            Vec3 perp = abom.getLookAngle().cross(new Vec3(0, 1, 0)).normalize();
            double sy = abom.getY() + 1;
            float angle = Mth.TWO_PI / chips;

            double abomX = abom.getX();
            double abomZ = abom.getZ();
            double sx = abomX;
            double sz = abomZ;

            for (int c = 0; c < circles; c++) {
                Vec3 offset = perp;
                for (int i = 0; i < chips; i++) {
                    ProjectileTargetedEntity proj = abom.readyTargeted();
                    proj.setPos(sx, sy + (c * chips + i) * 0.125, sz);
                    proj.setUp(delay + delay * c, delay, target, 1.5, abomX + 2 * offset.x, sy + 1 + c, abomZ + 2 * offset.z);
                    abom.level().addFreshEntity(proj);
                    offset = offset.yRot(angle);
                }
            }

            abom.playSound(MYFSounds.dameFortunaChipsStart.get(), 2.0F, (abom.random.nextFloat() - abom.random.nextFloat()) * 0.2F + 1.0F);
        }

        @Override
        public void stop() {
            abom.setCooldownNextAttack(abom.configInt(Config.ABOMINATION_CHIPS_COOLDOWN));
            abom.rollNextAttack(ATK_CHIPS_CIRCLE);
            //if (abom.isAttackPhase()) abom.setAnimation(ANIM_IDLE);
        }
    }

    private static class PhantomDaggerAttack extends StationaryAttack {
        private final AbominationEntity abom;
        private LivingEntity target;
        private int wavesRemaining, attackDelay, restTime, alignTime, driftTime, driftDirection;
        private boolean aligning;

        public PhantomDaggerAttack(AbominationEntity abom) {
            super(abom);
            this.abom = abom;
        }

        @Override
        public boolean canUse() {
            return abom.nextAttack == ATK_DAGGER && abom.phase == PHASE_2 && abom.attackCooldown <= 0 && abom.getTarget() != null && abom.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return abom.nextAttack == ATK_DAGGER && target != null && target.isAlive() && abom.phase == PHASE_2 && (wavesRemaining > 0 || attackDelay > 0 || restTime > 0);
        }

        @Override
        public void start() {
            super.start();
            target = abom.getTarget();
            wavesRemaining = abom.configInt(Config.ABOMINATION_DAGGER_WAVES);
            attackDelay = 0;
            restTime = 0;
            alignTime = abom.configInt(Config.ABOMINATION_DAGGER_ALIGN_TIME);
            driftTime = 0;
            driftDirection = abom.random.nextBoolean() ? 1 : -1;
            aligning = true;
            stationaryY = target.getY();
            //abom.setAnimation(ANIM_DAGGER_WINDUP);
            abom.playSound(MYFSounds.dameFortunaAttack.get(), abom.getSoundVolume(), abom.getVoicePitch());
        }

        @Override
        public void tick() {
            super.tick();
            if (aligning) {
                alignTime--;
                stationaryY = target.getY();
                abom.getMoveControl().setWantedPosition(abom.getX(), stationaryY, abom.getZ(), 1.5);
                if (Math.abs(abom.getY() - stationaryY) <= 0.25 || alignTime <= 0) {
                    aligning = false;
                    abom.setDeltaMovement(Vec3.ZERO);
                }
                return;
            }

            driftAroundTarget();

            if (restTime > 0) {
                restTime--;
                return;
            }

            if (attackDelay > 0) {
                attackDelay--;
                return;
            }

            if (wavesRemaining > 0) {
                wavesRemaining--;
                //abom.setAnimation(ANIM_DAGGER_THROW);
                throwDaggerWave();
                if (wavesRemaining > 0) {
                    attackDelay = abom.configInt(Config.ABOMINATION_DAGGER_WAVE_INTERVAL) - 1;
                }
                else {
                    restTime = abom.configInt(Config.ABOMINATION_DAGGER_REST_TIME);
                    //abom.setAnimation(ANIM_DAGGER_REST);
                }
            }
        }

        private void throwDaggerWave() {
            double sx = abom.getX();
            double sy = abom.getY() + abom.getBbHeight() * 0.5;
            double sz = abom.getZ();
            double randomOffset = abom.random.nextDouble();

            int daggerCount = abom.configInt(Config.ABOMINATION_DAGGERS_PER_WAVE);
            for (int i = 0; i < daggerCount; i++) {
                double angle = (i / (double) daggerCount + randomOffset) * Mth.TWO_PI;
                double dx = Math.cos(angle);
                double dz = Math.sin(angle);

                AbominationPhantomDaggerEntity dagger = new AbominationPhantomDaggerEntity(net.miauczel.legendary_monsters.entity.ModEntities.THROWN_PHANTOM_DAGGER.get(), abom.level());
                dagger.setOwner(abom);
                dagger.setReturnEntity(abom);
                dagger.setReturnTick(abom.configInt(Config.ABOMINATION_DAGGER_RETURN_TICK));
                dagger.setDamage(abom.configFloat(Config.ABOMINATION_DAGGER_DAMAGE));
                dagger.setRed(true);
                dagger.setPos(sx, sy, sz);
                dagger.setYRot((float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
                dagger.shoot(dx, 0, dz, abom.configFloat(Config.ABOMINATION_DAGGER_SPEED), 0.0F);
                abom.level().addFreshEntity(dagger);
            }

            abom.playSound(MYFSounds.dameFortunaShoot.get(), 2.0F, (abom.random.nextFloat() - abom.random.nextFloat()) * 0.2F + 1.0F);
        }

        private void driftAroundTarget() {
            driftTime--;
            stationaryY = target.getY();
            if (driftTime > 0 && abom.getMoveControl().hasWanted()) {
                return;
            }

            driftTime = abom.configInt(Config.ABOMINATION_DAGGER_DRIFT_INTERVAL);
            float angle = (abom.random.nextInt(4) + 2) * 10.0F * Mth.DEG_TO_RAD * driftDirection;
            Vec3 offset = new Vec3(abom.getX() - target.getX(), 0, abom.getZ() - target.getZ());
            if (offset.lengthSqr() < 1.0E-4) {
                offset = new Vec3(1, 0, 0);
            }

            offset = offset.normalize().yRot(angle);
            double distance = abom.random.nextDouble() * 2 + 3;
            abom.getMoveControl().setWantedPosition(
                    target.getX() + offset.x * distance,
                    stationaryY,
                    target.getZ() + offset.z * distance,
                    1.0);
        }

        @Override
        public void stop() {
            abom.setCooldownNextAttack(0);
            abom.rollNextAttack(ATK_DAGGER);
            //if (abom.isAttackPhase()) abom.setAnimation(ANIM_IDLE);
        }
    }

    private static class SummonAttack extends StationaryAttack {
        private final AbominationEntity abom;
        private LivingEntity target;
        private int restTime;

        public SummonAttack(AbominationEntity abom) {
            super(abom);
            this.abom = abom;
        }

        @Override
        public boolean canUse() {
            return abom.nextAttack == ATK_SUMMON && (abom.phase == PHASE_1 || abom.phase == PHASE_2)
                    && abom.attackCooldown <= 0 && abom.getTarget() != null && abom.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return abom.nextAttack == ATK_SUMMON && (abom.phase == PHASE_1 || abom.phase == PHASE_2)
                    && restTime > 0 && target != null && target.isAlive();
        }

        @Override
        public void start() {
            super.start();
            target = abom.getTarget();
            restTime = abom.configInt(Config.ABOMINATION_SUMMON_REST_TIME);
            summonRoseSpirits();
            abom.playSound(MYFSounds.dameFortunaAttack.get(), abom.getSoundVolume(), abom.getVoicePitch());
        }

        @Override
        public void tick() {
            super.tick();
            restTime--;
        }

        private void summonRoseSpirits() {
            int count = abom.phase == PHASE_1 ? abom.configInt(Config.ABOMINATION_SUMMON_PHASE_1_COUNT) : abom.configInt(Config.ABOMINATION_SUMMON_PHASE_2_COUNT);
            for (int i = 0; i < count; i++) {
                double angle = i * Mth.TWO_PI / count;
                RoseSpiritEntity spirit = MYFEntities.ROSE_SPIRIT.get().create(abom.level());
                if (spirit == null) {
                    continue;
                }

                spirit.setPos(
                        abom.getX() + Math.cos(angle) * abom.configDouble(Config.ABOMINATION_SUMMON_RADIUS),
                        abom.getY(),
                        abom.getZ() + Math.sin(angle) * abom.configDouble(Config.ABOMINATION_SUMMON_RADIUS)
                );
                spirit.getAttribute(Attributes.MAX_HEALTH).setBaseValue(abom.configDouble(Config.ABOMINATION_ROSE_HEALTH));
                spirit.getAttribute(Attributes.ARMOR).setBaseValue(abom.configDouble(Config.ABOMINATION_ROSE_ARMOR));
                spirit.setHealth(abom.configFloat(Config.ABOMINATION_ROSE_HEALTH));
                spirit.getPersistentData().putFloat(SUMMONED_ROSE_SPIRIT_DAMAGE_TAG, abom.getBossDamage());
                spirit.setOwner(abom);
                spirit.setTarget(target);
                abom.level().addFreshEntity(spirit);
            }
        }

        @Override
        public void stop() {
            abom.setCooldownNextAttack(0);
            abom.rollNextAttack(ATK_SUMMON);
        }
    }

    private static class BombAttack extends StationaryAttack {
        private final AbominationEntity abom;
        private LivingEntity target;
        private int roundsRemaining;
        private int roundTimer;
        private Vec3 previousSafetyCircle;
        private Vec3 arenaCenter;
        private boolean ringDetached;
        private boolean awaitingDetonation;

        public BombAttack(AbominationEntity abom) {
            super(abom);
            this.abom = abom;
        }

        @Override
        public boolean canUse() {
            return abom.nextAttack == ATK_BOMB && (abom.phase == PHASE_1 || abom.phase == PHASE_2)
                    && abom.attackCooldown <= 0 && abom.getTarget() != null && abom.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return abom.nextAttack == ATK_BOMB && (abom.phase == PHASE_1 || abom.phase == PHASE_2)
                    && roundsRemaining > 0 && target != null && target.isAlive();
        }

        @Override
        public void start() {
            super.start();
            target = abom.getTarget();
            roundsRemaining = abom.phase == PHASE_1 ? abom.configInt(Config.ABOMINATION_BOMB_PHASE_1_ROUNDS) : abom.configInt(Config.ABOMINATION_BOMB_PHASE_2_ROUNDS);
            previousSafetyCircle = null;
            awaitingDetonation = false;
            arenaCenter = abom.position();
            detachRestrictionRing();
            beginRound();
            abom.playSound(MYFSounds.dameFortunaAttack.get(), abom.getSoundVolume(), abom.getVoicePitch());
        }

        @Override
        public void tick() {
            super.tick();
            moveBelowSafetyCircle();
            if (roundTimer % abom.configInt(Config.ABOMINATION_BOMB_SAFETY_PARTICLE_INTERVAL) == 0) drawSafetyCircle();
            if (--roundTimer > 0) return;

            if (!awaitingDetonation) {
                summonMines();
                abom.bombSafetyTicks = abom.configInt(Config.ABOMINATION_BOMB_DETONATION_GRACE);
                awaitingDetonation = true;
                roundTimer = abom.configInt(Config.ABOMINATION_BOMB_DETONATION_GRACE);
            }
            else if (--roundsRemaining > 0) {
                awaitingDetonation = false;
                beginRound();
            }
        }

        private void beginRound() {
            Vec3 safetyCircle = chooseSafetyCircle();
            abom.bombSafetyX = safetyCircle.x;
            abom.bombSafetyZ = safetyCircle.z;
            abom.bombSafetyTicks = abom.configInt(Config.ABOMINATION_BOMB_INTERVAL);
            previousSafetyCircle = safetyCircle;
            roundTimer = abom.configInt(Config.ABOMINATION_BOMB_INTERVAL);
            drawSafetyCircle();
        }

        private void drawSafetyCircle() {
            if (!(abom.level() instanceof ServerLevel serverLevel)) return;

            int particleCount = abom.configInt(Config.ABOMINATION_BOMB_SAFETY_PARTICLES);
            double safetyRadius = abom.configDouble(Config.ABOMINATION_BOMB_SAFETY_RADIUS);
            for (int i = 0; i < particleCount; i++) {
                double angle = i * Mth.TWO_PI / particleCount;
                double x = abom.bombSafetyX + Math.cos(angle) * safetyRadius;
                double z = abom.bombSafetyZ + Math.sin(angle) * safetyRadius;
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, x, target.getY() + 0.1D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }

        private void moveBelowSafetyCircle() {
            abom.getMoveControl().setWantedPosition(abom.bombSafetyX, target.getY() + abom.configDouble(Config.ABOMINATION_BOMB_BOSS_OFFSET),
                    abom.bombSafetyZ, abom.configDouble(Config.ABOMINATION_BOMB_BOSS_SPEED));
        }

        private Vec3 chooseSafetyCircle() {
            double maxDistance = abom.restrictionBoundaryRadius() - abom.configDouble(Config.ABOMINATION_BOMB_SAFETY_RADIUS);
            Vec3 center = new Vec3(arenaCenter.x, 0.0D, arenaCenter.z);

            for (int attempt = 0; attempt < 32; attempt++) {
                double angle = abom.random.nextDouble() * Mth.TWO_PI;
                double distance = Math.sqrt(abom.random.nextDouble()) * maxDistance;
                Vec3 candidate = center.add(Math.cos(angle) * distance, 0.0D, Math.sin(angle) * distance);
                if (previousSafetyCircle == null
                        || candidate.distanceToSqr(previousSafetyCircle) >= abom.configDouble(Config.ABOMINATION_BOMB_SAFETY_DISTANCE) * abom.configDouble(Config.ABOMINATION_BOMB_SAFETY_DISTANCE)) {
                    return candidate;
                }
            }

            if (previousSafetyCircle == null) return center;
            Vec3 offset = previousSafetyCircle.subtract(center);
            if (offset.lengthSqr() < 1.0E-6D) offset = new Vec3(1.0D, 0.0D, 0.0D);
            offset = offset.normalize().scale(-maxDistance);
            return center.add(offset);
        }

        private void summonMines() {
            for (int i = 0; i < abom.configInt(Config.ABOMINATION_BOMB_MINE_COUNT); i++) {
                Vec3 minePosition = randomMinePosition(previousSafetyCircle);
                SwampMineEntity mine = new SwampMineEntity(abom.level(), minePosition.x,
                        target.getY(), minePosition.z, abom);
                mine.setFuse(0);
                abom.level().addFreshEntity(mine);
            }
        }

        private Vec3 randomMinePosition(Vec3 safetyCircle) {
            Vec3 center = new Vec3(arenaCenter.x, 0.0D, arenaCenter.z);
            for (int attempt = 0; attempt < 16; attempt++) {
                double angle = abom.random.nextDouble() * Mth.TWO_PI;
                double distance = Math.sqrt(abom.random.nextDouble()) * abom.bombAttackMineRadius();
                Vec3 candidate = center.add(Math.cos(angle) * distance, 0.0D, Math.sin(angle) * distance);
                double safetyRadius = abom.configDouble(Config.ABOMINATION_BOMB_SAFETY_RADIUS);
                if (candidate.distanceToSqr(safetyCircle) > safetyRadius * safetyRadius) {
                    return candidate;
                }
            }
            return center;
        }

        private void detachRestrictionRing() {
            AABB searchArea = new AABB(arenaCenter.x, arenaCenter.y, arenaCenter.z, arenaCenter.x, arenaCenter.y, arenaCenter.z)
                    .inflate(abom.restrictionBoundaryRadius() + 8.0D);
            for (RotatingSoulShieldEntity shield : abom.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == abom)) {
                shield.switchToAroundPosition(arenaCenter.x, arenaCenter.y, arenaCenter.z);
            }
            ringDetached = true;
        }

        private void restoreRestrictionRing() {
            if (!ringDetached) return;

            AABB searchArea = new AABB(arenaCenter.x, arenaCenter.y, arenaCenter.z, arenaCenter.x, arenaCenter.y, arenaCenter.z)
                    .inflate(abom.restrictionBoundaryRadius() + 8.0D);
            for (RotatingSoulShieldEntity shield : abom.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == abom)) {
                shield.switchToAroundCaster();
            }
            ringDetached = false;
        }

        @Override
        public void stop() {
            abom.bombSafetyTicks = 0;
            abom.setCooldownNextAttack(0);
            abom.rollNextAttack(ATK_BOMB);
            if (abom.nextAttack == ATK_LASER && abom.phase == PHASE_2 && target != null && target.isAlive()) {
                abom.bombArenaDetachedForLaser = true;
                abom.bombArenaCenterX = arenaCenter.x;
                abom.bombArenaCenterY = arenaCenter.y;
                abom.bombArenaCenterZ = arenaCenter.z;
            }
            else {
                abom.bombArenaDetachedForLaser = false;
                restoreRestrictionRing();
            }
        }
    }

    private static class LinearAttack extends Goal {
        private final AbominationEntity abom;
        private LivingEntity target;
        private Direction direction;
        private int attacksRemaining;
        private int attackDelay;

        public LinearAttack(AbominationEntity abom) {
            this.abom = abom;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return abom.nextAttack == ATK_LINEAR && (abom.phase == PHASE_2 || abom.phase == PHASE_3) && abom.attackCooldown <= 0
                    && abom.getTarget() != null && abom.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return abom.nextAttack == ATK_LINEAR && (abom.phase == PHASE_2 || abom.phase == PHASE_3) && attacksRemaining > 0
                    && target != null && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            target = abom.getTarget();
            attacksRemaining = abom.configInt(Config.ABOMINATION_LINEAR_COUNT);
            attackDelay = abom.configInt(Config.ABOMINATION_LINEAR_WINDUP);
            abom.attackCooldown = 2;

            BlockPos self = abom.blockPosition();
            BlockPos targetPos = target.blockPosition();
            direction = Direction.getNearest(targetPos.getX() - self.getX(), 0, targetPos.getZ() - self.getZ());
            abom.playSound(SoundEvents.BELL_RESONATE, 2.0F, 1.0F);
        }

        @Override
        public void tick() {
            abom.attackCooldown = 2;
            attackDelay--;
            if (attackDelay > 0) {
                return;
            }

            int linearCount = abom.configInt(Config.ABOMINATION_LINEAR_COUNT);
            int linearInterval = abom.configInt(Config.ABOMINATION_LINEAR_INTERVAL);
            int linearFinalInterval = abom.configInt(Config.ABOMINATION_LINEAR_FINAL_INTERVAL);
            attackDelay = (linearInterval - linearFinalInterval) * attacksRemaining / linearCount + linearFinalInterval;
            attacksRemaining--;
            fireProjectileLine();

            if (attacksRemaining <= 0) {
                stop();
            }
        }

        private void fireProjectileLine() {
            BlockPos targetPos = target.blockPosition();
            double targetX = targetPos.getX() + 0.5D;
            double targetY = targetPos.getY() + 0.1D;
            double targetZ = targetPos.getZ() + 0.5D;
            if (!target.onGround() && !target.isInWater() && !abom.level().getBlockState(targetPos.below()).blocksMotion()) {
                targetY -= 1.0D;
            }

            double directionX = direction.getStepX();
            double directionZ = direction.getStepZ();
            int offset = attacksRemaining % 2 == 0 ? 1 : -1;
            int linearAmount = abom.configInt(Config.ABOMINATION_LINEAR_AMOUNT);
            int linearDistance = abom.configInt(Config.ABOMINATION_LINEAR_DISTANCE);
            for (int i = -linearAmount; i <= linearAmount; i++) {
                ProjectileLineEntity projectile = abom.readyLineProjectile();
                setMyfProjectileDamage(projectile, abom.getBossDamage());
                projectile.setUp(15 + offset * i, directionX, 0, directionZ,
                        targetX - linearDistance * directionX + i * directionZ,
                        targetY,
                        targetZ - linearDistance * directionZ + i * directionX);
                abom.level().addFreshEntity(projectile);
            }

            abom.playSound(SoundEvents.BELL_BLOCK, 2.0F,
                    (abom.random.nextFloat() - abom.random.nextFloat()) * 0.2F + 1.0F);
        }

        @Override
        public void stop() {
            if (abom.phase == PHASE_3) {
                abom.finishPhase3();
                return;
            }
            abom.setCooldownNextAttack(0);
            abom.rollNextAttack(ATK_LINEAR);
        }
    }

    private static class LaserAttack extends Goal {
        private static final int PHASE_ALIGN = 0;
        private static final int PHASE_MOVE_TO_START = 1;
        private static final int PHASE_CHARGE = 2;
        private static final int PHASE_SWEEP = 3;
        private static final int PHASE_DONE = 4;

        private final AbominationEntity abom;
        private LivingEntity target;
        private int attackPhase;
        private int phaseTime;
        private int sweepsRemaining;
        private double ringCenterX;
        private double ringCenterY;
        private double ringCenterZ;
        private Vec3 sweepStart;
        private Vec3 sweepEnd;
        private float lockedSweepYaw;
        private boolean ringDetached;
        private final List<LightningBoltEntity> thunderBolts = new ArrayList<>();

        public LaserAttack(AbominationEntity abom) {
            this.abom = abom;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return abom.nextAttack == ATK_LASER && (abom.phase == PHASE_2 || abom.phase == PHASE_3)
                    && abom.attackCooldown <= 0 && abom.getTarget() != null && abom.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return abom.nextAttack == ATK_LASER && (abom.phase == PHASE_2 || abom.phase == PHASE_3)
                    && attackPhase != PHASE_DONE && target != null && target.isAlive();
        }

        @Override
        public void start() {
            target = abom.getTarget();
            abom.laserAttackActive = true;
            attackPhase = PHASE_ALIGN;
            phaseTime = abom.configInt(Config.ABOMINATION_LASER_ALIGN_TIME);
            sweepsRemaining = abom.configInt(Config.ABOMINATION_LASER_REPEATS);
            if (abom.bombArenaDetachedForLaser) {
                ringCenterX = abom.bombArenaCenterX;
                ringCenterY = findGroundYAt(ringCenterX, abom.bombArenaCenterY, abom.bombArenaCenterZ);
                ringCenterZ = abom.bombArenaCenterZ;
                ringDetached = true;
                abom.bombArenaDetachedForLaser = false;
            }
            else {
                ringCenterX = abom.getX();
                ringCenterY = findGroundY();
                ringCenterZ = abom.getZ();
                ringDetached = false;
            }
            thunderBolts.clear();
            abom.setDeltaMovement(Vec3.ZERO);
            abom.playSound(MYFSounds.dameFortunaAttack.get(), abom.getSoundVolume(), abom.getVoicePitch());
        }

        @Override
        public void tick() {
            if (attackPhase == PHASE_ALIGN) {
                alignToGround();
            }
            else if (attackPhase == PHASE_MOVE_TO_START) {
                moveToSweepStart();
            }
            else if (attackPhase == PHASE_CHARGE) {
                holdAtSweepStart();
                if (--phaseTime <= 0) {
                    attackPhase = PHASE_SWEEP;
                    phaseTime = abom.configInt(Config.ABOMINATION_LASER_SWEEP_TIME);
                    abom.playSound(SoundEvents.BEACON_POWER_SELECT, 2.0F, 1.0F);
                }
            }
            else if (attackPhase == PHASE_SWEEP) {
                sweepAcrossRing();
            }
        }

        private void alignToGround() {
            abom.getMoveControl().setWantedPosition(ringCenterX, ringCenterY, ringCenterZ, 1.5D);
            if (--phaseTime <= 0 || Math.abs(abom.getY() - ringCenterY) <= 0.25D) {
                if (!ringDetached) {
                    ringCenterX = abom.getX();
                    ringCenterZ = abom.getZ();
                    detachRestrictionRing();
                }
                prepareSweep();
            }
        }

        private void prepareSweep() {
            Vec3 outward = new Vec3(target.getX() - ringCenterX, 0, target.getZ() - ringCenterZ);
            if (outward.lengthSqr() < 1.0E-4) {
                outward = new Vec3(abom.getX() - ringCenterX, 0, abom.getZ() - ringCenterZ);
            }
            if (outward.lengthSqr() < 1.0E-4) {
                outward = new Vec3(0, 0, 1);
            }

            outward = outward.normalize();
            double distanceStart = abom.restrictionBoundaryRadius() + abom.configDouble(Config.ABOMINATION_LASER_RING_CLEARANCE);
            double distanceEnd = abom.restrictionBoundaryRadius() - abom.configDouble(Config.ABOMINATION_LASER_RING_CLEARANCE);
            sweepStart = new Vec3(ringCenterX + outward.x * distanceStart, ringCenterY, ringCenterZ + outward.z * distanceStart);
            sweepEnd = new Vec3(ringCenterX - outward.x * distanceEnd, ringCenterY, ringCenterZ - outward.z * distanceEnd);
            attackPhase = PHASE_MOVE_TO_START;
        }

        private void moveToSweepStart() {
            abom.getMoveControl().setWantedPosition(sweepStart.x, sweepStart.y, sweepStart.z, abom.configDouble(Config.ABOMINATION_LASER_MOVE_SPEED));
            double reachDistance = Math.max(abom.configDouble(Config.ABOMINATION_LASER_START_REACH_DISTANCE), abom.getBoundingBox().getSize());
            if (abom.distanceToSqr(sweepStart.x, sweepStart.y, sweepStart.z) <= reachDistance * reachDistance) {
                faceRingCenter();
                lockedSweepYaw = abom.yHeadRot;
                spawnSideLasers();
                attackPhase = PHASE_CHARGE;
                phaseTime = abom.configInt(Config.ABOMINATION_LASER_CHARGE_TIME);
                abom.setDeltaMovement(Vec3.ZERO);
                abom.playSound(SoundEvents.BEACON_ACTIVATE, 2.0F, 1.0F);
            }
        }

        private void spawnSideLasers() {
            int beamDuration = abom.configInt(Config.ABOMINATION_LASER_CHARGE_TIME)
                    + abom.configInt(Config.ABOMINATION_LASER_SWEEP_TIME)
                    + abom.configInt(Config.ABOMINATION_LASER_BEAM_END_BUFFER);
            float maxHealthDamage = abom.configFloat(Config.ABOMINATION_LASER_MAX_HEALTH_DAMAGE);
            float sideOffset = abom.configFloat(Config.ABOMINATION_LASER_SIDE_OFFSET);
            FlexibleEnergyBeamEntity left = new FlexibleEnergyBeamEntity(ModEntities.FLEXIBLE_ENERGY_BEAM.get(), abom.level(), abom,
                    beamDuration, abom.getLaserDamage(), maxHealthDamage, 90.0F, 0.0F);
            left.setRelativePositionOffset(0.0F, -sideOffset, 0.0F);

            FlexibleEnergyBeamEntity right = new FlexibleEnergyBeamEntity(ModEntities.FLEXIBLE_ENERGY_BEAM.get(), abom.level(), abom,
                    beamDuration, abom.getLaserDamage(), maxHealthDamage, -90.0F, 0.0F);
            right.setRelativePositionOffset(0.0F, sideOffset, 0.0F);

            abom.level().addFreshEntity(left);
            abom.level().addFreshEntity(right);
        }

        private void holdAtSweepStart() {
            faceRingCenter();
            abom.getMoveControl().setWantedPosition(sweepStart.x, sweepStart.y, sweepStart.z, 1.0D);
            abom.setDeltaMovement(Vec3.ZERO);
        }

        private void sweepAcrossRing() {
            phaseTime--;
            int sweepTime = abom.configInt(Config.ABOMINATION_LASER_SWEEP_TIME);
            double progress = 1.0D - phaseTime / (double) sweepTime;
            Vec3 destination = sweepStart.scale(1.0D - progress).add(sweepEnd.scale(progress));
            setFacing(lockedSweepYaw);
            abom.setPos(destination.x, destination.y, destination.z);
            abom.setDeltaMovement(Vec3.ZERO);

            int thunderInterval = Math.max(1, sweepTime / abom.configInt(Config.ABOMINATION_LASER_THUNDER_ROUNDS));
            if (phaseTime > 0 && phaseTime % thunderInterval == 0) {
                spawnThunderRow();
            }

            if (phaseTime <= 0) {
                sweepsRemaining--;
                if (sweepsRemaining > 0) {
                    prepareSweep();
                }
                else {
                    finishAttack();
                }
            }
        }

        private void faceRingCenter() {
            Vec3 toCenter = new Vec3(ringCenterX - abom.getX(), 0, ringCenterZ - abom.getZ());
            if (toCenter.lengthSqr() > 1.0E-4) {
                float yaw = (float) (Math.atan2(toCenter.z, toCenter.x) * Mth.RAD_TO_DEG) - 90.0F;
                setFacing(yaw);
            }
        }

        private void spawnThunderRow() {
            Vec3 sweepDirection = sweepEnd.subtract(sweepStart).normalize();
            Vec3 lateral = new Vec3(-sweepDirection.z, 0, sweepDirection.x);
            int thunderRounds = abom.configInt(Config.ABOMINATION_LASER_THUNDER_ROUNDS);
            double spacing = abom.restrictionBoundaryRadius() * 2.0D / (thunderRounds - 1);
            double centerOffset = (thunderRounds - 1) * 0.5D;

            for (int i = 0; i < thunderRounds; i++) {
                double distance = (i - centerOffset) * spacing;
                double x = abom.getX() + lateral.x * distance + randomJitter();
                double z = abom.getZ() + lateral.z * distance + randomJitter();
                AbominationLightningBoltEntity bolt = new AbominationLightningBoltEntity(
                        abom.level(), x, ringCenterY, z, 0.0F,
                        abom.configInt(Config.ABOMINATION_LASER_THUNDER_WARMUP), abom,
                        LASER_ATTACK_THUNDER_MAX_LIFETIME,
                        abom.configFloat(Config.ABOMINATION_LASER_THUNDER_DAMAGE)
                );
                thunderBolts.add(bolt);
                abom.level().addFreshEntity(bolt);
            }
        }

        private double randomJitter() {
            return (abom.random.nextDouble() * 2.0D - 1.0D) * abom.configDouble(Config.ABOMINATION_LASER_THUNDER_JITTER);
        }

        private void setFacing(float yaw) {
            abom.setYRot(yaw);
            abom.yHeadRot = yaw;
            abom.yBodyRot = yaw;
            abom.setXRot(0.0F);
        }

        private double findGroundY() {
            return findGroundYAt(abom.getX(), abom.getY(), abom.getZ());
        }

        private double findGroundYAt(double x, double y, double z) {
            BlockPos position = BlockPos.containing(x, y, z);
            while (position.getY() > abom.level().getMinBuildHeight()) {
                if (abom.level().getBlockState(position.below()).blocksMotion()) {
                    return position.getY();
                }
                position = position.below();
            }
            return y;
        }

        private void detachRestrictionRing() {
            AABB searchArea = new AABB(ringCenterX, ringCenterY, ringCenterZ, ringCenterX, ringCenterY, ringCenterZ)
                    .inflate(abom.restrictionBoundaryRadius() + 8.0D);
            for (RotatingSoulShieldEntity shield : abom.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == abom)) {
                shield.switchToAroundPosition(ringCenterX, ringCenterY, ringCenterZ);
            }
            ringDetached = true;
        }

        private void restoreRestrictionRing() {
            if (!ringDetached) {
                return;
            }

            AABB searchArea = new AABB(ringCenterX, ringCenterY, ringCenterZ, ringCenterX, ringCenterY, ringCenterZ)
                    .inflate(abom.restrictionBoundaryRadius() + 8.0D);
            for (RotatingSoulShieldEntity shield : abom.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == abom)) {
                shield.switchToAroundCaster();
            }
            ringDetached = false;
        }

        private void finishAttack() {
            attackPhase = PHASE_DONE;
            abom.laserAttackActive = false;
            discardThunderBolts();
            restoreRestrictionRing();
            abom.setDeltaMovement(Vec3.ZERO);
        }

        private void discardThunderBolts() {
            for (LightningBoltEntity bolt : thunderBolts) {
                if (!bolt.isRemoved()) {
                    bolt.discard();
                }
            }
            thunderBolts.clear();
        }

        @Override
        public void stop() {
            discardThunderBolts();
            restoreRestrictionRing();
            abom.setDeltaMovement(Vec3.ZERO);
            abom.laserAttackActive = false;
            if (abom.phase == PHASE_3 && (target == null || !target.isAlive())) {
                abom.finishPhase3();
                return;
            }
            abom.setCooldownNextAttack(abom.phase == PHASE_3 ? 0 : abom.configInt(Config.ABOMINATION_LASER_COOLDOWN));
            abom.rollNextAttack(ATK_LASER);
        }
    }


    private static class DashAttack extends Goal {
        private final AbominationEntity abom;
        private LivingEntity target;
        private Vec3 dashDirection;
        private int dashesRemaining, timer, attackPhase;

        private static final int PHASE_WINDUP = 0;
        private static final int PHASE_DASH = 1;
        private static final int PHASE_RECOVER = 2;
        private static final int PHASE_DONE = 3;

        public DashAttack(AbominationEntity abom) {
            this.abom = abom;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return abom.nextAttack == ATK_DASH && abom.isAttackPhase() && abom.attackCooldown <= 0 && abom.getTarget() != null && abom.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return abom.nextAttack == ATK_DASH && target != null && target.isAlive() && attackPhase != PHASE_DONE && abom.isAttackPhase();
        }

        @Override
        public void start() {
            target = abom.getTarget();
            dashesRemaining = abom.configInt(Config.ABOMINATION_DASH_COUNT);
            dashDirection = Vec3.ZERO;
            beginWindup();
            abom.playSound(MYFSounds.rosalyneSwingPrepare.get(), abom.getSoundVolume(), abom.getVoicePitch());
        }

        private void beginWindup() {
            attackPhase = PHASE_WINDUP;
            timer = abom.configInt(Config.ABOMINATION_DASH_WINDUP);
            if(abom.phase == PHASE_2){
                timer = abom.configInt(Config.ABOMINATION_DASH_WINDUP) * 2;
            }
            abom.setDeltaMovement(Vec3.ZERO);
            //abom.setAnimation(ANIM_DASH_WINDUP);
        }

        @Override
        public void tick() {
            if (target == null) {
                attackPhase = PHASE_DONE;
                return;
            }

            timer--;
            abom.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (attackPhase == PHASE_WINDUP) {
                abom.getMoveControl().setWantedPosition(abom.getX(), abom.getY(), abom.getZ(), 1);
                if (timer <= 0) {
                    beginDash();
                }
            }
            else if (attackPhase == PHASE_DASH) {
                abom.setDeltaMovement(dashDirection.scale(abom.configDouble(Config.ABOMINATION_DASH_SPEED)));
                hurtDashContacts();
                if (timer % 2 == 0) {
                    leaveDashTrail();
                }
                if (timer <= 0) {
                    beginRecover();
                }
            }
            else if (attackPhase == PHASE_RECOVER) {
                abom.setDeltaMovement(abom.getDeltaMovement().scale(0.6));
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

        private void beginDash() {
            Vec3 from = new Vec3(abom.getX(), abom.getY() + 0.75, abom.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
            dashDirection = to.subtract(from).normalize();
            if (dashDirection.lengthSqr() < 1.0E-4) {
                dashDirection = abom.getLookAngle().normalize();
            }

            dashesRemaining--;
            attackPhase = PHASE_DASH;
            timer = abom.configInt(Config.ABOMINATION_DASH_TIME);
            //abom.setAnimation(ANIM_DASH);
            leaveDashTrail();
            abom.playSound(MYFSounds.rosalyneSwing.get(), 1.0F, 1.0F);
        }

        private void beginRecover() {
            attackPhase = PHASE_RECOVER;
            timer = abom.configInt(Config.ABOMINATION_DASH_RECOVER);
            if(abom.phase == PHASE_2){
                timer = abom.configInt(Config.ABOMINATION_DASH_RECOVER) * 2;
            }
            abom.setDeltaMovement(Vec3.ZERO);
            //abom.setAnimation(ANIM_DASH_RECOVER);
        }

        private void hurtDashContacts() {
            for (LivingEntity entity : abom.level().getEntitiesOfClass(LivingEntity.class, abom.getBoundingBox().inflate(0.4D), entity -> entity != abom && entity.isAlive() && !(entity instanceof AbominationEntity))) {
                abom.doHurtTarget(entity);
            }
        }

        private void leaveDashTrail() {
            ProjectileLineEntity ghost = abom.readyLineProjectile();
            setMyfProjectileDamage(ghost, abom.getBossDamage());
            ghost.setUp(1, dashDirection.x, dashDirection.y, dashDirection.z, abom.getX(), abom.getY() + 0.75, abom.getZ());
            abom.level().addFreshEntity(ghost);

            if (abom.phase == PHASE_2 && abom.getBossDifficulty() >= 1) {
                Vec3 perpendicular = new Vec3(-dashDirection.z, 0, dashDirection.x).normalize();
                if (perpendicular.lengthSqr() < 1.0E-4) {
                    perpendicular = new Vec3(1, 0, 0);
                }

                ProjectileLineEntity left = abom.readyLineProjectile();
                setMyfProjectileDamage(left, abom.getBossDamage());
                left.setUp(1, perpendicular.x, perpendicular.y, perpendicular.z, abom.getX(), abom.getY() + 0.75, abom.getZ());
                abom.level().addFreshEntity(left);

                ProjectileLineEntity right = abom.readyLineProjectile();
                setMyfProjectileDamage(right, abom.getBossDamage());
                right.setUp(1, -perpendicular.x, -perpendicular.y, -perpendicular.z, abom.getX(), abom.getY() + 0.75, abom.getZ());
                abom.level().addFreshEntity(right);
            }
        }

        @Override
        public void stop() {
            abom.setDeltaMovement(Vec3.ZERO);
            abom.setCooldownNextAttack(abom.configInt(Config.ABOMINATION_DASH_COOLDOWN));
            abom.rollNextAttack(ATK_DASH);
            //if (abom.isAttackPhase()) abom.setAnimation(ANIM_IDLE);
        }
    }
}
