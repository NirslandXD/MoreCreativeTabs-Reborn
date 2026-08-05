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
            hasRun = true;
        }
        reloadTabs(resourceManager);
    }

    public static TabReloadResult reloadTabs() {
        return reloadTabs(Minecraft.getInstance().getResourceManager());
    }

    public static TabReloadResult reloadTabs(ResourceManager resourceManager) {
        ModConstants.logger.info("Checking for custom creative tabs");
        CustomCreativeTabManager.clearTabs();
        Map<ResourceLocation, Resource> entries = resourceManager.listResources("morecreativetabs",
                path -> path.getPath().endsWith(".json"));
        Map<ResourceLocation, Resource> customTabs = new HashMap<>();
        int[] policyFiles = new int[2];
        entries.entrySet().stream().sorted(java.util.Comparator.comparing(entry -> entry.getKey().toString())).forEach(entry -> {
            int policies = CustomCreativeTabManager.processPolicyEntry(entry.getKey(), entry.getValue());
            if (policies == 0) {
                customTabs.put(entry.getKey(), entry.getValue());
            } else {
                if ((policies & CustomCreativeTabManager.DISABLED_POLICY) != 0) {
                    policyFiles[0]++;
                }
                if ((policies & CustomCreativeTabManager.ORDERED_POLICY) != 0) {
                    policyFiles[1]++;
                }
            }
        });

        CustomCreativeTabManager.loadEntries(customTabs, new FabricTabCreator());
        TabReloadResult result = CustomCreativeTabManager.createReloadResult(customTabs.size(), policyFiles[0], policyFiles[1]);
        if (entries.isEmpty()) {
            ModConstants.logger.warn("No MoreCreativeTabs resource definitions are active. Add a resource pack containing assets/<namespace>/morecreativetabs/*.json");
        }
        ModConstants.logger.info(result.summary());
        return result;
    }

}
