package me.hypherionmc.morecreativetabs.mixin;

import me.hypherionmc.morecreativetabs.client.impl.ForgeTabData;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CreativeModeTab.class)
public class ForgeCreateTabMixin implements ForgeTabData {

    @Unique
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 79b2a8a (initial commit)
    private int page_index = -1;

    @Override
    public int getPageIndex() {
        return page_index;
<<<<<<< HEAD
=======
=======
    private int pageIndex = -1;

    @Override
    public int getPageIndex() {
        return pageIndex;
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
    }

    @Override
    public void setPageIndex(int page) {
<<<<<<< HEAD
        this.page_index = page;
=======
<<<<<<< HEAD
        this.page_index = page;
=======
        this.pageIndex = page;
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
    }
}
