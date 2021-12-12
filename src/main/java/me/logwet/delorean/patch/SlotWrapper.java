package me.logwet.delorean.patch;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import me.logwet.delorean.DeLorean;
import me.logwet.delorean.patch.data.DataFile;
import me.logwet.delorean.patch.data.PlayerData;
import me.logwet.delorean.patch.data.SlotData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class SlotWrapper {
    private static final IOFileFilter FILE_FILTER;
    private static final String SLOT_FILE_NAME = "slot.json";

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

    private final String id;
    private final File dir;
    private final DataFile<SlotData> slotDataFile;
    @Nullable private SlotData slotData;

    public SlotWrapper(File saveslotsDir, String id) {
        this.id = id;

        dir = new File(saveslotsDir, id);
        dir.mkdirs();

        slotDataFile = new DataFile<>(new File(dir, SLOT_FILE_NAME), SlotData.class);

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
                //noinspection UnstableApiUsage
                Files.copy(file, dest);
            } catch (IOException e) {
                DeLorean.log(Level.INFO, "Unable to copy file " + file.getAbsolutePath());
            }
        } else if (file.isDirectory()) {
            dest.mkdirs();
            for (File childFile : Objects.requireNonNull(file.listFiles())) {
                copyFileToDir(childFile, dest);
            }
        }
    }

    public static void unsafeCopyDirectory(File srcDir, File destDir, IOFileFilter fileFilter) {
        assert srcDir.isDirectory();
        for (File file : Objects.requireNonNull(srcDir.listFiles((FileFilter) fileFilter))) {
            copyFileToDir(file, destDir);
        }
    }

    private boolean saveSlotData() {
        return slotDataFile.write(slotData);
    }

    private List<PlayerData> collectPlayerData(MinecraftServer minecraftServer) {
        List<PlayerData> playerDataList = new ArrayList<>();

        PlayerList playerList = minecraftServer.getPlayerList();
        if (Objects.nonNull(playerList)) {
            for (ServerPlayer serverPlayer : playerList.getPlayers()) {
                Vec3 velocity = serverPlayer.getDeltaMovement();

                playerDataList.add(
                        new PlayerData(
                                serverPlayer.getStringUUID(), velocity.x, velocity.y, velocity.z));
            }
        }

        return playerDataList;
    }

    public boolean save(MinecraftServer minecraftServer) {
        DeLorean.log(Level.INFO, "Saving slot " + id);

        minecraftServer.getProfiler().push(DeLorean.MODID + "_saveSlot");

        String levelName = minecraftServer.getWorldData().getLevelName();
        File serverDir = minecraftServer.getWorldPath(LevelResource.ROOT).toFile();

        delete();
        slotData = new SlotData(id, collectPlayerData(minecraftServer));
        saveSlotData();

        minecraftServer.getPlayerList().saveAll();

        ((PatchedMinecraftServer) minecraftServer)
                .saveAllChunksWithListener(false, false, false, null);

        unsafeCopyDirectory(serverDir, dir, FILE_FILTER);

        System.gc();

        minecraftServer.getProfiler().pop();
        DeLorean.log(Level.INFO, "Saved slot " + id);

        return true;
    }

    public boolean load(MinecraftServer minecraftServer) {
        DeLorean.log(Level.INFO, "Loading slot " + id);
        minecraftServer.getProfiler().push(DeLorean.MODID + "_closeServer");

        String levelName = minecraftServer.getWorldData().getLevelName();
        File serverDir = minecraftServer.getWorldPath(LevelResource.ROOT).toFile();

        double mouseX = 0.0D;
        double mouseY = 0.0D;

        if (DeLorean.IS_CLIENT) {
            mouseX = Minecraft.getInstance().mouseHandler.xpos();
            mouseY = Minecraft.getInstance().mouseHandler.ypos();

            Minecraft.getInstance().setScreen(new PauseScreen(true));
        }

        for (ServerLevel serverLevel : minecraftServer.getAllLevels()) {
            serverLevel.noSave = true;
        }

        if (DeLorean.IS_CLIENT) {
            Objects.requireNonNull(Minecraft.getInstance().getSingleplayerServer()).halt(true);
        } else {
            minecraftServer.halt(true);
        }

        minecraftServer.getProfiler().pop();

        try {
            FileUtils.iterateFilesAndDirs(serverDir, FILE_FILTER, null)
                    .forEachRemaining(File::delete);

            FileUtils.copyDirectory(dir, serverDir, FILE_FILTER);

            slotData = slotDataFile.read();

            if (Objects.isNull(slotData)) {
                DeLorean.log(Level.ERROR, "Unable to load data for slot " + id);
                return false;
            }

            DeLorean.SLOTMANAGER.playerDataList = slotData.getPlayers();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (DeLorean.IS_CLIENT) {
            Minecraft mc = Minecraft.getInstance();
            mc.gui.getChat().clearMessages(true);

            GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), mouseX, mouseY);
            mc.mouseHandler.turnPlayer();

            //            mc.loadLevel(levelName);
            ((PatchedMinecraft) mc).loadSaveStateLevel(levelName);

            GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), mouseX, mouseY);
            mc.mouseHandler.turnPlayer();
        }

        System.gc();

        DeLorean.log(Level.INFO, "Loaded slot " + id);

        return true;
    }

    public boolean delete() {
        return dir.delete();
    }
}
