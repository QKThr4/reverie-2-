package com.liminalreverie.world;

import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.StoryData;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Declarative description of one liminal area. Every area owns a 512-block wide strip of the Reverie
 * dimension (origin x = index * 512), so positions alone identify the area; nothing depends on runtime state.
 */
public final class AreaDef {
    public static final int OY = 64;

    public final String id;
    public final int index;
    /** Always listed in the journal (otherwise it shows as ??? until entered). */
    public final boolean listed;
    public final int ox;
    public final int oy = OY;
    public final int oz = 0;

    public double sx, sy, sz;
    public float yaw;
    public final List<int[]> bounds = new ArrayList<>();
    public int maxPhase = 0;
    public boolean safe = false;
    public Predicate<StoryData> gate = d -> true;
    public Consumer<Build> builder = b -> { };
    public BiConsumer<Build, Integer> phaser = (b, n) -> { };
    public Consumer<AreaDef> triggerFn = a -> { };
    public List<NpcPlacement> npcs = List.of();
    public Supplier<SoundEvent> ambient = () -> ModSounds.AMB_LOBBY.get();
    public int fogR = 210, fogG = 200, fogB = 180, fogFar = 100;

    public AreaDef(String id, int index, boolean listed) {
        this.id = id;
        this.index = index;
        this.listed = listed;
        this.ox = index * 512;
    }

    public AreaDef spawn(double x, double y, double z, float yaw) {
        this.sx = x;
        this.sy = y;
        this.sz = z;
        this.yaw = yaw;
        return this;
    }

    /** Relative chunk-forcing bounds used while the area is being built. */
    public AreaDef bounds(int minX, int maxX, int minZ, int maxZ) {
        bounds.add(new int[]{minX, maxX, minZ, maxZ});
        return this;
    }

    public AreaDef phases(int max, BiConsumer<Build, Integer> f) {
        this.maxPhase = max;
        this.phaser = f;
        return this;
    }

    public AreaDef gate(Predicate<StoryData> g) {
        this.gate = g;
        return this;
    }

    public AreaDef builder(Consumer<Build> b) {
        this.builder = b;
        return this;
    }

    public AreaDef triggers(Consumer<AreaDef> t) {
        this.triggerFn = t;
        return this;
    }

    public AreaDef npcs(List<NpcPlacement> list) {
        this.npcs = list;
        return this;
    }

    public AreaDef ambient(Supplier<SoundEvent> s) {
        this.ambient = s;
        return this;
    }

    public AreaDef fog(int r, int g, int b, int far) {
        this.fogR = r;
        this.fogG = g;
        this.fogB = b;
        this.fogFar = far;
        return this;
    }

    public AreaDef safe() {
        this.safe = true;
        return this;
    }

    public BlockPos pos(int x, int y, int z) {
        return new BlockPos(ox + x, oy + y, oz + z);
    }
}
