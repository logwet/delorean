package me.logwet.delorean;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.Level;

@Environment(EnvType.SERVER)
public class DeLoreanServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        DeLorean.log(Level.INFO, "Server class initialized!");
    }
}
