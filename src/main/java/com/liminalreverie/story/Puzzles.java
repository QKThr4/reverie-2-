package com.liminalreverie.story;

import com.liminalreverie.registry.ModItems;
import com.liminalreverie.registry.ModSounds;
import com.liminalreverie.world.MallArea;
import com.liminalreverie.world.PoolArea;
import com.liminalreverie.world.TrainArea;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** The twelve environmental puzzles. Sequence puzzles store their partial progress in StoryData. */
public final class Puzzles {
    public record P(String id, String area, int[] seq) {}

    public static final List<P> ALL = List.of(
            new P("lobby_symbols", "lobby", null),
            new P("school_classroom", "school", null),
            new P("school_lockers", "school", new int[]{2, 7, 4}),
            new P("pool_sequence", "pool", new int[]{1, 2, 0, 3}),
            new P("pool_drain", "pool", null),
            new P("hotel_elevator", "hotel", new int[]{2, 0, 3}),
            new P("hotel_loop", "hotel", new int[]{1, 0, 1}),
            new P("mall_arcade", "mall", new int[]{1, 3, 0, 2}),
            new P("mall_shop", "mall", null),
            new P("train_ticket", "train", null),
            new P("fields_poles", "fields", new int[]{2, 0, 4, 1}),
            new P("last_clock", "last_room", new int[]{1, 3, 0, 2}));

    private Puzzles() {}

    public static P get(String id) {
        for (P p : ALL) if (p.id().equals(id)) return p;
        return null;
    }

    public static List<P> forArea(String area) {
        List<P> out = new ArrayList<>();
        for (P p : ALL) if (p.area().equals(area)) out.add(p);
        return out;
    }

    /** True when the area has puzzles and all of them are solved. */
    public static boolean areaDone(StoryData d, String area) {
        List<P> ps = forArea(area);
        if (ps.isEmpty()) return false;
        for (P p : ps) if (!d.puzzlesDone.contains(p.id())) return false;
        return true;
    }

    /** One step of a sequence puzzle. Any wrong step resets the sequence. */
    public static void stepSeq(ServerPlayer p, ServerLevel l, BlockPos pos, String id, int idx) {
        P pz = get(id);
        if (pz == null || pz.seq() == null) return;
        StoryData d = StoryData.get(p.server);
        StoryManager.seen(p, id);
        if (d.puzzlesDone.contains(id)) {
            Fx.hint(p, Component.translatable("lr.msg.solved").withStyle(ChatFormatting.GRAY), true);
            // lost the key before using it? the lockers give it back
            if (id.equals("school_lockers") && !d.puzzlesDone.contains("mall_shop")
                    && !p.getInventory().contains(new ItemStack(ModItems.BRASS_KEY.get()))) {
                StoryManager.giveItem(p, new ItemStack(ModItems.BRASS_KEY.get()));
            }
            return;
        }
        int prog = d.seq.getOrDefault(id, 0);
        if (prog >= pz.seq().length) prog = 0;
        if (pz.seq()[prog] == idx) {
            prog++;
            if (prog >= pz.seq().length) {
                d.seq.remove(id);
                d.setDirty();
                l.playSound(null, pos, ModSounds.SOLVE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                complete(p, id);
            } else {
                d.seq.put(id, prog);
                d.setDirty();
                l.playSound(null, pos, ModSounds.CHIME.get(), SoundSource.BLOCKS, 1.0F, 0.8F + 0.12F * prog);
            }
        } else {
            d.seq.remove(id);
            d.setDirty();
            l.playSound(null, pos, ModSounds.WRONG.get(), SoundSource.BLOCKS, 0.8F, 0.8F);
            Fx.hint(p, Component.translatable("lr.msg.reset").withStyle(ChatFormatting.GRAY), true);
        }
    }

    /** Marks a puzzle solved and applies its world effect / reward. */
    public static void complete(ServerPlayer p, String id) {
        StoryData d = StoryData.get(p.server);
        if (d.puzzlesDone.contains(id)) return;
        StoryManager.completePuzzle(p, id);
        ServerLevel level = p.serverLevel();
        switch (id) {
            case "school_lockers" -> StoryManager.giveItem(p, new ItemStack(ModItems.BRASS_KEY.get()));
            case "mall_shop" -> {
                MallArea.openShop(level);
                StoryManager.giveItem(p, new ItemStack(ModItems.FADED_TICKET.get()));
            }
            case "pool_drain" -> {
                PoolArea.openDrain(level);
                StoryManager.addSecretRoom(p, "pool");
            }
            case "train_ticket" -> TrainArea.ride(p);
            default -> { }
        }
        Fx.hint(p, Component.translatable("lr.puzzle." + id + ".done").withStyle(ChatFormatting.AQUA), true);
    }
}
