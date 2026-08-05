package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabManager;
import me.hypherionmc.morecreativetabs.util.CreativeTabUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabMixin {

    @Shadow public abstract String getRecipeFolderName();

    @Inject(at = @At("RETURN"), method = "getDisplayName", cancellable = true)
    public void getDisplayName(CallbackInfoReturnable<Component> cir) {
        if (CustomCreativeTabManager.showNames) {
            cir.setReturnValue(new TextComponent(this.getRecipeFolderName()));
            return;
        }

        CreativeTabUtils.replacementTab(getRecipeFolderName()).ifPresent(tabData -> {
            if (tabData.getLeft().tabName != null && !tabData.getLeft().tabName.isBlank())
                cir.setReturnValue(mct$tabName(tabData.getLeft().tabName));
        });

        CreativeModeTab tab = (CreativeModeTab) (Object) this;
        if (CustomCreativeTabManager.customTabs.contains(tab)) {
            String internalName = getRecipeFolderName();
            String customPrefix = CreativeTabUtils.prefix("");
            String configuredName = internalName.startsWith(customPrefix)
                    ? internalName.substring(customPrefix.length())
                    : internalName;
            cir.setReturnValue(mct$tabName(configuredName));
        }
    }

    @Unique
    private Component mct$tabName(String configuredName) {
        return new TranslatableComponent(CreativeTabUtils.prefix(configuredName));
    }

    @Inject(at = @At("RETURN"), method = "getBackgroundSuffix", cancellable = true)
    private void mct$replacementBackground(CallbackInfoReturnable<String> cir) {
        CreativeTabUtils.replacementTab(getRecipeFolderName()).ifPresent(tabData -> {
            if (CustomCreativeTabManager.isSearchBarDisabled((CreativeModeTab) (Object) this)) {
                String background = tabData.getLeft().tabBackground;
                cir.setReturnValue(background != null && !background.isBlank() && !background.equals("item_search.png")
                        ? background : "items.png");
            } else if (Boolean.TRUE.equals(tabData.getLeft().searchBar) && !CreativeTabUtils.selectorAliases(getRecipeFolderName()).contains("hotbar")) {
                cir.setReturnValue("item_search.png");
            } else if (tabData.getLeft().tabBackground != null && !tabData.getLeft().tabBackground.isBlank()) {
                cir.setReturnValue(tabData.getLeft().tabBackground);
            }
        });
    }

    @Inject(at = @At("RETURN"), method = "getIconItem", cancellable = true)
    public void injectIcon(CallbackInfoReturnable<ItemStack> cir) {
        CreativeTabUtils.replacementTab(getRecipeFolderName()).ifPresent(tabData -> {
            ItemStack stack = CreativeTabUtils.makeTabIcon(tabData.getLeft());
            if (!stack.isEmpty())
                cir.setReturnValue(stack);
        });
    }

    @Inject(method = "fillItemList", at = @At("HEAD"), cancellable = true)
    public void injectItems(NonNullList<ItemStack> stacks, CallbackInfo ci) {
        CreativeModeTab tab = (CreativeModeTab) (Object)this;
        // The Hotbar tab owns a special saved-hotbar list; do not replace it with category scanning.
        if (tab == CreativeModeTab.TAB_HOTBAR)
            return;
        if (!CustomCreativeTabManager.customTabs.contains(tab)) {
            ci.cancel();
            CreativeTabUtils.replacementTab(tab.getRecipeFolderName()).ifPresentOrElse(tabData -> {
                NonNullList<ItemStack> existing = NonNullList.create();
                fillTabItems(tab, existing, tabData.getLeft());
                addReplacementItems(stacks, existing, tabData.getLeft(), tabData.getRight());
            }, () -> fillTabItems(tab, stacks, null));
        }
    }

    @Unique
    private void addReplacementItems(NonNullList<ItemStack> output, List<ItemStack> existing,
                                     me.hypherionmc.morecreativetabs.client.data.jsonhelpers.CustomCreativeTab replacement,
                                     List<ItemStack> configured) {
        boolean keepExisting = replacement.keepExisting || configured.isEmpty();
        boolean existingAdded = false;
        for (ItemStack stack : configured) {
            if (CustomCreativeTabManager.isExistingSentinel(stack)) {
                if (keepExisting && !existingAdded) {
                    output.addAll(existing);
                    existingAdded = true;
                }
            } else {
                output.add(stack);
            }
        }
        if (keepExisting && !existingAdded)
            output.addAll(existing);
    }

    private void fillTabItems(CreativeModeTab tab, NonNullList<ItemStack> existing, me.hypherionmc.morecreativetabs.client.data.jsonhelpers.CustomCreativeTab replacement) {
        for (Item item : Registry.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (replacement != null && !CustomCreativeTabManager.shouldKeepExistingItem(replacement, stack))
                continue;

            if (!CustomCreativeTabManager.customTabs.contains(tab)) {
                if (!CustomCreativeTabManager.isDisabled(tab) && !CustomCreativeTabManager.hiddenStacks.contains(item))
                    item.fillItemCategory(tab, existing);
            } else {
                item.fillItemCategory(tab, existing);
            }
        }
    }
}
