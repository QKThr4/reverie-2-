package com.liminalreverie.story;

import com.liminalreverie.entity.NpcKind;
import com.liminalreverie.net.NotifyPayload;
import com.liminalreverie.net.SyncPayload;
import com.liminalreverie.registry.ModItems;
import com.liminalreverie.world.AreaDef;
import com.liminalreverie.world.Areas;
import com.liminalreverie.world.NpcSpawner;
import com.liminalreverie.world.Phases;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Set;

/** Server-authoritative story operations. Every mutation saves, re-checks chapters and syncs the journal. */
public final class StoryManager {
    private static final List<String> MAIN_AREAS = List.of("school", "pool", "hotel", "mall", "fields");
    private static final int SECRET_ROOMS = 6;
    private static final int HIDDEN_EVENTS = 8;

    private StoryManager() {}

    public static StoryData data(MinecraftServer s) {
        return StoryData.get(s);
    }

    // ---------------------------------------------------------------- messaging

    public static void notify(ServerPlayer sp, String key, String arg) {
        PacketDistributor.sendToPlayer(sp, new NotifyPayload(key, arg));
    }

    public static void sendJournal(ServerPlayer sp, boolean open) {
        CompoundTag snap = snapshot(data(sp.server));
        snap.putBoolean("entered", canOpenJournal(sp));
        PacketDistributor.sendToPlayer(sp, new SyncPayload(snap.toString(), open));
    }

    /** The journal only opens for players who have entered the Reverie at least once (operators can always open it). */
    public static boolean canOpenJournal(ServerPlayer sp) {
        return sp.hasPermissions(2) || data(sp.server).enteredPlayers.contains(sp.getUUID());
    }

