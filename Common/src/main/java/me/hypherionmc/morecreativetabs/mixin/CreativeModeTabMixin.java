package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.data.CustomCreativeTabJsonHelper;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import me.hypherionmc.morecreativetabs.utils.CreativeTabUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

import static me.hypherionmc.morecreativetabs.utils.CreativeTabUtils.getTabKey;

@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabMixin {

    @Shadow private Collection<ItemStack> displayItems;
    @Shadow private Set<ItemStack> displayItemsSearchTab;
    @Shadow public abstract void rebuildSearchTree();
    @Shadow @Final private Component displayName;

    @Group(name = "buildContentsHead", min = 1, max = 1)
    @Inject(method = {
            "buildContents(Lnet/minecraft/world/flag/FeatureFlagSet;Z)V",
            "m_258021_(Lnet/minecraft/world/flag/FeatureFlagSet;Z)V"
    }, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void injectBuildContents1193(CallbackInfo ci) {
        injectBuildContents(ci);
    }

    @Group(name = "buildContentsHead", min = 1, max = 1)
    @Inject(method = {
            "buildContents(Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;)V",
            "m_269498_(Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;)V",
            "method_47306(Lnet/minecraft/class_1761$class_8128;)V"
    }, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void injectBuildContents1194(CallbackInfo ci) {
        injectBuildContents(ci);
    }

    @Unique
    private void injectBuildContents(CallbackInfo ci) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;

        List<ItemStack> customItems = CustomCreativeTabRegistry.INSTANCE.getTabItems().get(self);
        if (CustomCreativeTabRegistry.INSTANCE.getCustomTabs().contains(self) && customItems != null) {
            ci.cancel();

            displayItems.clear();
            displayItemsSearchTab.clear();
            customItems.stream().filter(stack -> !isHidden(stack) && !isExistingSentinel(stack)).forEach(stack -> {
                displayItems.add(stack);
                displayItemsSearchTab.add(stack);
            });
            rebuildSearchTree();
        }
    }

    @Inject(method = "hasAnyItems", at = @At("RETURN"), cancellable = true)
    private void injectHasAnyItems(CallbackInfoReturnable<Boolean> cir) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);

        if (CustomCreativeTabRegistry.INSTANCE.getCustomTabs().contains(self) && CustomCreativeTabRegistry.INSTANCE.getTabItems().get(self) != null)
            cir.setReturnValue(true);
    }

    @Inject(method = "shouldDisplay", at = @At("HEAD"), cancellable = true)
    private void injectShouldDisplay(CallbackInfoReturnable<Boolean> cir) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;

        if (CustomCreativeTabRegistry.INSTANCE.getCustomTabs().contains(self) && CustomCreativeTabRegistry.INSTANCE.getTabItems().get(self) != null)
            cir.setReturnValue(true);
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void injectDisplayName(CallbackInfoReturnable<Component> cir) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        if (CustomCreativeTabRegistry.INSTANCE.isShowTabNames()) {
            cir.setReturnValue(Component.literal(getTabKey(this.displayName)));
            return;
        }

        CustomCreativeTabRegistry.INSTANCE.replacementTab(self).ifPresent(tabData -> {
            if (tabData.getLeft().getTabName() != null && !tabData.getLeft().getTabName().isBlank())
                cir.setReturnValue(Component.translatable(CreativeTabUtils.prefix(tabData.getLeft().getTabName())));
        });
    }

    @Inject(method = "getDisplayItems", at = @At("RETURN"), cancellable = true)
    private void injectDisplayItemsFilter(CallbackInfoReturnable<Collection<ItemStack>> cir) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        List<ItemStack> customItems = CustomCreativeTabRegistry.INSTANCE.getTabItems().get(self);
        if (CustomCreativeTabRegistry.INSTANCE.getCustomTabs().contains(self) && customItems != null) {
            cir.setReturnValue(customItems.stream().filter(stack -> !isHidden(stack) && !isExistingSentinel(stack)).toList());
            return;
        }

        cir.setReturnValue(filterItems(cir.getReturnValue()));
    }

    @Inject(method = "getSearchTabDisplayItems", at = @At("RETURN"), cancellable = true)
    private void injectSearchItemsFilter(CallbackInfoReturnable<Collection<ItemStack>> cir) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        List<ItemStack> customItems = CustomCreativeTabRegistry.INSTANCE.getTabItems().get(self);
        if (CustomCreativeTabRegistry.INSTANCE.getCustomTabs().contains(self) && customItems != null) {
            cir.setReturnValue(customItems.stream().filter(stack -> !isHidden(stack) && !isExistingSentinel(stack)).toList());
            return;
        }

        cir.setReturnValue(filterItems(cir.getReturnValue()));
    }

    @Inject(method = "contains", at = @At("RETURN"), cancellable = true)
    private void injectContains(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;

        List<ItemStack> customItems = CustomCreativeTabRegistry.INSTANCE.getTabItems().get(self);
        if (CustomCreativeTabRegistry.INSTANCE.getCustomTabs().contains(self) && customItems != null) {
            cir.setReturnValue(customItems.contains(stack) && !isExistingSentinel(stack));
            return;
        }

        if (isHidden(stack))
            cir.setReturnValue(false);
    }

    @Group(name = "buildContentsTail", min = 1, max = 1)
    @Inject(method = {
            "buildContents(Lnet/minecraft/world/flag/FeatureFlagSet;Z)V",
            "m_258021_(Lnet/minecraft/world/flag/FeatureFlagSet;Z)V"
    }, at = @At("TAIL"), remap = false, require = 0)
    private void filterBuiltContents1193(CallbackInfo ci) {
        filterBuiltContents();
    }

    @Group(name = "buildContentsTail", min = 1, max = 1)
    @Inject(method = {
            "buildContents(Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;)V",
            "m_269498_(Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;)V",
            "method_47306(Lnet/minecraft/class_1761$class_8128;)V"
    }, at = @At("TAIL"), remap = false, require = 0)
    private void filterBuiltContents1194(CallbackInfo ci) {
        filterBuiltContents();
    }

    @Unique
    private void filterBuiltContents() {
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        if (CustomCreativeTabRegistry.INSTANCE.getCustomTabs().contains(self))
            return;

        Optional<Pair<CustomCreativeTabJsonHelper, List<ItemStack>>> replacement = CustomCreativeTabRegistry.INSTANCE.replacementTab(self);
        if (replacement.isPresent()) {
            Collection<ItemStack> oldDisplayItems = new ArrayList<>(displayItems);
            Collection<ItemStack> oldSearchItems = new ArrayList<>(displayItemsSearchTab);
            boolean keepExisting = replacement.get().getLeft().isKeepExisting() || replacement.get().getRight().isEmpty();

            displayItems.clear();
            displayItemsSearchTab.clear();
            for (ItemStack stack : replacement.get().getRight()) {
                if (isExistingSentinel(stack)) {
                    if (keepExisting) {
                        addExistingItems(replacement.get().getLeft(), oldDisplayItems, displayItems);
                        addExistingItems(replacement.get().getLeft(), oldSearchItems, displayItemsSearchTab);
                        keepExisting = false;
                    }
                } else if (!isHidden(stack)) {
                    displayItems.add(stack);
                    displayItemsSearchTab.add(stack);
                }
            }
            if (keepExisting) {
                addExistingItems(replacement.get().getLeft(), oldDisplayItems, displayItems);
                addExistingItems(replacement.get().getLeft(), oldSearchItems, displayItemsSearchTab);
            }
            rebuildSearchTree();
            return;
        }

        boolean displayChanged = displayItems.removeIf(this::isHidden);
        boolean searchChanged = displayItemsSearchTab.removeIf(this::isHidden);
        if (displayChanged || searchChanged)
            rebuildSearchTree();
    }

    @Inject(method = "getIconItem", at = @At("RETURN"), cancellable = true)
    private void injectIcon(CallbackInfoReturnable<ItemStack> cir) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        CustomCreativeTabRegistry.INSTANCE.replacementTab(self).ifPresent(tabData -> {
            ItemStack stack = CreativeTabUtils.makeTabIcon(tabData.getLeft()).get();
            if (!stack.isEmpty()) {
                cir.setReturnValue(stack);
            }
        });
    }

    @Unique
    private Collection<ItemStack> filterItems(Collection<ItemStack> inputStacks) {
        Set<ItemStack> filtered = ItemStackLinkedSet.createTypeAndTagSet();
        inputStacks.stream().filter(stack -> !isHidden(stack)).forEach(filtered::add);
        return filtered;
    }

    @Unique
    private boolean isHidden(ItemStack stack) {
        return CustomCreativeTabRegistry.INSTANCE.isHidden(stack);
    }

    @Unique
    private void addExistingItems(CustomCreativeTabJsonHelper json, Collection<ItemStack> existingItems, Collection<ItemStack> output) {
        existingItems.stream().filter(stack -> CustomCreativeTabRegistry.INSTANCE.shouldKeepExistingItem(json, stack)).forEach(output::add);
    }

    @Unique
    private boolean isExistingSentinel(ItemStack stack) {
        return CustomCreativeTabRegistry.INSTANCE.isExistingSentinel(stack);
    }

}
