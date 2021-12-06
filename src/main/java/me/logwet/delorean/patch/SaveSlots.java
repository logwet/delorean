package me.logwet.delorean.patch;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

public class SaveSlots {
    public static final AtomicBoolean isLoading = new AtomicBoolean(false);
    public static final AtomicInteger currentSlot = new AtomicInteger(-1);
    private static final int MAX_SLOTS = 10;
    private static final PatchedMinecraftServer[] saveSlots = new PatchedMinecraftServer[MAX_SLOTS];

    public static PatchedMinecraftServer getSaveSlot(int slot) {
        return saveSlots[slot];
    }

    private static void setSaveSlot(int slot, MinecraftServer minecraftServer) {
        saveSlots[slot] = new PatchedIntegratedServer(minecraftServer, slot);
    }

    public static boolean genSaveSlot(int slot) {
        MinecraftServer minecraftServer = Minecraft.getInstance().getSingleplayerServer();

        if (minecraftServer != null) {
            setSaveSlot(slot, minecraftServer);
            return true;
        }

        return false;
    }

    public static boolean save(int slot) {
        return getSaveSlot(slot).saveSlot();
    }

    public static boolean load(int slot) {
        return getSaveSlot(slot).loadSlot();
    }
}
