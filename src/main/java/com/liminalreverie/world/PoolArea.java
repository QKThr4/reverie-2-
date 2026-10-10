package com.liminalreverie.world;

import com.liminalreverie.entity.NpcKind;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.Puzzles;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** The Blue Pool: an enormous, peaceful indoor swimming complex with a quiet secret underneath. */
public final class PoolArea {
    public static final AreaDef DEF = new AreaDef("pool", 2, true)
            .spawn(0.5, 0, 28.5, 180.0F)
            .bounds(-56, 36, -36, 36).bounds(194, 206, 194, 206)
            .phases(3, PoolArea::phase)
            .gate(d -> Puzzles.areaDone(d, "school"))
            .builder(PoolArea::build)
            .triggers(PoolArea::triggers)
            .npcs(List.of(new NpcPlacement(NpcKind.CHILD, -30.5, 0, 10.5, 270.0F, "child", d -> d.chapter >= 3, false)))
            .ambient(() -> ModSounds.AMB_POOL.get())
            .fog(190, 215, 225, 90);

    private PoolArea() {}

    private static BlockState ladder(Direction facing) {
        return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
    }

    static void build(Build b) {
        BlockState wall = Build.st(Blocks.LIGHT_BLUE_CONCRETE);
        BlockState tile = Pal.poolTile();
        BlockState glass = Build.st(Blocks.GLASS);
        BlockState water = Build.st(Blocks.WATER);

        // hall: thick tiled floor so the basins can be carved out of it
        b.box(-32, -6, -32, 32, -1, 32, tile);
        b.room(-32, -32, 32, 32, 12, wall, tile, tile);
        for (int x = -24; x <= 24; x += 8) for (int z = -24; z <= 24; z += 8) b.set(x, 12, z, Pal.light());
        b.box(-22, -4, -22, 22, -1, 6, water);   // main basin (full of water)
        b.box(-28, -2, 12, -14, -1, 24, water);   // children's basin
        b.box(-32, 6, -24, -32, 10, 24, glass);
        b.box(32, 6, -24, 32, 10, 24, glass);

        // changing cubicles along the north wall
        for (int x = -30; x <= 30; x += 5) b.box(x, 0, -31, x, 2, -27, wall);
        for (int x = -28; x <= 28; x += 5) b.set(x, 0, -31, Build.st(Blocks.BIRCH_SLAB));

        // slide tower and slide
        b.box(18, 0, 9, 20, 8, 11, wall);
        for (int y = 0; y <= 7; y++) b.set(19, y, 8, ladder(Direction.NORTH));
        for (int i = 0; i <= 8; i++) b.stairs(19, 8 - i, 8 - i, Blocks.QUARTZ_STAIRS, Direction.SOUTH);

        // four coloured pads (sequence puzzle) and the poster that explains them
        BlockState[] pads = {Build.st(Blocks.RED_CONCRETE), Build.st(Blocks.BLUE_CONCRETE),
                Build.st(Blocks.YELLOW_CONCRETE), Build.st(Blocks.LIME_CONCRETE)};
        for (int k = 0; k < 4; k++) b.box(-13 + 8 * k, -1, 23, -11 + 8 * k, -1, 25, pads[k]);
        b.set(0, 0, 26, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.NORTH));

        // the drain, and the quiet corridor beneath it
        b.set(0, -5, -8, Build.st(Blocks.IRON_TRAPDOOR));
        b.room(-3, -12, -10, 22, -6, 5, wall, tile, tile);
        b.set(6, -7, -8, Pal.light());
        b.set(14, -7, -8, Pal.light());
        b.box(0, -12, -9, 0, -8, -9, tile);
        for (int y = -12; y <= -8; y++) b.set(0, y, -8, ladder(Direction.SOUTH));
        b.door(22, -12, -8, Direction.WEST, Blocks.OAK_DOOR);
        b.fragment(18, -11, -8, 8);
        b.symbol(10, -11, -10, 4);

        // sealed underwater room (reached through the hatch in the deck)
        b.room(-50, -12, -4, -40, 4, 5, wall, tile, tile);
        b.box(-49, -12, -3, -41, -8, 3, water);
        b.set(-45, -7, 0, Pal.light());
        b.set(-41, -7, 0, Build.st(Blocks.IRON_TRAPDOOR));
        b.fragment(-45, -10, 0, 23);
        b.set(-26, 0, -26, Build.st(Blocks.OAK_TRAPDOOR));

        // way back, memories and a symbol
        b.door(4, 0, 32, Direction.NORTH, Blocks.OAK_DOOR);
        b.fragment(-28, 1, -29, 6);
        b.fragment(-20, 0, 18, 7);
        b.symbol(0, 1, -31, 3);
    }

    /** Called when the drain puzzle is solved: opens the shaft down to the hidden corridor. */
    public static void openDrain(ServerLevel level) {
        Build b = new Build(level, DEF);
        b.air(-22, -4, -22, 22, -1, 6);      // the pool drains
        b.air(0, -7, -8, 0, -5, -8);
        for (int y = -7; y <= -5; y++) b.set(0, y, -8, ladder(Direction.SOUTH));
        b.runNow();
    }

    static void phase(Build b, int n) {
        switch (n) {
            case 1 -> {
                for (int x = -12; x <= 12; x += 12) {
                    for (int z = -12; z <= 0; z += 12) b.stairs(x, 4, z, Blocks.OAK_STAIRS, Direction.NORTH);
                }
            }
            case 2 -> {
                for (int x = -24; x <= 24; x += 16) for (int z = -24; z <= 24; z += 16) b.set(x, 12, z, Pal.poolTile());
            }
            case 3 -> b.box(-28, -2, 12, -14, -1, 24, Build.AIR);
            default -> { }
        }
    }

    static void triggers(AreaDef d) {
        for (int k = 0; k < 4; k++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) Triggers.put(d.pos(-12 + 8 * k + dx, -1, 24 + dz), Triggers.seq("pool_sequence", k));
            }
        }
        Triggers.put(d.pos(0, -5, -8), Triggers.single("pool_drain"));
        Triggers.put(d.pos(0, 0, 26), Triggers.note("lr.note.pool_poster"));
        Triggers.putDoor(d, 22, -12, -8, Triggers.travel("hotel"));
        Triggers.putDoor(d, 4, 0, 32, Triggers.travel("lobby"));
        Triggers.put(d.pos(-26, 0, -26), Triggers.hop(d, -45.5, -11, 0.5, 0.0F));
        Triggers.put(d.pos(-41, -7, 0), Triggers.hop(d, -26.5, 0, -24.5, 0.0F));
    }
}
