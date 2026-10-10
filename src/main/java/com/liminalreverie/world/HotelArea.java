package com.liminalreverie.world;

import com.liminalreverie.story.Fx;
import com.liminalreverie.entity.NpcKind;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.Puzzles;
import com.liminalreverie.story.StoryData;
import com.liminalreverie.story.StoryManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** The Hotel: a long corridor of rooms, some of them impossible, and a hallway that leads back to itself. */
public final class HotelArea {
    public static final AreaDef DEF = new AreaDef("hotel", 3, true)
            .spawn(2.5, 0, 0.5, -90.0F)
            .bounds(-2, 152, -14, 14).bounds(8, 172, 34, 46).bounds(194, 206, 194, 206)
            .phases(3, HotelArea::phase)
            .gate(d -> Puzzles.areaDone(d, "pool"))
            .builder(HotelArea::build)
            .triggers(HotelArea::triggers)
            .npcs(List.of(new NpcPlacement(NpcKind.WANDERER, 19.5, 0, -6.5, 0.0F, "wanderer", d -> d.chapter >= 2, false)))
            .ambient(() -> ModSounds.AMB_HOTEL.get())
            .fog(205, 185, 185, 90);

    private HotelArea() {}

    /** The floating-furniture room (relative coordinates) where gravity is gentle. */
    public static boolean inFloatRoom(BlockPos rel) {
        return rel.getX() >= 107 && rel.getX() <= 110 && rel.getZ() >= 4 && rel.getZ() <= 10
                && rel.getY() >= -1 && rel.getY() <= 8;
    }

    private static void hotelRoom(Build b, int x0, int s, int number) {
        BlockState wall = Build.st(Blocks.PINK_TERRACOTTA);
        int za = s < 0 ? -11 : 3;
        int zb = s < 0 ? -3 : 11;
        Direction toCorridor = s < 0 ? Direction.SOUTH : Direction.NORTH;
        Direction toBack = s < 0 ? Direction.NORTH : Direction.SOUTH;
        b.room(x0, za, x0 + 5, zb, 5, wall, Pal.carpet(), wall);
        b.bed(x0 + 2, 0, s * 9, toBack, Blocks.WHITE_BED);
        b.set(x0 + 1, 0, s * 10, Build.st(Blocks.OAK_SLAB));
        b.set(x0 + 1, 1, s * 10, Build.st(Blocks.LANTERN));
        b.set(x0 + 2, 5, s * 6, Pal.light());
        b.air(x0 + 2, 0, s * 3, x0 + 2, 2, s * 3);
        b.sign(x0 + 2, 3, s < 0 ? -2 : 2, toCorridor, Component.literal(String.valueOf(number)));
    }

    static void build(Build b) {
        BlockState wall = Build.st(Blocks.PINK_TERRACOTTA);
        b.room(0, -3, 150, 3, 6, wall, Pal.carpet(), wall);
        for (int x = 3; x <= 147; x += 6) b.set(x, 6, 0, Pal.light());
        for (int i = 0; i < 24; i++) {
            int x0 = 4 + 6 * i;
            hotelRoom(b, x0, -1, 201 + i);
            hotelRoom(b, x0, 1, 101 + i);
        }

        // --- impossible rooms ---
        // a bedroom inside a bathroom (north, i=5, x0=34)
        b.box(35, -1, -10, 38, -1, -4, Build.st(Blocks.WHITE_CONCRETE));
        b.box(35, 0, -10, 35, 2, -4, Build.st(Blocks.WHITE_CONCRETE));
        b.set(38, 0, -9, Build.st(Blocks.CAULDRON));
        b.set(36, 0, -6, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.SOUTH));
        // a swimming pool inside a bedroom (south, i=9, x0=58)
        b.box(60, -2, 6, 61, -2, 9, Pal.poolTile());
        b.box(60, -1, 6, 61, -1, 9, Build.st(Blocks.WATER));
        // a miniature lobby (north, i=13, x0=82)
        b.stairs(84, 0, -6, Blocks.PURPUR_STAIRS, Direction.NORTH);
        b.stairs(85, 0, -6, Blocks.PURPUR_STAIRS, Direction.NORTH);
        b.set(86, 0, -10, Build.st(Blocks.POTTED_FERN));
        b.box(84, 2, -11, 85, 3, -11, Build.st(Blocks.GLASS));
        b.box(83, 0, -10, 83, 3, -10, Pal.wallpaper());
        b.symbol(82, 1, -7, 6);
        // floating furniture (south, i=17, x0=106)
        b.bed(108, 2, 6, Direction.SOUTH, Blocks.WHITE_BED);
        b.stairs(110, 3, 5, Blocks.OAK_STAIRS, Direction.EAST);
        b.set(107, 4, 9, Build.st(Blocks.LANTERN));
        b.fragment(108, 4, 9, 10);
        // loop entrance (north, i=21, x0=130)
        b.door(132, 0, -11, Direction.SOUTH, Blocks.DARK_OAK_DOOR);
        // secret bookshelf (north, i=3, x0=22)
        b.set(23, 0, -10, Build.st(Blocks.BOOKSHELF));
        b.set(23, 1, -10, Build.st(Blocks.BOOKSHELF));
        Secret.build(b, 25);

