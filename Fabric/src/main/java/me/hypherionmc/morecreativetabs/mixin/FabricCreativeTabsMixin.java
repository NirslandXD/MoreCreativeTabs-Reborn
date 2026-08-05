package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import me.hypherionmc.morecreativetabs.mixin.accessor.CreativeModeTabsAccessor;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;

@Mixin(value = CreativeModeTabs.class, priority = 0)
public abstract class FabricCreativeTabsMixin {

    @Shadow
    public static List<CreativeModeTab> allTabs() {
        return null;
    }

    @Inject(method = "allTabs", at = @At("RETURN"), cancellable = true)
    private static void injectCustomTabs(CallbackInfoReturnable<List<CreativeModeTab>> cir) {
        if (!CustomCreativeTabRegistry.INSTANCE.sortedTabs().isEmpty())
            cir.setReturnValue(CustomCreativeTabRegistry.INSTANCE.sortedTabs());
    }

    @Inject(method = "getDefaultTab", at = @At("RETURN"), cancellable = true)
    private static void injectDefaultTab(CallbackInfoReturnable<CreativeModeTab> cir) {
        if (!allTabs().isEmpty())
            cir.setReturnValue(allTabs().get(0));
    }

    @Inject(method = "tabs", at = @At("RETURN"), cancellable = true)
    private static void injectDisplayedTabs(CallbackInfoReturnable<List<CreativeModeTab>> cir) {
        List<CreativeModeTab> displayedTabs = CustomCreativeTabRegistry.INSTANCE.displayedTabs();
        if (displayedTabs.isEmpty())
            return;

        // Supplementaries crashes the game with our fake tabs, since they are not registered.
        // For that compatibility path, fall back to registered/displayable tabs only.
        if (moreCreativeTabs$isSupplementariesCreativeTabsCall()) {
            cir.setReturnValue(CreativeModeTabsAccessor.getTabs().stream().filter(CreativeModeTab::shouldDisplay).toList());
            return;
        }

        cir.setReturnValue(displayedTabs);
    }

    @Unique
    private static boolean moreCreativeTabs$isSupplementariesCreativeTabsCall() {
        return StackWalker.getInstance().walk(frames -> frames
                .map(StackWalker.StackFrame::getClassName)
                .map(className -> className.toLowerCase(Locale.ROOT))
                .anyMatch(className -> className.contains("supplementaries") && className.contains("modcreativetabs")));
    }

    @Group(name = "rebuildContents", min = 1, max = 1)
    @Inject(method = {
            "tryRebuildTabContents(Lnet/minecraft/world/flag/FeatureFlagSet;Z)Z",
            "m_257580_(Lnet/minecraft/world/flag/FeatureFlagSet;Z)Z",
            "method_47330(Lnet/minecraft/class_7699;Z)Z"
    }, at = @At("HEAD"), remap = false, require = 0)
    private static void invalidate1193CreativeCache(CallbackInfoReturnable<Boolean> cir) {
        invalidateCreativeCache();
    }

    @Group(name = "rebuildContents", min = 1, max = 1)
    @Inject(method = {
            "tryRebuildTabContents(Lnet/minecraft/world/flag/FeatureFlagSet;ZLnet/minecraft/core/HolderLookup$Provider;)Z",
            "m_269226_(Lnet/minecraft/world/flag/FeatureFlagSet;ZLnet/minecraft/core/HolderLookup$Provider;)Z",
            "method_47330(Lnet/minecraft/class_7699;ZLnet/minecraft/class_7225$class_7874;)Z"
    }, at = @At("HEAD"), remap = false, require = 0)
    private static void invalidate1194CreativeCache(CallbackInfoReturnable<Boolean> cir) {
        invalidateCreativeCache();
    }

    @Unique
    private static void invalidateCreativeCache() {
        if (!CustomCreativeTabRegistry.INSTANCE.consumeWasReloaded())
            return;

        try {
            for (Field field : CreativeModeTabs.class.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()))
                    continue;

                Class<?> type = field.getType();
                if (!type.getName().equals("net.minecraft.world.flag.FeatureFlagSet")
                        && !type.getSimpleName().equals("ItemDisplayParameters"))
                    continue;

                field.setAccessible(true);
                field.set(null, null);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invalidate creative tab contents after reload", e);
        }
    }

}
