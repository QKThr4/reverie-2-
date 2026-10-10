package com.liminalreverie.story;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * All persistent story state. Lives in the overworld's data storage, so it is saved with the world and
 * survives quitting / crashing / restarting. Loading is defensive: missing or odd data falls back to defaults.
 */
public class StoryData extends SavedData {
    public static final String NAME = "liminalreverie_story";
    public static final SavedData.Factory<StoryData> FACTORY =
            new SavedData.Factory<>(StoryData::new, StoryData::load, DataFixTypes.LEVEL);

    public int epoch = 0;
    public int chapter = 1;                 // 1..4 = chapter in progress, 5 = finished
    public boolean stayed = false;
    public boolean hasWrongDoor = false;
    public long wrongDoorPos = 0L;
    public long overworldSeconds = 0L;

    public final Set<String> areasEntered = new HashSet<>();
    public final Set<String> areasBuilt = new HashSet<>();
    public final Set<String> puzzlesDone = new HashSet<>();
    public final Set<String> puzzlesSeen = new HashSet<>();
    public final Set<String> secretRooms = new HashSet<>();
    public final Set<String> eventsSeen = new HashSet<>();
    public final Set<String> endings = new HashSet<>();
    public final Set<Integer> memories = new TreeSet<>();
    public final Set<Integer> symbols = new TreeSet<>();
    public final Map<String, Integer> talks = new HashMap<>();
    public final Map<String, Integer> phases = new HashMap<>();
    public final Map<String, Integer> seq = new HashMap<>();
    public final Map<String, String> lastLine = new HashMap<>();
    public final Map<String, UUID> npcs = new HashMap<>();
    public final Map<String, String> cameFrom = new HashMap<>();
    public final Set<UUID> knownPlayers = new HashSet<>();
    public final Set<UUID> enteredPlayers = new HashSet<>();
    public final Set<UUID> welcomed = new HashSet<>();
    /** Every block the overworld haunting placed (so it can be removed later) and which of them were water. */
    public final Set<Long> hauntBlocks = new HashSet<>();
    public final Set<Long> hauntWater = new HashSet<>();
    public final Set<Long> portalBlocks = new HashSet<>();

    public static StoryData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    public void resetAll() {
        epoch++;
        chapter = 1;
        stayed = false;
        hasWrongDoor = false;
        wrongDoorPos = 0L;
        overworldSeconds = 0L;
        areasEntered.clear();
        areasBuilt.clear();
        puzzlesDone.clear();
        puzzlesSeen.clear();
        secretRooms.clear();
        eventsSeen.clear();
        endings.clear();
        memories.clear();
        symbols.clear();
        talks.clear();
        phases.clear();
        seq.clear();
        lastLine.clear();
        npcs.clear();
        cameFrom.clear();
        enteredPlayers.clear();
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag t, HolderLookup.Provider registries) {
        t.putInt("version", 1);
        t.putInt("epoch", epoch);
        t.putInt("chapter", chapter);
        t.putBoolean("stayed", stayed);
        t.putBoolean("hasWrongDoor", hasWrongDoor);
        t.putLong("wrongDoorPos", wrongDoorPos);
        t.putLong("overworldSeconds", overworldSeconds);
        putSet(t, "areasEntered", areasEntered);
        putSet(t, "areasBuilt", areasBuilt);
        putSet(t, "puzzlesDone", puzzlesDone);
        putSet(t, "puzzlesSeen", puzzlesSeen);
        putSet(t, "secretRooms", secretRooms);
        putSet(t, "eventsSeen", eventsSeen);
        putSet(t, "endings", endings);
        t.putIntArray("memories", memories.stream().mapToInt(Integer::intValue).toArray());
        t.putIntArray("symbols", symbols.stream().mapToInt(Integer::intValue).toArray());
        t.put("talks", intMap(talks));
        t.put("phases", intMap(phases));
        t.put("seq", intMap(seq));
        CompoundTag ll = new CompoundTag();
        lastLine.forEach(ll::putString);
        t.put("lastLine", ll);
        CompoundTag np = new CompoundTag();
        npcs.forEach(np::putUUID);
        t.put("npcs", np);
        CompoundTag cf = new CompoundTag();
        cameFrom.forEach(cf::putString);
        t.put("cameFrom", cf);
        putUuids(t, "knownPlayers", knownPlayers);
        putUuids(t, "enteredPlayers", enteredPlayers);
        putUuids(t, "welcomed", welcomed);
        t.putLongArray("hauntBlocks", hauntBlocks.stream().mapToLong(Long::longValue).toArray());
        t.putLongArray("hauntWater", hauntWater.stream().mapToLong(Long::longValue).toArray());
        t.putLongArray("portalBlocks", portalBlocks.stream().mapToLong(Long::longValue).toArray());
        return t;
    }

