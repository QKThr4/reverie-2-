package com.liminalreverie.world;

import com.liminalreverie.block.MemoryFragmentBlock;
import com.liminalreverie.block.SymbolMarkBlock;
import com.liminalreverie.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Collects placement steps (relative to an area origin) that are executed later, a few thousand blocks per
 * tick, by {@link BuildManager}. Small edits (phases) run instantly through {@link #runNow()}.
 * Placement uses flags that skip neighbour updates, so building never cascades.
 */
public final class Build {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    public static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public interface Step {
        /** Runs as much as the budget allows. @return true when finished. */
        boolean run(ServerLevel level, int[] budget);
    }

    private static final class Fill implements Step {
        private final int x1, y1, z1, x2, y2, z2;
        private final BlockState state;
        private int x, y, z;

        Fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
            this.x1 = x1; this.y1 = y1; this.z1 = z1;
            this.x2 = x2; this.y2 = y2; this.z2 = z2;
            this.state = state;
            this.x = x1; this.y = y1; this.z = z1;
        }

        @Override
        public boolean run(ServerLevel level, int[] budget) {
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            while (budget[0] > 0) {
                p.set(x, y, z);
                level.setBlock(p, state, FLAGS);
                budget[0]--;
                if (++x > x2) {
                    x = x1;
                    if (++z > z2) {
                        z = z1;
                        if (++y > y2) return true;
                    }
                }
            }
            return false;
        }
    }

    private static final class Act implements Step {
        private final Consumer<ServerLevel> action;

        Act(Consumer<ServerLevel> action) {
            this.action = action;
        }

        @Override
        public boolean run(ServerLevel level, int[] budget) {
            action.accept(level);
            budget[0]--;
            return true;
        }
    }

    public final AreaDef def;
    private final ServerLevel level;
    private final List<Step> steps = new ArrayList<>();

    public Build(ServerLevel level, AreaDef def) {
        this.level = level;
        this.def = def;
    }

    public List<Step> steps() { return steps; }

    public ServerLevel level() { return level; }

    public static BlockState st(Block b) { return b.defaultBlockState(); }

    // ------------------------------------------------------------------ primitives

    public void box(int x1, int y1, int z1, int x2, int y2, int z2, BlockState s) {
        steps.add(new Fill(def.ox + Math.min(x1, x2), def.oy + Math.min(y1, y2), def.oz + Math.min(z1, z2),
                def.ox + Math.max(x1, x2), def.oy + Math.max(y1, y2), def.oz + Math.max(z1, z2), s));
    }

    public void set(int x, int y, int z, BlockState s) { box(x, y, z, x, y, z, s); }

    public void air(int x1, int y1, int z1, int x2, int y2, int z2) { box(x1, y1, z1, x2, y2, z2, AIR); }

    public void act(Consumer<ServerLevel> a) { steps.add(new Act(a)); }

    /** Hollow room: walls sit on the given coordinates, floor one block below y0, ceiling at y0 + h. */
    public void room(int x1, int y0, int z1, int x2, int z2, int h, BlockState wall, BlockState floor, BlockState ceil) {
        box(x1, y0 - 1, z1, x2, y0 - 1, z2, floor);
        box(x1, y0 + h, z1, x2, y0 + h, z2, ceil);
        box(x1, y0, z1, x2, y0 + h - 1, z1, wall);
        box(x1, y0, z2, x2, y0 + h - 1, z2, wall);
        box(x1, y0, z1, x1, y0 + h - 1, z2, wall);
        box(x2, y0, z1, x2, y0 + h - 1, z2, wall);
    }

    public void room(int x1, int z1, int x2, int z2, int h, BlockState wall, BlockState floor, BlockState ceil) {
        room(x1, 0, z1, x2, z2, h, wall, floor, ceil);
    }

    // ------------------------------------------------------------------ furniture & story objects

    public void door(int x, int y, int z, Direction facing, Block door) {
        BlockState lo = door.defaultBlockState().setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        set(x, y, z, lo);
        set(x, y + 1, z, lo.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    /** Foot at (x,y,z); the head sits one block towards {@code facing}. */
    public void bed(int x, int y, int z, Direction facing, Block bed) {
        BlockState foot = bed.defaultBlockState().setValue(BedBlock.FACING, facing).setValue(BedBlock.PART, BedPart.FOOT);
        set(x, y, z, foot);
        set(x + facing.getStepX(), y, z + facing.getStepZ(), foot.setValue(BedBlock.PART, BedPart.HEAD));
    }

    public void stairs(int x, int y, int z, Block b, Direction backSide) {
        set(x, y, z, Pal.stairs(b, backSide));
    }

    public void fragment(int x, int y, int z, int memoryId) {
        set(x, y, z, ModBlocks.MEMORY_FRAGMENT.get().defaultBlockState()
                .setValue(MemoryFragmentBlock.MEMORY, Math.max(0, Math.min(31, memoryId - 1))));
    }

    public void symbol(int x, int y, int z, int symbol) {
        set(x, y, z, ModBlocks.SYMBOL_MARK.get().defaultBlockState()
                .setValue(SymbolMarkBlock.SYMBOL, Math.max(0, Math.min(11, symbol))));
    }

    /** Wall sign at (x,y,z) facing {@code facing}; it attaches to the wall block behind it. */
    public void sign(int x, int y, int z, Direction facing, Component... lines) {
        BlockPos p = def.pos(x, y, z);
        act(l -> {
            l.setBlock(p, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, facing), FLAGS);
            if (l.getBlockEntity(p) instanceof SignBlockEntity be) {
                SignText text = new SignText().setColor(DyeColor.BLACK);
                for (int i = 0; i < lines.length && i < 4; i++) text = text.setMessage(i, lines[i]);
                be.setText(text, true);
                be.setWaxed(true);
            }
        });
    }

    /** Executes every queued step immediately (used for small phase edits). */
    public void runNow() {
        int[] budget = {Integer.MAX_VALUE / 2};
        for (Step s : steps) {
            while (!s.run(level, budget)) {
                budget[0] = Integer.MAX_VALUE / 2;
            }
        }
        steps.clear();
    }
}
