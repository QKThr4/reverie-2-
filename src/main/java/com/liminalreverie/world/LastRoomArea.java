package com.liminalreverie.world;

import com.liminalreverie.entity.NpcKind;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.Endings;
import com.liminalreverie.story.StoryData;
import com.liminalreverie.story.StoryManager;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * The Last Room: a completely ordinary bedroom. A window, a bed, a desk, a clock and one door.
 * Door = Wake Up. Bed = Stay. Desk = Remember. Window = the secret ending.
 */
public final class LastRoomArea {
    public static final AreaDef DEF = new AreaDef("last_room", 10, false)
            .spawn(0.5, 0, 1.5, 180.0F)
            .bounds(-5, 5, -5, 5)
            .phases(1, LastRoomArea::phase)
            .gate(d -> d.chapter >= 4 || !d.endings.isEmpty())
            .builder(LastRoomArea::build)
            .triggers(LastRoomArea::triggers)
            .npcs(List.of(new NpcPlacement(NpcKind.CHILD, -1.5, 0, 1.5, 0.0F, "child", d -> d.chapter >= 4, false)))
            .ambient(() -> ModSounds.AMB_SAFE.get())
            .fog(236, 228, 214, 40)
            .safe();

    private LastRoomArea() {}

    static void build(Build b) {
        b.room(-3, -3, 3, 3, 4, Build.st(Blocks.WHITE_CONCRETE), Build.st(Blocks.OAK_PLANKS), Build.st(Blocks.WHITE_CONCRETE));
        b.set(0, 4, 0, Pal.light());
        b.bed(-2, 0, -1, Direction.NORTH, Blocks.WHITE_BED);
        b.set(2, 0, -2, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.WEST));
        b.box(-1, 1, -3, 1, 2, -3, Build.st(Blocks.GLASS));
        b.door(0, 0, 3, Direction.NORTH, Blocks.OAK_DOOR);

        // the clock: four buttons on the east wall
        BlockState button = Blocks.OAK_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.WEST);
        for (int k = 0; k < 4; k++) b.set(2, 2, -2 + k, button);

        // objects from every place the player has been
        b.set(-2, 0, 2, Pal.wallpaper());
        b.set(-1, 0, 2, Pal.carpet());
        b.set(1, 0, 2, Pal.poolTile());
        b.set(2, 0, 2, Pal.mallTile());
        b.set(-2, 1, 2, Build.st(Blocks.GRASS_BLOCK));
        b.set(2, 1, 2, Build.st(Blocks.OAK_SLAB));
        b.fragment(0, 1, -2, 32);

        // a small sign at each choice
        b.sign(1, 2, 2, Direction.NORTH, Component.translatable("lr.sign.wake"));
        b.sign(-2, 2, -1, Direction.EAST, Component.translatable("lr.sign.stay"));
        b.sign(2, 1, -2, Direction.WEST, Component.translatable("lr.sign.remember"));
        b.sign(-2, 2, -2, Direction.SOUTH, Component.translatable("lr.sign.window"));
    }

    static void phase(Build b, int n) {
        if (n == 1) b.box(-1, 1, -3, 1, 2, -3, Build.st(Blocks.ORANGE_STAINED_GLASS));
    }

    static void triggers(AreaDef d) {
        Triggers.putDoor(d, 0, 0, 3, choose("wake"));
        Triggers.put(d.pos(-2, 0, -1), choose("stay"));
        Triggers.put(d.pos(-2, 0, -2), choose("stay"));
        Triggers.put(d.pos(2, 0, -2), choose("remember"));
        for (int x = -1; x <= 1; x++) {
            for (int y = 1; y <= 2; y++) Triggers.put(d.pos(x, y, -3), choose("window"));
        }
        for (int k = 0; k < 4; k++) Triggers.put(d.pos(2, 2, -2 + k), Triggers.seq("last_clock", k));
    }

    private static Triggers.T choose(String ending) {
        return (p, l, pos) -> {
            StoryData d = StoryData.get(p.server);
            boolean clock = d.puzzlesDone.contains("last_clock");
            boolean ok = switch (ending) {
                case "remember" -> d.memories.size() >= 26 && clock;
                case "window" -> d.symbols.size() >= 12 && clock;
                default -> true;
            };
            if (!ok) {
                StoryManager.note(p, "lr.note.last_" + ending + "_locked");
                return;
            }
            Endings.play(p, ending);
        };
    }
}
