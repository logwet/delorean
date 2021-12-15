package me.logwet.delorean;

import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.Objects;
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
                                "key.delorean.save",
                                Type.KEYSYM,
                                GLFW.GLFW_KEY_PAGE_UP,
                                "key.category.delorean"));

        KeyMapping loadstateKey =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping(
                                "key.delorean.load",
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
                    if (Objects.nonNull(client.player)
                            && Objects.nonNull(client.level)
                            && client.hasSingleplayerServer()
                            && Objects.nonNull(DeLorean.SLOTMANAGER)) {
                        if (DeLorean.CONTROL_ENABLED) {
                            while (savestateKey.consumeClick()) {
                                client.player.displayClientMessage(
                                        new TextComponent("Saving state..."), true);

                                DeLorean.TRIGGER_SAVE.set(true);
                            }

                            while (loadstateKey.consumeClick()) {
                                client.player.displayClientMessage(
                                        new TextComponent("Loaded state"), true);

                                DeLorean.TRIGGER_LOAD.set(true);
                            }

                            while (deleteKey.consumeClick()) {
                                client.player.displayClientMessage(
                                        new TextComponent("Deleting all states..."), true);

                                DeLorean.TRIGGER_DELETE.set(true);
                            }
                        }

                        if (DeLorean.TRIGGER_LOAD.getAndSet(false)) {
                            synchronized (DeLorean.SLOTMANAGER_LOCK) {
                                try {
                                    int slot;

                                    if ((slot = DeLorean.TRIGGER_LOAD_SLOT.getAndSet(-1)) != -1) {
                                        DeLorean.SLOTMANAGER.load(slot);
                                    } else {
                                        DeLorean.SLOTMANAGER.load();
                                    }

                                } catch (Exception e) {
                                    DeLorean.LOGGER.error("Failed to load state", e);
                                }
                            }
                        } else if (Objects.nonNull(DeLorean.LOCAL_PLAYER_DATA)) {
                            client.player.setPos(
                                    DeLorean.LOCAL_PLAYER_DATA.getPosition().getX(),
                                    DeLorean.LOCAL_PLAYER_DATA.getPosition().getY(),
                                    DeLorean.LOCAL_PLAYER_DATA.getPosition().getZ());

                            client.player.setDeltaMovement(
                                    DeLorean.LOCAL_PLAYER_DATA.getPosition().getVelX(),
                                    DeLorean.LOCAL_PLAYER_DATA.getPosition().getVelY(),
                                    DeLorean.LOCAL_PLAYER_DATA.getPosition().getVelZ());

                            DeLorean.log(Level.INFO, "Set player velocity");

                            DeLorean.LOCAL_PLAYER_DATA = null;
                        }
                    }
                });

        DeLorean.log(Level.INFO, "Client class initialized!");
    }
}
