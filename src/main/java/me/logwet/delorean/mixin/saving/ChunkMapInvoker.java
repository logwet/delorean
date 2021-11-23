package me.logwet.delorean.mixin.saving;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkMap.class)
public interface ChunkMapInvoker {
    @Invoker("isExistingChunkFull")
    boolean invokeIsExistingChunkFull(ChunkPos chunkPos);
}
