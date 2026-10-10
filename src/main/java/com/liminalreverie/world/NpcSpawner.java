package com.liminalreverie.world;

import com.liminalreverie.Reverie;
import com.liminalreverie.entity.LiminalNpc;
import com.liminalreverie.story.StoryData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/**
 * Spawns each unique character at most once per (area, name) placement. The spawned UUID is stored in the
 * world's story data. Entity types come from the registry-backed NpcKind, never from raw ids.
 */
public final class NpcSpawner {
    private NpcSpawner() {}

    public static void ensure(MinecraftServer server, AreaDef def) {
        ServerLevel level = Reverie.level(server);
        if (level == null || def.npcs.isEmpty()) return;
        StoryData d = StoryData.get(server);
        for (NpcPlacement p : def.npcs) {
            String key = def.id + ":" + p.name();
            if (d.npcs.containsKey(key) || !p.cond().test(d)) continue;
            BlockPos pos = def.pos((int) Math.floor(p.x()), (int) Math.floor(p.y()), (int) Math.floor(p.z()));
            if (!level.hasChunkAt(pos)) continue; // try again on a later tick, once the chunk is loaded
            LiminalNpc npc = p.kind().type().create(level);
            if (npc == null) continue;
            npc.moveTo(def.ox + p.x(), def.oy + p.y(), def.oz + p.z(), p.yaw(), 0.0F);
            npc.setPlacement(key, d.epoch);
            d.npcs.put(key, npc.getUUID());
            level.addFreshEntity(npc);
            if (p.sleeping()) npc.startSleeping(pos);
            d.setDirty();
        }
    }
}
