package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Portal_Abyss_Blast_Entity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** A Leviathan portal blast whose ray follows its caster's current facing. */
public class MutantPortalAbyssBlastEntity extends Portal_Abyss_Blast_Entity {
    private static final int FULL_APPEAR_TIMER = 3;

    public MutantPortalAbyssBlastEntity(Level level, LivingEntity caster, double x, double y, double z,
                                        int duration, float damage, float hpDamage) {
        super(com.github.L_Ender.cataclysm.init.ModEntities.PORTAL_ABYSS_BLAST.get(), level, caster, x, y, z,
                yawRadians(caster), pitchRadians(caster), duration, 90.0F, damage, hpDamage);
    }

    public boolean belongsTo(LivingEntity entity) {
        return caster == entity;
    }

    @Override
    public void tick() {
        resolveCaster();
        if (caster != null) {
            setYaw(yawRadians(caster));
            setPitch(pitchRadians(caster));
        }
        super.tick();
        appear.setTimer(FULL_APPEAR_TIMER);
        if (tickCount > getDuration() + 20) {
            discard();
        }
    }

    private void resolveCaster() {
        if (caster != null) {
            return;
        }
        Entity entity = level().getEntity(getCasterID());
        if (entity instanceof LivingEntity livingEntity) {
            caster = livingEntity;
        }
    }

    private static float yawRadians(LivingEntity caster) {
        return (float) Math.toRadians(caster.getYHeadRot() + 90.0F);
    }

    private static float pitchRadians(LivingEntity caster) {
        return (float) Math.toRadians(-caster.getXRot());
    }
}
