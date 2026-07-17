package com.cubr.fargo_bosses.fargo_bosses.client.renderer;

import com.cubr.fargo_bosses.fargo_bosses.entity.MutantLeviathanLaserEntity;
import com.github.L_Ender.cataclysm.Cataclysm;
import com.github.L_Ender.cataclysm.client.render.CMRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class MutantLeviathanLaserRenderer extends EntityRenderer<MutantLeviathanLaserEntity> {
    private static final ResourceLocation TEXTURE_0 = new ResourceLocation(Cataclysm.MODID,
            "textures/entity/leviathan/portal/abyss_portal_idle_0.png");
    private static final ResourceLocation[] TEXTURE_PROGRESS = new ResourceLocation[8];
    private static final ResourceLocation[] TEXTURE_IDLE = new ResourceLocation[4];

    public MutantLeviathanLaserRenderer(EntityRendererProvider.Context context) {
        super(context);
        for (int index = 0; index < TEXTURE_PROGRESS.length; index++) {
            TEXTURE_PROGRESS[index] = new ResourceLocation(Cataclysm.MODID,
                    "textures/entity/leviathan/portal/abyss_portal_grow_" + index + ".png");
        }
        for (int index = 0; index < TEXTURE_IDLE.length; index++) {
            TEXTURE_IDLE[index] = new ResourceLocation(Cataclysm.MODID,
                    "textures/entity/leviathan/portal/abyss_portal_idle_" + index + ".png");
        }
    }

    @Override
    public void render(MutantLeviathanLaserEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.01F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F + entity.getXRot()));
        poseStack.scale(4.0F, 4.0F, 4.0F);
        renderPortal(entity, poseStack, bufferSource);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private void renderPortal(MutantLeviathanLaserEntity entity, PoseStack poseStack, MultiBufferSource bufferSource) {
        ResourceLocation texture;
        if (entity.getLifespan() < 20) {
            texture = growingTexture((int) ((entity.getLifespan() * 0.5F) % 20));
        }
        else if (entity.tickCount < 20) {
            texture = growingTexture((int) ((entity.tickCount * 0.5F) % 20));
        }
        else {
            texture = idleTexture((int) ((entity.tickCount * 0.35F) % 3));
        }
        drawPortal(poseStack, bufferSource.getBuffer(CMRenderTypes.getfullBright(texture)));
    }

    private void drawPortal(PoseStack poseStack, VertexConsumer consumer) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        vertex(matrix, normal, consumer, -1, 0, -1, 0.0F, 0.0F);
        vertex(matrix, normal, consumer, -1, 0, 1, 0.0F, 1.0F);
        vertex(matrix, normal, consumer, 1, 0, 1, 1.0F, 1.0F);
        vertex(matrix, normal, consumer, 1, 0, -1, 1.0F, 0.0F);
    }

    private void vertex(Matrix4f matrix, Matrix3f normal, VertexConsumer consumer, int x, int y, int z, float u, float v) {
        consumer.vertex(matrix, x, y, z).color(255, 255, 255, 255).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    }

    private ResourceLocation idleTexture(int age) {
        return TEXTURE_IDLE[Mth.clamp(age, 0, TEXTURE_IDLE.length - 1)];
    }

    private ResourceLocation growingTexture(int age) {
        return TEXTURE_PROGRESS[Mth.clamp(age, 0, TEXTURE_PROGRESS.length - 1)];
    }

    @Override
    public ResourceLocation getTextureLocation(MutantLeviathanLaserEntity entity) {
        return TEXTURE_0;
    }
}
