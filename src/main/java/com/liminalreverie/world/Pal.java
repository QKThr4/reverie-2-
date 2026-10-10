package com.liminalreverie.world;

import com.liminalreverie.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Block palette helpers. Methods (not constants) so registry objects are only touched after registration. */
public final class Pal {
    private Pal() {}

    public static BlockState wallpaper() { return ModBlocks.WALLPAPER.get().defaultBlockState(); }
    public static BlockState carpet() { return ModBlocks.LIMINAL_CARPET.get().defaultBlockState(); }
    public static BlockState light() { return ModBlocks.FLUORESCENT_LIGHT.get().defaultBlockState(); }
    public static BlockState poolTile() { return ModBlocks.POOL_TILE.get().defaultBlockState(); }
    public static BlockState mallTile() { return ModBlocks.MALL_TILE.get().defaultBlockState(); }

    public static BlockState of(Block b) { return b.defaultBlockState(); }

    /** Stairs whose high side (the "back") is on {@code backSide}. */
    public static BlockState stairs(Block b, Direction backSide) {
        return b.defaultBlockState().setValue(StairBlock.FACING, backSide);
    }
}
