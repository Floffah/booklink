package dev.floffah.booklink;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;

record LinkBook(String name, boolean offHand) implements CustomPacketPayload {
    static final Type<LinkBook> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("booklink", "link_book"));
    static final StreamCodec<ByteBuf, LinkBook> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(64), LinkBook::name,
            ByteBufCodecs.BOOL, LinkBook::offHand, LinkBook::new);

    @Override
    public Type<LinkBook> type() {
        return TYPE;
    }

    static void handle(LinkBook payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        InteractionHand hand = payload.offHand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Booklink.UNLINKED_BOOK.get())) {
            player.displayClientMessage(Component.literal("Booklink: hold an Unlinked Book to link it."), false);
            return;
        }
        try {
            BookFiles.read(player.server.getWorldPath(LevelResource.ROOT), payload.name());
        } catch (IOException exception) {
            player.displayClientMessage(Component.literal("Booklink: " + exception.getMessage()), false);
            return;
        }
        LinkedBookItem.replace(player, hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40, payload.name());
    }
}
