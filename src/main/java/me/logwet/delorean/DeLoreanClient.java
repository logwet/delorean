package me.logwet.delorean;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class DeLoreanClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyMapping savestateKey =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping(
                                "key.delorean.savestate",
                                Type.KEYSYM,
                                GLFW.GLFW_KEY_PAGE_UP,
                                "key.category.delorean"));

        KeyMapping loadstateKey =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping(
                                "key.delorean.loadstate",
                                Type.KEYSYM,
                                GLFW.GLFW_KEY_PAGE_DOWN,
                                "key.category.delorean"));

        KeyMapping deleteKey =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping(
                                "key.delorean.delete",
                                Type.KEYSYM,
                                GLFW.GLFW_KEY_PAUSE,
                                "key.category.delorean"));

        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {
                    if (client.player != null && client.level != null) {
                        while (savestateKey.consumeClick()) {
                            client.player.displayClientMessage(
                                    new TextComponent("Saving latest state..."), true);

                            try {
                                Thread thread = new Thread(() -> DeLorean.SLOTMANAGER.save());
                                thread.start();
                            } catch (Exception e) {
                                DeLorean.LOGGER.error("Failed to save state", e);
                            }
                        }

                        while (loadstateKey.consumeClick()) {
                            client.player.displayClientMessage(
                                    new TextComponent("Loading latest state..."), true);

                            try {
                                DeLorean.SLOTMANAGER.load();
                            } catch (Exception e) {
                                DeLorean.LOGGER.error("Failed to load state", e);
                            }
                        }

                        while (deleteKey.consumeClick()) {
                            client.player.displayClientMessage(
                                    new TextComponent("Deleting all states..."), true);

                            try {
                                Thread thread = new Thread(() -> DeLorean.SLOTMANAGER.deleteAll());
                                thread.start();
                            } catch (Exception e) {
                                DeLorean.LOGGER.error("Failed to delete states", e);
                            }
                        }
                    }
                });

        DeLorean.log(Level.INFO, "Client class initialized!");
    }
}
