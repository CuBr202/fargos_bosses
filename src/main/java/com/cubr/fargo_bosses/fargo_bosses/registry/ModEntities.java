package com.cubr.fargo_bosses.fargo_bosses.registry;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.entity.AbominationEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.FlexibleEnergyBeamEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.FrostSpiritEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.FreeFortunaBombEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.MutantEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.MutantExtinctionSpearEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.MutantLaserBeamEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.MutantLeviathanLaserEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.MutantMagneticWeaponEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.MutantSpinningPeppermintEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.RotatingSoulShieldEntity;
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

    public static final RegistryObject<EntityType<FreeFortunaBombEntity>> FREE_FORTUNA_BOMB = REGISTER.register("free_fortuna_bomb",
            () -> EntityType.Builder.<FreeFortunaBombEntity>of(FreeFortunaBombEntity::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F)
                    .setUpdateInterval(1)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(""));

    public static final RegistryObject<EntityType<RotatingSoulShieldEntity>> ROTATING_SOUL_SHIELD = REGISTER.register("rotating_soul_shield",
            () -> EntityType.Builder.<RotatingSoulShieldEntity>of(RotatingSoulShieldEntity::new, MobCategory.MISC)
                    .sized(3.0F, 3.0F)
                    .setUpdateInterval(1)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(""));

    public static final RegistryObject<EntityType<FlexibleEnergyBeamEntity>> FLEXIBLE_ENERGY_BEAM = REGISTER.register("flexible_energy_beam",
            () -> EntityType.Builder.<FlexibleEnergyBeamEntity>of(FlexibleEnergyBeamEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .setUpdateInterval(1)
                    .setTrackingRange(128)
                    .setShouldReceiveVelocityUpdates(false)
                    .build(""));

    public static final RegistryObject<EntityType<MutantLaserBeamEntity>> MUTANT_LASER_BEAM = REGISTER.register("mutant_laser_beam",
            () -> EntityType.Builder.<MutantLaserBeamEntity>of(MutantLaserBeamEntity::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F)
                    .fireImmune()
                    .setUpdateInterval(1)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(""));

    public static final RegistryObject<EntityType<MutantLeviathanLaserEntity>> MUTANT_LEVIATHAN_LASER = REGISTER.register("mutant_leviathan_laser",
            () -> EntityType.Builder.<MutantLeviathanLaserEntity>of(MutantLeviathanLaserEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(3.0F, 0.15F)
                    .setUpdateInterval(1)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(false)
                    .build(""));

    public static final RegistryObject<EntityType<MutantMagneticWeaponEntity>> MUTANT_MAGNETIC_WEAPON = REGISTER.register("mutant_magnetic_weapon",
            () -> EntityType.Builder.<MutantMagneticWeaponEntity>of(MutantMagneticWeaponEntity::new, MobCategory.MISC)
                    .sized(MutantMagneticWeaponEntity.COLLISION_RADIUS * 2.0F,
                            MutantMagneticWeaponEntity.COLLISION_RADIUS * 2.0F)
                    .setUpdateInterval(1)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(""));

    public static final RegistryObject<EntityType<MutantSpinningPeppermintEntity>> MUTANT_SPINNING_PEPPERMINT = REGISTER.register("mutant_spinning_peppermint",
            () -> EntityType.Builder.<MutantSpinningPeppermintEntity>of(MutantSpinningPeppermintEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .setUpdateInterval(1)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(""));

    public static final RegistryObject<EntityType<MutantExtinctionSpearEntity>> MUTANT_EXTINCTION_SPEAR = REGISTER.register("mutant_extinction_spear",
            () -> EntityType.Builder.<MutantExtinctionSpearEntity>of(MutantExtinctionSpearEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .setUpdateInterval(1)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(""));

    public static final RegistryObject<EntityType<FrostSpiritEntity>> FROST_SPIRIT = REGISTER.register("frost_spirit",
            () -> EntityType.Builder.<FrostSpiritEntity>of(FrostSpiritEntity::new, MobCategory.MONSTER)
                    .sized(0.75F, 1.5F)
                    .setUpdateInterval(1)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(""));

    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ABOMINATION.get(), AbominationEntity.createAttributes().build());
        event.put(MUTANT.get(), MutantEntity.createAttributes().build());
        event.put(FROST_SPIRIT.get(), FrostSpiritEntity.createAttributes().build());
    }
}
