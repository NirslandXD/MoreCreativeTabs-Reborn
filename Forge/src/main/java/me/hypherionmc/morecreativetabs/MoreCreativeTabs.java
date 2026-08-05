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
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.HashMap;

@Mod(ModConstants.MOD_ID)
public class MoreCreativeTabs {

    private static boolean hasRun = false;

    public MoreCreativeTabs() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "", (a, b) -> true));
    }

    public static void reloadResources(ResourceManager resourceManager) {
        if (!hasRun) {
            CustomCreativeTabManager.tabsBefore = CreativeModeTab.TABS;
            hasRun = true;
        }
        reloadTabs(resourceManager);
    }

    public static TabReloadResult reloadTabs(ResourceManager resourceManager) {
        ModConstants.logger.info("Checking for custom creative tabs");
        CustomCreativeTabManager.clearTabs();
        final TabReloadResult[] result = new TabReloadResult[1];
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Map<ResourceLocation, Resource> entries = resourceManager.listResources("morecreativetabs",
                    path -> path.getPath().endsWith(".json"));
            Map<ResourceLocation, Resource> customTabs = new HashMap<>();
            entries.entrySet().stream().sorted(java.util.Comparator.comparing(entry -> entry.getKey().toString())).forEach(entry -> {
                if (!CustomCreativeTabManager.processPolicyEntry(entry.getKey(), entry.getValue())) {
                    customTabs.put(entry.getKey(), entry.getValue());
                }
            });

            CustomCreativeTabManager.loadEntries(customTabs, ((jsonHelper, stacks) -> CreativeTabUtils.defaultTabCreator(-1, jsonHelper, stacks)));
            result[0] = CustomCreativeTabManager.createReloadResult(customTabs.size());
            if (entries.isEmpty()) {
                ModConstants.logger.warn("No MoreCreativeTabs resource definitions are active. Add a resource pack containing assets/<namespace>/morecreativetabs/*.json");
            }
            ModConstants.logger.info(result[0].summary());
        });
        return result[0] == null ? CustomCreativeTabManager.createReloadResult(0) : result[0];
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
