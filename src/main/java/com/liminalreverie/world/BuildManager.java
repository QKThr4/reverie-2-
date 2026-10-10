package com.liminalreverie.world;

import com.liminalreverie.Reverie;
import com.liminalreverie.story.StoryData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Builds areas lazily, once, a limited number of blocks per tick. Players waiting for an area see the world
 * "fade" (Darkness) and are teleported in when it is ready. An interrupted build is simply restarted next
 * time: it is only marked as built when every step has finished, and all steps are idempotent.
 */
public final class BuildManager {
    private static final int BLOCKS_PER_TICK = 5000;

    private static final class Job {
        final AreaDef def;
        final List<Build.Step> steps;
        final Set<UUID> waiting = new LinkedHashSet<>();
        final List<int[]> forced = new ArrayList<>();
        int index = 0;

        Job(AreaDef def, List<Build.Step> steps) {
            this.def = def;
            this.steps = steps;
        }
    }

    private static final Map<String, Job> JOBS = new LinkedHashMap<>();

    private BuildManager() {}

    public static boolean isBuilding(String areaId) {
        return JOBS.containsKey(areaId);
    }

    public static void request(ServerPlayer p, AreaDef def) {
        ServerLevel rl = Reverie.level(p.server);
        if (rl == null) return;
        Job job = JOBS.get(def.id);
        if (job == null) {
            Build b = new Build(rl, def);
            def.builder.accept(b);
            job = new Job(def, b.steps());
            for (int[] bd : def.bounds) {
                for (int cx = (def.ox + bd[0]) >> 4; cx <= (def.ox + bd[1]) >> 4; cx++) {
                    for (int cz = (def.oz + bd[2]) >> 4; cz <= (def.oz + bd[3]) >> 4; cz++) {
                        rl.setChunkForced(cx, cz, true);
                        job.forced.add(new int[]{cx, cz});
                    }
                }
            }
            JOBS.put(def.id, job);
        }
        job.waiting.add(p.getUUID());
        hold(p);
    }

    private static void hold(ServerPlayer p) {
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false, false));
        p.displayClientMessage(Component.translatable("lr.msg.forming").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
    }

    public static void tick(MinecraftServer server) {
        if (JOBS.isEmpty()) return;
        ServerLevel rl = Reverie.level(server);
        if (rl == null) {
            JOBS.clear();
            return;
        }
        int[] budget = {BLOCKS_PER_TICK};
        boolean second = server.getTickCount() % 20 == 0;
        Iterator<Job> it = JOBS.values().iterator();
        while (it.hasNext()) {
            Job job = it.next();
            while (job.index < job.steps.size() && budget[0] > 0) {
                if (job.steps.get(job.index).run(rl, budget)) job.index++;
            }
            if (job.index >= job.steps.size()) {
                finish(server, rl, job);
                it.remove();
            } else if (second) {
                for (UUID id : job.waiting) {
                    ServerPlayer sp = server.getPlayerList().getPlayer(id);
                    if (sp != null) hold(sp);
                }
            }
        }
    }

    private static void finish(MinecraftServer server, ServerLevel rl, Job job) {
        for (int[] c : job.forced) rl.setChunkForced(c[0], c[1], false);
        StoryData d = StoryData.get(server);
        d.areasBuilt.add(job.def.id);
        d.setDirty();
        for (UUID id : job.waiting) {
            ServerPlayer sp = server.getPlayerList().getPlayer(id);
            if (sp != null) Transit.arrive(sp, job.def);
        }
    }

    public static void abortAll(MinecraftServer server) {
        ServerLevel rl = Reverie.level(server);
        if (rl != null) {
            for (Job j : JOBS.values()) for (int[] c : j.forced) rl.setChunkForced(c[0], c[1], false);
        }
        JOBS.clear();
    }
}
