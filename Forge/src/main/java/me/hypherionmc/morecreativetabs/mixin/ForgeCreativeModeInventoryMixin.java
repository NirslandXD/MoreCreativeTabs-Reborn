package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import net.minecraft.client.gui.components.EditBox;
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

<<<<<<< HEAD
    @Inject(method = "init", at = @At("HEAD"))
    private void validateSelectedTab(CallbackInfo ci) {
=======
<<<<<<< HEAD
    @Inject(method = "init", at = @At("HEAD"))
    private void validateSelectedTab(CallbackInfo ci) {
=======
    private boolean refreshingOnInit;

    @Inject(method = "selectTab", at = @At("HEAD"), cancellable = true)
    private void skipSelectedTab(CreativeModeTab tab, CallbackInfo ci) {
        if (!refreshingOnInit && tab == selectedTab)
            ci.cancel();
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void validateSelectedTab(CallbackInfo ci) {
        refreshingOnInit = true;
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
        List<CreativeModeTab> displayedTabs = CustomCreativeTabRegistry.INSTANCE.displayedTabs();
        if (!displayedTabs.contains(selectedTab) && !displayedTabs.isEmpty())
            selectedTab = displayedTabs.get(0);
    }

<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 79b2a8a (initial commit)
    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getBackgroundLocation()Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation useSearchBackground(CreativeModeTab tab) {
        ResourceLocation customBackground = CustomCreativeTabRegistry.INSTANCE.background(tab).orElse(null);
        if (customBackground != null)
            return customBackground;

        ResourceLocation background = tab.getBackgroundLocation();
        ResourceLocation normalBackground = ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/items.png");
        return CustomCreativeTabRegistry.INSTANCE.isSearchable(tab) && normalBackground.equals(background)
                ? ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/item_search.png")
                : background;
<<<<<<< HEAD
=======
=======
    @Inject(method = "init", at = @At("TAIL"))
    private void finishInventoryInit(CallbackInfo ci) {
        refreshingOnInit = false;
    }

    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getBackgroundLocation()Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation useSearchBackground(CreativeModeTab tab) {
        if (CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(tab))
            return ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tab_items.png");
        if (CustomCreativeTabRegistry.INSTANCE.shouldHandleSearch(tab))
            return ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tab_item_search.png");

        return tab.getBackgroundLocation();
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void updateSearchBox(CreativeModeTab tab, CallbackInfo ci) {
<<<<<<< HEAD
        if (!CustomCreativeTabRegistry.INSTANCE.isSearchable(selectedTab) || selectedTab.getType() == CreativeModeTab.Type.SEARCH)
=======
<<<<<<< HEAD
        if (!CustomCreativeTabRegistry.INSTANCE.isSearchable(selectedTab) || selectedTab.getType() == CreativeModeTab.Type.SEARCH)
=======
        if (searchBox == null)
            return;

        if (selectedTab != null && CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(selectedTab)) {
            searchBox.setVisible(false);
            searchBox.setCanLoseFocus(true);
            return;
        }

        if (selectedTab == null || !CustomCreativeTabRegistry.INSTANCE.shouldHandleSearch(selectedTab))
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
            return;

        searchBox.setVisible(true);
        searchBox.setCanLoseFocus(false);
        searchBox.setFocused(true);
    }
<<<<<<< HEAD
=======
<<<<<<< HEAD
=======

>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
}
