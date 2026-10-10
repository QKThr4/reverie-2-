package com.liminalreverie.client;

import net.minecraft.client.gui.GuiGraphics;

/** The twelve symbols as 5x5 pixel art (the block textures are generated from the same patterns). */
public final class SymbolArt {
    private static final String[][] PATTERNS = {
            {".###.", "#...#", "#...#", "#...#", ".###."},
            {"..#..", "..#..", "#####", "..#..", "..#.."},
            {"..#..", ".#.#.", "#...#", "#...#", "#####"},
            {"..#..", ".#.#.", "#...#", ".#.#.", "..#.."},
            {"#####", "#...#", "#...#", "#...#", "#####"},
            {"#...#", ".#.#.", "..#..", ".#.#.", "#...#"},
            {"..#..", "#.#.#", ".#.#.", ".....", "....."},
            {"..#..", ".###.", "#.#.#", "..#..", "..#.."},
            {"#.#.#", ".....", "#.#.#", ".....", "#.#.#"},
            {".....", ".###.", "#.#.#", ".###.", "....."},
            {".###.", "#...#", "#.#.#", "#...#", "#####"},
            {"#####", "#....", "#.###", "#...#", "#####"}};

    private SymbolArt() {}

    public static void draw(GuiGraphics g, int symbol, int x, int y, int px, int color) {
        if (symbol < 0 || symbol >= PATTERNS.length) return;
        String[] rows = PATTERNS[symbol];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (rows[r].charAt(c) == '#') g.fill(x + c * px, y + r * px, x + (c + 1) * px, y + (r + 1) * px, color);
            }
        }
    }
}