        // --- corridor ends ---
        b.door(0, 0, 2, Direction.EAST, Blocks.OAK_DOOR);          // back to the lobby
        b.door(0, 0, -2, Direction.EAST, Blocks.OAK_DOOR);         // stairs to the rooftop
        b.door(150, 0, 0, Direction.WEST, Blocks.IRON_DOOR);       // elevator to the mall
        BlockState button = Blocks.OAK_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.SOUTH);
        for (int k = 0; k < 4; k++) {
            b.set(145 + k, 1, -2, button);
            b.sign(145 + k, 2, -2, Direction.SOUTH, Component.literal(String.valueOf(k + 1)));
        }
        b.fragment(12, 1, 8, 9);
        b.fragment(148, 1, 0, 11);
        b.symbol(75, 1, -3, 5);

        // --- the hallway that leads back to itself: three identical stations ---
        for (int s = 0; s < 3; s++) {
            int sx = 10 + 50 * s;
            b.room(sx, 37, sx + 16, 43, 5, Build.st(Blocks.PURPLE_TERRACOTTA), Pal.carpet(), Build.st(Blocks.PURPLE_TERRACOTTA));
            b.set(sx + 4, 5, 40, Pal.light());
            b.set(sx + 12, 5, 40, Pal.light());
            b.door(sx + 16, 0, 39, Direction.WEST, Blocks.OAK_DOOR);
            b.door(sx + 16, 0, 41, Direction.WEST, Blocks.OAK_DOOR);
        }
        b.door(10, 0, 40, Direction.EAST, Blocks.OAK_DOOR);
        b.set(12, 0, 38, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.SOUTH));
    }

    static void phase(Build b, int n) {
        switch (n) {
            case 1 -> {
                for (int x : new int[]{15, 45, 75, 105}) b.set(x, 6, 0, Build.st(Blocks.PINK_TERRACOTTA));
            }
            case 2 -> {
                for (int x = 12; x <= 140; x += 16) b.set(x, 0, -2, Build.st(Blocks.POTTED_FERN));
            }
            case 3 -> {
                for (int i = 0; i < 4; i++) b.air(4 + 6 * i + 2, 0, -10, 4 + 6 * i + 2, 0, -9);
            }
            default -> { }
        }
    }

    private static Triggers.T loopDoor(AreaDef def, int station, boolean right) {
        return (p, l, pos) -> {
            StoryData d = StoryData.get(p.server);
            StoryManager.seen(p, "hotel_loop");
            if (d.puzzlesDone.contains("hotel_loop")) {
                Fx.hint(p, Component.translatable("lr.msg.solved").withStyle(ChatFormatting.GRAY), true);
                return;
            }
            int expected = Puzzles.get("hotel_loop").seq()[station];
            if ((right ? 1 : 0) == expected) {
                if (station == 2) {
                    Transit.hop(p, def, 125.5, 0, 0.5, -90.0F);
                    Puzzles.complete(p, "hotel_loop");
                    Fx.attached(p, ModSounds.SOLVE.get(), 1.0F, 1.0F);
                } else {
                    Transit.hop(p, def, 10 + 50 * (station + 1) + 2.5, 0, 40.5, -90.0F);
                    Fx.attached(p, ModSounds.CHIME.get(), 1.0F, 0.9F + 0.1F * station);
                }
            } else {
                Transit.hop(p, def, 12.5, 0, 40.5, -90.0F);
                Fx.attached(p, ModSounds.WRONG.get(), 1.0F, 0.7F);
                Fx.hint(p, Component.translatable("lr.msg.loop").withStyle(ChatFormatting.GRAY), true);
            }
        };
    }

    static void triggers(AreaDef d) {
        Triggers.putDoor(d, 0, 0, 2, Triggers.travel("lobby"));
        Triggers.putDoor(d, 0, 0, -2, Triggers.travel("rooftop"));
        Triggers.putDoor(d, 150, 0, 0, Triggers.travel("mall"));
        for (int k = 0; k < 4; k++) Triggers.put(d.pos(145 + k, 1, -2), Triggers.seq("hotel_elevator", k));
        Triggers.put(d.pos(36, 0, -6), Triggers.note("lr.note.hotel_elevator"));
        Triggers.put(d.pos(12, 0, 38), Triggers.note("lr.note.hotel_loop"));
        Triggers.putDoor(d, 132, 0, -11, Triggers.hop(d, 12.5, 0, 40.5, -90.0F));
        Triggers.putDoor(d, 10, 0, 40, Triggers.hop(d, 131.5, 0, -8.5, 0.0F));
        for (int s = 0; s < 3; s++) {
            Triggers.putDoor(d, 10 + 50 * s + 16, 0, 39, loopDoor(d, s, false));
            Triggers.putDoor(d, 10 + 50 * s + 16, 0, 41, loopDoor(d, s, true));
        }
        for (int y = 0; y <= 1; y++) Secret.triggers(d, 23, y, -10, "hotel", 24.5, 0, -6.5, 0.0F);
    }
}
