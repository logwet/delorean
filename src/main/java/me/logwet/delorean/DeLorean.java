package me.logwet.delorean;

import java.io.File;
import me.logwet.delorean.patch.SlotManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
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

    @NotNull public static SlotManager SLOTMANAGER;

    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MODID + " v" + VERSION + "] " + message);
    }

    public static void initServer(MinecraftServer minecraftServer) {
        SLOTMANAGER =
                new SlotManager(
                        new File(
                                minecraftServer.getWorldPath(LevelResource.ROOT).toFile(),
                                "savestates"),
                        minecraftServer);
    }

    @Override
    public void onInitialize() {
        log(Level.INFO, "Main class initialized!");
    }
}
