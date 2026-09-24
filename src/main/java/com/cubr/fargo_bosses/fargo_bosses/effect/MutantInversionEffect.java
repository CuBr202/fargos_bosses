package com.cubr.fargo_bosses.fargo_bosses.effect;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = FargoBosses.MODID)
public class MutantInversionEffect extends MobEffect {
    public MutantInversionEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFF2CB);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void allowInversion(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect() == ModEffects.MUTANT_INVERSION.get()) {
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void preventInversionRemoval(MobEffectEvent.Remove event) {
        if (event.getEffect() == ModEffects.MUTANT_INVERSION.get()) {
            // Expiration uses MobEffectEvent.Expired, so the normal duration still ends.
            event.setCanceled(true);
        }
    }
}
