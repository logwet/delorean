package me.logwet.delorean.mixin.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import java.io.File;
import me.logwet.delorean.DeLorean;
import me.logwet.delorean.saveslots.manager.DedicatedSlotManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.RegistryAccess.RegistryHolder;
import net.minecraft.server.ServerResources;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.SERVER)
@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(
            Thread thread,
            RegistryHolder registryHolder,
            LevelStorageAccess levelStorageAccess,
            PackRepository<Pack> packRepository,
            ServerResources serverResources,
            WorldData worldData,
            DedicatedServerSettings dedicatedServerSettings,
            DataFixer dataFixer,
            MinecraftSessionService minecraftSessionService,
            GameProfileRepository gameProfileRepository,
            GameProfileCache gameProfileCache,
            ChunkProgressListenerFactory chunkProgressListenerFactory,
            CallbackInfo ci) {
        DedicatedServer minecraftServer = (DedicatedServer) (Object) this;

        DeLorean.initSlotManager(
                new DedicatedSlotManager(
                        new File(
                                minecraftServer.getWorldPath(LevelResource.ROOT).toFile(),
                                DeLorean.SAVESTATES_DIR_NAME),
                        minecraftServer));
    }
}
