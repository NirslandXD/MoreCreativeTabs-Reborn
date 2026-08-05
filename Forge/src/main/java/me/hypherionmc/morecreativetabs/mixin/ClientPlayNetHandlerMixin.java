package me.hypherionmc.morecreativetabs.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.network.play.server.SCommandListPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetHandler.class)
public class ClientPlayNetHandlerMixin {

    @Shadow private CommandDispatcher<CommandSource> commandDispatcher;

    @Inject(method = "handleCommandList", at = @At("TAIL"))
    private void addMoreCreativeTabsCommands(SCommandListPacket packet, CallbackInfo callback) {
        this.commandDispatcher.register(Commands.literal("mct")
                .then(Commands.literal("reloadTabs").executes(context -> 0))
                .then(Commands.literal("showTabNames")
                        .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(context -> 0))));
    }
}
