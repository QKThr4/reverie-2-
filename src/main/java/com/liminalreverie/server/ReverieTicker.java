package com.liminalreverie.server;

import com.liminalreverie.Reverie;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.DreamEvents;
import com.liminalreverie.world.AreaDef;
import com.liminalreverie.world.Areas;
import com.liminalreverie.world.BuildManager;
import com.liminalreverie.world.HotelArea;
import com.liminalreverie.world.NpcSpawner;
import com.liminalreverie.world.Transit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Runs once per second per player inside the Reverie (never per tick): ambience, dream events, character spawning,
 * no hunger, gentle gravity in one room, and a safety net for falling out of the dream.
 * (The normal world is handled by OverworldHaunt.)
 */
public final class ReverieTicker {
    private static final class PS {
        int ambientCd = 4;
        int eventCd = 90;
        int npcCd = 2;
        long musicUntil = 0L;
    }

    private static final Map<UUID, PS> STATE = new HashMap<>();

    private ReverieTicker() {}

    public static void clear() {
        STATE.clear();
    }

    public static void swapMusic(ServerPlayer p, int ticks) {
        PS ps = STATE.computeIfAbsent(p.getUUID(), k -> new PS());
        ps.musicUntil = p.serverLevel().getGameTime() + ticks;
    }

    public static void second(ServerPlayer p) {
        ServerLevel l = p.serverLevel();
        if (Reverie.in(l)) inReverie(p, l);
    }

    private static void inReverie(ServerPlayer p, ServerLevel l) {
        AreaDef area = Areas.at(p.blockPosition());
        if (area == null) return;
        PS ps = STATE.computeIfAbsent(p.getUUID(), k -> new PS());
        RandomSource rnd = l.random;

        // The dream does not make you hungry.
        p.getFoodData().setFoodLevel(20);
        p.getFoodData().setSaturation(20.0F);

        if (p.getY() < 20.0) { // fell out of the dream: back to the area's start
            Transit.hop(p, area, area.sx, area.sy, area.sz, area.yaw);
            return;
        }
        if (BuildManager.isBuilding(area.id)) return;

        if (--ps.ambientCd <= 0) {
            ps.ambientCd = 6 + rnd.nextInt(7);
            boolean swapped = l.getGameTime() < ps.musicUntil;
            SoundEvent s = swapped ? ModSounds.CHIME.get() : area.ambient.get();
            l.playSound(null, p.getX(), p.getY(), p.getZ(), s, SoundSource.AMBIENT, swapped ? 0.5F : 1.0F, swapped ? 0.5F : 1.0F);
        }

        if (--ps.npcCd <= 0) {
            ps.npcCd = 5;
            NpcSpawner.ensure(p.server, area);
        }

        if (area == HotelArea.DEF) {
            BlockPos rel = p.blockPosition().offset(-area.ox, -area.oy, -area.oz);
            if (HotelArea.inFloatRoom(rel)) {
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, false, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 1, false, false, false));
            }
        }

        if (!area.safe && --ps.eventCd <= 0) {
            ps.eventCd = 100 + rnd.nextInt(100);
            List<DreamEvents> pool = DreamEvents.pool(rnd.nextInt(100) < 12);
            pool.get(rnd.nextInt(pool.size())).trigger(p);
        }
    }
}
