package com.cubr.fargo_bosses.fargo_bosses.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import com.github.L_Ender.cataclysm.entity.projectile.Flare_Bomb_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Flame_Jet_Entity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A Flare Bomb that replaces Cataclysm's fire-producing impact pattern with Mutant's radial Flame Jets. */
public class MutantFlareBombEntity extends Flare_Bomb_Entity {
    public MutantFlareBombEntity(Level level, LivingEntity thrower) {
        super(com.github.L_Ender.cataclysm.init.ModEntities.FLARE_BOMB.get(), level, thrower);
    }

    @Override
    protected void onHit(HitResult result) {
        if (!level().isClientSide) {
            if (result instanceof EntityHitResult entityHitResult) {
                onHitEntity(entityHitResult);
            }
            else if (result instanceof BlockHitResult) {
                spawnRadialFlameJets();
            }
            playSound(SoundEvents.GENERIC_BURN, 1.5F, 0.75F);
            level().broadcastEntityEvent(this, (byte) 4);
            discard();
        }
    }

    private void spawnRadialFlameJets() {
        for (int directionIndex = 0; directionIndex < MutantEntity.FLARE_BOMB_JET_DIRECTIONS; directionIndex++) {
            double angle = directionIndex * Math.PI * 2.0D / MutantEntity.FLARE_BOMB_JET_DIRECTIONS;
            for (int jetIndex = 1; jetIndex <= MutantEntity.FLARE_BOMB_JETS_PER_DIRECTION; jetIndex++) {
                double distance = jetIndex * MutantEntity.FLARE_BOMB_JET_SPACING;
                double x = getX() + Math.cos(angle) * distance;
                double z = getZ() + Math.sin(angle) * distance;
                spawnGroundJet(x, z, (float) angle, jetIndex * MutantEntity.FLARE_BOMB_JET_DELAY_PER_BLOCK);
            }
        }
    }

    private void spawnGroundJet(double x, double z, float rotation, int delay) {
        BlockPos position = BlockPos.containing(x, getY() + 2.0D, z);
        double surfaceOffset = 0.0D;
        double minimumY = getY() - 2.0D;
        while (position.getY() >= Math.floor(minimumY) - 1) {
            BlockPos below = position.below();
            BlockState blockState = level().getBlockState(below);
            if (blockState.isFaceSturdy(level(), below, Direction.UP)) {
                if (!level().isEmptyBlock(position)) {
                    VoxelShape shape = level().getBlockState(position).getCollisionShape(level(), position);
                    if (!shape.isEmpty()) {
                        surfaceOffset = shape.max(Direction.Axis.Y);
                    }
                }
                Flame_Jet_Entity jet = new Flame_Jet_Entity(level(), x, position.getY() + surfaceOffset, z, rotation, delay,
                        MutantEntity.FLARE_BOMB_JET_DAMAGE, getOwner() instanceof LivingEntity owner ? owner : null);
                level().addFreshEntity(jet);
                return;
            }
            position = below;
        }
    }
}
