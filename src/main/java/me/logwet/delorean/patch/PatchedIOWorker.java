package me.logwet.delorean.patch;

import java.io.File;
import net.minecraft.world.level.chunk.storage.IOWorker;

public class PatchedIOWorker extends IOWorker {
    public PatchedIOWorker(File file, boolean bl, String string) {
        super(file, bl, string);
    }
}
