package me.logwet.delorean.patch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import me.logwet.delorean.patch.data.DataFile;
import me.logwet.delorean.patch.data.PlayerData;
import me.logwet.delorean.patch.data.SlotData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SlotWrapper {
    private final String id;
    private final File dir;

    @Nullable private SlotData slotData;
    private DataFile<SlotData> slotDataFile;

    public SlotWrapper(File saveslotsDir, String id) {
        this.id = id;
        this.dir = new File(saveslotsDir, id);
        this.dir.mkdirs();
    }

    public SlotWrapper(File saveslotsDir, String id, MinecraftServer minecraftServer) {
        this(saveslotsDir, id);

        slotDataFile = new DataFile<>(new File(dir, "slot.json"));

        slotData = slotDataFile.read();

        if (Objects.isNull(slotData)) {
            List<PlayerData> playerDataList = new ArrayList<>();

            PlayerList playerList = minecraftServer.getPlayerList();
            if (Objects.nonNull(playerList)) {
                for (ServerPlayer serverPlayer : playerList.getPlayers()) {
                    Vec3 velocity = serverPlayer.getDeltaMovement();

                    playerDataList.add(
                            new PlayerData(
                                    serverPlayer.getStringUUID(),
                                    velocity.x,
                                    velocity.y,
                                    velocity.z));
                }
            }

            slotData = new SlotData(id, playerDataList);
            saveSlotData();
        }
    }

    private boolean saveSlotData() {
        return slotDataFile.write(slotData);
    }

    public boolean save(MinecraftServer minecraftServer) {
        saveSlotData();
        return false;
    }

    public boolean load(MinecraftServer minecraftServer) {
        return false;
    }

    public boolean delete() {
        return dir.delete();
    }
}
