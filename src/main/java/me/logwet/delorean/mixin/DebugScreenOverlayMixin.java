package me.logwet.delorean.mixin;

import java.util.List;
import me.logwet.delorean.DeLorean;
import me.logwet.delorean.patch.SaveSlots;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin {

    @Unique boolean occured = false;

    /**
     * @author DuncanRuns
     * @reason Puts mod notice in F3 menu
     */
    @Inject(at = @At("RETURN"), method = "getGameInformation")
    private void injectGetLeftText(CallbackInfoReturnable<List<String>> info) {
        if (!occured) {
            SaveSlots.genSaveSlot(3);

            //            SaveSlots.save(3);

            SaveSlots.load(3);

            occured = true;
        }

        info.getReturnValue().add(DeLorean.MODID + " mod v" + DeLorean.VERSION + " by logwet");
    }
}
