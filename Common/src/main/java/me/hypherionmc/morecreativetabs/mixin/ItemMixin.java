package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.tabs.CustomCreativeTabManager;
import me.hypherionmc.morecreativetabs.util.CreativeTabUtils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Arrays;

@Mixin(Item.class)
public class ItemMixin {

    @Shadow @Final @Nullable protected CreativeModeTab category;

    @Inject(method = "getItemCategory", at = @At("RETURN"), cancellable = true)
    private void injectCategory(CallbackInfoReturnable<CreativeModeTab> cir) {
        Item item = ((Item) (Object)this);
        CreativeModeTab oldTab = this.category;

        if (CustomCreativeTabManager.remappedItems.containsKey(item)) {
            String tab = CustomCreativeTabManager.remappedItems.get(item);
            Arrays.stream(CreativeModeTab.TABS)
                    .filter(t -> CreativeTabUtils.tabAliases(t).stream().anyMatch(CreativeTabUtils.selectorAliases(tab)::contains))
                    .findAny().ifPresent(cir::setReturnValue);
            return;
        }

        if (CustomCreativeTabManager.hiddenStacks.contains(item)) {
            cir.setReturnValue(null);
            return;
        }

        if (oldTab == null)
            return;

        if (CustomCreativeTabManager.isDisabled(oldTab)) {
            cir.setReturnValue(null);
            return;
        }

        CreativeTabUtils.replacementTab(oldTab.getRecipeFolderName()).ifPresent(t -> {
            boolean keepExisting = t.getKey().keepExisting || t.getValue().isEmpty()
                    || t.getValue().stream().anyMatch(CustomCreativeTabManager::isExistingSentinel);
            if (keepExisting) {
                if (!CustomCreativeTabManager.shouldKeepExistingItem(t.getKey(), new ItemStack(item)))
                    cir.setReturnValue(null);
                return;
            }

            if (t.getValue().stream().noneMatch(itemStack -> itemStack.getItem() == item))
                cir.setReturnValue(null);
        });
    }
}
