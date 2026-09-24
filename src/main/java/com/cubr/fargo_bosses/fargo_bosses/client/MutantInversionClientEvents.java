package com.cubr.fargo_bosses.fargo_bosses.client;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModEffects;
import com.github.L_Ender.cataclysm.init.ModEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FargoBosses.MODID, value = Dist.CLIENT)
public class MutantInversionClientEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void invertMovement(MovementInputUpdateEvent event) {
        if (!event.getEntity().hasEffect(ModEffects.MUTANT_INVERSION.get())) {
            return;
        }
        // Cataclysm already inverted the input if both curses are present.
        if (event.getEntity().hasEffect(ModEffect.EFFECTCURSE_OF_DESERT.get())) {
            return;
        }
        Options options = Minecraft.getInstance().options;
        if (options.keyDown.isDown()) {
            event.getInput().forwardImpulse *= -1.0F;
        }
        if (options.keyLeft.isDown()) {
            event.getInput().leftImpulse *= -1.0F;
        }
        if (options.keyRight.isDown()) {
            event.getInput().leftImpulse *= -1.0F;
        }
        if (options.keyUp.isDown()) {
            event.getInput().forwardImpulse *= -1.0F;
        }
    }
}
