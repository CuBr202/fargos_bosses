package com.cubr.fargo_bosses.fargo_bosses.client.renderer;

import com.cubr.fargo_bosses.fargo_bosses.entity.FrostSpiritEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lykrast.meetyourfight.renderer.RoseSpiritModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class FrostSpiritModel extends EntityModel<FrostSpiritEntity> {
    private final ModelPart blob;
    private final ModelPart hat;
    private final ModelPart main;

    public FrostSpiritModel(ModelPart root) {
        blob = root.getChild("blob");
        hat = root.getChild("hat");
        main = root.getChild("bb_main");
    }

    @Override
    public void setupAnim(FrostSpiritEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        blob.y = 10.0F;
        hat.y = 6.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        blob.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        hat.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        main.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public static final net.minecraft.client.model.geom.ModelLayerLocation MODEL = RoseSpiritModel.MODEL;
}
