package com.cubr.fargo_bosses.fargo_bosses.client.renderer;

import com.cubr.fargo_bosses.fargo_bosses.entity.MutantEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.MutantMagneticWeaponEntity;
import com.github.alexmodguy.alexscaves.client.render.entity.MagneticWeaponRenderer;
import com.github.alexmodguy.alexscaves.server.entity.item.MagneticWeaponEntity;
import com.github.alexthe666.citadel.client.render.LightningBoltData;
import com.github.alexthe666.citadel.client.render.LightningRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MutantMagneticWeaponRenderer extends MagneticWeaponRenderer {
    private final Map<UUID, LightningRender> lightningRenders = new HashMap<>();

    public MutantMagneticWeaponRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MagneticWeaponEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        if (!(entity instanceof MutantMagneticWeaponEntity weapon)) {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            return;
        }

        MutantEntity mutant = weapon.getMutantController();
        if (mutant != null && mutant.isAlive()) {
            renderConnection(mutant, weapon, partialTicks, poseStack, buffer);
        }

        super.render(weapon, entityYaw, partialTicks, poseStack, buffer, packedLight);
        if (weapon.isRemoved()) {
            lightningRenders.remove(weapon.getUUID());
        }
    }

    private void renderConnection(MutantEntity mutant, MutantMagneticWeaponEntity weapon, float partialTicks,
                                  PoseStack poseStack, MultiBufferSource buffer) {
        double weaponX = Mth.lerp(partialTicks, weapon.xOld, weapon.getX());
        double weaponY = Mth.lerp(partialTicks, weapon.yOld, weapon.getY());
        double weaponZ = Mth.lerp(partialTicks, weapon.zOld, weapon.getZ());
        Vec3 from = mutant.getPosition(partialTicks).add(0.0D, mutant.getBbHeight() * 0.72D, 0.0D);
        Vec3 to = weapon.getPosition(partialTicks).add(0.0D, 0.24D, 0.0D);
        float distance = weapon.distanceTo(mutant);
        int segments = Mth.clamp((int) distance + 2, 3, 30);
        float spread = Mth.clamp((10.0F - distance) / 10.0F * 0.2F, 0.01F, 0.2F);

        LightningBoltData.BoltRenderInfo boltInfo = new LightningBoltData.BoltRenderInfo(0.0F, spread, 0.0F, 0.0F,
                new Vector4f(0.2F, 0.45F, 1.0F, 0.85F), 0.1F);
        LightningBoltData bolt = new LightningBoltData(boltInfo, from, to, segments)
                .size(0.1F)
                .lifespan(1)
                .spawn(LightningBoltData.SpawnFunction.CONSECUTIVE)
                .fade(LightningBoltData.FadeFunction.NONE);

        poseStack.pushPose();
        poseStack.translate(-weaponX, -weaponY, -weaponZ);
        LightningRender lightning = lightningRenders.computeIfAbsent(weapon.getUUID(), ignored -> new LightningRender());
        lightning.update(weapon, bolt, partialTicks);
        lightning.render(partialTicks, poseStack, buffer);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(MagneticWeaponEntity entity, Frustum camera, double x, double y, double z) {
        if (super.shouldRender(entity, camera, x, y, z)) {
            return true;
        }

        if (!(entity instanceof MutantMagneticWeaponEntity weapon)) {
            return false;
        }

        MutantEntity mutant = weapon.getMutantController();
        if (mutant == null) {
            return false;
        }

        Vec3 weaponPosition = weapon.position();
        Vec3 mutantPosition = mutant.position();
        return camera.isVisible(new AABB(mutantPosition.x, mutantPosition.y, mutantPosition.z,
                weaponPosition.x, weaponPosition.y, weaponPosition.z));
    }
}
