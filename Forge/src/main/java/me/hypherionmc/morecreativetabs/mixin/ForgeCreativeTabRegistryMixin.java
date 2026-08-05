package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import me.hypherionmc.morecreativetabs.mixin.accessor.CreativeModeTabsAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.CreativeModeTabRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CreativeModeTabRegistry.class)
public abstract class ForgeCreativeTabRegistryMixin {

    @Inject(method = "getSortedCreativeModeTabs", at = @At("RETURN"), cancellable = true)
    private static void injectCustomTabs(CallbackInfoReturnable<List<CreativeModeTab>> cir) {
        // Keep the real Forge registry IDs before replacing its visible tab list.
        // Resource reloads use these IDs to resolve modded tab selectors.
        for (CreativeModeTab tab : cir.getReturnValue()) {
            ResourceLocation registryId = CreativeModeTabRegistry.getName(tab);
            CustomCreativeTabRegistry.INSTANCE.registerTabRegistryId(tab, registryId);
        }

        if (CustomCreativeTabRegistry.INSTANCE.sortedTabs().isEmpty())
            return;

        cir.setReturnValue(CustomCreativeTabRegistry.INSTANCE.displayedTabs().stream()
                .filter(tab -> !isDefaultTab(tab))
                .toList());
    }

    private static boolean isDefaultTab(CreativeModeTab tab) {
        return tab == CreativeModeTabsAccessor.getSearchTab()
                || tab == CreativeModeTabsAccessor.getHotbarTab()
                || tab == CreativeModeTabsAccessor.getInventoryTab()
                || tab == CreativeModeTabsAccessor.getOpBlockTab();
    }

}
