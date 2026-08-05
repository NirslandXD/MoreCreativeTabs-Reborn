package me.hypherionmc.morecreativetabs.client;

import me.hypherionmc.morecreativetabs.ModConstants;
import me.hypherionmc.morecreativetabs.client.impl.ForgeCreativeTabUtils;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import me.hypherionmc.morecreativetabs.client.tabs.TabReloadResult;
import me.hypherionmc.morecreativetabs.mixin.accessor.CreativeModeTabsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.CreativeModeTabRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class ForgeResourceLoader implements ResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        reloadTabs(resourceManager);
    }

    public static TabReloadResult reloadTabs(ResourceManager manager) {
        CustomCreativeTabRegistry.INSTANCE.clearTabs();
        LinkedHashSet<CreativeModeTab> registeredTabs = new LinkedHashSet<>(CreativeModeTabsAccessor.getTabs());
        Map<CreativeModeTab, ResourceLocation> registryIds = new IdentityHashMap<>();
        CreativeModeTabRegistry.getSortedCreativeModeTabs().forEach(tab -> {
            registeredTabs.add(tab);
            ResourceLocation id = CreativeModeTabRegistry.getName(tab);
            if (id != null)
                registryIds.put(tab, id);
        });
        CustomCreativeTabRegistry.INSTANCE.setVanillaTabs(new ArrayList<>(registeredTabs));
        CustomCreativeTabRegistry.INSTANCE.setTabRegistryIds(registryIds);

        ModConstants.logger.info("Checking for custom creative tabs");
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
        ForgeCreativeTabUtils.validateTabs(CustomCreativeTabRegistry.INSTANCE.sortedTabs());
        rebuildCreativeContents();
        TabReloadResult result = CustomCreativeTabRegistry.INSTANCE.createReloadResult(discoveredTabs, disabledFiles, orderedFiles);
        if (entries.isEmpty())
            ModConstants.logger.warn("No MoreCreativeTabs resource definitions are active. Add a resource pack containing assets/<namespace>/morecreativetabs/*.json");
        ModConstants.logger.info(result.summary());
        return result;
    }

    private static void rebuildCreativeContents() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null)
            return;

        try {
            for (Method method : CreativeModeTabs.class.getDeclaredMethods()) {
                if (!method.getName().equals("tryRebuildTabContents")
                        && !method.getName().equals("m_257580_")
                        && !method.getName().equals("m_269226_"))
                    continue;

                Class<?>[] parameterTypes = method.getParameterTypes();
                Object[] arguments = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    if (parameterType.getName().equals("net.minecraft.world.flag.FeatureFlagSet")) {
                        arguments[i] = minecraft.level.enabledFeatures();
                    } else if (parameterType == boolean.class) {
                        arguments[i] = minecraft.player != null && minecraft.player.canUseGameMasterBlocks();
                    } else if (parameterType.getName().equals("net.minecraft.core.HolderLookup$Provider")) {
                        if (minecraft.getConnection() == null) {
                            arguments = null;
                            break;
                        }
                        arguments[i] = minecraft.getConnection().registryAccess();
                    } else {
                        arguments = null;
                        break;
                    }
                }

                if (arguments != null) {
                    method.invoke(null, arguments);
                    return;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            ModConstants.logger.error("Failed to rebuild creative tab contents after reload", e);
        }
    }

}
