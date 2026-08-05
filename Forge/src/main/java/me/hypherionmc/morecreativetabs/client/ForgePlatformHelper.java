package me.hypherionmc.morecreativetabs.client;

import me.hypherionmc.morecreativetabs.MoreCreativeTabs;
import me.hypherionmc.morecreativetabs.platform.services.IPlatformHelper;
import me.hypherionmc.morecreativetabs.mixin.CreativeModeTabAccessor;
import net.minecraft.item.ItemGroup;

/**
 * @author HypherionSA
 * Forge Class for Platform Specific code
 */
public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public void setNewTabs(ItemGroup[] tabs) {
        CreativeModeTabAccessor.setTabs(tabs);
    }

    @Override
    public void reloadTabs() {
        MoreCreativeTabs.ClientEvents.reloadTabs();
    }

}
