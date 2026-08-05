package me.hypherionmc.morecreativetabs.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import me.hypherionmc.morecreativetabs.ModConstants;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.PackType;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

public class MoreCreativeTabsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModConstants.logger.info("Registering Commands");
        ClientCommandManager.DISPATCHER.register(literal("mct").then(literal("showTabNames")
                            .then(argument("enabled", bool()).executes(context -> {
                                boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                CustomCreativeTabManager.showNames = enabled;
                                context.getSource().sendFeedback(enabled ? new TextComponent("Showing tab registry names") : new TextComponent("Showing tab names"));
                                return 1;
                            }))).then(literal("reloadTabs").executes(ctx -> {
                        var result = FabricResourceLoader.reloadTabs();
                        CustomCreativeTabManager.refreshOpenScreen();
                        ctx.getSource().sendFeedback(new TextComponent(result.summary()));
                        return 1;
                    }))
            );

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new FabricResourceLoader());
    }
}
