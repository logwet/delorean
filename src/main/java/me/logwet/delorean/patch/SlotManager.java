package me.logwet.delorean.patch;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import me.logwet.delorean.patch.data.DataFile;
import me.logwet.delorean.patch.data.SlotsData;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlotManager {
    private final Map<String, SlotWrapper> saveSlots = new HashMap<>();
    private final File saveslotsDir;
    private final MinecraftServer minecraftServer;

    @NotNull private SlotsData slotsData;
    private DataFile<SlotsData> slotsDataFile;

    public SlotManager(File saveslotsDir, MinecraftServer minecraftServer) {
        this.saveslotsDir = saveslotsDir;
        this.saveslotsDir.mkdirs();
        this.minecraftServer = minecraftServer;

        slotsDataFile = new DataFile<>(new File(saveslotsDir, "slots.json"));

        slotsData = slotsDataFile.read();

        if (Objects.isNull(slotsData)) {
            slotsData = new SlotsData();
            saveSlotsData();
        }
    }

    private boolean saveSlotsData() {
        return slotsDataFile.write(slotsData);
    }

    private String getLatestId() {
        return slotsData.getSlots().lastEntry().getValue();
    }

    private SlotWrapper getSlotWrapper(String id) {
        return saveSlots.computeIfAbsent(id, s -> new SlotWrapper(saveslotsDir, s));
    }

    private void putSlotWrapper(String id, SlotWrapper slot) {
        saveSlots.put(id, slot);
    }

    private SlotWrapper removeSlotWrapper(String id) {
        return saveSlots.remove(id);
    }

    public String add(@Nullable String id) {
        if (Objects.isNull(id)) {
            id = UUID.randomUUID().toString();
        }

        int i = 0;

        if (!slotsData.getSlots().containsValue(id)) {
            if (slotsData.getSlots().size() > 0) {
                int last = slotsData.getSlots().lastKey();
                for (i = 0; i <= last + 1; i++) {
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
            putSlotWrapper(id, new SlotWrapper(saveslotsDir, id, minecraftServer));
            return id;
        }

        return null;
    }

    public String add(Integer slot) {
        return add(new UUID(slot, 0L).toString());
    }

    public String add() {
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

    public boolean save(String id) {
        return getSlotWrapper(id).save(minecraftServer);
    }

    public boolean save(int slot) {
        String id = slotsData.getSlots().get(slot);
        return save(id);
    }

    public boolean load(String id) {
        return getSlotWrapper(id).load(minecraftServer);
    }

    public boolean load(int slot) {
        String id = slotsData.getSlots().get(slot);
        return load(id);
    }
}
