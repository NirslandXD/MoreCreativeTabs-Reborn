package me.hypherionmc.morecreativetabs;

import me.hypherionmc.morecreativetabs.client.ForgeResourceReloader;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabManager;
import me.hypherionmc.morecreativetabs.client.tabs.TabReloadResult;
import me.hypherionmc.morecreativetabs.util.CreativeTabUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Mod(ModConstants.MOD_ID)
public class MoreCreativeTabs {

    private static boolean hasRun = false;

    public MoreCreativeTabs() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "", (a, b) -> true));
    }

    public static void reloadResources(ResourceManager manager) {
        if (!hasRun) {
            CustomCreativeTabManager.tabsBefore = CreativeModeTab.TABS;
            reloadTabs(manager);
            hasRun = true;
        } else {
            reloadTabs(manager);
        }
    }

    public static TabReloadResult reloadTabs(ResourceManager manager) {
        ModConstants.logger.info("Checking for custom creative tabs");
        CustomCreativeTabManager.clearTabs();
        Map<ResourceLocation, Resource> entries = findResources(manager,
                manager.listResources("morecreativetabs", path -> path.endsWith(".json")));
        Map<ResourceLocation, Resource> customTabs = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> entry : sortedEntries(entries)) {
            if (!CustomCreativeTabManager.processPolicyEntry(entry.getKey(), entry.getValue())) {
                addFreshResource(manager, customTabs, entry.getKey());
            }
        }

        CustomCreativeTabManager.loadEntries(customTabs,
                (jsonHelper, stacks) -> CreativeTabUtils.defaultTabCreator(-1, jsonHelper, stacks));
        TabReloadResult result = CustomCreativeTabManager.createReloadResult(customTabs.size());
        if (entries.isEmpty()) {
            ModConstants.logger.warn("No MoreCreativeTabs resource definitions are active. Add a resource pack containing assets/<namespace>/morecreativetabs/*.json");
        }
        ModConstants.logger.info(result.summary());
        return result;
    }

    private static Map<ResourceLocation, Resource> findResources(ResourceManager manager, Collection<ResourceLocation> locations) {
        Map<ResourceLocation, Resource> resources = new HashMap<>();
        for (ResourceLocation location : locations) {
            try {
                resources.put(location, manager.getResource(location));
            } catch (Exception e) {
                ModConstants.logger.error("Failed to open resource {}", location, e);
            }
        }
        return resources;
    }

    private static Collection<Map.Entry<ResourceLocation, Resource>> sortedEntries(Map<ResourceLocation, Resource> entries) {
        java.util.List<Map.Entry<ResourceLocation, Resource>> sorted = new java.util.ArrayList<>(entries.entrySet());
        sorted.sort(java.util.Comparator.comparing(entry -> entry.getKey().toString()));
        return sorted;
    }

    private static void addFreshResource(ResourceManager manager, Map<ResourceLocation, Resource> resources, ResourceLocation location) {
        try {
            resources.put(location, manager.getResource(location));
        } catch (Exception e) {
            ModConstants.logger.error("Failed to reopen resource {}", location, e);
        }
    }


    @Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new ForgeResourceReloader());
        }


        public static TabReloadResult reloadTabs() {
            return MoreCreativeTabs.reloadTabs(net.minecraft.client.Minecraft.getInstance().getResourceManager());
        }
    }
}
