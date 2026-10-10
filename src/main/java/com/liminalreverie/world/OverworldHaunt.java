package com.liminalreverie.world;

import com.liminalreverie.net.EventPayload;
import com.liminalreverie.registry.ModBlocks;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.Fx;
import com.liminalreverie.story.Scheduler;
import com.liminalreverie.story.StoryData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The normal world slowly stops making sense.
 * 10-30 min: strange, pointless liminal constructions and sounds, more of them and stranger as time passes.
 * 30+ min: doors, sky stairs, lone elevators and open corridors that lead to the Lobby, every minute or so.
 * Both keep appearing until every player has entered the Reverie (or an ending was reached); then the mod
 * removes exactly the blocks it placed. Surface, cave and underwater get their own variants.
 */
public final class OverworldHaunt {
    public static final int START = 600;
    public static final int PORTALS = 1800;
    private static final int CLEAN_PER_TICK = 200;

    private enum Env { SURFACE, CAVE, WATER }

    private enum Kind {
        LONE_DOOR(1), LIGHT_POLE(1), WALL_COLUMN(1), CARPET_PATCH(1), GLASS_PANELS(1),
        EMPTY_ROOM(2), DEAD_END(2), TILE_PATH(2), DOOR_ROW(2),
        GRAND_HALL(3), STAIRS_NOWHERE(3), FLOATING_ROOM(3), LIGHT_ROW(3), TALL_WALL(3), GLASS_HOUSE(3);

        final int tier;

        Kind(int tier) {
            this.tier = tier;
        }
    }

    private enum PKind { DOOR, SKY_STAIRS, ELEVATOR, CORRIDOR }

    private record Entry(BlockPos pos, BlockState state, boolean portal) {}

    /** A structure described in local coordinates: f = forward (away from the player), s = to the right, u = up. */
    private static final class Plan {
        final BlockPos base;
        final Direction face;
        final Direction side;
        final Map<Long, Entry> map = new LinkedHashMap<>();

        Plan(BlockPos base, Direction face) {
            this.base = base;
            this.face = face;
            this.side = face.getClockWise();
        }

        BlockPos at(int f, int s, int u) {
            return base.relative(face, f).relative(side, s).above(u);
        }

        void add(int f, int s, int u, BlockState st) {
            BlockPos p = at(f, s, u);
            map.put(p.asLong(), new Entry(p, st, false));
        }

        void box(int f1, int s1, int u1, int f2, int s2, int u2, BlockState st) {
            for (int f = Math.min(f1, f2); f <= Math.max(f1, f2); f++)
                for (int s = Math.min(s1, s2); s <= Math.max(s1, s2); s++)
                    for (int u = Math.min(u1, u2); u <= Math.max(u1, u2); u++) add(f, s, u, st);
        }

