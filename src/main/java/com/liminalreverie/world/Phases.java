package com.liminalreverie.world;

import com.liminalreverie.Reverie;
import com.liminalreverie.story.StoryData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * "Wait... wasn't this different before?" Each area can have a few phases: small block edits applied while
 * the player is away. A phase advances when the player leaves after spending a while in the area.
 */
public final class Phases {
    private static final long MIN_STAY_TICKS = 20L * 90L;
    private static final Map<String, Long> ENTERED = new HashMap<>();

    private Phases() {}

    public static void markEntered(MinecraftServer s, AreaDef def) {
        ENTERED.put(def.id, s.overworld().getGameTime());
    }

    public static void onLeave(MinecraftServer s, AreaDef def) {
        Long t0 = ENTERED.get(def.id);
        if (t0 == null || def.maxPhase <= 0) return;
        if (s.overworld().getGameTime() - t0 >= MIN_STAY_TICKS) advance(s, def);
    }

    public static void advance(MinecraftServer s, AreaDef def) {
        StoryData d = StoryData.get(s);
        int cur = d.phases.getOrDefault(def.id, 0);
        if (cur >= def.maxPhase) return;
        apply(s, def, cur + 1);
        d.phases.put(def.id, cur + 1);
        d.setDirty();
    }

    /** Brings an area to at least the given phase (used by chapter-bound rooms). */
    public static void syncTo(MinecraftServer s, AreaDef def, int target) {
        StoryData d = StoryData.get(s);
        int cur = d.phases.getOrDefault(def.id, 0);
        target = Math.min(target, def.maxPhase);
        while (cur < target) {
            cur++;
            apply(s, def, cur);
        }
        if (cur != d.phases.getOrDefault(def.id, 0)) {
            d.phases.put(def.id, cur);
            d.setDirty();
        }
    }

    private static void apply(MinecraftServer s, AreaDef def, int phase) {
        ServerLevel l = Reverie.level(s);
        if (l == null) return;
        Build b = new Build(l, def);
        def.phaser.accept(b, phase);
        b.runNow();
    }
}
