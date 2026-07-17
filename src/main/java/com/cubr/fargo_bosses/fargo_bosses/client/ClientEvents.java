package com.cubr.fargo_bosses.fargo_bosses.client;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.client.renderer.AbominationModel;
import com.cubr.fargo_bosses.fargo_bosses.client.renderer.AbominationRenderer;
import com.cubr.fargo_bosses.fargo_bosses.client.renderer.FreeFortunaBombRenderer;
import com.cubr.fargo_bosses.fargo_bosses.client.renderer.FrostSpiritRenderer;
import com.cubr.fargo_bosses.fargo_bosses.client.renderer.MutantModel;
import com.cubr.fargo_bosses.fargo_bosses.client.renderer.MutantMagneticWeaponRenderer;
import com.cubr.fargo_bosses.fargo_bosses.client.renderer.MutantLeviathanLaserRenderer;
import com.cubr.fargo_bosses.fargo_bosses.client.renderer.MutantRenderer;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModEntities;
import com.github.L_Ender.cataclysm.client.render.entity.Laser_Beam_Renderer;
import com.github.alexmodguy.alexscaves.client.render.entity.SpinningPeppermintRenderer;
import com.github.alexmodguy.alexscaves.client.render.entity.ExtinctionSpearRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.miauczel.legendary_monsters.entity.client.Render.SoulShieldRenderer;
import net.miauczel.legendary_monsters.entity.client.Render.EnergyBeamRender2d;
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
        event.registerEntityRenderer(ModEntities.ROTATING_SOUL_SHIELD.get(), soulShieldRendererProvider());
        event.registerEntityRenderer(ModEntities.FLEXIBLE_ENERGY_BEAM.get(), energyBeamRendererProvider());
        event.registerEntityRenderer(ModEntities.MUTANT_LASER_BEAM.get(), laserBeamRendererProvider());
        event.registerEntityRenderer(ModEntities.MUTANT_LEVIATHAN_LASER.get(), MutantLeviathanLaserRenderer::new);
        event.registerEntityRenderer(ModEntities.MUTANT_MAGNETIC_WEAPON.get(), magneticWeaponRendererProvider());
        event.registerEntityRenderer(ModEntities.MUTANT_SPINNING_PEPPERMINT.get(), spinningPeppermintRendererProvider());
        event.registerEntityRenderer(ModEntities.MUTANT_EXTINCTION_SPEAR.get(), extinctionSpearRendererProvider());
        event.registerEntityRenderer(ModEntities.FROST_SPIRIT.get(), FrostSpiritRenderer::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static EntityRendererProvider soulShieldRendererProvider() {
        return context -> (EntityRenderer) new SoulShieldRenderer(context);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static EntityRendererProvider energyBeamRendererProvider() {
        return context -> (EntityRenderer) new EnergyBeamRender2d(context);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static EntityRendererProvider laserBeamRendererProvider() {
        return context -> (EntityRenderer) new Laser_Beam_Renderer(context);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static EntityRendererProvider magneticWeaponRendererProvider() {
        return context -> (EntityRenderer) new MutantMagneticWeaponRenderer(context);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static EntityRendererProvider spinningPeppermintRendererProvider() {
        return context -> (EntityRenderer) new SpinningPeppermintRenderer(context);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static EntityRendererProvider extinctionSpearRendererProvider() {
        return context -> (EntityRenderer) new ExtinctionSpearRenderer(context);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AbominationModel.MODEL, () -> AbominationModel.createBodyLayer(CubeDeformation.NONE));
        event.registerLayerDefinition(MutantModel.MODEL, () -> MutantModel.createBodyLayer(CubeDeformation.NONE));
    }
}
