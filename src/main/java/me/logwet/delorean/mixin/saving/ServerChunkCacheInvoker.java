package me.logwet.delorean.mixin.saving;

import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerChunkCache.class)
public interface ServerChunkCacheInvoker {
    @Invoker("runDistanceManagerUpdates")
    boolean invokeRunDistanceManagerUpdate();
}
