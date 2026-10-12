package org.mcdatabase.openmcsurvey.mixin;

import org.mcdatabase.openmcsurvey.OpenMCSurvey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.Minecraft.class)
public class MinecraftInitMixin {
    @Inject(at = @At("TAIL"), method = "<init>")
    private void openmcsurvey$surveyOnLaunch(CallbackInfo ci) {
        OpenMCSurvey.collectAndSend();
    }
}
