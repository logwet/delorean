package me.logwet.delorean.patch;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import me.logwet.delorean.DeLorean;
import me.logwet.delorean.mixin.saving.MinecraftServerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import org.apache.logging.log4j.Level;

public abstract class PatchedMinecraftServer {
    MinecraftServer minecraftServer;

    int slot;

    LevelStorageAccess patchedStorageSource;
    PlayerDataStorage patchedPlayerDataStorage;
    StructureManager patchedStructureManager;

    public PatchedMinecraftServer(MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
    }

    public void setSaveSlot(int index) {
        MinecraftServerAccessor minecraftServerAccessor = (MinecraftServerAccessor) minecraftServer;

        slot = index;

        Path gameDirectory = Minecraft.getInstance().gameDirectory.toPath();

        String levelId = minecraftServerAccessor.getStorageSource().getLevelId();

        LevelStorageSource levelStorageSource =
                new LevelStorageSource(
                        gameDirectory.resolve("saves/" + levelId + "/savestates"),
                        gameDirectory.resolve("backups/" + levelId + "/backupstates"),
                        DataFixers.getDataFixer());

        patchedStorageSource = null;

        try {
            patchedStorageSource = levelStorageSource.createAccess("state_" + index);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (patchedStorageSource != null) {
            patchedPlayerDataStorage = patchedStorageSource.createPlayerStorage();

            patchedStructureManager =
                    new StructureManager(
                            minecraftServerAccessor.getResources().getResourceManager(),
                            patchedStorageSource,
                            DataFixers.getDataFixer());
        } else {
            DeLorean.log(Level.ERROR, "Unable to create LevelStorageAccess for slot " + index);
        }
    }

    public boolean saveSlot() {
        boolean bl = false;
        boolean bl2 = false;
        boolean bl3 = false;

        MinecraftServerAccessor minecraftServerAccessor = (MinecraftServerAccessor) minecraftServer;

        boolean levelsSaved = false;

        for (Iterator<ServerLevel> var5 = minecraftServer.getAllLevels().iterator();
                var5.hasNext();
                levelsSaved = true) {
            ServerLevel serverLevel = var5.next();
            if (!bl) {
                DeLorean.log(
                        Level.INFO,
                        String.format(
                                "Saving chunks for slot %d - level '%s'/%s",
                                slot, serverLevel, serverLevel.dimension().location()));
            }

            serverLevel.save(null, bl2, serverLevel.noSave && !bl3);
        }

        ServerLevel serverLevel2 = minecraftServer.overworld();
        ServerLevelData serverLevelData = minecraftServerAccessor.getWorldData().overworldData();
        serverLevelData.setWorldBorder(serverLevel2.getWorldBorder().createSettings());
        minecraftServerAccessor
                .getWorldData()
                .setCustomBossEvents(minecraftServer.getCustomBossEvents().save());

        DeLorean.log(Level.INFO, String.format("Saving data for slot %d", slot));
        patchedStorageSource.saveDataTag(
                minecraftServerAccessor.getRegistryHolder(),
                minecraftServerAccessor.getWorldData(),
                minecraftServer.getPlayerList().getSingleplayerData());

        return levelsSaved;
    }
}
