package me.hypherionmc.morecreativetabs.client;

import me.hypherionmc.morecreativetabs.ModConstants;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import me.hypherionmc.morecreativetabs.client.tabs.TabReloadResult;
<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
import net.minecraft.client.Minecraft;
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
<<<<<<< HEAD

import java.util.ArrayList;
import java.util.HashMap;
=======
<<<<<<< HEAD

import java.util.ArrayList;
import java.util.HashMap;
=======
import net.minecraft.world.item.CreativeModeTabs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
import java.util.Map;

public class ForgeResourceLoader implements ResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        reloadTabs(resourceManager);
    }

    public static TabReloadResult reloadTabs(ResourceManager manager) {
        CustomCreativeTabRegistry.INSTANCE.setVanillaTabs(new ArrayList<>(BuiltInRegistries.CREATIVE_MODE_TAB.stream().toList()));
        ModConstants.logger.info("Checking for custom creative tabs");
        CustomCreativeTabRegistry.INSTANCE.clearTabs();
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 79b2a8a (initial commit)
        Map<ResourceLocation, Resource> customTabs = manager.listResources("morecreativetabs",
                path -> path.getPath().endsWith(".json") && !isDisabledTabsFile(path)
                        && !isOrderedTabsFile(path));
        Map<ResourceLocation, Resource> disabledTabs = manager.listResources("morecreativetabs", ForgeResourceLoader::isDisabledTabsFile);
        Map<ResourceLocation, Resource> orderedTabs = manager.listResources("morecreativetabs", ForgeResourceLoader::isOrderedTabsFile);

        if (!disabledTabs.isEmpty())
            CustomCreativeTabRegistry.INSTANCE.loadDisabledTabs(disabledTabs);
        if (!orderedTabs.isEmpty())
            CustomCreativeTabRegistry.INSTANCE.loadOrderedTabs(orderedTabs);

        Map<ResourceLocation, Resource> actualTabs = new HashMap<>();
        customTabs.forEach((location, resource) -> {
            if (!CustomCreativeTabRegistry.INSTANCE.processPolicyEntry(location, resource))
                actualTabs.put(location, resource);
        });
        CustomCreativeTabRegistry.INSTANCE.processEntries(actualTabs);
        int policyFiles = disabledTabs.size() + customTabs.size() - actualTabs.size();
        TabReloadResult result = CustomCreativeTabRegistry.INSTANCE.createReloadResult(actualTabs.size(), policyFiles, orderedTabs.size());
        if (customTabs.isEmpty() && disabledTabs.isEmpty() && orderedTabs.isEmpty())
<<<<<<< HEAD
=======
=======
        Map<ResourceLocation, Resource> entries = manager.listResources("morecreativetabs",
                path -> path.getPath().endsWith(".json"));

        Map<ResourceLocation, Resource> actualTabs = new LinkedHashMap<>();
        int discoveredTabs = 0;
        int disabledFiles = 0;
        int orderedFiles = 0;
        for (Map.Entry<ResourceLocation, Resource> entry : entries.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            CustomCreativeTabRegistry.PolicyFileResult policy = CustomCreativeTabRegistry.INSTANCE.processPolicyEntry(entry.getKey(), entry.getValue());
            if (!policy.policyFile() && !policy.malformed()) {
                actualTabs.put(entry.getKey(), entry.getValue());
                discoveredTabs++;
            } else if (policy.failed()) {
                discoveredTabs++;
            }
            if (policy.disabledFile())
                disabledFiles++;
            if (policy.orderedFile())
                orderedFiles++;
        }
        CustomCreativeTabRegistry.INSTANCE.processEntries(actualTabs);
        rebuildCreativeContents();
        TabReloadResult result = CustomCreativeTabRegistry.INSTANCE.createReloadResult(discoveredTabs, disabledFiles, orderedFiles);
        if (entries.isEmpty())
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
            ModConstants.logger.warn("No MoreCreativeTabs resource definitions are active. Add a resource pack containing assets/<namespace>/morecreativetabs/*.json");
        ModConstants.logger.info(result.summary());
        return result;
    }

<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 79b2a8a (initial commit)
    private static boolean isDisabledTabsFile(ResourceLocation path) {
        return path.getPath().endsWith("disabled_tabs.json") || path.getPath().endsWith("disable.json");
    }

    private static boolean isOrderedTabsFile(ResourceLocation path) {
        return path.getPath().endsWith("ordered_tabs.json");
    }
<<<<<<< HEAD
=======
=======
    private static void rebuildCreativeContents() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.getConnection() == null)
            return;

        CreativeModeTabs.tryRebuildTabContents(
                minecraft.level.enabledFeatures(),
                minecraft.player != null && minecraft.player.canUseGameMasterBlocks(),
                minecraft.getConnection().registryAccess());
    }

>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
}
