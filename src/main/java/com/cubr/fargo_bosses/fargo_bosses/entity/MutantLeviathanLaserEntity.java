package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Portal_Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class MutantLeviathanLaserEntity extends Abyss_Portal_Entity {
    private static final EntityDataAccessor<Integer> CASTER_ID = SynchedEntityData.defineId(MutantLeviathanLaserEntity.class,
            EntityDataSerializers.INT);

    @Nullable
    private LivingEntity caster;
    @Nullable
    private UUID casterUuid;
    @Nullable
    private MutantPortalAbyssBlastEntity beam;
    private boolean beamSpawned;
    private float beamDamage = 33.0F;
    private float beamHpDamage;
    private int beamDuration = -1;

    public MutantLeviathanLaserEntity(EntityType<? extends MutantLeviathanLaserEntity> type, Level level) {
        super(type, level);
    }

    public MutantLeviathanLaserEntity(EntityType<? extends MutantLeviathanLaserEntity> type, Level level, LivingEntity caster) {
        this(type, level);
        setCaster(caster);
    }

    public void setCaster(@Nullable LivingEntity caster) {
        this.caster = caster;
        casterUuid = caster == null ? null : caster.getUUID();
        entityData.set(CASTER_ID, caster == null ? -1 : caster.getId());
    }

    @Nullable
    public LivingEntity getCaster() {
        return caster;
    }

    public void setBeamDamage(float damage, float hpDamage) {
        beamDamage = damage;
        beamHpDamage = hpDamage;
    }

    public void setBeamDuration(int duration) {
        beamDuration = Math.max(1, duration);
    }

    public boolean isLaserActive() {
        return !isRemoved() || (beam != null && !beam.isRemoved());
    }

    public void discardWithBeam() {
        if (beam != null) {
            beam.discard();
        }
        discard();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CASTER_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();
        if (isRemoved()) {
            return;
        }
        resolveCaster();
        if (caster == null || !caster.isAlive()) {
            discard();
            return;
        }

        setYRot(caster.getYHeadRot());
        setXRot(caster.getXRot());
        yRotO = getYRot();
        xRotO = getXRot();
        if (!level().isClientSide && !beamSpawned) {
            int duration = beamDuration >= 0 ? beamDuration : Math.max(1, getLifespan() - 20);
            beam = new MutantPortalAbyssBlastEntity(level(), caster, getX(), getY(), getZ(),
                    duration, beamDamage, beamHpDamage);
            if (!level().addFreshEntity(beam)) {
                beam.discard();
            }
            beamSpawned = true;
        }
    }

    private void resolveCaster() {
        if (caster != null) {
            return;
        }

        Entity entity = level().getEntity(entityData.get(CASTER_ID));
        if (entity instanceof LivingEntity livingEntity) {
            caster = livingEntity;
            casterUuid = livingEntity.getUUID();
        }
        else if (!level().isClientSide && casterUuid != null && level() instanceof ServerLevel serverLevel) {
            Entity savedCaster = serverLevel.getEntity(casterUuid);
            if (savedCaster instanceof LivingEntity livingEntity) {
                setCaster(livingEntity);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        casterUuid = compound.hasUUID("CasterUUID") ? compound.getUUID("CasterUUID") : null;
        beamSpawned = compound.getBoolean("BeamSpawned");
        if (compound.contains("BeamDamage")) {
            beamDamage = compound.getFloat("BeamDamage");
        }
        if (compound.contains("BeamHpDamage")) {
            beamHpDamage = compound.getFloat("BeamHpDamage");
        }
        if (compound.contains("BeamDuration")) {
            beamDuration = compound.getInt("BeamDuration");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (casterUuid != null) {
            compound.putUUID("CasterUUID", casterUuid);
        }
        compound.putBoolean("BeamSpawned", beamSpawned);
        compound.putFloat("BeamDamage", beamDamage);
        compound.putFloat("BeamHpDamage", beamHpDamage);
        compound.putInt("BeamDuration", beamDuration);
    }
}
