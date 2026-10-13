package org.mcdatabase.openmcsurvey.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.mcdatabase.openmcsurvey.OpenMCSurvey;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends ConfirmScreen {
    private final @Nullable Screen parent;

    public ConfigScreen(@Nullable Screen parent) {
        super(
                result -> {
                    OpenMCSurvey.setOptedIn(result);
                    Minecraft.getInstance().gui.setScreen(parent);
                },
                Component.translatable("openmcsurvey.config.title").withStyle(ChatFormatting.UNDERLINE),
                Component.empty()
                        .append(Component.translatable("openmcsurvey.optin.message.prefix"))
                        .append(Component.literal(" "))
                        .append(Component.translatable("openmcsurvey.optin.message.bold")
                                .withStyle(ChatFormatting.BOLD))
                        .append(Component.literal(" "))
                        .append(Component.translatable("openmcsurvey.config.message.suffix"))
                        .append(Component.literal("\n\n"))
                        .append(Component.translatable(OpenMCSurvey.noUserConsent()
                                ? "openmcsurvey.config.status.out"
                                : "openmcsurvey.config.status.in")),
                Component.translatable("openmcsurvey.config.optin"),
                Component.translatable("openmcsurvey.config.optout"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().gui.setScreen(parent);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
