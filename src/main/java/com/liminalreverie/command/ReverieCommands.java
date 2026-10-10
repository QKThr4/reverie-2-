package com.liminalreverie.command;

import com.liminalreverie.Reverie;
import com.liminalreverie.entity.LiminalNpc;
import com.liminalreverie.story.DreamEvents;
import com.liminalreverie.story.Endings;
import com.liminalreverie.story.Memories;
import com.liminalreverie.story.Puzzles;
import com.liminalreverie.story.StoryData;
import com.liminalreverie.story.StoryManager;
import com.liminalreverie.world.AreaDef;
import com.liminalreverie.world.Areas;
import com.liminalreverie.world.BuildManager;
import com.liminalreverie.world.Transit;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/** Testing commands. All require operator permission (cheats on in singleplayer). */
public final class ReverieCommands {
    private ReverieCommands() {}

    private static List<String> areaIds() {
        List<String> ids = new ArrayList<>();
        for (AreaDef a : Areas.ALL) ids.add(a.id);
        ids.add("overworld");
        return ids;
    }

    private static List<String> eventIds() {
        List<String> ids = new ArrayList<>();
        for (DreamEvents e : DreamEvents.values()) ids.add(e.id);
        return ids;
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("reverie")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("progress").executes(c -> progress(c.getSource())))
                .then(Commands.literal("chapter")
                        .then(Commands.argument("number", IntegerArgumentType.integer(1, 5))
                                .executes(c -> chapter(c, IntegerArgumentType.getInteger(c, "number")))))
                .then(Commands.literal("teleport")
                        .then(Commands.argument("area", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(areaIds(), b))
                                .executes(c -> teleport(c, StringArgumentType.getString(c, "area")))))
                .then(Commands.literal("memory")
                        .then(Commands.argument("id", IntegerArgumentType.integer(1, Memories.TOTAL))
                                .executes(c -> memory(c, IntegerArgumentType.getInteger(c, "id")))))
                .then(Commands.literal("event")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(eventIds(), b))
                                .executes(c -> event(c, StringArgumentType.getString(c, "id")))))
                .then(Commands.literal("reset").executes(c -> reset(c)))
                .then(Commands.literal("ending")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(Endings.ids(), b))
                                .executes(c -> ending(c, StringArgumentType.getString(c, "id"))))));
    }

    private static int progress(CommandSourceStack src) {
        StoryData d = StoryData.get(src.getServer());
        String msg = "Chapter " + d.chapter + " | " + StoryManager.percent(d) + "% | memories " + d.memories.size() + "/"
                + Memories.TOTAL + " | puzzles " + d.puzzlesDone.size() + "/" + Puzzles.ALL.size()
                + " | symbols " + d.symbols.size() + "/12 | endings " + d.endings;
        src.sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }

    private static int chapter(CommandContext<CommandSourceStack> c, int n) throws CommandSyntaxException {
        ServerPlayer sp = c.getSource().getPlayerOrException();
        StoryData d = StoryData.get(sp.server);
        d.chapter = n;
        StoryManager.finish(sp, d);
        c.getSource().sendSuccess(() -> Component.literal("Chapter set to " + n), true);
        return 1;
    }

    private static int teleport(CommandContext<CommandSourceStack> c, String id) throws CommandSyntaxException {
        ServerPlayer sp = c.getSource().getPlayerOrException();
        if (id.equals("overworld")) {
            Transit.toOverworld(sp);
            return 1;
        }
        AreaDef a = Areas.byId(id);
        if (a == null) {
            c.getSource().sendFailure(Component.literal("Unknown area: " + id));
            return 0;
        }
        Transit.force(sp, a);
        return 1;
    }

    private static int memory(CommandContext<CommandSourceStack> c, int id) throws CommandSyntaxException {
        ServerPlayer sp = c.getSource().getPlayerOrException();
        boolean added = StoryManager.collectMemory(sp, id);
        c.getSource().sendSuccess(() -> Component.literal(added ? "Memory " + id + " added" : "Already had memory " + id), true);
        return 1;
    }

    private static int event(CommandContext<CommandSourceStack> c, String id) throws CommandSyntaxException {
        ServerPlayer sp = c.getSource().getPlayerOrException();
        DreamEvents ev = DreamEvents.byId(id);
        if (ev == null) {
            c.getSource().sendFailure(Component.literal("Unknown event: " + id));
            return 0;
        }
        ev.trigger(sp);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer sp = c.getSource().getPlayerOrException();
        StoryData d = StoryData.get(sp.server);
        BuildManager.abortAll(sp.server);
        ServerLevel rl = Reverie.level(sp.server);
        if (rl != null) {
            List<Entity> doomed = new ArrayList<>();
            for (Entity e : rl.getAllEntities()) if (e instanceof LiminalNpc) doomed.add(e);
            for (Entity e : doomed) e.discard();
        }
        d.resetAll();
        sp.removeAllEffects();
        StoryManager.sendJournal(sp, false);
        c.getSource().sendSuccess(() -> Component.literal(
                "Story reset. Areas will be rebuilt on next entry (phase edits from before may remain)."), true);
        return 1;
    }

    private static int ending(CommandContext<CommandSourceStack> c, String id) throws CommandSyntaxException {
        ServerPlayer sp = c.getSource().getPlayerOrException();
        if (!Endings.valid(id)) {
            c.getSource().sendFailure(Component.literal("Unknown ending: " + id));
            return 0;
        }
        Endings.play(sp, id);
        return 1;
    }
}
