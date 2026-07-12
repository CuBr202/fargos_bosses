package com.cubr.fargo_bosses.fargo_bosses.registry;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FargoBosses.MODID);

    public static final RegistryObject<SoundEvent> ABOM_PHASE_1 = register("abom_phase_1");
    public static final RegistryObject<SoundEvent> ABOM_PHASE_2 = register("abom_phase_2");
    public static final RegistryObject<SoundEvent> MUTANT_PHASE_1 = register("mutant_phase_1");
    public static final RegistryObject<SoundEvent> MUTANT_PHASE_2 = register("mutant_phase_2");
    public static final RegistryObject<SoundEvent> MUTANT_PHASE_3 = register("mutant_phase_3");
    public static final RegistryObject<SoundEvent> MUTANT_PHASE_4 = register("mutant_phase_4");

    private static RegistryObject<SoundEvent> register(String name) {
        return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(FargoBosses.rl(name)));
    }
}
