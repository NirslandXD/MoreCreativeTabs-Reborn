package me.hypherionmc.morecreativetabs.client;

import me.hypherionmc.morecreativetabs.MoreCreativeTabs;
import me.hypherionmc.morecreativetabs.platform.services.IPlatformHelper;
import me.hypherionmc.morecreativetabs.mixin.CreativeModeTabAccessor;
import net.minecraft.world.item.CreativeModeTab;

/**
 * @author HypherionSA
 * Forge Class for Platform Specific code
 */
public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public void setNewTabs(CreativeModeTab[] tabs) {
        CreativeModeTabAccessor.setTabs(tabs);
    }

    @Override
    public void reloadTabs() {
        MoreCreativeTabs.ClientEvents.reloadTabs();
    }

}
