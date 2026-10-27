package dev.floffah.booklink;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

record OpenBook(String text) implements CustomPacketPayload {
    static final Type<OpenBook> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("booklink", "open_book"));
    static final StreamCodec<ByteBuf, OpenBook> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, OpenBook::text, OpenBook::new);

    @Override
    public Type<OpenBook> type() {
        return TYPE;
    }
}
