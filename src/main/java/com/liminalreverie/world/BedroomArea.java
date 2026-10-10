package com.liminalreverie.world;

import com.liminalreverie.entity.NpcKind;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.story.Dialogue;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * The Bedroom: a small nostalgic bedroom that changes with the story chapter (see Phases.syncTo).
 * The Sleeper rests in the second bed once the story reaches the people who are still here.
 */
public final class BedroomArea {
    public static final AreaDef DEF = new AreaDef("bedroom", 9, false)
            .spawn(0.5, 0, 2.0, 180.0F)
            .bounds(-6, 6, -5, 5)
            .phases(3, BedroomArea::phase)
            .gate(d -> d.chapter >= 3)
            .builder(BedroomArea::build)
            .triggers(BedroomArea::triggers)
            .npcs(List.of(new NpcPlacement(NpcKind.SLEEPER, 3.5, 0.5, 0.5, 90.0F, "sleeper", d -> d.chapter >= 3, true)))
            .ambient(() -> ModSounds.AMB_SAFE.get())
            .fog(235, 205, 205, 50)
            .safe();

    private BedroomArea() {}

    static void build(Build b) {
        b.room(-4, -3, 4, 3, 4, Build.st(Blocks.PINK_TERRACOTTA), Build.st(Blocks.OAK_PLANKS), Build.st(Blocks.WHITE_CONCRETE));
        b.set(0, 4, 0, Pal.light());
        b.bed(-3, 0, 0, Direction.NORTH, Blocks.WHITE_BED);        // the player's bed (foot z=0, head z=-1)
        b.bed(3, 0, 1, Direction.NORTH, Blocks.WHITE_BED);         // the second bed (head at 3,0,0)
        b.set(-1, 0, -2, Build.st(Blocks.OAK_SLAB));               // desk
        b.set(0, 0, -2, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.SOUTH));
        b.box(-1, 1, -3, 1, 2, -3, Build.st(Blocks.GLASS));        // window
        b.box(-1, 0, 0, 1, 0, 1, Build.st(Blocks.RED_CARPET));     // rug
        b.set(-2, 0, 2, Build.st(Blocks.RED_WOOL));
        b.set(-1, 0, 2, Build.st(Blocks.YELLOW_WOOL));
        b.set(-2, 1, 2, Build.st(Blocks.BLUE_WOOL));
        b.set(2, 0, -2, Build.st(Blocks.POTTED_FERN));
        b.door(4, 0, -1, Direction.WEST, Blocks.OAK_DOOR);          // back to the lounge
        b.fragment(-3, 1, 2, 21);
        b.fragment(2, 1, 2, 22);
        b.fragment(3, 1, -2, 30);
    }

    static void phase(Build b, int n) {
        BlockState air = Build.AIR;
        switch (n) {
            case 1 -> {
                b.box(-1, 1, -3, 1, 2, -3, Build.st(Blocks.LIGHT_BLUE_STAINED_GLASS));
                b.set(-3, 0, -2, Build.st(Blocks.POTTED_FERN));
            }
            case 2 -> {
                b.box(-1, 1, -3, 1, 2, -3, air);                   // the window is open now
                b.set(1, 0, 2, Build.st(Blocks.GREEN_WOOL));
                b.set(2, 0, 2, Build.st(Blocks.LIGHT_BLUE_WOOL));
            }
            case 3 -> b.set(0, 4, 0, Build.st(Blocks.SHROOMLIGHT));
            default -> { }
        }
    }

    static void triggers(AreaDef d) {
        Triggers.putDoor(d, 4, 0, -1, Triggers.back("bedroom", "lounge"));
        // clicking the Sleeper's bed talks to the Sleeper (the bed is easier to hit than the person in it)
        Triggers.T talk = (p, l, pos) -> Dialogue.talk(p, NpcKind.SLEEPER);
        Triggers.put(d.pos(3, 0, 0), talk);
        Triggers.put(d.pos(3, 0, 1), talk);
        Triggers.put(d.pos(0, 0, -2), Triggers.note("lr.note.bedroom_desk"));
    }
}
