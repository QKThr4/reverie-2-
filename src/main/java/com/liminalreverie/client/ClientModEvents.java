package com.liminalreverie.client;

import com.liminalreverie.LiminalReverie;
import com.liminalreverie.Reverie;
import com.liminalreverie.registry.ModEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/** Client-only, mod-bus registrations: key mapping, renderers and the notification HUD layer. */
@EventBusSubscriber(modid = LiminalReverie.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    public static final KeyMapping JOURNAL = new KeyMapping(
            "key.liminalreverie.journal", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.liminalreverie");

    private ClientModEvents() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(JOURNAL);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CARETAKER.get(), NpcRenderer::new);
        event.registerEntityRenderer(ModEntities.CHILD.get(), NpcRenderer::new);
        event.registerEntityRenderer(ModEntities.WANDERER.get(), NpcRenderer::new);
        event.registerEntityRenderer(ModEntities.SLEEPER.get(), NpcRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Reverie.id("notifications"), (graphics, delta) -> ClientState.renderNotes(graphics));
        event.registerAboveAll(Reverie.id("cards"), (graphics, delta) -> ClientState.renderCard(graphics));
    }
}
