package me.logwet.delorean;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
    public static final AtomicInteger TRIGGER_SAVE_IN_TICKS = new AtomicInteger(-1);
    public static final AtomicBoolean TRIGGER_LOAD = new AtomicBoolean(false);
    public static final AtomicInteger TRIGGER_LOAD_SLOT = new AtomicInteger(-1);
    public static final AtomicBoolean TRIGGER_DELETE = new AtomicBoolean(false);
    public static final AtomicInteger TRIGGER_DELETE_SLOT = new AtomicInteger(-1);

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
        synchronized (SLOTMANAGER_LOCK) {
            return SLOTMANAGER.getSlotsData().getSlots();
        }
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(CommandManager::register);

        ServerTickEvents.END_SERVER_TICK.register(
                server -> {
                    if (ENABLED && Objects.nonNull(SLOTMANAGER)) {
                        if (DeLorean.TRIGGER_SAVE_IN_TICKS.get() >= 0) {
                            DeLorean.TRIGGER_SAVE_IN_TICKS.getAndDecrement();
                        }

                        if (DeLorean.TRIGGER_SAVE_IN_TICKS.get() < 0
                                && DeLorean.TRIGGER_SAVE.get()) {
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

                                DeLorean.TRIGGER_SAVE.set(false);
                                DeLorean.TRIGGER_SAVE.notifyAll();
                            }
                        }

                        if (DeLorean.TRIGGER_DELETE.get()) {
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

                                DeLorean.TRIGGER_DELETE.set(false);
                                DeLorean.TRIGGER_DELETE.notifyAll();
                            }
                        }
                    }
                });

        log(Level.INFO, "Main class initialized!");
    }
}
