package me.hypherionmc.morecreativetabs.client;

import me.hypherionmc.morecreativetabs.MoreCreativeTabs;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;

public class ForgeResourceReloader implements IResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        MoreCreativeTabs.reloadResources(resourceManager);
    }
}
