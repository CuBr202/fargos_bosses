package com.cubr.fargo_bosses.fargo_bosses.entity;

import lykrast.meetyourfight.entity.movement.VexMovementController;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.Projectile.IceSpikeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class FrostSpiritEntity extends Monster {
    public static final int HP = 40;
    public static final double ARMOR = 5.0D;
    public static final double ORBIT_RADIUS = 6.0D;
    public static final float ORBIT_ANGULAR_SPEED = Mth.TWO_PI / 120.0F;
    public static final int ICE_SPIKE_ATTACK_INTERVAL = 3 * 20;
    public static final int ICE_SPIKE_ATTACK_ROUNDS = 3;
    public static final double ICE_SPIKE_LINE_LENGTH = ORBIT_RADIUS * 2.0D;
    public static final double ICE_SPIKE_SPACING = 1.0D;
    public static final float ICE_SPIKE_SIDE_ANGLE = 30.0F * Mth.DEG_TO_RAD;
    public static final int ICE_SPIKE_WARMUP = 10;

    private int attackTimer = ICE_SPIKE_ATTACK_INTERVAL;
    private int attackRounds;
    private int attackRoundLimit = ICE_SPIKE_ATTACK_ROUNDS;
    private float orbitAngle;

    public FrostSpiritEntity(EntityType<? extends FrostSpiritEntity> type, Level level) {
        super(type, level);
        moveControl = new VexMovementController(this).slowdown(0.2D);
        xpReward = 5;
        orbitAngle = random.nextFloat() * Mth.TWO_PI;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, HP)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    public void setIceSpikeAttackRounds(int rounds) {
        attackRoundLimit = Math.max(1, rounds);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("IceSpikeAttackTimer")) attackTimer = tag.getInt("IceSpikeAttackTimer");
        if (tag.contains("IceSpikeAttackRounds")) attackRounds = tag.getInt("IceSpikeAttackRounds");
        if (tag.contains("IceSpikeAttackRoundLimit")) {
            attackRoundLimit = Math.max(1, tag.getInt("IceSpikeAttackRoundLimit"));
        }
        if (tag.contains("OrbitAngle")) orbitAngle = tag.getFloat("OrbitAngle");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("IceSpikeAttackTimer", attackTimer);
        tag.putInt("IceSpikeAttackRounds", attackRounds);
        tag.putInt("IceSpikeAttackRoundLimit", attackRoundLimit);
        tag.putFloat("OrbitAngle", orbitAngle);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        setHealth(getMaxHealth());
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new OrbitTargetGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, movement);
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
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (getTarget() == null || !getTarget().isAlive()) {
            attackTimer = ICE_SPIKE_ATTACK_INTERVAL;
            return;
        }

        if (--attackTimer <= 0) {
            fireIceSpikeLines(getTarget());
            attackTimer = ICE_SPIKE_ATTACK_INTERVAL;
            if (++attackRounds >= attackRoundLimit) discard();
        }
    }

    private void fireIceSpikeLines(LivingEntity target) {
        Vec3 direction = new Vec3(target.getX() - getX(), 0.0D, target.getZ() - getZ()).normalize();
        if (direction.lengthSqr() < 1.0E-6D) direction = new Vec3(1.0D, 0.0D, 0.0D);

        spawnIceSpikeLine(target, direction);
        spawnIceSpikeLine(target, direction.yRot(ICE_SPIKE_SIDE_ANGLE));
        spawnIceSpikeLine(target, direction.yRot(-ICE_SPIKE_SIDE_ANGLE));
    }

    private void spawnIceSpikeLine(LivingEntity target, Vec3 direction) {
        float rotation = (float) Math.atan2(direction.z, direction.x);
        for (double distance = 0.0D; distance <= ICE_SPIKE_LINE_LENGTH; distance += ICE_SPIKE_SPACING) {
            double x = getX() + direction.x * distance;
            double z = getZ() + direction.z * distance;
            spawnIceSpike(x, z, target.getY(), rotation);
        }
    }

    private void spawnIceSpike(double x, double z, double startY, float rotation) {
        BlockPos position = BlockPos.containing(x, startY, z);
        int lowestY = Mth.floor(startY) - 16;
        double surfaceOffset = 0.0D;

        while (position.getY() >= lowestY) {
            BlockPos below = position.below();
            BlockState belowState = level().getBlockState(below);
            if (belowState.isFaceSturdy(level(), below, Direction.UP)) {
                BlockState state = level().getBlockState(position);
                if (!state.isAir()) {
                    VoxelShape shape = state.getCollisionShape(level(), position);
                    if (!shape.isEmpty()) surfaceOffset = shape.max(Direction.Axis.Y);
                }
                level().addFreshEntity(new IceSpikeEntity(level(), x, position.getY() + surfaceOffset, z,
                        rotation, ICE_SPIKE_WARMUP, this));
                return;
            }
            position = position.below();
        }
    }

    private static class OrbitTargetGoal extends Goal {
        private final FrostSpiritEntity spirit;

        private OrbitTargetGoal(FrostSpiritEntity spirit) {
            this.spirit = spirit;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return spirit.getTarget() != null && spirit.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            LivingEntity target = spirit.getTarget();
            if (target == null) return;

            spirit.orbitAngle += ORBIT_ANGULAR_SPEED;
            double x = target.getX() + Mth.cos(spirit.orbitAngle) * ORBIT_RADIUS;
            double z = target.getZ() + Mth.sin(spirit.orbitAngle) * ORBIT_RADIUS;
            spirit.setPos(x, target.getY(), z);
            spirit.setDeltaMovement(Vec3.ZERO);
            spirit.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }
}
