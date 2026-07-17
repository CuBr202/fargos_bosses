package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.github.alexmodguy.alexscaves.server.entity.item.SpinningPeppermintEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class MutantSpinningPeppermintEntity extends SpinningPeppermintEntity {
    @Nullable
    private Vec3 orbitCenter;
    private int remainingLifetime;
    private float damage;
    private float hitRange;
    private float spinDirection = 1.0F;
    private boolean expandingSpiral;
    private float spiralTargetRadius;
    private float spiralRadiusGrowth;

    public MutantSpinningPeppermintEntity(EntityType<? extends MutantSpinningPeppermintEntity> type, Level level) {
        super(type, level);
    }

    public void configure(float radius, float spinSpeed, float startAngle, int lifetime, float damage, float hitRange) {
        setSpinRadius(radius);
        setSpinSpeed(spinSpeed);
        setStartAngle(startAngle);
        setLifespan(lifetime);
        remainingLifetime = lifetime;
        this.damage = damage;
        this.hitRange = hitRange;
    }

    public void configure(float radius, float spinSpeed, float startAngle, int lifetime, float damage, float hitRange,
                          float spinDirection) {
        configure(radius, spinSpeed, startAngle, lifetime, damage, hitRange);
        setSpinDirection(spinDirection);
    }

    public void setSpinDirection(float spinDirection) {
        this.spinDirection = spinDirection < 0.0F ? -1.0F : 1.0F;
    }

    public float getSpinDirection() {
        return spinDirection;
    }

    public void setOrbitCenter(Vec3 orbitCenter) {
        this.orbitCenter = orbitCenter;
        setSpinAroundPosition(orbitCenter);
    }

    public void startSpiralFromCaster(float targetRadius, float radiusGrowthPerTick) {
        LivingEntity caster = getOwner();
        if (caster == null) {
            return;
        }
        startSpiralFromCaster(caster, targetRadius, radiusGrowthPerTick);
    }

    public void startSpiralFromCaster(LivingEntity caster, float targetRadius, float radiusGrowthPerTick) {
        setOwner(caster);
        setOrbitCenter(caster.position());
        setPos(caster.position());
        setSpinRadius(0.0F);
        spiralTargetRadius = Math.max(0.0F, targetRadius);
        spiralRadiusGrowth = Math.max(0.0F, radiusGrowthPerTick);
        expandingSpiral = spiralTargetRadius > 0.0F && spiralRadiusGrowth > 0.0F;
    }

    @Override
    public float getDespawnTime(float partialTicks) {
        // The base renderer uses this to scale the peppermint in. Our custom
        // tick owns lifetime handling, so the base class's private counter is
        // never initialized.
        return getLifespan();
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            // The original tick applies the server's position interpolation on
            // clients. Without it, custom peppermints remain at spawn position.
            super.tick();
            return;
        }

        baseTick();
        if (isRemoved()) {
            return;
        }

        if (--remainingLifetime <= 0) {
            discard();
            return;
        }

        updateSpiral();
        if (isStraight()) {
            Vec3 velocity = new Vec3(0.0D, 0.0D, -0.01D * getSpinSpeed())
                    .yRot((float) -Math.toRadians(getYRot()));
            setDeltaMovement(getDeltaMovement().add(velocity));
            move(MoverType.SELF, getDeltaMovement());
        }
        else if (orbitCenter != null) {
            float angle = getStartAngle() + tickCount * getSpinSpeed() * spinDirection;
            Vec3 offset = new Vec3(0.0D, 0.0D, getSpinRadius())
                    .yRot((float) -Math.toRadians(angle));
            setPos(orbitCenter.add(offset));
            setDeltaMovement(Vec3.ZERO);
        }

        hurtEntities();
    }

    private void updateSpiral() {
        if (!expandingSpiral) {
            return;
        }

        LivingEntity caster = getOwner();
        if (caster != null) {
            setOrbitCenter(caster.position());
        }
        float radius = Math.min(spiralTargetRadius, getSpinRadius() + spiralRadiusGrowth);
        setSpinRadius(radius);
        if (radius >= spiralTargetRadius) {
            expandingSpiral = false;
        }
    }

    private void hurtEntities() {
        LivingEntity owner = getOwner();
        if (owner == null) {
            return;
        }

        DamageSource source = damageSources().mobProjectile(this, owner);
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox(),
                entity -> getBoundingBox().intersects(entity.getBoundingBox()))) {
            if (entity != owner && !owner.isAlliedTo(entity) && entity.hurt(source, damage)) {
                entity.knockback(0.3F, getX() - entity.getX(), getZ() - entity.getZ());
            }
        }
    }
}
