package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.commands.MoreCreativeTabsCommand;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void handleMoreCreativeTabsCommand(String message, CallbackInfo callback) {
        if (MoreCreativeTabsCommand.handleCommand(message))
            callback.cancel();
    }
}
