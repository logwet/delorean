package me.logwet.delorean;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import me.logwet.delorean.data.PlayerData;
import me.logwet.delorean.saveslots.manager.SlotManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
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

    public static final AtomicBoolean TRIGGER_SAVE = new AtomicBoolean(false);
    public static final AtomicInteger TRIGGER_SAVE_SLOT = new AtomicInteger(-1);
    public static final AtomicReference<String> TRIGGER_SAVE_ID = new AtomicReference<>("");
    public static final AtomicInteger TRIGGER_SAVE_IN_TICKS = new AtomicInteger(-1);
    public static final AtomicBoolean TRIGGER_LOAD = new AtomicBoolean(false);
    public static final AtomicInteger TRIGGER_LOAD_SLOT = new AtomicInteger(-1);
    public static final AtomicReference<String> TRIGGER_LOAD_ID = new AtomicReference<>("");
    public static final AtomicBoolean TRIGGER_DELETE = new AtomicBoolean(false);
    public static final AtomicInteger TRIGGER_DELETE_SLOT = new AtomicInteger(-1);
    public static final AtomicReference<String> TRIGGER_DELETE_ID = new AtomicReference<>("");

    protected static final String SLOTMANAGER_LOCK = "slotmanager_lock";
    public static String SAVESTATES_DIR_NAME = "savestates";

    public static boolean ENABLED = true;
    public static boolean CONTROL_ENABLED = true;

    public static volatile PlayerData LOCAL_PLAYER_DATA;
    @Nullable protected static SlotManager SLOTMANAGER;

    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MODID + " v" + VERSION + "] " + message);
    }

    public static void initSlotManager(SlotManager slotManager) {
        synchronized (SLOTMANAGER_LOCK) {
            SLOTMANAGER = slotManager;
        }
    }

    public static Map<Integer, String> getSlots() {
        try {
            synchronized (SLOTMANAGER_LOCK) {
                assert SLOTMANAGER != null;
                return SLOTMANAGER.getSlotsData().getSlots();
            }
        } catch (Exception e) {
            LOGGER.error("Unable to get slot map", e);
        }

        return Collections.emptyMap();
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(CommandManager::register);

        ServerTickEvents.END_SERVER_TICK.register(
                server -> {
                    if (ENABLED && Objects.nonNull(SLOTMANAGER)) {
                        if (TRIGGER_SAVE_IN_TICKS.get() >= 0) {
                            TRIGGER_SAVE_IN_TICKS.getAndDecrement();
                        }

                        if (TRIGGER_SAVE_IN_TICKS.get() < 0
                                && TRIGGER_SAVE.get()) {
                            synchronized (SLOTMANAGER_LOCK) {
                                try {
                                    int slot;
                                    String id;

                                    if ((slot = TRIGGER_SAVE_SLOT.getAndSet(-1)) != -1) {
                                        SLOTMANAGER.save(slot);
                                    } else if (!Objects.equals(
                                            id = TRIGGER_SAVE_ID.getAndSet(""), "")) {
                                        SLOTMANAGER.save(id);
                                    } else {
                                        SLOTMANAGER.save();
                                    }
                                } catch (Exception e) {
                                    LOGGER.error("Failed to save state", e);
                                }

                                TRIGGER_SAVE.set(false);
                                TRIGGER_SAVE.notifyAll();
                            }
                        }

                        if (TRIGGER_DELETE.get()) {
                            synchronized (SLOTMANAGER_LOCK) {
                                try {
                                    int slot;
                                    String id;

                                    if ((slot = TRIGGER_DELETE_SLOT.getAndSet(-1)) != -1) {
                                        SLOTMANAGER.delete(slot);
                                    } else if (!Objects.equals(
                                            id = TRIGGER_DELETE_ID.getAndSet(""), "")) {
                                        SLOTMANAGER.delete(id);
                                    } else {
                                        SLOTMANAGER.deleteAll();
                                    }
                                } catch (Exception e) {
                                    LOGGER.error("Failed to delete state", e);
                                }

                                TRIGGER_DELETE.set(false);
                                TRIGGER_DELETE.notifyAll();
                            }
                        }
                    }
                });

        log(Level.INFO, "Main class initialized!");
    }
}
