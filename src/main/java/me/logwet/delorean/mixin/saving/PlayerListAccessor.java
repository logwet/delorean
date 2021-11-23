package me.logwet.delorean.mixin.saving;

import java.util.Map;
import java.util.UUID;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerList.class)
public interface PlayerListAccessor {
    @Accessor("stats")
    Map<UUID, ServerStatsCounter> getStats();

    @Accessor("advancements")
    Map<UUID, PlayerAdvancements> getAdvancements();
}
