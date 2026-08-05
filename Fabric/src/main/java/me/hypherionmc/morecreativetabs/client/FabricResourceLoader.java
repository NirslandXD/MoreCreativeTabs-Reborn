package me.hypherionmc.morecreativetabs.client;

import me.hypherionmc.morecreativetabs.ModConstants;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabManager;
import me.hypherionmc.morecreativetabs.client.tabs.TabReloadResult;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.CreativeModeTab;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FabricResourceLoader implements SimpleSynchronousResourceReloadListener {

    private boolean hasRun = false;

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation("morecreativetabs", "tabs");
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        if (!hasRun) {
            CustomCreativeTabManager.tabsBefore = CreativeModeTab.TABS;
            reloadTabs(resourceManager);
            hasRun = true;
        } else {
            reloadTabs(resourceManager);
        }
    }

    public static TabReloadResult reloadTabs() {
        return reloadTabs(Minecraft.getInstance().getResourceManager());
    }

    public static TabReloadResult reloadTabs(ResourceManager manager) {
        ModConstants.logger.info("Checking for custom creative tabs");
        CustomCreativeTabManager.clearTabs();
        Map<ResourceLocation, Resource> entries = findResources(manager,
                manager.listResources("morecreativetabs", path -> path.endsWith(".json")));
        Map<ResourceLocation, Resource> customTabs = new HashMap<>();
        int disabledFiles = 0;
        int orderedFiles = 0;
        for (Map.Entry<ResourceLocation, Resource> entry : sortedEntries(entries)) {
            int policies = CustomCreativeTabManager.processPolicyEntry(entry.getKey(), entry.getValue());
            if (policies == 0) {
                addFreshResource(manager, customTabs, entry.getKey());
                continue;
            }
            if ((policies & CustomCreativeTabManager.DISABLED_POLICY) != 0) {
                disabledFiles++;
            }
            if ((policies & CustomCreativeTabManager.ORDERED_POLICY) != 0) {
                orderedFiles++;
            }
        }

        CustomCreativeTabManager.loadEntries(customTabs, new FabricTabCreator());
        TabReloadResult result = CustomCreativeTabManager.createReloadResult(customTabs.size(), disabledFiles, orderedFiles);
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

}
