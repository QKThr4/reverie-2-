package com.liminalreverie.world;

import com.liminalreverie.story.Fx;
import com.liminalreverie.entity.NpcKind;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.Puzzles;
import com.liminalreverie.story.StoryData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** The Lobby: a huge, empty, beige hotel lobby. The first safe hub. */
public final class LobbyArea {
    private static final String[] EXITS = {"school", "lounge", "pool", "hotel", "mall", "fields", "bedroom", "last_room"};

    public static final AreaDef DEF = new AreaDef("lobby", 0, true)
            .spawn(0.5, 0, -18.5, 0.0F)
            .bounds(-26, 26, -26, 26).bounds(194, 206, 194, 206)
            .phases(3, LobbyArea::phase)
            .builder(LobbyArea::build)
            .triggers(LobbyArea::triggers)
            .npcs(List.of(
                    new NpcPlacement(NpcKind.CARETAKER, 0.5, 0, 4.5, 180.0F, "caretaker", d -> true, false),
                    new NpcPlacement(NpcKind.CHILD, -18.5, 0, 12.5, 90.0F, "child", d -> d.chapter >= 3, false)))
            .ambient(() -> ModSounds.AMB_LOBBY.get())
            .fog(214, 205, 180, 110);

    private LobbyArea() {}

    private static int exitX(int i) {
        return -21 + 6 * i;
    }

    private static void couch(Build b, int x, int z, int len, Direction back) {
        for (int i = 0; i < len; i++) b.stairs(x + i, 0, z, Blocks.PURPUR_STAIRS, back);
        b.set(x + len / 2, 0, z + 2, Build.st(Blocks.SPRUCE_SLAB));
    }

    static void build(Build b) {
        BlockState wp = Pal.wallpaper();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        b.room(-24, -24, 24, 24, 9, wp, Pal.carpet(), wp);
        for (int x = -18; x <= 18; x += 6) for (int z = -18; z <= 18; z += 6) b.set(x, 9, z, Pal.light());
        for (int sx : new int[]{-12, 12}) for (int sz : new int[]{-12, 12}) b.box(sx, 0, sz, sx + 1, 8, sz + 1, wp);

        // great windows onto an impossible sky
        b.box(-20, 2, -24, 20, 6, -24, Build.st(Blocks.GLASS));
        for (int x = -20; x <= 20; x += 8) b.box(x, 2, -24, x, 6, -24, wp);

        // waiting areas
        couch(b, -8, -6, 5, Direction.NORTH);
        couch(b, 3, -6, 5, Direction.NORTH);
        couch(b, -8, 8, 5, Direction.SOUTH);
        couch(b, 3, 8, 5, Direction.SOUTH);

        // plants
        for (int x = -20; x <= 20; x += 10) b.set(x, 0, -21, Build.st(Blocks.POTTED_FERN));
        for (int sx : new int[]{-20, 20}) {
            for (int sz : new int[]{-20, 18}) {
                b.set(sx, 0, sz, leaves);
                b.set(sx, 1, sz, leaves);
            }
        }

        // doors that lead nowhere in particular
        b.door(-24, 0, 8, Direction.EAST, Blocks.OAK_DOOR);
        b.door(-24, 0, -8, Direction.EAST, Blocks.OAK_DOOR);

        // exits
        for (int i = 0; i < EXITS.length; i++) {
            int x = exitX(i);
            b.door(x, 0, 24, Direction.NORTH, i == 7 ? Blocks.IRON_DOOR : Blocks.OAK_DOOR);
            Component name = i == 7 ? Component.literal("?") : Component.translatable("lr.area." + EXITS[i]);
            b.sign(x, 3, 23, Direction.NORTH, name);
        }

        // directory panel + reception desk
        b.set(-22, 0, 0, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.EAST));
        b.set(0, 0, -8, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.SOUTH));

        // memories, a symbol, a secret
        b.fragment(-20, 1, -20, 1);
        b.fragment(20, 1, -8, 2);
        b.symbol(24, 1, -8, 0);
        b.set(22, 0, -20, leaves);
        b.set(22, 1, -20, leaves);
        Secret.build(b, 28);
    }

    static void phase(Build b, int n) {
        switch (n) {
            case 1 -> b.door(24, 0, 0, Direction.WEST, Blocks.OAK_DOOR);
            case 2 -> b.box(-24, 0, 8, -24, 1, 8, Pal.wallpaper());
            case 3 -> {
                b.box(-20, 2, -24, 20, 6, -24, Build.st(Blocks.PINK_STAINED_GLASS));
                for (int x = -20; x <= 20; x += 8) b.box(x, 2, -24, x, 6, -24, Pal.wallpaper());
                couch(b, 10, -14, 5, Direction.NORTH);
            }
            default -> { }
        }
    }

    static void triggers(AreaDef d) {
        for (int i = 0; i < EXITS.length; i++) {
            Triggers.putDoor(d, exitX(i), 0, 24, Triggers.travel(EXITS[i]));
        }
        Triggers.put(d.pos(-22, 0, 0), panel());
        Triggers.put(d.pos(0, 0, -8), Triggers.note("lr.note.lobby_desk"));
        Secret.triggers(d, 22, 0, -20, "lobby", 20.5, 0, -17.5, 0.0F);
        Secret.triggers(d, 22, 1, -20, "lobby", 20.5, 0, -17.5, 0.0F);
    }

    /** Needs the symbols found in the lobby, the pool and the mall. */
    private static Triggers.T panel() {
        return (p, l, pos) -> {
            com.liminalreverie.story.StoryManager.seen(p, "lobby_symbols");
            StoryData d = StoryData.get(p.server);
            if (d.puzzlesDone.contains("lobby_symbols")) {
                Fx.hint(p, Component.translatable("lr.msg.solved").withStyle(ChatFormatting.GRAY), true);
                return;
            }
            if (d.symbols.contains(0) && d.symbols.contains(3) && d.symbols.contains(7)) {
                l.playSound(null, pos, ModSounds.SOLVE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                Puzzles.complete(p, "lobby_symbols");
            } else {
                l.playSound(null, pos, ModSounds.WRONG.get(), SoundSource.BLOCKS, 0.6F, 0.8F);
                Fx.hint(p, Component.translatable("lr.msg.panel").withStyle(ChatFormatting.GRAY), true);
            }
        };
    }
}
