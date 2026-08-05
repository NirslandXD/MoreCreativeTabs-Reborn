package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventorySearchMixin {

    @Shadow private static int selectedTab;
    @Shadow private EditBox searchBox;
    @Shadow @Final private Set<TagKey<Item>> visibleTags;
    @Shadow private float scrollOffs;

    @Inject(method = "init", at = @At("HEAD"))
    private void validateSelectedTab(CallbackInfo ci) {
        if (selectedTab < 0 || selectedTab >= CreativeModeTab.TABS.length)
            selectedTab = CreativeModeTab.TABS.length == 0 ? -1 : 0;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void updateInitialSearchBox(CallbackInfo ci) {
        CreativeModeTab tab = selectedTab >= 0 && selectedTab < CreativeModeTab.TABS.length ? CreativeModeTab.TABS[selectedTab] : null;
        updateSearchBox(tab, false);
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void updateCustomSearchOnTabSelect(CreativeModeTab tab, CallbackInfo ci) {
        updateSearchBox(tab, true);
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void typeInCustomTab(char character, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!isCustomSearch())
            return;

        String oldValue = searchBox.getValue();
        if (searchBox.charTyped(character, modifiers)) {
            if (!Objects.equals(oldValue, searchBox.getValue()))
                refreshCustomSearch();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void keyInCustomTab(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!isCustomSearch())
            return;

        String oldValue = searchBox.getValue();
        if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(oldValue, searchBox.getValue()))
                refreshCustomSearch();
            cir.setReturnValue(true);
            return;
        }

        if (searchBox.canConsumeInput() && keyCode != 256)
            cir.setReturnValue(true);
    }

    @Inject(method = "refreshSearchResults", at = @At("HEAD"), cancellable = true)
    private void refreshCustomTabSearch(CallbackInfo ci) {
        if (isVanillaSearch()) {
            ci.cancel();
            refreshVanillaSearch();
            return;
        }

        if (!isCustomSearch())
            return;

        ci.cancel();
        refreshCustomSearch();
    }


    private boolean isVanillaSearch() {
        return selectedCreativeTab() == CreativeModeTab.TAB_SEARCH && searchBox != null && searchBox.isVisible();
    }

    private boolean isCustomSearch() {
        CreativeModeTab tab = selectedCreativeTab();
        return tab != null && !CustomCreativeTabManager.isProtectedSearchTab(tab)
                && CustomCreativeTabManager.shouldHandleSearch(tab) && searchBox != null && searchBox.isVisible();
    }

    private CreativeModeTab selectedCreativeTab() {
        return selectedTab >= 0 && selectedTab < CreativeModeTab.TABS.length ? CreativeModeTab.TABS[selectedTab] : null;
    }

    private void updateSearchBox(CreativeModeTab tab, boolean refresh) {
        if (searchBox == null || tab == null)
            return;

        if (CustomCreativeTabManager.isSearchBarDisabled(tab)) {
            searchBox.setValue("");
            searchBox.setVisible(false);
            searchBox.setCanLoseFocus(true);
            searchBox.setFocus(false);
            ((Screen) (Object) this).setFocused(null);
            return;
        }

        if (!CustomCreativeTabManager.shouldHandleSearch(tab))
            return;

        searchBox.setVisible(true);
        searchBox.setCanLoseFocus(false);
        searchBox.setFocus(true);
        ((Screen) (Object) this).setFocused(searchBox);
        if (refresh)
            refreshCustomSearch();
    }

    private void refreshCustomSearch() {
        if (selectedTab < 0 || selectedTab >= CreativeModeTab.TABS.length)
            return;

        CreativeModeTab tab = CreativeModeTab.TABS[selectedTab];
        if (tab == CreativeModeTab.TAB_SEARCH) {
            refreshVanillaSearch();
            return;
        }
        if (tab == CreativeModeTab.TAB_HOTBAR || tab == CreativeModeTab.TAB_INVENTORY)
            return;
        CreativeModeInventoryScreen.ItemPickerMenu menu = (CreativeModeInventoryScreen.ItemPickerMenu) ((CreativeModeInventoryScreen) (Object) this).getMenu();
        menu.items.clear();
        visibleTags.clear();
        String query = searchBox.getValue().toLowerCase(Locale.ROOT).trim();
        boolean tagSearch = query.startsWith("#");
        if (tagSearch)
            query = query.substring(1);

        net.minecraft.core.NonNullList<ItemStack> stacks = net.minecraft.core.NonNullList.create();
        tab.fillItemList(stacks);
        for (ItemStack stack : stacks) {
            if (query.isEmpty() || matches(stack, query, tagSearch))
                menu.items.add(stack);
        }
        scrollOffs = 0.0F;
        menu.scrollTo(0.0F);
    }

    private void refreshVanillaSearch() {
        CreativeModeInventoryScreen.ItemPickerMenu menu = (CreativeModeInventoryScreen.ItemPickerMenu) ((CreativeModeInventoryScreen) (Object) this).getMenu();
        menu.items.clear();
        visibleTags.clear();

        String query = searchBox.getValue().toLowerCase(Locale.ROOT).trim();
        boolean tagSearch = query.startsWith("#");
        if (tagSearch)
            query = query.substring(1);

        LinkedHashSet<ItemStack> orderedMatches = new LinkedHashSet<>();
        for (CreativeModeTab tab : CustomCreativeTabManager.orderedVisibleTabs()) {
            if (CustomCreativeTabManager.isProtectedSearchTab(tab))
                continue;

            net.minecraft.core.NonNullList<ItemStack> stacks = net.minecraft.core.NonNullList.create();
            tab.fillItemList(stacks);
            for (ItemStack stack : stacks) {
                if ((query.isEmpty() || matches(stack, query, tagSearch)) && orderedMatches.stream().noneMatch(existing -> ItemStack.isSameItemSameTags(existing, stack))) {
                    orderedMatches.add(stack);
                }
            }
        }

        menu.items.addAll(orderedMatches);
        scrollOffs = 0.0F;
        menu.scrollTo(0.0F);
    }


    private boolean matches(ItemStack stack, String query, boolean tagSearch) {
        if (tagSearch) {
            return stack.getTags().anyMatch(tag -> {
                boolean matches = tag.location().toString().toLowerCase(Locale.ROOT).contains(query);
                if (matches)
                    visibleTags.add(tag);
                return matches;
            });
        }

        String itemId = Registry.ITEM.getKey(stack.getItem()).toString().toLowerCase(Locale.ROOT);
        if (itemId.contains(query))
            return true;

        Minecraft minecraft = Minecraft.getInstance();
        TooltipFlag flag = minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        return stack.getTooltipLines(minecraft.player, flag).stream()
                .anyMatch(line -> line.getString().toLowerCase(Locale.ROOT).contains(query));
    }
}
