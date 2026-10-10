package com.liminalreverie.world;

import com.liminalreverie.story.Fx;
import com.liminalreverie.registry.ModItems;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.Puzzles;
import com.liminalreverie.story.Scheduler;
import com.liminalreverie.story.StoryData;
import com.liminalreverie.story.StoryManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** The Empty Train: a stationary carriage with sunset scenery outside. Using the ticket starts the ride. */
public final class TrainArea {
    public static final AreaDef DEF = new AreaDef("train", 8, false)
            .spawn(0.5, 0, 0.5, -90.0F)
            .bounds(-44, 44, -44, 44)
            .gate(d -> Puzzles.areaDone(d, "mall"))
            .builder(TrainArea::build)
            .triggers(TrainArea::triggers)
            .npcs(List.of())
            .ambient(() -> ModSounds.AMB_SAFE.get())
            .fog(240, 175, 150, 80)
            .safe();

    private TrainArea() {}

    static void build(Build b) {
        BlockState white = Build.st(Blocks.WHITE_CONCRETE);
        b.room(-12, -2, 12, 2, 4, Build.st(Blocks.LIGHT_GRAY_CONCRETE), Build.st(Blocks.SPRUCE_PLANKS), white);
        b.box(-10, 1, -2, 10, 2, -2, Build.st(Blocks.GLASS));
        b.box(-10, 1, 2, 10, 2, 2, Build.st(Blocks.GLASS));
        for (int x : new int[]{-8, 0, 8}) b.set(x, 4, 0, Pal.light());
        for (int x = -10; x <= -2; x += 2) {
            b.stairs(x, 0, -1, Blocks.BIRCH_STAIRS, Direction.NORTH);
            b.stairs(x, 0, 1, Blocks.BIRCH_STAIRS, Direction.SOUTH);
        }
        for (int x = 2; x <= 10; x += 2) {
            b.stairs(x, 0, -1, Blocks.BIRCH_STAIRS, Direction.NORTH);
            b.stairs(x, 0, 1, Blocks.BIRCH_STAIRS, Direction.SOUTH);
        }
        b.set(9, 1, -2, Build.st(Blocks.DISPENSER));        // the ticket slot
        b.door(-12, 0, 0, Direction.EAST, Blocks.OAK_DOOR); // back to the lobby
        b.fragment(-9, 1, 0, 20);

        // scenery outside the windows
        b.box(-40, -3, -40, 40, -3, 40, Build.st(Blocks.GRASS_BLOCK));
        b.box(-40, -2, -4, 40, -2, -3, Build.st(Blocks.GRAVEL));
        for (int x = -30; x <= 30; x += 10) {
            b.box(x, -2, 8, x, 5, 8, Build.st(Blocks.OAK_FENCE));
            b.box(x - 1, 5, 8, x + 1, 5, 8, Build.st(Blocks.OAK_FENCE));
        }
    }

    static void triggers(AreaDef d) {
        Triggers.putDoor(d, -12, 0, 0, Triggers.travel("lobby"));
        Triggers.put(d.pos(9, 1, -2), slot());
    }

    private static Triggers.T slot() {
        return (p, l, pos) -> {
            StoryData d = StoryData.get(p.server);
            StoryManager.seen(p, "train_ticket");
            if (d.puzzlesDone.contains("train_ticket")) {
                ride(p);
                return;
            }
            ItemStack held = p.getMainHandItem();
            if (held.is(ModItems.FADED_TICKET.get())) {
                held.shrink(1);
                l.playSound(null, pos, ModSounds.SOLVE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                Puzzles.complete(p, "train_ticket");
            } else {
                l.playSound(null, pos, ModSounds.WRONG.get(), SoundSource.BLOCKS, 0.6F, 0.8F);
                Fx.hint(p, Component.translatable("lr.msg.need_ticket").withStyle(ChatFormatting.GRAY), true);
            }
        };
    }

    /** A short, calm ride: the world fades, three lines appear, and the doors open on the fields. */
    public static void ride(ServerPlayer p) {
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 260, 0, false, false, false));
        for (int i = 1; i <= 3; i++) {
            final int n = i;
            Scheduler.later(20 + 60 * (i - 1), () -> Fx.hint(p, 
                    Component.translatable("lr.msg.ride." + n).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true));
        }
        Scheduler.later(230, () -> Transit.force(p, Areas.byId("fields")));
    }
}
