package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CreativeModeInventoryScreen.class)
public class ForgeCreativeModeInventoryMixin {

    @Shadow private static CreativeModeTab selectedTab;
    @Shadow private EditBox searchBox;
    @Shadow private void refreshSearchResults() {}

    private boolean refreshingOnInit;

    @Inject(method = "selectTab", at = @At("HEAD"), cancellable = true)
    private void skipSelectedTab(CreativeModeTab tab, CallbackInfo ci) {
        if (!refreshingOnInit && tab == selectedTab)
            ci.cancel();
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void validateSelectedTab(CallbackInfo ci) {
        refreshingOnInit = true;
        List<CreativeModeTab> displayedTabs = CustomCreativeTabRegistry.INSTANCE.displayedTabs();
        if (!displayedTabs.contains(selectedTab) && !displayedTabs.isEmpty())
            selectedTab = displayedTabs.get(0);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void finishInventoryInit(CallbackInfo ci) {
        refreshingOnInit = false;
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void hideDisabledInitialSearchBox(CallbackInfo ci) {
        if (searchBox != null && CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(selectedTab))
            hideSearchBox();
    }

    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getBackgroundLocation()Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation useSearchBackground(CreativeModeTab tab) {
        if (CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(tab))
            return new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tab_items.png");
        if (CustomCreativeTabRegistry.INSTANCE.shouldHandleSearch(tab))
            return new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tab_item_search.png");

        return tab.getBackgroundLocation();
    }

    @Redirect(method = "selectTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;hasSearchBar()Z"))
    private boolean useConfiguredSearchBar(CreativeModeTab tab) {
        if (CustomCreativeTabRegistry.INSTANCE.isVanillaSearch(tab))
            return tab.hasSearchBar();
        return CustomCreativeTabRegistry.INSTANCE.shouldHandleSearch(tab);
    }

    @Inject(method = "selectTab", at = @At("RETURN"))
    private void updateSearchBox(CreativeModeTab tab, CallbackInfo ci) {
        CreativeModeTab activeTab = selectedTab != null ? selectedTab : tab;
        if (searchBox == null)
            return;

        if (activeTab != null && CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(activeTab)) {
            hideSearchBox();
            return;
        }

        if (activeTab == null || !CustomCreativeTabRegistry.INSTANCE.shouldHandleSearch(activeTab))
            return;

        searchBox.setValue("");
        searchBox.setResponder(value -> refreshSearchResults());
        searchBox.setVisible(true);
        searchBox.setCanLoseFocus(false);
        ((Screen) (Object) this).setFocused(searchBox);
        refreshSearchResults();
    }

    private void hideSearchBox() {
        searchBox.setValue("");
        searchBox.setVisible(false);
        searchBox.setCanLoseFocus(true);
        ((Screen) (Object) this).setFocused(null);
    }
}
