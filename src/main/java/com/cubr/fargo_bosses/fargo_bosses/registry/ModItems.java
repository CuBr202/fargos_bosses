package com.cubr.fargo_bosses.fargo_bosses.registry;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = FargoBosses.MODID)
public class ModItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, FargoBosses.MODID);

    public static final RegistryObject<ForgeSpawnEggItem> ABOMINATION_SPAWN_EGG = REGISTER.register("abomination_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ABOMINATION, 0x49205D, 0x68E9EA, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> MUTANT_SPAWN_EGG = REGISTER.register("mutant_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MUTANT, 0x302C35, 0xACECDB, new Item.Properties()));

    @SubscribeEvent
    public static void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
            event.accept(ABOMINATION_SPAWN_EGG.get());
            event.accept(MUTANT_SPAWN_EGG.get());
        }
    }
}
