package com.cubr.fargo_bosses.fargo_bosses.client.renderer;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.entity.MutantEntity;
import lykrast.meetyourfight.renderer.GenericGlowLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MutantRenderer extends HumanoidMobRenderer<MutantEntity, MutantModel> {
    public static final ResourceLocation TEXTURE = FargoBosses.rl("textures/entity/mutant.png");
    public static final ResourceLocation GLOW = FargoBosses.rl("textures/entity/mutant_glow.png");

    public MutantRenderer(Context context) {
        super(context, new MutantModel(context.bakeLayer(MutantModel.MODEL)), 0.5F);
        addLayer(new GenericGlowLayer<>(this, GLOW));
    }

    @Override
    public ResourceLocation getTextureLocation(MutantEntity entity) {
        return TEXTURE;
    }
}
