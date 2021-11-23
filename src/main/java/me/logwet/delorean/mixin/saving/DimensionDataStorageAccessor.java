package me.logwet.delorean.mixin.saving;

import java.util.Map;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionDataStorage.class)
public interface DimensionDataStorageAccessor {
    @Accessor("cache")
    Map<String, SavedData> getCache();
}
