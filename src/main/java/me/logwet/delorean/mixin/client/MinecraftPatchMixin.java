package me.logwet.delorean.mixin.client;

import com.mojang.datafixers.util.Function4;
import java.util.function.Function;
import me.logwet.delorean.patch.PatchedMinecraft;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Minecraft.ExperimentalDialogType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.RegistryHolder;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public abstract class MinecraftPatchMixin implements PatchedMinecraft {
    @Shadow
    protected abstract void doLoadLevel(
            String string,
            RegistryHolder registryHolder,
            Function<LevelStorageAccess, DataPackConfig> function,
            Function4<
                            LevelStorageAccess,
                            RegistryHolder,
                            ResourceManager,
                            DataPackConfig,
                            WorldData>
                    function4,
            boolean bl,
            ExperimentalDialogType experimentalDialogType);

    public void loadSaveStateLevel(String string) {
        this.doLoadLevel(
                string,
                RegistryAccess.builtin(),
                Minecraft::loadDataPacks,
                Minecraft::loadWorldData,
                true,
                ExperimentalDialogType.NONE);
    }
}
