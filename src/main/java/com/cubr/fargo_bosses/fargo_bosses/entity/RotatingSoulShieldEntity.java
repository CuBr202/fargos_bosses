package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.cubr.fargo_bosses.fargo_bosses.Config;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModEntities;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
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

public class RotatingSoulShieldEntity extends SoulShieldEntity {
    private static final float DEFAULT_RADIUS = 4.0F;
    private static final float DEFAULT_ORBIT_SPEED = 0.12F;

    private float orbitAngle;
    private float orbitRadius = DEFAULT_RADIUS;
    private float orbitSpeed = DEFAULT_ORBIT_SPEED;
    private double yOffset;
    private boolean orbitAroundPosition;
    private double orbitCenterX;
    private double orbitCenterY;
    private double orbitCenterZ;
    private int manualLifetime = -1;

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

    public void faceRadiusFrom(double centerX, double centerY, double centerZ) {
        Vec3 radius = new Vec3(getX() - centerX, getY() - centerY, getZ() - centerZ);
        if (radius.lengthSqr() > 1.0E-4) {
            setYRot((float) Math.toDegrees(Math.atan2(radius.z, radius.x)) - 90.0F);
            setXRot(-(float) Math.toDegrees(Math.atan2(radius.y, Math.sqrt(radius.x * radius.x + radius.z * radius.z))));
        }
    }

    @Override
    public void tick() {
        baseTick();
        if (getAnimationState() > 0) {
            AnimationTicks++;
        }

        LivingEntity caster = getCaster();
        if (!level().isClientSide && caster != null && !caster.isAlive()) {
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
