package com.liminalreverie.world;

import com.liminalreverie.entity.NpcKind;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.Puzzles;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Sunset Fields: a huge grass plain under an exaggerated sunset. The most peaceful place in the mod. */
public final class FieldsArea {
    private static final int[][] HOUSES = {{-60, -40}, {30, -55}, {60, 45}, {-30, 55}};

    public static final AreaDef DEF = new AreaDef("fields", 5, true)
            .spawn(-70.5, 0, 0.5, -90.0F)
            .bounds(-84, 84, -84, 84).bounds(194, 206, 194, 206)
            .phases(3, FieldsArea::phase)
            .gate(d -> d.puzzlesDone.contains("train_ticket"))
            .builder(FieldsArea::build)
            .triggers(FieldsArea::triggers)
            .npcs(List.of(new NpcPlacement(NpcKind.CHILD, 31.5, 0, -18.5, 90.0F, "child", d -> d.chapter >= 2, false)))
            .ambient(() -> ModSounds.AMB_FIELDS.get())
            .fog(255, 170, 120, 85);

    private FieldsArea() {}

    static void build(Build b) {
        BlockState fence = Build.st(Blocks.OAK_FENCE);
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        BlockState white = Build.st(Blocks.WHITE_CONCRETE);

        // ground and invisible borders
        b.box(-80, -4, -80, 80, -2, 80, Build.st(Blocks.DIRT));
        b.box(-80, -1, -80, 80, -1, 80, Build.st(Blocks.GRASS_BLOCK));
        BlockState barrier = Build.st(Blocks.BARRIER);
        b.box(-81, 0, -81, 81, 4, -81, barrier);
        b.box(-81, 0, 81, 81, 4, 81, barrier);
        b.box(-81, 0, -80, -81, 4, 80, barrier);
        b.box(81, 0, -80, 81, 4, 80, barrier);

        // road and telephone poles
        b.box(-80, -1, -2, 80, -1, 2, Build.st(Blocks.GRAY_CONCRETE));
        for (int x = -78; x <= 78; x += 4) b.box(x, -1, 0, x + 1, -1, 0, Build.st(Blocks.YELLOW_CONCRETE));
        for (int k = 0; k < 5; k++) {
            int x = -56 + 14 * k;
            b.box(x, 0, 4, x, 6, 4, fence);
            b.box(x - 1, 6, 4, x + 1, 6, 4, fence);
            b.set(x, 7, 4, Build.st(Blocks.LANTERN));
        }

        // isolated trees (deterministic)
        RandomSource rnd = RandomSource.create(5150L);
        for (int i = 0; i < 14; i++) {
            int x = rnd.nextInt(150) - 75;
            int z = rnd.nextInt(150) - 75;
            if (Math.abs(z) < 9) continue;
            int h = 5 + rnd.nextInt(3);
            b.box(x - 2, h - 2, z - 2, x + 2, h, z + 2, leaves);
            b.box(x, 0, z, x, h - 1, z, Build.st(Blocks.OAK_LOG));
        }

        // distant houses
        for (int[] h : HOUSES) {
            int hx = h[0];
            int hz = h[1];
            b.room(hx - 4, hz - 3, hx + 4, hz + 3, 4, white, Build.st(Blocks.OAK_PLANKS), Build.st(Blocks.RED_TERRACOTTA));
            b.box(hx - 5, 5, hz - 4, hx + 5, 5, hz + 4, Build.st(Blocks.RED_TERRACOTTA));
            b.set(hx, 4, hz, Pal.light());
            b.box(hx - 1, 1, hz + 3, hx + 1, 2, hz + 3, Build.st(Blocks.GLASS));
        }
        b.door(60, 0, 42, Direction.NORTH, Blocks.OAK_DOOR);   // the house door leads to the Lounge

        // playground: two swings and a sandbox
        b.box(28, 0, -20, 28, 3, -20, fence);
        b.box(34, 0, -20, 34, 3, -20, fence);
        b.box(28, 3, -20, 34, 3, -20, fence);
        for (int x : new int[]{30, 32}) {
            b.set(x, 2, -20, Build.st(Blocks.CHAIN));
            b.set(x, 1, -20, Build.st(Blocks.CHAIN));
            b.set(x, 0, -20, Build.st(Blocks.OAK_SLAB));
        }
        b.box(36, -1, -16, 40, -1, -12, Build.st(Blocks.SAND));
        b.fragment(38, 1, -14, 16);

        // bus stop with a bench
        b.box(-42, 0, 6, -42, 2, 6, fence);
        b.box(-38, 0, 6, -38, 2, 6, fence);
        b.box(-42, 0, 10, -42, 2, 10, fence);
        b.box(-38, 0, 10, -38, 2, 10, fence);
        b.box(-42, 3, 6, -38, 3, 10, Build.st(Blocks.OAK_SLAB));
        b.set(-40, 0, 8, Build.st(Blocks.OAK_SLAB));
        b.fragment(-40, 1, 9, 17);

        // a lone door in the middle of nowhere (back to the lobby), memories, symbols
        b.door(0, 0, 30, Direction.SOUTH, Blocks.OAK_DOOR);
        b.fragment(-20, 1, -30, 15);
        b.set(-14, 0, 6, Build.st(Blocks.SMOOTH_STONE));
        b.symbol(-14, 1, 6, 9);
        b.set(30, 0, 10, Build.st(Blocks.SMOOTH_STONE));
        b.symbol(30, 1, 10, 10);

        // a signpost where you arrive, so nobody wonders what to do here
        b.set(-68, 0, 2, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.WEST));

