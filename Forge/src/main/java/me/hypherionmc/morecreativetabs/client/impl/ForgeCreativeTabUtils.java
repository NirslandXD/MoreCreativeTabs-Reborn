package me.hypherionmc.morecreativetabs.client.impl;

import me.hypherionmc.morecreativetabs.mixin.accessor.CreativeModeTabsAccessor;
import me.hypherionmc.morecreativetabs.mixin.accessor.ForceCreativeTabAccessor;
import net.minecraft.world.item.CreativeModeTab;

import java.util.HashMap;
import java.util.List;

public final class ForgeCreativeTabUtils {

    private ForgeCreativeTabUtils() {
    }

    public static void validateTabs(List<CreativeModeTab> tabs) {
        int tabsPerPage = 10;
        int count = 0;

        for (CreativeModeTab tab : tabs) {
            ForgeTabData forgeTab = (ForgeTabData) tab;
            if (isDefaultTab(tab)) {
                forgeTab.setPageIndex(0);
                continue;
            }

            ForceCreativeTabAccessor tabAccessor = (ForceCreativeTabAccessor) tab;
            int pageIndex = count % tabsPerPage;
            forgeTab.setPageIndex(count / tabsPerPage);
            CreativeModeTab.Row row = pageIndex < (tabsPerPage / 2) ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM;
            tabAccessor.setRow(row);
            tabAccessor.setColumn(row == CreativeModeTab.Row.TOP ? pageIndex % tabsPerPage : (pageIndex - tabsPerPage / 2) % tabsPerPage);

            count++;
        }

        record TabPosition(CreativeModeTab.Row row, int column, int page) { }
        HashMap<TabPosition, String> positions = new HashMap<>();
        for (CreativeModeTab tab : tabs) {
            ForgeTabData forgeTab = (ForgeTabData) tab;
            TabPosition position = new TabPosition(tab.row(), tab.column(), forgeTab.getPageIndex());
            String existingName = positions.put(position, tab.getDisplayName().getString());
            if (existingName != null) {
                throw new IllegalArgumentException("Duplicate position: (%s) for item groups %s vs %s".formatted(position, tab.getDisplayName().getString(), existingName));
            }
        }
    }

    private static boolean isDefaultTab(CreativeModeTab tab) {
        return tab == CreativeModeTabsAccessor.getSearchTab()
                || tab == CreativeModeTabsAccessor.getHotbarTab()
                || tab == CreativeModeTabsAccessor.getInventoryTab()
                || tab == CreativeModeTabsAccessor.getOpBlockTab();
    }
}