        void door(int f, int s, int u, Block door, boolean portal) {
            BlockState lo = door.defaultBlockState().setValue(DoorBlock.FACING, face.getOpposite())
                    .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
            BlockPos p1 = at(f, s, u);
            BlockPos p2 = at(f, s, u + 1);
            map.put(p1.asLong(), new Entry(p1, lo, portal));
            map.put(p2.asLong(), new Entry(p2, lo.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), portal));
        }
    }

    private static int strangeCd = 30;
    private static int soundCd = 20;
    private static int portalCd = 10;
    private static Set<Block> palette;

    private OverworldHaunt() {}

    // ---------------------------------------------------------------- state rules

    public static boolean allEntered(StoryData d) {
        return !d.knownPlayers.isEmpty() && d.enteredPlayers.containsAll(d.knownPlayers);
    }

    /** The haunting runs only while someone has not entered the Reverie yet and no ending was reached. */
    public static boolean active(StoryData d) {
        return d.endings.isEmpty() && !allEntered(d);
    }

    private static Set<Block> palette() {
        if (palette == null) {
            palette = Set.of(ModBlocks.WALLPAPER.get(), ModBlocks.LIMINAL_CARPET.get(),
                    ModBlocks.FLUORESCENT_LIGHT.get(), ModBlocks.POOL_TILE.get(), ModBlocks.MALL_TILE.get(),
                    Blocks.OAK_DOOR, Blocks.IRON_DOOR, Blocks.OAK_FENCE, Blocks.GLASS, Blocks.QUARTZ_STAIRS);
        }
        return palette;
    }

    // ---------------------------------------------------------------- once per second

    public static void second(MinecraftServer server) {
        StoryData d = StoryData.get(server);
        if (!active(d)) return;
        ServerLevel ow = server.overworld();
        List<ServerPlayer> ps = new ArrayList<>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.level() == ow && p.isAlive() && !p.isSpectator() && !d.enteredPlayers.contains(p.getUUID())) ps.add(p);
        }
        if (ps.isEmpty()) return;

        d.overworldSeconds++;
        if (d.overworldSeconds % 30 == 0) d.setDirty();
        long t = d.overworldSeconds;
        if (t < START) return;

        RandomSource r = ow.random;
        ServerPlayer target = ps.get(r.nextInt(ps.size()));
        double prog = Mth.clamp((t - START) / (double) (PORTALS - START), 0.0, 1.0);

        if (--soundCd <= 0) {
            soundCd = (int) Mth.lerp(prog, 40.0, 14.0) + r.nextInt(12);
            strangeSound(target, ow, r, prog);
        }
        if (--strangeCd <= 0) {
            strangeCd = (int) Mth.lerp(prog, 90.0, 30.0) + r.nextInt(10);
            spawn(target, ow, d, r, false, t);
        }
        if (t >= PORTALS && --portalCd <= 0) {
            portalCd = r.nextInt(100) < 80 ? 55 + r.nextInt(12) : 120 + r.nextInt(60);
            spawn(target, ow, d, r, true, t);
        }
    }

    private static Vec3 forward(ServerPlayer p) {
        float yaw = p.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    private static void strangeSound(ServerPlayer p, ServerLevel l, RandomSource r, double prog) {
        Vec3 pos = p.position();
        Vec3 f = forward(p);
        switch (r.nextInt(7)) {
            case 0 -> Fx.at(l, pos.subtract(f.scale(16)), ModSounds.BELL.get(), 1.0F, 0.7F);
            case 1 -> Fx.at(l, pos, ModSounds.AMB_LOBBY.get(), 1.0F, 0.8F);
            case 2 -> {
                for (int i = 0; i < 5; i++) {
                    final int step = i;
                    Scheduler.later(i * 12, () -> Fx.at(l, p.position().subtract(forward(p).scale(9.0 - step * 1.5)),
                            ModSounds.STEP.get(), 0.9F, 0.9F));
                }
            }
            case 3 -> Fx.at(l, pos.add(f.scale(10)), ModSounds.DING.get(), 0.9F, 0.9F);
            case 4 -> PacketDistributor.sendToPlayer(p, new EventPayload("fog_shift", 300));
            case 5 -> Fx.at(l, pos.subtract(f.scale(14)), ModSounds.LAUGH.get(), 0.9F, 1.2F);
            default -> PacketDistributor.sendToPlayer(p, new EventPayload("silence", 120));
        }
        if (prog > 0.6 && r.nextInt(5) == 0) {
            p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false, false));
        }
    }

    // ---------------------------------------------------------------- spawning

    private static Env envOf(ServerPlayer p, ServerLevel l) {
        if (p.isEyeInFluid(FluidTags.WATER)) return Env.WATER;
        BlockPos pos = p.blockPosition();
        if (!l.canSeeSky(pos.above()) && p.getY() < l.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) - 6) {
            return Env.CAVE;
        }
        return Env.SURFACE;
    }

    private static void spawn(ServerPlayer p, ServerLevel l, StoryData d, RandomSource r, boolean portal, long t) {
        Env env = envOf(p, l);
        int maxTier = t < 900 ? 1 : t < 1200 ? 2 : 3;
        for (int attempt = 0; attempt < 10; attempt++) {
            BlockPos base = findSpot(p, l, env, r, portal);
            if (base == null) continue;
            Direction face = Direction.getNearest(base.getX() - p.getX(), 0.0, base.getZ() - p.getZ());
            Plan plan = new Plan(base, face);
            if (portal) {
                plan(plan, pickPortal(env, r, l));
            } else {
                plan(plan, pickStrange(env, r, maxTier));
            }
            if (commit(l, d, plan, env == Env.WATER)) {
                Vec3 c = Vec3.atCenterOf(base);
                if (portal) {
                    Fx.at(l, c, ModSounds.DING.get(), 1.0F, 0.8F);
                    Fx.attached(p, ModSounds.BELL.get(), 0.9F, 0.6F);
                }
                d.setDirty();
                return;
            }
        }
    }

    private static Kind pickStrange(Env env, RandomSource r, int maxTier) {
        Kind[] pool;
        switch (env) {
            case WATER -> pool = new Kind[]{Kind.LONE_DOOR, Kind.WALL_COLUMN, Kind.TILE_PATH, Kind.LIGHT_POLE};
            case CAVE -> pool = new Kind[]{Kind.LONE_DOOR, Kind.WALL_COLUMN, Kind.CARPET_PATCH, Kind.TILE_PATH,
                    Kind.DOOR_ROW, Kind.LIGHT_ROW};
            default -> pool = Kind.values();
        }
        List<Kind> ok = new ArrayList<>();
        int want = 1 + r.nextInt(maxTier);
        for (Kind k : pool) if (k.tier == want) ok.add(k);
        if (ok.isEmpty()) for (Kind k : pool) if (k.tier <= maxTier) ok.add(k);
        return ok.get(r.nextInt(ok.size()));
    }

    private static PKind pickPortal(Env env, RandomSource r, ServerLevel l) {
        if (env == Env.WATER) return PKind.DOOR;
        PKind[] all = env == Env.CAVE ? new PKind[]{PKind.DOOR, PKind.ELEVATOR} : PKind.values();
        return all[r.nextInt(all.length)];
    }

    private static BlockPos findSpot(ServerPlayer p, ServerLevel l, Env env, RandomSource r, boolean portal) {
        for (int i = 0; i < 16; i++) {
            double ang;
            int dist;
            if (portal && r.nextBoolean()) { // behind the player
                float yaw = p.getYRot() * Mth.DEG_TO_RAD;
                ang = Math.atan2(Mth.cos(yaw), -Mth.sin(yaw)) + Math.PI + (r.nextDouble() - 0.5) * 0.9;
                dist = 10 + r.nextInt(9);
            } else {
                ang = r.nextDouble() * Math.PI * 2.0;
                dist = portal ? 14 + r.nextInt(26) : 20 + r.nextInt(40);
            }
            int x = Mth.floor(p.getX() + Math.cos(ang) * dist);
            int z = Mth.floor(p.getZ() + Math.sin(ang) * dist);
            if (!l.hasChunkAt(new BlockPos(x, 64, z))) continue;
            switch (env) {
                case SURFACE -> {
                    int y = l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState below = l.getBlockState(pos.below());
                    if (l.getBlockState(pos).isAir() && below.isFaceSturdy(l, pos.below(), Direction.UP)
                            && below.getFluidState().isEmpty()) return pos;
                }
                case CAVE -> {
                    BlockPos pos = new BlockPos(x, p.getBlockY() + r.nextInt(9) - 4, z);
                    if (l.getBlockState(pos).isAir() && l.getBlockState(pos.above()).isAir()
                            && l.getBlockState(pos.above(2)).isAir()
                            && l.getBlockState(pos.below()).isFaceSturdy(l, pos.below(), Direction.UP)) return pos;
                }
                case WATER -> {
                    for (int yy = p.getBlockY() + 3; yy >= p.getBlockY() - 18; yy--) {
                        BlockPos pos = new BlockPos(x, yy, z);
                        if (l.getBlockState(pos).is(Blocks.WATER)
                                && l.getBlockState(pos.below()).isFaceSturdy(l, pos.below(), Direction.UP)) return pos;
                    }
                }
            }
        }
        return null;
    }

    private static boolean free(ServerLevel l, BlockPos p, boolean water) {
        BlockState s = l.getBlockState(p);
        if (s.isAir()) return true;
        if (water) return s.is(Blocks.WATER);
        return s.canBeReplaced() && s.getFluidState().isEmpty();
    }

    /** Checks that the whole structure fits in loaded, free space; only then places it and records every block. */
    private static boolean commit(ServerLevel l, StoryData d, Plan plan, boolean water) {
        if (plan.map.isEmpty()) return false;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (Entry e : plan.map.values()) {
            minX = Math.min(minX, e.pos().getX());
            minY = Math.min(minY, e.pos().getY());
            minZ = Math.min(minZ, e.pos().getZ());
            maxX = Math.max(maxX, e.pos().getX());
            maxY = Math.max(maxY, e.pos().getY());
            maxZ = Math.max(maxZ, e.pos().getZ());
        }
        if (maxY >= l.getMaxBuildHeight() - 2 || minY <= l.getMinBuildHeight() + 2) return false;
        if (!l.hasChunksAt(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ))) return false;
        for (Entry e : plan.map.values()) if (!free(l, e.pos(), water)) return false;
        for (Entry e : plan.map.values()) {
            if (e.state().isAir()) continue; // carved-out space needs no block
            boolean wasWater = l.getBlockState(e.pos()).is(Blocks.WATER);
            l.setBlock(e.pos(), e.state(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            long k = e.pos().asLong();
            if (wasWater) d.hauntWater.add(k);
            else d.hauntBlocks.add(k);
            if (e.portal()) d.portalBlocks.add(k);
        }
        return true;
    }

    // ---------------------------------------------------------------- structures

    private static void plan(Plan p, Kind k) {
        BlockState wp = Pal.wallpaper();
        BlockState carpet = Pal.carpet();
        BlockState light = Pal.light();
        BlockState glass = Build.st(Blocks.GLASS);
        switch (k) {
            case LONE_DOOR -> p.door(0, 0, 0, Blocks.OAK_DOOR, false);
            case LIGHT_POLE -> {
                p.box(0, 0, 0, 0, 0, 3, Build.st(Blocks.OAK_FENCE));
                p.add(0, 0, 4, light);
            }
            case WALL_COLUMN -> p.box(0, 0, 0, 0, 0, 2, wp);
            case CARPET_PATCH -> p.box(0, -1, 0, 2, 1, 0, carpet);
            case GLASS_PANELS -> p.box(0, 0, 0, 1, 1, 0, glass);
            case EMPTY_ROOM -> {
                p.box(0, -2, 0, 4, 2, 2, wp);
                p.box(1, -1, 0, 3, 1, 2, Build.AIR);
                p.box(0, 0, 0, 0, 0, 1, Build.AIR);
                p.box(0, -2, 3, 4, 2, 3, wp);
                p.add(2, 0, 3, light);
            }
            case DEAD_END -> {
                p.box(0, -2, 0, 9, -2, 2, wp);
                p.box(0, 2, 0, 9, 2, 2, wp);
                p.box(10, -2, 0, 10, 2, 2, wp);
                p.box(0, -2, 3, 9, 2, 3, wp);
                p.add(2, 0, 3, light);
                p.add(6, 0, 3, light);
            }
            case TILE_PATH -> p.box(0, -1, 0, 15, 1, 0, p.base.getY() % 2 == 0 ? Pal.poolTile() : Pal.mallTile());
            case DOOR_ROW -> {
                for (int s = -2; s <= 2; s += 2) p.door(0, s, 0, Blocks.OAK_DOOR, false);
            }
            case GRAND_HALL -> {
                p.box(0, -6, 0, 12, -6, 4, wp);
                p.box(0, 6, 0, 12, 6, 4, wp);
                p.box(12, -6, 0, 12, 6, 4, wp);
                p.box(0, -6, 5, 12, 6, 5, wp);
                p.box(0, -1, 0, 0, 1, 3, Build.AIR);
                for (int f : new int[]{3, 9}) for (int s : new int[]{-3, 3}) p.box(f, s, 0, f, s, 4, wp);
                for (int f : new int[]{3, 9}) p.add(f, 0, 5, light);
            }
            case STAIRS_NOWHERE -> {
                for (int i = 0; i < 14; i++) {
                    p.add(i, 0, i, Pal.stairs(Blocks.QUARTZ_STAIRS, p.face));
                    if (i % 4 == 3) p.add(i, 1, i, light);
                }
            }
            case FLOATING_ROOM -> {
                p.box(0, -2, 10, 4, 2, 14, wp);
                p.box(1, -1, 11, 3, 1, 13, Build.AIR);
                p.add(2, 0, 13, light);
            }
            case LIGHT_ROW -> {
                for (int i = 0; i < 8; i++) p.add(i * 2, 0, 6, light);
            }
            case TALL_WALL -> {
                p.box(0, -4, 0, 0, 4, 11, wp);
                p.door(0, 0, 0, Blocks.OAK_DOOR, false);
            }
            case GLASS_HOUSE -> {
                p.box(0, -3, 0, 6, 3, 3, glass);
                p.box(1, -2, 0, 5, 2, 2, Build.AIR);
                p.box(0, 0, 0, 0, 0, 1, Build.AIR);
                p.add(3, 0, 3, light);
            }
        }
    }

    private static void plan(Plan p, PKind k) {
        BlockState wp = Pal.wallpaper();
        BlockState light = Pal.light();
        switch (k) {
            case DOOR -> {
                p.door(0, 0, 0, Blocks.OAK_DOOR, true);
                p.box(0, -1, 0, 0, -1, 2, wp);
                p.box(0, 1, 0, 0, 1, 2, wp);
                p.add(0, 0, 2, light);
            }
            case SKY_STAIRS -> {
                int n = Math.max(8, Math.min(36, p.base.getY() + 60 > 300 ? 300 - p.base.getY() : 36));
                for (int i = 0; i < n; i++) {
                    p.add(i, 0, i, Pal.stairs(Blocks.QUARTZ_STAIRS, p.face));
                    if (i % 6 == 5) p.add(i, 1, i, light);
                }
                p.box(n, -1, n - 1, n + 2, 1, n - 1, wp);
                p.door(n + 2, 0, n, Blocks.IRON_DOOR, true);
                p.add(n + 2, 0, n + 2, light);
            }
            case ELEVATOR -> {
                p.box(0, -2, 0, 3, -2, 2, wp);
                p.box(0, 2, 0, 3, 2, 2, wp);
                p.box(3, -2, 0, 3, 2, 2, wp);
                p.box(0, -1, 0, 0, 1, 2, wp);
                p.box(0, -2, 3, 3, 2, 3, wp);
                p.add(1, 0, 3, light);
                p.door(0, 0, 0, Blocks.IRON_DOOR, true);
            }
            case CORRIDOR -> {
                p.box(0, -2, 0, 9, -2, 2, wp);
                p.box(0, 2, 0, 9, 2, 2, wp);
                p.box(10, -2, 0, 10, 2, 2, wp);
                p.add(3, -2, 2, light);
                p.add(6, 2, 2, light);
                p.door(10, 0, 0, Blocks.OAK_DOOR, true);
            }
        }
    }

    // ---------------------------------------------------------------- cleanup

    /** Removes what the haunting placed, a few hundred blocks per tick, once it is no longer active. */
    public static void cleanupTick(MinecraftServer server) {
        StoryData d = StoryData.get(server);
        if (d.hauntBlocks.isEmpty() && d.hauntWater.isEmpty() && d.portalBlocks.isEmpty()) return;
        if (active(d)) return;
        ServerLevel ow = server.overworld();
        int budget = clean(ow, d.hauntBlocks, false, CLEAN_PER_TICK);
        clean(ow, d.hauntWater, true, budget);
        if (d.hauntBlocks.isEmpty() && d.hauntWater.isEmpty()) d.portalBlocks.clear();
        d.setDirty();
    }

    private static int clean(ServerLevel ow, Set<Long> set, boolean water, int budget) {
        Iterator<Long> it = set.iterator();
        while (it.hasNext() && budget > 0) {
            long l = it.next();
            it.remove();
            budget--;
            BlockPos p = BlockPos.of(l);
            if (palette().contains(ow.getBlockState(p).getBlock())) {
                ow.setBlock(p, water ? Blocks.WATER.defaultBlockState() : Build.AIR,
                        Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
        return budget;
    }
}
