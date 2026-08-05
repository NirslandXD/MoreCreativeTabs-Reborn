package me.hypherionmc.morecreativetabs.client.commands;

import me.hypherionmc.morecreativetabs.ModConstants;
import me.hypherionmc.morecreativetabs.MoreCreativeTabs;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MoreCreativeTabsCommand {

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        if (handleCommand(event.getMessage()))
            event.setCanceled(true);
    }

    public static boolean handleCommand(String input) {
        String message = input.trim();
        if (message.startsWith("/"))
            message = message.substring(1);
        if (!message.regionMatches(true, 0, "mct", 0, 3)
                || (message.length() > 3 && !Character.isWhitespace(message.charAt(3))))
            return false;

        if (message.equalsIgnoreCase("mct reloadTabs")) {
            me.hypherionmc.morecreativetabs.client.tabs.TabReloadResult result =
                    MoreCreativeTabs.reloadTabs(Minecraft.getInstance().getResourceManager());
            CustomCreativeTabManager.refreshOpenScreen();
            sendFeedback(result.summary());
            return true;
        }

        if (message.toLowerCase(java.util.Locale.ROOT).startsWith("mct showtabnames ")) {
            String value = message.substring("mct showTabNames ".length()).trim();
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                boolean enabled = Boolean.parseBoolean(value);
                CustomCreativeTabManager.showNames = enabled;
                sendFeedback(enabled ? "Showing tab registry names" : "Showing tab names");
                return true;
            }
        }

        sendFeedback("Usage: /mct reloadTabs or /mct showTabNames <true|false>");
        return true;
    }

    private static void sendFeedback(String message) {
        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new StringTextComponent(message));
    }
}
