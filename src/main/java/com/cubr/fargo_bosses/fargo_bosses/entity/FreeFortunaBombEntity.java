package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.cubr.fargo_bosses.fargo_bosses.registry.ModEntities;
import lykrast.meetyourfight.entity.FortunaBombEntity;
import lykrast.meetyourfight.entity.ProjectileLineEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class FreeFortunaBombEntity extends FortunaBombEntity {
    private static final double GRAVITY = -0.08;
    private static final double HALF_SQRT2 = Math.sqrt(2) / 2;

    @Nullable
    private LivingEntity bomber;
    @Nullable
    private UUID bomberId;
    private int moveFuse;
    private final List<Vec3> explosionDirections = new ArrayList<>();

    public FreeFortunaBombEntity(EntityType<? extends FreeFortunaBombEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FreeFortunaBombEntity(Level level, double x, double y, double z, @Nullable LivingEntity igniter) {
        this(ModEntities.FREE_FORTUNA_BOMB.get(), level);
        setPos(x, y, z);
        xo = x;
        yo = y;
        zo = z;
        bomber = igniter;
        if (igniter != null) {
            bomberId = igniter.getUUID();
        }
    }

    @Override
    public void setup(int fuse, int moveFuse, double tx, double ty, double tz) {
        setFuse(fuse);
        this.moveFuse = moveFuse;

        if (moveFuse <= 0) {
            setDeltaMovement(0, 0, 0);
        }
        else {
            double compensation = (-(moveFuse + 1) / 2.0) * GRAVITY;
            double scale = 1.0 / moveFuse;
            setDeltaMovement((tx - getX()) * scale, (ty - getY()) * scale + compensation, (tz - getZ()) * scale);
        }
    }

    public void clearExplosionDirections() {
        explosionDirections.clear();
    }

    public void addExplosionDirection(double dx, double dy, double dz) {
        addExplosionDirection(new Vec3(dx, dy, dz));
    }

    public void addExplosionDirection(Vec3 direction) {
        if (direction.lengthSqr() > 1.0E-7) {
            explosionDirections.add(direction.normalize());
        }
    }

    public void setExplosionDirections(Collection<Vec3> directions) {
        clearExplosionDirections();
        for (Vec3 direction : directions) {
            addExplosionDirection(direction);
        }
    }
    public void setExplosionDirectionsUniformly(int directions) {
        if (directions <= 0) return;
        clearExplosionDirections();
        for(int i=0; i<directions; i++){
            double partDirection = i/(double)directions;
            addExplosionDirection(Math.cos(partDirection*Math.PI*2),0,Math.sin(partDirection*Math.PI*2));
        }
    }



    @Override
    public void tick() {
        if (moveFuse > 0) {
            setDeltaMovement(getDeltaMovement().add(0, GRAVITY, 0));
        }

        move(MoverType.SELF, getDeltaMovement());
        if (moveFuse > 0) {
            moveFuse--;
            if (moveFuse == 0) {
                setDeltaMovement(0, 0, 0);
            }
        }

        int remaining = getFuse() - 1;
        setFuse(remaining);
        if (remaining <= 0) {
            remove(RemovalReason.KILLED);
            playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 0.5F);
            if (!level().isClientSide) {
                explodeFreely();
            }
        }
        else {
            updateInWaterStateAndDoFluidPushing();
            if (level().isClientSide) {
                level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.5D, getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private void explodeFreely() {
        LivingEntity owner = getBomber();
        if (owner == null) {
            return;
        }

        for (Vec3 direction : explosionDirections) {
            projectile(owner, direction);
        }
    }

    @Nullable
    private LivingEntity getBomber() {
        if (bomber == null && bomberId != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(bomberId);
            if (entity instanceof LivingEntity livingEntity) {
                bomber = livingEntity;
            }
        }
        return bomber;
    }

    private void projectile(LivingEntity owner, Vec3 direction) {
        ProjectileLineEntity proj = new ProjectileLineEntity(level(), owner);
        proj.setOwner(owner);
        proj.setPos(position());
        proj.setVariant(ProjectileLineEntity.VAR_DAME_FORTUNA);
        proj.setUp(1, direction.x, direction.y, direction.z, getX() + direction.x * 0.1, getY() + direction.y * 0.1, getZ() + direction.z * 0.1);
        level().addFreshEntity(proj);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putShort("Fuse", (short) getFuse());
        compound.putShort("MFuse", (short) moveFuse);
        if (bomber != null) {
            compound.putUUID("Bomber", bomber.getUUID());
        }
        else if (bomberId != null) {
            compound.putUUID("Bomber", bomberId);
        }

        ListTag directions = new ListTag();
        for (Vec3 direction : explosionDirections) {
            CompoundTag directionTag = new CompoundTag();
            directionTag.putDouble("X", direction.x);
            directionTag.putDouble("Y", direction.y);
            directionTag.putDouble("Z", direction.z);
            directions.add(directionTag);
        }
        compound.put("Directions", directions);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        setFuse(compound.getShort("Fuse"));
        moveFuse = compound.getShort("MFuse");
        if (compound.hasUUID("Bomber")) {
            bomberId = compound.getUUID("Bomber");
        }

        explosionDirections.clear();
        ListTag directions = compound.getList("Directions", Tag.TAG_COMPOUND);
        for (int i = 0; i < directions.size(); i++) {
            CompoundTag directionTag = directions.getCompound(i);
            addExplosionDirection(directionTag.getDouble("X"), directionTag.getDouble("Y"), directionTag.getDouble("Z"));
        }
    }
}
