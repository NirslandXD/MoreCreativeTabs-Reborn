package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.impl.FabricCreativeTabUtils;
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
public class FabricCreativeInventoryScreenMixin {

    @Shadow private static CreativeModeTab selectedTab;
    @Shadow private EditBox searchBox;

<<<<<<< HEAD
    @Inject(method = "init", at = @At("HEAD"))
    private void validateTabs(CallbackInfo ci) {
=======
<<<<<<< HEAD
    @Inject(method = "init", at = @At("HEAD"))
    private void validateTabs(CallbackInfo ci) {
=======
    private boolean refreshingOnInit;

    @Inject(method = "selectTab", at = @At("HEAD"), cancellable = true)
    private void skipSelectedTab(CreativeModeTab tab, CallbackInfo ci) {
        if (!refreshingOnInit && tab == selectedTab)
            ci.cancel();
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void validateTabs(CallbackInfo ci) {
        refreshingOnInit = true;
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
        List<CreativeModeTab> displayedTabs = CustomCreativeTabRegistry.INSTANCE.displayedTabs();
        if (!displayedTabs.contains(selectedTab) && !displayedTabs.isEmpty())
            selectedTab = displayedTabs.get(0);

        FabricCreativeTabUtils.validateTabs(displayedTabs);
    }

<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 79b2a8a (initial commit)
    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getBackgroundSuffix()Ljava/lang/String;"))
    private String useSearchBackground(CreativeModeTab tab) {
        ResourceLocation customBackground = CustomCreativeTabRegistry.INSTANCE.background(tab).orElse(null);
        if (customBackground != null)
            return "../../" + customBackground.getNamespace() + "/" + customBackground.getPath();

        String suffix = tab.getBackgroundSuffix();
        return CustomCreativeTabRegistry.INSTANCE.isSearchable(tab) && "items.png".equals(suffix)
<<<<<<< HEAD
=======
=======
    @Inject(method = "init", at = @At("TAIL"))
    private void finishInventoryInit(CallbackInfo ci) {
        refreshingOnInit = false;
    }

    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getBackgroundSuffix()Ljava/lang/String;"))
    private String useSearchBackground(CreativeModeTab tab) {
        String suffix = tab.getBackgroundSuffix();
        if (CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(tab) && "item_search.png".equals(suffix))
            return "items.png";
        return CustomCreativeTabRegistry.INSTANCE.shouldHandleSearch(tab) && "items.png".equals(suffix)
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
                ? "item_search.png"
                : suffix;
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

}
