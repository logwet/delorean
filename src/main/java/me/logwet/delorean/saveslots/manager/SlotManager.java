package me.logwet.delorean.saveslots.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import me.logwet.delorean.data.JSONDataFile;
import me.logwet.delorean.data.PlayerData;
import me.logwet.delorean.data.SlotsData;
import me.logwet.delorean.saveslots.wrapper.IntegratedSlotWrapper;
import me.logwet.delorean.saveslots.wrapper.SlotWrapper;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlotManager {
    private final Map<String, SlotWrapper> saveSlots = new HashMap<>();
    private final File saveslotsDir;
    private final MinecraftServer minecraftServer;
    private final JSONDataFile<SlotsData> slotsDataFile;
    @NotNull public SlotsData slotsData;
    protected List<PlayerData> playerDataList = new ArrayList<>();

    public SlotManager(File saveslotsDir, MinecraftServer minecraftServer) {
        this.saveslotsDir = saveslotsDir;
        this.saveslotsDir.mkdirs();
        this.minecraftServer = minecraftServer;

        slotsDataFile = new JSONDataFile<>(new File(saveslotsDir, "slots.json"), SlotsData.class);

        slotsData = slotsDataFile.read();

        if (Objects.isNull(slotsData)) {
            slotsData = new SlotsData();
            saveSlotsData();
        }
    }

    private boolean saveSlotsData() {
        return slotsDataFile.write(slotsData);
    }

    public int getLatestSlot() {
        return slotsData.getSlots().lastKey();
    }

    public String getLatestId() {
        return slotsData.getSlots().lastEntry().getValue();
    }

    private SlotWrapper getSlotWrapper(String id) {
        return saveSlots.computeIfAbsent(id, s -> new IntegratedSlotWrapper(saveslotsDir, s));
    }

    private void putSlotWrapper(String id, SlotWrapper slot) {
        saveSlots.put(id, slot);
    }

    private SlotWrapper removeSlotWrapper(String id) {
        return saveSlots.remove(id);
    }

    private String add(@Nullable String id) {
        if (Objects.isNull(id)) {
            id = UUID.randomUUID().toString();
        }

        int i = 0;

        if (!slotsData.getSlots().containsValue(id)) {
            if (slotsData.getSlots().size() > 0) {
                int last = slotsData.getSlots().lastKey();
                for (i = 0; i <= last; i++) {
                    Integer ceil = slotsData.getSlots().ceilingKey(i);
                    if (Objects.nonNull(ceil)) {
                        if (ceil > i) {
                            break;
                        }
                    }
                }
            }
        } else {
            for (Map.Entry<Integer, String> entry : slotsData.getSlots().entrySet()) {
                if (entry.getValue().equals(id)) {
                    i = entry.getKey();
                    break;
                }
            }
        }

        slotsData.getSlots().put(i, id);
        saveSlotsData();

        if (Objects.nonNull(minecraftServer)) {
            putSlotWrapper(id, new IntegratedSlotWrapper(saveslotsDir, id));
            return id;
        }

        return null;
    }

    private String add(Integer slot) {
        return add(new UUID(slot, 0L).toString());
    }

    private String add() {
        return add((String) null);
    }

    public boolean delete(String id) {
        SlotWrapper slotWrapper = removeSlotWrapper(id);
        if (Objects.nonNull(slotWrapper)) {
            return slotWrapper.delete();
        }
        return false;
    }

    public boolean delete(int slot) {
        String id = slotsData.getSlots().remove(slot);
        return delete(id);
    }

    public boolean deleteAll() {
        boolean r = true;

        for (String id : slotsData.getSlots().values()) {
            if (!delete(id)) {
                r = false;
            }
        }

        return r;
    }

    public boolean save(String id) {
        add(id);
        return getSlotWrapper(id).save(minecraftServer);
    }

    public boolean save(int slot) {
        String id = slotsData.getSlots().get(slot);
        return save(id);
    }

    public boolean save() {
        return save(add());
    }

    public boolean load(String id) {
        return getSlotWrapper(id).load(minecraftServer);
    }

    public boolean load(int slot) {
        String id = slotsData.getSlots().get(slot);
        return load(id);
    }

    public boolean load() {
        return load(getLatestId());
    }
}
