package com.liminalreverie.registry;

import com.liminalreverie.LiminalReverie;
import com.liminalreverie.item.JournalItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LiminalReverie.MODID);

    private ModItems() {}

    public static final DeferredItem<JournalItem> JOURNAL =
            ITEMS.register("journal", () -> new JournalItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> BRASS_KEY =
            ITEMS.registerSimpleItem("brass_key", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> FADED_TICKET =
            ITEMS.registerSimpleItem("faded_ticket", new Item.Properties().stacksTo(1));

    public static final DeferredItem<BlockItem> WALLPAPER =
            ITEMS.registerSimpleBlockItem("wallpaper", ModBlocks.WALLPAPER);
    public static final DeferredItem<BlockItem> LIMINAL_CARPET =
            ITEMS.registerSimpleBlockItem("liminal_carpet", ModBlocks.LIMINAL_CARPET);
    public static final DeferredItem<BlockItem> FLUORESCENT_LIGHT =
            ITEMS.registerSimpleBlockItem("fluorescent_light", ModBlocks.FLUORESCENT_LIGHT);
    public static final DeferredItem<BlockItem> POOL_TILE =
            ITEMS.registerSimpleBlockItem("pool_tile", ModBlocks.POOL_TILE);
    public static final DeferredItem<BlockItem> MALL_TILE =
            ITEMS.registerSimpleBlockItem("mall_tile", ModBlocks.MALL_TILE);
    public static final DeferredItem<BlockItem> MEMORY_FRAGMENT =
            ITEMS.registerSimpleBlockItem("memory_fragment", ModBlocks.MEMORY_FRAGMENT);
    public static final DeferredItem<BlockItem> SYMBOL_MARK =
            ITEMS.registerSimpleBlockItem("symbol_mark", ModBlocks.SYMBOL_MARK);
}
