package me.hypherionmc.morecreativetabs.client.impl;

import me.hypherionmc.morecreativetabs.mixin.accessor.CreativeModeTabsAccessor;
import net.fabricmc.fabric.impl.client.itemgroup.FabricCreativeGuiComponents;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroup;
import net.fabricmc.fabric.mixin.itemgroup.ItemGroupAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;

<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 79b2a8a (initial commit)
import java.util.HashMap;
import java.util.List;

public class FabricCreativeTabUtils {

    public static void validateTabs(List<CreativeModeTab> tabs) {
        int TABS_PER_PAGE = 10;
        int count = 0;

        CreativeModeTab OP_TAB = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getOpBlockTab());
<<<<<<< HEAD
=======
=======
import java.util.List;

public final class FabricCreativeTabUtils {

    private FabricCreativeTabUtils() {
    }

    public static void validateTabs(List<CreativeModeTab> tabs) {
        int tabsPerPage = 10;
        int count = 0;

        CreativeModeTab operatorTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getOpBlockTab());
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)

        for (CreativeModeTab tab : tabs) {
            final FabricItemGroup fabricItemGroup = (FabricItemGroup) tab;

<<<<<<< HEAD
            if (FabricCreativeGuiComponents.COMMON_GROUPS.contains(tab) || tab == OP_TAB) {
=======
<<<<<<< HEAD
            if (FabricCreativeGuiComponents.COMMON_GROUPS.contains(tab) || tab == OP_TAB) {
=======
            if (FabricCreativeGuiComponents.COMMON_GROUPS.contains(tab) || tab == operatorTab) {
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
                fabricItemGroup.setPage(0);
                continue;
            }

            final ItemGroupAccessor itemGroupAccessor = (ItemGroupAccessor) tab;
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 79b2a8a (initial commit)
            fabricItemGroup.setPage((count / TABS_PER_PAGE));
            int pageIndex = count % TABS_PER_PAGE;
            CreativeModeTab.Row row = pageIndex < (TABS_PER_PAGE / 2) ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM;
            itemGroupAccessor.setRow(row);
            itemGroupAccessor.setColumn(row == CreativeModeTab.Row.TOP ? pageIndex % TABS_PER_PAGE : (pageIndex - TABS_PER_PAGE / 2) % (TABS_PER_PAGE));
<<<<<<< HEAD
=======
=======
            fabricItemGroup.setPage(count / tabsPerPage);
            int pageIndex = count % tabsPerPage;
            CreativeModeTab.Row row = pageIndex < tabsPerPage / 2 ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM;
            itemGroupAccessor.setRow(row);
            itemGroupAccessor.setColumn(row == CreativeModeTab.Row.TOP ? pageIndex % tabsPerPage : (pageIndex - tabsPerPage / 2) % tabsPerPage);
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)

            count++;
        }

<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 79b2a8a (initial commit)
        // Overlapping group detection logic, with support for pages.
        record ItemGroupPosition(CreativeModeTab.Row row, int column, int page) { }
        var map = new HashMap<ItemGroupPosition, String>();

        for (CreativeModeTab tab : tabs) {
            final FabricItemGroup fabricItemGroup = (FabricItemGroup) tab;
            final String displayName = tab.getDisplayName().getString();
            final var position = new ItemGroupPosition(tab.row(), tab.column(), fabricItemGroup.getPage());
            final String existingName = map.put(position, displayName);

            if (existingName != null) {
                throw new IllegalArgumentException("Duplicate position: (%s) for item groups %s vs %s".formatted(position, displayName, existingName));
            }
<<<<<<< HEAD
=======
=======
        // Give hidden tabs unique pages to avoid collisions in Fabric API
        int hiddenCount = 1000;
        for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
            if (tabs.contains(tab) || FabricCreativeGuiComponents.COMMON_GROUPS.contains(tab) || tab == operatorTab) {
                continue;
            }

            final FabricItemGroup fabricItemGroup = (FabricItemGroup) tab;
            final ItemGroupAccessor itemGroupAccessor = (ItemGroupAccessor) tab;
            fabricItemGroup.setPage(hiddenCount++);
            itemGroupAccessor.setRow(CreativeModeTab.Row.TOP);
            itemGroupAccessor.setColumn(0);
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
        }
    }

}
