package com.cubr.fargo_bosses.fargo_bosses.client.renderer;

import com.cubr.fargo_bosses.fargo_bosses.entity.FrostSpiritEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lykrast.meetyourfight.MeetYourFight;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class FrostSpiritIceLayer extends RenderLayer<FrostSpiritEntity, FrostSpiritModel> {
    private static final RenderType ICE_TINT = RenderType.entityTranslucent(MeetYourFight.rl("textures/entity/rose_spirit.png"));

    public FrostSpiritIceLayer(RenderLayerParent<FrostSpiritEntity, FrostSpiritModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, FrostSpiritEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        VertexConsumer consumer = buffer.getBuffer(ICE_TINT);
        getParentModel().renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                0.35F, 0.75F, 1.0F, 0.75F);
    }
}
