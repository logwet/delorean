package me.logwet.delorean.saveslots.wrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import me.logwet.delorean.data.PlayerData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

@Environment(EnvType.SERVER)
public class DedicatedSlotWrapper extends AbstractSlotWrapper {
    public DedicatedSlotWrapper(File saveslotsDir, String id) {
        super(saveslotsDir, id);
    }

    @Override
    protected List<PlayerData> collectPlayerData(MinecraftServer minecraftServer) {
        List<PlayerData> playerDataList = new ArrayList<>();

        PlayerList playerList = minecraftServer.getPlayerList();
        if (Objects.nonNull(playerList)) {
            for (ServerPlayer player : playerList.getPlayers()) {
                addPlayerDataToList(playerDataList, player);
            }
        }

        return playerDataList;
    }

    @Override
    public boolean load(MinecraftServer minecraftServer) {
        return false;
    }
}
