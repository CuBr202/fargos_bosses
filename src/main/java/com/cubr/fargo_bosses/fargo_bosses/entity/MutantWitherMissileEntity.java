package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.github.L_Ender.cataclysm.entity.projectile.Wither_Homing_Missile_Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Preserves Mutant's side-lane launch briefly before Cataclysm's homing takes over. */
public class MutantWitherMissileEntity extends Wither_Homing_Missile_Entity {
    private final Vec3 launchDirection;
    private final double launchSpeed;
    private final int launchDuration;
    private int launchTicks;

    public MutantWitherMissileEntity(LivingEntity owner, Vec3 direction, Level level, float damage,
                                    LivingEntity target, double launchSpeed, int launchDuration) {
        super(owner, direction, level, damage, target);
        launchDirection = direction.normalize();
        this.launchSpeed = launchSpeed;
        this.launchDuration = launchDuration;
        setDeltaMovement(launchDirection.scale(launchSpeed));
    }

    @Override
    public void tick() {
        super.tick();
        if (!isRemoved() && launchTicks++ < launchDuration) {
            setDeltaMovement(launchDirection.scale(launchSpeed));
        }
    }
}
