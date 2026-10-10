package com.liminalreverie.story;

import com.liminalreverie.block.MemoryFragmentBlock;
import com.liminalreverie.net.CardPayload;
import com.liminalreverie.registry.ModBlocks;
import com.liminalreverie.world.Areas;
import com.liminalreverie.world.Transit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The four endings: wake, stay, remember and the secret "window". No boss fight: a few full-screen text cards (long
 * enough to read, word-wrapped), a final card that names the ending ("Ending X of 4") and says what happens next,
 * then a destination. Reaching any ending finishes the story but never locks the world.
 */
public final class Endings {
    private static final Map<String, Integer> LINES = Map.of("wake", 3, "stay", 3, "remember", 4, "window", 3);
    private static final Set<UUID> PLAYING = new HashSet<>();
    private static final int STEP = 200;

    private Endings() {}

    public static boolean valid(String id) {
        return LINES.containsKey(id);
    }

    public static Iterable<String> ids() {
        return LINES.keySet();
    }

    public static void play(ServerPlayer sp, String id) {
        Integer n = LINES.get(id);
        if (n == null || !PLAYING.add(sp.getUUID())) return;
        for (int i = 0; i < n; i++) {
            final String key = "lr.end." + id + "." + (i + 1);
            Scheduler.later(20 + STEP * i, () -> card(sp, key, STEP - 10));
        }
        Scheduler.later(20 + STEP * n, () -> card(sp, "lr.end.final." + id, 300));
        Scheduler.later(20 + STEP * n + 320, () -> finish(sp, id));
    }

    private static void card(ServerPlayer sp, String key, int ticks) {
        PacketDistributor.sendToPlayer(sp, new CardPayload(key, ticks));
    }

    private static void finish(ServerPlayer sp, String id) {
        PLAYING.remove(sp.getUUID());
        StoryData d = StoryData.get(sp.server);
        d.endings.add(id);
        d.chapter = 5;
        if (!id.equals("wake")) d.stayed = true;
        d.setDirty();
        sp.removeEffect(MobEffects.DARKNESS);
        switch (id) {
            case "wake" -> {
                Transit.toOverworld(sp);
                placeDetail(sp.serverLevel());
            }
            case "remember" -> Transit.force(sp, Areas.byId("last_room"));
            default -> Transit.force(sp, Areas.byId("fields"));
        }
        StoryManager.notify(sp, "lr.notify.ending", "");
        StoryManager.finish(sp, d);
    }

    /** The small impossible detail left behind in the normal world: a wallpaper pillar, a ceiling light, a memory. */
    private static void placeDetail(ServerLevel ow) {
        BlockPos s = ow.getSharedSpawnPos();
        int x = s.getX() + 3;
        int z = s.getZ() + 3;
        int y = ow.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        BlockState wall = ModBlocks.WALLPAPER.get().defaultBlockState();
        ow.setBlock(new BlockPos(x, y, z), wall, 3);
        ow.setBlock(new BlockPos(x, y + 1, z), wall, 3);
        ow.setBlock(new BlockPos(x, y + 2, z), ModBlocks.FLUORESCENT_LIGHT.get().defaultBlockState(), 3);
        ow.setBlock(new BlockPos(x + 1, y, z),
                ModBlocks.MEMORY_FRAGMENT.get().defaultBlockState().setValue(MemoryFragmentBlock.MEMORY, 0), 3);
    }
}
