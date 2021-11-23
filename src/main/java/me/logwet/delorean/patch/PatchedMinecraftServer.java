package me.logwet.delorean.patch;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import me.logwet.delorean.DeLorean;
import me.logwet.delorean.mixin.saving.ChunkMapAccessor;
import me.logwet.delorean.mixin.saving.ChunkMapInvoker;
import me.logwet.delorean.mixin.saving.DimensionDataStorageAccessor;
import me.logwet.delorean.mixin.saving.MinecraftServerAccessor;
import me.logwet.delorean.mixin.saving.PlayerAdvancementsAccessor;
import me.logwet.delorean.mixin.saving.PlayerListAccessor;
import me.logwet.delorean.mixin.saving.ServerChunkCacheInvoker;
import me.logwet.delorean.mixin.saving.ServerStatsCounterInvoker;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;

public abstract class PatchedMinecraftServer {
    MinecraftServer minecraftServer;

    int slot;

    LevelStorageAccess patchedStorageSource;
    PlayerDataStorage patchedPlayerDataStorage;
    StructureManager patchedStructureManager;

    PlayerList playerList;

    public PatchedMinecraftServer(MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;

        playerList = minecraftServer.getPlayerList();
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

        if (playerList != null) {
            saveAllPlayers();
        }

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

            saveLevel(serverLevel, null, bl2, serverLevel.noSave && !bl3);
        }

        ServerLevel serverLevel2 = minecraftServer.overworld();
        ServerLevelData serverLevelData = minecraftServer.getWorldData().overworldData();
        serverLevelData.setWorldBorder(serverLevel2.getWorldBorder().createSettings());
        minecraftServer
                .getWorldData()
                .setCustomBossEvents(minecraftServer.getCustomBossEvents().save());

        DeLorean.log(Level.INFO, String.format("Saving data for slot %d", slot));

        patchedStorageSource.saveDataTag(
                minecraftServerAccessor.getRegistryHolder(),
                minecraftServer.getWorldData(),
                minecraftServer.getPlayerList().getSingleplayerData());

