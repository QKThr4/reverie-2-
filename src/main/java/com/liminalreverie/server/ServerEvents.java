package com.liminalreverie.server;

import com.liminalreverie.LiminalReverie;
import com.liminalreverie.Reverie;
import com.liminalreverie.command.ReverieCommands;
import com.liminalreverie.entity.LiminalNpc;
import com.liminalreverie.story.Scheduler;
import com.liminalreverie.story.StoryData;
import com.liminalreverie.story.StoryManager;
import com.liminalreverie.world.Areas;
import com.liminalreverie.world.BuildManager;
import com.liminalreverie.world.OverworldHaunt;
import com.liminalreverie.world.Transit;
import com.liminalreverie.world.Triggers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Game-bus (server logic) events. Each handler is a static method registered exactly once by the annotation. */
@EventBusSubscriber(modid = LiminalReverie.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class ServerEvents {
    private static final Set<UUID> DIED_IN_REVERIE = new HashSet<>();

    private ServerEvents() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        MinecraftServer server = e.getServer();
        Scheduler.tick();
        BuildManager.tick(server);
        OverworldHaunt.cleanupTick(server);
        if (server.getTickCount() % 20 == 0) {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) ReverieTicker.second(p);
            OverworldHaunt.second(server);
        }
    }

    // ---------------------------------------------------------------- joining

    private static boolean anyoneInReverie(MinecraftServer server, UUID except) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (!p.getUUID().equals(except) && Reverie.in(p.level())) return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        MinecraftServer server = sp.server;
        StoryData d = StoryData.get(server);
        UUID id = sp.getUUID();
        d.knownPlayers.add(id);
        if (Reverie.in(sp.level())) d.enteredPlayers.add(id);
        if (d.welcomed.add(id)) {
            sp.sendSystemMessage(Component.translatable("lr.msg.welcome").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        d.setDirty();
        // Someone is already inside the Reverie: a newcomer goes straight to the Lobby to join them.
        if (!d.enteredPlayers.contains(id) && anyoneInReverie(server, id)) {
            Scheduler.later(60, () -> {
                ServerPlayer p2 = server.getPlayerList().getPlayer(id);
                if (p2 != null && !Reverie.in(p2.level())) Transit.enter(p2, Areas.byId("lobby"));
            });
        }
        StoryManager.sendJournal(sp, false);
    }

    /** Dying inside the dream sends you back to the Lobby (until someone reaches an ending). */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && Reverie.in(sp.level())) {
            DIED_IN_REVERIE.add(sp.getUUID());
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        UUID id = sp.getUUID();
        if (!DIED_IN_REVERIE.remove(id)) return;
        MinecraftServer server = sp.server;
        if (!StoryData.get(server).endings.isEmpty()) return;
        Scheduler.later(20, () -> {
            ServerPlayer p2 = server.getPlayerList().getPlayer(id);
            if (p2 != null) Transit.force(p2, Areas.byId("lobby"));
        });
    }

    // ---------------------------------------------------------------- interaction

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        if (e.getLevel().isClientSide() || e.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(e.getEntity() instanceof ServerPlayer sp) || !(e.getLevel() instanceof ServerLevel sl)) return;
        BlockPos pos = e.getPos();
        if (Reverie.in(sl)) {
            Triggers.T t = Triggers.get(pos);
            if (t != null) {
                t.use(sp, sl, pos);
                cancel(e);
            }
        } else if (sl.dimension() == Level.OVERWORLD) {
            StoryData d = StoryData.get(sp.server);
            if (d.portalBlocks.contains(pos.asLong())) {
                Transit.enter(sp, Areas.byId("lobby"));
                cancel(e);
            }
        }
    }

    private static void cancel(PlayerInteractEvent.RightClickBlock e) {
        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.SUCCESS);
    }

    /** Keeps the dream intact (no breaking/placing in the Reverie except creative) and keeps portal doors in place. */
    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent e) {
        Player p = e.getPlayer();
        if (p.isCreative() || !(e.getLevel() instanceof Level lvl)) return;
        if (Reverie.in(lvl)) {
            e.setCanceled(true);
        } else if (lvl instanceof ServerLevel sl && lvl.dimension() == Level.OVERWORLD
                && StoryData.get(sl.getServer()).portalBlocks.contains(e.getPos().asLong())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (e.getEntity() instanceof Player p && !p.isCreative()
                && e.getLevel() instanceof Level lvl && Reverie.in(lvl)) {
            e.setCanceled(true);
        }
    }

    /** Duplicate / stale characters (after a reset, or a restored backup) are dropped instead of added. */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide()) return;
        if (e.getEntity() instanceof LiminalNpc npc && e.getLevel() instanceof ServerLevel sl) {
            String key = npc.placementKey();
            if (key.isEmpty()) return;
            StoryData d = StoryData.get(sl.getServer());
            UUID registered = d.npcs.get(key);
            if (npc.epoch() != d.epoch || registered == null || !registered.equals(npc.getUUID())) {
                e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onStopping(ServerStoppingEvent e) {
        BuildManager.abortAll(e.getServer());
        Scheduler.clear();
        ReverieTicker.clear();
        DIED_IN_REVERIE.clear();
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent e) {
        ReverieCommands.register(e.getDispatcher());
    }
}
