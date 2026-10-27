package dev.floffah.booklink;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod("booklink")
public final class Booklink {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("booklink");
    static final DeferredItem<LinkedBookItem> LINKED_BOOK = ITEMS.register("linked_book", LinkedBookItem::new);
    static final DeferredItem<UnlinkedBookItem> UNLINKED_BOOK = ITEMS.register("unlinked_book", UnlinkedBookItem::new);

    static final DeferredItem<UnlinkedBookAndQuillItem> UNLINKED_BOOK_AND_QUILL = ITEMS.register(
            "unlinked_book_and_quill", UnlinkedBookAndQuillItem::new);

    public Booklink(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener(Booklink::registerPayloads);
        bus.addListener(Booklink::addCreativeItems);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("2");
        registrar.playToClient(OpenBook.TYPE, OpenBook.CODEC,
                (payload, context) -> ClientBooks.open(payload.text()));
        registrar.playToServer(LinkBook.TYPE, LinkBook.CODEC, LinkBook::handle);
    }

    private static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(UNLINKED_BOOK);
            event.accept(UNLINKED_BOOK_AND_QUILL);
        }
    }
}
