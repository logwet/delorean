package me.logwet.delorean;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import me.logwet.delorean.util.CommandManager;
import me.logwet.delorean.util.SlotManager;
import me.logwet.delorean.util.data.PlayerData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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

    public static String SAVESTATES_DIR_NAME = "savestates";

    @NotNull public static SlotManager SLOTMANAGER;

    public static boolean CONTROL_ENABLED = true;

    public static AtomicBoolean TRIGGER_LOAD = new AtomicBoolean(false);
    public static AtomicInteger TRIGGER_LOAD_SLOT = new AtomicInteger(-1);

    public static PlayerData LOCAL_PLAYER_DATA;

    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MODID + " v" + VERSION + "] " + message);
    }

    public static void initServer(MinecraftServer minecraftServer) {
        SLOTMANAGER =
                new SlotManager(
                        new File(
                                minecraftServer.getWorldPath(LevelResource.ROOT).toFile(),
                                SAVESTATES_DIR_NAME),
                        minecraftServer);
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(CommandManager::register);

        log(Level.INFO, "Main class initialized!");
    }
}