        // the house: a table laid for dinner, a bed, a lamp and a note
        b.set(60, 0, 45, Build.st(Blocks.SPRUCE_SLAB));
        b.set(60, 1, 45, Build.st(Blocks.LANTERN));
        b.stairs(59, 0, 45, Blocks.OAK_STAIRS, Direction.WEST);
        b.stairs(61, 0, 45, Blocks.OAK_STAIRS, Direction.EAST);
        b.bed(58, 0, 46, Direction.NORTH, Blocks.WHITE_BED);
        b.set(62, 0, 44, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.WEST));
        b.set(62, 0, 46, Build.st(Blocks.POTTED_FERN));

        // hatch to a small bunker
        b.set(50, 0, -30, Build.st(Blocks.OAK_TRAPDOOR));
        Secret.build(b, 27);
    }

    static void phase(Build b, int n) {
        switch (n) {
            case 1 -> {
                for (int[] h : HOUSES) b.set(h[0], 3, h[1] - 4, Build.st(Blocks.LANTERN));
            }
            case 2 -> b.box(-20, 0, -60, 20, 0, -60, Build.st(Blocks.OAK_FENCE));
            case 3 -> b.box(60, 30, -78, 66, 36, -78, Build.st(Blocks.SEA_LANTERN));
            default -> { }
        }
    }

    static void triggers(AreaDef d) {
        for (int k = 0; k < 5; k++) {
            for (int y = 0; y <= 2; y++) Triggers.put(d.pos(-56 + 14 * k, y, 4), Triggers.seq("fields_poles", k));
        }
        Triggers.put(d.pos(-40, 0, 8), Triggers.note("lr.note.bus_stop"));
        Triggers.put(d.pos(-68, 0, 2), Triggers.note("lr.note.fields_start"));
        Triggers.put(d.pos(62, 0, 44), Triggers.note("lr.note.house"));
        Triggers.putDoor(d, 0, 0, 30, Triggers.travel("lobby"));
        Triggers.putDoor(d, 60, 0, 42, Triggers.travel("lounge"));
        Secret.triggers(d, 50, 0, -30, "fields", 50.5, 0, -27.5, 0.0F);
    }

    /** Which pole index (0..4) the note should refer to is fixed in {@link Puzzles}; kept here for reference. */
    static int poleCount() {
        return 5;
    }
}
