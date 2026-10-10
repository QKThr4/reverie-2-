package com.liminalreverie;

import com.liminalreverie.registry.ModBlocks;
import com.liminalreverie.registry.ModCreativeTabs;
import com.liminalreverie.registry.ModEntities;
import com.liminalreverie.registry.ModItems;
import com.liminalreverie.registry.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/** Liminal Reverie - dreamcore / liminal spaces adventure. Registries only; logic lives in separate systems. */
@Mod(LiminalReverie.MODID)
public class LiminalReverie {
    public static final String MODID = "liminalreverie";

    public LiminalReverie(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
    }
}
