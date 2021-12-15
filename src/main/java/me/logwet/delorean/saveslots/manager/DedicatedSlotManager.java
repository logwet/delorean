package me.logwet.delorean.saveslots.manager;

import java.io.File;
import me.logwet.delorean.saveslots.wrapper.DedicatedSlotWrapper;
import me.logwet.delorean.saveslots.wrapper.SlotWrapper;
import net.minecraft.server.dedicated.DedicatedServer;

public class DedicatedSlotManager extends AbstractSlotManager {
    public DedicatedSlotManager(File saveslotsDir, DedicatedServer minecraftServer) {
        super(saveslotsDir, minecraftServer);
    }

    @Override
    protected SlotWrapper buildNewSlotWrapper(String id) {
        return new DedicatedSlotWrapper(saveslotsDir, id);
    }
}
