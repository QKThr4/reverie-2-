package com.liminalreverie.registry;

import com.liminalreverie.LiminalReverie;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LiminalReverie.MODID);

    private ModCreativeTabs() {}

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.liminalreverie"))
                    .icon(() -> new ItemStack(ModItems.JOURNAL.get()))
                    .displayItems((params, out) -> {
                        out.accept(ModItems.JOURNAL.get());
                        out.accept(ModItems.BRASS_KEY.get());
                        out.accept(ModItems.FADED_TICKET.get());
                        out.accept(ModItems.WALLPAPER.get());
                        out.accept(ModItems.LIMINAL_CARPET.get());
                        out.accept(ModItems.FLUORESCENT_LIGHT.get());
                        out.accept(ModItems.POOL_TILE.get());
                        out.accept(ModItems.MALL_TILE.get());
                        out.accept(ModItems.MEMORY_FRAGMENT.get());
                        out.accept(ModItems.SYMBOL_MARK.get());
                    }).build());
}
