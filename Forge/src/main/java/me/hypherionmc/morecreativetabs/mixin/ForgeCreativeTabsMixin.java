package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.impl.ForgeTabData;
import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import me.hypherionmc.morecreativetabs.mixin.accessor.ForceCreativeTabAccessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.CreativeModeTabRegistry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
import org.spongepowered.asm.mixin.Unique;
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
import java.util.Locale;
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
import java.util.stream.Stream;

@Mixin(value = CreativeModeTabs.class, priority = 0)
public abstract class ForgeCreativeTabsMixin {

    @Shadow
    public static List<CreativeModeTab> allTabs() {
        return null;
    }

    @Shadow @Nullable private static CreativeModeTab.ItemDisplayParameters CACHED_PARAMETERS;

    @Inject(method = "streamAllTabs", at = @At("RETURN"), cancellable = true)
    private static void injectCustomTabs(CallbackInfoReturnable<Stream<CreativeModeTab>> cir) {
        if (!CustomCreativeTabRegistry.INSTANCE.sortedTabs().isEmpty())
            cir.setReturnValue(CustomCreativeTabRegistry.INSTANCE.sortedTabs().stream());
    }

    // Supplementaries crashes the game with our tabs, since they are not registered (they are fake tabs)
    // Work around to return the registered tabs only
    @Inject(method = "tabs", at = @At("RETURN"), cancellable = true)
    private static void injectTabsCompat(CallbackInfoReturnable<List<CreativeModeTab>> cir) {
<<<<<<< HEAD
        String thread = Thread.currentThread().getStackTrace()[3].getClassName();

        if (!thread.isEmpty() && thread.toLowerCase().contains("supplementaries") && thread.toLowerCase().contains("modcreativetabs")) {
=======
<<<<<<< HEAD
        String thread = Thread.currentThread().getStackTrace()[3].getClassName();

        if (!thread.isEmpty() && thread.toLowerCase().contains("supplementaries") && thread.toLowerCase().contains("modcreativetabs")) {
=======
        if (moreCreativeTabs$isSupplementariesCreativeTabsCall()) {
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
            cir.setReturnValue(BuiltInRegistries.CREATIVE_MODE_TAB.stream().filter(CreativeModeTab::shouldDisplay).toList());
        }
    }

<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
    @Unique
    private static boolean moreCreativeTabs$isSupplementariesCreativeTabsCall() {
        return StackWalker.getInstance().walk(frames -> frames
                .map(StackWalker.StackFrame::getClassName)
                .map(className -> className.toLowerCase(Locale.ROOT))
                .anyMatch(className -> className.contains("supplementaries") && className.contains("modcreativetabs")));
    }

>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
    @Inject(method = "getDefaultTab", at = @At("RETURN"), cancellable = true)
    private static void injectDefaultTab(CallbackInfoReturnable<CreativeModeTab> cir) {
        if (!allTabs().isEmpty())
            cir.setReturnValue(allTabs().get(0));
    }

    @Inject(method = "validate", at = @At("HEAD"), cancellable = true)
    private static void injectValidation(CallbackInfo ci) {
        ci.cancel();
<<<<<<< HEAD
        int TABS_PER_PAGE = 10;
=======
<<<<<<< HEAD
        int TABS_PER_PAGE = 10;
=======
        int tabsPerPage = 10;
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
        int count = 0;

        for (CreativeModeTab tab : CustomCreativeTabRegistry.INSTANCE.sortedTabs()) {

            ForgeTabData forgeTab = (ForgeTabData) tab;
            if (CreativeModeTabRegistry.getDefaultTabs().contains(tab)) {
                forgeTab.setPageIndex(0);
                continue;
            }

            final ForceCreativeTabAccessor itemGroupAccessor = (ForceCreativeTabAccessor) tab;
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 79b2a8a (initial commit)
            int pageIndex = count % TABS_PER_PAGE;
            forgeTab.setPageIndex((count / TABS_PER_PAGE));
            CreativeModeTab.Row row = pageIndex < (TABS_PER_PAGE / 2) ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM;
            itemGroupAccessor.setRow(row);
            itemGroupAccessor.setColumn(row == CreativeModeTab.Row.TOP ? pageIndex % TABS_PER_PAGE : (pageIndex - TABS_PER_PAGE / 2) % (TABS_PER_PAGE));
<<<<<<< HEAD
=======
=======
            int pageIndex = count % tabsPerPage;
            forgeTab.setPageIndex(count / tabsPerPage);
            CreativeModeTab.Row row = pageIndex < tabsPerPage / 2 ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM;
            itemGroupAccessor.setRow(row);
            itemGroupAccessor.setColumn(row == CreativeModeTab.Row.TOP ? pageIndex % tabsPerPage : (pageIndex - tabsPerPage / 2) % tabsPerPage);
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)

            count++;
        }

        record ItemGroupPosition(CreativeModeTab.Row row, int column, int page) { }
        var map = new HashMap<ItemGroupPosition, String>();

        for (CreativeModeTab tab : CustomCreativeTabRegistry.INSTANCE.sortedTabs()) {
            final ForgeTabData forgeTabData = (ForgeTabData) tab;
            final String displayName = tab.getDisplayName().getString();
            final var position = new ItemGroupPosition(tab.row(), tab.column(), forgeTabData.getPageIndex());
            final String existingName = map.put(position, displayName);

            if (existingName != null) {
                throw new IllegalArgumentException("Duplicate position: (%s) for item groups %s vs %s".formatted(position, displayName, existingName));
            }
        }
    }

    @Inject(method = "tryRebuildTabContents", at = @At("HEAD"))
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 79b2a8a (initial commit)
    private static void injectReload(FeatureFlagSet arg, boolean bl, HolderLookup.Provider arg2, CallbackInfoReturnable<Boolean> cir) {
        if (CustomCreativeTabRegistry.INSTANCE.isWasReloaded()) {
            CACHED_PARAMETERS = null;
            CustomCreativeTabRegistry.INSTANCE.setWasReloaded(false);
<<<<<<< HEAD
=======
=======
    private static void injectReload(FeatureFlagSet featureFlags, boolean hasPermissions, HolderLookup.Provider lookupProvider, CallbackInfoReturnable<Boolean> cir) {
        if (CustomCreativeTabRegistry.INSTANCE.consumeWasReloaded()) {
            CACHED_PARAMETERS = null;
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
        }
    }

}
