package com.liminalreverie.client;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;

/** Entry points called by the (common) payload classes. Only ever invoked on a physical client. */
public final class ClientHooks {
    private ClientHooks() {}

    public static void onSync(String snbt, boolean open) {
        try {
            CompoundTag tag = TagParser.parseTag(snbt);
            ClientState.snapshot = tag;
        } catch (CommandSyntaxException e) {
            return; // never crash on a bad snapshot; keep showing the previous one
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (open) {
            mc.setScreen(new JournalScreen());
        } else if (mc.screen instanceof JournalScreen js) {
            js.refresh();
        }
    }

    public static void onNotify(String key, String arg) {
        ClientState.addNote(arg == null || arg.isEmpty() ? Component.translatable(key) : Component.translatable(key, arg));
    }

    public static void onCard(String key, int ticks) {
        ClientState.setCard(Component.translatable(key), ticks);
    }

    public static void onEvent(String id, int ticks) {
        long until = Util.getMillis() + ticks * 50L;
        switch (id) {
            case "silence" -> ClientState.silenceUntil = until;
            case "fog_shift" -> ClientState.fogShiftUntil = until;
            case "sky_blush" -> ClientState.skyBlushUntil = until;
            default -> { }
        }
    }
}
