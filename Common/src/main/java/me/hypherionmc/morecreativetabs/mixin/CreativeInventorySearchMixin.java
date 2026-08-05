package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import me.hypherionmc.morecreativetabs.mixin.accessor.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
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

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void typeInCustomTab(char character, int modifiers, CallbackInfoReturnable<Boolean> cir) {
<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
        if (CustomCreativeTabRegistry.INSTANCE.isVanillaSearch(selectedTab))
            return;

        if (CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(selectedTab)) {
            cir.setReturnValue(false);
            return;
        }

>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
        if (!isCustomSearch())
            return;

        String oldValue = searchBox.getValue();
        if (searchBox.charTyped(character, modifiers)) {
            if (!Objects.equals(oldValue, searchBox.getValue()))
                refreshCustomSearch();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void keyInCustomTab(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
        if (CustomCreativeTabRegistry.INSTANCE.isVanillaSearch(selectedTab))
            return;

        if (CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(selectedTab))
            return;

>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
        if (!isCustomSearch())
            return;

        String oldValue = searchBox.getValue();
        if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(oldValue, searchBox.getValue()))
                refreshCustomSearch();
            cir.setReturnValue(true);
            return;
        }

        if (searchBox.isFocused() && searchBox.isVisible() && keyCode != 256)
            cir.setReturnValue(true);
    }

    @Inject(method = "refreshSearchResults", at = @At("HEAD"), cancellable = true)
    private void refreshCustomTabSearch(CallbackInfo ci) {
<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
        if (CustomCreativeTabRegistry.INSTANCE.isVanillaSearch(selectedTab))
            return;

        if (CustomCreativeTabRegistry.INSTANCE.isSearchBarDisabled(selectedTab))
            return;


>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
        if (!isCustomSearch())
            return;

        ci.cancel();
        refreshCustomSearch();
    }

    private boolean isCustomSearch() {
<<<<<<< HEAD
        return selectedTab != null && selectedTab.getType() != CreativeModeTab.Type.SEARCH && CustomCreativeTabRegistry.INSTANCE.isSearchable(selectedTab);
=======
<<<<<<< HEAD
        return selectedTab != null && selectedTab.getType() != CreativeModeTab.Type.SEARCH && CustomCreativeTabRegistry.INSTANCE.isSearchable(selectedTab);
=======
        return CustomCreativeTabRegistry.INSTANCE.shouldHandleSearch(selectedTab);
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
    }

    private void refreshCustomSearch() {
        CreativeModeInventoryScreen.ItemPickerMenu menu = (CreativeModeInventoryScreen.ItemPickerMenu) ((AbstractContainerScreenAccessor) this).getInternalMenu();
        menu.items.clear();
        visibleTags.clear();
        String query = searchBox.getValue().toLowerCase(Locale.ROOT).trim();
        boolean tagSearch = query.startsWith("#");
        if (tagSearch)
            query = query.substring(1);

<<<<<<< HEAD
        for (ItemStack stack : selectedTab.getDisplayItems()) {
=======
<<<<<<< HEAD
        for (ItemStack stack : selectedTab.getDisplayItems()) {
=======
        for (ItemStack stack : selectedTab.getSearchTabDisplayItems()) {
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
            if (query.isEmpty() || matches(stack, query, tagSearch))
                menu.items.add(stack);
        }

<<<<<<< HEAD
=======
<<<<<<< HEAD
=======

>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
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
