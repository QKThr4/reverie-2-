package com.liminalreverie.world;

import com.liminalreverie.registry.ModSounds;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** The Lounge: a warm, quiet safe room with couches, lamps, bookshelves and a window. */
public final class LoungeArea {
    public static final AreaDef DEF = new AreaDef("lounge", 6, false)
            .spawn(0.5, 0, 5.5, 180.0F)
            .bounds(-12, 12, -10, 10)
            .gate(d -> d.chapter >= 2)
            .builder(LoungeArea::build)
            .triggers(LoungeArea::triggers)
            .npcs(List.of())
            .ambient(() -> ModSounds.AMB_SAFE.get())
            .fog(232, 200, 165, 60)
            .safe();

    private LoungeArea() {}

    static void build(Build b) {
        BlockState warm = Build.st(Blocks.TERRACOTTA);
        b.room(-10, -8, 10, 8, 6, warm, Pal.carpet(), Build.st(Blocks.DARK_OAK_PLANKS));
        for (int x : new int[]{-5, 5}) for (int z : new int[]{-3, 3}) b.set(x, 6, z, Build.st(Blocks.SHROOMLIGHT));
        b.box(-6, 2, -8, 6, 4, -8, Build.st(Blocks.GLASS));
        b.box(-9, 0, -6, -9, 3, 6, Build.st(Blocks.BOOKSHELF));
        for (int i = 0; i < 4; i++) b.stairs(-3 + i, 0, 2, Blocks.BRICK_STAIRS, Direction.SOUTH);
        for (int i = 0; i < 4; i++) b.stairs(-3 + i, 0, -2, Blocks.BRICK_STAIRS, Direction.NORTH);
        b.set(-1, 0, 0, Build.st(Blocks.SPRUCE_SLAB));
        b.set(-1, 1, 0, Build.st(Blocks.LANTERN));
        b.set(0, 0, -6, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        b.set(8, 0, -6, Build.st(Blocks.POTTED_FERN));
        b.set(8, 0, 6, Build.st(Blocks.POTTED_FERN));
        b.door(0, 0, 8, Direction.NORTH, Blocks.OAK_DOOR);     // back to the lobby
        b.door(10, 0, 0, Direction.WEST, Blocks.OAK_DOOR);     // to the bedroom
        b.fragment(-8, 1, 5, 18);
        b.fragment(9, 1, -7, 31);
    }

    static void triggers(AreaDef d) {
        Triggers.putDoor(d, 0, 0, 8, Triggers.back("lounge", "lobby"));
        Triggers.putDoor(d, 10, 0, 0, Triggers.travel("bedroom"));
        for (int y = 0; y <= 3; y++) Triggers.put(d.pos(-9, y, 0), Triggers.note("lr.note.lounge_books"));
    }
}
