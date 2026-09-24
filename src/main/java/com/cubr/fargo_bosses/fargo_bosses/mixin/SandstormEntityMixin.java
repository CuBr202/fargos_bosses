package com.cubr.fargo_bosses.fargo_bosses.mixin;

import com.cubr.fargo_bosses.fargo_bosses.entity.MutantEntity;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModEffects;
import com.github.L_Ender.cataclysm.entity.effect.Sandstorm_Entity;
import com.github.L_Ender.cataclysm.init.ModEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Sandstorm_Entity.class, remap = false)
public abstract class SandstormEntityMixin {
    @Redirect(method = "damage", at = @At(value = "FIELD",
            target = "Lcom/github/L_Ender/cataclysm/init/ModEffect;EFFECTCURSE_OF_DESERT:Lnet/minecraftforge/registries/RegistryObject;"),
            remap = false)
    private RegistryObject<MobEffect> fargoBosses$replaceDesertCurse() {
        return ((Sandstorm_Entity) (Object) this).getCaster() instanceof MutantEntity
                ? ModEffects.MUTANT_INVERSION : ModEffect.EFFECTCURSE_OF_DESERT;
    }
}
