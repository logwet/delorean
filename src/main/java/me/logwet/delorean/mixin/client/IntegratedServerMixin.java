package me.logwet.delorean.mixin.client;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import me.logwet.delorean.DeLorean;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.RegistryAccess.RegistryHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(
            Thread thread,
            Minecraft minecraft,
            RegistryHolder registryHolder,
            LevelStorageAccess levelStorageAccess,
            PackRepository<Pack> packRepository,
            ServerResources serverResources,
            WorldData worldData,
            MinecraftSessionService minecraftSessionService,
            GameProfileRepository gameProfileRepository,
            GameProfileCache gameProfileCache,
            ChunkProgressListenerFactory chunkProgressListenerFactory,
            CallbackInfo ci) {
        DeLorean.initServer((MinecraftServer) (Object) this);
    }
}
