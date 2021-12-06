package me.logwet.delorean.mixin.loading;

import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {
    //    @Redirect(
    //            method = "halt",
    //            at =
    //                    @At(
    //                            value = "INVOKE",
    //                            target =
    //
    // "Lnet/minecraft/client/server/IntegratedServer;executeBlocking(Ljava/lang/Runnable;)V"))
    //    public void redirectPlayerRemoval(IntegratedServer integratedServer, Runnable runnable) {
    //        if (SaveSlots.isLoading.get()) {
    //            return;
    //        } else {
    //            integratedServer.executeBlocking(runnable);
    //        }
    //    }
}
