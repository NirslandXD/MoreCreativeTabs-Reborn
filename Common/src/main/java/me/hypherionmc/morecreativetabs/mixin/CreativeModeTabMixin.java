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

    @Inject(method = "buildContents", at = @At("HEAD"), cancellable = true)
    private void injectBuildContents(CreativeModeTab.ItemDisplayParameters arg, CallbackInfo ci) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;

        if (CustomCreativeTabRegistry.INSTANCE.getCustomTabs().contains(self) && CustomCreativeTabRegistry.INSTANCE.getTabItems().containsKey(self)) {
            ci.cancel();

            displayItems.clear();
            displayItemsSearchTab.clear();
            List<ItemStack> stacks = CustomCreativeTabRegistry.INSTANCE.getTabItems().get(self);

<<<<<<< HEAD
            stacks.stream().filter(stack -> !isHidden(stack)).forEach(stack -> {
=======
<<<<<<< HEAD
            stacks.stream().filter(stack -> !isHidden(stack)).forEach(stack -> {
=======
            stacks.stream().filter(stack -> !isHidden(stack) && !isExistingSentinel(stack)).forEach(stack -> {
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
                displayItems.add(stack);
                displayItemsSearchTab.add(stack);
            });
            rebuildSearchTree();
        }
    }

    @Inject(method = "hasAnyItems", at = @At("RETURN"), cancellable = true)
    private void injectHasAnyItems(CallbackInfoReturnable<Boolean> cir) {
        CreativeModeTab self = (CreativeModeTab) ((Object) this);

        if (CustomCreativeTabRegistry.INSTANCE.getCustomTabs().contains(self) && CustomCreativeTabRegistry.INSTANCE.getTabItems().containsKey(self)) {
            cir.setReturnValue(true);
        }
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
        cir.setReturnValue(filterItems(cir.getReturnValue()));
    }

    @Inject(method = "getSearchTabDisplayItems", at = @At("RETURN"), cancellable = true)
    private void injectSearchItemsFilter(CallbackInfoReturnable<Collection<ItemStack>> cir) {
        cir.setReturnValue(filterItems(cir.getReturnValue()));
    }

    @Inject(method = "buildContents", at = @At("TAIL"))
    private void filterBuiltContents(CreativeModeTab.ItemDisplayParameters parameters, CallbackInfo ci) {
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
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 79b2a8a (initial commit)
            replacement.get().getRight().forEach(stack -> {
                displayItems.add(stack);
                displayItemsSearchTab.add(stack);
            });
<<<<<<< HEAD
=======
=======
            for (ItemStack stack : replacement.get().getRight()) {
                if (stack.getItem() == net.minecraft.world.item.Items.COMMAND_BLOCK && stack.hasTag() && stack.getTag().getBoolean("MCT_EXISTING")) {
                    if (keepExisting) {
                        oldDisplayItems.stream().filter(existingStack -> !isExcepted(replacement.get().getLeft(), existingStack)).forEach(displayItems::add);
                        oldSearchItems.stream().filter(existingStack -> !isExcepted(replacement.get().getLeft(), existingStack)).forEach(displayItemsSearchTab::add);
                        keepExisting = false;
                    }
                } else if (!isHidden(stack) && !isExistingSentinel(stack)) {
                    displayItems.add(stack);
                    displayItemsSearchTab.add(stack);
                }
            }
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
            if (keepExisting) {
                oldDisplayItems.stream().filter(stack -> !isExcepted(replacement.get().getLeft(), stack)).forEach(displayItems::add);
                oldSearchItems.stream().filter(stack -> !isExcepted(replacement.get().getLeft(), stack)).forEach(displayItemsSearchTab::add);
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
<<<<<<< HEAD
        inputStacks.stream().filter(stack -> !isHidden(stack)).forEach(filtered::add);
=======
<<<<<<< HEAD
        inputStacks.stream().filter(stack -> !isHidden(stack)).forEach(filtered::add);
=======
        inputStacks.stream().filter(stack -> !isHidden(stack) && !isExistingSentinel(stack)).forEach(filtered::add);
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
        return filtered;
    }

    @Unique
    private boolean isHidden(ItemStack stack) {
        return CustomCreativeTabRegistry.INSTANCE.isHidden(stack);
    }

    @Unique
<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
    private boolean isExistingSentinel(ItemStack stack) {
        return CustomCreativeTabRegistry.INSTANCE.isExistingSentinel(stack);
    }

    @Unique
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
    private boolean isExcepted(CustomCreativeTabJsonHelper json, ItemStack stack) {
        return json.getExistingExceptions().contains(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }

}
