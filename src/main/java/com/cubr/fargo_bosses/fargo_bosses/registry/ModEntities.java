package com.cubr.fargo_bosses.fargo_bosses.registry;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.entity.AbominationEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.MutantEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = FargoBosses.MODID)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FargoBosses.MODID);

    public static final RegistryObject<EntityType<AbominationEntity>> ABOMINATION = REGISTER.register("abomination",
            () -> EntityType.Builder.<AbominationEntity>of(AbominationEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .setUpdateInterval(1)
                    .setTrackingRange(128)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(""));

    public static final RegistryObject<EntityType<MutantEntity>> MUTANT = REGISTER.register("mutant",
            () -> EntityType.Builder.<MutantEntity>of(MutantEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .setUpdateInterval(1)
                    .setTrackingRange(128)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(""));

    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ABOMINATION.get(), AbominationEntity.createAttributes().build());
        event.put(MUTANT.get(), MutantEntity.createAttributes().build());
    }
}
