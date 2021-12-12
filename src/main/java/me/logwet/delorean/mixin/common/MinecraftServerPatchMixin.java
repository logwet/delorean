package me.logwet.delorean.mixin.common;

import java.util.Iterator;
import me.logwet.delorean.DeLorean;
import me.logwet.delorean.patch.PatchedMinecraftServer;
import net.minecraft.core.RegistryAccess.RegistryHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerPatchMixin implements PatchedMinecraftServer {
    @Shadow @Final protected WorldData worldData;
    @Shadow @Final protected LevelStorageAccess storageSource;
    @Shadow @Final protected RegistryHolder registryHolder;

    @Shadow
    public abstract Iterable<ServerLevel> getAllLevels();

    @Shadow
    public abstract ServerLevel overworld();

    @Shadow
    public abstract CustomBossEvents getCustomBossEvents();

    @Shadow
    public abstract PlayerList getPlayerList();

    public boolean saveAllChunksWithListener(
            boolean bl, boolean bl2, boolean bl3, ProgressListener progressListener) {
        boolean bl4 = false;

        for (Iterator var5 = this.getAllLevels().iterator(); var5.hasNext(); bl4 = true) {
            ServerLevel serverLevel = (ServerLevel) var5.next();
            if (!bl) {
                DeLorean.LOGGER.info(
                        "Saving chunks for level '{}'/{}",
                        serverLevel,
                        serverLevel.dimension().location());
            }

            serverLevel.save(progressListener, bl2, serverLevel.noSave && !bl3);
        }

        ServerLevel serverLevel2 = this.overworld();
        ServerLevelData serverLevelData = this.worldData.overworldData();
        serverLevelData.setWorldBorder(serverLevel2.getWorldBorder().createSettings());
        this.worldData.setCustomBossEvents(this.getCustomBossEvents().save());
        this.storageSource.saveDataTag(
                this.registryHolder, this.worldData, this.getPlayerList().getSingleplayerData());
        return bl4;
    }
}
