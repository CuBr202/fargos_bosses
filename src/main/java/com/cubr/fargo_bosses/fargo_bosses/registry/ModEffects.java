package com.cubr.fargo_bosses.fargo_bosses.registry;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.effect.MutantInversionEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, FargoBosses.MODID);

    public static final RegistryObject<MobEffect> MUTANT_INVERSION = REGISTER.register("mutant_inversion", MutantInversionEffect::new);
}
