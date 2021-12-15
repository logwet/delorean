package me.logwet.delorean.util.wrapper;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import me.logwet.delorean.DeLorean;
import me.logwet.delorean.patch.PatchedMinecraftServer;
import me.logwet.delorean.util.data.JSONDataFile;
import me.logwet.delorean.util.data.PlayerData;
import me.logwet.delorean.util.data.SlotData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSlotWrapper implements SlotWrapper {
    protected static final IOFileFilter FILE_FILTER;
    protected static final String SLOT_FILE_NAME = "slot.json";

    static {
        FILE_FILTER =
                FileFilterUtils.notFileFilter(
                        FileFilterUtils.or(
                                FileFilterUtils.makeDirectoryOnly(
                                        FileFilterUtils.nameFileFilter(
                                                DeLorean.SAVESTATES_DIR_NAME, null)),
                                FileFilterUtils.nameFileFilter(SLOT_FILE_NAME, null),
                                FileFilterUtils.nameFileFilter("session.lock", null)));
    }

    protected final String id;
    protected final File dir;
    protected final JSONDataFile<SlotData> slotDataFile;
    @Nullable protected SlotData slotData;

    protected AbstractSlotWrapper(File saveslotsDir, String id) {
        this.id = id;

        dir = new File(saveslotsDir, id);
        dir.mkdirs();

        slotDataFile = new JSONDataFile<>(new File(dir, SLOT_FILE_NAME), SlotData.class);

        slotData = slotDataFile.read();

        if (Objects.isNull(slotData)) {
            slotData = new SlotData(id, new ArrayList<>());
            saveSlotData();
        }
    }

    private static void copyFileToDir(File file, File dir) {
        File dest = new File(dir, file.getName());

        if (file.isFile()) {
            try {
                //                Files.copy(file, dest);
                FileUtils.copyFile(file, dest, false);
            } catch (IOException e) {
                DeLorean.log(Level.INFO, "Unable to fully copy file " + file.getAbsolutePath());
            }
        } else if (file.isDirectory()) {
            dest.mkdirs();
            for (File childFile : Objects.requireNonNull(file.listFiles())) {
                copyFileToDir(childFile, dest);
            }
        }
    }

    private static void unsafeCopyDirectory(File srcDir, File destDir, IOFileFilter fileFilter) {
        assert srcDir.isDirectory();
        for (File file : Objects.requireNonNull(srcDir.listFiles((FileFilter) fileFilter))) {
            copyFileToDir(file, destDir);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    private boolean saveSlotData() {
        return slotDataFile.write(slotData);
    }

    protected void addPlayerDataToList(List<PlayerData> playerDataList, Player player) {
        Vec3 velocity = player.getDeltaMovement();

        String vehicleUUID = null;
        Entity vehicle = player.getVehicle();
        if (Objects.nonNull(vehicle)) {
            vehicleUUID = vehicle.getStringUUID();
        }

        playerDataList.add(
                new PlayerData(
                        player.getStringUUID(), vehicleUUID, velocity.x, velocity.y, velocity.z));
    }

    protected abstract List<PlayerData> collectPlayerData(MinecraftServer minecraftServer);

    @Override
    public boolean save(MinecraftServer minecraftServer) {
        DeLorean.log(Level.INFO, "Saving slot " + id + "...");

        minecraftServer.getProfiler().push(DeLorean.MODID + "_saveSlot");

        File serverDir = minecraftServer.getWorldPath(LevelResource.ROOT).toFile();

        delete();
        slotData = new SlotData(id, collectPlayerData(minecraftServer));
        saveSlotData();

        minecraftServer.getPlayerList().saveAll();

        ((PatchedMinecraftServer) minecraftServer)
                .saveAllChunksWithListener(false, false, false, null);

        unsafeCopyDirectory(serverDir, dir, FILE_FILTER);

        minecraftServer.getProfiler().pop();
        DeLorean.log(Level.INFO, "Saved slot " + id);

        return true;
    }

    @Override
    public boolean load(MinecraftServer minecraftServer) {
        DeLorean.log(Level.INFO, "Shutting down old server...");

        File serverDir = minecraftServer.getWorldPath(LevelResource.ROOT).toFile();

        for (ServerLevel serverLevel : minecraftServer.getAllLevels()) {
            serverLevel.noSave = true;
        }

        minecraftServer.getProfiler().push(DeLorean.MODID + "_haltServer");
        minecraftServer.halt(true);
        minecraftServer.getProfiler().pop();

        try {
            slotData = slotDataFile.read();
            if (Objects.isNull(slotData)) {
                DeLorean.log(Level.ERROR, "Unable to load data for slot " + id);
                return false;
            }

            FileUtils.iterateFilesAndDirs(serverDir, FILE_FILTER, null)
                    .forEachRemaining(File::delete);

            FileUtils.copyDirectory(dir, serverDir, FILE_FILTER);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean delete() {
        return dir.delete();
    }
}
