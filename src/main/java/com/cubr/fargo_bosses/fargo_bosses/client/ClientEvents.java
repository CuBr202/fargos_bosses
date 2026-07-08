package com.cubr.fargo_bosses.fargo_bosses.client;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.client.renderer.AbominationModel;
import com.cubr.fargo_bosses.fargo_bosses.client.renderer.AbominationRenderer;
import com.cubr.fargo_bosses.fargo_bosses.client.renderer.FreeFortunaBombRenderer;
import com.cubr.fargo_bosses.fargo_bosses.client.renderer.MutantModel;
import com.cubr.fargo_bosses.fargo_bosses.client.renderer.MutantRenderer;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModEntities;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = FargoBosses.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ABOMINATION.get(), AbominationRenderer::new);
        event.registerEntityRenderer(ModEntities.MUTANT.get(), MutantRenderer::new);
        event.registerEntityRenderer(ModEntities.FREE_FORTUNA_BOMB.get(), FreeFortunaBombRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AbominationModel.MODEL, () -> AbominationModel.createBodyLayer(CubeDeformation.NONE));
        event.registerLayerDefinition(MutantModel.MODEL, () -> MutantModel.createBodyLayer(CubeDeformation.NONE));
    }
}
