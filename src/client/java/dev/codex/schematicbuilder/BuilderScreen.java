package dev.codex.schematicbuilder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A small, visible control panel for the building assistant. */
final class BuilderScreen extends Screen {
    private static final int PANEL_WIDTH = 276;
    private static final int PANEL_HEIGHT = 188;

    BuilderScreen() {
        super(Component.literal("Build Buddy"));
    }

    @Override
    protected void init() {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Start / Pause"), button ->
                        BuildController.get().toggle(this.minecraft))
                .bounds(left + 22, top + 124, 112, 22)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Stop"), button ->
                        BuildController.get().stop(this.minecraft))
                .bounds(left + 142, top + 124, 112, 22)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(left + 82, top + 154, 112, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        // Soft cream card with a pink edge and a lighter inset.
        graphics.fill(left - 2, top - 2, left + PANEL_WIDTH + 2, top + PANEL_HEIGHT + 2, 0xFFCF8FAF);
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFFFFF4E8);
        graphics.fill(left + 8, top + 8, left + PANEL_WIDTH - 8, top + 42, 0xFFFFE4EC);

        graphics.drawCenteredString(this.font, "♡  Build Buddy  ♡", this.width / 2, top + 19, 0xFF8E4964);
        graphics.drawCenteredString(this.font, "Your little schematic helper", this.width / 2, top + 54, 0xFF6F5962);
        graphics.drawCenteredString(this.font, "Status", this.width / 2, top + 75, 0xFF9A5D76);
        graphics.drawCenteredString(this.font, BuildController.get().status(), this.width / 2, top + 91, 0xFF51434A);
        graphics.drawCenteredString(this.font, "B: open menu     P: start / pause     X: stop", this.width / 2, top + 110, 0xFF806D74);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
