package me.hypherionmc.morecreativetabs.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Shadow private CommandDispatcher<SharedSuggestionProvider> commands;

    @Inject(method = "handleCommands", at = @At("TAIL"))
    private void addMoreCreativeTabsCommands(ClientboundCommandsPacket packet, CallbackInfo callback) {
        this.commands.register(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("mct")
                .then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("reloadTabs").executes(context -> 0))
                .then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("showTabNames")
                        .then(RequiredArgumentBuilder.<SharedSuggestionProvider, Boolean>argument("enabled", BoolArgumentType.bool())
                                .executes(context -> 0))));
    }
}
