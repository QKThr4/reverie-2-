package com.liminalreverie.net;

import com.liminalreverie.Reverie;
import com.liminalreverie.client.ClientHooks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server -> client: the journal snapshot as SNBT text. {@code open} also opens the journal screen. */
public record SyncPayload(String snbt, boolean open) implements CustomPacketPayload {
    public static final Type<SyncPayload> TYPE = new Type<>(Reverie.id("sync"));
    public static final StreamCodec<ByteBuf, SyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncPayload::snbt,
            ByteBufCodecs.BOOL, SyncPayload::open,
            SyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPayload p, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientHooks.onSync(p.snbt(), p.open()));
    }
}
