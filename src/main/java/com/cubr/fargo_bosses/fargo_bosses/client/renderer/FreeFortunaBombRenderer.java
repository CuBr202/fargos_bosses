package com.cubr.fargo_bosses.fargo_bosses.client.renderer;

import com.cubr.fargo_bosses.fargo_bosses.entity.FreeFortunaBombEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.renderer.FortunaBombModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FreeFortunaBombRenderer extends EntityRenderer<FreeFortunaBombEntity> {
    private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/fortuna_dice.png");
    private static final ResourceLocation TEXTURE_WARNING = MeetYourFight.rl("textures/entity/fortuna_dice_white.png");

    private final FortunaBombModel<FreeFortunaBombEntity> model;

    public FreeFortunaBombRenderer(Context context) {
        super(context);
        model = new FortunaBombModel<>(context.bakeLayer(FortunaBombModel.MODEL));
    }

    @Override
    protected int getBlockLightLevel(FreeFortunaBombEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(FreeFortunaBombEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float yRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        model.setupAnim(entity, 0, 0, 0, yRot, xRot);
        VertexConsumer vertexConsumer = buffer.getBuffer(model.renderType(TEXTURE));
        poseStack.translate(0, 0.15, 0);
        poseStack.pushPose();

        int fuse = entity.getFuse();
        int overlay = OverlayTexture.NO_OVERLAY;
        if (fuse - partialTicks + 1 < 10) {
            float scaleIn = 1.0F - ((float) fuse - partialTicks + 1.0F) / 10.0F;
            scaleIn = Mth.clamp(scaleIn, 0, 1);
            scaleIn *= scaleIn;
            scaleIn *= scaleIn;
            float scale = 1.0F + scaleIn * 2;
            poseStack.scale(scale, scale, scale);
            if (fuse - partialTicks + 1 < 5) {
                overlay = OverlayTexture.pack(OverlayTexture.u(1), 10);
            }
        }

        float age = (float) entity.tickCount + partialTicks;
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(age * 0.1F) * 180));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.cos(age * 0.1F) * 180));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(age * 0.15F) * 360));

        model.renderToBuffer(poseStack, vertexConsumer, packedLight, overlay, 1, 1, 1, 1);
        poseStack.popPose();

        if (fuse - partialTicks + 1 < 5) {
            VertexConsumer warningVertex = buffer.getBuffer(model.renderType(TEXTURE_WARNING));
            float scale = 1.0F - (fuse - partialTicks) / 5.0F;
            scale = Mth.clamp(scale, 0, 1);
            scale *= scale;
            scale *= scale;
            scale = (1 - scale) * 0.125F;
            model.setupAnim(entity, 0, 0, 0, 0, 0);
            for (int i = 0; i < 4; i++) {
                poseStack.mulPose(Axis.YP.rotationDegrees(45));
                poseStack.pushPose();
                poseStack.scale(64, scale, scale);
                model.renderToBuffer(poseStack, warningVertex, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
                poseStack.popPose();
            }
        }
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FreeFortunaBombEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(FreeFortunaBombEntity entity, Frustum frustum, double x, double y, double z) {
        return super.shouldRender(entity, frustum, x, y, z) || entity.getFuse() < 7;
    }
}
