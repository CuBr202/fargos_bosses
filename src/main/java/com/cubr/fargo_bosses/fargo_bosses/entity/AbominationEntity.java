package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModEntities;
import lykrast.meetyourfight.entity.BossEntity;
import lykrast.meetyourfight.entity.ProjectileLineEntity;
import lykrast.meetyourfight.entity.ProjectileTargetedEntity;
import lykrast.meetyourfight.entity.ai.MoveFrontOfTarget;
import lykrast.meetyourfight.entity.ai.StationaryAttack;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.registry.MYFSounds;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.Projectile.LightningBoltEntity;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.Projectile.ThrownPhantomDaggerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class AbominationEntity extends BossEntity {
    public static final int HP = 200, DMG = 10;

    public static final int PHASE_1 = 0, PHASE_1_TRANSITION = 1, PHASE_2 = 2, PHASE_2_TRANSITION = 3, PHASE_3 = 4, DEATH_PHASE = 5;
    private static final float PHASE_1_THRESHOLD = 2.0F / 3.0F;
    private static final int TRANSITION_TIME = 2 * 20;

    public static final int ATK_DICE = 0, ATK_CHIPS_CIRCLE = 1, ATK_DASH = 2, ATK_DAGGER = 3, ATK_LINEAR = 4, ATK_LASER = 5;

    // Settings
    private static final int DICE_ATTACK_DELAY = 15;
    private static final int DICE_ATTACK_AFTER_COOLDOWN = 20;

    private static final int DICE_AMOUNT_PHASE_1 = 8;
    private static final int DICE_AMOUNT_PHASE_2 = 12;

    private static final int CHIPS_ATTACK_DELAY = 20;

    private static final int CHIPS_CIRCLE_DELAY = 10;
    private static final int CHIPS_ATTACK_AFTER_COOLDOWN = 20;

    private static final int DAGGER_WAVES = 4;
    private static final int DAGGERS_PER_WAVE = 12;
    private static final int DAGGER_ALIGN_TIME = 10;
    private static final int DAGGER_WAVE_INTERVAL = 20;
    private static final int DAGGER_REST_TIME = 160;
    private static final int DAGGER_DRIFT_INTERVAL = 15;
    private static final float DAGGER_DAMAGE = 6.0F;
    private static final float DAGGER_SPEED = 0.7F;
    private static final int DAGGER_RETURN_TICK = 80;

    private static final int DASH_ATTACK_AFTER_COOLDOWN = 20;
    private static final int DASH_ATTACK_COUNT = 5;
    private static final int DASH_WINDUP = 5;
    private static final int DASH_TIME = 10;
    private static final int DASH_RECOVER = 5;
    private static final double DASH_SPEED = 2;

    private static final int LINEAR_ATTACK_AMOUNT = 10; // notice that the actual attack amount is LINEAR_ATTACK_AMOUNT*2
    private static final int LINEAR_ATTACK_WINDUP = 30;
    private static final int LINEAR_ATTACK_COUNT = 20;
    private static final int LINEAR_ATTACK_INTERVAL = 30; // Initially

    private static final int LINEAR_ATTACK_INTERVAL_FINAL = 10;
    private static final int LINEAR_ATTACK_DISTANCE = 7;

    private static final int LASER_ALIGN_TIME = 20;
    private static final double LASER_MOVE_SPEED = 3.0D;
    private static final double LASER_START_REACH_DISTANCE = 0.5D;
    private static final int LASER_CHARGE_TIME = 10;
    private static final int LASER_SWEEP_TIME = 80;
    private static final int LASER_BEAM_END_BUFFER = 0;
    private static final int LASER_BEAM_DURATION = LASER_CHARGE_TIME + LASER_SWEEP_TIME + LASER_BEAM_END_BUFFER;
    private static final int LASER_MIN_REPEATS = 2;
    private static final int LASER_MAX_REPEATS = 2;
    private static final float LASER_BASE_DAMAGE = 5.0F;
    private static final float LASER_MAX_HEALTH_DAMAGE = 0.01F;
    private static final double LASER_RING_CLEARANCE = 8.0D;
    private static final float LASER_SIDE_OFFSET = 2.0F;
    private static final int LASER_ATTACK_AFTER_COOLDOWN = 20;
    private static final int LASER_ATTACK_THUNDER_ROUNDS = 7;
    private static final int LASER_ATTACK_THUNDER_WARMUP = 0;
    private static final int LASER_ATTACK_THUNDER_MAX_LIFETIME = Integer.MAX_VALUE;
    private static final float LASER_ATTACK_THUNDER_DAMAGE = 5.0F;
    private static final double LASER_ATTACK_THUNDER_JITTER = 4.0D;


    private static final int RESTRICTION_BOUNDARY_SHIELDS = 24;
    private static final float RESTRICTION_BOUNDARY_RADIUS = 20.0F;
    private static final float RESTRICTION_BOUNDARY_DAMAGE = 10.0F;
    private static final float RESTRICTION_BOUNDARY_ORBIT_SPEED = Mth.TWO_PI / 160.0F;

    private int phase, phaseTimer, attackCooldown, nextAttack;
    private boolean restrictionBoundaryCreated;

    public AbominationEntity(EntityType<? extends AbominationEntity> type, Level level) {
        super(type, level);
        moveControl = new VexMovementController(this);
        xpReward = 10000;
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
        goalSelector.addGoal(1, new DiceAttack(this));
        goalSelector.addGoal(2, new ChipsAttack(this));
        goalSelector.addGoal(3, new PhantomDaggerAttack(this));
        goalSelector.addGoal(4, new DashAttack(this));
        goalSelector.addGoal(5, new LinearAttack(this));
        goalSelector.addGoal(6, new LaserAttack(this));
        goalSelector.addGoal(7, new MoveFrontOfTarget(this, 1));
        goalSelector.addGoal(8, new VexMoveRandomGoal(this, 0.25));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, HP)
                .add(Attributes.FOLLOW_RANGE, 64)
                .add(Attributes.ATTACK_DAMAGE, DMG);
    }

    public static void spawn(Player player, Level world) {
        RandomSource rand = player.getRandom();
        AbominationEntity abomination = ModEntities.ABOMINATION.get().create(world);
        if (abomination == null) {
            return;
        }

        abomination.moveTo(player.getX() + rand.nextInt(15) - 7, player.getY() + rand.nextInt(9) - 1, player.getZ() + rand.nextInt(15) - 7, rand.nextFloat() * 360 - 180, 0);
        if (!player.getAbilities().instabuild) {
            abomination.setTarget(player);
        }
        abomination.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));

        ForgeEventFactory.onFinalizeSpawn(abomination, (ServerLevel) world, world.getCurrentDifficultyAt(abomination.blockPosition()), MobSpawnType.EVENT, null, null);
        world.addFreshEntity(abomination);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 2, 1);
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        phase = PHASE_1;
        phaseTimer = 0;
        restrictionBoundaryCreated = false;
        setInvulnerable(false);
        setHealth(getMaxHealth());
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if ((phase == PHASE_1_TRANSITION || phase == PHASE_2_TRANSITION || phase == PHASE_3)) {
            return false;
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
        return MYFSounds.musicBellringer.get();
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return FargoBosses.rl("entities/bellringer");
    }

    public boolean isAttackPhase() {
        return phase == PHASE_1 || phase == PHASE_2 || phase == PHASE_3;
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
        else if (ignore == ATK_DASH && phase == PHASE_2) {
            nextAttack = ATK_LASER;//random.nextBoolean() ? ATK_LINEAR : ATK_LASER;
        }
        else if (ignore == ATK_CHIPS_CIRCLE || ignore == ATK_DAGGER || ignore == ATK_DICE) {
            nextAttack = ATK_DASH;
        }
        else {
            nextAttack = ATK_DICE;
        }
    }

    private ProjectileTargetedEntity readyTargeted() {
        ProjectileTargetedEntity proj = new ProjectileTargetedEntity(level(), this);
        proj.setOwner(this);
        proj.setPos(getX(), getEyeY() + 1, getZ());
        return proj;
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
        if (phase == PHASE_1 && tickCount > 10 && getHealth() <= getMaxHealth() * PHASE_1_THRESHOLD) {
            beginPhase1Transition();
        }
        else if (phase == PHASE_2 && getHealth() <= 1.0F) {
            beginPhase2Transition();
        }

        if (phase == PHASE_1_TRANSITION || phase == PHASE_2_TRANSITION) {
            phaseTimer--;
            if (phaseTimer <= 0) {
                finishTimedPhase();
            }
        }
        super.customServerAiStep();
    }

    private void beginPhase1Transition() {
        phase = PHASE_1_TRANSITION;
        phaseTimer = TRANSITION_TIME;
        attackCooldown = TRANSITION_TIME;
        setInvulnerable(true);
        createRestrictionBoundary();
    }

    private void createRestrictionBoundary() {
        if (level().isClientSide || restrictionBoundaryCreated) {
            return;
        }

        restrictionBoundaryCreated = true;
        for (int i = 0; i < RESTRICTION_BOUNDARY_SHIELDS; i++) {
            float angle = i / (float) RESTRICTION_BOUNDARY_SHIELDS * Mth.TWO_PI;
            RotatingSoulShieldEntity shield = new RotatingSoulShieldEntity(level(), this, 0.0D, RESTRICTION_BOUNDARY_RADIUS, angle, RESTRICTION_BOUNDARY_ORBIT_SPEED, RESTRICTION_BOUNDARY_DAMAGE, true);
            shield.setManualLifetime(-1);
            shield.setOrbitAroundCaster(RESTRICTION_BOUNDARY_RADIUS, angle, RESTRICTION_BOUNDARY_ORBIT_SPEED, 0.0D);
            shield.setPos(getX() + Math.cos(angle) * RESTRICTION_BOUNDARY_RADIUS, getY(), getZ() + Math.sin(angle) * RESTRICTION_BOUNDARY_RADIUS);
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
    }

    private void finishTimedPhase() {
        if (phase == PHASE_1_TRANSITION) {
            phase = PHASE_2;
            phaseTimer = 0;
            attackCooldown = 20;
            setHealth(getMaxHealth());
            setInvulnerable(false);
            rollNextAttack(-1);
        }
        else if (phase == PHASE_2_TRANSITION) {
            phase = PHASE_3;
            phaseTimer = 0;
            attackCooldown = 0;
            setHealth(1.0F);
            setInvulnerable(true);
            nextAttack = ATK_DICE;
        }
    }

    private void finishPhase3() {
        if (phase != PHASE_3) {
            return;
        }

        phase = DEATH_PHASE;
        setInvulnerable(false);
        kill();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        phase = compound.getInt("Phase");
        phaseTimer = compound.getInt("PhaseTimer");
        attackCooldown = compound.getInt("AttackCooldown");
        nextAttack = compound.getInt("NxtAt");
        restrictionBoundaryCreated = compound.getBoolean("RestrictionBoundaryCreated");
        setInvulnerable(phase == PHASE_1_TRANSITION || phase == PHASE_2_TRANSITION || phase == PHASE_3);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Phase", phase);
        compound.putInt("PhaseTimer", phaseTimer);
        compound.putInt("AttackCooldown", attackCooldown);
        compound.putInt("NxtAt", nextAttack);
        compound.putBoolean("RestrictionBoundaryCreated", restrictionBoundaryCreated);
    }

    //Regular attacks below
    private static class DiceAttack extends StationaryAttack {
        private AbominationEntity abom;
        private LivingEntity target;
        private int attackRemaining, attackDelay;

        public DiceAttack(AbominationEntity abom) {
            super(abom);
            this.abom = abom;
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
            //abom.setAnimation(ANIM_DICE_WINDUP);
            attackDelay = getPhaseDelay(DICE_ATTACK_DELAY);
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
            if (abom.phase == PHASE_1) return DICE_AMOUNT_PHASE_1;
            //Phase 1
            else if(abom.phase == PHASE_2) return DICE_AMOUNT_PHASE_2;
            return DICE_AMOUNT_PHASE_2;
        }

        private int getPhaseDelay(int normalDelay) {
            return abom.phase == PHASE_3 ? Math.max(1, normalDelay* 2/3) : normalDelay;
        }

        @Override
        public void tick() {
            super.tick();
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

            int dettime = abom.phase == PHASE_1 ? 0 : abom.random.nextInt(11);
            bomb.setup(25 + dettime, 15 + dettime, bombX, bombY, bombZ);
            abom.level().addFreshEntity(bomb);
            abom.playSound(MYFSounds.dameFortunaShoot.get(), 2.0F, (abom.random.nextFloat() - abom.random.nextFloat()) * 0.2F + 1.0F);
        }

        @Override
        public void stop() {
            if (abom.phase == PHASE_3) {
                if (target == null || !target.isAlive()) {
                    abom.finishPhase3();
                }
                else {
                    abom.rollNextAttack(ATK_DICE);
                }
                return;
            }
            abom.setCooldownNextAttack(DICE_ATTACK_AFTER_COOLDOWN);
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
            attackDelay = CHIPS_ATTACK_DELAY;
            attackRemaining = 3;
            circleDelay = CHIPS_CIRCLE_DELAY;
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
            attackDelay = attackRemaining > 0 ? CHIPS_ATTACK_DELAY : CHIPS_ATTACK_DELAY + 10;
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
            abom.setCooldownNextAttack(CHIPS_ATTACK_AFTER_COOLDOWN);
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
            wavesRemaining = DAGGER_WAVES;
            attackDelay = 0;
            restTime = 0;
            alignTime = DAGGER_ALIGN_TIME;
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
                    attackDelay = DAGGER_WAVE_INTERVAL - 1;
                }
                else {
                    restTime = DAGGER_REST_TIME;
                    //abom.setAnimation(ANIM_DAGGER_REST);
                }
            }
        }

        private void throwDaggerWave() {
            double sx = abom.getX();
            double sy = abom.getY() + abom.getBbHeight() * 0.5;
            double sz = abom.getZ();
            double randomOffset = abom.random.nextDouble();

            for (int i = 0; i < DAGGERS_PER_WAVE; i++) {
                double angle = (i / (double) DAGGERS_PER_WAVE + randomOffset) * Mth.TWO_PI;
                double dx = Math.cos(angle);
                double dz = Math.sin(angle);

                ThrownPhantomDaggerEntity dagger = new ThrownPhantomDaggerEntity(net.miauczel.legendary_monsters.entity.ModEntities.THROWN_PHANTOM_DAGGER.get(), abom.level());
                dagger.setOwner(abom);
                dagger.setReturnEntity(abom);
                dagger.setReturnTick(DAGGER_RETURN_TICK);
                dagger.setDamage(DAGGER_DAMAGE);
                dagger.setRed(true);
                dagger.setPos(sx, sy, sz);
                dagger.setYRot((float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
                dagger.shoot(dx, 0, dz, DAGGER_SPEED, 0.0F);
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

            driftTime = DAGGER_DRIFT_INTERVAL;
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
            attacksRemaining = LINEAR_ATTACK_COUNT;
            attackDelay = LINEAR_ATTACK_WINDUP;
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

            attackDelay = (LINEAR_ATTACK_INTERVAL - LINEAR_ATTACK_INTERVAL_FINAL) * attacksRemaining / LINEAR_ATTACK_COUNT + LINEAR_ATTACK_INTERVAL_FINAL;
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
            for (int i = -LINEAR_ATTACK_AMOUNT; i <= LINEAR_ATTACK_AMOUNT; i++) {
                ProjectileLineEntity projectile = abom.readyLineProjectile();
                projectile.setUp(15 + offset * i, directionX, 0, directionZ,
                        targetX - LINEAR_ATTACK_DISTANCE * directionX + i * directionZ,
                        targetY,
                        targetZ - LINEAR_ATTACK_DISTANCE * directionZ + i * directionX);
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
            attackPhase = PHASE_ALIGN;
            phaseTime = LASER_ALIGN_TIME;
            sweepsRemaining = LASER_MIN_REPEATS + abom.random.nextInt(LASER_MAX_REPEATS - LASER_MIN_REPEATS + 1);
            ringCenterX = abom.getX();
            ringCenterY = findGroundY();
            ringCenterZ = abom.getZ();
            ringDetached = false;
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
                    phaseTime = LASER_SWEEP_TIME;
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
                ringCenterX = abom.getX();
                ringCenterZ = abom.getZ();
                detachRestrictionRing();
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
            double distanceStart = RESTRICTION_BOUNDARY_RADIUS + LASER_RING_CLEARANCE;
            double distanceEnd = RESTRICTION_BOUNDARY_RADIUS - LASER_RING_CLEARANCE;
            sweepStart = new Vec3(ringCenterX + outward.x * distanceStart, ringCenterY, ringCenterZ + outward.z * distanceStart);
            sweepEnd = new Vec3(ringCenterX - outward.x * distanceEnd, ringCenterY, ringCenterZ - outward.z * distanceEnd);
            attackPhase = PHASE_MOVE_TO_START;
        }

        private void moveToSweepStart() {
            abom.getMoveControl().setWantedPosition(sweepStart.x, sweepStart.y, sweepStart.z, LASER_MOVE_SPEED);
            if (abom.distanceToSqr(sweepStart.x, sweepStart.y, sweepStart.z) <= LASER_START_REACH_DISTANCE * LASER_START_REACH_DISTANCE) {
                faceRingCenter();
                lockedSweepYaw = abom.yHeadRot;
                spawnSideLasers();
                attackPhase = PHASE_CHARGE;
                phaseTime = LASER_CHARGE_TIME;
                abom.setDeltaMovement(Vec3.ZERO);
                abom.playSound(SoundEvents.BEACON_ACTIVATE, 2.0F, 1.0F);
            }
        }

        private void spawnSideLasers() {
            FlexibleEnergyBeamEntity left = new FlexibleEnergyBeamEntity(ModEntities.FLEXIBLE_ENERGY_BEAM.get(), abom.level(), abom,
                    LASER_BEAM_DURATION, LASER_BASE_DAMAGE, LASER_MAX_HEALTH_DAMAGE, 90.0F, 0.0F);
            left.setRelativePositionOffset(0.0F, -LASER_SIDE_OFFSET, 0.0F);

            FlexibleEnergyBeamEntity right = new FlexibleEnergyBeamEntity(ModEntities.FLEXIBLE_ENERGY_BEAM.get(), abom.level(), abom,
                    LASER_BEAM_DURATION, LASER_BASE_DAMAGE, LASER_MAX_HEALTH_DAMAGE, -90.0F, 0.0F);
            right.setRelativePositionOffset(0.0F, LASER_SIDE_OFFSET, 0.0F);

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
            double progress = 1.0D - phaseTime / (double) LASER_SWEEP_TIME;
            Vec3 destination = sweepStart.scale(1.0D - progress).add(sweepEnd.scale(progress));
            setFacing(lockedSweepYaw);
            abom.setPos(destination.x, destination.y, destination.z);
            abom.setDeltaMovement(Vec3.ZERO);

            int thunderInterval = Math.max(1, LASER_SWEEP_TIME / LASER_ATTACK_THUNDER_ROUNDS);
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
            double spacing = RESTRICTION_BOUNDARY_RADIUS * 2.0D / (LASER_ATTACK_THUNDER_ROUNDS - 1);
            double centerOffset = (LASER_ATTACK_THUNDER_ROUNDS - 1) * 0.5D;

            for (int i = 0; i < LASER_ATTACK_THUNDER_ROUNDS; i++) {
                double distance = (i - centerOffset) * spacing;
                double x = abom.getX() + lateral.x * distance + randomJitter();
                double z = abom.getZ() + lateral.z * distance + randomJitter();
                LightningBoltEntity bolt = new LightningBoltEntity(abom.level(), x, ringCenterY, z, 0.0F,
                        LASER_ATTACK_THUNDER_WARMUP, abom, LASER_ATTACK_THUNDER_MAX_LIFETIME, LASER_ATTACK_THUNDER_DAMAGE);
                thunderBolts.add(bolt);
                abom.level().addFreshEntity(bolt);
            }
        }

        private double randomJitter() {
            return (abom.random.nextDouble() * 2.0D - 1.0D) * LASER_ATTACK_THUNDER_JITTER;
        }

        private void setFacing(float yaw) {
            abom.setYRot(yaw);
            abom.yHeadRot = yaw;
            abom.yBodyRot = yaw;
            abom.setXRot(0.0F);
        }

        private double findGroundY() {
            BlockPos position = abom.blockPosition();
            while (position.getY() > abom.level().getMinBuildHeight()) {
                if (abom.level().getBlockState(position.below()).blocksMotion()) {
                    return position.getY();
                }
                position = position.below();
            }
            return abom.getY();
        }

        private void detachRestrictionRing() {
            AABB searchArea = new AABB(ringCenterX, ringCenterY, ringCenterZ, ringCenterX, ringCenterY, ringCenterZ)
                    .inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
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
                    .inflate(RESTRICTION_BOUNDARY_RADIUS + 8.0D);
            for (RotatingSoulShieldEntity shield : abom.level().getEntitiesOfClass(RotatingSoulShieldEntity.class, searchArea,
                    shield -> shield.getCaster() == abom)) {
                shield.switchToAroundCaster();
            }
            ringDetached = false;
        }

        private void finishAttack() {
            attackPhase = PHASE_DONE;
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
            if (abom.phase == PHASE_3 && (target == null || !target.isAlive())) {
                abom.finishPhase3();
                return;
            }
            abom.setCooldownNextAttack(abom.phase == PHASE_3 ? 0 : LASER_ATTACK_AFTER_COOLDOWN);
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
            dashesRemaining = DASH_ATTACK_COUNT;
            dashDirection = Vec3.ZERO;
            beginWindup();
            abom.playSound(MYFSounds.rosalyneSwingPrepare.get(), abom.getSoundVolume(), abom.getVoicePitch());
        }

        private void beginWindup() {
            attackPhase = PHASE_WINDUP;
            timer = DASH_WINDUP;
            if(abom.phase == PHASE_2){
                timer = DASH_WINDUP*2;
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
                abom.setDeltaMovement(dashDirection.scale(DASH_SPEED));
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
            timer = DASH_TIME;
            //abom.setAnimation(ANIM_DASH);
            leaveDashTrail();
            abom.playSound(MYFSounds.rosalyneSwing.get(), 1.0F, 1.0F);
        }

        private void beginRecover() {
            attackPhase = PHASE_RECOVER;
            timer = DASH_RECOVER;
            if(abom.phase == PHASE_2){
                timer = DASH_RECOVER*2;
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
            ghost.setUp(1, dashDirection.x, dashDirection.y, dashDirection.z, abom.getX(), abom.getY() + 0.75, abom.getZ());
            abom.level().addFreshEntity(ghost);

            if (abom.phase == PHASE_2) {
                Vec3 perpendicular = new Vec3(-dashDirection.z, 0, dashDirection.x).normalize();
                if (perpendicular.lengthSqr() < 1.0E-4) {
                    perpendicular = new Vec3(1, 0, 0);
                }

                ProjectileLineEntity left = abom.readyLineProjectile();
                left.setUp(1, perpendicular.x, perpendicular.y, perpendicular.z, abom.getX(), abom.getY() + 0.75, abom.getZ());
                abom.level().addFreshEntity(left);

                ProjectileLineEntity right = abom.readyLineProjectile();
                right.setUp(1, -perpendicular.x, -perpendicular.y, -perpendicular.z, abom.getX(), abom.getY() + 0.75, abom.getZ());
                abom.level().addFreshEntity(right);
            }
        }

        @Override
        public void stop() {
            abom.setDeltaMovement(Vec3.ZERO);
            abom.setCooldownNextAttack(DASH_ATTACK_AFTER_COOLDOWN);
            abom.rollNextAttack(ATK_DASH);
            //if (abom.isAttackPhase()) abom.setAnimation(ANIM_IDLE);
        }
    }
}
