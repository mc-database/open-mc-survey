package org.mcdatabase.openmcsurvey.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.mcdatabase.openmcsurvey.OpenMCSurvey;
import org.mcdatabase.openmcsurvey.screen.ConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// hacky but works, trying to do everything without fapi so we can have multiloader later
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(at = @At("TAIL"), method = "handleCommands")
    private void openmcsurvey$registerSuggestionNode(ClientboundCommandsPacket packet, CallbackInfo ci) {
        openmcsurvey$registerSuggestionNode();
    }

    @Inject(at = @At("TAIL"), method = "handleLogin")
    private void openmcsurvey$registerSuggestionNodeOnLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        openmcsurvey$registerSuggestionNode();
    }

    @Unique
    private void openmcsurvey$registerSuggestionNode() {
        CommandDispatcher<ClientSuggestionProvider> dispatcher = ((ClientPacketListener) (Object) this).getCommands();
        if (dispatcher.getRoot().getChild(OpenMCSurvey.COMMAND) == null) {
            dispatcher.register(LiteralArgumentBuilder.<ClientSuggestionProvider>literal(OpenMCSurvey.COMMAND).executes(_ -> 0));
        }
    }

    @Inject(at = @At("HEAD"), method = "sendCommand", cancellable = true)
    private void openmcsurvey$openConfigCommand(String command, CallbackInfo ci) {
        if (!command.trim().equals(OpenMCSurvey.COMMAND)) {
            return;
        }
        ci.cancel();
        openmcsurvey$openConfigScreen();
    }

    @Inject(at = @At("HEAD"), method = "sendUnattendedCommand", cancellable = true)
    private void openmcsurvey$openConfigCommandUnattended(String command, Screen screenAfterCommand, CallbackInfo ci) {
        if (!command.trim().equals(OpenMCSurvey.COMMAND)) {
            return;
        }
        ci.cancel();
        openmcsurvey$openConfigScreen();
    }

    @Unique
    private static void openmcsurvey$openConfigScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.schedule(() -> minecraft.gui.setScreen(new ConfigScreen(minecraft.gui.screen())));
    }
}
