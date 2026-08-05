package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import me.hypherionmc.morecreativetabs.mixin.accessor.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventorySearchMixin {

    @Shadow private static CreativeModeTab selectedTab;
    @Shadow private EditBox searchBox;
    @Shadow @Final private Set<TagKey<Item>> visibleTags;
    @Shadow private float scrollOffs;

    @Inject(method = "init", at = @At("TAIL"))
    private void enableInitialCustomSearch(CallbackInfo ci) {
        if (!isCustomSearchTab(selectedTab))
            return;

        activateCustomSearchBox(true);
        refreshCustomSearch();
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void enableSearchForCustomTab(CreativeModeTab tab, CallbackInfo ci) {
        CreativeModeTab activeTab = selectedTab != null ? selectedTab : tab;
        if (!isCustomSearchTab(activeTab))
            return;

        activateCustomSearchBox(true);
        refreshCustomSearch();
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void typeInCustomTab(char character, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (CustomCreativeTabRegistry.INSTANCE.isVanillaSearch(selectedTab))
            return;

        if (CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(selectedTab)) {
            cir.setReturnValue(false);
            return;
        }

        if (!isCustomSearch())
            return;

        activateCustomSearchBox(false);
        String oldValue = searchBox.getValue();
        if (searchBox.charTyped(character, modifiers)) {
            if (!Objects.equals(oldValue, searchBox.getValue()))
                refreshCustomSearch();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void keyInCustomTab(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (CustomCreativeTabRegistry.INSTANCE.isVanillaSearch(selectedTab))
            return;

        if (CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(selectedTab))
            return;

        if (!isCustomSearch())
            return;

        activateCustomSearchBox(false);
        String oldValue = searchBox.getValue();
        if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(oldValue, searchBox.getValue()))
                refreshCustomSearch();
            cir.setReturnValue(true);
            return;
        }

        if (searchBox.canConsumeInput() && keyCode != 256)
            cir.setReturnValue(true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void clickCustomSearchBox(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!isCustomSearch())
            return;

        activateCustomSearchBox(false);
        if (searchBox.mouseClicked(mouseX, mouseY, button)) {
            ((Screen) (Object) this).setFocused(searchBox);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "refreshSearchResults", at = @At("HEAD"), cancellable = true)
    private void refreshCustomTabSearch(CallbackInfo ci) {
        if (CustomCreativeTabRegistry.INSTANCE.isVanillaSearch(selectedTab))
            return;

        if (CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(selectedTab))
            return;


        if (!isCustomSearch())
            return;

        ci.cancel();
        refreshCustomSearch();
    }

    private boolean isCustomSearch() {
        return searchBox != null && CustomCreativeTabRegistry.INSTANCE.shouldHandleSearch(selectedTab);
    }

    @Unique
    private boolean isCustomSearchTab(CreativeModeTab tab) {
        return CustomCreativeTabRegistry.INSTANCE.shouldHandleSearch(tab);
    }

    @Unique
    private void activateCustomSearchBox(boolean clear) {
        if (searchBox == null)
            return;

        if (clear)
            searchBox.setValue("");
        searchBox.setResponder(value -> {
            if (isCustomSearch())
                refreshCustomSearch();
        });
        searchBox.setVisible(true);
        searchBox.setCanLoseFocus(false);
        ((Screen) (Object) this).setFocused(searchBox);
    }

    private void refreshCustomSearch() {
        CreativeModeInventoryScreen.ItemPickerMenu menu = (CreativeModeInventoryScreen.ItemPickerMenu) ((AbstractContainerScreenAccessor) this).getInternalMenu();
        menu.items.clear();
        visibleTags.clear();
        String query = searchBox.getValue().toLowerCase(Locale.ROOT).trim();
        boolean tagSearch = query.startsWith("#");
        if (tagSearch)
            query = query.substring(1);

        for (ItemStack stack : selectedTab.getSearchTabDisplayItems()) {
            if (query.isEmpty() || matches(stack, query, tagSearch))
                menu.items.add(stack);
        }

        scrollOffs = 0.0F;
        menu.scrollTo(0.0F);
    }

    private boolean matches(ItemStack stack, String query, boolean tagSearch) {
        if (tagSearch) {
            return stack.getTags().anyMatch(tag -> {
                boolean matches = tag.location().toString().toLowerCase(Locale.ROOT).contains(query);
                if (matches)
                    visibleTags.add(tag);
                return matches;
            });
        }

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase(Locale.ROOT);
        if (itemId.contains(query))
            return true;

        Minecraft minecraft = Minecraft.getInstance();
        TooltipFlag flag = minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        return stack.getTooltipLines(minecraft.player, flag).stream()
                .anyMatch(line -> line.getString().toLowerCase(Locale.ROOT).contains(query));
    }
}
