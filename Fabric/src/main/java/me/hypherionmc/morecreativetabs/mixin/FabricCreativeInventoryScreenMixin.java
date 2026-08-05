package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.impl.FabricCreativeTabUtils;
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
public class FabricCreativeInventoryScreenMixin {

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
    private void validateTabs(CallbackInfo ci) {
        refreshingOnInit = true;
        List<CreativeModeTab> displayedTabs = CustomCreativeTabRegistry.INSTANCE.displayedTabs();
        if (!displayedTabs.contains(selectedTab) && !displayedTabs.isEmpty())
            selectedTab = displayedTabs.get(0);

        FabricCreativeTabUtils.validateTabs(displayedTabs);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void finishInventoryInit(CallbackInfo ci) {
        refreshingOnInit = false;
    }

    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getBackgroundSuffix()Ljava/lang/String;"))
    private String useSearchBackground(CreativeModeTab tab) {
        String suffix = tab.getBackgroundSuffix();
        if (CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(tab))
            return "items.png";
        return CustomCreativeTabRegistry.INSTANCE.shouldHandleSearch(tab) && "items.png".equals(suffix)
                ? "item_search.png"
                : suffix;
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void updateSearchBox(CreativeModeTab tab, CallbackInfo ci) {
        CreativeModeTab activeTab = selectedTab != null ? selectedTab : tab;
        if (searchBox == null)
            return;

        if (activeTab != null && CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(activeTab)) {
            searchBox.setVisible(false);
            searchBox.setCanLoseFocus(true);
            ((Screen) (Object) this).setFocused(null);
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
}
