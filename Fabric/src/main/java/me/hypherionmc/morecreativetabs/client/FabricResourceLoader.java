package me.hypherionmc.morecreativetabs.client;

import me.hypherionmc.morecreativetabs.ModConstants;
import me.hypherionmc.morecreativetabs.client.impl.FabricCreativeTabUtils;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import me.hypherionmc.morecreativetabs.client.tabs.TabReloadResult;
import me.hypherionmc.morecreativetabs.mixin.accessor.CreativeModeTabsAccessor;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.CreativeModeTabs;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author HypherionSA
 * Loads custom tab definitions during client resource reloads.
 */
public class FabricResourceLoader implements SimpleSynchronousResourceReloadListener {

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation("morecreativetabs", "tabs");
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        reloadTabs();
    }

    public static TabReloadResult reloadTabs() {
        CustomCreativeTabRegistry.INSTANCE.setVanillaTabs(CreativeModeTabsAccessor.getTabs());
        ModConstants.logger.info("Checking for custom creative tabs");
        CustomCreativeTabRegistry.INSTANCE.clearTabs();
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
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
        FabricCreativeTabUtils.validateTabs(CustomCreativeTabRegistry.INSTANCE.sortedTabs());
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
                int modifiers = method.getModifiers();
                if (method.getReturnType() != boolean.class
                        || !Modifier.isPublic(modifiers)
                        || !Modifier.isStatic(modifiers))
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
            ModConstants.logger.error("Failed to find creative tab rebuild method after reload");
        } catch (IllegalAccessException | InvocationTargetException e) {
            ModConstants.logger.error("Failed to rebuild creative tab contents after reload", e);
        }
    }

}
