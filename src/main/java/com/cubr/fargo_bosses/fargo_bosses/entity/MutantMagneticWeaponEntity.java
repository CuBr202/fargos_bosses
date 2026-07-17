package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.github.alexmodguy.alexscaves.server.entity.item.MagneticWeaponEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * MagneticWeaponEntity only supplies its autonomous Teletor behavior when its
 * controller is a TeletorEntity. This variant retains the original item and
 * renderer behavior while allowing Mutant to direct it during an attack.
 */
public class MutantMagneticWeaponEntity extends MagneticWeaponEntity {
    private static final EntityDataAccessor<Integer> MUTANT_CONTROLLER_ID = SynchedEntityData.defineId(MutantMagneticWeaponEntity.class, EntityDataSerializers.INT);

    @Nullable
    private LivingEntity attackTarget;
    private float attackDamage;
    private double chaseSpeed;
    private double hitDistance;
    private int hitCooldownDuration;
    private int rangeParticleInterval;
    private int rangeParticlePoints;
    private double rangeParticleHeight;
    private int hitCooldown;

    public MutantMagneticWeaponEntity(EntityType<? extends MutantMagneticWeaponEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MUTANT_CONTROLLER_ID, -1);
    }

    public void setMutantController(MutantEntity mutant) {
        setControllerUUID(mutant.getUUID());
        entityData.set(MUTANT_CONTROLLER_ID, mutant.getId());
    }

    @Nullable
    public MutantEntity getMutantController() {
        if (!level().isClientSide) {
            return getController() instanceof MutantEntity mutant ? mutant : null;
        }

        return level().getEntity(entityData.get(MUTANT_CONTROLLER_ID)) instanceof MutantEntity mutant ? mutant : null;
    }

    public void setAttackTarget(@Nullable LivingEntity attackTarget) {
        this.attackTarget = attackTarget;
    }

    public void setAttackDamage(float attackDamage) {
        this.attackDamage = attackDamage;
    }

    public void configureAttack(double chaseSpeed, double hitDistance, int hitCooldownDuration,
                                int rangeParticleInterval, int rangeParticlePoints, double rangeParticleHeight) {
        this.chaseSpeed = chaseSpeed;
        this.hitDistance = hitDistance;
        this.hitCooldownDuration = hitCooldownDuration;
        this.rangeParticleInterval = rangeParticleInterval;
        this.rangeParticlePoints = rangeParticlePoints;
        this.rangeParticleHeight = rangeParticleHeight;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide || isRemoved()) {
            return;
        }

        MutantEntity mutant = getMutantController();
        if (mutant == null || attackTarget == null || !attackTarget.isAlive()) {
            discard();
            return;
        }

        if (hitCooldown > 0) {
            hitCooldown--;
        }

        Vec3 destination = attackTarget.getBoundingBox().getCenter();
        Vec3 direction = destination.subtract(position());
        double distance = direction.length();
        if (distance > 1.0E-4D) {
            Vec3 velocity = direction.scale(chaseSpeed / distance);
            setDeltaMovement(velocity);
            hasImpulse = true;

            float yaw = (float) (-Math.atan2(velocity.x, velocity.z) * 180.0D / Math.PI);
            float pitch = (float) (-Math.atan2(velocity.y, velocity.horizontalDistance()) * 180.0D / Math.PI);
            setYRot(yaw);
            setXRot(pitch);
        }

        if (distance <= hitDistance && hitCooldown == 0) {
            attackTarget.hurt(mutant.damageSources().mobAttack(mutant), attackDamage);
            hitCooldown = hitCooldownDuration;
        }

        if (rangeParticleInterval > 0 && rangeParticlePoints > 0 && tickCount % rangeParticleInterval == 0) {
            spawnAttackRangeParticles();
        }
    }

    private void spawnAttackRangeParticles() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int i = 0; i < rangeParticlePoints; i++) {
            double angle = i * Math.PI * 2.0D / rangeParticlePoints;
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    getX() + Math.cos(angle) * hitDistance, getY() + rangeParticleHeight,
                    getZ() + Math.sin(angle) * hitDistance, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }
}
