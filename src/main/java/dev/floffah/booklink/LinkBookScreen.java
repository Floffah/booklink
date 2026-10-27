package dev.floffah.booklink;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

final class LinkBookScreen extends Screen {
    private final InteractionHand hand;
    private EditBox name;
    private Button done;

    private LinkBookScreen(InteractionHand hand) {
        super(Component.translatable("screen.booklink.link"));
        this.hand = hand;
    }

    static void open(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new LinkBookScreen(hand));
    }

    @Override
    protected void init() {
        String previous = name == null ? "" : name.getValue();
        name = addRenderableWidget(new EditBox(font, width / 2 - 120, height / 2 - 10, 240, 20,
                Component.translatable("screen.booklink.name")));
        name.setMaxLength(64);
        done = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> submit())
                .bounds(width / 2 - 120, height / 2 + 24, 116, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                .bounds(width / 2 + 4, height / 2 + 24, 116, 20).build());
        name.setResponder(value -> done.active = !value.isBlank());
        name.setValue(previous);
        done.active = !previous.isBlank();
        setInitialFocus(name);
    }

    private void submit() {
        if (done.active) {
            PacketDistributor.sendToServer(new LinkBook(name.getValue(), hand == InteractionHand.OFF_HAND));
            onClose();
        }
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            submit();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 52, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("screen.booklink.name"),
                width / 2 - 120, height / 2 - 26, 0xFFFFFF);
    }
}