    public static StoryData load(CompoundTag t, HolderLookup.Provider registries) {
        StoryData d = new StoryData();
        try {
            d.epoch = t.getInt("epoch");
            d.chapter = t.contains("chapter") ? Mth.clamp(t.getInt("chapter"), 1, 5) : 1;
            d.stayed = t.getBoolean("stayed");
            d.hasWrongDoor = t.getBoolean("hasWrongDoor");
            d.wrongDoorPos = t.getLong("wrongDoorPos");
            d.overworldSeconds = t.getLong("overworldSeconds");
            getSet(t, "areasEntered", d.areasEntered);
            getSet(t, "areasBuilt", d.areasBuilt);
            getSet(t, "puzzlesDone", d.puzzlesDone);
            getSet(t, "puzzlesSeen", d.puzzlesSeen);
            getSet(t, "secretRooms", d.secretRooms);
            getSet(t, "eventsSeen", d.eventsSeen);
            getSet(t, "endings", d.endings);
            for (int i : t.getIntArray("memories")) if (i >= 1 && i <= Memories.TOTAL) d.memories.add(i);
            for (int i : t.getIntArray("symbols")) if (i >= 0 && i < 12) d.symbols.add(i);
            getIntMap(t, "talks", d.talks);
            getIntMap(t, "phases", d.phases);
            getIntMap(t, "seq", d.seq);
            CompoundTag ll = t.getCompound("lastLine");
            for (String k : ll.getAllKeys()) d.lastLine.put(k, ll.getString(k));
            CompoundTag np = t.getCompound("npcs");
            for (String k : np.getAllKeys()) if (np.hasUUID(k)) d.npcs.put(k, np.getUUID(k));
            CompoundTag cf = t.getCompound("cameFrom");
            for (String k : cf.getAllKeys()) d.cameFrom.put(k, cf.getString(k));
            getUuids(t, "knownPlayers", d.knownPlayers);
            getUuids(t, "enteredPlayers", d.enteredPlayers);
            getUuids(t, "welcomed", d.welcomed);
            for (long l : t.getLongArray("hauntBlocks")) d.hauntBlocks.add(l);
            for (long l : t.getLongArray("hauntWater")) d.hauntWater.add(l);
            for (long l : t.getLongArray("portalBlocks")) d.portalBlocks.add(l);
        } catch (RuntimeException e) {
            // Corrupted or very old data: keep whatever loaded and carry on with defaults.
        }
        return d;
    }

    private static void putUuids(CompoundTag t, String key, Set<UUID> set) {
        ListTag l = new ListTag();
        for (UUID u : set) l.add(StringTag.valueOf(u.toString()));
        t.put(key, l);
    }

    private static void getUuids(CompoundTag t, String key, Set<UUID> out) {
        ListTag l = t.getList(key, Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) {
            try {
                out.add(UUID.fromString(l.getString(i)));
            } catch (IllegalArgumentException ignored) {
                // skip a damaged entry
            }
        }
    }

    private static void putSet(CompoundTag t, String key, Set<String> set) {
        ListTag l = new ListTag();
        for (String s : set) l.add(StringTag.valueOf(s));
        t.put(key, l);
    }

    private static void getSet(CompoundTag t, String key, Set<String> out) {
        ListTag l = t.getList(key, Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) out.add(l.getString(i));
    }

    private static CompoundTag intMap(Map<String, Integer> m) {
        CompoundTag c = new CompoundTag();
        m.forEach(c::putInt);
        return c;
    }

    private static void getIntMap(CompoundTag t, String key, Map<String, Integer> out) {
        CompoundTag c = t.getCompound(key);
        for (String k : c.getAllKeys()) out.put(k, c.getInt(k));
    }
}
