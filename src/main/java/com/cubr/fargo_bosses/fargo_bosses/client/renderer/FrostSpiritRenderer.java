package com.cubr.fargo_bosses.fargo_bosses.client.renderer;

import com.cubr.fargo_bosses.fargo_bosses.entity.FrostSpiritEntity;
import lykrast.meetyourfight.MeetYourFight;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FrostSpiritRenderer extends MobRenderer<FrostSpiritEntity, FrostSpiritModel> {
    private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/rose_spirit.png");

    public FrostSpiritRenderer(Context context) {
        super(context, new FrostSpiritModel(context.bakeLayer(FrostSpiritModel.MODEL)), 0.5F);
        addLayer(new FrostSpiritIceLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(FrostSpiritEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getWhiteOverlayProgress(FrostSpiritEntity entity, float partialTick) {
        return 0.0F;
    }

}
