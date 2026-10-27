package dev.floffah.booklink.mixin;

import dev.floffah.booklink.DraftBooks;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class BookEditHandlerMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "updateBookContents", at = @At("HEAD"), cancellable = true)
    private void booklink$saveDraft(List<FilteredText> pages, int slot, CallbackInfo callback) {
        if (DraftBooks.save(player, slot, pages, null)) {
            callback.cancel();
        }
    }

    @Inject(method = "signBook", at = @At("HEAD"), cancellable = true)
    private void booklink$sign(FilteredText title, List<FilteredText> pages, int slot, CallbackInfo callback) {
        if (DraftBooks.save(player, slot, pages, title)) {
            callback.cancel();
        }
    }
}
