package com.liminalreverie.world;

import com.liminalreverie.story.Fx;
import com.liminalreverie.registry.ModItems;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.Puzzles;
import com.liminalreverie.story.StoryData;
import com.liminalreverie.story.StoryManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** The Endless Mall: atrium, stores, an arcade, a cinema and a parking garage that goes on a little too long. */
public final class MallArea {
    public static final AreaDef DEF = new AreaDef("mall", 4, true)
            .spawn(0.5, 0, 34.5, 180.0F)
            .bounds(-62, 62, -42, 102).bounds(194, 206, 194, 206)
            .phases(3, MallArea::phase)
            .gate(d -> Puzzles.areaDone(d, "hotel"))
            .builder(MallArea::build)
            .triggers(MallArea::triggers)
            .npcs(List.of())
            .ambient(() -> ModSounds.AMB_MALL.get())
            .fog(232, 224, 214, 110);

    private MallArea() {}

    static void build(Build b) {
        BlockState white = Build.st(Blocks.WHITE_CONCRETE);
        BlockState tile = Pal.mallTile();
        BlockState glass = Build.st(Blocks.GLASS);
        BlockState gray = Build.st(Blocks.GRAY_CONCRETE);

        // atrium
        b.room(-40, -40, 40, 40, 14, white, tile, white);
        for (int x = -30; x <= 30; x += 10) for (int z = -30; z <= 30; z += 10) b.set(x, 14, z, Pal.light());
        b.box(-4, 14, -4, 4, 14, 4, glass);
        b.box(-4, 0, -4, 4, 0, 4, Build.st(Blocks.LIGHT_BLUE_CONCRETE));
        b.box(-3, 0, -3, 3, 0, 3, Build.st(Blocks.WATER));

        // balconies and escalators
        b.box(-39, 5, -31, -33, 5, 31, tile);
        b.box(33, 5, -31, 39, 5, 31, tile);
        for (int i = 0; i <= 5; i++) {
            for (int z = -1; z <= 1; z++) {
                b.stairs(-27 - i, i, z, Blocks.QUARTZ_STAIRS, Direction.WEST);
                b.stairs(27 + i, i, z, Blocks.QUARTZ_STAIRS, Direction.EAST);
            }
        }
        b.box(-33, 6, -31, -33, 6, -2, Build.st(Blocks.IRON_BARS));
        b.box(-33, 6, 2, -33, 6, 31, Build.st(Blocks.IRON_BARS));
        b.box(33, 6, -31, 33, 6, -2, Build.st(Blocks.IRON_BARS));
        b.box(33, 6, 2, 33, 6, 31, Build.st(Blocks.IRON_BARS));

        // food court
        for (int x : new int[]{-14, -6, 6, 14}) {
            for (int z : new int[]{14, 20, 26}) {
                b.set(x, 0, z, Build.st(Blocks.BIRCH_SLAB));
                b.stairs(x - 1, 0, z, Blocks.BIRCH_STAIRS, Direction.WEST);
                b.stairs(x + 1, 0, z, Blocks.BIRCH_STAIRS, Direction.EAST);
            }
        }
        b.fragment(-15, 1, 25, 13);

        // stores along the north wall (store 4 is closed and needs the brass key)
        for (int i = 0; i < 8; i++) {
            int x0 = -36 + 9 * i;
            b.room(x0, -40, x0 + 8, -32, 6, Build.st(Blocks.QUARTZ_BLOCK), tile, white);
            b.set(x0 + 4, 6, -36, Pal.light());
            b.box(x0 + 1, 0, -32, x0 + 7, 3, -32, glass);
            if (i == 4) {
                b.door(x0 + 4, 0, -32, Direction.SOUTH, Blocks.IRON_DOOR);
                b.set(x0 + 2, 0, -38, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.SOUTH));
                b.set(x0 + 6, 0, -38, Build.st(Blocks.CAULDRON));
                b.set(x0 + 4, 0, -36, Build.st(Blocks.OAK_SLAB));
            } else {
                b.air(x0 + 3, 0, -32, x0 + 4, 2, -32);
                b.set(x0 + 4, 0, -37, Build.st(Blocks.POTTED_FERN));
            }
        }

