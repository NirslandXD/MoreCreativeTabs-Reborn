package me.hypherionmc.morecreativetabs.client.commands;

import me.hypherionmc.morecreativetabs.ModConstants;
import me.hypherionmc.morecreativetabs.MoreCreativeTabs;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MoreCreativeTabsCommand {

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String message = event.getMessage().trim();
        if (!message.regionMatches(true, 0, "/mct", 0, 4)
                || (message.length() > 4 && !Character.isWhitespace(message.charAt(4))))
            return;

        event.setCanceled(true);
        if (message.equalsIgnoreCase("/mct reloadTabs")) {
            me.hypherionmc.morecreativetabs.client.tabs.TabReloadResult result =
                    MoreCreativeTabs.reloadTabs(Minecraft.getInstance().getResourceManager());
            CustomCreativeTabManager.refreshOpenScreen();
            sendFeedback(result.summary());
            return;
        }

        if (message.toLowerCase(java.util.Locale.ROOT).startsWith("/mct showtabnames ")) {
            String value = message.substring("/mct showTabNames ".length()).trim();
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                boolean enabled = Boolean.parseBoolean(value);
                CustomCreativeTabManager.showNames = enabled;
                sendFeedback(enabled ? "Showing tab registry names" : "Showing tab names");
                return;
            }
        }

        sendFeedback("Usage: /mct reloadTabs or /mct showTabNames <true|false>");
    }

    private static void sendFeedback(String message) {
        Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(message));
    }
}
