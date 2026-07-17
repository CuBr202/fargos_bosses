package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.github.L_Ender.cataclysm.entity.projectile.Laser_Beam_Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MutantLaserBeamEntity extends Laser_Beam_Entity {
    private static final int BLOCK_HIT_PARTICLE_COUNT = 4;

    public MutantLaserBeamEntity(EntityType<? extends MutantLaserBeamEntity> type, Level level) {
        super(type, level);
    }

    public MutantLaserBeamEntity(EntityType<? extends MutantLaserBeamEntity> type, LivingEntity owner,
                                 double x, double y, double z, Vec3 direction, float damage, Level level) {
        super(type, owner, x, y, z, direction, damage, level);
    }

    public void setInitialSpeed(double initialSpeed) {
        Vec3 direction = getDeltaMovement();
        if (direction.lengthSqr() > 1.0E-4D) {
            setDeltaMovement(direction.normalize().scale(initialSpeed));
            hasImpulse = true;
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        /*
        if (level() instanceof ServerLevel serverLevel) {
            Vec3 impactPosition = result.getLocation();
            serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, impactPosition.x, impactPosition.y, impactPosition.z,
                    BLOCK_HIT_PARTICLE_COUNT, 0.0D, 0.0D, 0.0D, 0.0D);
        }
         */
    }
}
