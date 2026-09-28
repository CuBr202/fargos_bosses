package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.cubr.fargo_bosses.fargo_bosses.Config;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModEntities;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.miauczel.legendary_monsters.Particle.ModParticles;
import net.miauczel.legendary_monsters.damagetype.ModDamageTypes;
import net.miauczel.legendary_monsters.effect.ModEffects;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.AnimatedEntity.SoulShieldEntity;
import net.miauczel.legendary_monsters.util.EntityUtil;
import net.miauczel.legendary_monsters.util.MathUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RotatingSoulShieldEntity extends SoulShieldEntity {
    private static final float DEFAULT_RADIUS = 4.0F;
    private static final float DEFAULT_ORBIT_SPEED = 0.12F;
    private static final int RESTRICTION_RING_CHECK_INTERVAL = 200;
    private static final double RESTRICTION_RING_SPACING_TOLERANCE = 0.5D;
    private static final double RESTRICTION_RING_HEIGHT_TOLERANCE = 2.0D;
    private static final String RESTRICTION_RING_CENTER_TAG = "FargoRestrictionRingCenter";
    private static final String RESTRICTION_RING_GENERATION_TAG = "FargoRestrictionRingGeneration";

    private float orbitAngle;
    private float orbitRadius = DEFAULT_RADIUS;
    private float orbitSpeed = DEFAULT_ORBIT_SPEED;
    private double yOffset;
    private boolean orbitAroundPosition;
    private double orbitCenterX;
    private double orbitCenterY;
    private double orbitCenterZ;
    private int manualLifetime = -1;
    private int lerpSteps;
    private Vec3 lerpPosition = Vec3.ZERO;
    private float lerpYaw;
    private float lerpPitch;

    public RotatingSoulShieldEntity(EntityType<? extends SoulShieldEntity> type, Level level) {
        super(type, level);
        setIsOuter(false);
        setMaxUpStep(1.5F);
        noPhysics = true;
    }

    public RotatingSoulShieldEntity(Level level, LivingEntity caster, double yOffset, float orbitRadius, float orbitAngle, float orbitSpeed, float damage, boolean isRed) {
        this(ModEntities.ROTATING_SOUL_SHIELD.get(), level);
        setCaster(caster);
        setDamage(damage);
        setRed(isRed);
        setOrbitAroundCaster(orbitRadius, orbitAngle, orbitSpeed, yOffset);
        if (caster != null) {
            setPos(caster.getX() + Math.cos(orbitAngle) * orbitRadius, caster.getY(), caster.getZ() + Math.sin(orbitAngle) * orbitRadius);
        }
    }

    public void setOrbit(float radius, float angle, float speed, double yOffset) {
        setOrbitAroundCaster(radius, angle, speed, yOffset);
    }

    public void setOrbitAroundCaster(float radius, float angle, float speed, double yOffset) {
        orbitAroundPosition = false;
        orbitRadius = radius;
        orbitAngle = angle;
        orbitSpeed = speed;
        this.yOffset = yOffset;
        updateOrbitDestination();
    }

    public void setOrbitAroundPosition(double x, double y, double z, float radius, float angle, float speed, double yOffset) {
        orbitAroundPosition = true;
        orbitCenterX = x;
        orbitCenterY = y;
        orbitCenterZ = z;
        orbitRadius = radius;
        orbitAngle = angle;
        orbitSpeed = speed;
        this.yOffset = yOffset;
        updatePositionOrbitDestination();
    }

    public void switchToAroundPosition(double x, double y, double z) {
        orbitAroundPosition = true;
        orbitCenterX = x;
        orbitCenterY = y;
        orbitCenterZ = z;
        updatePositionOrbitDestination();
    }

    public void switchToAroundCaster() {
        orbitAroundPosition = false;
        updateOrbitDestination();
    }

    public void setManualLifetime(int ticks) {
        manualLifetime = ticks;
    }

    public static void setRestrictionRingCenter(LivingEntity caster, @Nullable Vec3 center) {
        if (caster.level().isClientSide) return;

        // Keep the anchor on the boss so repairs also work when shields are missing.
        CompoundTag anchor = new CompoundTag();
        anchor.putBoolean("Detached", center != null);
        if (center != null) {
            anchor.putDouble("X", center.x);
            anchor.putDouble("Y", center.y);
            anchor.putDouble("Z", center.z);
        }
        caster.getPersistentData().put(RESTRICTION_RING_CENTER_TAG, anchor);
    }

    public static void repairRestrictionRing(LivingEntity caster, int shieldCount, float radius,
                                             float speed, float damage) {
        if (!(caster.level() instanceof ServerLevel serverLevel) || !caster.isAlive()
                || caster.tickCount <= 0 || caster.tickCount % RESTRICTION_RING_CHECK_INTERVAL != 0
                || shieldCount <= 0 || radius <= 0.0F) {
            return;
        }

        // Include displaced shields outside the usual arena search box, but never another boss's ring.
        List<RotatingSoulShieldEntity> shields = new ArrayList<>();
        for (Entity entity : serverLevel.getAllEntities()) {
            if (entity instanceof RotatingSoulShieldEntity shield && !shield.isRemoved()
                    && shield.manualLifetime < 0 && shield.getCaster() == caster) {
                shields.add(shield);
            }
        }

        if (!caster.getPersistentData().contains(RESTRICTION_RING_CENTER_TAG)) {
            // Existing saves predate the boss-side anchor; recover it from a surviving shield.
            RotatingSoulShieldEntity previous = shields.isEmpty() ? null : shields.get(0);
            setRestrictionRingCenter(caster, previous != null && previous.orbitAroundPosition
                    ? new Vec3(previous.orbitCenterX, previous.orbitCenterY, previous.orbitCenterZ) : null);
        }
        CompoundTag anchor = caster.getPersistentData().getCompound(RESTRICTION_RING_CENTER_TAG);
        boolean detached = anchor.getBoolean("Detached");
        Vec3 center = detached ? new Vec3(anchor.getDouble("X"), anchor.getDouble("Y"), anchor.getDouble("Z"))
                : caster.position();

        boolean correctOrbit = true;
        List<Vec3> positions = new ArrayList<>();
        for (RotatingSoulShieldEntity shield : shields) {
            positions.add(shield.position());
            if (shield.orbitAroundPosition != detached || Math.abs(shield.orbitRadius - radius) > 0.01F
                    || (detached && new Vec3(shield.orbitCenterX, shield.orbitCenterY, shield.orbitCenterZ)
                    .distanceToSqr(center) > 0.01D)) {
                correctOrbit = false;
            }
        }
        if (correctOrbit && isRingEvenlyDistributed(positions, center, shieldCount, radius)) {
            return;
        }

        double startAngle = shields.isEmpty() ? 0.0D
                : Math.atan2(shields.get(0).getZ() - center.z, shields.get(0).getX() - center.x);
        if (!Double.isFinite(startAngle)) startAngle = 0.0D;
        boolean red = shields.isEmpty() || shields.get(0).getRed();
        long generation = caster.getPersistentData().getLong(RESTRICTION_RING_GENERATION_TAG) + 1;
        List<RotatingSoulShieldEntity> replacements = new ArrayList<>();
        for (int i = 0; i < shieldCount; i++) {
            float angle = (float) (startAngle + i * Math.PI * 2.0D / shieldCount);
            RotatingSoulShieldEntity shield = new RotatingSoulShieldEntity(serverLevel, caster, 0.0D,
                    radius, angle, speed, damage, red);
            shield.setManualLifetime(-1);
            shield.getPersistentData().putLong(RESTRICTION_RING_GENERATION_TAG, generation);
            if (detached) {
                shield.setOrbitAroundPosition(center.x, center.y, center.z, radius, angle, speed, 0.0D);
            }
            shield.setPos(center.x + Math.cos(angle) * radius, center.y, center.z + Math.sin(angle) * radius);
            shield.faceRadiusFrom(center.x, center.y, center.z);
            if (!serverLevel.addFreshEntity(shield)) {
                shield.discard();
                replacements.forEach(Entity::discard);
                return;
            }
            replacements.add(shield);
        }
        caster.getPersistentData().putLong(RESTRICTION_RING_GENERATION_TAG, generation);
        shields.forEach(Entity::discard);
    }

    private boolean isObsoleteRestrictionShield(LivingEntity caster) {
        return manualLifetime < 0 && caster.getPersistentData().contains(RESTRICTION_RING_CENTER_TAG)
                && getPersistentData().getLong(RESTRICTION_RING_GENERATION_TAG)
                < caster.getPersistentData().getLong(RESTRICTION_RING_GENERATION_TAG);
    }

    private static boolean isRingEvenlyDistributed(List<Vec3> positions, Vec3 center, int shieldCount, float radius) {
        if (positions.size() != shieldCount || shieldCount <= 0) return false;

        double[] angles = new double[shieldCount];
        double minimumRadius = Double.POSITIVE_INFINITY;
        double maximumRadius = 0.0D;
        for (int i = 0; i < shieldCount; i++) {
            Vec3 position = positions.get(i);
            double dx = position.x - center.x;
            double dz = position.z - center.z;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (!Double.isFinite(distance) || !Double.isFinite(position.y) || distance < 0.1D
                    || Math.abs(position.y - center.y) > RESTRICTION_RING_HEIGHT_TOLERANCE) {
                return false;
            }
            minimumRadius = Math.min(minimumRadius, distance);
            maximumRadius = Math.max(maximumRadius, distance);
            angles[i] = Math.atan2(dz, dx);
        }
        // Pursuit movement can shrink a healthy ring uniformly; check distortion, not exact radius.
        if (maximumRadius - minimumRadius > Math.max(1.0D, radius * 0.1D)) return false;
        if (minimumRadius < radius * 0.5D || maximumRadius > radius * 1.5D) return false;

        Arrays.sort(angles);
        double expectedGap = Math.PI * 2.0D / shieldCount;
        for (int i = 0; i < shieldCount; i++) {
            double nextAngle = i + 1 < shieldCount ? angles[i + 1] : angles[0] + Math.PI * 2.0D;
            double gap = nextAngle - angles[i];
            if (Math.abs(gap - expectedGap) > expectedGap * RESTRICTION_RING_SPACING_TOLERANCE) return false;
        }
        return true;
    }

    public void faceRadiusFrom(double centerX, double centerY, double centerZ) {
        Vec3 radius = new Vec3(getX() - centerX, getY() - centerY, getZ() - centerZ);
        if (radius.lengthSqr() > 1.0E-4) {
            setYRot((float) Math.toDegrees(Math.atan2(radius.z, radius.x)) - 90.0F);
            setXRot(-(float) Math.toDegrees(Math.atan2(radius.y, Math.sqrt(radius.x * radius.x + radius.z * radius.z))));
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        if (!level().isClientSide || teleport || steps <= 0) {
            lerpSteps = 0;
            super.lerpTo(x, y, z, yaw, pitch, steps, teleport);
            return;
        }
        lerpPosition = new Vec3(x, y, z);
        lerpYaw = yaw;
        lerpPitch = pitch;
        lerpSteps = steps;
    }

    private void interpolateClientPosition() {
        if (lerpSteps <= 0) {
            return;
        }
        setPos(position().lerp(lerpPosition, 1.0D / lerpSteps));
        setYRot(getYRot() + Mth.wrapDegrees(lerpYaw - getYRot()) / lerpSteps);
        setXRot(getXRot() + (lerpPitch - getXRot()) / lerpSteps);
        lerpSteps--;
    }

    @Override
    public void tick() {
        baseTick();
        if (getAnimationState() > 0) {
            AnimationTicks++;
        }

        if (level().isClientSide) {
            // Follow server positions only; independently chasing destinations fights network updates.
            interpolateClientPosition();
            spawnConfiguredParticles();
            RendercontrolledAnim2.increaseTimer();
            prevactivateProgress = activateProgress;
            return;
        }

        LivingEntity caster = getCaster();
        if (caster != null && (!caster.isAlive() || isObsoleteRestrictionShield(caster))) {
            discard();
            return;
        }

        if (orbitAroundPosition) {
            updatePositionOrbitDestination();
        }
        else {
            updateOrbitDestination();
        }

        spawnConfiguredParticles();
        if (getIsOuter()) {
            updateOuterDestination();
        }
        for (LivingEntity livingEntity : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.25F))) {
            damage(livingEntity);
        }

        Vec3 toDest = new Vec3(getDestinationX() - getX(), getDestinationY() - getY(), getDestinationZ() - getZ());
        double distance = toDest.length();
        double speed = 0.1 + distance * 0.05F;
        if (distance > speed) {
            move(MoverType.SELF, toDest.normalize().scale(speed));
        }
        RendercontrolledAnim2.increaseTimer();

        lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(getDestinationX(), getDestinationY(), getDestinationZ()));
        prevactivateProgress = activateProgress;

        if (!level().isClientSide) {
            if (manualLifetime == 0) {
                discard();
            }
            else if (manualLifetime > 0) {
                manualLifetime--;
            }
        }
    }

    private void spawnConfiguredParticles() {
        if (!level().isClientSide || Config.rotatingSoulShieldParticlesPerBurst <= 0) {
            return;
        }
        if (tickCount % Math.max(1, Config.rotatingSoulShieldParticleInterval) != 0) {
            return;
        }

        level().addParticle(getRed() ? ModParticles.GROUNDSOUL_RED.get() : ModParticles.GROUNDSOUL.get(), getX(), getY() + 2, getZ(), 0, 0, 0);
        for (int i = 0; i < Config.rotatingSoulShieldParticlesPerBurst; i++) {
            double d0 = getX() + (random.nextDouble() * 2.0D - 1.0D) * getBbWidth() * 0.5D;
            double d1 = getY() + 0.03D;
            double d2 = getZ() + (random.nextDouble() * 2.0D - 1.0D) * getBbWidth() * 0.5D;
            double d3 = random.nextGaussian() * 0.07D;
            double d4 = random.nextGaussian() * 0.07D;
            double d5 = random.nextGaussian() * 0.07D;
            level().addParticle(getRed() ? ModParticles.GHOSTLY_SOUL_RED.get() : ModParticles.GHOSTLY_SOUL.get(), d0, d1, d2, d3, d4, d5);
        }
    }

    private void updateOuterDestination() {
        float f = (float) Math.cos(getYRot() * (Math.PI / 180F));
        float f1 = (float) Math.sin(getYRot() * (Math.PI / 180F));
        double theta = getYRot() * (Math.PI / 180);
        theta += Math.PI / 2;
        double vecX = Math.cos(theta);
        double vecZ = Math.sin(theta);
        float vec = 8.0F;
        float offset = 0.0F;
        setDestinationX((float) (getX() + vec * vecX + f * offset));
        setDestinationY((float) getY());
        setDestinationZ((float) (getZ() + vec * vecZ + f1 * offset));
    }

    private void damage(LivingEntity impactEntity) {
        LivingEntity livingentity = getCaster();
        if (impactEntity.isAlive() && !impactEntity.isInvulnerable() && impactEntity != livingentity && tickCount % 5 == 0) {
            if (livingentity != null && livingentity.isAlliedTo(impactEntity)) {
                return;
            }

            DamageSource damageSource = new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.GHOST), this, livingentity);
            boolean hurt = impactEntity.hurt(damageSource, getDamage() + MathUtils.entityBasedHpDamage(impactEntity, 3));
            if (hurt) {
                if (!(livingentity instanceof AbominationEntity abomination) || abomination.isSoulFractureEnabled()) {
                    EntityUtil.applyStackingEffect(impactEntity, ModEffects.SOUL_FRACTURE.get(), 1, 4, MathUtils.toTicks(10));
                }
                if (livingentity != null) {
                    livingentity.heal(5);
                }
            }
        }
    }

    private void updateOrbitDestination() {
        LivingEntity caster = getCaster();
        if (caster == null || !caster.isAlive()) {
            return;
        }

        orbitAngle += orbitSpeed;
        double nextX = caster.getX() + Math.cos(orbitAngle) * orbitRadius;
        double nextY = caster.getY();
        double nextZ = caster.getZ() + Math.sin(orbitAngle) * orbitRadius;
        setDestination(nextX, nextY, nextZ, caster.getX(), nextY, caster.getZ());
    }

    private void updatePositionOrbitDestination() {
        orbitAngle += orbitSpeed;
        double nextX = orbitCenterX + Math.cos(orbitAngle) * orbitRadius;
        double nextY = orbitCenterY + yOffset;
        double nextZ = orbitCenterZ + Math.sin(orbitAngle) * orbitRadius;
        setDestination(nextX, nextY, nextZ, orbitCenterX, nextY, orbitCenterZ);
    }

    private void setDestination(double nextX, double nextY, double nextZ, double centerX, double centerY, double centerZ) {
        setDestinationX((float) nextX);
        setDestinationY((float) nextY);
        setDestinationZ((float) nextZ);

        Vec3 radius = new Vec3(nextX - centerX, nextY - centerY, nextZ - centerZ);
        if (radius.lengthSqr() > 1.0E-4) {
            setYRot((float) Math.toDegrees(Math.atan2(radius.z, radius.x)) - 90.0F);
            setXRot(-(float) Math.toDegrees(Math.atan2(radius.y, Math.sqrt(radius.x * radius.x + radius.z * radius.z))));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        orbitAngle = compound.getFloat("OrbitAngle");
        orbitRadius = compound.contains("OrbitRadius") ? compound.getFloat("OrbitRadius") : DEFAULT_RADIUS;
        orbitSpeed = compound.contains("OrbitSpeed") ? compound.getFloat("OrbitSpeed") : DEFAULT_ORBIT_SPEED;
        yOffset = compound.getDouble("YOffset");
        orbitAroundPosition = compound.getBoolean("OrbitAroundPosition");
        orbitCenterX = compound.getDouble("OrbitCenterX");
        orbitCenterY = compound.getDouble("OrbitCenterY");
        orbitCenterZ = compound.getDouble("OrbitCenterZ");
        manualLifetime = compound.contains("ManualLifetime") ? compound.getInt("ManualLifetime") : -1;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("OrbitAngle", orbitAngle);
        compound.putFloat("OrbitRadius", orbitRadius);
        compound.putFloat("OrbitSpeed", orbitSpeed);
        compound.putDouble("YOffset", yOffset);
        compound.putBoolean("OrbitAroundPosition", orbitAroundPosition);
        compound.putDouble("OrbitCenterX", orbitCenterX);
        compound.putDouble("OrbitCenterY", orbitCenterY);
        compound.putDouble("OrbitCenterZ", orbitCenterZ);
        compound.putInt("ManualLifetime", manualLifetime);
    }
}
