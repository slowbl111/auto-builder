package dev.codex.schematicbuilder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;

public final class SchematicBuilderClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("schematic_builder", "controls")
    );

    private static final KeyMapping TOGGLE_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                    "key.schematic_builder.toggle",
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_P,
                    CATEGORY
            )
    );

    private static final KeyMapping STOP_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                    "key.schematic_builder.stop",
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_X,
                    CATEGORY
            )
    );

    private static final KeyMapping MENU_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                    "key.schematic_builder.menu",
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_B,
                    CATEGORY
            )
    );

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            BuildController.get().tick(client);
            while (MENU_KEY.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new BuilderScreen());
                }
            }
            while (TOGGLE_KEY.consumeClick()) {
                toggle(client);
            }
            while (STOP_KEY.consumeClick()) {
                stop(client);
            }
        });
    }

    private void toggle(Minecraft client) {
        BuildController.get().toggle(client);
        notify(client, BuildController.get().status());
    }

    private void stop(Minecraft client) {
        BuildController.get().stop(client);
        notify(client, BuildController.get().status());
    }

    private static void notify(Minecraft client, String message) {
        if (client.player != null) {
            client.player.displayClientMessage(Component.literal("[Builder] " + message), false);
        }
    }
}
