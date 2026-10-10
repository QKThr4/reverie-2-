package com.liminalreverie.net;

import com.liminalreverie.LiminalReverie;
import com.liminalreverie.entity.LiminalNpc;
import com.liminalreverie.registry.ModEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Mod-bus events shared by client and server: payload registration and entity attributes. Registered once. */
@EventBusSubscriber(modid = LiminalReverie.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEvents {
    private ModEvents() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("1");
        r.playToServer(RequestJournalPayload.TYPE, RequestJournalPayload.CODEC, RequestJournalPayload::handle);
        r.playToClient(SyncPayload.TYPE, SyncPayload.CODEC, SyncPayload::handle);
        r.playToClient(NotifyPayload.TYPE, NotifyPayload.CODEC, NotifyPayload::handle);
        r.playToClient(CardPayload.TYPE, CardPayload.CODEC, CardPayload::handle);
        r.playToClient(EventPayload.TYPE, EventPayload.CODEC, EventPayload::handle);
    }

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CARETAKER.get(), LiminalNpc.createAttributes().build());
        event.put(ModEntities.CHILD.get(), LiminalNpc.createAttributes().build());
        event.put(ModEntities.WANDERER.get(), LiminalNpc.createAttributes().build());
        event.put(ModEntities.SLEEPER.get(), LiminalNpc.createAttributes().build());
    }
}
