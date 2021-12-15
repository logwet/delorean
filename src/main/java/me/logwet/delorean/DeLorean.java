package me.logwet.delorean;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import me.logwet.delorean.util.CommandManager;
import me.logwet.delorean.util.SlotManager;
import me.logwet.delorean.util.data.PlayerData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DeLorean implements ModInitializer {
    public static final String MODID = "delorean";
    public static final String VERSION =
            FabricLoader.getInstance()
                    .getModContainer(MODID)
                    .get()
                    .getMetadata()
                    .getVersion()
                    .getFriendlyString();
    public static final boolean IS_CLIENT =
            FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final String SLOTMANAGER_LOCK = "slotmanager_lock";
    public static String SAVESTATES_DIR_NAME = "savestates";
    @Nullable public static SlotManager SLOTMANAGER;
    public static boolean CONTROL_ENABLED = true;

    public static AtomicBoolean TRIGGER_SAVE = new AtomicBoolean(false);
    public static AtomicInteger TRIGGER_SAVE_SLOT = new AtomicInteger(-1);

    public static AtomicBoolean TRIGGER_LOAD = new AtomicBoolean(false);
    public static AtomicInteger TRIGGER_LOAD_SLOT = new AtomicInteger(-1);

    public static AtomicBoolean TRIGGER_DELETE = new AtomicBoolean(false);
    public static AtomicInteger TRIGGER_DELETE_SLOT = new AtomicInteger(-1);

    public static volatile PlayerData LOCAL_PLAYER_DATA;

    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MODID + " v" + VERSION + "] " + message);
    }

    public static void initServer(MinecraftServer minecraftServer) {
        synchronized (SLOTMANAGER_LOCK) {
            SLOTMANAGER =
                    new SlotManager(
                            new File(
                                    minecraftServer.getWorldPath(LevelResource.ROOT).toFile(),
                                    SAVESTATES_DIR_NAME),
                            minecraftServer);
        }
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(CommandManager::register);

        ServerTickEvents.END_SERVER_TICK.register(
                server -> {
                    if (Objects.nonNull(SLOTMANAGER)) {
                        if (TRIGGER_SAVE.getAndSet(false)) {
                            synchronized (SLOTMANAGER_LOCK) {
                                try {
                                    int slot;

                                    if ((slot = TRIGGER_SAVE_SLOT.getAndSet(-1)) != -1) {
                                        SLOTMANAGER.save(slot);
                                    } else {
                                        SLOTMANAGER.save();
                                    }
                                } catch (Exception e) {
                                    DeLorean.LOGGER.error("Failed to save state", e);
                                }
                            }
                        }

                        if (TRIGGER_DELETE.getAndSet(false)) {
                            synchronized (SLOTMANAGER_LOCK) {
                                try {
                                    int slot;

                                    if ((slot = TRIGGER_DELETE_SLOT.getAndSet(-1)) != -1) {
                                        SLOTMANAGER.delete(slot);
                                    } else {
                                        SLOTMANAGER.deleteAll();
                                    }
                                } catch (Exception e) {
                                    DeLorean.LOGGER.error("Failed to delete state", e);
                                }
                            }
                        }
                    }
                });

        log(Level.INFO, "Main class initialized!");
    }
}
