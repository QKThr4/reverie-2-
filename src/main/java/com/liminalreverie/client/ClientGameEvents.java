package com.liminalreverie.client;

import com.liminalreverie.LiminalReverie;
import com.liminalreverie.Reverie;
import com.liminalreverie.net.RequestJournalPayload;
import com.liminalreverie.world.AreaDef;
import com.liminalreverie.world.Areas;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Client-only game-bus events: journal key, per-area fog colour/distance, and the "silence" dream event. */
@EventBusSubscriber(modid = LiminalReverie.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        while (ClientModEvents.JOURNAL.consumeClick()) {
            if (mc.player != null && mc.level != null && mc.screen == null) {
                PacketDistributor.sendToServer(new RequestJournalPayload());
            }
        }
    }

    private static float mix(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        long now = Util.getMillis();
        if (Reverie.in(mc.level)) {
            AreaDef a = Areas.at(mc.player.blockPosition());
            if (a == null) return;
            float r = a.fogR / 255.0F;
            float g = a.fogG / 255.0F;
            float b = a.fogB / 255.0F;
            if (now < ClientState.skyBlushUntil) {
                r = mix(r, 1.0F, 0.6F);
                g = mix(g, 0.62F, 0.6F);
                b = mix(b, 0.75F, 0.6F);
            } else if (now < ClientState.fogShiftUntil) {
                r = mix(r, 0.55F, 0.5F);
                g = mix(g, 0.8F, 0.5F);
                b = mix(b, 0.85F, 0.5F);
            }
            e.setRed(r);
            e.setGreen(g);
            e.setBlue(b);
        } else if (mc.level.dimension() == Level.OVERWORLD && now < ClientState.fogShiftUntil) {
            e.setRed(mix(e.getRed(), 1.0F, 0.35F));
            e.setGreen(mix(e.getGreen(), 0.82F, 0.35F));
            e.setBlue(mix(e.getBlue(), 0.62F, 0.35F));
        }
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !Reverie.in(mc.level)) return;
        if (e.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return;
        if (mc.player.isEyeInFluid(FluidTags.WATER) || mc.player.isEyeInFluid(FluidTags.LAVA)) return;
        if (mc.player.hasEffect(MobEffects.DARKNESS) || mc.player.hasEffect(MobEffects.BLINDNESS)) return;
        AreaDef a = Areas.at(mc.player.blockPosition());
        if (a == null) return;
        e.setNearPlaneDistance(0.0F);
        e.setFarPlaneDistance(a.fogFar);
        e.setCanceled(true);
    }

    /** During a "silence" event everything except UI sound is muted for a few seconds. */
    @SubscribeEvent
    public static void onSound(PlaySoundEvent e) {
        if (Util.getMillis() >= ClientState.silenceUntil) return;
        SoundInstance s = e.getSound();
        if (s != null && s.getSource() != SoundSource.MASTER) e.setSound(null);
    }
}
