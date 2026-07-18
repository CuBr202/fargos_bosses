package com.cubr.fargo_bosses.fargo_bosses.client.renderer;

import com.cubr.fargo_bosses.fargo_bosses.entity.MutantEntity;
import lykrast.meetyourfight.MeetYourFight;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MutantArmorLayer extends EnergySwirlLayer<MutantEntity, MutantModel> {
    private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/rosalyne_armor.png");
    private final MutantModel model;

    public MutantArmorLayer(RenderLayerParent<MutantEntity, MutantModel> parent, EntityModelSet modelSet) {
        super(parent);
        model = new MutantModel(modelSet.bakeLayer(MutantModel.MODEL));
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
    protected EntityModel<MutantEntity> model() {
        return model;
    }
}
