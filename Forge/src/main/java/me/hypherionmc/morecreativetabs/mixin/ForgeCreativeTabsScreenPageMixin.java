package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.client.gui.CreativeTabsScreenPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CreativeTabsScreenPage.class)
public class ForgeCreativeTabsScreenPageMixin {

    @Inject(method = "getVisibleTabs", at = @At("RETURN"), cancellable = true)
    private void filterDisabledTabs(CallbackInfoReturnable<List<CreativeModeTab>> cir) {
        cir.setReturnValue(cir.getReturnValue().stream()
                .filter(tab -> !CustomCreativeTabRegistry.INSTANCE.isDisabled(tab))
                .toList());
    }

    @Inject(method = "isTop", at = @At("RETURN"), cancellable = true)
    private void injectIsTop(CreativeModeTab tab, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(tab.row() == CreativeModeTab.Row.TOP);
    }

    @Inject(method = "getColumn", at = @At("RETURN"), cancellable = true)
    private void injectGetColumn(CreativeModeTab tab, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(tab.column());
    }

}
