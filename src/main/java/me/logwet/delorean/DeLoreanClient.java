package me.logwet.delorean;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.Level;

@Environment(EnvType.CLIENT)
public class DeLoreanClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DeLorean.log(Level.INFO, "Client class initialized!");
    }
}
