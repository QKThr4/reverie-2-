package com.liminalreverie.client;

import com.liminalreverie.entity.NpcKind;
import com.liminalreverie.story.Memories;
import com.liminalreverie.story.Puzzles;
import com.liminalreverie.world.AreaDef;
import com.liminalreverie.world.Areas;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The dream journal: an old notebook with five tabs. It only ever reads the last snapshot sent by the
 * server and tolerates missing data everywhere: undiscovered things show as "???".
 */
public class JournalScreen extends Screen {
    private static final int W = 320;
    private static final int H = 200;
    private static final int COL_W = 132;
    private static final int COL_LINES = 14;
    private static final int LH = 10;

    private static final int HEAD = 0x6B3E1E;
    private static final int OK = 0x2F6B3A;
    private static final int WARN = 0x8A5A1E;
    private static final int DIM = 0x8A7F6A;
    private static final int TEXT = 0x3B2F2F;

    private static final String[] TABS = {
            "lr.ui.tab.progress", "lr.ui.tab.memories", "lr.ui.tab.characters", "lr.ui.tab.places", "lr.ui.tab.symbols"};

    private int left;
    private int top;
    private int tab = 0;
    private int spread = 0;
    private List<FormattedCharSequence> lines = List.of();
    private Button prev;
    private Button next;

    public JournalScreen() {
        super(Component.translatable("lr.ui.title"));
    }

