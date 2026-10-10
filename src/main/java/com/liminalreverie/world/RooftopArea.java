package com.liminalreverie.world;

import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.Puzzles;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/** The Rooftop: an impossible city at sunset. Pure resting place. */
public final class RooftopArea {
    public static final AreaDef DEF = new AreaDef("rooftop", 7, false)
            .spawn(0.5, 0, 0.5, 0.0F)
            .bounds(-100, 100, -100, 100)
            .gate(d -> Puzzles.areaDone(d, "hotel"))
            .builder(RooftopArea::build)
            .triggers(RooftopArea::triggers)
            .npcs(List.of())
            .ambient(() -> ModSounds.AMB_SAFE.get())
            .fog(255, 160, 140, 120)
            .safe();

    private RooftopArea() {}

    static void build(Build b) {
        b.box(-20, -1, -15, 20, -1, 15, Build.st(Blocks.GRAY_CONCRETE));
        b.box(-20, 0, -15, 20, 1, -15, Build.st(Blocks.LIGHT_GRAY_CONCRETE));
        b.box(-20, 0, 15, 20, 1, 15, Build.st(Blocks.LIGHT_GRAY_CONCRETE));
        b.box(-20, 0, -15, -20, 1, 15, Build.st(Blocks.LIGHT_GRAY_CONCRETE));
        b.box(20, 0, -15, 20, 1, 15, Build.st(Blocks.LIGHT_GRAY_CONCRETE));

        // benches and lamp posts
        for (int x = -12; x <= 12; x += 12) {
            for (int i = 0; i < 3; i++) b.stairs(x + i, 0, 6, Blocks.SPRUCE_STAIRS, Direction.SOUTH);
        }
        for (int x : new int[]{-18, 18}) for (int z : new int[]{-13, 13}) {
            b.box(x, 0, z, x, 2, z, Build.st(Blocks.OAK_FENCE));
            b.set(x, 3, z, Build.st(Blocks.LANTERN));
        }

        // the impossible city: tall pale towers with glowing bands, all at a distance
        RandomSource rnd = RandomSource.create(1977L);
        for (int i = 0; i < 18; i++) {
            double ang = (Math.PI * 2.0) * i / 18.0 + rnd.nextDouble() * 0.2;
            int dist = 45 + rnd.nextInt(45);
            int x = (int) Math.round(Math.cos(ang) * dist);
            int z = (int) Math.round(Math.sin(ang) * dist);
            int w = 5 + rnd.nextInt(4);
            int h = 25 + rnd.nextInt(40);
            b.box(x, -30, z, x + w, h, z + w, Build.st(Blocks.LIGHT_GRAY_CONCRETE));
            for (int y = 4; y < h; y += 5) {
                b.box(x, y, z, x + w, y, z + w, Build.st(Blocks.YELLOW_STAINED_GLASS));
            }
        }

        b.door(-18, 0, 0, Direction.EAST, Blocks.OAK_DOOR);     // a door standing on the roof, back to the lobby
        b.fragment(10, 1, -10, 19);
        b.fragment(19, 1, 14, 29);
        b.symbol(0, 0, -15, 11);
    }

    static void triggers(AreaDef d) {
        Triggers.putDoor(d, -18, 0, 0, Triggers.travel("lobby"));
    }
}
