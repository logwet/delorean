package me.logwet.delorean.mixin.saving;

import com.google.gson.Gson;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerAdvancements.class)
public interface PlayerAdvancementsAccessor {
    @Accessor("GSON")
    static Gson getGson() {
        throw new AssertionError();
    }

    @Accessor("advancements")
    Map<Advancement, AdvancementProgress> getAdvancements();
}