    @Override
    protected void init() {
        left = (this.width - W) / 2;
        top = (this.height - H) / 2;
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            this.addRenderableWidget(Button.builder(Component.translatable(TABS[i]), b -> {
                tab = idx;
                spread = 0;
                rebuild();
            }).bounds(left + 6 + i * 62, top - 20, 60, 16).build());
        }
        prev = this.addRenderableWidget(Button.builder(Component.literal("<"), b -> flip(-1))
                .bounds(left + 10, top + H - 22, 24, 14).build());
        next = this.addRenderableWidget(Button.builder(Component.literal(">"), b -> flip(1))
                .bounds(left + W - 34, top + H - 22, 24, 14).build());
        rebuild();
    }

    /** Called when a fresh snapshot arrives while the journal is open (the page updates by itself). */
    public void refresh() {
        if (this.font != null) rebuild();
    }

    private void flip(int dir) {
        spread = Math.max(0, Math.min(spreads() - 1, spread + dir));
        updateButtons();
    }

    private int spreads() {
        return Math.max(1, (lines.size() + COL_LINES * 2 - 1) / (COL_LINES * 2));
    }

    private void updateButtons() {
        if (prev != null) prev.active = spread > 0 && tab != 4;
        if (next != null) next.active = spread < spreads() - 1 && tab != 4;
    }

    // ------------------------------------------------------------------ content

    private CompoundTag snap() {
        return ClientState.snapshot == null ? new CompoundTag() : ClientState.snapshot;
    }

    private static MutableComponent t(String key, Object... args) {
        return Component.translatable(key, args);
    }

    private static MutableComponent col(MutableComponent c, int rgb) {
        return c.withStyle(s -> s.withColor(rgb));
    }

    private static Set<String> strings(CompoundTag s, String key) {
        Set<String> out = new HashSet<>();
        ListTag l = s.getList(key, Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) out.add(l.getString(i));
        return out;
    }

    private static Set<Integer> ints(CompoundTag s, String key) {
        Set<Integer> out = new HashSet<>();
        for (int i : s.getIntArray(key)) out.add(i);
        return out;
    }

    private void rebuild() {
        List<Component> src = switch (tab) {
            case 0 -> progress();
            case 1 -> memories();
            case 2 -> characters();
            case 3 -> places();
            default -> List.of();
        };
        List<FormattedCharSequence> out = new ArrayList<>();
        for (Component c : src) {
            if (c.getString().isEmpty()) out.add(FormattedCharSequence.EMPTY);
            else out.addAll(this.font.split(c, COL_W));
        }
        lines = out;
        spread = Math.max(0, Math.min(spread, spreads() - 1));
        updateButtons();
    }

    private List<Component> progress() {
        CompoundTag s = snap();
        List<Component> o = new ArrayList<>();
        int ch = s.contains("chapter") ? s.getInt("chapter") : 1;

        o.add(col(t("lr.ui.sec.chapters"), HEAD));
        for (int i = 1; i <= 4; i++) {
            Component name = i <= ch ? t("lr.chapter." + i) : Component.literal("???");
            o.add(col(t("lr.ui.chapter", i, name), i <= ch ? TEXT : DIM));
            String st = i < ch ? "completed" : i == ch ? "progress" : "locked";
            o.add(col(t("lr.ui.state." + st), i < ch ? OK : i == ch ? WARN : DIM));
        }

        o.add(Component.empty());
        o.add(col(t("lr.ui.sec.areas"), HEAD));
        CompoundTag areas = s.getCompound("areas");
        for (AreaDef a : Areas.ALL) {
            int st = areas.getInt(a.id);
            boolean hide = st == 0 || (!a.listed && st <= 2);
            if (hide) {
                o.add(col(Component.literal("???"), DIM));
            } else {
                o.add(col(t("lr.ui.dash", t("lr.area." + a.id), t("lr.ui.area." + st)),
                        st == 5 ? OK : st >= 3 ? TEXT : DIM));
            }
        }

        o.add(Component.empty());
        o.add(col(t("lr.ui.sec.puzzles"), HEAD));
        Set<String> done = strings(s, "puzzlesDone");
        Set<String> seen = strings(s, "puzzlesSeen");
        o.add(t("lr.ui.puzzles_done", done.size(), Puzzles.ALL.size()));
        int unseen = 0;
        for (Puzzles.P p : Puzzles.ALL) {
            if (seen.contains(p.id()) || done.contains(p.id())) {
                boolean d = done.contains(p.id());
                o.add(col(t("lr.ui.puzzle_line", d ? "[x]" : "[ ]", t("lr.puzzle." + p.id())), d ? OK : TEXT));
            } else {
                unseen++;
            }
        }
        if (unseen > 0) o.add(col(Component.literal("???"), DIM));

        o.add(Component.empty());
        o.add(col(t("lr.ui.sec.memories"), HEAD));
        Set<Integer> mem = ints(s, "memories");
        o.add(t("lr.ui.memories_count", mem.size(), Memories.TOTAL));
        for (int id = 1; id <= Memories.TOTAL; id++) {
            boolean have = mem.contains(id);
            String star = Memories.KEY.contains(id) ? " *" : Memories.optional(id) ? " ~" : "";
            o.add(col(Component.literal(String.format("%02d%s  ", id, star)).append(
                    have ? t("lr.ui.found") : Component.literal("???")), have ? TEXT : DIM));
        }

        o.add(Component.empty());
        o.add(col(t("lr.ui.sec.characters"), HEAD));
        CompoundTag talks = s.getCompound("talks");
        for (NpcKind k : NpcKind.values()) {
            int n = talks.getInt(k.id);
            if (n > 0) o.add(col(t("lr.ui.character_line", t("lr.npc." + k.id), Math.min(n, k.lines), k.lines), TEXT));
            else o.add(col(Component.literal("???"), DIM));
        }

        o.add(Component.empty());
        o.add(col(t("lr.ui.sec.secrets"), HEAD));
        int rooms = s.getInt("secretRooms");
        int hidden = s.getInt("hiddenEvents");
        int syms = s.getIntArray("symbols").length;
        int opt = 0;
        for (int id : mem) if (Memories.optional(id)) opt++;
        int endings = s.getList("endings", Tag.TAG_STRING).size();
        if (ch >= 3 || rooms + hidden + syms + opt + endings > 0) {
            o.add(t("lr.ui.secret_rooms", rooms, 6));
            o.add(t("lr.ui.hidden_events", hidden, 8));
            o.add(t("lr.ui.symbols_count", syms, 12));
            o.add(t("lr.ui.optional_memories", opt, 10));
            o.add(t("lr.ui.endings_count", endings, 4));
        } else {
            o.add(col(Component.literal("???"), DIM));
        }

        o.add(Component.empty());
        o.add(col(t("lr.ui.soon"), DIM));
        o.add(Component.empty());
        o.add(col(t("lr.ui.overall", s.getInt("pct")), HEAD));
        return o;
    }

    private List<Component> memories() {
        CompoundTag s = snap();
        List<Component> o = new ArrayList<>();
        int[] ids = s.getIntArray("memories");
        if (ids.length == 0) {
            o.add(col(Component.literal("???"), DIM));
            return o;
        }
        for (int id : ids) {
            String star = Memories.KEY.contains(id) ? " *" : "";
            o.add(col(Component.literal(String.format("No. %02d%s", id, star)), HEAD));
            o.add(t("lr.memory." + id));
            o.add(Component.empty());
        }
        return o;
    }

    private List<Component> characters() {
        CompoundTag s = snap();
        List<Component> o = new ArrayList<>();
        CompoundTag talks = s.getCompound("talks");
        CompoundTag last = s.getCompound("last");
        boolean any = false;
        for (NpcKind k : NpcKind.values()) {
            if (talks.getInt(k.id) <= 0) continue;
            any = true;
            o.add(col(t("lr.npc." + k.id), HEAD));
            o.add(col(t("lr.npc.desc." + k.id), DIM));
            String key = last.getString(k.id);
            if (!key.isEmpty()) o.add(t(key));
            o.add(Component.empty());
        }
        if (!any) o.add(col(Component.literal("???"), DIM));
        return o;
    }

    private List<Component> places() {
        CompoundTag s = snap();
        List<Component> o = new ArrayList<>();
        CompoundTag areas = s.getCompound("areas");
        boolean any = false;
        for (AreaDef a : Areas.ALL) {
            if (areas.getInt(a.id) < 3) continue;
            any = true;
            o.add(col(t("lr.area." + a.id), HEAD));
            o.add(t("lr.place." + a.id));
            o.add(Component.empty());
        }
        if (!any) o.add(col(Component.literal("???"), DIM));
        return o;
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
        g.fill(left - 4, top - 4, left + W + 4, top + H + 4, 0xFF2B1D14);
        g.fill(left, top, left + W, top + H, 0xFFE8DDC0);
        g.fill(left + W / 2 - 1, top, left + W / 2 + 1, top + H, 0xFFB9A883);
        for (int y = top + 8; y < top + H - 8; y += 14) {
            g.fill(left + W / 2 - 3, y, left + W / 2 + 3, y + 3, 0xFF5A4636);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawString(this.font, this.title, left + 10, top + 5, HEAD, false);
        int shimmer = 110 + (int) (60 * Math.sin(Util.getMillis() / 500.0));
        g.fill(left + 10, top + 15, left + 80, top + 16, (shimmer << 24) | 0x7A5A3A);
        if (tab == 4) {
            renderSymbols(g);
        } else {
            renderLines(g);
            String page = (spread + 1) + " / " + spreads();
            g.drawCenteredString(this.font, page, left + W / 2, top + H - 18, DIM);
        }
    }

    private void renderLines(GuiGraphics g) {
        int y0 = top + 22;
        for (int col = 0; col < 2; col++) {
            int x = left + (col == 0 ? 12 : W / 2 + 10);
            for (int i = 0; i < COL_LINES; i++) {
                int idx = spread * COL_LINES * 2 + col * COL_LINES + i;
                if (idx >= lines.size()) break;
                g.drawString(this.font, lines.get(idx), x, y0 + i * LH, TEXT, false);
            }
        }
    }

    private void renderSymbols(GuiGraphics g) {
        Set<Integer> have = ints(snap(), "symbols");
        g.drawCenteredString(this.font, t("lr.ui.symbols_count", have.size(), 12), left + W / 2, top + 22, HEAD);
        for (int i = 0; i < 12; i++) {
            int cx = left + 18 + (i % 4) * 74;
            int cy = top + 40 + (i / 4) * 50;
            g.fill(cx, cy, cx + 62, cy + 42, 0x22000000);
            if (have.contains(i)) {
                SymbolArt.draw(g, i, cx + 19, cy + 9, 5, 0xFF3B2F2F);
            } else {
                g.drawCenteredString(this.font, "?", cx + 31, cy + 17, DIM);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ClientModEvents.JOURNAL.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        if (keyCode == 263) { // left arrow
            flip(-1);
            return true;
        }
        if (keyCode == 262) { // right arrow
            flip(1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
