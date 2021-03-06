package me.logwet.delorean.saveslots.wrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.logwet.delorean.DeLorean;
import me.logwet.delorean.data.PlayerData;
import me.logwet.delorean.mixin.common.MinecraftServerAccessor;
import me.logwet.delorean.patch.PatchedMinecraft;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class IntegratedSlotWrapper extends AbstractSlotWrapper {
    public IntegratedSlotWrapper(File saveslotsDir, String id) {
        super(saveslotsDir, id);
    }

    @Override
    protected List<PlayerData> collectPlayerData(MinecraftServer minecraftServer) {
        List<PlayerData> playerDataList = new ArrayList<>();

        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;

        addPlayerDataToList(playerDataList, player);

        return playerDataList;
    }

    @Override
    public boolean load(MinecraftServer minecraftServer) {
        DeLorean.log(Level.INFO, "Loading slot " + id + "...");

        Minecraft mc = Minecraft.getInstance();

        double mouseX = mc.mouseHandler.xpos();
        double mouseY = mc.mouseHandler.ypos();

        mc.setScreen(new GenericDirtMessageScreen(new TranslatableComponent("gui.delorean.load")));

        String levelName =
                ((MinecraftServerAccessor) minecraftServer).getStorageSource().getLevelId();

        if (!super.load(minecraftServer)) {
            return false;
        }

        DeLorean.log(Level.INFO, "Loading new server...");

        Minecraft.getInstance()
                .clearLevel(
                        new GenericDirtMessageScreen(
                                new TranslatableComponent("gui.delorean.load")));

        ((PatchedMinecraft) mc).loadSaveStateLevel(levelName);

        for (PlayerData playerData : slotData.getPlayers()) {
            if (playerData.getUUID().equals(mc.getUser().getUuid())) {
                DeLorean.LOCAL_PLAYER_DATA = playerData;
                break;
            }
        }

        GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), mouseX, mouseY);
        mc.mouseHandler.turnPlayer();

        DeLorean.log(Level.INFO, "Loaded slot " + id);

        return true;
    }
}
