package me.logwet.delorean.data;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class SlotData {
    private final String id;
    private final List<PlayerData> players;

    public SlotData(String id, List<PlayerData> players) {
        this.id = id;
        this.players = players;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public List<PlayerData> getPlayers() {
        return players;
    }
}
