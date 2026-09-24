package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.github.alexmodguy.alexscaves.server.entity.item.MagneticWeaponEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * An incoming magnetic weapon that converges on Mutant and damages only entities
 * crossed by its physical hitbox.
 */
public class MutantMagneticWeaponEntity extends MagneticWeaponEntity {
    public static final float COLLISION_RADIUS = 0.5F;
    private static final double CONTACT_EPSILON = 1.0E-7D;
    private final Set<UUID> hitEntities = new HashSet<>();
    private float attackDamage;
    private double flightSpeed;
    private double targetHeight;
    private int remainingLifetime;

    public MutantMagneticWeaponEntity(EntityType<? extends MutantMagneticWeaponEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setGlowingTag(true);
    }

    public void setMutantController(MutantEntity mutant) {
        setControllerUUID(mutant.getUUID());
    }

    @Nullable
    public MutantEntity getMutantController() {
        return getController() instanceof MutantEntity mutant ? mutant : null;
    }

    public void configureFlight(double flightSpeed, float attackDamage, double targetHeight, int lifetime) {
        this.flightSpeed = Math.max(0.01D, flightSpeed);
        this.attackDamage = attackDamage;
        this.targetHeight = targetHeight;
        remainingLifetime = Math.max(1, lifetime);
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            super.tick();
            return;
        }

        MutantEntity mutant = getMutantController();
        if (mutant == null || !mutant.isAlive() || --remainingLifetime < 0) {
            discard();
            return;
        }

        AABB bossContactBox = mutant.getBoundingBox().inflate(getBbWidth() * 0.5D, getBbHeight() * 0.5D,
                getBbWidth() * 0.5D);
        if (touchesBoss(mutant)) {
            discard();
            return;
        }

        Vec3 destination = mutant.position().add(0.0D, targetHeight, 0.0D);
        Vec3 direction = destination.subtract(position());
        double distance = direction.length();
        if (distance < 1.0E-4D) {
            discard();
            return;
        }

        Vec3 velocity = direction.scale(Math.min(flightSpeed, distance) / distance);
        setDeltaMovement(velocity);
        hasImpulse = true;
        setYRot((float) -Math.toDegrees(Math.atan2(velocity.x, velocity.z)));
        setXRot((float) -Math.toDegrees(Math.atan2(velocity.y, velocity.horizontalDistance())));

        AABB previousBox = getBoundingBox();
        super.tick();
        if (isRemoved()) {
            return;
        }

        Vec3 end = getBoundingBox().getCenter();
        Optional<Vec3> bossContact = bossContactBox.clip(previousBox.getCenter(), end);
        // Damage only along the path before the weapon reaches its caster.
        hurtCrossedEntities(mutant, previousBox, bossContact.orElse(end));
        if (bossContact.isPresent() || touchesBoss(mutant)
                || distance <= flightSpeed || horizontalCollision || verticalCollision) {
            discard();
        }
    }

    private boolean touchesBoss(MutantEntity mutant) {
        AABB weaponBox = getBoundingBox();
        AABB bossBox = mutant.getBoundingBox();
        return weaponBox.maxX >= bossBox.minX - CONTACT_EPSILON && weaponBox.minX <= bossBox.maxX + CONTACT_EPSILON
                && weaponBox.maxY >= bossBox.minY - CONTACT_EPSILON && weaponBox.minY <= bossBox.maxY + CONTACT_EPSILON
                && weaponBox.maxZ >= bossBox.minZ - CONTACT_EPSILON && weaponBox.minZ <= bossBox.maxZ + CONTACT_EPSILON;
    }

    private void hurtCrossedEntities(MutantEntity mutant, AABB previousBox, Vec3 end) {
        Vec3 start = previousBox.getCenter();
        AABB searchArea = previousBox.expandTowards(end.subtract(start));
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, searchArea,
                entity -> entity.isAlive() && !entity.isSpectator() && entity != mutant
                        && !mutant.isAlliedTo(entity) && !entity.isAlliedTo(mutant)
                        && !hitEntities.contains(entity.getUUID()))) {
            // Sweep the actual box along this tick's movement, without adding an attack radius.
            AABB contactBox = entity.getBoundingBox().inflate(getBbWidth() * 0.5D, getBbHeight() * 0.5D,
                    getBbWidth() * 0.5D);
            if ((contactBox.contains(start) || contactBox.clip(start, end).isPresent())
                    && entity.hurt(mutant.damageSources().mobAttack(mutant), attackDamage)) {
                hitEntities.add(entity.getUUID());
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("FlightSpeed", flightSpeed);
        tag.putFloat("AttackDamage", attackDamage);
        tag.putDouble("TargetHeight", targetHeight);
        tag.putInt("RemainingLifetime", remainingLifetime);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        flightSpeed = Math.max(0.01D, tag.getDouble("FlightSpeed"));
        attackDamage = tag.getFloat("AttackDamage");
        targetHeight = tag.getDouble("TargetHeight");
        remainingLifetime = tag.getInt("RemainingLifetime");
    }
}
