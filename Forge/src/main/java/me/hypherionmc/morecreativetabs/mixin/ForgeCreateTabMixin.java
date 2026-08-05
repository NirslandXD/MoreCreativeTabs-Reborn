package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.impl.ForgeTabData;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CreativeModeTab.class)
public class ForgeCreateTabMixin implements ForgeTabData {

    @Unique
    private int pageIndex = -1;

    @Override
    public int getPageIndex() {
        return pageIndex;
    }

    @Override
    public void setPageIndex(int page) {
        this.pageIndex = page;
    }
}
