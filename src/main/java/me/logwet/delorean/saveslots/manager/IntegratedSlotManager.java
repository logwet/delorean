package me.logwet.delorean.saveslots.manager;

import java.io.File;
import me.logwet.delorean.saveslots.wrapper.IntegratedSlotWrapper;
import me.logwet.delorean.saveslots.wrapper.SlotWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.server.IntegratedServer;

@Environment(EnvType.CLIENT)
public class IntegratedSlotManager extends AbstractSlotManager {
    public IntegratedSlotManager(File saveslotsDir, IntegratedServer minecraftServer) {
        super(saveslotsDir, minecraftServer);
    }

    @Override
    protected SlotWrapper buildNewSlotWrapper(String id) {
        return new IntegratedSlotWrapper(saveslotsDir, id);
    }
}