    public static void note(ServerPlayer sp, String key) {
        sp.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    public static void giveItem(ServerPlayer sp, ItemStack stack) {
        if (!sp.getInventory().add(stack)) sp.drop(stack, false);
    }

    public static void giveJournalIfMissing(ServerPlayer sp) {
        if (!sp.getInventory().contains(new ItemStack(ModItems.JOURNAL.get()))) {
            giveItem(sp, new ItemStack(ModItems.JOURNAL.get()));
        }
    }

    // ---------------------------------------------------------------- mutations

    /** Saves, re-evaluates chapters and pushes a fresh snapshot (does not open the screen). */
    public static void finish(ServerPlayer sp, StoryData d) {
        d.setDirty();
        if (d.chapter == 2 && mainDone(d)) {
            d.chapter = 3;
            notify(sp, "lr.notify.chapter", "");
        }
        sendJournal(sp, false);
    }

    public static boolean collectMemory(ServerPlayer sp, int id) {
        if (id < 1 || id > Memories.TOTAL) return false;
        StoryData d = data(sp.server);
        if (!d.memories.add(id)) return false;
        notify(sp, "lr.notify.memory", "");
        finish(sp, d);
        return true;
    }

    public static boolean collectSymbol(ServerPlayer sp, int sym) {
        if (sym < 0 || sym > 11) return false;
        StoryData d = data(sp.server);
        if (!d.symbols.add(sym)) return false;
        notify(sp, "lr.notify.symbol", "");
        finish(sp, d);
        return true;
    }

    public static void seen(ServerPlayer sp, String puzzleId) {
        StoryData d = data(sp.server);
        if (d.puzzlesSeen.add(puzzleId)) d.setDirty();
    }

    public static void completePuzzle(ServerPlayer sp, String id) {
        StoryData d = data(sp.server);
        d.puzzlesSeen.add(id);
        if (!d.puzzlesDone.add(id)) return;
        notify(sp, "lr.notify.puzzle", d.puzzlesDone.size() + "/" + Puzzles.ALL.size());
        finish(sp, d);
    }

    public static void addSecretRoom(ServerPlayer sp, String key) {
        StoryData d = data(sp.server);
        if (!d.secretRooms.add(key)) return;
        notify(sp, "lr.notify.secret", "");
        finish(sp, d);
    }

    public static void recordEvent(ServerPlayer sp, String id) {
        StoryData d = data(sp.server);
        if (!d.eventsSeen.add(id)) return;
        DreamEvents ev = DreamEvents.byId(id);
        if (ev != null && ev.hidden) notify(sp, "lr.notify.secret", "");
        finish(sp, d);
    }

    public static void markEntered(ServerPlayer sp, AreaDef def) {
        StoryData d = data(sp.server);
        if (d.areasEntered.add(def.id)) {
            notify(sp, "lr.notify.area", "");
            finish(sp, d);
        }
    }

    /** Called after every successful arrival in an area. */
    public static void onArrive(ServerPlayer sp, AreaDef def) {
        StoryData first = data(sp.server);
        if (first.enteredPlayers.add(sp.getUUID())) first.setDirty();
        markEntered(sp, def);
        Phases.markEntered(sp.server, def);
        if (def.id.equals("lobby")) giveJournalIfMissing(sp);
        StoryData d = data(sp.server);
        if (def.id.equals("bedroom")) Phases.syncTo(sp.server, def, Math.max(0, Math.min(3, d.chapter - 2)));
        if (def.id.equals("last_room")) Phases.syncTo(sp.server, def, d.endings.isEmpty() ? 0 : 1);
        NpcSpawner.ensure(sp.server, def);
        sendJournal(sp, false);
    }

    private static boolean mainDone(StoryData d) {
        for (String a : MAIN_AREAS) if (!Puzzles.areaDone(d, a)) return false;
        return true;
    }

    // ---------------------------------------------------------------- journal data

    /** 0 unknown, 1 locked, 2 not discovered, 3 discovered, 4 in progress, 5 completed. */
    public static int areaState(StoryData d, AreaDef a) {
        boolean entered = d.areasEntered.contains(a.id);
        if (a.id.equals("last_room") && d.chapter < 4 && !entered) return 0;
        if (!entered) return a.gate.test(d) ? 2 : 1;
        List<Puzzles.P> ps = Puzzles.forArea(a.id);
        if (ps.isEmpty()) return 3;
        int done = 0;
        boolean seen = false;
        for (Puzzles.P p : ps) {
            if (d.puzzlesDone.contains(p.id())) done++;
            if (d.puzzlesSeen.contains(p.id())) seen = true;
        }
        if (done == ps.size()) return 5;
        return (done > 0 || seen) ? 4 : 3;
    }

    private static int hiddenEvents(StoryData d) {
        int n = 0;
        for (String id : d.eventsSeen) {
            DreamEvents ev = DreamEvents.byId(id);
            if (ev != null && ev.hidden) n++;
        }
        return n;
    }

    private static int optionalMemories(StoryData d) {
        int n = 0;
        for (int m : d.memories) if (Memories.optional(m)) n++;
        return n;
    }

    /** Overall completion: meaningful progress only, never playtime or block counts. Reaching an ending needs none of it. */
    public static int percent(StoryData d) {
        double chapters = Math.max(0, Math.min(4, d.chapter - 1)) / 4.0;
        int puzzleAreas = 0;
        int areasDone = 0;
        for (AreaDef a : Areas.ALL) {
            if (!Puzzles.forArea(a.id).isEmpty()) {
                puzzleAreas++;
                if (Puzzles.areaDone(d, a.id)) areasDone++;
            }
        }
        double areas = puzzleAreas == 0 ? 0 : areasDone / (double) puzzleAreas;
        double puzzles = d.puzzlesDone.size() / (double) Puzzles.ALL.size();
        double mem = d.memories.size() / (double) Memories.TOTAL;
        int talked = 0;
        int totalLines = 0;
        for (NpcKind k : NpcKind.values()) {
            talked += Math.min(k.lines, d.talks.getOrDefault(k.id, 0));
            totalLines += k.lines;
        }
        double npc = talked / (double) totalLines;
        double secrets = (Math.min(1.0, d.secretRooms.size() / (double) SECRET_ROOMS)
                + hiddenEvents(d) / (double) HIDDEN_EVENTS
                + d.symbols.size() / 12.0
                + optionalMemories(d) / 10.0) / 4.0;
        double end = Math.min(4, d.endings.size()) / 4.0;
        double total = 0.20 * chapters + 0.15 * areas + 0.15 * puzzles + 0.20 * mem + 0.10 * npc
                + 0.15 * secrets + 0.05 * end;
        return (int) Math.round(Math.max(0.0, Math.min(1.0, total)) * 100.0);
    }

    public static CompoundTag snapshot(StoryData d) {
        CompoundTag t = new CompoundTag();
        t.putInt("chapter", d.chapter);
        t.putIntArray("memories", d.memories.stream().mapToInt(Integer::intValue).toArray());
        t.putIntArray("symbols", d.symbols.stream().mapToInt(Integer::intValue).toArray());
        t.put("puzzlesDone", strings(d.puzzlesDone));
        t.put("puzzlesSeen", strings(d.puzzlesSeen));
        t.put("endings", strings(d.endings));
        CompoundTag areas = new CompoundTag();
        for (AreaDef a : Areas.ALL) areas.putInt(a.id, areaState(d, a));
        t.put("areas", areas);
        CompoundTag talks = new CompoundTag();
        d.talks.forEach(talks::putInt);
        t.put("talks", talks);
        CompoundTag last = new CompoundTag();
        d.lastLine.forEach(last::putString);
        t.put("last", last);
        t.putInt("secretRooms", d.secretRooms.size());
        t.putInt("hiddenEvents", hiddenEvents(d));
        t.putInt("pct", percent(d));
        return t;
    }

    private static ListTag strings(Set<String> set) {
        ListTag l = new ListTag();
        for (String s : set) l.add(StringTag.valueOf(s));
        return l;
    }
}
