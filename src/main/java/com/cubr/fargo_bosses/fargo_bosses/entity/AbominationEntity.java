package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModEntities;
import lykrast.meetyourfight.entity.BossEntity;
import lykrast.meetyourfight.entity.FortunaBombEntity;
import lykrast.meetyourfight.entity.ai.MoveFrontOfTarget;
import lykrast.meetyourfight.entity.ai.StationaryAttack;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;

public class AbominationEntity extends BossEntity {
    public static final int HP = 200;

    public static final int PHASE_1 = 0, PHASE_1_TRANSITION = 1, PHASE_2 = 2, PHASE_2_TRANSITION = 3, PHASE_3 = 4, DEATH_PHASE = 5;
    private static final float PHASE_1_THRESHOLD = 2.0F / 3.0F;
    private static final int TRANSITION_TIME = 2 * 20;
    private static final int PHASE_3_TIME = 20 * 20;

    public  static final int ATK_DICE = 0;

    // Settings
    private static final int DICE_ATTACK_DELAY = 15;
    private static final int DICE_ATTACK_AFTER_COOLDOWN = 20;


    private int phase, phaseTimer, attackCooldown, nextAttack;

    public AbominationEntity(EntityType<? extends AbominationEntity> type, Level level) {
        super(type, level);
        moveControl = new VexMovementController(this);
        xpReward = 50;
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
        goalSelector.addGoal(1,new DiceAttack(this));
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
                .add(Attributes.FOLLOW_RANGE, 64);
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
        /*
        int max = ATK_CHIPS_CIRCLE;
        if (phase == PHASE_2 || phase == PHASE_3) max = ATK_CHIPS_STRAFE;
        if (ignore >= 0) {
            nextAttack = random.nextInt(max);
            if (nextAttack >= ignore) nextAttack++;
        }
        else nextAttack = random.nextInt(max+1);
         */
        nextAttack = ATK_DICE;
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

        if (phase == PHASE_1_TRANSITION || phase == PHASE_2_TRANSITION || phase == PHASE_3) {
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
            phaseTimer = PHASE_3_TIME;
            attackCooldown = 0;
            setHealth(1.0F);
            setInvulnerable(true);
            rollNextAttack(-1);
        }
        else if (phase == PHASE_3) {
            phase = DEATH_PHASE;
            setInvulnerable(false);
            kill();
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        phase = compound.getInt("Phase");
        phaseTimer = compound.getInt("PhaseTimer");
        attackCooldown = compound.getInt("AttackCooldown");
        nextAttack = compound.getInt("NxtAt");
        setInvulnerable(phase == PHASE_1_TRANSITION || phase == PHASE_2_TRANSITION || phase == PHASE_3);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Phase", phase);
        compound.putInt("PhaseTimer", phaseTimer);
        compound.putInt("AttackCooldown", attackCooldown);
        compound.putInt("NxtAt", nextAttack);
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
            attackDelay = DICE_ATTACK_DELAY;
            attackRemaining = getAttackCount();
            abom.playSound(MYFSounds.dameFortunaAttack.get(), abom.getSoundVolume(), abom.getVoicePitch());
        }

        private int getAttackCount() {
            if (abom.phase == PHASE_2) return 7;
            //Phase 1
            return 6;
        }

        private int getSplitDirectionCount() {
            if (abom.phase == PHASE_2) return 12;
            //Phase 1
            return 8;
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
            attackDelay = attackRemaining == 0 ? 40 : abom.phase == PHASE_3 ? 12 : 20;

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
            abom.setCooldownNextAttack(DICE_ATTACK_AFTER_COOLDOWN);
            abom.rollNextAttack(ATK_DICE);
            //if we going to shuffle phase, let that handle the next animation
            //if (abom.isAttackPhase()) abom.setAnimation(ANIM_IDLE);
        }

    }
}
