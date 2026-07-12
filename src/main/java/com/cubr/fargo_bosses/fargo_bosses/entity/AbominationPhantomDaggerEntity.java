package com.cubr.fargo_bosses.fargo_bosses.entity;

import net.miauczel.legendary_monsters.damagetype.ModDamageTypes;
import net.miauczel.legendary_monsters.effect.ModEffects;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.CollapsedKingdom.Fractured.FracturedApostleEntity;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.Projectile.ThrownPhantomDaggerEntity;
import net.miauczel.legendary_monsters.tag.ModEntityTags;
import net.miauczel.legendary_monsters.util.EntityUtil;
import net.miauczel.legendary_monsters.util.MathUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class AbominationPhantomDaggerEntity extends ThrownPhantomDaggerEntity {
    public AbominationPhantomDaggerEntity(EntityType<? extends ThrownPhantomDaggerEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner = getOwner();

        if (target instanceof TamableAnimal animal && animal.getOwner() == owner) return;
        if (owner != null && target == owner && !(target instanceof TamableAnimal animal && animal.getOwner() == owner)) return;

        if (owner instanceof LivingEntity livingOwner && target instanceof LivingEntity livingTarget) {
            if (target.getType().is(ModEntityTags.POSSESSED_ARMOR_TEAM) && owner.getType().is(ModEntityTags.POSSESSED_ARMOR_TEAM)) {
                return;
            }

            float healthDamage = owner instanceof Player ? 0.0F : MathUtils.toPercent(livingTarget.getMaxHealth());
            boolean hurt = target.hurt(ModDamageTypes.causeGhostlyDamage(livingOwner, livingOwner), getDamage() + healthDamage);
            if (hurt) {
                if (!(owner instanceof AbominationEntity abomination) || abomination.isSoulFractureEnabled()
                        && !(owner instanceof Player || owner instanceof FracturedApostleEntity)) {
                    EntityUtil.applyStackingEffect(livingTarget, ModEffects.SOUL_FRACTURE.get(), 1, 4, MathUtils.toTicks(10));
                }
                livingOwner.heal(3);
            }
        }
    }
}
