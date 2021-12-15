package me.logwet.delorean.util;

import net.minecraft.server.MinecraftServer;

public interface SlotWrapper {
    String getId();

    boolean save(MinecraftServer minecraftServer);

    boolean load(MinecraftServer minecraftServer);

    boolean delete();
}
