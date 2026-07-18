package com.cubr.fargo_bosses.fargo_bosses.client.renderer;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.entity.MutantEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class MutantWingLayer extends RenderLayer<MutantEntity, MutantModel> {
    private static final ResourceLocation TEXTURE = FargoBosses.rl("textures/entity/mutant_wing.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucentEmissive(TEXTURE, false);
    private static final float WING_WIDTH = 1.25F;
    private static final float WING_HEIGHT = 1.1F;
    private static final float WING_ROOT_Y = -0.55F;
    private static final float WING_ROOT_Z = 0.3475F;
    private static final float RESTING_SWEEP = 0.22F;
    private static final float RESTING_DROOP = 0.18F;
    private static final float FLAP_SPEED = 0.55F;
    private static final float FLAP_AMOUNT = 0.5F;

    public MutantWingLayer(RenderLayerParent<MutantEntity, MutantModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, MutantEntity mutant,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        VertexConsumer consumer = buffer.getBuffer(RENDER_TYPE);
        float flap = isAirborne(mutant) ? Mth.sin(ageInTicks * FLAP_SPEED) * FLAP_AMOUNT : 0.0F;
        renderWing(poseStack, consumer, -1.0F, flap);
        renderWing(poseStack, consumer, 1.0F, flap);
    }

    private boolean isAirborne(MutantEntity mutant) {
        if (mutant.onGround()) {
            return false;
        }
        BlockPos below = BlockPos.containing(mutant.getX(), mutant.getBoundingBox().minY - 0.05D, mutant.getZ());
        return !mutant.level().getBlockState(below).blocksMotion();
    }

    private void renderWing(PoseStack poseStack, VertexConsumer consumer, float side, float flap) {
        poseStack.pushPose();
        poseStack.translate(side * 0.22F, WING_ROOT_Y, WING_ROOT_Z);
        poseStack.mulPose(Axis.YP.rotation(side * (RESTING_SWEEP + flap)));
        poseStack.mulPose(Axis.ZP.rotation(side * RESTING_DROOP));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float outerX = side * WING_WIDTH;

        vertex(consumer, matrix, normal, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, normal, outerX, 0.08F, 0.0F, 0.0F, 0.0F);
        vertex(consumer, matrix, normal, outerX, WING_HEIGHT, 0.0F, 0.0F, 1.0F);
        vertex(consumer, matrix, normal, 0.0F, WING_HEIGHT * 0.82F, 0.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    private void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                        float x, float y, float z, float u, float v) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0.0F, 0.0F, -1.0F)
                .endVertex();
    }
}
