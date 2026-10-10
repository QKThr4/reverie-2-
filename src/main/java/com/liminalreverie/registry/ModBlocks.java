package com.liminalreverie.registry;

import com.liminalreverie.LiminalReverie;
import com.liminalreverie.block.MemoryFragmentBlock;
import com.liminalreverie.block.SymbolMarkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LiminalReverie.MODID);

    private ModBlocks() {}

    private static BlockBehaviour.Properties solid(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(2.0F, 6.0F).sound(SoundType.STONE);
    }

    public static final DeferredBlock<Block> WALLPAPER =
            BLOCKS.register("wallpaper", () -> new Block(solid(MapColor.SAND)));
    public static final DeferredBlock<Block> LIMINAL_CARPET =
            BLOCKS.register("liminal_carpet", () -> new Block(solid(MapColor.COLOR_BROWN).sound(SoundType.WOOL)));
    public static final DeferredBlock<Block> FLUORESCENT_LIGHT =
            BLOCKS.register("fluorescent_light",
                    () -> new Block(solid(MapColor.QUARTZ).sound(SoundType.GLASS).lightLevel(s -> 15)));
    public static final DeferredBlock<Block> POOL_TILE =
            BLOCKS.register("pool_tile", () -> new Block(solid(MapColor.COLOR_LIGHT_BLUE)));
    public static final DeferredBlock<Block> MALL_TILE =
            BLOCKS.register("mall_tile", () -> new Block(solid(MapColor.TERRACOTTA_WHITE)));

    public static final DeferredBlock<MemoryFragmentBlock> MEMORY_FRAGMENT =
            BLOCKS.register("memory_fragment", () -> new MemoryFragmentBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK)
                            .strength(-1.0F, 3600000.0F).noCollission().noOcclusion()
                            .sound(SoundType.AMETHYST_CLUSTER).lightLevel(s -> 11)));

    public static final DeferredBlock<SymbolMarkBlock> SYMBOL_MARK =
            BLOCKS.register("symbol_mark", () -> new SymbolMarkBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                            .strength(-1.0F, 3600000.0F).sound(SoundType.STONE).lightLevel(s -> 3)));
}
