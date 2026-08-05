package me.hypherionmc.morecreativetabs.client.tabs;

import me.hypherionmc.morecreativetabs.client.data.jsonhelpers.CustomCreativeTab;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Creates a platform-specific creative tab from parsed configuration. */
public interface TabCreator {
    CreativeModeTab createTab(CustomCreativeTab jsonHelper, List<ItemStack> stacks);
}
