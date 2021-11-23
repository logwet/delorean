package me.logwet.delorean.mixin.saving;

import net.minecraft.core.RegistryAccess.RegistryHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Accessor("storageSource")
    LevelStorageAccess getStorageSource();

    @Accessor("resources")
    ServerResources getResources();

    @Accessor("registryHolder")
    RegistryHolder getRegistryHolder();
}
