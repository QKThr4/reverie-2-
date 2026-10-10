package com.liminalreverie.net;

import com.liminalreverie.Reverie;
import com.liminalreverie.client.ClientHooks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server -> client: a purely visual/audio dream event (silence, fog shift, sky blush) for some ticks. */
public record EventPayload(String id, int ticks) implements CustomPacketPayload {
    public static final Type<EventPayload> TYPE = new Type<>(Reverie.id("event"));
    public static final StreamCodec<ByteBuf, EventPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, EventPayload::id,
            ByteBufCodecs.VAR_INT, EventPayload::ticks,
            EventPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EventPayload p, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientHooks.onEvent(p.id(), p.ticks()));
    }
}
