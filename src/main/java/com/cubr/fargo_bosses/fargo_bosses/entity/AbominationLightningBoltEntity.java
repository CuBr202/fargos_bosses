package com.cubr.fargo_bosses.fargo_bosses.entity;

import net.miauczel.legendary_monsters.entity.AnimatedMonster.Projectile.LightningBoltEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class AbominationLightningBoltEntity extends LightningBoltEntity {
    private static final double HIT_RADIUS = 1.0D;

    public AbominationLightningBoltEntity(EntityType<? extends LightningBoltEntity> type, Level level) {
        super(type, level);
    }

    public AbominationLightningBoltEntity(Level level, double x, double y, double z, float yaw, int warmup,
                                          AbominationEntity caster, int lifetime, float damage) {
        super(level, x, y, z, yaw, warmup, caster, lifetime, damage);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || tickCount < 6 || tickCount > 29 || tickCount % 5 != 0) return;

        LivingEntity caster = getCaster();
        if (!(caster instanceof AbominationEntity)) return;

        DamageSource source = new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.INDIRECT_MAGIC), caster);
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(HIT_RADIUS, 6.0D, HIT_RADIUS))) {
            if (target != caster && target.isAlive() && !caster.isAlliedTo(target)) {
                target.hurt(source, getDamage());
            }
        }
    }
}
