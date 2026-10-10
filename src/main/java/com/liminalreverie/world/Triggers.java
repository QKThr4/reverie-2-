package com.liminalreverie.world;

import com.liminalreverie.story.Fx;
import com.liminalreverie.Reverie;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.Puzzles;
import com.liminalreverie.story.StoryData;
import com.liminalreverie.story.StoryManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Right-click interactions for placed blocks (doors, puzzle objects, notes). Triggers are registered by absolute
 * block position, computed deterministically from each area's constants, so nothing here needs saving.
 */
public final class Triggers {
    @FunctionalInterface
    public interface T {
        void use(ServerPlayer p, ServerLevel level, BlockPos pos);
    }

    private static final Map<Long, T> MAP = new HashMap<>();
    private static boolean ready = false;

    private Triggers() {}

    public static synchronized T get(BlockPos pos) {
        if (!ready) {
            ready = true;
            for (AreaDef a : Areas.ALL) a.triggerFn.accept(a);
        }
        return MAP.get(pos.asLong());
    }

    public static void put(BlockPos pos, T t) {
        MAP.put(pos.asLong(), t);
    }

    /** Registers the same trigger on both halves of a door. */
    public static void putDoor(AreaDef def, int x, int y, int z, T t) {
        put(def.pos(x, y, z), t);
        put(def.pos(x, y + 1, z), t);
    }

    // ------------------------------------------------------------------ factories

    public static T travel(String destId) {
        return (p, l, pos) -> Transit.enter(p, Areas.byId(destId));
    }

    /** A door that returns to wherever the player came from (so side rooms never dump you somewhere odd). */
    public static T back(String from, String fallback) {
        return (p, l, pos) -> Transit.enter(p, Areas.byId(Transit.back(p, from, fallback)));
    }

    public static T hop(AreaDef def, double x, double y, double z, float yaw) {
        return (p, l, pos) -> Transit.hop(p, def, x, y, z, yaw);
    }

    public static T hopIf(AreaDef def, double x, double y, double z, float yaw,
                          Predicate<StoryData> cond, String lockedKey) {
        return (p, l, pos) -> {
            if (cond.test(StoryData.get(p.server))) {
                Transit.hop(p, def, x, y, z, yaw);
            } else {
                Fx.hint(p, Component.translatable(lockedKey).withStyle(ChatFormatting.GRAY), true);
                l.playSound(null, pos, ModSounds.WRONG.get(), SoundSource.BLOCKS, 0.5F, 0.7F);
            }
        };
    }

    public static T note(String key) {
        return (p, l, pos) -> StoryManager.note(p, key);
    }

    /** A puzzle solved by one correct interaction. */
    public static T single(String puzzleId) {
        return (p, l, pos) -> {
            StoryManager.seen(p, puzzleId);
            if (StoryData.get(p.server).puzzlesDone.contains(puzzleId)) {
                Fx.hint(p, Component.translatable("lr.msg.solved").withStyle(ChatFormatting.GRAY), true);
                return;
            }
            l.playSound(null, pos, ModSounds.SOLVE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            Puzzles.complete(p, puzzleId);
        };
    }

    public static T seq(String puzzleId, int index) {
        return (p, l, pos) -> Puzzles.stepSeq(p, l, pos, puzzleId, index);
    }

    public static boolean inDream(ServerLevel l) {
        return Reverie.in(l);
    }
}
