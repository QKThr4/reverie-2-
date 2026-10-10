package com.liminalreverie.net;

import com.liminalreverie.Reverie;
import com.liminalreverie.client.ClientHooks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server -> client: a subtle, temporary notification ("Memory Fragment discovered"). */
public record NotifyPayload(String key, String arg) implements CustomPacketPayload {
    public static final Type<NotifyPayload> TYPE = new Type<>(Reverie.id("notify"));
    public static final StreamCodec<ByteBuf, NotifyPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, NotifyPayload::key,
            ByteBufCodecs.STRING_UTF8, NotifyPayload::arg,
            NotifyPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(NotifyPayload p, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientHooks.onNotify(p.key(), p.arg()));
    }
}
