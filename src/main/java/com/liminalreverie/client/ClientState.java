package com.liminalreverie.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Client-only presentation state: the last journal snapshot, fading notifications and timed visual events. */
public final class ClientState {
    private record Note(Component text, long start) {}

    private static final long NOTE_LIFE_MS = 4500L;
    private static final long NOTE_FADE_MS = 1000L;
    private static final List<Note> NOTES = new ArrayList<>();

    public static CompoundTag snapshot = new CompoundTag();
    public static long silenceUntil = 0L;
    public static long fogShiftUntil = 0L;
    public static long skyBlushUntil = 0L;

    private static Component cardText = null;
    private static long cardStart = 0L;
    private static long cardEnd = 0L;

    private ClientState() {}

    public static void setCard(Component c, int ticks) {
        cardText = c;
        cardStart = Util.getMillis();
        cardEnd = cardStart + ticks * 50L;
    }

    /** A dark full-screen card with centred, word-wrapped text (endings). Fades in and out. */
    public static void renderCard(GuiGraphics g) {
        if (cardText == null) return;
        long now = Util.getMillis();
        if (now > cardEnd) {
            cardText = null;
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        double f = Math.min(Math.min(1.0, (now - cardStart) / 700.0), Math.min(1.0, (cardEnd - now) / 700.0));
        int w = g.guiWidth();
        int h = g.guiHeight();
        g.fill(0, 0, w, h, ((int) (235 * f)) << 24);
        List<FormattedCharSequence> lines = mc.font.split(cardText, (int) (w * 0.6));
        int y = h / 2 - lines.size() * 6;
        int alpha = Math.max(8, (int) (255 * f));
        for (FormattedCharSequence line : lines) {
            g.drawCenteredString(mc.font, line, w / 2, y, (alpha << 24) | 0xF2E8D0);
            y += 12;
        }
    }

    public static void addNote(Component c) {
        NOTES.add(new Note(c, Util.getMillis()));
        while (NOTES.size() > 4) NOTES.remove(0);
    }

    /** Subtle top-centre notifications that fade away; never a permanent tracker. */
    public static void renderNotes(GuiGraphics g) {
        if (NOTES.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        long now = Util.getMillis();
        int y = 18;
        for (Iterator<Note> it = NOTES.iterator(); it.hasNext(); ) {
            Note n = it.next();
            long age = now - n.start();
            if (age > NOTE_LIFE_MS) {
                it.remove();
                continue;
            }
            long left = NOTE_LIFE_MS - age;
            double f = left < NOTE_FADE_MS ? left / (double) NOTE_FADE_MS : 1.0;
            int alpha = Math.max(8, (int) (255 * f));
            g.drawCenteredString(mc.font, n.text(), g.guiWidth() / 2, y, (alpha << 24) | 0xE8DCC0);
            y += 12;
        }
    }
}
