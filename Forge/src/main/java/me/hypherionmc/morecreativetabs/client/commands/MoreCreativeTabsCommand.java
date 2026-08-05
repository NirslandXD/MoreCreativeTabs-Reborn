package me.hypherionmc.morecreativetabs.client.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import me.hypherionmc.morecreativetabs.ModConstants;
import me.hypherionmc.morecreativetabs.MoreCreativeTabs;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabManager;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MoreCreativeTabsCommand {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mct").then(Commands.literal("showTabNames")
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                    CustomCreativeTabManager.showNames = enabled;
                    context.getSource().sendSuccess(enabled ? new TextComponent("Showing tab registry names") : new TextComponent("Showing tab names"), true);
                    return 1;
                }))).then(Commands.literal("reloadTabs").executes(context -> {
                    var result = MoreCreativeTabs.reloadTabs(Minecraft.getInstance().getResourceManager());
                    CustomCreativeTabManager.refreshOpenScreen();
                    context.getSource().sendSuccess(new TextComponent(result.summary()), true);
                    return 1;
                }))
        );
    }
}
