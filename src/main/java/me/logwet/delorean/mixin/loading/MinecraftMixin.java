package me.logwet.delorean.mixin.loading;

import me.logwet.delorean.DeLorean;
import me.logwet.delorean.patch.SaveSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @ModifyVariable(method = "doLoadLevel", at = @At("STORE"), ordinal = 0)
    private LevelStorageAccess injectPatchedLevelStorageAccess(
            LevelStorageAccess levelStorageAccess) {
        if (SaveSlots.isLoading.get()) {
            int slot = SaveSlots.currentSlot.get();
            LevelStorageAccess access = SaveSlots.getSaveSlot(slot).patchedStorageSource;
            if (access != null) {
                return access;
            } else {
                DeLorean.log(
                        Level.ERROR,
                        "Unable to grab levelStorageAccess for slot " + slot + " while loading.");
            }
        }
        return levelStorageAccess;
    }
}
