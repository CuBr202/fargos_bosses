package com.cubr.fargo_bosses.fargo_bosses.client.renderer;

import com.cubr.fargo_bosses.fargo_bosses.entity.AbominationEntity;
import lykrast.meetyourfight.MeetYourFight;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class AbominationArmorLayer extends EnergySwirlLayer<AbominationEntity, AbominationModel> {
    private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/rosalyne_armor.png");
    private final AbominationModel model;

    public AbominationArmorLayer(RenderLayerParent<AbominationEntity, AbominationModel> parent, EntityModelSet modelSet) {
        super(parent);
        model = new AbominationModel(modelSet.bakeLayer(AbominationModel.MODEL));
    }

    @Override
    protected float xOffset(float ticks) {
        return Mth.cos(ticks * 0.02F) * 2.0F;
    }

    @Override
    protected ResourceLocation getTextureLocation() {
        return TEXTURE;
    }

    @Override
    protected EntityModel<AbominationEntity> model() {
        return model;
    }
}
