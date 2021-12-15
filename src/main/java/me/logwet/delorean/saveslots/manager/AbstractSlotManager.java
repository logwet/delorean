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
import me.logwet.delorean.saveslots.wrapper.SlotWrapper;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSlotManager implements SlotManager {
    protected final Map<String, SlotWrapper> saveSlots = new HashMap<>();
    protected final File saveslotsDir;
    protected final MinecraftServer minecraftServer;
    protected final JSONDataFile<SlotsData> slotsDataFile;
    @NotNull protected SlotsData slotsData;
    protected List<PlayerData> playerDataList = new ArrayList<>();

    public AbstractSlotManager(File saveslotsDir, MinecraftServer minecraftServer) {
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

    @Override
    public SlotsData getSlotsData() {
        return slotsData;
    }

    private boolean saveSlotsData() {
        return slotsDataFile.write(slotsData);
    }

    private int getLatestSlot() {
        return slotsData.getSlots().lastKey();
    }

    private String getLatestId() {
        return slotsData.getSlots().lastEntry().getValue();
    }

    protected abstract SlotWrapper buildNewSlotWrapper(String id);

    private SlotWrapper getSlotWrapper(String id) {
        return saveSlots.computeIfAbsent(id, this::buildNewSlotWrapper);
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
            putSlotWrapper(id, buildNewSlotWrapper(id));
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

    @Override
    public boolean save(String id) {
        add(id);
        return getSlotWrapper(id).save(minecraftServer);
    }

    @Override
    public boolean save(int slot) {
        String id = slotsData.getSlots().get(slot);
        return save(id);
    }

    @Override
    public boolean save() {
        return save(add());
    }

    @Override
    public boolean load(String id) {
        return getSlotWrapper(id).load(minecraftServer);
    }

    @Override
    public boolean load(int slot) {
        String id = slotsData.getSlots().get(slot);
        return load(id);
    }

    @Override
    public boolean load() {
        return load(getLatestId());
    }

    @Override
    public boolean delete(String id) {
        SlotWrapper slotWrapper = removeSlotWrapper(id);
        if (Objects.nonNull(slotWrapper)) {
            return slotWrapper.delete();
        }
        return false;
    }

    @Override
    public boolean delete(int slot) {
        String id = slotsData.getSlots().remove(slot);
        return delete(id);
    }

    @Override
    public boolean delete() {
        return delete(getLatestId());
    }

    @Override
    public boolean deleteAll() {
        boolean r = true;

        for (String id : slotsData.getSlots().values()) {
            if (!delete(id)) {
                r = false;
            }
        }

        return r;
    }
}
