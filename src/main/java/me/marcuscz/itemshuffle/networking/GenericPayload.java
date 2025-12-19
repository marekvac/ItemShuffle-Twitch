package me.marcuscz.itemshuffle.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record GenericPayload(Type<GenericPayload> id, FriendlyByteBuf buf) implements CustomPacketPayload {

    public static StreamCodec<FriendlyByteBuf, GenericPayload> CODEC = StreamCodec.ofMember(GenericPayload::write, GenericPayload::new);

    public GenericPayload(FriendlyByteBuf packetByteBuf) {
        this(
                new Type<>(packetByteBuf.readIdentifier()),
                new FriendlyByteBuf(packetByteBuf.readBytes(packetByteBuf.readVarInt()))
        );
    }

    private void write(FriendlyByteBuf packetByteBuf) {
        packetByteBuf.writeIdentifier(id.id());
        packetByteBuf.writeVarInt(buf.readableBytes());
        packetByteBuf.writeBytes(buf.copy());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return id;
    }
}
