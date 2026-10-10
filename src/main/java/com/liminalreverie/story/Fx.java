package com.liminalreverie.story;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Small presentation helpers: messages that stay long enough to read, and sounds that follow the player. */
public final class Fx {
    private static final Map<UUID, Integer> TOKENS = new HashMap<>();

    private Fx() {}

    /**
     * Action-bar message that lasts about 8 seconds instead of 3: it is re-sent every 2 seconds, and a newer message
     * cancels the repeats of an older one. The third parameter exists only so old call sites keep their shape.
     */
    public static void hint(ServerPlayer p, Component c, boolean actionBar) {
        int token = TOKENS.merge(p.getUUID(), 1, Integer::sum);
        p.displayClientMessage(c, true);
        for (int i = 1; i <= 3; i++) {
            Scheduler.later(40 * i, () -> {
                if (TOKENS.getOrDefault(p.getUUID(), 0) == token) p.displayClientMessage(c, true);
            });
        }
    }

    /** A sound that is attached to the player: it keeps following them even if they move or teleport. */
    public static void attached(ServerPlayer p, SoundEvent ev, float vol, float pitch) {
        p.connection.send(new ClientboundSoundEntityPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ev),
                SoundSource.PLAYERS, p, vol, pitch, p.level().random.nextLong()));
    }

    public static void at(ServerLevel l, Vec3 pos, SoundEvent ev, float vol, float pitch) {
        l.playSound(null, pos.x, pos.y, pos.z, ev, SoundSource.AMBIENT, vol, pitch);
    }
}