        return levelsSaved;
    }

    public void saveAllPlayers() {
        PlayerListAccessor playerListAccessor = (PlayerListAccessor) playerList;

        for (ServerPlayer serverPlayer : playerList.getPlayers()) {
            patchedPlayerDataStorage.save(serverPlayer);

            ServerStatsCounter serverStatsCounter =
                    playerListAccessor.getStats().get(serverPlayer.getUUID());
            if (serverStatsCounter != null) {
                File file =
                        new File(
                                getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile(),
                                serverPlayer.getUUID() + ".json");

                try {
                    FileUtils.writeStringToFile(
                            file, ((ServerStatsCounterInvoker) serverStatsCounter).invokeToJson());
                } catch (IOException e) {
                    DeLorean.LOGGER.error("Couldn't save stats", e);
                }
            }

            PlayerAdvancements playerAdvancements =
                    playerListAccessor.getAdvancements().get(serverPlayer.getUUID());
            if (playerAdvancements != null) {
                File file =
                        new File(
                                getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).toFile(),
                                serverPlayer.getUUID() + ".json");
                Gson GSON = PlayerAdvancementsAccessor.getGson();

                Map<ResourceLocation, AdvancementProgress> map = Maps.newHashMap();

                for (Entry<Advancement, AdvancementProgress> advancementAdvancementProgressEntry :
                        ((PlayerAdvancementsAccessor) playerAdvancements)
                                .getAdvancements()
                                .entrySet()) {
                    AdvancementProgress advancementProgress =
                            advancementAdvancementProgressEntry.getValue();
                    if (advancementProgress.hasProgress()) {
                        map.put(
                                advancementAdvancementProgressEntry.getKey().getId(),
                                advancementProgress);
                    }
                }

                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }

                JsonElement jsonElement = GSON.toJsonTree(map);
                jsonElement
                        .getAsJsonObject()
                        .addProperty(
                                "DataVersion",
                                SharedConstants.getCurrentVersion().getWorldVersion());

                try {
                    OutputStream outputStream = new FileOutputStream(file);
                    Throwable var38 = null;

                    try {
                        Writer writer =
                                new OutputStreamWriter(outputStream, Charsets.UTF_8.newEncoder());
                        Throwable var6 = null;

                        try {
                            GSON.toJson(jsonElement, writer);
                        } finally {
                            if (writer != null) {
                                if (var6 != null) {
                                    try {
                                        writer.close();
                                    } catch (Throwable var30) {
                                        var6.addSuppressed(var30);
                                    }
                                } else {
                                    writer.close();
                                }
                            }
                        }
                    } finally {
                        if (outputStream != null) {
                            if (var38 != null) {
                                try {
                                    outputStream.close();
                                } catch (Throwable var29) {
                                    var38.addSuppressed(var29);
                                }
                            } else {
                                outputStream.close();
                            }
                        }
                    }
                } catch (IOException var35) {
                    DeLorean.LOGGER.error("Couldn't save player advancements to {}", file, var35);
                }
            }
        }
    }

    public void saveLevel(
            ServerLevel serverLevel, ProgressListener progressListener, boolean bl, boolean bl2) {
        // Value of bl is ignored

        if (!bl2) {
            if (progressListener != null) {
                progressListener.progressStartNoAbort(
                        new TranslatableComponent("menu.savingLevel"));
            }

            {
                if (serverLevel.dragonFight() != null) {
                    minecraftServer
                            .getWorldData()
                            .setEndDragonFightData(
                                    Objects.requireNonNull(serverLevel.dragonFight()).saveData());
                }

                DimensionDataStorage dimensionDataStorage =
                        serverLevel.getChunkSource().getDataStorage();
                DimensionDataStorageAccessor dimensionDataStorageAccessor =
                        (DimensionDataStorageAccessor) dimensionDataStorage;

                File file =
                        new File(
                                patchedStorageSource.getDimensionPath(serverLevel.dimension()),
                                "data");
                file.mkdirs();

                for (SavedData savedData : dimensionDataStorageAccessor.getCache().values()) {
                    if (savedData != null) {
                        savedData.save(new File(file, savedData.getId() + ".dat"));
                    }
                }
            }

            if (progressListener != null) {
                progressListener.progressStage(new TranslatableComponent("menu.savingChunks"));
            }

            {
                ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
                ((ServerChunkCacheInvoker) serverChunkCache).invokeRunDistanceManagerUpdate();
                ChunkMap chunkMap = serverChunkCache.chunkMap;

                {
                    ChunkMapAccessor chunkMapAccessor = (ChunkMapAccessor) chunkMap;

                    boolean forceWrites = minecraftServer.forceSynchronousWrites();

                    File file =
                            new File(
                                    patchedStorageSource.getDimensionPath(serverLevel.dimension()),
                                    "region");
                    IOWorker worker = new PatchedIOWorker(file, forceWrites, "chunk");

                    chunkMapAccessor.getVisibleChunkMap().values().stream()
                            .filter(ChunkHolder::wasAccessibleSinceLastSave)
                            .forEach(
                                    (chunkHolder) -> {
                                        ChunkAccess chunkAccess =
                                                chunkHolder.getChunkToSave().getNow(null);
                                        if (chunkAccess instanceof ImposterProtoChunk
                                                || chunkAccess instanceof LevelChunk) {
                                            save:
                                            {
                                                chunkMapAccessor
                                                        .getPoiManager()
                                                        .flush(chunkAccess.getPos());
                                                if (!chunkAccess.isUnsaved()) {
                                                    break save;
                                                } else {
                                                    chunkAccess.setLastSaveTime(
                                                            serverLevel.getGameTime());
                                                    chunkAccess.setUnsaved(false);
                                                    ChunkPos chunkPos = chunkAccess.getPos();

                                                    try {
                                                        ChunkStatus chunkStatus =
                                                                chunkAccess.getStatus();
                                                        if (chunkStatus.getChunkType()
                                                                != ChunkStatus.ChunkType
                                                                        .LEVELCHUNK) {
                                                            if (((ChunkMapInvoker) chunkMap)
                                                                    .invokeIsExistingChunkFull(
                                                                            chunkPos)) {
                                                                break save;
                                                            }

                                                            if (chunkStatus == ChunkStatus.EMPTY
                                                                    && chunkAccess
                                                                            .getAllStarts()
                                                                            .values()
                                                                            .stream()
                                                                            .noneMatch(
                                                                                    StructureStart
                                                                                            ::isValid)) {
                                                                break save;
                                                            }
                                                        }

                                                        serverLevel
                                                                .getProfiler()
                                                                .incrementCounter("chunkSave");
                                                        CompoundTag compoundTag =
                                                                ChunkSerializer.write(
                                                                        serverLevel, chunkAccess);

                                                        {
                                                            worker.store(chunkPos, compoundTag);
                                                        }

                                                        {
                                                            chunkMapAccessor
                                                                    .getChunkTypeCache()
                                                                    .put(
                                                                            chunkPos.toLong(),
                                                                            (byte)
                                                                                    (chunkStatus
                                                                                                            .getChunkType()
                                                                                                    == ChunkStatus
                                                                                                            .ChunkType
                                                                                                            .PROTOCHUNK
                                                                                            ? -1
                                                                                            : 1));
                                                        }

                                                        break save;
                                                    } catch (Exception var5) {
                                                        DeLorean.LOGGER.error(
                                                                "Failed to save chunk {},{}",
                                                                chunkPos.x,
                                                                chunkPos.z,
                                                                var5);
                                                        break save;
                                                    }
                                                }
                                            }
                                            chunkHolder.refreshAccessibility();
                                        }
                                    });
                }
            }
        }
    }

    public Path getWorldPath(LevelResource levelResource) {
        return patchedStorageSource.getLevelPath(levelResource);
    }
}
