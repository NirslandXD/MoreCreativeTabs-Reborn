package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.CreativeModeTabRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CreativeModeTabRegistry.class)
public abstract class ForgeCreativeTabRegistryMixin {

    @Shadow
    public static List<CreativeModeTab> getDefaultTabs() {
        return null;
    }

    @Inject(method = "getSortedCreativeModeTabs", at = @At("RETURN"), cancellable = true)
    private static void injectCustomTabs(CallbackInfoReturnable<List<CreativeModeTab>> cir) {
<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
        if (CustomCreativeTabRegistry.INSTANCE.sortedTabs().isEmpty())
            return;

>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
        cir.setReturnValue(CustomCreativeTabRegistry.INSTANCE.displayedTabs().stream()
                .filter(tab -> !getDefaultTabs().contains(tab))
                .toList());
    }

}