        // arcade (west)
        b.room(-56, -12, -40, 12, 6, gray, Build.st(Blocks.BLUE_CONCRETE), Build.st(Blocks.BLACK_CONCRETE));
        b.set(-48, 6, 0, Pal.light());
        for (int k = 0; k < 5; k++) {
            int z = -8 + 4 * k;
            b.box(-54, 0, z, -54, 2, z, Build.st(Blocks.BLACK_CONCRETE));
            b.set(-54, 1, z, Build.st(Blocks.SEA_LANTERN));
        }
        b.set(-42, 0, 0, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.EAST));
        b.symbol(-56, 1, 0, 7);
        b.set(-50, 1, -12, Build.st(Blocks.IRON_BARS));
        Secret.build(b, 26);

        // cinema (east)
        b.room(40, -12, 56, 12, 8, Build.st(Blocks.PURPLE_TERRACOTTA), Pal.carpet(), Build.st(Blocks.BLACK_CONCRETE));
        b.box(41, 2, -6, 41, 5, 6, white);
        b.set(48, 8, 0, Pal.light());
        for (int r = 0; r < 5; r++) {
            int x = 44 + 2 * r;
            b.box(x, 0, -8, x + 1, r, 8, Build.st(Blocks.BLACK_CONCRETE));
            for (int z = -6; z <= 6; z += 2) b.stairs(x, r + 1, z, Blocks.RED_NETHER_BRICK_STAIRS, Direction.EAST);
        }
        b.fragment(54, 1, 10, 12);

        // parking garage (south)
        b.room(-60, 40, 60, 100, 4, gray, Build.st(Blocks.GRAY_CONCRETE), gray);
        for (int x = -40; x <= 40; x += 20) for (int z = 50; z <= 90; z += 20) b.set(x, 4, z, Pal.light());
        for (int x = -50; x <= 50; x += 20) {
            for (int z = 50; z <= 90; z += 20) b.box(x, 0, z, x + 1, 3, z + 1, Build.st(Blocks.LIGHT_GRAY_CONCRETE));
        }
        for (int z = 45; z <= 95; z += 10) b.box(-55, -1, z, 55, -1, z, white);
        b.fragment(-55, 1, 95, 14);
        b.symbol(30, 1, 100, 8);

        // doorways and doors in shared walls
        b.air(-40, 0, -1, -40, 2, 1);
        b.air(40, 0, -1, 40, 2, 1);
        b.air(-1, 0, 40, 1, 2, 40);
        b.door(-10, 0, 40, Direction.NORTH, Blocks.OAK_DOOR);   // back to the lobby
        b.door(10, 0, 40, Direction.NORTH, Blocks.IRON_DOOR);   // elevator back up to the hotel
        b.door(0, 0, 100, Direction.NORTH, Blocks.OAK_DOOR);    // to the train
    }

    /** Called when the key opens store 4. */
    public static void openShop(ServerLevel level) {
        Build b = new Build(level, DEF);
        b.air(4, 0, -32, 4, 1, -32);
        b.runNow();
    }

    static void phase(Build b, int n) {
        switch (n) {
            case 1 -> {
                for (int x = -30; x <= 30; x += 10) b.set(x, 0, -31, Build.st(Blocks.POTTED_FERN));
            }
            case 2 -> b.box(-3, 0, -3, 3, 0, 3, Build.AIR);
            case 3 -> b.box(-4, 14, -4, 4, 14, 4, Build.st(Blocks.ORANGE_STAINED_GLASS));
            default -> { }
        }
    }

    private static Triggers.T shopDoor() {
        return (p, l, pos) -> {
            StoryData d = StoryData.get(p.server);
            StoryManager.seen(p, "mall_shop");
            if (d.puzzlesDone.contains("mall_shop")) {
                Fx.hint(p, Component.translatable("lr.msg.solved").withStyle(ChatFormatting.GRAY), true);
                return;
            }
            ItemStack held = p.getMainHandItem();
            if (held.is(ModItems.BRASS_KEY.get())) {
                held.shrink(1);
                l.playSound(null, pos, ModSounds.SOLVE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                Puzzles.complete(p, "mall_shop");
            } else {
                l.playSound(null, pos, ModSounds.WRONG.get(), SoundSource.BLOCKS, 0.6F, 0.8F);
                Fx.hint(p, Component.translatable("lr.msg.need_key").withStyle(ChatFormatting.GRAY), true);
            }
        };
    }

    static void triggers(AreaDef d) {
        Triggers.putDoor(d, -10, 0, 40, Triggers.travel("lobby"));
        Triggers.putDoor(d, 10, 0, 40, Triggers.travel("hotel"));
        Triggers.putDoor(d, 0, 0, 100, Triggers.travel("train"));
        Triggers.putDoor(d, 4, 0, -32, shopDoor());
        for (int k = 0; k < 5; k++) Triggers.put(d.pos(-54, 1, -8 + 4 * k), Triggers.seq("mall_arcade", k));
        Triggers.put(d.pos(-42, 0, 0), Triggers.note("lr.note.arcade"));
        Secret.triggers(d, -50, 1, -12, "mall", -50.5, 0, -9.5, 180.0F);
    }
}
