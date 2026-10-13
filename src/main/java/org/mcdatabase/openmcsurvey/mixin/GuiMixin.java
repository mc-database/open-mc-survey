package org.mcdatabase.openmcsurvey.mixin;

import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;
import org.mcdatabase.openmcsurvey.OpenMCSurvey;
import org.mcdatabase.openmcsurvey.screen.OptInScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.Gui.class)
public class GuiMixin {
    @Inject(at = @At("HEAD"), method = "setScreen", cancellable = true)
    private void openmcsurvey$showOptInFirst(@Nullable Screen screen, CallbackInfo ci) {
        if (screen instanceof OptInScreen || !OpenMCSurvey.shouldShowOptIn()) {
            return;
        }
        OpenMCSurvey.LOGGER.info("Showing one-time opt-in screen before {}", screen == null ? "null" : screen.getClass().getSimpleName());
        ((net.minecraft.client.gui.Gui) (Object) this).setScreen(new OptInScreen(screen));
        ci.cancel();
    }
}
