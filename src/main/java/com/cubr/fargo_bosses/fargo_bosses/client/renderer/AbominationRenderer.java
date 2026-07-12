package com.cubr.fargo_bosses.fargo_bosses.client.renderer;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.entity.AbominationEntity;
import lykrast.meetyourfight.renderer.GenericGlowLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AbominationRenderer extends HumanoidMobRenderer<AbominationEntity, AbominationModel> {
    public static final ResourceLocation TEXTURE = FargoBosses.rl("textures/entity/abomination.png");
    public static final ResourceLocation GLOW = FargoBosses.rl("textures/entity/abomination_glow.png");

    public AbominationRenderer(Context context) {
        super(context, new AbominationModel(context.bakeLayer(AbominationModel.MODEL)), 0.5F);
        addLayer(new GenericGlowLayer<>(this, GLOW));
        addLayer(new AbominationArmorLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(AbominationEntity entity) {
        return TEXTURE;
    }
}
