package me.hypherionmc.morecreativetabs.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import me.hypherionmc.morecreativetabs.ModConstants;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * @author HypherionSA
 */
public class MoreCreativeTabsFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        /* Register Client Commands */
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            ModConstants.logger.info("Registering Commands");
            dispatcher.register(literal("mct").then(literal("showTabNames")
                            .then(argument("enabled", bool()).executes(context -> {
                                boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                CustomCreativeTabRegistry.INSTANCE.setShowTabNames(enabled);
                                context.getSource().sendFeedback(enabled ? Component.literal("Showing tab registry names") : Component.literal("Showing tab names"));
                                return 1;
                            }))).then(literal("reloadTabs").executes(ctx -> {
                    var result = FabricResourceLoader.reloadTabs();
                    CustomCreativeTabRegistry.INSTANCE.refreshOpenScreen();
                    ctx.getSource().sendFeedback(Component.literal(result.summary()));
                        return 1;
                    }))
            );
        });


        /* Load initial entries and cache old tabs */
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new FabricResourceLoader());
    }
}
