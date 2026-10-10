package com.liminalreverie.story;

import com.liminalreverie.entity.NpcKind;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/** Deterministic NPC dialogue. New lines unlock with progress; once exhausted the NPC alternates idle lines. */
public final class Dialogue {
    private static final List<String> MAIN_AREAS = List.of("school", "pool", "hotel", "mall", "fields");

    private Dialogue() {}

    public static int available(NpcKind k, StoryData d) {
        int entered = 0;
        int done = 0;
        for (String a : MAIN_AREAS) {
            if (d.areasEntered.contains(a)) entered++;
            if (Puzzles.areaDone(d, a)) done++;
        }
        int mem = d.memories.size();
        return switch (k) {
            case CARETAKER -> Math.min(k.lines, 1 + done);
            case CHILD -> Math.min(k.lines, 1 + entered);
            case WANDERER -> Math.min(k.lines, 1 + mem / 4);
            case SLEEPER -> 1 + (mem >= 12 ? 1 : 0) + (mem >= 20 ? 1 : 0) + (mem >= 28 ? 1 : 0);
        };
    }

    public static void talk(ServerPlayer sp, NpcKind k) {
        StoryData d = StoryData.get(sp.server);
        int n = d.talks.getOrDefault(k.id, 0);
        String key;
        if (n < available(k, d) && n < k.lines) {
            n++;
            d.talks.put(k.id, n);
            key = "lr.dlg." + k.id + "." + n;
            onLine(sp, k, n, d);
        } else {
            int idle = d.seq.merge(k.id + "_idle", 1, Integer::sum);
            key = d.stayed ? "lr.dlg." + k.id + ".stay" : "lr.dlg." + k.id + ".idle" + (1 + idle % 2);
        }
        d.lastLine.put(k.id, key);
        d.setDirty();
        Component line = Component.empty()
                .append(Component.translatable("lr.npc." + k.id).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(": "))
                .append(Component.translatable(key));
        sp.sendSystemMessage(line);
        StoryManager.finish(sp, d);
    }

    private static void onLine(ServerPlayer sp, NpcKind k, int n, StoryData d) {
        if (k == NpcKind.CARETAKER && n == 1 && d.chapter == 1) {
            d.chapter = 2;
            StoryManager.notify(sp, "lr.notify.chapter", "");
            StoryManager.giveJournalIfMissing(sp);
        }
        if (k == NpcKind.SLEEPER && n == 1 && d.chapter == 3) {
            d.chapter = 4;
            StoryManager.notify(sp, "lr.notify.chapter", "");
        }
    }
}
