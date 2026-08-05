package me.hypherionmc.morecreativetabs.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.impl.client.item.group.CreativeGuiExtensions;
import net.fabricmc.fabric.impl.client.item.group.FabricCreativeGuiComponents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
@SuppressWarnings("unchecked")
public abstract class FabricCreativeInventoryScreenMixin extends AbstractContainerScreen {

    @Shadow
    private static int selectedTab;

    private boolean refreshingOnInit;

    @Inject(method = "selectTab", at = @At("HEAD"), cancellable = true)
    private void skipSelectedTab(CreativeModeTab tab, CallbackInfo ci) {
        if (!refreshingOnInit && tab.getId() == selectedTab)
            ci.cancel();
    }

    public FabricCreativeInventoryScreenMixin(AbstractContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void beginInventoryInit(CallbackInfo ci) {
        refreshingOnInit = true;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void finishInventoryInit(CallbackInfo ci) {
        refreshingOnInit = false;
    }

    /**
     * Mixin here to return the correct Tab Column, Row and Alignment
     */
    @Inject(method = "renderTabButton", at = @At(value = "HEAD"), cancellable = true)
    private void injectRenderTabButton(PoseStack poseStack, CreativeModeTab tab, CallbackInfo ci) {
        ci.cancel();
        if (!isGroupVisibleFabric(tab)) {
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

        RenderSystem.enableBlend(); //Forge: Make sure blend is enabled else tabs show a white border.
        this.blit(poseStack, x, y, textureX, textureY, 28, 32);
        this.itemRenderer.blitOffset = 100.0F;
        x += 6;
        y += 8 + (topRow ? 1 : -1);
        ItemStack icon = tab.getIconItem();
        this.itemRenderer.renderAndDecorateItem(icon, x, y);
        this.itemRenderer.renderGuiItemDecorations(this.font, icon, x, y);
        this.itemRenderer.blitOffset = 0.0F;
    }

    /**
     * Mixin here to return the correct Tab Column, Row and Alignment
     */
    @Inject(method = "checkTabClicked", at = @At(value = "HEAD"), cancellable = true)
    private void injectCheckTabClicked(CreativeModeTab tab, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (!isGroupVisibleFabric(tab)) {
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

    /**
     * Mixin here to return the correct Tab Column, Row and Alignment
     */
    @Inject(method = "checkTabHovering", at = @At(value = "HEAD"), cancellable = true)
    private void injectCheckTabHovering(PoseStack poseStack, CreativeModeTab tab, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (!isGroupVisibleFabric(tab)) {
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

    // Extracted from CreativeModeTab
    private boolean isAlignedRight(CreativeModeTab tab) {
        if (tab == CreativeModeTab.TAB_INVENTORY)
            return true;

        CreativeGuiExtensions extensions = ((CreativeGuiExtensions) this);

        if (extensions.fabric_currentPage() == 0) {
            return tab.getId() == 4 || tab.getId() == 5 || tab.getId() == 11;
        }
        return tab == CreativeModeTab.TAB_SEARCH || tab == CreativeModeTab.TAB_INVENTORY;
    }

    public int getColumn(CreativeModeTab tab) {
        int id = tab.getId();
        CreativeGuiExtensions extensions = ((CreativeGuiExtensions) this);

        if (tab == CreativeModeTab.TAB_INVENTORY) {
            id = 11;
        } else if (extensions.fabric_currentPage() == 1) {
            if (tab == CreativeModeTab.TAB_SEARCH)
                id = 5;
        }

        if (id > 11) return ((id - 12) % 10) % 5;
        return id % 6;
    }

    public boolean isTopRow(CreativeModeTab tab) {
        int id = tab.getId();
        CreativeGuiExtensions extensions = ((CreativeGuiExtensions) this);

        if (tab == CreativeModeTab.TAB_INVENTORY) {
            id = 11;
        } else if (extensions.fabric_currentPage() == 1) {
            if (tab == CreativeModeTab.TAB_SEARCH)
                id = 5;
        }

        if (id > 11) return ((id - 12) % 10) < 5;
        return id < 6;
    }

    private boolean isGroupVisibleFabric(CreativeModeTab itemGroup) {
        if (FabricCreativeGuiComponents.COMMON_GROUPS.contains(itemGroup)) {
            return true;
        }

        CreativeGuiExtensions extensions = ((CreativeGuiExtensions) this);
        return extensions.fabric_currentPage() == pageOffsetFabric(itemGroup.getId());
    }

    private int pageOffsetFabric(int offset) {
        if (offset < 12) {
            return 0;
        } else {
            return 1 + ((offset - 12) / (12 - FabricCreativeGuiComponents.COMMON_GROUPS.size()));
        }
    }
}
