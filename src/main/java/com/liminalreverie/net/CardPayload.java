package com.liminalreverie.net;

import com.liminalreverie.Reverie;
import com.liminalreverie.client.ClientHooks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server -> client: show a centred, word-wrapped text card (used by the endings) for some ticks. */
public record CardPayload(String key, int ticks) implements CustomPacketPayload {
    public static final Type<CardPayload> TYPE = new Type<>(Reverie.id("card"));
    public static final StreamCodec<ByteBuf, CardPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CardPayload::key,
            ByteBufCodecs.VAR_INT, CardPayload::ticks,
            CardPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CardPayload p, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientHooks.onCard(p.key(), p.ticks()));
    }
}
