package org.mcdatabase.openmcsurvey.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.mcdatabase.openmcsurvey.OpenMCSurvey;

@Environment(EnvType.CLIENT)
public class OptInScreen extends ConfirmScreen {
    private static final Component TITLE =
            Component.translatable("openmcsurvey.optin.title").withStyle(ChatFormatting.UNDERLINE);
    private static final Component MESSAGE = Component.empty()
            .append(Component.translatable("openmcsurvey.optin.message.prefix"))
            .append(Component.literal("\n\n"))
            .append(Component.translatable("openmcsurvey.optin.message.bold").withStyle(ChatFormatting.BOLD))
            .append(Component.literal("\n\n"))
            .append(Component.translatable("openmcsurvey.optin.message.suffix"));
    private static final Component OPT_IN_BUTTON = Component.translatable("openmcsurvey.optin.optin");

    private final @Nullable Screen parent;

    public OptInScreen(@Nullable Screen parent) {
        super(
                result -> {
                    OpenMCSurvey.setOptedIn(result);
                    Minecraft.getInstance().gui.setScreen(parent);
                },
                TITLE,
                MESSAGE,
                OPT_IN_BUTTON,
                CommonComponents.GUI_CANCEL);
        this.parent = parent;
    }

    @Override
    public void onClose() {
        OpenMCSurvey.setOptedIn(false);
        Minecraft.getInstance().gui.setScreen(parent);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
