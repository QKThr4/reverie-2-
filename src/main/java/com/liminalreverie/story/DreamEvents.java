package com.liminalreverie.story;

import com.liminalreverie.net.EventPayload;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.server.ReverieTicker;
import com.liminalreverie.world.AreaDef;
import com.liminalreverie.world.Areas;
import com.liminalreverie.world.Phases;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/** Dreamlike (never frightening) events. Eight of them are rare "hidden" ones that count as secrets. */
public enum DreamEvents {
    SILENCE("silence", false),
    FOG_SHIFT("fog_shift", false),
    BELL("bell", false),
    MUSIC_SWAP("music_swap", false),
    SHIFT("shift", false),
    ECHO_STEPS("echo_steps", true),
    PHANTOM_CHIME("phantom_chime", true),
    LOW_GRAVITY("low_gravity", true),
    WARM_WIND("warm_wind", true),
    DISTANT_LAUGH("distant_laugh", true),
    CLOCK_STOPS("clock_stops", true),
    FLOATING_OBJECT("floating_object", true),
    SKY_BLUSH("sky_blush", true);

    public final String id;
    public final boolean hidden;

    DreamEvents(String id, boolean hidden) {
        this.id = id;
        this.hidden = hidden;
    }

    public static DreamEvents byId(String id) {
        for (DreamEvents e : values()) if (e.id.equals(id)) return e;
        return null;
    }

    public static List<DreamEvents> pool(boolean hiddenOnes) {
        List<DreamEvents> out = new ArrayList<>();
        for (DreamEvents e : values()) if (e.hidden == hiddenOnes) out.add(e);
        return out;
    }

    private static Vec3 flatForward(ServerPlayer p) {
        float yaw = p.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    private static void sound(ServerPlayer p, net.minecraft.sounds.SoundEvent ev, Vec3 pos, float vol, float pitch) {
        p.serverLevel().playSound(null, pos.x, pos.y, pos.z, ev, SoundSource.AMBIENT, vol, pitch);
    }

    public void trigger(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        Vec3 here = p.position();
        Vec3 fwd = flatForward(p);
        switch (this) {
            case SILENCE -> PacketDistributor.sendToPlayer(p, new EventPayload("silence", 160));
            case FOG_SHIFT -> PacketDistributor.sendToPlayer(p, new EventPayload("fog_shift", 400));
            case SKY_BLUSH -> PacketDistributor.sendToPlayer(p, new EventPayload("sky_blush", 300));
            case CLOCK_STOPS -> {
                sound(p, ModSounds.DING.get(), here, 0.8F, 0.6F);
                PacketDistributor.sendToPlayer(p, new EventPayload("silence", 120));
            }
            case BELL -> sound(p, ModSounds.BELL.get(), here.subtract(fwd.scale(18)), 0.9F, 0.7F);
            case MUSIC_SWAP -> ReverieTicker.swapMusic(p, 1200);
            case SHIFT -> {
                AreaDef cur = Areas.at(p.blockPosition());
                List<AreaDef> opts = new ArrayList<>();
                StoryData d = StoryData.get(p.server);
                for (AreaDef a : Areas.ALL) {
                    if (a != cur && a.maxPhase > 0 && d.areasBuilt.contains(a.id)) opts.add(a);
                }
                if (!opts.isEmpty()) Phases.advance(p.server, opts.get(level.random.nextInt(opts.size())));
            }
            case ECHO_STEPS -> {
                for (int i = 0; i < 6; i++) {
                    final int step = i;
                    Scheduler.later(i * 12, () -> {
                        Vec3 pos = p.position().subtract(flatForward(p).scale(9.0 - step * 1.2));
                        sound(p, ModSounds.STEP.get(), pos, 0.5F, 0.9F);
                    });
                }
            }
            case PHANTOM_CHIME -> sound(p, ModSounds.DING.get(), here.add(fwd.scale(10)), 0.7F, 1.1F);
            case LOW_GRAVITY -> {
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0, false, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.JUMP, 400, 1, false, false, false));
            }
            case WARM_WIND -> {
                sound(p, ModSounds.WIND.get(), here, 0.8F, 0.8F);
                level.sendParticles(ParticleTypes.END_ROD, here.x, here.y + 1.2, here.z, 24, 3.0, 1.0, 3.0, 0.01);
            }
            case DISTANT_LAUGH -> sound(p, ModSounds.LAUGH.get(), here.add(fwd.scale(-16)), 0.6F, 1.3F);
            case FLOATING_OBJECT -> {
                Vec3 pos = here.add(fwd.scale(4)).add(0, 1.4, 0);
                ItemEntity item = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.PAPER));
                item.setNoGravity(true);
                item.setDeltaMovement(Vec3.ZERO);
                item.setPickUpDelay(32767);
                level.addFreshEntity(item);
                Scheduler.later(900, item::discard);
            }
        }
        StoryManager.recordEvent(p, id);
    }
}
