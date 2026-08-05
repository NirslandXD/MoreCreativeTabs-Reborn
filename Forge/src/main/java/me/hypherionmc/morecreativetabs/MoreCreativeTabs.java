package me.hypherionmc.morecreativetabs;

import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabManager;
import me.hypherionmc.morecreativetabs.client.tabs.TabReloadResult;
import me.hypherionmc.morecreativetabs.util.CreativeTabUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Mod(ModConstants.MOD_ID)
public class MoreCreativeTabs {

    private static boolean hasRun = false;

    public MoreCreativeTabs() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(() -> "", (remoteVersion, isServer) -> true));
    }

    public static void reloadResources(IResourceManager manager) {
        if (!hasRun) {
            CustomCreativeTabManager.tabsBefore = ItemGroup.GROUPS;
            reloadTabs(manager);
            hasRun = true;
        } else {
            reloadTabs(manager);
        }
    }

    public static TabReloadResult reloadTabs(IResourceManager manager) {
        ModConstants.logger.info("Checking for custom creative tabs");
        CustomCreativeTabManager.clearTabs();
        Map<ResourceLocation, IResource> entries = findResources(manager,
                manager.getAllResourceLocations("morecreativetabs", path -> path.endsWith(".json")));
        Map<ResourceLocation, IResource> customTabs = new HashMap<>();
        for (Map.Entry<ResourceLocation, IResource> entry : sortedEntries(entries)) {
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

    private static Map<ResourceLocation, IResource> findResources(IResourceManager manager, Collection<ResourceLocation> locations) {
        Map<ResourceLocation, IResource> resources = new HashMap<>();
        for (ResourceLocation location : locations) {
            try {
                resources.put(location, manager.getResource(location));
            } catch (Exception e) {
                ModConstants.logger.error("Failed to open resource {}", location, e);
            }
        }
        return resources;
    }

    private static Collection<Map.Entry<ResourceLocation, IResource>> sortedEntries(Map<ResourceLocation, IResource> entries) {
        java.util.List<Map.Entry<ResourceLocation, IResource>> sorted = new java.util.ArrayList<>(entries.entrySet());
        sorted.sort(java.util.Comparator.comparing(entry -> entry.getKey().toString()));
        return sorted;
    }

    private static void addFreshResource(IResourceManager manager, Map<ResourceLocation, IResource> resources, ResourceLocation location) {
        try {
            resources.put(location, manager.getResource(location));
        } catch (Exception e) {
            ModConstants.logger.error("Failed to reopen resource {}", location, e);
        }
    }

    @Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void loadComplete(FMLLoadCompleteEvent event) {
            event.enqueueWork(() -> {
                IResourceManager manager = net.minecraft.client.Minecraft.getInstance().getResourceManager();
                if (manager instanceof IReloadableResourceManager) {
                    ((IReloadableResourceManager) manager).addReloadListener(new me.hypherionmc.morecreativetabs.client.ForgeResourceReloader());
                }
                reloadResources(manager);
            });
        }

        public static TabReloadResult reloadTabs() {
            return MoreCreativeTabs.reloadTabs(net.minecraft.client.Minecraft.getInstance().getResourceManager());
        }
    }


}
