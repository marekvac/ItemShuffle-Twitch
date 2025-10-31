package me.marcuscz.itemshuffle.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record GenericPayload(Id<GenericPayload> id, PacketByteBuf buf) implements CustomPayload {

    public static PacketCodec<PacketByteBuf, GenericPayload> CODEC = PacketCodec.of(GenericPayload::write, GenericPayload::new);

    public GenericPayload(PacketByteBuf packetByteBuf) {
        this(
                new Id<>(packetByteBuf.readIdentifier()),
                new PacketByteBuf(packetByteBuf.readBytes(packetByteBuf.readVarInt()))
        );
    }

    private void write(PacketByteBuf packetByteBuf) {
        packetByteBuf.writeIdentifier(id.id());
        packetByteBuf.writeVarInt(buf.readableBytes());
        packetByteBuf.writeBytes(buf.copy());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return id;
    }
}
