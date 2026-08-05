package me.hypherionmc.morecreativetabs.client.impl;

import me.hypherionmc.morecreativetabs.mixin.accessor.CreativeModeTabsAccessor;
import net.fabricmc.fabric.impl.client.itemgroup.FabricCreativeGuiComponents;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroup;
import net.fabricmc.fabric.mixin.itemgroup.ItemGroupAccessor;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.List;

public final class FabricCreativeTabUtils {

    private FabricCreativeTabUtils() {
    }

    public static void validateTabs(List<CreativeModeTab> tabs) {
        int tabsPerPage = 10;
        int count = 0;

        CreativeModeTab operatorTab = CreativeModeTabsAccessor.getOpBlockTab();

        for (CreativeModeTab tab : tabs) {
            final FabricItemGroup fabricItemGroup = (FabricItemGroup) tab;

            if (FabricCreativeGuiComponents.COMMON_GROUPS.contains(tab) || tab == operatorTab) {
                fabricItemGroup.setPage(0);
                continue;
            }

            final ItemGroupAccessor itemGroupAccessor = (ItemGroupAccessor) tab;
            fabricItemGroup.setPage(count / tabsPerPage);
            int pageIndex = count % tabsPerPage;
            CreativeModeTab.Row row = pageIndex < tabsPerPage / 2 ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM;
            itemGroupAccessor.setRow(row);
            itemGroupAccessor.setColumn(row == CreativeModeTab.Row.TOP ? pageIndex % tabsPerPage : (pageIndex - tabsPerPage / 2) % tabsPerPage);

            count++;
        }

        // Give hidden tabs unique pages to avoid collisions in Fabric API.
        // Use the raw backing tab list here, not CreativeModeTabs.allTabs(), because
        // our mixin overrides allTabs() to return the filtered/custom ordering.
        int hiddenCount = 1000;
        for (CreativeModeTab tab : CreativeModeTabsAccessor.getTabs()) {
            if (tabs.contains(tab) || FabricCreativeGuiComponents.COMMON_GROUPS.contains(tab) || tab == operatorTab) {
                continue;
            }

            final FabricItemGroup fabricItemGroup = (FabricItemGroup) tab;
            final ItemGroupAccessor itemGroupAccessor = (ItemGroupAccessor) tab;
            fabricItemGroup.setPage(hiddenCount++);
            itemGroupAccessor.setRow(CreativeModeTab.Row.TOP);
            itemGroupAccessor.setColumn(0);
        }
    }

}
