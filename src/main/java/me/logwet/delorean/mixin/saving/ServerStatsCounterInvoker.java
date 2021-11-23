package me.logwet.delorean.mixin.saving;

import net.minecraft.stats.ServerStatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerStatsCounter.class)
public interface ServerStatsCounterInvoker {
    @Invoker("toJson")
    String invokeToJson();
}
