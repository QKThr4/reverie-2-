package com.liminalreverie.net;

import com.liminalreverie.Reverie;
import com.liminalreverie.story.Fx;
import com.liminalreverie.story.StoryManager;
import net.minecraft.network.chat.Component;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server: the journal key was pressed. The server answers with a SyncPayload that opens the screen. */
public record RequestJournalPayload() implements CustomPacketPayload {
    public static final Type<RequestJournalPayload> TYPE = new Type<>(Reverie.id("request_journal"));
    public static final StreamCodec<ByteBuf, RequestJournalPayload> CODEC = StreamCodec.unit(new RequestJournalPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestJournalPayload p, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) {
                if (StoryManager.canOpenJournal(sp)) StoryManager.sendJournal(sp, true);
                else Fx.hint(sp, Component.translatable("lr.msg.journal_locked"), true);
            }
        });
    }
}
