package me.hypherionmc.morecreativetabs.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeScreen.class)
@SuppressWarnings("unchecked")
public abstract class ForgeCreativeInventoryScreenMixin extends ContainerScreen {

    @Shadow private static int selectedTabIndex;

    @Shadow(remap = false) private static int tabPage;

    @Shadow protected abstract boolean isMouseOverGroup(ItemGroup tab, double mouseX, double mouseY);

    private boolean refreshingOnInit;

    @Inject(method = "setCurrentCreativeTab", at = @At("HEAD"), cancellable = true)
    private void skipSelectedTab(ItemGroup tab, CallbackInfo ci) {
        if (!refreshingOnInit && tab.getIndex() == selectedTabIndex)
            ci.cancel();
    }

    public ForgeCreativeInventoryScreenMixin(Container menu, PlayerInventory inventory, ITextComponent title) {
        super(menu, inventory, title);
    }

    @Redirect(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;getBackgroundImage()Lnet/minecraft/util/ResourceLocation;", remap = false))
    private ResourceLocation useConfiguredBackground(ItemGroup tab) {
        ResourceLocation background = tab.getBackgroundImage();
        if (CustomCreativeTabManager.isSearchBarDisabled(tab) && background.getPath().endsWith("tab_item_search.png"))
            return new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tab_items.png");
        return background;
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void validateSelectedTab(CallbackInfo ci) {
        refreshingOnInit = true;
        int reloadedTab = CustomCreativeTabManager.consumeSelectedTabAfterReload();
        if (reloadedTab >= 0)
            selectedTabIndex = reloadedTab;
        if (!isSelectableTab(selectedTabIndex))
            selectedTabIndex = CustomCreativeTabManager.orderedVisibleTabs().stream()
                    .filter(tab -> tab != null && !CustomCreativeTabManager.isDisabled(tab))
                    .mapToInt(ItemGroup::getIndex)
                    .filter(this::isSelectableTab)
                    .findFirst()
                    .orElse(0);

        tabPage = isSelectableTab(selectedTabIndex) ? ItemGroup.GROUPS[selectedTabIndex].getTabPage() : 0;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void finishInventoryInit(CallbackInfo ci) {
        refreshingOnInit = false;
    }

    @Inject(method = "func_238808_a_", at = @At(value = "HEAD"), cancellable = true)
    private void injectRenderTabButton(MatrixStack poseStack, ItemGroup tab, CallbackInfo ci) {
        ci.cancel();
        if (!isVisibleOnCurrentPage(tab)) {
            return;
        }

        boolean selected = tab.getIndex() == selectedTabIndex;
        boolean topRow = isTopRow(tab);
        int column = getColumn(tab);
        int textureX = column * 28;
        int textureY = 0;
        int x = this.guiLeft + 28 * column;
        int y = this.guiTop;
        if (selected) {
            textureY += 32;
        }

        if (isAlignedRight(tab)) {
            x = this.guiLeft + this.xSize - 28 * (6 - column);
        } else if (column > 0) {
            x += column;
        }

        if (topRow) {
            y -= 28;
        } else {
            textureY += 64;
            y += this.ySize - 4;
        }

        RenderSystem.enableBlend();
        this.blit(poseStack, x, y, textureX, textureY, 28, 32);
        this.itemRenderer.zLevel = 100.0F;
        x += 6;
        y += 8 + (topRow ? 1 : -1);
        ItemStack icon = tab.getIcon();
        this.itemRenderer.renderItemAndEffectIntoGUI(icon, x, y);
        this.itemRenderer.renderItemOverlays(this.font, icon, x, y);
        this.itemRenderer.zLevel = 0.0F;
    }

    @Inject(method = "isMouseOverGroup", at = @At(value = "HEAD"), cancellable = true)
    private void injectCheckTabClicked(ItemGroup tab, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (!isVisibleOnCurrentPage(tab)) {
            cir.setReturnValue(false);
            return;
        }

        int i = getColumn(tab);
        int j = 28 * i;
        int k = 0;
        if (isAlignedRight(tab)) {
            j = this.xSize - 28 * (6 - i) + 2;
        } else if (i > 0) {
            j += i;
        }

        if (isTopRow(tab)) {
            k -= 32;
        } else {
            k += this.ySize;
        }

        cir.setReturnValue(mouseX >= (double)j && mouseX <= (double)(j + 28) && mouseY >= (double)k && mouseY <= (double)(k + 32));
    }

    @Inject(method = "func_238809_a_", at = @At(value = "HEAD"), cancellable = true)
    private void injectCheckTabHovering(MatrixStack stack, ItemGroup tab, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (!isVisibleOnCurrentPage(tab)) {
            cir.setReturnValue(false);
            return;
        }

        int i = getColumn(tab);
        int j = 28 * i;
        int k = 0;
        if (isAlignedRight(tab)) {
            j = this.xSize - 28 * (6 - i) + 2;
        } else if (i > 0) {
            j += i;
        }

        if (isTopRow(tab)) {
            k -= 32;
        } else {
            k += this.ySize;
        }

        if (this.isPointInRegion(j + 3, k + 3, 23, 27, (double)mouseX, (double)mouseY)) {
            this.renderTooltip(stack, tab.getGroupName(), mouseX, mouseY);
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }

    private boolean isSelectableTab(int tabId) {
        if (tabId < 0 || tabId >= ItemGroup.GROUPS.length)
            return false;
        ItemGroup tab = ItemGroup.GROUPS[tabId];
        return tab != null && !CustomCreativeTabManager.isDisabled(tab)
                && CustomCreativeTabManager.orderedVisibleTabs().contains(tab);
    }

    private boolean isVisibleOnCurrentPage(ItemGroup tab) {
        return tab != null && !CustomCreativeTabManager.isDisabled(tab)
                && CustomCreativeTabManager.orderedVisibleTabs().contains(tab)
                && (tab.getTabPage() == tabPage || tab == ItemGroup.SEARCH || tab == ItemGroup.INVENTORY);
    }

    private boolean isAlignedRight(ItemGroup tab) {
        if (tab == ItemGroup.INVENTORY)
            return true;

        if (tabPage == 0) {
            return tab.getIndex() == 4 || tab.getIndex() == 5 || tab.getIndex() == 11;
        }
        return tab == ItemGroup.SEARCH || tab == ItemGroup.INVENTORY;
    }

    public int getColumn(ItemGroup tab) {
        int id = tab.getIndex();
        if (tab == ItemGroup.INVENTORY) {
            id = 11;
        } else if (tabPage != 0) {
            if (tab == ItemGroup.SEARCH)
                id = 5;
        }

        if (id > 11) return ((id - 12) % 10) % 5;
        return id % 6;
    }

    public boolean isTopRow(ItemGroup tab) {
        int id = tab.getIndex();
        if (tab == ItemGroup.INVENTORY) {
            id = 11;
        } else if (tabPage != 0) {
            if (tab == ItemGroup.SEARCH)
                id = 5;
        }

        if (id > 11) return ((id - 12) % 10) < 5;
        return id < 6;
    }
}
