package dev.floffah.booklink;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.IOException;

final class LinkedBookItem extends Item {
    LinkedBookItem() {
        super(new Properties().stacksTo(1));
    }

    static void replace(ServerPlayer player, int slot, String name) {
        ItemStack held = player.getInventory().getItem(slot);
        ItemStack linked = new ItemStack(Booklink.LINKED_BOOK.get());
        CompoundTag data = new CompoundTag();
        data.putString("booklink_file", name);
        linked.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        linked.set(DataComponents.CUSTOM_NAME, Component.literal(BookFiles.displayName(name)));
        if (held.getCount() == 1) {
            player.getInventory().setItem(slot, linked);
        } else {
            held.shrink(1);
            if (!player.getInventory().add(linked)) {
                player.drop(linked, false);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            String name = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag().getString("booklink_file");
            try {
                String text = BookFiles.read(serverPlayer.server.getWorldPath(LevelResource.ROOT), name);
                PacketDistributor.sendToPlayer(serverPlayer, new OpenBook(text));
            } catch (IOException exception) {
                player.displayClientMessage(Component.literal("Booklink: " + exception.getMessage()), false);
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
