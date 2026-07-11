package com.cubr.fargo_bosses.fargo_bosses.entity;

import net.miauczel.legendary_monsters.Particle.ModParticles;
import net.miauczel.legendary_monsters.damagetype.ModDamageTypes;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.Projectile.EnergyBeamEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FlexibleEnergyBeamEntity extends EnergyBeamEntity {
    private static final EntityDataAccessor<Float> YAW_OFFSET = SynchedEntityData.defineId(FlexibleEnergyBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH_OFFSET = SynchedEntityData.defineId(FlexibleEnergyBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FORWARD_OFFSET = SynchedEntityData.defineId(FlexibleEnergyBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SIDE_OFFSET = SynchedEntityData.defineId(FlexibleEnergyBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> VERTICAL_OFFSET = SynchedEntityData.defineId(FlexibleEnergyBeamEntity.class, EntityDataSerializers.FLOAT);

    public FlexibleEnergyBeamEntity(EntityType<? extends FlexibleEnergyBeamEntity> type, Level level) {
        super(type, level);
    }

    public FlexibleEnergyBeamEntity(EntityType<? extends FlexibleEnergyBeamEntity> type, Level level, LivingEntity caster,
                                    int duration, float baseDamage, float maxHealthDamageMultiplier,
                                    float yawOffsetDegrees, float pitchOffsetDegrees) {
        this(type, level);
        this.caster = caster;
        setDuration(duration);
        setDamage(baseDamage);
        setHpDamage(maxHealthDamageMultiplier);
        setFacingOffsetDegrees(yawOffsetDegrees, pitchOffsetDegrees);
        updateWithCaster();
        calculateEndPos();
        if (!level.isClientSide) {
            setCasterID(caster.getId());
        }
    }

    public void setFacingOffsetDegrees(float yawOffsetDegrees, float pitchOffsetDegrees) {
        setFacingOffsetRadians(yawOffsetDegrees * Mth.DEG_TO_RAD, pitchOffsetDegrees * Mth.DEG_TO_RAD);
    }

    public void setFacingOffsetRadians(float yawOffsetRadians, float pitchOffsetRadians) {
        entityData.set(YAW_OFFSET, yawOffsetRadians);
        entityData.set(PITCH_OFFSET, pitchOffsetRadians);
    }

    public float getYawOffsetDegrees() {
        return entityData.get(YAW_OFFSET) * Mth.RAD_TO_DEG;
    }

    public float getPitchOffsetDegrees() {
        return entityData.get(PITCH_OFFSET) * Mth.RAD_TO_DEG;
    }

    public void setRelativePositionOffset(float forward, float side, float vertical) {
        entityData.set(FORWARD_OFFSET, forward);
        entityData.set(SIDE_OFFSET, side);
        entityData.set(VERTICAL_OFFSET, vertical);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(YAW_OFFSET, 0.0F);
        entityData.define(PITCH_OFFSET, 0.0F);
        entityData.define(FORWARD_OFFSET, 0.0F);
        entityData.define(SIDE_OFFSET, 0.0F);
        entityData.define(VERTICAL_OFFSET, 0.0F);
    }

    @Override
    public void tick() {
        baseTick();
        if (getDuration() > 0 && tickCount >= getDuration()) {
            discard();
            return;
        }

        if (caster == null && level().isClientSide) {
            Entity entity = level().getEntity(getCasterID());
            if (entity instanceof LivingEntity livingEntity) {
                caster = livingEntity;
            }
        }
        if (caster == null && level().isClientSide) {
            return;
        }
        if (caster == null || !caster.isAlive()) {
            discard();
            return;
        }

        updateWithCaster();
        calculateEndPos();

        if (on && tickCount > 20) {
            appear.increaseTimer();
        }
        else {
            appear.decreaseTimer();
        }
        if (!on && appear.getTimer() == 0) {
            discard();
            return;
        }

        prevCollidePosX = collidePosX;
        prevCollidePosY = collidePosY;
        prevCollidePosZ = collidePosZ;
        prevYaw = renderYaw;
        prevPitch = renderPitch;
        xo = getX();
        yo = getY();
        zo = getZ();
        renderYaw = getYaw();
        renderPitch = getPitch();

        if (tickCount <= 20) {
            return;
        }

        Vec3 start = position();
        Vec3 end = new Vec3(endPosX, endPosY, endPosZ);
        spawnSpiralParticles(start, end);

        List<LivingEntity> hitEntities = raytraceLivingEntities(start, end);
        if (blockSide != null) {
            spawnImpactParticles();
        }
        if (!level().isClientSide) {
            for (LivingEntity target : hitEntities) {
                if (target != caster && !caster.isAlliedTo(target)) {
                    boolean damaged = target.hurt(ModDamageTypes.causeEnergyBeamDamage(this, caster),
                            getDamage() + target.getMaxHealth() * getHpDamage());
                    if (damaged && getFire()) {
                        target.setSecondsOnFire(2);
                    }
                }
            }
        }
    }

    private void updateWithCaster() {
        float bodyYawRadians = caster.yBodyRot * Mth.DEG_TO_RAD;
        double spawnX = caster.getX() - Mth.sin(bodyYawRadians) + 0.5D;
        double spawnZ = caster.getZ() + Mth.cos(bodyYawRadians) - 0.5D * Mth.sin(bodyYawRadians);
        float headYawRadians = caster.yHeadRot * Mth.DEG_TO_RAD;
        float forward = entityData.get(FORWARD_OFFSET);
        float side = entityData.get(SIDE_OFFSET);
        spawnX += -Mth.sin(headYawRadians) * forward + Mth.cos(headYawRadians) * side;
        spawnZ += Mth.cos(headYawRadians) * forward + Mth.sin(headYawRadians) * side;
        setYaw((caster.yHeadRot + 90.0F) * Mth.DEG_TO_RAD + entityData.get(YAW_OFFSET));
        setPitch(-caster.getXRot() * Mth.DEG_TO_RAD + entityData.get(PITCH_OFFSET));
        setPos(spawnX, caster.getY() + 1.0D + entityData.get(VERTICAL_OFFSET), spawnZ);
    }

    private void calculateEndPos() {
        endPosX = getX() + RADIUS * Math.cos(getYaw()) * Math.cos(getPitch());
        endPosZ = getZ() + RADIUS * Math.sin(getYaw()) * Math.cos(getPitch());
        endPosY = getY() + RADIUS * Math.sin(getPitch());
    }

    private List<LivingEntity> raytraceLivingEntities(Vec3 start, Vec3 end) {
        BlockHitResult blockHit = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 collision = end;
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            collision = blockHit.getLocation();
            collidePosX = collision.x;
            collidePosY = collision.y;
            collidePosZ = collision.z;
            blockSide = blockHit.getDirection();
        }
        else {
            collidePosX = end.x;
            collidePosY = end.y;
            collidePosZ = end.z;
            blockSide = null;
        }

        AABB searchArea = new AABB(
                Math.min(start.x, collision.x), Math.min(start.y, collision.y), Math.min(start.z, collision.z),
                Math.max(start.x, collision.x), Math.max(start.y, collision.y), Math.max(start.z, collision.z)
        ).inflate(1.0D);
        List<LivingEntity> hitEntities = new ArrayList<>();
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, searchArea)) {
            if (entity == caster) {
                continue;
            }

            float padding = entity.getPickRadius() + 0.5F;
            AABB hitBox = entity.getBoundingBox().inflate(padding);
            Optional<Vec3> hit = hitBox.clip(start, collision);
            if (hitBox.contains(start) || hit.isPresent()) {
                hitEntities.add(entity);
            }
        }
        return hitEntities;
    }

    private void spawnSpiralParticles(Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        for (double travelled = 0; travelled < distance; travelled += 0.2D) {
            Vec3 center = start.add(direction.scale(travelled));
            double angle = travelled / distance * Mth.TWO_PI * 20.0D;
            level().addParticle(ModParticles.BEAM.get(), center.x + 0.7D * Math.cos(angle),
                    center.y + 0.7D * Math.sin(angle), center.z, 0, 0, 0);
        }
    }

    private void spawnImpactParticles() {
        for (int i = 0; i < 5; i++) {
            float angle = random.nextFloat() * Mth.TWO_PI;
            level().addParticle(ModParticles.BEAM.get(), collidePosX, collidePosY + 0.1D, collidePosZ,
                    1.5F * Mth.cos(angle), random.nextFloat() * 0.8F, 1.5F * Mth.sin(angle));
        }
    }
}
