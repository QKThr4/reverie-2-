package com.liminalreverie.world;

import com.liminalreverie.story.StoryManager;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Small hidden rooms. In every area they sit at the same relative spot (200, 200), well away from the main build. */
public final class Secret {
    public static final int CX = 200;
    public static final int CZ = 200;

    private Secret() {}

    public static void build(Build b, int fragmentId) {
        BlockState wp = Pal.wallpaper();
        b.room(CX - 4, CZ - 4, CX + 4, CZ + 4, 5, wp, Pal.carpet(), wp);
        b.set(CX, 5, CZ, Pal.light());
        b.fragment(CX, 1, CZ, fragmentId);
        b.door(CX, 0, CZ + 4, Direction.NORTH, Blocks.OAK_DOOR);
    }

    /** Entry trigger at (ex,ey,ez); the exit door inside leads back to (rx,ry,rz). */
    public static void triggers(AreaDef def, int ex, int ey, int ez, String key, double rx, double ry, double rz, float ryaw) {
        Triggers.put(def.pos(ex, ey, ez), (p, l, pos) -> {
            StoryManager.addSecretRoom(p, key);
            Transit.hop(p, def, CX + 0.5, 0, CZ - 1.5, 0.0F);
        });
        Triggers.T back = (p, l, pos) -> Transit.hop(p, def, rx, ry, rz, ryaw);
        Triggers.put(def.pos(CX, 0, CZ + 4), back);
        Triggers.put(def.pos(CX, 1, CZ + 4), back);
    }
}
