package com.cubr.fargo_bosses.fargo_bosses;

import com.cubr.fargo_bosses.fargo_bosses.registry.ModEntities;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModSounds;
import com.cubr.fargo_bosses.fargo_bosses.entity.AbominationEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.FlexibleEnergyBeamEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.RotatingSoulShieldEntity;
import com.mojang.logging.LogUtils;
import lykrast.meetyourfight.entity.RoseSpiritEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(FargoBosses.MODID)
public class FargoBosses {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "fargo_bosses";

    public static final Logger LOGGER = LogUtils.getLogger();

    public FargoBosses() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.REGISTER.register(modEventBus);
        ModSounds.REGISTER.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void applyAbominationDamage(LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        Entity directEntity = event.getSource().getDirectEntity();



        AbominationEntity explosionOwner = null;
        if (attacker instanceof AbominationEntity abomination) {
            explosionOwner = abomination;
        }
        else if (directEntity instanceof AbominationEntity directAbomination) {
            explosionOwner = directAbomination;
        }
        if (explosionOwner != null && event.getSource().is(DamageTypeTags.IS_EXPLOSION)
                && explosionOwner.isInsideBombSafetyCircle(event.getEntity())) {
            event.setAmount(0.0F);
            return;
        }

        if (event.getEntity() instanceof AbominationEntity && attacker instanceof RoseSpiritEntity spirit
                && spirit.getOwner() == event.getEntity()) {
            event.setAmount(0.0F);
            return;
        }

        if (directEntity instanceof RotatingSoulShieldEntity shield && shield.getCaster() instanceof AbominationEntity) {
            event.setAmount(((AbominationEntity) shield.getCaster()).getBorderDamage());
            return;
        }

        if (directEntity instanceof FlexibleEnergyBeamEntity && attacker instanceof AbominationEntity) {
            event.setAmount(((AbominationEntity) attacker).getLaserDamage());
            return;
        }

        if ((directEntity != null && directEntity.getPersistentData().contains(AbominationEntity.MYF_PROJECTILE_DAMAGE_TAG))
                || attacker instanceof AbominationEntity
                || (attacker instanceof RoseSpiritEntity spirit
                && spirit.getPersistentData().contains(AbominationEntity.SUMMONED_ROSE_SPIRIT_DAMAGE_TAG))) {
            if (attacker instanceof AbominationEntity abomination) event.setAmount(abomination.getBossDamage());
            else if (directEntity != null && directEntity.getPersistentData().contains(AbominationEntity.MYF_PROJECTILE_DAMAGE_TAG)) {
                event.setAmount(directEntity.getPersistentData().getFloat(AbominationEntity.MYF_PROJECTILE_DAMAGE_TAG));
            }
            else if (attacker instanceof RoseSpiritEntity spirit) {
                event.setAmount(spirit.getPersistentData().getFloat(AbominationEntity.SUMMONED_ROSE_SPIRIT_DAMAGE_TAG));
            }
        }


    }

    @SubscribeEvent
    public void debugDifficulty(MobSpawnEvent event){
        if(event.getEntity() instanceof AbominationEntity abom){
            ((AbominationEntity) abom).setBossDifficulty(2);
        }
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }

}
