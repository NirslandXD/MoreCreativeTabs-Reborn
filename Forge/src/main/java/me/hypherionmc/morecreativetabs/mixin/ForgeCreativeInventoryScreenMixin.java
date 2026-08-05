package me.hypherionmc.morecreativetabs.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(CreativeModeInventoryScreen.class)
@SuppressWarnings("unchecked")
public abstract class ForgeCreativeInventoryScreenMixin extends AbstractContainerScreen {

    @Shadow private static int selectedTab;

    @Shadow(remap = false) private static int tabPage;

    private boolean refreshingOnInit;

    @Inject(method = "selectTab", at = @At("HEAD"), cancellable = true)
    private void skipSelectedTab(CreativeModeTab tab, CallbackInfo ci) {
        if (!refreshingOnInit && tab.getId() == selectedTab)
            ci.cancel();
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void validateSelectedTab(CallbackInfo ci) {
        refreshingOnInit = true;
        CreativeModeTab activeTab = selectedTab >= 0 && selectedTab < CreativeModeTab.TABS.length
                ? CreativeModeTab.TABS[selectedTab]
                : null;
        if (activeTab == null || CustomCreativeTabManager.isDisabled(activeTab)) {
            activeTab = CustomCreativeTabManager.orderedVisibleTabs().stream()
                    .filter(tab -> tab != null && !CustomCreativeTabManager.isDisabled(tab))
                    .findFirst()
                    .orElseGet(() -> Arrays.stream(CreativeModeTab.TABS)
                            .filter(tab -> tab != null && !CustomCreativeTabManager.isDisabled(tab))
                            .findFirst()
                            .orElse(null));
            selectedTab = activeTab == null ? 0 : activeTab.getId();
        }
        if (activeTab != null)
            tabPage = activeTab.getTabPage();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void finishInventoryInit(CallbackInfo ci) {
        refreshingOnInit = false;
    }

    @Redirect(method = "selectTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;hasSearchBar()Z", remap = false))
    private boolean useConfiguredSearchBar(CreativeModeTab tab) {
        if (CustomCreativeTabManager.isVanillaSearch(tab))
            return tab.hasSearchBar();
        return CustomCreativeTabManager.shouldHandleSearch(tab);
    }

    @Redirect(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getBackgroundImage()Lnet/minecraft/resources/ResourceLocation;", remap = false))
    private ResourceLocation useConfiguredBackground(CreativeModeTab tab) {
        ResourceLocation background = tab.getBackgroundImage();
        if (CustomCreativeTabManager.isSearchBarDisabled(tab) && background.getPath().endsWith("tab_item_search.png"))
            return new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tab_items.png");
        return background;
    }

    public ForgeCreativeInventoryScreenMixin(AbstractContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "renderTabButton", at = @At(value = "HEAD"), cancellable = true)
    private void injectRenderTabButton(PoseStack poseStack, CreativeModeTab tab, CallbackInfo ci) {
        ci.cancel();
        if (!isTabVisible(tab)) {
            return;
        }

        boolean selected = tab.getId() == selectedTab;
        boolean topRow = isTopRow(tab);
        int column = getColumn(tab);
        int textureX = column * 28;
        int textureY = 0;
        int x = this.leftPos + 28 * column;
        int y = this.topPos;
        if (selected) {
            textureY += 32;
        }

        if (isAlignedRight(tab)) {
            x = this.leftPos + this.imageWidth - 28 * (6 - column);
        } else if (column > 0) {
            x += column;
        }

        if (topRow) {
            y -= 28;
        } else {
            textureY += 64;
            y += this.imageHeight - 4;
        }

        RenderSystem.enableBlend();
        this.blit(poseStack, x, y, textureX, textureY, 28, 32);
        this.itemRenderer.blitOffset = 100.0F;
        x += 6;
        y += 8 + (topRow ? 1 : -1);
        ItemStack icon = tab.getIconItem();
        this.itemRenderer.renderAndDecorateItem(icon, x, y);
        this.itemRenderer.renderGuiItemDecorations(this.font, icon, x, y);
        this.itemRenderer.blitOffset = 0.0F;
    }

    @Inject(method = "checkTabClicked", at = @At(value = "HEAD"), cancellable = true)
    private void injectCheckTabClicked(CreativeModeTab tab, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (!isTabVisible(tab)) {
            cir.setReturnValue(false);
            return;
        }

        int column = getColumn(tab);
        int x = 28 * column;
        int y = 0;
        if (isAlignedRight(tab)) {
            x = this.imageWidth - 28 * (6 - column) + 2;
        } else if (column > 0) {
            x += column;
        }

        if (isTopRow(tab)) {
            y -= 32;
        } else {
            y += this.imageHeight;
        }

        cir.setReturnValue(mouseX >= x && mouseX <= x + 28 && mouseY >= y && mouseY <= y + 32);
    }

    @Inject(method = "checkTabHovering", at = @At(value = "HEAD"), cancellable = true)
    private void injectCheckTabHovering(PoseStack poseStack, CreativeModeTab tab, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (!isTabVisible(tab)) {
            cir.setReturnValue(false);
            return;
        }

        int column = getColumn(tab);
        int x = 28 * column;
        int y = 0;
        if (isAlignedRight(tab)) {
            x = this.imageWidth - 28 * (6 - column) + 2;
        } else if (column > 0) {
            x += column;
        }

        if (isTopRow(tab)) {
            y -= 32;
        } else {
            y += this.imageHeight;
        }

        if (this.isHovering(x + 3, y + 3, 23, 27, mouseX, mouseY)) {
            this.renderTooltip(poseStack, tab.getDisplayName(), mouseX, mouseY);
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }

    private boolean isAlignedRight(CreativeModeTab tab) {
        if (tab == CreativeModeTab.TAB_INVENTORY)
            return true;

        if (tabPage == 0) {
            return tab.getId() == 4 || tab.getId() == 5 || tab.getId() == 11;
        }
        return tab == CreativeModeTab.TAB_SEARCH || tab == CreativeModeTab.TAB_INVENTORY;
    }

    public int getColumn(CreativeModeTab tab) {
        int id = tab.getId();
        if (tab == CreativeModeTab.TAB_INVENTORY) {
            id = 11;
        } else if (tabPage != 0) {
            if (tab == CreativeModeTab.TAB_SEARCH)
                id = 5;
        }

        if (id > 11) return ((id - 12) % 10) % 5;
        return id % 6;
    }

    public boolean isTopRow(CreativeModeTab tab) {
        int id = tab.getId();
        if (tab == CreativeModeTab.TAB_INVENTORY) {
            id = 11;
        } else if (tabPage != 0) {
            if (tab == CreativeModeTab.TAB_SEARCH)
                id = 5;
        }

        if (id > 11) return ((id - 12) % 10) < 5;
        return id < 6;
    }

    private boolean isTabVisible(CreativeModeTab tab) {
        if (tab == null || CustomCreativeTabManager.isDisabled(tab))
            return false;
        return tab.getTabPage() == tabPage || tab == CreativeModeTab.TAB_SEARCH || tab == CreativeModeTab.TAB_INVENTORY;
    }
}
