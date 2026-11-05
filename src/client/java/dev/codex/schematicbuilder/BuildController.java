package dev.codex.schematicbuilder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A compact, pastel control screen inspired by module-list client menus. */
final class BuilderScreen extends Screen {
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 208;

    BuilderScreen() {
        super(Component.literal("Build Buddy"));
    }

    @Override
    protected void init() {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        int buttonY = top + 160;

        this.addRenderableWidget(Button.builder(Component.literal("♡  Start / Pause"), button ->
                        BuildController.get().toggle(this.minecraft))
                .bounds(left + 112, buttonY, 124, 22)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Stop"), button ->
                        BuildController.get().stop(this.minecraft))
                .bounds(left + 244, buttonY, 62, 22)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(left + 244, top + 12, 62, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        // Rounded-window look built from simple vanilla fills, so it needs no extra assets.
        graphics.fill(left - 2, top - 2, left + PANEL_WIDTH + 2, top + PANEL_HEIGHT + 2, 0xFFB9799A);
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFF211E28);
        graphics.fill(left + 2, top + 2, left + 96, top + PANEL_HEIGHT - 2, 0xFF2B2633);
        graphics.fill(left + 2, top + 2, left + PANEL_WIDTH - 2, top + 38, 0xFF362B3A);
        graphics.fill(left + 108, top + 52, left + PANEL_WIDTH - 14, top + 145, 0xFF2B2633);
        graphics.fill(left + 118, top + 96, left + PANEL_WIDTH - 24, top + 98, 0xFF62485A);

        graphics.drawString(this.font, "♡  BUDDY", left + 13, top + 13, 0xFFFFC6DD, false);
        graphics.drawString(this.font, "BUILD", left + 19, top + 54, 0xFFFFC6DD, false);
        graphics.drawString(this.font, "MODULE", left + 19, top + 74, 0xFFB9A8B8, false);
        graphics.drawString(this.font, "SCHEMATIC", left + 19, top + 94, 0xFFB9A8B8, false);

        graphics.drawString(this.font, "Build Buddy  ·  your tiny builder helper", left + 111, top + 14, 0xFFFFE7F0, false);
        graphics.drawString(this.font, "BUILD MODULE", left + 120, top + 62, 0xFFFFC6DD, false);
        graphics.drawString(this.font, "Status", left + 120, top + 78, 0xFFB9A8B8, false);
        graphics.drawString(this.font, BuildController.get().status(), left + 120, top + 109, 0xFFF4EAF1, false);
        graphics.drawString(this.font, "P  start / pause     X  stop", left + 111, top + 143, 0xFFB9A8B8, false);
        graphics.drawString(this.font, "made with care  ✿", left + 13, top + PANEL_HEIGHT - 16, 0xFFD9A9C0, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
