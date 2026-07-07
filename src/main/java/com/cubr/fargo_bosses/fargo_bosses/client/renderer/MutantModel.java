package com.cubr.fargo_bosses.fargo_bosses.client.renderer;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.entity.MutantEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class MutantModel extends HumanoidModel<MutantEntity> {
    public static final ModelLayerLocation MODEL = new ModelLayerLocation(FargoBosses.rl("mutant"), "main");

    private final ModelPart bell;
    private final ModelPart rightSleeve;
    private final ModelPart leftSleeve;
    private final ModelPart jacket;

    public MutantModel(ModelPart modelPart) {
        super(modelPart);
        leftLeg.visible = false;
        bell = modelPart.getChild("bell");
        rightSleeve = modelPart.getChild("right_sleeve");
        leftSleeve = modelPart.getChild("left_sleeve");
        jacket = modelPart.getChild("jacket");
    }

    public static LayerDefinition createBodyLayer(CubeDeformation deform) {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(deform, 0);
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeDeformation clothes = deform.extend(0.25F);

        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(26, 48).addBox(-1.0F, -1.0F, -2.0F, 6.0F, 10.0F, 4.0F, clothes), PartPose.offset(-1.9F, 12.0F, 0.0F));
        partDefinition.addOrReplaceChild("bell", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, 5.0F, 2.0F, 6.0F, 6.0F, 7.0F), PartPose.offset(-5.0F, 2.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform), PartPose.offset(-5.0F, 2.0F, 0.0F));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform), PartPose.offset(5.0F, 2.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(0, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, clothes), PartPose.offset(-5.0F, 2.5F, 0.0F));
        partDefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, clothes), PartPose.offset(5.0F, 2.5F, 0.0F));
        partDefinition.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, clothes), PartPose.ZERO);

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(bell, rightSleeve, leftSleeve, jacket));
    }

    @Override
    public void setupAnim(MutantEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        rightArm.xRot = (float) (-Math.PI / 2);
        if (attackTime > 0) {
            float f = 1 - attackTime;
            f = f * f;
            f = f * f;
            f = 1 - f;
            float f1 = Mth.sin(f * (float) Math.PI);
            float f2 = Mth.sin(attackTime * (float) Math.PI) * -(head.xRot - 0.7F) * 0.75F;
            rightArm.xRot -= f1 * 1.2F + f2;
        }
        bell.copyFrom(rightArm);
        rightSleeve.copyFrom(rightArm);
        leftSleeve.copyFrom(leftArm);
        jacket.copyFrom(body);
    }
}
