package dev.floffah.booklink;

import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

final class ClientBooks {
    static void open(String text) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new BookViewScreen(new BookViewScreen.BookAccess(pages(text, minecraft.font.getSplitter()))));
    }

    static void edit(Player player, ItemStack stack, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new BookEditScreen(player, stack, hand));
    }

    static List<Component> pages(String text, StringSplitter splitter) {
        List<Component> pages = new ArrayList<>();
        for (String section : text.split("(?m)\\n?^---[ \\t]*$\\n?", -1)) {
            List<FormattedText> lines = splitter.splitLines(
                    section.replaceAll("(?m)^\\\\(\\\\*---[ \\t]*)$", "$1"), 114, Style.EMPTY);
            if (lines.isEmpty()) {
                pages.add(Component.empty());
            }
            for (int start = 0; start < lines.size(); start += 14) {
                StringBuilder page = new StringBuilder();
                for (int line = start; line < Math.min(start + 14, lines.size()); line++) {
                    if (line > start) {
                        page.append("\n");
                    }
                    page.append(lines.get(line).getString());
                }
                pages.add(Component.literal(page.toString()));
            }
        }
        return pages;
    }
}
