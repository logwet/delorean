package me.logwet.delorean.patch;

import net.minecraft.util.ProgressListener;

public interface PatchedMinecraftServer {
    boolean saveAllChunksWithListener(
            boolean bl, boolean bl2, boolean bl3, ProgressListener progressListener);
}
