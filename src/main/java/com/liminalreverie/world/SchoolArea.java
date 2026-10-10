package com.liminalreverie.world;

import com.liminalreverie.entity.NpcKind;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.Puzzles;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** The Empty School: a very long corridor, twelve classrooms, lockers, a gym, a library and a cafeteria. */
public final class SchoolArea {
    private static final int CORRECT_ROOM = 9;

    public static final AreaDef DEF = new AreaDef("school", 1, true)
            .spawn(2.5, 0, 0.5, -90.0F)
            .bounds(-2, 164, -36, 36).bounds(194, 206, 194, 206)
            .phases(3, SchoolArea::phase)
            .gate(d -> d.chapter >= 2)
            .builder(SchoolArea::build)
            .triggers(SchoolArea::triggers)
            .npcs(List.of(new NpcPlacement(NpcKind.CHILD, 9.5, 0, -9.5, 0.0F, "child", d -> d.chapter >= 2, false)))
            .ambient(() -> ModSounds.AMB_SCHOOL.get())
            .fog(226, 222, 204, 96);

    private SchoolArea() {}

    private static void classroom(Build b, int x0, int s, int num) {
        BlockState white = Build.st(Blocks.WHITE_CONCRETE);
        BlockState floor = Build.st(Blocks.LIGHT_GRAY_TERRACOTTA);
        int za = s < 0 ? -17 : 4;
        int zb = s < 0 ? -4 : 17;
        Direction toCorridor = s < 0 ? Direction.SOUTH : Direction.NORTH;
        b.room(x0, za, x0 + 10, zb, 6, white, floor, white);
        b.box(x0 + 2, 1, 16 * s, x0 + 8, 3, 16 * s, Build.st(Blocks.GREEN_TERRACOTTA));
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 4; c++) {
                int x = x0 + 2 + 2 * c;
                b.set(x, 0, s * (8 + 2 * r), Build.st(Blocks.OAK_SLAB));
                b.stairs(x, 0, s * (7 + 2 * r), Blocks.OAK_STAIRS, toCorridor);
            }
        }
        b.set(x0 + 5, 0, 14 * s, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, toCorridor));
        b.set(x0 + 3, 6, 10 * s, Pal.light());
        b.set(x0 + 7, 6, 10 * s, Pal.light());
        b.sign(x0 + 6, 2, s < 0 ? -3 : 3, toCorridor, Component.literal(String.valueOf(num)));
    }

    static void build(Build b) {
        BlockState white = Build.st(Blocks.WHITE_CONCRETE);
        BlockState blue = Build.st(Blocks.LIGHT_BLUE_CONCRETE);
        BlockState floor = Build.st(Blocks.LIGHT_GRAY_TERRACOTTA);

        // main corridor
        b.room(0, -4, 90, 4, 6, white, floor, white);
        for (int x = 3; x <= 87; x += 6) b.set(x, 6, 0, Pal.light());
        for (int i = 0; i < 6; i++) {
            classroom(b, 4 + 14 * i, -1, i + 1);
            classroom(b, 4 + 14 * i, 1, 7 + i);
        }
        b.box(0, 0, -4, 90, 1, -4, blue);
        b.box(0, 0, 4, 90, 1, 4, blue);
        for (int i = 0; i < 6; i++) {
            int x0 = 4 + 14 * i;
            b.air(x0 + 5, 0, -4, x0 + 5, 2, -4);
            b.air(x0 + 5, 0, 4, x0 + 5, 2, 4);
        }
        for (int x = 11; x <= 22; x++) {
            b.box(x, 0, -3, x, 2, -3, Build.st(x % 2 == 0 ? Blocks.CYAN_TERRACOTTA : Blocks.LIGHT_BLUE_TERRACOTTA));
        }
        b.door(0, 0, 0, Direction.EAST, Blocks.OAK_DOOR);

        // gym
        b.room(90, -16, 130, 16, 12, white, Build.st(Blocks.SPRUCE_PLANKS), white);
        for (int x = 96; x <= 124; x += 8) for (int z = -8; z <= 8; z += 8) b.set(x, 12, z, Pal.light());
        b.box(92, 0, 0, 92, 3, 0, Build.st(Blocks.IRON_BARS));
        b.box(128, 0, 0, 128, 3, 0, Build.st(Blocks.IRON_BARS));

        // library (south of the gym) and cafeteria (north of it)
        b.room(100, 16, 126, 33, 7, white, Build.st(Blocks.DARK_OAK_PLANKS), white);
        for (int z = 20; z <= 28; z += 4) {
            b.box(102, 0, z, 110, 3, z, Build.st(Blocks.BOOKSHELF));
            b.box(116, 0, z, 124, 3, z, Build.st(Blocks.BOOKSHELF));
        }
        for (int x = 106; x <= 120; x += 14) for (int z = 20; z <= 28; z += 8) b.set(x, 7, z, Pal.light());
        b.set(113, 0, 31, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.NORTH));

        b.room(100, -33, 126, -16, 7, white, Build.st(Blocks.BIRCH_PLANKS), white);
        for (int x = 104; x <= 122; x += 6) {
            for (int z = -30; z <= -22; z += 4) {
                b.set(x, 0, z, Build.st(Blocks.SPRUCE_SLAB));
                b.set(x + 1, 0, z, Build.st(Blocks.SPRUCE_SLAB));
                b.stairs(x, 0, z - 1, Blocks.SPRUCE_STAIRS, Direction.NORTH);
                b.stairs(x, 0, z + 1, Blocks.SPRUCE_STAIRS, Direction.SOUTH);
            }
        }
        for (int x = 106; x <= 120; x += 14) for (int z = -28; z <= -20; z += 8) b.set(x, 7, z, Pal.light());

        // hall to the pool
        b.room(130, -2, 160, 2, 5, white, floor, white);
        b.set(145, 5, 0, Pal.light());
        b.door(160, 0, 0, Direction.WEST, Blocks.OAK_DOOR);

        // doorways (after all walls exist)
        b.air(90, 0, -1, 90, 3, 1);
        b.air(130, 0, -1, 130, 3, 1);
        b.air(112, 0, 16, 114, 2, 16);
        b.air(112, 0, -16, 114, 2, -16);

        // memories and symbols
        b.fragment(69, 1, 15, 3);
        b.fragment(124, 1, 31, 4);
        b.fragment(124, 1, -31, 5);
        b.symbol(13, 1, -16, 1);
        b.symbol(110, 1, -15, 2);

        // secret reading room behind a bookshelf
        for (int y = 0; y <= 3; y++) b.set(102, y, 28, Build.st(Blocks.BOOKSHELF));
        Secret.build(b, 24);
    }

    static void phase(Build b, int n) {
        switch (n) {
            case 1 -> {
                b.air(34, 0, -12, 40, 0, -8);
                b.set(37, 0, -10, Build.st(Blocks.OAK_SLAB));
            }
            case 2 -> {
                for (int x = 6; x <= 84; x += 12) b.set(x, 0, 2, Build.st(Blocks.POTTED_FERN));
            }
            case 3 -> {
                b.box(95, 5, -16, 125, 8, -16, Build.st(Blocks.YELLOW_STAINED_GLASS));
                b.box(95, 5, 16, 125, 8, 16, Build.st(Blocks.YELLOW_STAINED_GLASS));
            }
            default -> { }
        }
    }

    static void triggers(AreaDef d) {
        for (int i = 0; i < 6; i++) {
            int x0 = 4 + 14 * i;
            Triggers.put(d.pos(x0 + 5, 0, -14), classroomTrigger(i + 1));
            Triggers.put(d.pos(x0 + 5, 0, 14), classroomTrigger(7 + i));
        }
        for (int x = 11; x <= 22; x++) {
            for (int y = 0; y <= 2; y++) Triggers.put(d.pos(x, y, -3), Triggers.seq("school_lockers", x - 11));
        }
        Triggers.put(d.pos(113, 0, 31), Triggers.note("lr.note.library"));
        Triggers.putDoor(d, 0, 0, 0, Triggers.travel("lobby"));
        Triggers.putDoor(d, 160, 0, 0, Triggers.travel("pool"));
        for (int y = 0; y <= 3; y++) {
            Secret.triggers(d, 102, y, 28, "school", 106.5, 0, 22.5, 0.0F);
        }
    }

    private static Triggers.T classroomTrigger(int num) {
        return num == CORRECT_ROOM
                ? Triggers.single("school_classroom")
                : Triggers.note("lr.note.class_wrong." + (1 + num % 3));
    }
}
