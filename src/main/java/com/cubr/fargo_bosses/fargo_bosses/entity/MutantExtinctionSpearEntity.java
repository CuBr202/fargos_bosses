package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.github.alexmodguy.alexscaves.server.entity.item.ExtinctionSpearEntity;
import com.github.alexmodguy.alexscaves.server.entity.util.TephraExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;

/** An Extinction Spear whose ground impact uses Mutant's larger Tephra blast. */
public class MutantExtinctionSpearEntity extends ExtinctionSpearEntity {
    private static final float EXPLOSION_RADIUS = 5.0F;
    private static final int FIRE_CLEAR_RADIUS = 7;
    private boolean exploded;

    public MutantExtinctionSpearEntity(EntityType<? extends MutantExtinctionSpearEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && inGround && !exploded && !isRemoved()) {
            exploded = true;
            TephraExplosion explosion = new TephraExplosion(level(), getOwner(), getX(), getY(0.5D), getZ(),
                    EXPLOSION_RADIUS, Explosion.BlockInteraction.KEEP);
            explosion.explode();
            explosion.finalizeExplosion(true);
            clearExplosionFire();
            discard();
        }
    }

    private void clearExplosionFire() {
        BlockPos center = blockPosition();
        for (BlockPos position : BlockPos.betweenClosed(center.offset(-FIRE_CLEAR_RADIUS, -FIRE_CLEAR_RADIUS, -FIRE_CLEAR_RADIUS),
                center.offset(FIRE_CLEAR_RADIUS, FIRE_CLEAR_RADIUS, FIRE_CLEAR_RADIUS))) {
            if (level().getBlockState(position).getBlock() instanceof BaseFireBlock) {
                level().setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
            }
        }
    }
}
