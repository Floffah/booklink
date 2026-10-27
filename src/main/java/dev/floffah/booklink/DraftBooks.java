package dev.floffah.booklink;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.storage.LevelResource;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public final class DraftBooks {
    public static boolean save(ServerPlayer player, int slot, List<FilteredText> pages, @Nullable FilteredText title) {
        ItemStack held = player.getInventory().getItem(slot);
        if (!held.is(Booklink.UNLINKED_BOOK_AND_QUILL.get())) {
            return false;
        }
        try {
            BookFiles.validatePages(pages.stream().map(FilteredText::raw).toList());
            held.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(pages.stream()
                    .map(page -> player.isTextFilteringEnabled()
                            ? Filterable.passThrough(page.filteredOrEmpty()) : Filterable.from(page)).toList()));
            if (title != null) {
                String name = BookFiles.create(player.server.getWorldPath(LevelResource.ROOT), title.filteredOrEmpty(),
                        pages.stream().map(FilteredText::filteredOrEmpty).toList());
                LinkedBookItem.replace(player, slot, name);
            }
        } catch (IOException exception) {
            player.displayClientMessage(Component.literal("Booklink: " + exception.getMessage()), false);
        }
        return true;
    }
}
